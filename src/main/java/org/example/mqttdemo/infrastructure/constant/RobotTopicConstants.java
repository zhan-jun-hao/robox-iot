package org.example.mqttdemo.infrastructure.constant;

public interface RobotTopicConstants {

    /**
     * 所有机器的状态
     * +是通配符 接收所有 robot/001/status 或 robot/002/status 的MQTT请求
     */
    String ROBOT_STATUS_TOPIC = "robot/+/status";

}
