package com.server.client;

import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.server.util.YarUtil;

/**
 * server
 * @author wangliguang
 *
 */
@Controller
@RequestMapping(value = "yarServer")
public class YarServer {
	
	private static Logger logger = LogManager.getLogger(YarServer.class);

	/**
	 * server处理请求
	 * @param param yar参数体
	 * @return 
	 */
	public Object deal(Map param){
		
		logger.info("param="+param+" p="+param.get("p"));
		Map p =  (Map)((List) param.get("p")).get(0);		
		Object serviceClassName = p.get("service");//目前写死，如果愿意可以把key配置到配置文件，不过没有多大必要
		Object serviceMethodName = p.get("method");//目前写死，如果愿意可以把key配置到配置文件，不过没有多大必要
		Object paramClassName = p.get("paramClassName");//空的话默认map，传值的话不能是interface，只能是实现类，并且实现类内部包装体也必须是实现类，不能是interface
		Class clazz = null;
		if(serviceClassName==null||serviceClassName.toString().length()==0||serviceMethodName==null||serviceMethodName.toString().length()==0){			
			logger.error("service is null param="+param);
			return null;
		}		
		String serviceClass = serviceClassName.toString();
		try {
			Class service = Class.forName(serviceClass);
			if(paramClassName==null){//默认map，必须有值否则无法获取method
				clazz = Map.class;
			}else{
				clazz =  Class.forName(paramClassName.toString());
			}
			
			Method method = service.getMethod(serviceMethodName.toString(), clazz);

			Object returnValue = null;
			if(paramClassName==null){
				returnValue = method.invoke(service.newInstance(), (Map)p.get("args"));
			}else{
				Object paramObj = YarUtil.convertMap(clazz, (Map)p.get("args"));//目前认为方法必须有参数，并且只有一个参数
				returnValue = method.invoke(service.newInstance(), paramObj);
			}
				
			return returnValue;
		} catch (Exception e) {
			logger.error("",e);
		}
		return null;
	}
	

	
	/**
	 * 接受请求
	 * @param request
	 * @param response
	 */
	@RequestMapping(value = { "/requestServer" })
	public void requestServer(HttpServletRequest request, HttpServletResponse response) {
		OutputStream outputStream = null;
		
		Map resMap = new HashMap();
		//resMap.put("s", value);状态码后期可以server 和 client 约定后加上，暂时不填
		resMap.put("s", null);//状态码后期可以server 和 client 约定后根绝不同情况返回状态码加上，暂时不填
		
		//server接受请求
		StructMy my = null;
		try {
			my = YarUtil.inputToStructMy(request.getInputStream(), null);
			resMap.put("i", my.getBody().get("i"));	
		} catch (Exception e) {
			logger.error("inputToStructMy error");
			this.makeExceptionRes(e, response, resMap, my,outputStream);
			return;
		}	
		
		try {
					
			//处理调用
			Object result = this.deal(my.getBody());
			if(result==null){
				logger.error("deal error param="+my.getBody());				
				resMap.put("r", null);//异常无返回值
				resMap.put("o", "deal error");
				resMap.put("e", "deal error");
				my.setResBody(resMap);
				//server协议转换
				byte [] byteArray = YarUtil.transByteArr(my);
				outputStream = response.getOutputStream();
				outputStream.write(byteArray);		
				return;
			}

			resMap.put("r", result);
			resMap.put("o", "ok");
			resMap.put("e", null);
			my.setResBody(resMap);
			
			//server协议转换
			byte [] byteArray = YarUtil.transByteArr(my);
			outputStream = response.getOutputStream();
			outputStream.write(byteArray);					
		} catch (Exception e) {			
			logger.error("",e);
			this.makeExceptionRes(e, response, resMap, my,outputStream);
			return;
		} finally{
			try {
				outputStream.close();
			} catch (Exception e) {
				logger.error("",e);
			}
		}
	}
	
	private void makeExceptionRes(Exception e,HttpServletResponse response,Map resMap,StructMy my,OutputStream outputStream){
		
		if(my==null){
			my = new StructMy();
		}
		resMap.put("r", null);//异常无返回值
		resMap.put("o", e.getMessage());
		resMap.put("e", e.getStackTrace()); 
		my.setResBody(resMap);
		//server协议转换
		byte[] byteArray;
		try {
			byteArray = YarUtil.transByteArr(my);
			outputStream = response.getOutputStream();
			outputStream.write(byteArray);	
		} catch (Exception e1) {
			logger.error("",e1);
		}	
	}

}
