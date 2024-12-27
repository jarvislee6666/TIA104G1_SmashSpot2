package com.smashspot.coupon.model;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class CouponService {

	@Autowired
	CouponRepository repository;
	
	@Autowired
    private SessionFactory sessionFactory;
	
	public void addCoupon(CouponVO couponVO) {
		repository.save(couponVO);
	}

	public void updateCoupon(CouponVO couponVO) {
		repository.save(couponVO);
	}

	public CouponVO getOneCoupon(Integer copid) {
		Optional<CouponVO> optional = repository.findById(copid);
		return optional.orElse(null);  // public T orElse(T other) : 如果值存在就回傳其值，否則回傳other的值
	}
	
	public void deleteCoupon(Integer copid) {
		if (repository.existsById(copid))
			repository.deleteById(copid);
	}

	public List<CouponVO> getAll() {
		return repository.findAll();
	}

}