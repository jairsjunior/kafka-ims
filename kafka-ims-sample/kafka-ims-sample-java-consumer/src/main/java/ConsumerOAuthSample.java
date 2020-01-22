
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Properties;

public class ConsumerOAuthSample {

    public static Logger log = LoggerFactory.getLogger(ConsumerOAuthSample.class);

    public static void main(String[] args) {
        //Create the properties file
        Properties properties = configureConsumer();
        //Create a new kafka consumer
        Consumer<String, String> consumer = new KafkaConsumer<>(properties);

        //Subscribe the topic 'topic-name'
        consumer.subscribe(Arrays.asList("topic-name"));

        log.info("Start Consuming batch: ");
        while (true) {
            try {
                ConsumerRecords<String, String> records = consumer.poll(100);
                for (ConsumerRecord<String, String> record : records)
                    log.info("offset = " + record.offset() + ", key = " + record.key() + ", value = " + record.value() + "\n");
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }
        consumer.close();
    }

    private static Properties configureConsumer() {
        //Create the kafka.properties file to be used by this producer
        Properties properties = new Properties();

        // Broker addresses
        String bootstrapServers = "broker:9093";

        // kafka bootstrap server
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        // kafka consumer props
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "group-id-1");
        properties.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 1);
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        // kafka IMS configuration
        // You can set the values on the code using the propertie 'sasl.jaas.config' or
        // you can create a file kafka_client_jaas.conf (see a model on resources directory)
        // with this configuration an set the file and using
        // '-Djava.security.auth.login.config=/etc/kafka/kafka_server_jaas.conf'
        // at your JAVA_OPTS environment or on the java -jar command
        String imsTokenUrl = "https://adobeid-na1-stg1.services.adobe.com/ims/token/v1";
        String imsClientId = "DIM_REST_SERVICE_ACCOUNT";
        String imsClientSecret = "31d8bfb4-22e3-440b-bb45-487447607275";
        String imsClientCode = "eyJ4NXUiOiJpbXNfbmExLXN0ZzEta2V5LTEuY2VyIiwiYWxnIjoiUlMyNTYifQ.eyJpZCI6IkRJTV9SRVNUX1NFUlZJQ0VfQUNDT1VOVF9zdGciLCJjbGllbnRfaWQiOiJESU1fUkVTVF9TRVJWSUNFX0FDQ09VTlQiLCJ1c2VyX2lkIjoiRElNX1JFU1RfU0VSVklDRV9BQ0NPVU5UQEFkb2JlSUQiLCJ0eXBlIjoiYXV0aG9yaXphdGlvbl9jb2RlIiwiYXMiOiJpbXMtbmExLXN0ZzEiLCJvdG8iOiJmYWxzZSIsImV4cGlyZXNfaW4iOiIyNTkyMDAwMDAwMDAiLCJzY29wZSI6InN5c3RlbSxvcGVuaWQsQWRvYmVJRCIsImNyZWF0ZWRfYXQiOiIxNTc2MDEzMDc2Njc4In0.W9dMF9V0jQpZLFs1jlcIMshHPMKO0WbufZd6laMpKCuCckvd91SPX6RK7Jr07WlRf9PzzNNPNjuGqDjD_rzowYik24FryfZX8ymBIygvAQERtEhzTqu12WBNPvRCcs7xQbLwYQhNFDpUKZRVv590VML71dEwD4pyQfNaYmfr-FY1EF43w8QS-c4mnlTymGCyxm8MSySnYu4Zmp-iM0FNcsFa2VEuwm_QKvah0gT22wYyHrsHDuqf8LaKQMD1mYSh1cde2gSY51YpZNafaBYqJ6k3fJOn8pzgdvycfsmK3dLiQ1YLm2jY6XRbflSVYk2gt9jol5iFWeGCjb3rUYtI0Q";

        properties.setProperty("sasl.jaas.config",
                               "org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule required "
                               + "ims.token.url=\"" + imsTokenUrl + "\" "
                               + "ims.grant.type=\"authorization_code\" "
                               + "ims.client.id=\"" + imsClientId + "\" "
                               + "ims.client.secret=\"" + imsClientSecret + "\" "
                               + "ims.client.code=\"" + imsClientCode + "\";");
        properties.setProperty("security.protocol", "SASL_PLAINTEXT");
        properties.setProperty("sasl.mechanism", "OAUTHBEARER");
        properties.setProperty("sasl.login.callback.handler.class", "com.adobe.ids.dim.security.java.IMSAuthenticateLoginCallbackHandler");

        return properties;
    }

}
