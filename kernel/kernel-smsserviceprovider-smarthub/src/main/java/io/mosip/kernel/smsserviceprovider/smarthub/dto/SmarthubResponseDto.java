package io.mosip.kernel.smsserviceprovider.smarthub.dto;

import lombok.Data;

@Data
public class SmarthubResponseDto {
	/**
	 * Status for request.
	 */
	private Boolean success;

	/**
	 * Response message
	 */
	private String message;

	/**
	 * Response code.
	 */
	private String data;
}
