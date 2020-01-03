kafka-acls --authorizer-properties zookeeper.connect=localhost:2181 \
     --remove --allow-principal User:DIM_SERVICE_ACCOUNT \
     --operation Read --topic test-topic --force

kafka-acls --authorizer-properties zookeeper.connect=localhost:2181 \
     --add --allow-principal User:DIM_SERVICE_ACCOUNT \
     --producer --topic test-topic
