. ./00-env.ps1
& "$Env:KAFKA_HOME/bin/windows/connect-standalone.bat" `
        $Env:KAFKA_HOME/config/connect-standalone.properties `
        $Env:KAFKA_HOME/config/connect-file-source.properties `
        $Env:KAFKA_HOME/config/connect-file-sink.properties
