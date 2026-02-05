. ./00-env.ps1
& "$Env:KAFKA_HOME/bin/windows/kafka-console-producer.bat" --topic quickstart-events --bootstrap-server localhost:9092
