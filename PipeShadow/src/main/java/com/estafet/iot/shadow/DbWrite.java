//package com.estafet.iot.shadow;
//
//import com.amazonaws.auth.profile.ProfileCredentialsProvider;
//import com.amazonaws.regions.Regions;
//import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
//import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
//import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
//import com.amazonaws.services.dynamodbv2.document.*;
//import com.amazonaws.services.dynamodbv2.model.*;
//import com.estafet.iot.devices.GasPipe;
//import com.estafet.iot.devices.TempSensor;
//
//import java.io.IOException;
//import java.util.*;
//
//import static com.estafet.iot.devices.GasPipe.CRITICAL_PRESSURE;
//import static com.estafet.iot.devices.GasPipe.MAX_PRESSURE;
//import static com.estafet.iot.devices.TempSensor.*;
////import static com.estafet.iot.devices.GasPipe.Entity.*;
//
///**
// * Created by Delcho Delov on 27.12.2016 г..
// */
//public class DbWrite {
//    private final static AmazonDynamoDBClient client = new AmazonDynamoDBClient(
//            new ProfileCredentialsProvider()).withRegion(Regions.EU_WEST_1);
//    private final static DynamoDB dynamoDB = new DynamoDB(client);
//    private final static Random rnd = new Random(10748213434l);
////    private static final AtomicInteger idNum = new AtomicInteger(2);
//
//
//    public static void main(String[] args) throws IOException, InterruptedException {
////        createTempSensorTable();
////        createGasPipeTable();
////        listMyTables();
////        insertTempRecord();
////        searchRecordsWithTempWithin(-0.88f, 23.55f);
//        insertGasRecords(10);
//        GasPipe.searchRecordsWithPressureWithin(client, 2.4f, 400f);
//    }
//    private static void insertGasRecords(int numRec){
//        GasPipe gasPipe = new GasPipe("GP_1");
//        for(int i=0; i<numRec; ++i) {
//            boolean leakDetected = false;
//            float pressure = (float) (rnd.nextFloat() * MAX_PRESSURE);
//            if (pressure > CRITICAL_PRESSURE) {
//                leakDetected = rnd.nextBoolean();
//            }
//            gasPipe.setPressure(pressure);
//            gasPipe.setLeakDetected(leakDetected);
//            gasPipe.insertGasRecord(dynamoDB);
//        }
//    }
//    static void listMyTables() {
//        TableCollection<ListTablesResult> tables = dynamoDB.listTables();
//        Iterator<Table> iterator = tables.iterator();
//
//        System.out.println("Listing table names");
//
//        while (iterator.hasNext()) {
//            Table table = iterator.next();
//            System.out.println(table.getTableName());
//        }
//    }
//    private static void createGasPipeTable() throws InterruptedException {
//        ArrayList<AttributeDefinition> attributeDefinitions= new ArrayList<AttributeDefinition>();
//        attributeDefinitions.add(new AttributeDefinition().withAttributeName(GasPipe.TSTAMP).withAttributeType("S"));
//        attributeDefinitions.add(new AttributeDefinition().withAttributeName(GasPipe.THING_ID).withAttributeType("S"));
//
//        ArrayList<KeySchemaElement> keySchema = new ArrayList<KeySchemaElement>();
//        keySchema.add(new KeySchemaElement().withAttributeName(GasPipe.TSTAMP).withKeyType(KeyType.HASH));
//        keySchema.add(new KeySchemaElement().withAttributeName(GasPipe.THING_ID).withKeyType(KeyType.RANGE));
//        //createTable
//        CreateTableRequest request = new CreateTableRequest()
//                .withTableName(GasPipe.TABLE_NAME)
//                .withKeySchema(keySchema)
//                .withAttributeDefinitions(attributeDefinitions)
//                .withProvisionedThroughput(new ProvisionedThroughput()
//                        .withReadCapacityUnits(1L)
//                        .withWriteCapacityUnits(1L));
//        Table table = dynamoDB.createTable(request);
//        table.waitForActive();
//        getTableInformation(GasPipe.TABLE_NAME);
//    }
//
//    private static void createTempSensorTable() throws InterruptedException {
//        ArrayList<AttributeDefinition> attributeDefinitions= new ArrayList<AttributeDefinition>();
//        attributeDefinitions.add(new AttributeDefinition().withAttributeName(TempSensor.THING_ID).withAttributeType("S"));
//
//        ArrayList<KeySchemaElement> keySchema = new ArrayList<KeySchemaElement>();
//        keySchema.add(new KeySchemaElement().withAttributeName(TempSensor.THING_ID).withKeyType(KeyType.HASH));
//        //createTable
//        final String tableName = "TempSensor";
//        CreateTableRequest request = new CreateTableRequest()
//                .withTableName(tableName)
//                .withKeySchema(keySchema)
//                .withAttributeDefinitions(attributeDefinitions)
//                .withProvisionedThroughput(new ProvisionedThroughput()
//                        .withReadCapacityUnits(1L)
//                        .withWriteCapacityUnits(1L));
//        Table table = dynamoDB.createTable(request);
//        table.waitForActive();
//        getTableInformation(tableName);
//    }
//    static void getTableInformation(String tableName) {
//        System.out.println("Describing " + tableName);
//
//        TableDescription tableDescription = dynamoDB.getTable(tableName).describe();
//        System.out.format("Name: %s:\n" + "Status: %s \n"
//                        + "Provisioned Throughput (read capacity units/sec): %d \n"
//                        + "Provisioned Throughput (write capacity units/sec): %d \n",
//                tableDescription.getTableName(),
//                tableDescription.getTableStatus(),
//                tableDescription.getProvisionedThroughput().getReadCapacityUnits(),
//                tableDescription.getProvisionedThroughput().getWriteCapacityUnits());
//    }
//
//    private static void insertTempRecord(){
//        Item item = new Item()
//                .withPrimaryKey(TempSensor.THING_ID, "Tmp_One")
//                .withString(CLS, "Mbiba")
//                .withNumber(TEMPERATURE, 5.0450);
//        final String tableName = "TempSensor";
//        Table table = dynamoDB.getTable(tableName);
//        PutItemOutcome outcome = table.putItem(item);
//        System.out.println("Done!");
//    }
//
//    private static void searchRecordsWithTempWithin(float minTemp, float maxTemp) {
//        DynamoDBMapper mapper = new DynamoDBMapper(client);
//        Map<String, AttributeValue> eav = new HashMap<String, AttributeValue>();
//        eav.put(":minTemp", new AttributeValue().withN(String.valueOf(minTemp)));
//        eav.put(":maxTemp", new AttributeValue().withN(String.valueOf(maxTemp)));
//
////        DynamoDBQueryExpression<TempSensor> queryExpression = new DynamoDBQueryExpression<TempSensor>()
////                .withKeyConditionExpression("ThingID=:id and temperature between :minTemp and :maxTemp")
////                .withExpressionAttributeValues(eav);
//        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
//                .withFilterExpression("temperature between :minTemp and :maxTemp")
//                .withExpressionAttributeValues(eav);
//        List<TempSensor> betweenReplies = mapper.scan(TempSensor.class, scanExpression);
//
//        for (TempSensor reply : betweenReplies) {
//            System.out.format("ThingID=%s, cls=%s, temperature=%f",
//                    reply.getThingId(), reply.getCls(), reply.getTemperature());
//        }
//    }
//    private static void searchTempRecord() {
//        final String tableName = "TempSensor";
//        Table table = dynamoDB.getTable(tableName);
//        final Item tmp_one = table.getItem(THING_ID, "Tmp_One");
//        System.out.println("Item retrieved:");
//        System.out.println(tmp_one);
//    }
//}
