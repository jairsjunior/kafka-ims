# Kafka IMS Rest Sample

This repository contains a [Swagger](https://swagger.io/) file with all the call listed on Confluent's Rest Proxy API documentation with new return codes and the authentication included with the IMS plugin.

## Swagger

The file *rest-proxy.yml* is the editable file contains all the Rest calls that can be made using the Confluent's Rest Proxy API. You can edit this file using one online tool like [Swagger Editor](https://editor.swagger.io/).

The file *rest-proxy.json* is the publish file that you can serve in one of the distributions of [Swagger UI](https://swagger.io/tools/swagger-ui/). This file can be generated at the Swagger Editor using the menu File > Convert and save as JSON.

## rest-documentation (Swagger UI)

This project contains a simple *Express server* to provide the *swagger-ui*. With this UI we can host the swagger file to
use the calls of API described on that file.

### Requirements

- NodeJS version (7.8 or above)
- NPM version (4.2 or above)

### How to start

With all requirements installed on the machine, we just need to run the command `npm start` on the *rest-documentation* folder. After the start of server the we can access the server using the address [http://localhost:3000](http://localhost:3000)
