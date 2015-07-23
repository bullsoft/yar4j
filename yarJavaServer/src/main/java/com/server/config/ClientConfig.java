package com.server.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.server.util.StringUtil;

public class ClientConfig {

	/** default is: 0x80DFEC60 该值在Client与Server都应该保持一致。否则视为不合法的数据 */
	public static int magicNumber;
	/** 序列化方式（Json，Mspack，PHP）目前只实现了Mspack */
	public static String pack;
	/** reqeust from who，与server约定好 */
	public static String provider;
	/** 保留字段 */
	public static int reserved;
	/** request token, used for authentication */
	public static String token;
	/** protocl version */
	public static short version;

	static { // 初始化
		try {
			Properties prop = new Properties();
			InputStream in = ClientConfig.class.getResourceAsStream("/yarserver.properties");
			prop.load(in);
			String marg = prop.getProperty("magicNumber");
			magicNumber = (marg == null ? 0 : (Integer.parseInt(marg)));

			String packStr = prop.getProperty("pack");
			pack = (StringUtil.isNull(packStr) ? null : packStr.trim());

			String providerStr = prop.getProperty("provider");
			provider = (StringUtil.isNull(providerStr) ? null : providerStr.trim());

			String reservedStr = prop.getProperty("reserved");
			reserved = (StringUtil.isNull(reservedStr) ? 0 : (Integer.parseInt(reservedStr)));

			String tokenStr = prop.getProperty("token");
			token = (StringUtil.isNull(tokenStr) ? null : tokenStr.trim());

			String versionStr = prop.getProperty("version");
			version = (StringUtil.isNull(versionStr) ? 0 : Short.parseShort(versionStr));

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
