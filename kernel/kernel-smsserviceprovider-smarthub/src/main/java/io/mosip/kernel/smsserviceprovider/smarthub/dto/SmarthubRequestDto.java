package io.mosip.kernel.smsserviceprovider.smarthub.dto;

import lombok.Data;

@Data
public class SmarthubRequestDto {
	private String id;
	private String message;
	private String msisdn;
	private String nonce;
	private String signature;
}
