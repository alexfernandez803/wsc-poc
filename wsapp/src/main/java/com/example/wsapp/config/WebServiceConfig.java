package com.example.wsapp.config;

import java.io.IOException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.config.annotation.WsConfigurerAdapter;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.soap.security.wss4j2.Wss4jSecurityInterceptor;
import org.springframework.ws.soap.security.wss4j2.support.CryptoFactoryBean;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.springframework.xml.xsd.XsdSchema;

@EnableWs
@Configuration
public class WebServiceConfig extends WsConfigurerAdapter {

  private static final Logger LOGGER = LoggerFactory.getLogger(WebServiceConfig.class);

  @Value("${server.signature.key-store}")
  private Resource keyStore;

  @Value("${server.signature.key-store-password}")
  private String keyStorePassword;

  @Value("${server.signature.key-alias}")
  private String keyAlias;

  @Value("${server.signature.key-password}")
  private String keyPassword;

  @Value("${server.signature.trust-store}")
  private Resource trustStore;

  @Value("${server.signature.trust-store-password}")
  private String trustStorePassword;


  @Bean
  public ServletRegistrationBean<MessageDispatcherServlet> messageDispatcherServlet(
      ApplicationContext applicationContext) {
    MessageDispatcherServlet servlet = new MessageDispatcherServlet();
    servlet.setApplicationContext(applicationContext);
    servlet.setTransformWsdlLocations(true);
    return new ServletRegistrationBean<>(servlet, "/ws/*");
  }

  @Bean(name = "countries")
  public DefaultWsdl11Definition defaultWsdl11Definition(XsdSchema countriesSchema) {
    DefaultWsdl11Definition wsdl11Definition = new DefaultWsdl11Definition();
    wsdl11Definition.setPortTypeName("CountriesPort");
    wsdl11Definition.setLocationUri("/ws");
    wsdl11Definition.setTargetNamespace("http://example.com/ws");
    wsdl11Definition.setSchema(countriesSchema);
    return wsdl11Definition;
  }

  @Bean
  public XsdSchema countriesSchema() {
    return new SimpleXsdSchema(new ClassPathResource("countries.xsd"));
  }


  @Override
  public void addInterceptors(List<EndpointInterceptor> interceptors) {
    interceptors.add(serverSecurityInterceptor());
  }

  @Bean
  public Wss4jSecurityInterceptor serverSecurityInterceptor() {
    Wss4jSecurityInterceptor securityInterceptor = new Wss4jSecurityInterceptor();
    // check the time stamp and signature of the request
    securityInterceptor.setValidationActions("Signature Timestamp");


    // add a time stamp and sign the request
    securityInterceptor.setSecurementActions("Signature Timestamp");
    // alias of the private key
    securityInterceptor.setSecurementUsername(keyAlias);
    // password of the private key
    securityInterceptor.setSecurementPassword(keyPassword);

    securityInterceptor.setSecurementSignatureParts(
        "{Element}{http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd}Timestamp;");

    securityInterceptor.setSecurementSignatureDigestAlgorithm(
        "http://www.w3.org/2000/09/xmldsig#sha1");
    securityInterceptor.setSecurementSignatureAlgorithm(
        "http://www.w3.org/2000/09/xmldsig#rsa-sha1");
    securityInterceptor.setSecurementSignatureKeyIdentifier("DirectReference");
    securityInterceptor.setSecurementMustUnderstand(true);

    // trust store that contains the trusted certificate
    try {
      securityInterceptor
          .setValidationSignatureCrypto(serverTrustStoreCryptoFactoryBean().getObject());
    } catch (Exception e) {
      LOGGER.error("unable to get the server trust store", e);
    }
    // key store that contains the private key
    try {
      securityInterceptor
          .setSecurementSignatureCrypto(serverKeyStoreCryptoFactoryBean().getObject());
    } catch (Exception e) {
      LOGGER.error("unable to get the server key store", e);
    }

    return securityInterceptor;
  }

  @Bean
  public CryptoFactoryBean serverTrustStoreCryptoFactoryBean() throws IOException {
    CryptoFactoryBean cryptoFactoryBean = new CryptoFactoryBean();
    cryptoFactoryBean.setKeyStoreLocation(trustStore);
    cryptoFactoryBean.setKeyStorePassword(trustStorePassword);

    return cryptoFactoryBean;
  }

  @Bean
  public CryptoFactoryBean serverKeyStoreCryptoFactoryBean() throws IOException {
    CryptoFactoryBean cryptoFactoryBean = new CryptoFactoryBean();
    cryptoFactoryBean.setKeyStoreLocation(keyStore);
    cryptoFactoryBean.setKeyStorePassword(keyStorePassword);

    return cryptoFactoryBean;
  }
}