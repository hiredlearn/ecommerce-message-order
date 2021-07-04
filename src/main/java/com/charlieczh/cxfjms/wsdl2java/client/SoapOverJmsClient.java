package com.charlieczh.cxfjms.wsdl2java.client;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.cxf.transport.jms.JMSConfigFeature;
import org.apache.cxf.transport.jms.JMSConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;

import com.charlieczh.cxfjms.wsdl2java.service.OrderService;
import com.charlieczh.cxfjms.wsdl2java.service.OrderService_Service;
import com.charlieczh.cxfjms.wsdl2java.service.PostOrderRequest;
import com.gmail.merikbest2015.ecommerce.domain.Order;
import com.gmail.merikbest2015.ecommerce.domain.OrderItem;

@Configuration
public class SoapOverJmsClient {

	public String postOrder(Order o) throws DatatypeConfigurationException {
		OrderService_Service service = new OrderService_Service();
		ConnectionFactory cf = new ActiveMQConnectionFactory("tcp://localhost:61616");
		JMSConfigFeature feature = new JMSConfigFeature();
		JMSConfiguration jmsConfiguration = new JMSConfiguration();
		jmsConfiguration.setConnectionFactory(cf);
		jmsConfiguration.setRequestURI("jms://");
		jmsConfiguration.setTargetDestination("request.queue");
		jmsConfiguration.setReplyDestination("client.queue");
		jmsConfiguration.setReplyToDestination("response.queue");
		jmsConfiguration.setMessageType("text");
		feature.setJmsConfig(jmsConfiguration);
		OrderService os = service.getOrderServicePort(feature);

		PostOrderRequest.Arg0 arg = new PostOrderRequest.Arg0();
		com.charlieczh.order.Order _o = new com.charlieczh.order.Order();
		_o.setTotalPrice(o.getTotalPrice());
		LocalDate date = o.getDate();
		GregorianCalendar gcal = GregorianCalendar.from(date.atStartOfDay(ZoneId.systemDefault()));
		XMLGregorianCalendar xcal = DatatypeFactory.newInstance().newXMLGregorianCalendar(gcal);
		_o.setDate(xcal);
		_o.setFirstName(o.getFirstName());
		_o.setLastName(o.getLastName());
		_o.setCity(o.getCity());
		_o.setAddress(o.getAddress());
		_o.setEmail(o.getEmail());
		_o.setPhoneNumber(o.getPhoneNumber());
		_o.setPostIndex(BigInteger.valueOf(o.getPostIndex().intValue()));
		List<com.charlieczh.orderitem.OrderItem> orderItems = new ArrayList<>();
		for (OrderItem oi : o.getOrderItems()) {
			com.charlieczh.orderitem.OrderItem _oi = new com.charlieczh.orderitem.OrderItem();
			_oi.setAmount(oi.getAmount());
			_oi.setQuantity(oi.getQuantity());
			com.charlieczh.perfume.Perfume _p = new com.charlieczh.perfume.Perfume();
			_p.setId(oi.getPerfume().getId());
			_oi.setPerfume(_p);
			orderItems.add(_oi);
		}
		arg.setOrder(_o);

		return os.postOrderRequest(arg);
	}

	public static void main(String[] args) throws Exception {
		OrderService_Service service = new OrderService_Service();
		ConnectionFactory cf = new ActiveMQConnectionFactory("tcp://localhost:61616");
		JMSConfigFeature feature = new JMSConfigFeature();
		JMSConfiguration jmsConfiguration = new JMSConfiguration();
		jmsConfiguration.setConnectionFactory(cf);
		jmsConfiguration.setRequestURI("jms://");
		jmsConfiguration.setTargetDestination("request.queue");
		jmsConfiguration.setReplyDestination("client.queue");
		jmsConfiguration.setReplyToDestination("response.queue");
		jmsConfiguration.setMessageType("text");
		feature.setJmsConfig(jmsConfiguration);
		OrderService os = service.getOrderServicePort(feature);

		com.charlieczh.order.Order o = new com.charlieczh.order.Order();
		o.setTotalPrice(12.0);
		GregorianCalendar c = new GregorianCalendar();
		c.setTime(new Date());
		XMLGregorianCalendar date2 = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
		o.setDate(date2);
		o.setFirstName("Rick");
		o.setLastName("Morty");
		o.setCity("HK");
		o.setAddress("123");
		o.setEmail("charlieqq55@gmail.com");
		o.setPhoneNumber("456");
		o.setPostIndex(BigInteger.TEN);
		List<com.charlieczh.orderitem.OrderItem> orderItems = new ArrayList<>();
		com.charlieczh.orderitem.OrderItem oi1 = new com.charlieczh.orderitem.OrderItem();
		oi1.setAmount(456);
		oi1.setQuantity(789);
		com.charlieczh.perfume.Perfume p = new com.charlieczh.perfume.Perfume();
		p.setId(123456789);
		oi1.setPerfume(p);
		orderItems.add(oi1);
		o.getOrderItem().add(oi1);
		PostOrderRequest.Arg0 arg = new PostOrderRequest.Arg0();
		arg.setOrder(o);

		String message = os.postOrderRequest(arg);
		System.out.println(message);
	}

    @JmsListener(destination = "response.queue")
    public void receiveMessage(final Message message) throws JMSException {
    	System.out.println("E-commerce Client Received JMS Message -> " + message);
        String messageData = null;

        if(message instanceof TextMessage) {
            TextMessage textMessage = (TextMessage)message;
            messageData = textMessage.getText();
            System.out.println("E-commerce Client Received JMS Message data -> "+messageData);

        }
        JmsTemplate template = new JmsTemplate();
		template.setConnectionFactory(new ActiveMQConnectionFactory("tcp://localhost:61616"));
        template.convertAndSend("client.queue", message);
    }
}
