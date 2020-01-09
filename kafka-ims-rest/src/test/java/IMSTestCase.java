/*
 * ADOBE CONFIDENTIAL. Copyright 2019 Adobe Systems Incorporated. All Rights Reserved. NOTICE: All information contained
 * herein is, and remains the property of Adobe Systems Incorporated and its suppliers, if any. The intellectual and
 * technical concepts contained herein are proprietary to Adobe Systems Incorporated and its suppliers and are protected
 * by all applicable intellectual property laws, including trade secret and copyright law. Dissemination of this
 * information or reproduction of this material is strictly forbidden unless prior written permission is obtained
 * from Adobe Systems Incorporated.
 */

public class IMSTestCase {

//    private static final Logger logger = LoggerFactory.getLogger(IMSTestCase.class);
//
//    @ClassRule
//    public static final SharedKafkaTestResource sharedKafkaTestResource = new SharedKafkaTestResource()
//            .registerListener(new SaslPlainListener())
//            .withBrokerProperty("security.inter.broker.protocol", "PLAINTEXT")
//            .withBrokerProperty("sasl.mechanism.inter.broker.protocol", "PLAIN")
//            .withBrokerProperty("listener.name.sasl_plaintext.oauthbearer.sasl.server.callback.handler.class", "com.adobe.ids.dim.security.java.IMSAuthenticateValidatorCallbackHandler")
//            .withBrokerProperty("sasl.enabled.mechanisms", "OAUTHBEARER");
//
//
//    @Test
//    public void testCreateBroker() throws Exception {
//        KafkaTestUtils broker = getKafkaTestUtils();
//
//        final String topicName = "ProducerAndConsumerTest" + System.currentTimeMillis();
//        getKafkaTestUtils().createTopic(topicName, 1, (short) 1);
//
//        final int partitionId = 0;
//
//        // Define our message
//        final String expectedKey = "my-key";
//        final String expectedValue = "my test message";
//
//        // Define the record we want to produce
//        final ProducerRecord<String, String> producerRecord = new ProducerRecord<>(topicName, partitionId, expectedKey, expectedValue);
//
//        // Create a new producer
//        try (final KafkaProducer<String, String> producer =
//                     getKafkaTestUtils().getKafkaProducer(StringSerializer.class, StringSerializer.class)) {
//
//            // Produce it & wait for it to complete.
//            final Future<RecordMetadata> future = producer.send(producerRecord);
//            producer.flush();
//            while (!future.isDone()) {
//                Thread.sleep(500L);
//            }
//            logger.info("Produce completed");
//        }
//
//
//        try (final KafkaConsumer<String, String> kafkaConsumer =
//                     getKafkaTestUtils().getKafkaConsumer(StringDeserializer.class, StringDeserializer.class)) {
//
//            final List<TopicPartition> topicPartitionList = new ArrayList<>();
//            for (final PartitionInfo partitionInfo: kafkaConsumer.partitionsFor(topicName)) {
//                topicPartitionList.add(new TopicPartition(partitionInfo.topic(), partitionInfo.partition()));
//            }
//            kafkaConsumer.assign(topicPartitionList);
//            kafkaConsumer.seekToBeginning(topicPartitionList);
//
//            // Pull records from kafka, keep polling until we get nothing back
//            ConsumerRecords<String, String> records;
//            do {
//                records = kafkaConsumer.poll(2000L);
//                logger.info("Found {} records in kafka", records.count());
//                for (ConsumerRecord<String, String> record: records) {
//                    // Validate
//                    assertEquals("Key matches expected", expectedKey, record.key());
//                    assertEquals("value matches expected", expectedValue, record.value());
//                }
//            }
//            while (!records.isEmpty());
//        }
//    }
//
//    private KafkaTestUtils getKafkaTestUtils() {
//        return sharedKafkaTestResource.getKafkaTestUtils();
//    }
}
