package com.server.util;

public class StringUtil {

	public static boolean isNull(String s){
		if(s==null||s.length()==0){
			return true;
		}
		return false;
	}
}
