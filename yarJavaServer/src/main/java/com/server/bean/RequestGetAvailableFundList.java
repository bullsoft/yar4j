package com.server.bean;

import java.util.Date;

import org.msgpack.annotation.Message;


public class RequestGetAvailableFundList {
	private String condition;
	private Date requestDatetime;
	private Pageable pageable;
	
	public String getCondition() {
		return condition;
	}
	public void setCondition(String condition) {
		this.condition = condition;
	}
	public Date getRequestDatetime() {
		return requestDatetime;
	}
	public void setRequestDatetime(Date requestDatetime) {
		this.requestDatetime = requestDatetime;
	}
	public Pageable getPageable() {
		return pageable;
	}
	public void setPageable(Pageable pageable) {
		this.pageable = pageable;
	}
	
	
}
