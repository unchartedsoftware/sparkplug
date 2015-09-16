## Sparkplug

#### About

**Sparkplug** is a library that allows a user to quickly and easily build an asynchronous messaging system between Spark and a remote application.

#### Testing

```
docker exec -it sparkplugcontainer_sparkmaster_1 /bin/bash
cd /opt/sparkplug/sparkplug-test
./gradlew build spark
```