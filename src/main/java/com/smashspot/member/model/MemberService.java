// 定義套件路徑，表明這個類別屬於會員相關的模組
package com.smashspot.member.model;

// 引入必要的Java和Spring框架相關類別
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 使用@Service註解標記這是一個服務層元件，名稱為"memberService"
@Service("memberService")
public class MemberService {
    
    // 注入資料訪問層的Repository，用於操作資料庫
    @Autowired
    MemberRepository repository;
    
    // 注入郵件服務，用於發送系統郵件
    @Autowired
    private EmailService emailService;
    
    // 注入Redis服務，用於處理快取和臨時數據存儲
    @Autowired
    private RedisService redisService;
    
    // 注入Hibernate的SessionFactory，用於複雜查詢
    @Autowired
    private SessionFactory sessionFactory;
    
    // 創建日誌記錄器實例，用於記錄系統運行狀態和錯誤信息
    private static final Logger logger = LoggerFactory.getLogger(MemberService.class);
    
    // 新增會員方法，使用@Transactional確保資料操作的原子性
    @Transactional
    public void addMember(MemberVO memberVO) {
        // 記錄開始新增會員的日誌
        logger.info("開始新增會員: {}", memberVO.getAccount());
        try {
            // 保存會員資料到資料庫
            repository.save(memberVO);
            // 記錄會員新增成功的日誌
            logger.info("會員新增成功: {}", memberVO.getAccount());
        } catch (Exception e) {
            // 如果發生錯誤，記錄錯誤日誌並重新拋出異常
            logger.error("會員新增失敗", e);
            throw e;
        }
    }
    
    
    @Transactional
    public void updateMember(MemberVO memberVO) {
        try {
            // 記錄更新前的資料狀態
            logger.info("開始更新會員資料，會員ID: {}", memberVO.getMemid());
            logger.debug("更新的會員資料: {}", memberVO);

            // 檢查必要欄位
            if (memberVO.getMemid() == null) {
                throw new IllegalArgumentException("會員ID不能為空");
            }

            // 驗證資料格式
            if (!isValidPhoneNumber(memberVO.getPhone())) {
                throw new IllegalArgumentException("手機號碼格式不正確");
            }

            if (!isValidEmail(memberVO.getEmail())) {
                throw new IllegalArgumentException("Email格式不正確");
            }

            // 保存資料
            repository.save(memberVO);
            
            logger.info("會員資料更新成功");
        } catch (Exception e) {
            logger.error("會員資料更新失敗", e);
            throw new RuntimeException("更新會員資料時發生錯誤: " + e.getMessage());
        }
    }

    // 驗證方法
    private boolean isValidPhoneNumber(String phone) {
        return phone != null && phone.matches("^09\\d{8}$");
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }
    
    
    
    
    // 根據會員ID查詢單一會員資料
    public MemberVO getOneMember(Integer memid) {
        // 使用Optional包裝查詢結果，避免空指針異常
        Optional<MemberVO> optional = repository.findById(memid);
        // 如果找到會員返回會員對象，否則返回null
        return optional.orElse(null);
    }
    
    // 查詢所有會員資料
    public List<MemberVO> getAll() {
        // 返回所有會員列表
        return repository.findAll();
    }

    // 待實現的方法，目前返回null
    public MemberVO getRepository() {
        return null;
    }

    // 根據帳號查詢會員資料
    public MemberVO findByAccount(String account) {
        return repository.findByAccount(account);
    }

    // 根據電子郵件查詢會員資料
    public MemberVO findByEmail(String email) {
        return repository.findByEmail(email);
    }
    
    // 根據手機號碼查詢會員資料
    public MemberVO findByPhone(String phone) {
        return repository.findByPhone(phone);
    }
    
    // 會員登入驗證方法
    public MemberVO login(String account, String password) {
        // 先根據帳號查詢會員
        MemberVO mem = repository.findByAccount(account);
        // 檢查會員是否存在且密碼是否匹配
        if (mem != null && mem.getPassword().equals(password)) {
            return mem;
        }
        // 驗證失敗返回null
        return null;
    }

    /**
     * 根據 memberId 查詢 MemberVO 並返回 name
     * @param memberId 成員 ID
     * @return Optional 包裝的名稱
     */
    public Optional<String> getMemberNameById(Integer memberId) {
        try {
            return repository.findById(memberId)
                    .map(MemberVO::getName);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid memberId format. Must be an integer.");
        }
    }

    //沃寯添加
    public List<MemberVO> getAll(Map<String, String[]> map) {
        String name = null;
        String status = null;  // 保持為 String 類型
        
        if (map != null && !map.isEmpty()) {
            if (map.containsKey("name")) {
                name = map.get("name")[0];
                if (name.trim().isEmpty()) {
                    name = null;
                }
            }
            
            if (map.containsKey("status")) {
                status = map.get("status")[0];  // 直接使用字串值
                if (status.trim().isEmpty()) {
                    status = null;
                }
            }
        }
        
        List<MemberVO> result = repository.findByConditions(name, status);
        return result;
    }
    
}