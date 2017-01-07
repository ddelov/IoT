package com.estafet.iot.devices;

import com.amazonaws.services.iot.client.AWSIotException;
import com.amazonaws.services.iot.client.AWSIotMqttClient;
import com.estafet.iot.util.CommandArguments;
import com.estafet.iot.util.SampleUtil;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Delcho Delov on 5.1.2017 г..
 */
public class Device2Shadow {
    public static final double CRITICAL_PRESSURE = 400;
    public static final double MAX_PRESSURE = 500;
    private static Logger log = Logger.getLogger("Device2Shadow");

    public static void main(String[] args) throws AWSIotException, InterruptedException {
        CommandArguments arguments = CommandArguments.parse(args);
//        final String accessKey = arguments.getNotNull("clientId", SampleUtil.getConfig("clientId"));
//        final String secretKey = arguments.getNotNull("secretAccessKey", SampleUtil.getConfig("secretAccessKey"));
//        final AWSIotClient iotClient = getIotClient(accessKey, secretKey);
        AWSIotMqttClient awsIotMqttClient = null;

        String clientEndpoint = arguments.getNotNull("clientEndpoint", SampleUtil.getConfig("clientEndpoint"));
        String clientId = arguments.getNotNull("clientId", SampleUtil.getConfig("clientId"));
        if(clientEndpoint==null || clientId==null){
            throw new IllegalArgumentException("Failed to construct client due to missing parameters.");
        }

        String certificateFile = arguments.get("certificateFile", SampleUtil.getConfig("certificateFile"));
        String privateKeyFile = arguments.get("privateKeyFile", SampleUtil.getConfig("privateKeyFile"));
        String algorithm = arguments.get("keyAlgorithm", SampleUtil.getConfig("keyAlgorithm"));
        String secretAccessKey = arguments.get("secretAccessKey", SampleUtil.getConfig("secretAccessKey"));
        String sessionToken = arguments.get("sessionToken", SampleUtil.getConfig("sessionToken"));
        if(certificateFile!=null && privateKeyFile!=null){
            awsIotMqttClient = SampleUtil.initClientWithCert(clientEndpoint, clientId, certificateFile, privateKeyFile, algorithm);
        }else{
            if(clientId!=null && secretAccessKey!=null){
                awsIotMqttClient = SampleUtil.initClientWithToken(clientEndpoint, clientId, clientId, secretAccessKey, sessionToken);
            }
        }
        if(awsIotMqttClient==null) {
            throw new IllegalArgumentException("Failed to construct client due to missing certificate or credentials.");
        }
//        final String thingName = arguments.getNotNull("thingName", SampleUtil.getConfig("thingName"));
        final String timeBetweenInvocationSecStr = arguments.get("timeBetweenInvocationSec", SampleUtil.getConfig("timeBetweenInvocationSec"));
        final Integer timeBetweenInvocationSec = Integer.valueOf(timeBetweenInvocationSecStr);
        System.out.println("begin");
        final String thingName = "GasPipe_4";
        updatePipe(awsIotMqttClient, thingName, timeBetweenInvocationSec);
//        updateSimDevice(awsIotMqttClient, "Sim_1", timeBetweenInvocationSec);
        System.out.println(".. unexpected end!");
    }
    private static void updatePipe(AWSIotMqttClient awsIotMqttClient, String thingName, Integer timeBetweenInvocationSec) throws AWSIotException, InterruptedException {
        final long intervalInMillis = TimeUnit.MILLISECONDS.convert(timeBetweenInvocationSec, TimeUnit.SECONDS);
        GasPipeDevice device = new GasPipeDevice(thingName);
        device.setReportInterval(intervalInMillis);
        device.setEnableVersioning(false);
        awsIotMqttClient.attach(device);
        awsIotMqttClient.connect();
        Random rnd = new Random(8837457864l);

        while(true) {
            boolean leakDetected = false;
            float pressure = (float) (rnd.nextFloat() * MAX_PRESSURE);
            if (pressure > CRITICAL_PRESSURE) {
                leakDetected = rnd.nextBoolean();
            }
            device.setPressure(pressure);
            device.setLeakDetected(leakDetected);
//            String reported = gson.toJson(device, GasPipeDevice.class);
            String jsonDocument = device.get();
            System.out.println(System.currentTimeMillis() +" pressure = " + pressure + " / leakDetected = " + leakDetected);
            try {
                device.update(jsonDocument);
            }catch (AWSIotException e){
                log.log(Level.SEVERE, e.getMessage());
                System.err.println(e.getMessage());
            }
            //2 insert new record in DB
//            gasPipe.insertGasRecord(dynamoDB);
//        device.onShadowUpdate("ala Bala");
            String report2 = device.get();
            log.log(Level.FINE, System.currentTimeMillis() + ": state after update = " + report2);
            Thread.sleep(intervalInMillis);
        }
//        awsIotClient.disconnect();
    }
}
