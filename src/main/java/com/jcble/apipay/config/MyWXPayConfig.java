package com.jcble.apipay.config;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import com.github.wxpay.sdk.WXPayConfig;

public class MyWXPayConfig implements WXPayConfig {
	
	private String appid;
	
	private String mchid;
	
	private String key;
	
	private String notifyUrl;
	
	private byte[] certData;
	
	public MyWXPayConfig(String appid, String mchid, String key, String notifyUrl) {
		this.appid = appid;
		this.mchid = mchid;
		this.key = key;
		this.notifyUrl = notifyUrl;
	}

	public MyWXPayConfig(String certPath) throws Exception {
		File file = new File(certPath);
		InputStream certStream = new FileInputStream(file);
		this.certData = new byte[(int) file.length()];
		certStream.read(this.certData);
		certStream.close();
	}
	
	public String getNotifyUrl() {
		return notifyUrl;
	}
	
	public String getSpBillCreateIp() {
		return "116.62.67.31";
	}

	@Override
	public String getAppID() {
		return appid;
	}

	@Override
	public String getMchID() {
		return mchid;
	}

	@Override
	public String getKey() {
		return key;
	}

	public InputStream getCertStream() {
		ByteArrayInputStream certBis = new ByteArrayInputStream(this.certData);
		return certBis;
	}

	@Override
	public int getHttpConnectTimeoutMs() {
		return 8000;
	}

	@Override
	public int getHttpReadTimeoutMs() {
		return 10000;
	}

}
