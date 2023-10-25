package com.tripyle.common.service;

import com.tripyle.common.exception.BadRequestException;
import com.tripyle.common.exception.ServerErrorException;
import com.tripyle.common.model.dto.HttpRes;
import com.tripyle.common.model.dto.SmsDto;
import com.tripyle.config.EnvConfig;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

@DependsOn("env")
@Service
@RequiredArgsConstructor
public class SmsService {

    RestTemplate restTemplate;

    @Autowired
    public SmsService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    private static final String API_KEY = EnvConfig.smsApiKey();
    private static final String ACCESS_KEY = EnvConfig.smsAccessKey();
    private static final String SECRET_KEY = EnvConfig.smsSecretKey();
    private static final String OUTGOING_PHONE_NUM = EnvConfig.smsOutgoingPhoneNum();
    private static final String SMS_URI = String.format("/sms/v2/services/%s/messages", API_KEY);
    private static final String SMS_URL = "https://sens.apigw.ntruss.com" + SMS_URI;

    public ResponseEntity<HttpRes<String>> sendSms(String incomingPhoneNum, String messageContent) {
        String timestamp = String.valueOf(System.currentTimeMillis());

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("x-ncp-apigw-timestamp", timestamp);
        headers.set("x-ncp-iam-access-key", ACCESS_KEY);
        headers.set("x-ncp-apigw-signature-v2", createSignature(SMS_URI, timestamp)); // Secret Key 암호화

        List<SmsDto.IncomingNumDto> incomingNums = new ArrayList<>();
        incomingNums.add(SmsDto.IncomingNumDto.builder()
                .to(incomingPhoneNum)
                .build());

        SmsDto.SmsRequestDto request = SmsDto.SmsRequestDto.builder()
                .type("SMS")
                .contentType("COMM")
                .countryCode("82")
                .from(OUTGOING_PHONE_NUM)
                .content(messageContent)
                .incomingNums(incomingNums)
                .build();

        HttpEntity<SmsDto.SmsRequestDto> requestHttpEntity = new HttpEntity<>(request, headers);
        try {
            SmsDto.SmsResponseDto response = restTemplate.postForObject(SMS_URL, requestHttpEntity, SmsDto.SmsResponseDto.class);
            if(response == null || response.getStatusCode() != HttpStatus.ACCEPTED.value()){
                throw new ServerErrorException();
            }
        }
        catch (RestClientException e) {
            throw new ServerErrorException();
        }
        HttpRes<String> httpRes = new HttpRes<>(incomingPhoneNum + "로 인증번호가 전송되었습니다.");
        return new ResponseEntity<>(httpRes, HttpStatus.OK);
    }

    public String createSignature(String uri, String timestamp) {
        String message = "POST " + uri + "\n" + timestamp + "\n" + ACCESS_KEY;

        byte[] bytes;
        bytes = SECRET_KEY.getBytes(StandardCharsets.UTF_8);

        SecretKeySpec signingKey = new SecretKeySpec(bytes, "HmacSHA256");

        Mac mac;
        try {
            mac = Mac.getInstance("HmacSHA256");
        }
        catch (NoSuchAlgorithmException e) {
            throw new BadRequestException();
        }

        try {
            mac.init(signingKey);
        }
        catch (InvalidKeyException e) {
            throw new BadRequestException();
        }

        byte[] rawHmac;
        rawHmac = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));

        return Base64.encodeBase64String(rawHmac);
    }
}
