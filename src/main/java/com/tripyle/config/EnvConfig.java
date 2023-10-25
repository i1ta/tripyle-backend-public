package com.tripyle.config;

import lombok.Data;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Component("env")
public class EnvConfig {
    private static EnvProperties envProperties;

    @Autowired
    public EnvConfig(EnvProperties envProperties) {
        EnvConfig.envProperties = envProperties;
    }

    public static String jwtSecret() {
        return envProperties.getJwtSecret();
    }
    public static String smsApiKey() {
        return envProperties.getSms().getApiKey();
    }
    public static String smsAccessKey() {
        return envProperties.getSms().getAccessKey();
    }
    public static String smsSecretKey() {
        return envProperties.getSms().getSecretKey();
    }
    public static String smsOutgoingPhoneNum() {
        return envProperties.getSms().getOutgoingPhoneNum();
    }

    public static String naverLoginClientId() {
        return envProperties.getNaverLogin().getClientId();
    }
    public static String naverLoginClientSecret() {
        return envProperties.getNaverLogin().getClientSecret();
    }

    @Component
    @ConfigurationProperties(prefix="env")
    @Data
    public static class EnvProperties {
        private String jwtSecret;
        private Sms sms;
        private NaverLogin naverLogin;

        @Data
        private static class Sms {
            private String apiKey;
            private String accessKey;
            private String secretKey;
            private String outgoingPhoneNum;
        }

        @Data
        private static class NaverLogin {
            private String clientId;
            private String clientSecret;
        }
    }
}
