package com.jcble.apipay.dto;

public class Response extends BaseResponse {

	private Object payload;

	private Object meta;

	public Object getPayload() {
		return payload;
	}

	public void setPayload(Object payload) {
		this.payload = payload;
	}

	public Object getMeta() {
		return meta;
	}

	public void setMeta(Object meta) {
		this.meta = meta;
	}

}
