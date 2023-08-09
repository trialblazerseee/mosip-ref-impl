package io.mosip.kernel.smsserviceprovider.smarthub.dto;

import lombok.Data;

@Data
public class SmsVendorRequestDto {
	private String from;
	private String to;
	private String text;
}
