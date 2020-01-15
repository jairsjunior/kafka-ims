# kafka-ims-rest

This is a plugin for use at Confluent Rest Proxy product. The principal purpose of this plugin is permit to use IMS Authentication server to control authentication and authorization using a BEARER in the header of a HTTP call.

## Configure REST Proxy

* Add skinny jar files that is created after building the modules to kafka lib directory (/usr/share/java/kafka/kafka-rest). List of jar files needed:

  * kafka-ims-common-\<version\>.jar
  * kafka-ims-java-\<version\>.jar
  * kafka-ims-rest-\<version\>.jar

* Add following properties.

| Property | Value |
| :-------:|:-----:|
| security.protocol | SASL_PLAINTEXT |
| sasl.mechanism  | OAUTHBEARER |
| kafka.rest.resource.extension.class | com.adobe.ids.dim.security.rest.KafkaOAuthSecurityRestResourceExtension |
| sasl.login.callback.handler.class | com.adobe.ids.dim.security.java.IMSAuthenticateLoginCallbackHandler |
| ims.rest.client.sasl.login.callback.handler.class | com.adobe.ids.dim.security.rest.IMSAuthenticateRestCallbackHandler |

* In addition, clients need to be started with following JAAS configuration. IMS token URL depends on the environment. Client ID, client secret and client code are issued to each client when they register with IMS and are unique to each client.

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

## Configure clients

* Clients need to get the `access_token` with the IMS Server and send it on HTTP header `Authorization` with the prefix `Bearer `.

Example:
```sh
curl http://yourserver.com/brokers \
    -H "Authorization: Bearer eyJ4NXUiOiJpbXNfbmExLXN0ZzEta2V5LTEuY2VyIiwiYWxnIjoiUlMyNTYifQ.eyJpZCI6IjE1NzYxOTYxMjA1NjBfMjIzMWYyODMtMDAxMi00OWFkLTk5MDQtNDQ2ODk2NTc4NjZjX3VlMSIsImNsaWVudF9pZCI6IkRJTV9TRVJWSUNFX0FDQ09VTlQiLCJ1c2VyX2lkIjoiRElNX1NFUlZJQ0VfQUNDT1VOVEBBZG9iZUlEIiwidHlwZSI6ImFjY2Vzc190b2tlbiIsImFzIjoiaW1zLW5hMS1zdGcxIiwicGFjIjoiRElNX1NFUlZJQ0VfQUNDT1VOVF9zdGciLCJydGlkIjoiMTU3NjE5NjEyMDU2MF81YWE1MTcyOS04YzkzLTRlMWUtOWIzNC1mY2YxZGRiNzlkNjRfdWUxIiwicnRlYSI6IjE1Nzc0MDU3MjA1NjAiLCJtb2kiOiIyNGZjNmJlNSIsImMiOiJwWnpjOEFsbVR2cElQS2dwaUNTSGh3PT0iLCJleHBpcmVzX2luIjoiODY0MDAwMDAiLCJzY29wZSI6InN5c3RlbSxvcGVuaWQsQWRvYmVJRCxkaW0uY29yZS5zZXJ2aWNlcyIsImNyZWF0ZWRfYXQiOiIxNTc2MTk2MTIwNTYwIn0.B0uO6LxkVNuQ4kqa8UrxVkCR1hmOLerSrq0zT7ssJiIPZh5siKDSDyMNgAuimdS7gEfooXp-yUMxKORKeZRBbyh-32jrPWpXYTUqbg0RyvWl3CaaWOAwQB5LbEKeBQgMQW_2dOmk-X1gbyogcVZXvs-pgmjEnnHhvc4SJAZuGzeuhf263Ck1fOLlFqIQWm3-o9k4mkoLPTsFeEnh7X8HNYTpCTFgqVnetAHaqvV-pV3bFz0h02ujwouQF2ESpZdU_qNsqwnLqDDxQD-HyHaOeqWUaORh4Wd7LXrZSpdImKXEaNCcjvcBt88GbWanc5Unv7IAROKR5WQr4YNhxxUT5w"
```
