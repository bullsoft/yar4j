package com.yarclient.client;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.yarclient.util.YarUtil;

public class StructMy {

	 private int id;// transaction id
	 private short version;// protocl version
	 private int magic_number;// default is: 0x80DFEC60
	 private int reserved;
	 private char[] provider;//[32] reqeust from who *********************************
	 private char[] token;//[32] request token, used for authentication******************************
	 private int body_len;// request body len
	 private String pack;
	 private Map body;
	 
	 private Map resBody;

	 public static void main(String []args){
		 System.out.println("-----------------");
		 System.out.println("\u0000");
		 System.out.println(JSON.toJSONString(" "));
		 System.out.println("-----------------");
	 }
	 public byte [] transByteArr(){
		 
		 byte[] bodayarr = YarUtil.mapToMsPack(this.body);
		 
		 this.body_len = bodayarr.length;
		 byte [] all = new byte[90+body_len];
	
		 all = YarUtil.intToByteArray(this.id,all,0);
		 all = YarUtil.shortToByteArray(this.version,all,4);	
		 all = YarUtil.intToByteArray(this.magic_number,all,6);		
		 all = YarUtil.intToByteArray(this.reserved,all,10);			 
		 all = YarUtil.getBytes(provider,all,14);		
		 all = YarUtil.getBytes(token,all,46);	
		 all = YarUtil.intToByteArray(this.body_len+8,all,78);
		 if(pack!=null){
			 all = YarUtil.getBytes(pack.toCharArray(), all, 82);
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
	 
	public int getId() {
		return id;
	}
	public Map getResBody() {
		return resBody;
	}

	public void setResBody(Map resBody) {
		this.resBody = resBody;
	}

	public void setId(int id) {
		this.id = id;
	}
	public short getVersion() {
		return version;
	}
	public void setVersion(short version) {
		this.version = version;
	}
	public int getMagic_number() {
		return magic_number;
	}
	public void setMagic_number(int magic_number) {
		this.magic_number = magic_number;
	}
	public int getReserved() {
		return reserved;
	}
	public void setReserved(int reserved) {
		this.reserved = reserved;
	}
	public char[] getProvider() {
		return provider;
	}
	public void setProvider(char[] provider) {
		this.provider = provider;
	}
	public char[] getToken() {
		return token;
	}
	public void setToken(char[] token) {
		this.token = token;
	}
	public int getBody_len() {
		return body_len;
	}
	public void setBody_len(int body_len) {
		this.body_len = body_len;
	}
	public String getPack() {
		return pack;
	}
	public void setPack(String pack) {
		this.pack = pack;
	}
	public Map getBody() {
		return body;
	}

	public void setBody(Map body) {
		this.body = body;
	}
	 
	 

}
