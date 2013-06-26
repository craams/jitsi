mkdir -p $HOME/.sip-communicator/log

export PATH=$PATH:native
export JAVA_HOME=jre
${JAVA_HOME}/bin/java -classpath "lib/bcprovider.jar:lib/felix.jar:sc-bundles/sc-launcher.jar:sc-bundles/util.jar" -Djna.library.path=native -Djava.library.path=native -Dfelix.config.properties=file:./lib/felix.client.run.properties -Djava.util.logging.config.file=lib/logging.properties net.java.sip.communicator.launcher.SIPCommunicator