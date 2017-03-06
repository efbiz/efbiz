/**
 * Project Name:efbiz
 * File Name:ActiveMqUtil.java
 * Package Name:org.efbiz.rest.util
 * Date:2016年8月12日下午7:44:22
 * Copyright (c) 2016, chenzhou1025@126.com All Rights Reserved.
 *
*/

package org.efbiz.rest.util;
import java.util.Map;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.MapMessage;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.ofbiz.base.util.UtilProperties;

/**
 * ClassName:ActiveMqUtil <br/>
 * Date:     2016年8月12日 下午7:44:22 <br/>
 * @author   thanos
 * @version  
 * @since    JDK 1.6
 * @see 	 
 */
public class ActiveMqUtil {

	private static final String DEFAULT_USER = UtilProperties.getPropertyValue("activemq.properties", "user", "admin");
	private static final String DEFAULT_PASSWORD = UtilProperties.getPropertyValue("activemq.properties", "password", "admin");
	private static final String DEFAULT_HOST = UtilProperties.getPropertyValue("activemq.properties", "host", "115.29.99.208");
	private static final String SERVERQUEUE = UtilProperties.getPropertyValue("activemq.properties", "serverQueue", "serverQueue");
	
	public static void main(String[] args) {
		 
	}

	public static void sendMessage(Map<String ,String> serviceMap)
			throws Exception {

		// ConnectionFactory ：连接工厂，JMS 用它创建连接
		ConnectionFactory connectionFactory; // Connection ：JMS 客户端到JMS
		// Provider 的连接
		Connection connection = null; // Session： 一个发送或接收消息的线程
		Session session; // Destination ：消息的目的地;消息发送给谁.
		Destination destination; // MessageProducer：消息发送者
		MessageProducer producer; // TextMessage message;
		// 构造ConnectionFactory实例对象，此处采用ActiveMq的实现jar
		connectionFactory = new ActiveMQConnectionFactory( DEFAULT_USER, DEFAULT_PASSWORD, "tcp://"+DEFAULT_HOST+":61616");
		try { // 构造从工厂得到连接对象
			connection = connectionFactory.createConnection();
			// 启动
			connection.start();
			// 获取操作连接
			session = connection.createSession(Boolean.TRUE,
					Session.AUTO_ACKNOWLEDGE);
			// 获取session注意参数值xingbo.xu-queue是一个服务器的queue，须在在ActiveMq的console配置
			destination = session.createQueue(SERVERQUEUE);
			// 得到消息生成者【发送者】
			producer = session.createProducer(destination);
			// 设置不持久化，此处学习，实际根据项目决定
			producer.setDeliveryMode(DeliveryMode.PERSISTENT);
			MapMessage message = session.createMapMessage();
			for(String key :serviceMap.keySet()){
				message.setString(key, serviceMap.get(key));
			}
			producer.send(message);
			session.commit();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (null != connection)
					connection.close();
			} catch (Throwable ignore) {
			}
		}
	}
	 

}

