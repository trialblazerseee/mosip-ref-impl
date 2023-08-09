package io.mosip.kernel.smsserviceprovider.smarthub.test;

import io.mosip.kernel.core.notification.exception.InvalidNumberException;
import io.mosip.kernel.core.notification.model.SMSResponseDto;
import io.mosip.kernel.smsserviceprovider.smarthub.SMSServiceProviderBootApplication;
import io.mosip.kernel.smsserviceprovider.smarthub.constant.SmsPropertyConstant;
import io.mosip.kernel.smsserviceprovider.smarthub.dto.SmarthubRequestDto;
import io.mosip.kernel.smsserviceprovider.smarthub.dto.SmarthubResponseDto;
import io.mosip.kernel.smsserviceprovider.smarthub.dto.SmsServerResponseDto;
import io.mosip.kernel.smsserviceprovider.smarthub.impl.SMSServiceProviderImpl;
import io.mosip.kernel.smsserviceprovider.smarthub.util.HMACUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.sql.Timestamp;

import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { SMSServiceProviderBootApplication.class })
public class SmsServiceProviderTest {

	@Autowired
	SMSServiceProviderImpl service;

	@MockBean
	RestTemplate restTemplate;

	@Value("${mosip.kernel.sms.api}")
	String api;

	@Value("${mosip.kernel.sms.authkey}")
	String authkey;

	@Value("${mosip.kernel.sms.country.code}")
	String countryCode;

	@Value("${mosip.kernel.sms.sender}")
	String senderId;

	@Value("${mosip.kernel.sms.route}")
	String route;

	@Value("${mosip.kernel.sms.number.length}")
	String length;

	@Value("${mosip.kernel.sms.apiid}")
	String apiid;

	@Test
	public void sendSmsTest() {
		SmarthubResponseDto serverResponse = new SmarthubResponseDto();
		serverResponse.setData("SMS Sent with success");
		serverResponse.setMessage(" AIEG");
		serverResponse.setSuccess(true);

		when(restTemplate.postForEntity(Mockito.anyString(), Mockito.anyObject(), Mockito.eq(SmarthubResponseDto.class)))
				.thenReturn(new ResponseEntity<>(serverResponse, HttpStatus.OK));

		String contactNumber="92181351";
		String message="Votre MOSIP Pré-Enregistrment OTP est 9999999. Il est valide pour 5 minutes.";
		SMSResponseDto respocnce= service.sendSms(contactNumber,message);
		System.out.println(respocnce);
	}

//
//	@Test(expected = InvalidNumberException.class)
//	public void invalidContactNumberTest() {
//		service.sendSms("jsbchb", "hello your otp is 45373");
//	}
//
//	@Test(expected = InvalidNumberException.class)
//	public void contactNumberMinimumThresholdTest() {
//		service.sendSms("92181351", "hello your otp is 45373");
//	}
//
//	@Test(expected = InvalidNumberException.class)
//	public void contactNumberMaximumThresholdTest() {
//		service.sendSms("7897897458673484376", "hello your otp is 45373");
//	}
//
//	@Test
//	public void validGateWayTest() {
//		service.sendSms("8208356358", "hello your otp is 45373");
//	}
}