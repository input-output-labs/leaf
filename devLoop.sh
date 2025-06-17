#!/bin/bash

while :
do
	mvn install
	cd demo
	mvn spring-boot:run
	cd ..
	echo "Press [CTRL+C] to stop.."
	sleep 1
done
