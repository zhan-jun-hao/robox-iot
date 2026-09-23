package org.example.mqttdemo.interfaces;

import lombok.RequiredArgsConstructor;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.example.mqttdemo.MqttClientService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/robot")
@RequiredArgsConstructor
public class RobotController {

    private final MqttClientService mqttClientService;

    @GetMapping("/{robotId}/open-door")
    public String openDoor(@PathVariable String robotId) throws MqttException {
        String topic = "robot/" + robotId + "/command";
        String payload = """
                {
                  "action": "OPEN_DOOR",
                  "doorId": 1
                }
                """;
        mqttClientService.publish(topic, payload, 1);
        return "开舱指令已发送";
    }
}