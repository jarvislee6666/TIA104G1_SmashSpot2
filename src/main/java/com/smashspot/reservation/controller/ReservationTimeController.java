package com.smashspot.reservation.controller;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smashspot.reservationtime.model.ReservationTimeService;

@Controller
@RequestMapping("/reservation")
public class ReservationTimeController {

    @Autowired
    private ReservationTimeService reservationTimeService;

    @GetMapping("/week")
    public String getWeeklyReservation(
        @RequestParam("stdmId") Integer stdmId,
        @RequestParam(value = "week", defaultValue = "0") Integer week,
        Model model
    ) {
        // 檢查 week 範圍
        if (week < 0) week = 0;
        if (week > 4) week = 4;

        // 設置今天日期
        Date today = new Date();
        model.addAttribute("today", today);
        
        // 設置28天後的日期限制
        Calendar futureLimit = Calendar.getInstance();
        futureLimit.setTime(today);
        futureLimit.add(Calendar.DAY_OF_MONTH, 28);
        model.addAttribute("futureLimit", futureLimit.getTime());
        
        // 計算本週日期
        Calendar startCal = Calendar.getInstance();
        startCal.setTime(today);
        startCal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        startCal.set(Calendar.HOUR_OF_DAY, 0);
        startCal.set(Calendar.MINUTE, 0);
        startCal.set(Calendar.SECOND, 0);
        startCal.set(Calendar.MILLISECOND, 0);
        
        // 調整到指定週
        startCal.add(Calendar.WEEK_OF_YEAR, week);
        Date startDate = startCal.getTime();
        model.addAttribute("startDate", startDate);
        
        // 計算週結束日期
        Calendar endCal = (Calendar) startCal.clone();
        endCal.add(Calendar.DAY_OF_WEEK, 6);
        endCal.set(Calendar.HOUR_OF_DAY, 23);
        endCal.set(Calendar.MINUTE, 59);
        endCal.set(Calendar.SECOND, 59);
        model.addAttribute("endDate", endCal.getTime());

        // 查詢預約資料
        List<Object[]> rawList = reservationTimeService.findReservationByStdmIdAndWeeks(
            stdmId, 
            week,
            week
        );

        // 確保有完整的七天資料
        List<Map<String, Object>> reservationList = new ArrayList<>();
        Calendar tempCal = (Calendar) startCal.clone();
        
        for (int i = 0; i < 7; i++) {
            // 尋找對應日期的資料
            Date currentDate = tempCal.getTime();
            Object[] matchingRow = findMatchingRow(rawList, currentDate);
            
            Map<String, Object> map = new HashMap<>();
            map.put("date", currentDate);
            
            if (matchingRow != null) {
                String rsvAvaStr = (String) matchingRow[3];
                String bookedStr = (String) matchingRow[4];
                map.put("rsvAva", rsvAvaStr);
                map.put("booked", bookedStr);
                map.put("leftover", computeLeftover(rsvAvaStr, bookedStr));
                map.put("stdmId", matchingRow[0]);
                map.put("stdmname", matchingRow[1]);
            } else {
                map.put("rsvAva", "xxxxxxxxxxxx");
                map.put("booked", "xxxxxxxxxxxx");
                map.put("leftover", "xxxxxxxxxxxx");
            }
            
            reservationList.add(map);
            tempCal.add(Calendar.DAY_OF_MONTH, 1);
        }

        model.addAttribute("stdmId", stdmId);
        model.addAttribute("week", week);
        model.addAttribute("reservationList", reservationList);

        return "back-end/client/reservationtime/court-reservation";
    }

    private Object[] findMatchingRow(List<Object[]> rawList, Date targetDate) {
        if (rawList == null || rawList.isEmpty()) return null;
        
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        
        for (Object[] row : rawList) {
            Date rowDate = (Date) row[2];
            cal1.setTime(targetDate);
            cal2.setTime(rowDate);
            
            if (cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH)) {
                return row;
            }
        }
        
        return null;
    }

    private String computeLeftover(String rsvAvaStr, String bookedStr) {
        if (rsvAvaStr == null || bookedStr == null) {
            return null;
        }
        if (rsvAvaStr.length() != 12 || bookedStr.length() != 12) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            char ava = rsvAvaStr.charAt(i);
            char bkd = bookedStr.charAt(i);

            if (ava == 'x') {
                sb.append('x');
            } else {
                int valAva = ava - '0';
                int valBkd = bkd - '0';
                int leftover = valAva - valBkd;
                if (leftover < 0) leftover = 0;
                sb.append(leftover);
            }
        }
        return sb.toString();
    }
}