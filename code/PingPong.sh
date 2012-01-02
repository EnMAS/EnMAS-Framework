export CLASSPATH=$CLASSPATH:lib/akka-2_0-M1/akka-actor-2.0-M1.jar:lib/akka-2_0-M1/akka-remote-2.0-M1.jar
export CLASSPATH=$CLASSPATH:resources/akka-pingpong
scala -cp $CLASSPATH PingPong