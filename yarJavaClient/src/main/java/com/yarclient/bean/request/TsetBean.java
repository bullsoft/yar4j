package com.yarclient.bean.request;

import java.util.Map;


public class TsetBean {
	
	private String aa;
	private int bb;
	private Map map;
	private TsetBean test;
	
	
	public TsetBean getTest() {
		return test;
	}
	public void setTest(TsetBean test) {
		this.test = test;
	}
	public Map getMap() {
		return map;
	}
	public void setMap(Map map) {
		this.map = map;
	}
	public String getAa() {
		return aa;
	}
	public void setAa(String aa) {
		this.aa = aa;
	}
	public int getBb() {
		return bb;
	}
	public void setBb(int bb) {
		this.bb = bb;
	}
	
	
}
