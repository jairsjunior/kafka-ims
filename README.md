# kafka-ims plugins

## How to Build

TODO

## How to Use

TODO

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
