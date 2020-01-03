# kafka-ims-rest

This is a plugin for use on kafka-rest product developed by confluentinc. The principal purpose of this plugin is permit to use
IMS Authentication server to control authentication and authorization using a BEARER in the header to HTTP/HTTPS call.

## Configure REST Proxy
* Add skinny jar that is created after building the module (kafka-sasl-oauth-handler-1.0-SNAPSHOT.jar, for example) to kafka lib directory (/usr/share/java/kafka/kafka-rest).
* Add following property.

| Property | Value |
| :-------:|:-----:|
| kafka.rest.resource.extension.class | com.adobe.ids.dim.security.rest.KafkaOAuthSecurityRestResourceExtension |
