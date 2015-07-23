package com.server.bean;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;

import com.server.bean.response.TestRes;
import com.server.util.YarUtil;

public class RequestUser {
	
	private static Logger logger = LogManager.getLogger(RequestUser.class);

	public String req1(Map map){
		BeanNameAware b = null;
		b.setBeanName("");
		InstantiationAwareBeanPostProcessorAdapter    a = null;
		return "111111111";
	}
	public int req2(Map map){
		return 4;
	}
	public Map req3(Map map){
		return map;
	}
	public TestRes req4(Map map){
		TestRes s = new TestRes();
		s.setA(10);
		return s;
	}
	public Map req5(RequestGetAvailableFundList fundlist){

		try {
			logger.info("date="+fundlist.getRequestDatetime());
			Map map = YarUtil.convertBeanToMap(fundlist);
			logger.info("map requestDatetime="+map.get("requestDatetime"));
			return map;
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IntrospectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	public static void main(String []args){
		
		System.out.println(Long.class.toString().equals("class java.lang.Long"));
		System.out.println(Integer.class.toString().equals("class java.lang.Integer"));
		System.out.println(int.class.toString());
		
		Object c = new HashMap();
		Map a = new HashMap();
		HashMap b = new HashMap();
		if(a instanceof Map){
			System.out.println("111111111");
		}
		if(b instanceof Map){
			System.out.println("22222222");
		}
		if(c instanceof Map){
			System.out.println("22222222");
		}
		
	}
	
}
