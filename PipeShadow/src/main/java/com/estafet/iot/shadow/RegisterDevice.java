package com.estafet.iot.shadow;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.iot.AWSIotClient;
import com.amazonaws.services.iot.client.AWSIotDevice;
import com.amazonaws.services.iot.client.AWSIotMqttClient;
import com.amazonaws.services.iot.model.*;
import com.estafet.iot.devices.GasPipeDevice;
import com.estafet.iot.util.CommandArguments;
import com.estafet.iot.util.SampleUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Delcho Delov on 3.1.2017 г..
 */
public class RegisterDevice {
    private static Logger log = Logger.getLogger("RegisterDevice");
//    private static AWSIotClient iotClient;
//    private static AWSIotMqttClient awsIotMqttClient;

    public static String createSimpleThing(AWSIotClient iotClient, String thingName) {
        CreateThingRequest ctr = new CreateThingRequest();
        ctr.setThingName(thingName);
        Map<String, String> map = new HashMap<>(5);
        map.put("state", "switchedOff");
        AttributePayload attributePayload = new AttributePayload();
        attributePayload.setAttributes(map);
        ctr.setAttributePayload(attributePayload);
        final CreateThingResult thing = iotClient.createThing(ctr);
        AttachThingPrincipalRequest thingPrincipalRequest = new AttachThingPrincipalRequest();
        String certificateGasPipeARN = "arn:aws:iot:eu-west-1:573802978597:cert/eb7e6425c46ba8c9b41736c04ec2de74b03e754d14b542e333bf9cbc3ad6e805";
        thingPrincipalRequest.setPrincipal(certificateGasPipeARN);
        thingPrincipalRequest.setThingName(thingName);
        iotClient.attachThingPrincipal(thingPrincipalRequest);
        return thing.getThingArn();
    }
    private static AWSIotClient getIotClient(String accessKey, String secretKey){
        final AWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
        final AWSIotClient iotClient = new AWSIotClient(awsCredentials).withRegion(Regions.EU_WEST_1);
        return iotClient;
    }
//    private static AWSIotMqttClient getAwsIotMqttClient(String... args){
//        CommandArguments arguments = CommandArguments.parse(args);
//        String clientEndpoint = arguments.getNotNull("clientEndpoint", SampleUtil.getConfig("clientEndpoint"));
//        String clientId = arguments.getNotNull("clientId", SampleUtil.getConfig("clientId"));
//        if(clientEndpoint==null || clientId==null){
//            throw new IllegalArgumentException("Failed to construct client due to missing parameters.");
//        }
//
//        String certificateFile = arguments.get("certificateFile", SampleUtil.getConfig("certificateFile"));
//        String privateKeyFile = arguments.get("privateKeyFile", SampleUtil.getConfig("privateKeyFile"));
//        String algorithm = arguments.get("keyAlgorithm", SampleUtil.getConfig("keyAlgorithm"));
//        if(certificateFile!=null && privateKeyFile!=null && algorithm!=null){
//            AWSIotMqttClient awsIotMqttClient = SampleUtil.initClientWithCert(clientEndpoint, clientId, certificateFile, privateKeyFile, algorithm);
//            return awsIotMqttClient;
//        }
//
//        String awsAccessKeyId = arguments.get("awsAccessKeyId", SampleUtil.getConfig("awsAccessKeyId"));
//        String awsSecretAccessKey = arguments.get("awsSecretAccessKey", SampleUtil.getConfig("awsSecretAccessKey"));
//        String sessionToken = arguments.get("sessionToken", SampleUtil.getConfig("sessionToken"));
//        if(awsAccessKeyId!=null && awsSecretAccessKey!=null && sessionToken!=null){
//            AWSIotMqttClient awsIotMqttClient = SampleUtil.initClientWithToken(clientEndpoint, clientId, awsAccessKeyId, awsSecretAccessKey, sessionToken);
//            return awsIotMqttClient;
//        }
//        throw new IllegalArgumentException("Failed to construct client due to missing certificate or credentials.");
//    }
    private static void createGasPipe(AWSIotClient iotClient, String thingName){
        CreateThingRequest ctr = new CreateThingRequest();
        ctr.setThingName(thingName);
        ctr.setThingTypeName(GasPipeDevice.TYPE_NAME);
        AttributePayload attributePayload = new AttributePayload();
        attributePayload.setAttributes(GasPipeDevice.getDeviceAttributes());
        ctr.setAttributePayload(attributePayload);
        final CreateThingResult thing = iotClient.createThing(ctr);
        log.log(Level.FINE, "arn = " + thing.getThingArn());
    }
    private static void attachCertificate(AWSIotClient iotClient, String thingName){
        //attach certificate
        final AttachThingPrincipalRequest thingPrincipalRequest = new AttachThingPrincipalRequest();
        final String certificateGasPipeARN = "arn:aws:iot:eu-west-1:573802978597:cert/eb7e6425c46ba8c9b41736c04ec2de74b03e754d14b542e333bf9cbc3ad6e805";
        thingPrincipalRequest.setPrincipal(certificateGasPipeARN);
        thingPrincipalRequest.setThingName(thingName);
        iotClient.attachThingPrincipal(thingPrincipalRequest);
    }
    private static void cloneDetectLeakRule(AWSIotClient iotClient, String thingName){
        //clone detect leak rule
        CreateTopicRuleRequest topicRuleRequest = new CreateTopicRuleRequest();
        topicRuleRequest.setRuleName("detectLeak"+thingName);
        topicRuleRequest.setTopicRulePayload(new DetectLeakTopicRulePayload(thingName));
        iotClient.createTopicRule(topicRuleRequest);
    }

