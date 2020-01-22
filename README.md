# kafka-ims plugins

This is a plugin for use on Confluent's Rest Proxy product. The principal purpose of this plugin is permit to use IMS Authentication server to control authentication and authorization using a BEARER in the header of HTTP call.

## Modules

- kafka-ims-common: Common functions to be used at another modules
- kafka-ims-java: AuthenticateCallbackHandler implementations used at Kafka broker and by Java Clients to handle IMS Authentication using SASL OAUTHBEARER.
- kafka-ims-rest: RestResourceExtension and AuthenticateCallbackHandler implementations that add handle of Bearer token sent on HTTP call to connect to a SASL using OAUTHBEREAR secured broker.
- kafka-ims-sample: Sample implementation of Producer/Consumer Java Clients using the kafka-ims-java dependencies and a swagger-ui project that contains the documentations of kafka-ims-rest calls.

## Building the jar files

### Requirements

- JDK 8 or above
- Maven 3.6 or above

### How to Build all modules

With all requirements installed, we just need to run the command ```mvn -e clean package``` on the root folder *kafka-ims* to generate all modules jar files. The jar files will be on each module folder on the target directory.

### How to Build a single module

With all requirements installed, we just need to run the command ```mvn -e -pl <module-name> -am clean package``` on the root folder *kafka-ims* to generate all modules jar files. The jar files will be at the module folder on the target directory.

Example: To build only the kafka-ims-java module and his dependencies we will be run the command ```mvn -e -pl kafka-ims-java -am clean package```

## Building the package
The package is built in docker using modern features.  

Ensure that docker is installed on your system:

### Ubuntu
Follow these instructions if you want to install docker using native Ubuntu mechanisms.

1. Follow the instructions at https://docs.docker.com/install/linux/docker-ce/ubuntu/.
2. Ensure that docker is running:
   ```bash
   systemctl start docker
   ```
3. If you want to run containers as your own account (rather than root), add your account to the "docker" group
and ensure you log out and back in.

### Using Homebrew/Linuxbrew
Follow these instructions if you want to install docker using homebrew.

```bash
# once
brew install make docker
# once for MacOS
brew cask install docker-edge
```

### Login to docker.io (once - after first install of docker)
This is required the first time you need to build something with docker.
```bash
docker login --username <docker.io username>
```

### Build the package
```bash
make rpm
```

## Deploy the distribution
Use your [API Key](https://wiki.corp.adobe.com/display/Artifactory/API+Keys) to deploy to Artifactory.  Prior
to doing this, you must have the jfrog CLI installed somewhere in your PATH.

### Using Homebrew/Linuxbrew
This example uses [Homebrew](https://brew.sh) or [Linuxbrew](https://linuxbrew.sh) to install the jfrog CLI.

```bash
brew install jfrog-cli-go
```

### Using Other OS
Download the jfrog CLI from https://jfrog.com/getcli/ and put it somewhere in your PATH

### Preparing jfrog CLI
After install, provider your user and API key to the CLI for future use.

```bash
jfrog rt c corp --url=https://artifactory.corp.adobe.com/artifactory --user=$USER --access-token=$API_KEY
```

### Deploying to Artifactory
```bash
make deploy
```

## Setup Docker Development Ambient

To test this generated jars we have a docker directory at this root project with the instructions to setup
and run all this components.
