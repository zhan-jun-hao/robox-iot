package org.example.mqttdemo;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.example.mqttdemo.infrastructure.config.MqttProperties;
import org.springframework.stereotype.Component;
import org.example.mqttdemo.infrastructure.constant.RobotTopicConstants;

@Component
@RequiredArgsConstructor
public class MqttClientService {

    private final MqttProperties mqttProperties;
    // Paho实现的Java MQTT客户端
    private MqttClient mqttClient;

    @PostConstruct
    public void init() throws MqttException {
        // 1. 创建MQTT Client
        mqttClient = new MqttClient(mqttProperties.getBrokerUrl()
                , mqttProperties.getClientId(), new MemoryPersistence());
        // 2. 创建MQTT连接对象
        MqttConnectOptions options = new MqttConnectOptions();
        // 连接断了以后自动尝试重连
        options.setAutomaticReconnect(true);
        // 重建连接时，不保留以前的会话状态
        options.setCleanSession(true);
        // 连接Broker最多等10秒
        options.setConnectionTimeout(10);
        // 每30s内要有心跳 确保客户端没挂 通过PING报文类型确认
        options.setKeepAliveInterval(30);
        // 3.设置回调
        mqttClient.setCallback(new MqttCallbackExtended() {

            /**
             * 连接成功回调
             * @param reconnect If true, the connection was the result of automatic reconnect.
             * @param serverURI The server URI that the connection was made to.
             */
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                System.out.println("MQTT连接成功：" + serverURI);
                try {
                    // 连接成功以后订阅机器人状态
                    mqttClient.subscribe(RobotTopicConstants.ROBOT_STATUS_TOPIC, 1);
                    System.out.println("订阅成功：" + RobotTopicConstants.ROBOT_STATUS_TOPIC);
                } catch (MqttException e) {
                    throw new RuntimeException(e);
                }
            }

            /**
             * 连接断开回调
             * @param cause the reason behind the loss of connection.
             */
            @Override
            public void connectionLost(Throwable cause) {
                System.out.println("MQTT连接断开");
            }

            /**
             * 监听到设备端的消息回调
             * @param topic name of the topic on the message was published to
             * @param message the actual message.
             */
            @Override
            public void messageArrived(String topic, MqttMessage message) {
                String payload = new String(message.getPayload());

                System.out.println("==============================");
                System.out.println("收到设备消息");
                System.out.println("Topic：" + topic);
                System.out.println("QoS：" + message.getQos());
                System.out.println("Payload：" + payload);
                System.out.println("==============================");
            }

            /**
             * 消息发送完成回调
             * @param token the delivery token associated with the message.
             */
            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                System.out.println("服务端已经完成向设备端发送消息");
            }
        });

        // 4. 连接 EMQX
        mqttClient.connect(options);
    }

    @PreDestroy
    public void destroy() throws MqttException {

        if (mqttClient != null && mqttClient.isConnected()) {
            mqttClient.disconnect();
        }

        if (mqttClient != null) {
            mqttClient.close();
        }
    }

    public void publish(String topic, String payload, int qos) throws MqttException {
        MqttMessage message = new MqttMessage(payload.getBytes());
        message.setQos(qos);
        mqttClient.publish(topic, message);
        System.out.println("MQTT消息发送成功");
        System.out.println("Topic：" + topic);
        System.out.println("Payload：" + payload);
    }
}