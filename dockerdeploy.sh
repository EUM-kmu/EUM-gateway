#!/bin/sh

#port=`lsof -i :8000 -t`

#kill -9 $port
#git checkout dev
#git pull origin dev
./gradlew build -x test
docker-compose up --build
#nohup ./gradlew bootRun > gradlew.log 2>&1 &
