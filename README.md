# Spring Boot Rest Template SPREST
![key visual for the SPREST project](docs/img/social-preview-sprest.jpg)
## Purpose
This project is meant as a template to quickly get up an running with a java spring boot ReSTful backend for a new app or a micro service.
It provides basic capabilities that most applications need such as user management and serviceability, like e.g. 
changing log level at runtime and making announcements.
Integration tests are setup out of the box to run with docker test containers.
## SPREST Roadmap
- [x] [provide intial db migration script](https://github.com/jacomac/spring-boot-rest-template/issues/1)
- [x] [mirror access rights in DB and create initial admin user](https://github.com/jacomac/spring-boot-rest-template/issues/2)
- [x] [make all tests work](https://github.com/jacomac/spring-boot-rest-template/issues/3) :recycle:
- [x] [add a small shopping list sample](https://github.com/jacomac/spring-boot-rest-template/issues/4), so that there is something to do for regular users (v1.0) :notebook:
- [ ] provide support for [OAUTH2 Identity providers](https://spring.io/guides/tutorials/spring-boot-oauth2/) like Github & Google (v1.1) :tada:
- [ ] build docker image and upload to a docker repository :cherries:
- [ ] localization of messages in English / German (v1.2)

## Prerequisites
* Java 17 LTS / Maven 3
* ensure you have [Docker Desktop](https://docs.docker.com/desktop/) running
* a Java IDE for further development, IntelliJ recommended

## How to build
```shell
$ ./mvn clean package
```
## How to run the App
### database
1. go to [deployment](deployment/) sub folder and open a bash command line
2. the frist tiem, you need to copy the `.env.template` to `.env` (customization possible, but not necessary) 
3. start postgres database as docker container with
```shell
$ ./postres.sh up -d
```
The postgres database server is now listening on localhost on the default port and the sprest db is ready to be used.
The shell script `postgres.sh` is just a shortcut and takes all the regular docker-compose commands, 
such as `down` for stopping the container again.

### application server
from command line:
```shell
$ java -jar target/sprest-template-webapp-0.9.0-SNAPSHOT.jar
```
from within the IDE: run the main class in the webapp module
```
sprest.SprestApplication
```
## How to run the Integration Tests
The integration tests are run using docker test containers, so ensure docker desktop has been started. 
Then you either run them with your regular maven build or inside your IDE with no further preparation steps.

## Further Documentation
More documentation can be found in the [docs folder](docs/overview-sprest.md)