FROM confluentinc/cp-kafka:5.3.1

RUN curl https://repo1.maven.org/maven2/org/jolokia/jolokia-jvm/1.6.2/jolokia-jvm-1.6.2-agent.jar -o /usr/share/java/kafka/jolokia-jvm-1.6.2-agent.jar

# This script needs to be changed because of the name of property that use _ (underscore) characther on his name and
# the start script of this container translate environment name with _ to . (dot)
# In this script we make a handling to use __ to translated to _
ADD scripts/dub* /usr/local/lib/python2.7/dist-packages/confluent/docker_utils/

# Include configuration files to kafka folder
ADD config/kafka* /etc/kafka/

# Include jar plugin to kafka jars folder
ADD libs/kafka* /usr/share/java/kafka/

