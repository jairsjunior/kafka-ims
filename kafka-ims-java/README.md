# kafka-ims-java

This is an implementation of the idea that was circulated as part of [KIP-255](https://cwiki.apache.org/confluence/pages/viewpage.action?pageId=75968876) that makes it possible to use IMS to authenticate Java clients against Kafka clusters.

## Configure brokers

* Add skinny jar files that is created after building the modules to kafka lib directory (/usr/share/java/kafka). 
List of jar files needed:

  * kafka-ims-common-\<version\>.jar
  * kafka-ims-java-\<version\>.jar
  
Note that these jars are deployed by installing kafka-ims-java-\<version\>.noarch.rpm on brokers.

* Start the brokers with following JAAS configuration. IMS token validation URL depends on the environment.

```conf
KafkaServer {
    org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule required
    ims.token.validation.url="<URL>"
    LoginStringClaim_sub="admin";
};
```

* In addition, brokers need following properties set.

| Property | Value |
| :-------:|:-----:|
| listener.name.sasl_plaintext.oauthbearer.sasl.server.callback.handler.class | com.adobe.ids.dim.security.java.IMSAuthenticateValidatorCallbackHandler |
| sasl.enabled.mechanisms | OAUTHBEARER |

## Configure clients

* Add fat jar that is created after building the module (kafka-ims-uber-client-\<version\>.jar) to the classpath of the client. Note that this jar is deployed by installing kafka-ims-uber-client-\<version\>.noarch.rpm on the client.
* Java clients need to set following properties in the code (or via settings).

| Property | Value |
| :-------:|:-----:|
| security.protocol | SASL_PLAINTEXT |
| sasl.mechanism  | OAUTHBEARER |
| sasl.login.callback.handler.class | com.adobe.ids.dim.security.java.IMSAuthenticateLoginCallbackHandler |

* In addition, clients need to be started with following JAAS configuration. IMS token  URL depends on the environment. Client ID, client secret and client code are issued to each client when they register with IMS and are unique to each client.

```conf
KafkaClient {
    org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule required
    ims.token.url="<URL>"
    ims.grant.type="authorization_code" 
    ims.client.id="<Client ID>"
    ims.client.secret="<Client Secret>"
    ims.client.code="<Client Code>";
};
```
