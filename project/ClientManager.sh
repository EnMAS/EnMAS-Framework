export CLASSPATH=$CLASSPATH:lib/akka-actor-2.0-M2.jar:lib/akka-remote-2.0-M2.jar
export CLASSPATH=$CLASSPATH:lib/netty-3.2.6.Final.jar:lib/protobuf-java-2.4.1.jar
export CLASSPATH=$CLASSPATH:lib/commons-io-2.0.1.jar
export CLASSPATH=$CLASSPATH:config:target/scala-2.9.1/enmas_2.9.1-0.5.jar

scala -cp $CLASSPATH org.enmas.client.ClientManager
