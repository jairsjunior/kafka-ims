import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.ThreadLocalRandom;

public class ProducerOAuthSample {

    public static Logger log = LoggerFactory.getLogger(ProducerOAuthSample.class);

    public static void main(String[] args) {
        //Create the properties file
        Properties properties = configureProducer();
        //Create a new kafka producer
        Producer<String, String> producer = new KafkaProducer<>(properties);

        int i = 0;
        //Start to produce 10 cycles
        while (i < 10) {
            log.info("Producing batch: " + i);
            try {
                //Send a new event with the name `Person 1` and wait 100 millis
                producer.send(newEvent("Person 1"));
                Thread.sleep(100);
                //Send a new event with the name `Person 2` and wait 100 millis
                producer.send(newEvent("Person 2"));
                Thread.sleep(100);
                //Send a new event with the name `Person 3` and wait 100 millis
                producer.send(newEvent("Person 3"));
                Thread.sleep(100);
                i += 1;
            } catch (InterruptedException e) {
                log.error("Error at produce cycle: ", e);
                break;
            }
        }
        //Close the producer client
        producer.close();
    }

    private static ProducerRecord<String, String> newEvent(String name) {
        //Create an JSON Object
        ObjectNode transaction = JsonNodeFactory.instance.objectNode();
        ObjectNode singleTran = JsonNodeFactory.instance.objectNode();
        ObjectNode labels = JsonNodeFactory.instance.objectNode();

        //Mount of object
        singleTran.put("id", ThreadLocalRandom.current().nextInt(0, 9000));
        labels.put("account", ThreadLocalRandom.current().nextInt(0, 5));
        Integer amount = ThreadLocalRandom.current().nextInt(0, 100);
        transaction.set("transaction", singleTran);
        transaction.put("id", ThreadLocalRandom.current().nextInt(0, 900000));
        transaction.put("balance", amount);
        transaction.set("labels",labels);

        log.info("Json Event Object: " + transaction.toString());

        //Returns a new producer record to the topic `topic-name`, with the name and our event object
        return new ProducerRecord<>("topic-name", name, transaction.toString());
    }

    private static Properties configureProducer() {
        //Create the kafka.properties file to be used by this producer
        Properties properties = new Properties();

        // Broker addresses
        String bootstrapServers = "broker:9093";

        // kafka bootstrap server
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        // producer acks
        properties.setProperty(ProducerConfig.ACKS_CONFIG, "all");
        properties.setProperty(ProducerConfig.RETRIES_CONFIG, "3");
        properties.setProperty(ProducerConfig.LINGER_MS_CONFIG, "1");

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
