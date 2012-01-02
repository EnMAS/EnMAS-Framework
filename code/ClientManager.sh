export CLASSPATH=$CLASSPATH:lib/akka-2.0-M2/akka-actor-2.0-M2.jar:lib/akka-2.0-M2/akka-remote-2.0-M2.jar
export CLASSPATH=$CLASSPATH:lib/akka-2.0-M2/netty-3.2.6.Final.jar:lib/akka-2.0-M2/protobuf-java-2.4.1.jar
export CLASSPATH=$CLASSPATH:lib/akka/commons-io-2.0.1.jar
export CLASSPATH=$CLASSPATH:resources:dist/EnMAS.jar

scala -cp $CLASSPATH org.enmas.client.ClientManager