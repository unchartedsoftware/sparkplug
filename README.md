## Sparkplug

#### About

**Sparkplug** is a Spring-based library that allows a user to quickly and easily build an asynchronous messaging system between Spark and a remote application.
  To use it, simply `@Import({SparkplugConfiguration.class, RabbitAutoConfiguration.class})`, and add the following values to your `application.properties`
  (or `application.yaml`) file:

```
spring.rabbitmq.host=rabbitmq
spring.rabbitmq.port=5672
spring.rabbitmq.username=admin
spring.rabbitmq.password=sparkplug
spring.rabbitmq.virtualHost=/

spring.rabbitmq.dynamic=true
```

The following properties can also be added; the default values are as shown:

```
sparkplug.spark-master=null # null tells us to use u local spark context, rather than connecting to a remote instance
sparkplug.spark-app-name=Sparkplug

sparkplug.inbound-exchange=sparkplug-inbound
sparkplug.inbound-routing-key=sparkplug-request

sparkplug.outbound-exchange=sparkplug-outbound
sparkplug.outbound-routing-key=sparkplug-request

sparkplug.session-timeout=1800000 # 30 minutes, in seconds
```

You then implement the `CommandHandler` class (multiple times) and register it to listen for a specific command key in the `SparkplugListener` (which is
exposed as a bean, so you can `@Autowire` it).

Upon deployment to Spark, it connects to a specified RabbitMQ server and creates two exchanges - `sparkplug-inbound` and `sparkplug-outbound` (the names can
be overridden as described above).

#### Testing

```
docker exec -it sparkplugcontainer_sparkmaster_1 /bin/bash
cd /opt/sparkplug/sparkplug-test
./gradlew build spark
```
