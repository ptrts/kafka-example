. ./00-env.ps1
& "$Env:KAFKA_HOME/bin/windows/kafka-console-consumer.bat" --topic quickstart-events --from-beginning --bootstrap-server localhost:9092
