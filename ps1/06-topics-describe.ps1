. ./00-env.ps1
& "$Env:KAFKA_HOME/bin/windows/kafka-topics.bat" --describe --topic quickstart-events --bootstrap-server localhost:9092
