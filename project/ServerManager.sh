export CLASSPATH=$CLASSPATH:lib_managed/jars/com.typesafe.akka/akka-actor/akka-actor-2.0-M3.jar
export CLASSPATH=$CLASSPATH:lib_managed/jars/com.typesafe.akka/akka-remote/akka-remote-2.0-M3.jar
export CLASSPATH=$CLASSPATH:lib_managed/bundles/io.netty/netty/netty-3.3.0.Final.jar
export CLASSPATH=$CLASSPATH:lib_managed/jars/com.google.protobuf/protobuf-java/protobuf-java-2.4.1.jar
export CLASSPATH=$CLASSPATH:lib_managed/jars/commons-io/commons-io/commons-io-1.4.jar
export CLASSPATH=$CLASSPATH:config:target/scala-2.9.1/enmas_2.9.1-0.5.jar

scala -cp $CLASSPATH org.enmas.server.ServerManager
