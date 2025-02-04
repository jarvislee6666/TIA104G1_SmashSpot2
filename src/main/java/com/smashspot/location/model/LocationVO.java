package com.smashspot.location.model;

import java.sql.Timestamp;
import java.util.Base64;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.smashspot.stadium.model.StadiumVO;






@Entity
@Table(name = "location")
public class LocationVO implements java.io.Serializable {
 private static final long serialVersionUID = 1L;
 
 @OneToMany(mappedBy = "locationVO", cascade = CascadeType.ALL)   // "stadium" 必須對應 ReservationTimeVO 中的 @ManyToOne 的變數名稱
 @JsonIgnore
 private Set<StadiumVO> stadium; 
 
 
 public Set<StadiumVO> getStadium() {
  return this.stadium;
 }

 public void setStadium(Set<StadiumVO> stadium) {
  this.stadium = stadium;
 }

 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 @Column(name = "loc_id", updatable = false) 
 private Integer locId;//loc_id
 
 @Column(name = "region")
 private String region;//region

 public Integer getLocId() {
  return locId;
 }

 public void setLocId(Integer locId) {
  this.locId = locId;
 }

 public String getRegion() {
  return region;
 }

 public void setRegion(String region) {
  this.region = region;
 }
 
 

}
