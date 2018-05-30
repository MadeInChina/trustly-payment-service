package org.trustly.paymentservice.configuration;

import java.net.URISyntaxException;
import java.security.Security;
import org.apache.commons.lang3.ArrayUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.trustly.paymentService.trustly.NotificationHandler;
import org.trustly.paymentService.trustly.SignedAPI;

@Configuration
class Trustly {

  @Value("${trustly.privateKeyPath}")
  String privateKeyPath;

  @Value("${trustly.privateKeyPassword}")
  String privateKeyPassword;

  @Value("${trustly.username}")
  String username;

  @Value("${trustly.password}")
  String password;

  @Autowired Environment environment;

  @Bean
  SignedAPI createSignedApi() throws URISyntaxException {
    Security.addProvider(new BouncyCastleProvider());
    SignedAPI api = new SignedAPI();
    boolean testEnvironment = !ArrayUtils.contains(environment.getActiveProfiles(), "production");
    api.init(privateKeyPath, privateKeyPassword, username, password, testEnvironment);

    return api;
  }

  @Bean
  NotificationHandler createNotificationHandler() {
    return new NotificationHandler();
  }
}
