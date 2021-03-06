package com.server.util;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.msgpack.MessagePack;
import org.msgpack.template.Template;
import org.msgpack.template.Templates;
import org.msgpack.unpacker.Unpacker;

import com.server.client.StructMy;
import com.server.templete.MyMapTemplate;
import com.server.templete.ObjectTemplate;
import com.server.bean.Pageable;
import com.server.bean.RequestGetAvailableFundList;

/** 
 * 工具类
 * @author wangliguang */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class YarUtil {

	private static Logger logger = LogManager.getLogger(YarUtil.class);

	
	/**
	 * server返回请求时，把包装类转换成byte数组
	 * @param structMy
	 * @return
	 */
	public static byte [] transByteArr(StructMy structMy) throws Exception{
		 
		 //byte[] bodayarr = YarUtil.mapToMsPack(structMy.getBody());
		 Object obj = structMy.getResBody().get("r");
		 if(obj!=null&&YarUtil.isObject(obj)){//是java对象
			 structMy.getResBody().put("r", YarUtil.convertBeanToMap(obj));			 		
		 }
		 byte[] bodayarr = YarUtil.objectToMsPack(structMy.getResBody());;		 
		 int body_len = bodayarr.length;
		 
		 byte [] all = new byte[90+body_len];
	
		 all = YarUtil.intToByteArray(structMy.getId(),all,0);
		 all = YarUtil.shortToByteArray(structMy.getVersion(),all,4);	
		 all = YarUtil.intToByteArray(structMy.getMagic_number(),all,6);		
		 all = YarUtil.intToByteArray(structMy.getReserved(),all,10);			 
		 all = YarUtil.getBytes(structMy.getProvider(),all,14);		
		 all = YarUtil.getBytes(structMy.getToken(),all,46);	
		 all = YarUtil.intToByteArray(body_len+8,all,78);
		 if(structMy.getPack()!=null){
			 all = YarUtil.getBytes(structMy.getPack().toCharArray(), all, 82);
		 }
		 
		 
		 for(int i=0;i<body_len;i++){
			 all[90+i] =  bodayarr[i];					 
		 }
		 /*System.out.println("----------------------");
		 for(int i=0;i<all.length;i++){
			 System.out.println(i+"="+Integer.toHexString((int)all[i]));
		 }
		 System.out.println("----------------------");*/
		return all;
		 
	 }
	
	/** server接受请求时，把input转化成StructMy包装类
	 * 
	 * @param in
	 * @return */
	public static StructMy inputToStructMy(InputStream in, Template template) {
		try {

			BufferedInputStream bufin = new BufferedInputStream(in);
			int buffSize = 1024;
			ByteArrayOutputStream out = new ByteArrayOutputStream(buffSize);
			byte[] temp = new byte[buffSize];
			int size = 0;
			while ((size = bufin.read(temp)) != -1) {
				out.write(temp, 0, size);
			}
			bufin.close();
			in.close();
			byte[] structMyArr = out.toByteArray();
			/*
			 * System.out.println("======================="); for(int
			 * i=0;i<structMyArr.length;i++){ //if(i>=90){
			 * //System.out.println(Integer.toHexString((int)structMyArr[i]));
			 * //} if(i>=90){ System.out.print((char)structMyArr[i]); } }
			 * System.out.println("=======================");
			 */
			StructMy my = new StructMy();
			my.setId(YarUtil.bytesToInt(structMyArr, 0));
			my.setVersion(YarUtil.byte2Short(structMyArr, 4));
			my.setMagic_number(YarUtil.bytesToInt(structMyArr, 6));
			my.setReserved(YarUtil.bytesToInt(structMyArr, 10));
			my.setProvider(YarUtil.bytesToCharArr(structMyArr, 14, 32));
			my.setToken(YarUtil.bytesToCharArr(structMyArr, 46, 32));
			my.setBody_len(YarUtil.bytesToInt(structMyArr, 78));
			my.setPack(YarUtil.bytesToString(structMyArr, 82, 8));

			int bodayArrayLength = my.getBody_len() - 8;// 发现yar把pack的长度也算进来了
			byte[] bodayClassArr = new byte[my.getBody_len()];
			for (int i = 0; i < structMyArr.length-90; i++) {
				bodayClassArr[i] = structMyArr[90 + i];
			}
			//my.setResBody(YarUtil.toBodayClas(bodayClassArr, template));
			my.setBody(YarUtil.toBodayClas(bodayClassArr, template));//因为接受的是请求，所以获取boday

			return my;

		} catch (Exception e) {
			logger.error("",e);
		}
		return null;
	}

	/** 把byte数组转化成Map
	 * 
	 * @param resBodayClass
	 * @param type
	 *            0:key-value格式（包括map 和 对象），1:list格式，2直接String
	 * @return */
	public static Map toBodayClas(byte[] resBodayClass, Template template) {
		MessagePack msgpack = new MessagePack();
		try {
			ByteArrayInputStream in = new ByteArrayInputStream(resBodayClass);
			Unpacker unpacker = msgpack.createUnpacker(in);
			Object object = null;
			if (template == null) {
				template = new MyMapTemplate(Templates.TString, ObjectTemplate.getInstance());
			}
			Map map = (Map)(unpacker.read(template));
			in.close();
			return map;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/** 递归处理map
	 * 
	 * @param rMap
	 * @param template
	 * @return */
	private static Map getMapInner(Map rMap, Template template) {
		Set keySet = rMap.keySet();
		Iterator iterator = keySet.iterator();
		String className = null;
		Object value;
		Map newRMap = new HashMap();
		Object key = null;
		String keyStr = null;
		while (iterator.hasNext()) {
			key = iterator.next();
			value = rMap.get(key);

			if (key == null) {
				className = ((String) value).trim();
			} else {
				keyStr = key.toString();
				if (keyStr.indexOf("\u0000") != -1 || keyStr.indexOf(" ") != -1) {
					int index = 0;
					if (keyStr.indexOf("\u0000", 1) != -1) {
						index = keyStr.indexOf("\u0000", 1);
					} else {
						index = keyStr.indexOf(" ", 1);
					}
					className = keyStr.substring(0, index);
				}
				if (value instanceof Map) {
					if (className == null) {
						newRMap.put(keyStr.trim(), getMapInner((Map) value, template));
					} else {
						newRMap.put(keyStr.replace(className, "").trim(), getMapInner((Map) value, template));
					}
				} else if (value instanceof List) {
					if (className == null) {
						newRMap.put(keyStr, getListInner((List) value, template));
					} else {
						newRMap.put(keyStr.replace(className, "").trim(), getListInner((List) value, template));
					}
				} else {
					if (className == null) {
						newRMap.put(keyStr.trim(), value);
					} else {
						newRMap.put(keyStr.replace(className, "").trim(), value);
					}
				}
			}
		}
		return newRMap;
	}

	/** 处理list
	 * 
	 * @param value
	 * @param template
	 * @return */
	private static List getListInner(List value, Template template) {

		Object listObj = null;
		for (int i = 0; i < value.size(); i++) {
			listObj = value.get(i);
			if (listObj instanceof Map) {
				value.set(i, getMapInner((Map) listObj, template));
			} else if (listObj instanceof List) {
				getListInner((List) listObj, template);
			}
		}
		return value;
	}

	/** 把map转化成byte
	 * 
	 * @param s
	 * @return */
	public static byte[] objectToMsPack(Object s) {
		MessagePack msgpack = new MessagePack();
		// Serialize
		try {
			//return msgpack.write(s, new MyMapTemplate(Templates.TString, ObjectTemplate.getInstance()));
			return msgpack.write(s);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/** 把java的char转成C的char代表的byte
	 * 
	 * @param data
	 * @return */
	public static byte getBytesByChar(char data) {
		return (byte) (data);
	}

	public static char getCharByByte(byte data) {
		return (char) (data);
	}

	/** java里的char数组转化成byte数组，用于传输
	 * 
	 * @param chars
	 * @param arr
	 * @param offsetin
	 * @return */
	public static byte[] getBytes(char[] chars, byte[] arr, int offsetin) {
		if (chars == null || chars.length == 0) {
			return arr;
		}
		for (int i = 0; i < chars.length; i++) {
			byte charBtye = YarUtil.getBytesByChar(chars[i]);
			arr[offsetin + i] = charBtye;

		}
		return arr;
	}

	/** 把byte数组转化成char数组
	 * 
	 * @param b
	 * @param offset
	 * @param length
	 * @return */
	public static char[] bytesToCharArr(byte[] b, int offset, int length) {
		char[] charArr = new char[length];
		for (int i = 0; i < length; i++) {
			charArr[i] = YarUtil.getCharByByte(b[i + offset]);
		}
		return charArr;
	}

	/** 把C的char表示的byte转成java的char再合并成string
	 * 
	 * @param b
	 * @param offset
	 * @param length
	 * @return */
	public static String bytesToString(byte[] b, int offset, int length) {
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < length; i++) {
			buffer.append(YarUtil.getCharByByte(b[offset + i]));
		}
		return buffer.toString();
	}

	/** int值转成4字节的byte数组
	 * 
	 * @param num
	 * @return */
	public static byte[] intToByteArray(int value, byte[] arr, int offsetin) {
		byte[] src = new byte[4];
		src[3] = (byte) ((value) & 0xFF);
		src[2] = (byte) ((value >> 8) & 0xFF);
		src[1] = (byte) ((value >> 16) & 0xFF);
		src[0] = (byte) ((value >> 24) & 0xFF);

		arr[offsetin] = src[0];
		arr[offsetin + 1] = src[1];
		arr[offsetin + 2] = src[2];
		arr[offsetin + 3] = src[3];
		return arr;
	}

	/** 把byte转化成int
	 * 
	 * @param src
	 * @param offset
	 * @return */
	public static int bytesToInt(byte[] src, int offset) {
		int value;
		value = (int) (((src[offset] & 0xFF) << 24) | ((src[offset + 1] & 0xFF) << 16)
		        | ((src[offset + 2] & 0xFF) << 8) | (src[offset + 3] & 0xFF));
		return value;
	}

	/** 把byte转化成short
	 * 
	 * @param b
	 * @param offset
	 * @return */
	public static short byte2Short(byte[] b, int offset) {
		return (short) (((b[offset] & 0xff) << 8) | (b[offset + 1] & 0xff));
	}

	/** 把short转化成byte
	 * 
	 * @param s
	 * @param arr
	 * @param offsetin
	 * @return */
	public static byte[] shortToByteArray(short s, byte[] arr, int offsetin) {
		for (int i = 0; i < 2; i++) {
			int offset = (2 - 1 - i) * 8;
			arr[offsetin + i] = (byte) ((s >>> offset) & 0xff);
		}
		return arr;
	}

	/**
	 * 把map内所有obj转换成map格式
	 * @param map
	 * @return
	 */
	public static Map convertMapBean(Map map) throws Exception{
		Iterator iterator = map.keySet().iterator();
		while(iterator.hasNext()){
			Object key = iterator.next();
			Object value = map.get(key);
			if(YarUtil.isObject(value)){
				map.put(key, YarUtil.convertBeanToMap(value));
			}else if(value instanceof Map){
				map.put(key, YarUtil.convertMapBean((Map)value));
			}else if(value instanceof List){
				List valueList = (List)value;
				YarUtil.convertListBean(valueList);				
			}
		}		
		return map;
	}
	
	/**
	 * 把list里的obj转换成map
	 * @param list
	 * @return
	 */
	public static void convertListBean(List valueList) throws Exception{
		for(int i=0;i<valueList.size();i++){
			Object value = valueList.get(i);
			
			if(YarUtil.isObject(value)){
				valueList.set(i, YarUtil.convertBeanToMap(value));
			}else if(value instanceof Map){
				valueList.set(i, YarUtil.convertMapBean((Map)value));;
			}else if(value instanceof List){
				YarUtil.convertListBean((List)value);		
			}			
		}
	}
	/**
	 * obj转换map
	 * @param bean
	 * @return
	 * @throws IntrospectionException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public static Map convertBeanToMap(Object bean)
	            throws IntrospectionException, IllegalAccessException, InvocationTargetException {	
	        Class type = bean.getClass();

	        Map returnMap = new HashMap();      
	        BeanInfo beanInfo = Introspector.getBeanInfo(type);

	        PropertyDescriptor[] propertyDescriptors =  beanInfo.getPropertyDescriptors();
	        for (int i = 0; i< propertyDescriptors.length; i++) {
	            PropertyDescriptor descriptor = propertyDescriptors[i];
	            String propertyName = descriptor.getName();
	            if (!propertyName.equals("class")) {
	                Method readMethod = descriptor.getReadMethod();
	                Object value = readMethod.invoke(bean, new Object[0]);
	                if(YarUtil.isObject(value)){
	                	returnMap.put(propertyName, YarUtil.convertBeanToMap(value));
	                }else if(value!=null){
	                	returnMap.put(propertyName, value);
	                }	                
	            }
	        }
	        return returnMap;
	}
	 
	/**
	 * 判断是否是java obj
	 * @param value
	 * @return
	 */
	public static boolean isObject(Object value){
		if (value==null ||  value.getClass().isArray() || value instanceof Date ||value instanceof Boolean || value instanceof Integer || value instanceof String || value instanceof Float || value instanceof List || value instanceof Set || value instanceof Map) { // boolean 
        	return false;
	    } else {//这个对象是个object
	    	return true;
	    }
	}
	
	/**
	 * map转对象
	 * @param type
	 * @param map
	 * @return
	 * @throws IntrospectionException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws InvocationTargetException
	 */
	public static Object convertMap(Class type, Map map) 
            throws IntrospectionException, IllegalAccessException, 
            InstantiationException, InvocationTargetException { 
        BeanInfo beanInfo = Introspector.getBeanInfo(type); // 获取类属性 
        Object obj = type.newInstance(); // 创建 JavaBean 对象 

        // 给 JavaBean 对象的属性赋值 
        PropertyDescriptor[] propertyDescriptors =  beanInfo.getPropertyDescriptors(); 
        for (int i = 0; i< propertyDescriptors.length; i++) { 
            PropertyDescriptor descriptor = propertyDescriptors[i]; 
            String propertyName = descriptor.getName(); 

            if (map.containsKey(propertyName)) { 
                // 下面一句可以 try 起来，这样当一个属性赋值失败的时候就不会影响其他属性赋值。 
            	Object value = null;
            	Class methodParamClass = descriptor.getWriteMethod().getParameterTypes()[0];
            	if(map.get(propertyName) instanceof Map){
            		value = convertMap(methodParamClass, (Map)map.get(propertyName)) ; 
            	}else{
            		value = map.get(propertyName); 
            	}
            	logger.info("value="+value+" obj="+obj+" method paramType="+methodParamClass+" value type="+value.getClass()+" propertyName="+propertyName+" method="+descriptor.getWriteMethod().getName());
            	if(value.getClass().toString().equals("class java.lang.Long")&&(methodParamClass.toString().equals("class java.lang.Integer")||methodParamClass.toString().equals("int"))){
            		//由于masgpack int long 不分所以加这个处理            		
            		descriptor.getWriteMethod().invoke(obj, Integer.parseInt(value.toString()));             		
            	}else if(value.getClass().toString().equals("class java.lang.Long")&&methodParamClass.toString().equals("class java.util.Date")){
            		descriptor.getWriteMethod().invoke(obj, new Date(Long.parseLong(value.toString()))); 
            	}else if(value.getClass().toString().equals("class java.lang.Double")&&(methodParamClass.toString().equals("class java.lang.Float")||methodParamClass.toString().equals("float"))){
            		descriptor.getWriteMethod().invoke(obj, Float.parseFloat(value.toString())); 
            	}else{
            		descriptor.getWriteMethod().invoke(obj, value); 
            	}
            	
                
            } 
        } 
        logger.info("obj="+obj);
        return obj; 
    } 

	public static void main(String []args){

		Pageable pageable = new Pageable();
		pageable.setPageNo("1");
		pageable.setPageSize("1");
		pageable.setSort(1);		
		RequestGetAvailableFundList fundlist = new RequestGetAvailableFundList();
		//fundlist.setCondition(condition);
		fundlist.setPageable(pageable);
		fundlist.setRequestDatetime(new Date(new Date().getTime()));
		try {
			Map map = YarUtil.convertBeanToMap(fundlist);
			System.out.println(map);
			
			Object obj = YarUtil.convertMap(fundlist.getClass(), map);
			System.out.println(YarUtil.convertBeanToMap(obj));
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
}
