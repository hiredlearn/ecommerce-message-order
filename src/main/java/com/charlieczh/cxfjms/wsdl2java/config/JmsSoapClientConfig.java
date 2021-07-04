package com.charlieczh.cxfjms.wsdl2java.config;

import org.apache.cxf.transport.jms.JMSConfigFeature;
import org.apache.cxf.transport.jms.JMSConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import javax.jms.ConnectionFactory;

@Configuration
public class JmsSoapClientConfig {

	@Bean
	public JMSConfigFeature jmsConfigFeature(ConnectionFactory mqConnectionFactory) {
		JMSConfigFeature feature = new JMSConfigFeature();

		JMSConfiguration jmsConfiguration = new JMSConfiguration();
		jmsConfiguration.setConnectionFactory(mqConnectionFactory);
		jmsConfiguration.setRequestURI("jms://");
		jmsConfiguration.setTargetDestination("request.queue");
        jmsConfiguration.setReplyDestination("client.queue");
        jmsConfiguration.setReplyToDestination("response.queue");
		jmsConfiguration.setMessageType("text");

		feature.setJmsConfig(jmsConfiguration);
		return feature;
	}
}
