. ./00-env.ps1
$Env:KAFKA_CLUSTER_ID="9b44d396-e9eb-4b27-95fb-cd0a40ddb5d0"
& "$Env:KAFKA_HOME/bin/windows/kafka-storage.bat" format --standalone -t $Env:KAFKA_CLUSTER_ID -c $Env:KAFKA_HOME/config/server.properties
