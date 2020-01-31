# Getting Started

## Build this project jar file

Our first step is build the jar file of our project. You can use your IDE of preference or the maven client to assembly the jar file. The instructions to generate the jar files are on the root project README.md file at the sections *Building the jar files*

## Change the content of `kafka_rest_proxy_jaas.conf`

First, we need to change the file at folder rest-proxy/config/kafka_rest_proxy_jaas.conf, filling the properties `ims.token.url` with the complete url of your IMS server, `ims.client.id` with your client id, `ims.client.secret` with your secret and `ims.client.code` with your access_token code.

Example:

- ims.token.url="http://yourserver.com/ims/token/v1"
- ims.client.id="YOUR_CLIENT_ID_HERE"
- ims.client.secret="YOUR_CLIENT_SECRET_HERE"
- ims.client.code="YOUR_ACCESS_TOKEN_CODE_HERE"

## Change the content of `kafka_rest_proxy_jaas.conf`

We need to change the file at folder cp-kafka-oauth/config/kafka_server_jaas.conf, filling the propertie `ims.token.validation.url` with the complete validation url of your IMS server.

Example:

- ims.token.validation.url="http://yourserver.com/ims/validate_token/v1"

## Build the Containers

To build the containers needed to test our implementation, you will execute the shell script `build.sh`. At the end of this execution the containers needed will be builded.

## Run the `docker-compose.yml` file

To run all the containers needed to make our tests, you will execute the shell script `run.sh`. The result of this execution is run five containers(zookeeper,kafka-broker,schema-registry and rest-proxy) with all environments configured.
After run all this conteiners, the shell script tail the logs of the rest-proxy container.

## Executing Tests

Here, the first step is get your *access_token* using your credentials with your IMS Authentication Server. With the access_token in hands you can replace the ENVIRONMENT VARIABLE at the `test.sh` file named BEARER_TOKEN and save the file.

You can access the control-center and create a topic called **test-topic**

After this, you can run the `test.sh` file. This file had some basic operations:
    - Produce one message to an topic
    - Create a consumer for JSON data
    - Set the topic that will be consumed
    - Consume the messages of the topic
    - Delete the consumer instance

# JMX Metrics

We created JMX metrics to monitor some behaviours at the broker and kafka-rest. Above we will detail a little be more this metrics and how to visualize using [Hawtio](https://hawt.io/).

## Broker Metrics

### kafka-broker:name=ims-metrics

- CountOfRequestFailedWithoutScope: Counter of connections using the AuthenticatorCallbackHandler implementation trying to connect with a valid token but without scope

## Kafka-Rest Metrics

### kafka.rest:type=ims-metrics,name=ims-request-metrics-total

- ExpiredTokenErrorCount: Counter of requests made with a expired token
- InvalidTokenErrorCount: Counter of requests made with a invalid token
- SuccessfullRequestCount: Counter of requests made with success
- WithoutScopeErrorCount: Counter of requests trying to connect with a valid token but without scope

### kafka.rest:type=ims-acl-metrics,name=ims-acl-metrics-total

- ACLDeniedRequestCount : Count of requests trying to connect with a valid token but without ACL

### kafka.rest:type=ims-metrics,endpoint=[endpoint-address]

- ExpiredTokenErrorCount: Counter of requests made with a expired token at this endpoint
- InvalidTokenErrorCount: Counter of requests made with a invalid token at this endpoint
- SuccessfullRequestCount: Counter of requests made with success at this endpoint
- WithoutScopeErrorCount: Counter of requests trying to connect with a valid token but without scope at this endpoint

### kafka.rest:type=ims-acl-metrics,topic=[topic-name],endpoint=[endpoint-address]

- ACLDeniedRequestCount : Count of requests trying to connect with a valid token but without ACL by topic and endpoint

# Hawtio

Hawtio is a lightweight and modular Web console with lots of plugins for managing your Java stuff. We using it to watch our metrics at our docker setup.

## Accessing the UI

At our docker-compose we started the hawtio in an container and exposed the port *8080* to access the service. You can use the address `http://localhost:8080` to access the application.

## Configuring Remote Metrics

To connect to our another container we need to setup 2 new connections at the Hawtio application, one for the broker and another to kafka-rest. To create a new connection we will use the `Connect` menu at the right side of the screen and click on the `add connection` button.

### Broker Connection

At the `Add Connection` modal, we will fill the inputs with the above information and click at `Test Connection` button, if everything is right we will receive a message with the text `Connection Sucessful` and we can save this connection using the `Save` button.

- Name: Broker
- Scheme: http
- Host: broker
- Port: 49998
- Path: /jolokia

### Kafka-Rest Connection

At the `Add Connection` modal, we will fill the inputs with the above information and click at `Test Connection` button, if everything is right we will receive a message with the text `Connection Sucessful` and we can save this connection using the `Save` button.

- Name: Kafka-Rest
- Scheme: http
- Host: rest-proxy
- Port: 49998
- Path: /jolokia

## Watching Metrics

At the `Connect` menu on the tab Remote now we had 2 connections (Broker,Kafka-Rest). To watch the metric at each one we will use the `Connect` button of each configuration. A new window will be opened and there we had the `JMX` menu with all MBeans registered at this JVM.

### Hawtio Broker Metrics

Our metrics will be at the `kafka-broker` folder on the tree visualization.

### Hawtion Kafka-Rest Metrics

Our metrics will be at the `kafka.rest` folder on the tree visualization. Inside this folder we will had another 2 folders with more specific metrics (ims-acl-metrics, ims-metrics).
