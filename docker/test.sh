#!/bin/sh

BEARER_TOKEN="eyJ4NXUiOiJpbXNfbmExLXN0ZzEta2V5LTEuY2VyIiwiYWxnIjoiUlMyNTYifQ.eyJpZCI6IjE1NzY2MzAxMzg4OThfYmVlM2JmODEtNmU2My00MGQxLTg5YTktNTcwMWQxOTgwMDE4X3VlMSIsImNsaWVudF9pZCI6IkRJTV9TRVJWSUNFX0FDQ09VTlQiLCJ1c2VyX2lkIjoiRElNX1NFUlZJQ0VfQUNDT1VOVEBBZG9iZUlEIiwidHlwZSI6ImFjY2Vzc190b2tlbiIsImFzIjoiaW1zLW5hMS1zdGcxIiwicGFjIjoiRElNX1NFUlZJQ0VfQUNDT1VOVF9zdGciLCJydGlkIjoiMTU3NjYzMDEzODg5OF80NTQ5ZTdkNi0wNjJhLTQzNzYtOGYxYi05Y2IzZjQzYjVmMGJfdWUxIiwicnRlYSI6IjE1Nzc4Mzk3Mzg4OTgiLCJtb2kiOiJlM2NiMTk2MCIsImMiOiJCQ0dKY0gxSmRmWmZILzlCbDMxWnpnPT0iLCJleHBpcmVzX2luIjoiODY0MDAwMDAiLCJzY29wZSI6InN5c3RlbSxvcGVuaWQsQWRvYmVJRCxkaW0uY29yZS5zZXJ2aWNlcyIsImNyZWF0ZWRfYXQiOiIxNTc2NjMwMTM4ODk4In0.F3kVB8lRx1vdQ6c55506cnkLb5JiJx2U43ioVg-50wlpuYhGO7_sFe6zLIvdm7fquyzvyqh6a7vJdWZ7muoS0iJZsJKav2JU2e90pjaNx-jlpDVfll-bPTXByI8chE8lzzCyXrgGVT3PS1AmWlo68XMZpR1WWmtvb58Zw3B4z7kr1-CCLXLE4NTYX7dgBogX4lpYM8ozhwnSUuhcE68a1xXCv8L79xz7u8a7aRHAIPRHqdGPpbQyG_fkUaM_eSVII0f8KdJK5JqbKb12AooNWn81BO_KpJbvapfrhgp6thde8sCRLjyoxyPrkcBMWtArumGUhnBoWR4C0-DE9HrpHg"

# Produce a message using JSON with the value '{ "foo": "bar" }' to the topic test-topic
curl -X POST -H "Content-Type: application/vnd.kafka.json.v2+json" \
      -H "Accept: application/vnd.kafka.v2+json" \
      -H "Authorization: Bearer $BEARER_TOKEN" \
      --data '{"records":[{"value":{"foo":"bar"}}]}' "http://localhost:8082/topics/test-topic"

# Expected output from preceding command
#   {
#    "offsets":[{"partition":0,"offset":0,"error_code":null,"error":null}],"key_schema_id":null,"value_schema_id":null
#   }

echo ""
sleep 10

# Create a consumer for JSON data, starting at the beginning of the topic's
# log and subscribe to a topic. Then consume some data using the base URL in the first response.
# Finally, close the consumer with a DELETE to make it leave the group and clean up
# its resources.
curl -X POST -H "Content-Type: application/vnd.kafka.v2+json" \
      -H "Authorization: Bearer $BEARER_TOKEN" \
      --data '{"name": "my_consumer_instance", "format": "json", "auto.offset.reset": "earliest"}' \
      http://localhost:8082/consumers/my_json_consumer


echo ""

# Expected output from preceding command
#  {
#   "instance_id":"my_consumer_instance",
#   "base_uri":"http://localhost:8082/consumers/my_json_consumer/instances/my_consumer_instance"
#  }

curl -X POST -H "Content-Type: application/vnd.kafka.v2+json" --data '{"topics":["test-topic"]}' \
      -H "Authorization: Bearer $BEARER_TOKEN" \
      http://localhost:8082/consumers/my_json_consumer/instances/my_consumer_instance/subscription
# No content in response

echo ""

curl -X GET -H "Accept: application/vnd.kafka.json.v2+json" \
      -H "Authorization: Bearer $BEARER_TOKEN" \
      http://localhost:8082/consumers/my_json_consumer/instances/my_consumer_instance/records

# Expected output from preceding command
#   [
#    {"key":null,"value":{"foo":"bar"},"partition":0,"offset":0,"topic":"test-topic"}
#   ]

# curl -X DELETE -H "Content-Type: application/vnd.kafka.v2+json" \
#       -H "Authorization: Bearer $BEARER_TOKEN" \
#       http://localhost:8082/consumers/my_json_consumer/instances/my_consumer_instance
# No content in response
