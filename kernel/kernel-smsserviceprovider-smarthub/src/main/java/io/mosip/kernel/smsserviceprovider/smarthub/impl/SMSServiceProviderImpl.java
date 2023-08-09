/**
 * 
 */
package io.mosip.kernel.smsserviceprovider.smarthub.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.logger.logback.factory.Logfactory;
import io.mosip.kernel.smsserviceprovider.smarthub.dto.SmarthubRequestDto;
import io.mosip.kernel.smsserviceprovider.smarthub.dto.SmarthubResponseDto;
import io.mosip.kernel.smsserviceprovider.smarthub.util.HMACUtil;
import org.apache.http.protocol.HTTP;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import io.mosip.kernel.core.notification.exception.InvalidNumberException;
import io.mosip.kernel.core.notification.model.SMSResponseDto;
import io.mosip.kernel.core.notification.spi.SMSServiceProvider;
import io.mosip.kernel.core.util.StringUtils;
import io.mosip.kernel.smsserviceprovider.smarthub.constant.SmsExceptionConstant;
import io.mosip.kernel.smsserviceprovider.smarthub.constant.SmsPropertyConstant;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;

/**
 * @author Ritesh Sinha
 * @since 1.0.0
 */
@Component
public class SMSServiceProviderImpl implements SMSServiceProvider {

	@Autowired
	RestTemplate restTemplate;

	@Value("${mosip.kernel.sms.enabled:false}")
	boolean smsEnabled;

	@Value("${mosip.kernel.sms.country.code}")
	String countryCode;

	@Value("${mosip.kernel.sms.number.length}")
	int numberLength;

	@Value("${mosip.kernel.sms.api}")
	String api;

	@Value("${mosip.kernel.sms.sender}")
	String sender;

	@Value("${mosip.kernel.sms.password:null}")
	private String password;

	@Value("${mosip.kernel.sms.route:null}")
	String route;

	@Value("${mosip.kernel.sms.authkey:null}")
	String authkey;

	@Value("${mosip.kernel.sms.unicode:1}")
	String unicode;

	@Value("${mosip.kernel.sms.apiid}")
	String apiid;

	private String getInputData(String apiid, String message, String contactNumber, String timestamp) {
		StringBuilder builder = new StringBuilder();
		builder.append("id=").
				append(apiid).append("&")
				.append("message=")
				.append(message).append("&")
				.append("msisdn=")
				.append(contactNumber).append("&")
				.append("nonce=")
				.append(timestamp);
		return  builder.toString();
	}
	@Override
	public SMSResponseDto sendSms(String contactNumber, String message) {
		ObjectMapper mapper = new ObjectMapper();
		SMSResponseDto smsResponseDTO = new SMSResponseDto();

		try {
			validateInput(contactNumber);
			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			String timestampStr = timestamp.toInstant().toString();
			StringBuilder cnBbuilder = new StringBuilder();
			contactNumber=cnBbuilder.append(countryCode).append(contactNumber).toString();
			ScriptEngineManager manager = new ScriptEngineManager();
			ScriptEngine engine = manager.getEngineByName("JavaScript");

			String script1 = "var convert = function(data) { return encodeURI(data) }";
			engine.eval(script1);

			Invocable inv = (Invocable) engine;
			// call function from script file
			String encodedMessage =  (String) inv.invokeFunction("convert", message);
			String dataForHashing = getInputData(apiid,encodedMessage, contactNumber, timestampStr);
			String sign = HMACUtil.generateHMACSHA256Hash(dataForHashing, authkey);
			SmarthubRequestDto requestDto = getSMSRequest(apiid,contactNumber,message,timestampStr, sign);
			System.out.println("SMSPROVIDER : SMARTHUB : Request for SMS : " + contactNumber);

			ResponseEntity<SmarthubResponseDto> responseDto = restTemplate.postForEntity(api, requestDto, SmarthubResponseDto.class);
			System.out.println("SMSPROVIDER : SMARTHUB : Response : " + mapper.writeValueAsString(responseDto));

			if (responseDto.hasBody()) {
				if (responseDto.getBody().getSuccess()) {
					smsResponseDTO.setMessage(SmsPropertyConstant.SUCCESS_RESPONSE.getProperty());
					smsResponseDTO.setStatus("success");
				} else {
					smsResponseDTO.setMessage(responseDto.getBody().getMessage());
					smsResponseDTO.setStatus("failure");
				}
			}
		} catch (HttpClientErrorException | HttpServerErrorException e) {
			throw new RuntimeException(e.getResponseBodyAsString());
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return smsResponseDTO;
	}

	private SmarthubRequestDto getSMSRequest( String apiid, String contactNumber, String message, String timestamp, String sign) {
		SmarthubRequestDto requestDto = new SmarthubRequestDto();
		requestDto.setId(apiid);
		requestDto.setMsisdn(contactNumber);
		requestDto.setNonce(timestamp);
		requestDto.setMessage(message);
		requestDto.setSignature(sign);
		return requestDto;
	}

	private void validateInput(String contactNumber) {
		if (!StringUtils.isNumeric(contactNumber) || contactNumber.length() < numberLength
				|| contactNumber.length() > numberLength) {
			throw new InvalidNumberException(SmsExceptionConstant.SMS_INVALID_CONTACT_NUMBER.getErrorCode(),
					SmsExceptionConstant.SMS_INVALID_CONTACT_NUMBER.getErrorMessage() + numberLength
							+ SmsPropertyConstant.SUFFIX_MESSAGE.getProperty());
		}
	}
}