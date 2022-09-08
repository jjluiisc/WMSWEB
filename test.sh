#!/bin/bash

JAVA_HOME=/usr
TOMCAT=/opt/apache-tomcat-9.0.36
WMS=/Users/joelbecerramiranda/Apps/REDER/WMSWEB/target/WMSWEB-1.0-SNAPSHOT/WEB-INF
LOG4J=${WMS}/log4j.properties
CLASSPATH=

CLASSPATH=${WMS}/classes:${WMS}/lib/AtCloudLibrary.jar
for jar in $(ls -1 ${TOMCAT}/lib/*.jar)
do
	CLASSPATH=$CLASSPATH:$jar
done 

#echo $CLASSPATH

${JAVA_HOME}/bin/java -Dlog4j=${LOG4J} -Decommerce.home=/Users/joelbecerramiranda/ecommerce -cp ${CLASSPATH} mx.reder.wms.util.Test "$@"

