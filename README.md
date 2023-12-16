# Spring Boot Rest Template SPREST
## Purpose
This project is meant as a template to quickly get up an running with a ReSTful backend for a new app or a micro service.
It provides basic capabilities that most applications need such as user management and serviceability, like e.g. 
changing log level at runtime, announcements, user impersonation for admins.
Integration Tests are setup out of the box to run with docker test containers.
## SPREST Roadmap
- [x] [provide intial db migration script](https://github.com/jacomac/spring-boot-rest-template/issues/1)
- [x] [mirror access rights in DB and create initial admin user](https://github.com/jacomac/spring-boot-rest-template/issues/2)
- [ ] make all tests work :recycle:
- [ ] add a small shopping list sample, so that there is something to do for regular users (v1.0) :notebook:
- [ ] provide support for [OAUTH2 Identity providers](https://spring.io/guides/tutorials/spring-boot-oauth2/) like Github & Google (v1.1) :tada:
- [ ] build docker image and upload to githhub repo :cherries:
- [ ] localization of messages in English / German (v1.2)

## How to build
```shell
$ ./mvn clean package
```
## How to run
from command line:
```shell
$ java -jar target/sprest-template-webapp-0.9.0-SNAPSHOT.jar
```
from within the IDE: run the main class in the webapp module
```
sprest.SprestApplication
```
## Runtime configuration
This micro service has enabled the following [Actuator endpoints](https://docs.spring.io/spring-boot/docs/2.0.x/actuator-api/html/):
* info - returns basic information about micro service instance
* health - reports health status
* loggers - allows adjusting logging levels
* flyway - reports Flyway migrations

The basic path for the aforementioned endpoints is `/api/actuator`. 
All endpoints are secured and require `MANAGE_SYSTEM_SETTINGS` authority.

### Change log level at runtime
To change the log level at runtime execute the following command from a terminal:

```shell
$ curl -v http://[HOST]:[PORT]/api/actuator/loggers/sprest \
  -H "Cookie: JSESSIONID=[COOKIE_ID]" \
  -X POST -H 'Content-Type: application/json' \
  -d '{"configuredLevel":"debug"}'
```
Replace `HOST`, `PORT` and `COOKIE_ID` with proper values depending on the environment. 
Refer to the [actuator documentation](https://docs.spring.io/spring-boot/docs/2.0.x/actuator-api/html/#loggers) for more details.

## Useful Commands
### Database dump 
when using in postgre docker container:
```
docker exec deployment-postgres-1 pg_dump sprest -U sprest_user > /home/jacomac/dumps/V1_intial_setup.sql
```

