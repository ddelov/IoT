package com.estafet.iot.devices;

import com.amazonaws.services.iot.client.AWSIotDevice;
import com.amazonaws.services.iot.client.AWSIotDeviceProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Delcho Delov on 3.1.2017 г..
 */
public class GasPipeDevice extends AWSIotDevice {
    public static final String TYPE_NAME = "TypePipe";
    public static final String MATERIAL_YELLOW_BRASS = "YellowBrass";
    public static final int DIAMETER_H2 = 2;

    @AWSIotDeviceProperty(name = "Diameter"/*, enableReport = false, allowUpdate = false*/)
    private float Diameter = DIAMETER_H2;
    @AWSIotDeviceProperty(name = "Material"/*, enableReport = false, allowUpdate = false*/)
    private String Material = MATERIAL_YELLOW_BRASS;
    @AWSIotDeviceProperty
    private boolean leakDetected = false;
    @AWSIotDeviceProperty
    private float pressure = 250.0f;
    /**
     * Instantiates a new device instance.
     *
     * @param thingName the thing name
     */
    public GasPipeDevice(String thingName) {
        super(thingName);
    }

    public boolean isLeakDetected() {
        return leakDetected;
    }

    public void setLeakDetected(boolean leakDetected) {
        this.leakDetected = leakDetected;
    }

    public float getPressure() {
        return pressure;
    }

    public void setPressure(float pressure) {
        this.pressure = pressure;
    }

    public float getDiameter() {
        return Diameter;
    }

    public void setDiameter(float diameter) {
        this.Diameter = diameter;
    }

    public String getMaterial() {
        return Material;
    }

    public void setMaterial(String material) {
        this.Material = material;
    }

//    public static Map<String, String> getDeviceTypeAttributes() {
//        Map<String, String> attributes = new HashMap<>();
//        attributes.put("Diameter", "N");
//        attributes.put("Material", "S");
//        return attributes;
//    }
    public static Map<String, String> getDeviceAttributes() {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("Diameter", ""+DIAMETER_H2);
        attributes.put("Material", MATERIAL_YELLOW_BRASS);
        attributes.put("leakDetected", "false");
        attributes.put("pressure", "250.0");
        return attributes;
    }
    public static String getInitialState(){
        return "{\"state\": {\"reported\" : {\"Diameter\":" +DIAMETER_H2 + ", \"Material\":\""+MATERIAL_YELLOW_BRASS +"\", \"pressure\":250.0, \"leakDetected\" : false}}}";
    }
}
