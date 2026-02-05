. ./00-env.ps1
$Env:KAFKA_LOG4J_OPTS="-Dlog4j2.configurationFile=file:/$Env:KAFKA_HOME/config/tools-log4j2.yaml"
& "$Env:KAFKA_HOME/bin/windows/kafka-server-start.bat" $Env:KAFKA_HOME/config/server.properties
