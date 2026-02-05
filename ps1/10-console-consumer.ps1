. ./00-env.ps1
& "$Env:KAFKA_HOME/bin/windows/kafka-console-consumer.bat" --bootstrap-server localhost:9092 --topic connect-test --from-beginning
