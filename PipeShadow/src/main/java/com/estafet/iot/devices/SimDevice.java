package com.estafet.iot.devices;

import com.amazonaws.services.iot.client.AWSIotDevice;
import com.amazonaws.services.iot.client.AWSIotDeviceProperty;
import com.estafet.iot.shadow.DevAttribute;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Delcho Delov on 3.1.2017 г..
 */
public class SimDevice extends AWSIotDevice implements DevAttribute {
    @AWSIotDeviceProperty
    private String state="switchedOff";
    /**
     * Instantiates a new device instance.
     *
     * @param thingName the thing name
     */
    public SimDevice(String thingName) {
        super(thingName);
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Override
    public Map<String, Object> getDeviceAttributes() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("state", "switchedOff");
        return attributes;
    }
}
