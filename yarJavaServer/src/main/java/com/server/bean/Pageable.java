package com.server.bean;

import org.msgpack.annotation.Message;


public class Pageable {
	private String pageNo;
	private String pageSize;	
	private int sort;
	
	public String getPageNo() {
		return pageNo;
	}
	public void setPageNo(String pageNo) {
		this.pageNo = pageNo;
	}
	public String getPageSize() {
		return pageSize;
	}
	public void setPageSize(String pageSize) {
		this.pageSize = pageSize;
	}
	public int getSort() {
		return sort;
	}
	public void setSort(int sort) {
		this.sort = sort;
	}
	
}
