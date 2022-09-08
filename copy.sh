#!/bin/bash

for dir in index.jsp view css js img
do
	cp -R src/main/webapp/${dir} /opt/apache-tomcat-9.0.36/webapps/wms/
done

cp -R src/main/resources/* /opt/apache-tomcat-9.0.36/webapps/wms/WEB-INF/classes/

