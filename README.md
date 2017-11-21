## Real time statistics

### Architecture
This service consists on a REST microservice built with the Kotlin language, over a InfluxDB database. 

### Requirements
- java 8+
- gradle
- docker/docker-compose

### Local development
To run the unit tests, spin up a influxdb instance (see `docker-compose.yml`) and then `./gradlew clean build`

### Local usage
To run it on local just type in `./gradlew clean build && docker-compose up`. The service will be available on port 7000