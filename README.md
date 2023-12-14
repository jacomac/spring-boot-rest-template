# Spring Boot Rest Template SPREST
## How to build
```shell
$ ./mvn clean package
```
## How to run
```shell
$ java -jar target/sprest-template-webapp-0.0.9-SNAPSHOT.jar
```

## Runtime configuration
This micro service has enabled the following [Actuator endpoints](https://docs.spring.io/spring-boot/docs/2.0.x/actuator-api/html/):
* info - returns basic information about micro service instance
* health - reports health status
* loggers - allows adjusting logging levels
* flyway - reports Flyway migrations

The basic path for the aforementioned endpoints is `/api/actuator`. 
All endpoints are secured and require `BasicUserRight.MANAGE_SYSTEM_SETTINGS` authority.

### Change log level at runtime
To change the log level at runtime execute the following command from a terminal:

```shell
$ curl -v http://[HOST]:[PORT]/api/actuator/loggers/sprest \
  -H "Cookie: JSESSIONID=[COOKIE_ID]" \
  -X POST -H 'Content-Type: application/json' \
  -d '{"configuredLevel":"debug"}'
```
Replace `HOST`, `PORT` and `COOKIE_ID` with proper values depending on the environment. 
Refer to the [documentation](https://docs.spring.io/spring-boot/docs/2.0.x/actuator-api/html/#loggers) for more details.