    static class DetectLeakTopicRulePayload extends TopicRulePayload{
        private static final String LAMBDA_FUNC_ARN = "arn:aws:lambda:eu-west-1:573802978597:function:shadowLeakDetect";
        private String thingName;
        DetectLeakTopicRulePayload(String thingName){
            this.thingName = thingName;
        }

        @Override
        public String getSql() {
//            return "SELECT state.reported, timestamp() as timestamp FROM '$aws/things/"+thingName +"/shadow/update/accepted' WHERE state.reported.leakDetected = true";
            return "SELECT topic(3) as thingId, get(state.reported, \"leakDetected\") as leakDetected, get(state.reported, \"pressure\") as pressure,  timestamp() as timestamp FROM '$aws/things/"+thingName +"/shadow/update/accepted' WHERE get(state.reported, \"leakDetected\")=true";
        }

        @Override
        public String getDescription() {
            return "Register leak from shadow update event in DynamoDB (table LeakHistory)";
        }

        @Override
        public List<Action> getActions() {
            final List<Action> res = new ArrayList<Action>(1);
            final Action action = new Action();
            final LambdaAction lambdaAction = new LambdaAction();
            lambdaAction.setFunctionArn(LAMBDA_FUNC_ARN);
            action.setLambda(lambdaAction);
            res.add(action);
            return res;
        }
    }
    private static void createShadowWithFirstUpdate(AWSIotMqttClient awsIotMqttClient, String thingName) throws com.amazonaws.services.iot.client.AWSIotException {
        final AWSIotDevice device = new AWSIotDevice(thingName);
        awsIotMqttClient.attach(device);
        awsIotMqttClient.connect();
//        final String initialState = "{\"state\": {\"reported\" : {\"state\" : \"switchedOff\", \"leakDetected\" : \"false\"}}}";
        awsIotMqttClient.publish("$aws/things/"+thingName +"/shadow/update", GasPipeDevice.getInitialState());
        awsIotMqttClient.disconnect();
    }

    public static void main(String[] args) throws com.amazonaws.services.iot.client.AWSIotException, InterruptedException {
        CommandArguments arguments = CommandArguments.parse(args);
        final String accessKey = arguments.getNotNull("clientId", SampleUtil.getConfig("clientId"));
        final String secretKey = arguments.getNotNull("secretAccessKey", SampleUtil.getConfig("secretAccessKey"));
        final AWSIotClient iotClient = getIotClient(accessKey, secretKey);
        AWSIotMqttClient awsIotMqttClient = null;

        String clientEndpoint = arguments.getNotNull("clientEndpoint", SampleUtil.getConfig("clientEndpoint"));
        String clientId = arguments.getNotNull("clientId", SampleUtil.getConfig("clientId"));
        if(clientEndpoint==null || clientId==null){
            throw new IllegalArgumentException("Failed to construct client due to missing parameters.");
        }

        String certificateFile = arguments.get("certificateFile", SampleUtil.getConfig("certificateFile"));
        String privateKeyFile = arguments.get("privateKeyFile", SampleUtil.getConfig("privateKeyFile"));
        String algorithm = arguments.get("keyAlgorithm", SampleUtil.getConfig("keyAlgorithm"));
        String awsAccessKeyId = arguments.get("awsAccessKeyId", SampleUtil.getConfig("awsAccessKeyId"));
        String awsSecretAccessKey = arguments.get("awsSecretAccessKey", SampleUtil.getConfig("awsSecretAccessKey"));
        String sessionToken = arguments.get("sessionToken", SampleUtil.getConfig("sessionToken"));
        if(certificateFile!=null && privateKeyFile!=null){
            awsIotMqttClient = SampleUtil.initClientWithCert(clientEndpoint, clientId, certificateFile, privateKeyFile, algorithm);
        }else{
            if(awsAccessKeyId!=null && awsSecretAccessKey!=null){
                awsIotMqttClient = SampleUtil.initClientWithToken(clientEndpoint, clientId, awsAccessKeyId, awsSecretAccessKey, sessionToken);
            }
        }
        if(awsIotMqttClient==null) {
            throw new IllegalArgumentException("Failed to construct client due to missing certificate or credentials.");
        }

        final String thingName = "GasPipe_4";
        createGasPipe(iotClient,  thingName);
        attachCertificate(iotClient, thingName);
        createShadowWithFirstUpdate(awsIotMqttClient, thingName);
        cloneDetectLeakRule(iotClient, thingName);
        //TODO clone rule detectLeak for this new device
        System.out.println("Done!");
    }


}
