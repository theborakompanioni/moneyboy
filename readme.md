abby
===============

ABB test server.


## Build

### Setup

### Package
```
mvn clean package
```

## Test
```
mvn test
```

## Run
```
mvn clean install package
java -jar abby-boot/target/abby-boot-${version}-fat.jar -conf bin/sample-conf.json
```
