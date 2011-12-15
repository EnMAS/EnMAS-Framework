export CLASSPATH=$CLASSPATH:lib/akka/akka-actor-1.2.jar:lib/akka/akka-remote-1.2.jar
export CLASSPATH=$CLASSPATH:lib/akka/netty-3.2.5.Final.jar:lib/akka/protobuf-java-2.4.1.jar
export CLASSPATH=$CLASSPATH:lib/akka/commons-io-2.0.1.jar
export CLASSPATH=$CLASSPATH:dist/EnMAS.jar

scala -cp $CLASSPATH org.enmas.bundler.Bundler