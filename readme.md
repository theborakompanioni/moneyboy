moneta
===============
Moneta is an API for current and historical currency exchange rates.

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

## API
Get the latest foreign exchange reference rates.
Request specific exchange rates by setting the `target` parameter. 
```
GET /api/latest&target=USD
{
  "base" : "EUR",
  "date" : "2016-07-25",
  "rates" : [ {
    "base" : "EUR",
    "derived" : false,
    "factor" : 1.0982,
    "provider" : "ECB",
    "target" : "USD",
    "type" : "DEFERRED"
  },
  ... 
  ]
}
```

Rates are quoted against the Euro by default. 
Quote against a different currency by setting the base parameter in your request.

```
GET /api/latest&base=USD&target=EUR
{
  "base" : "USD",
  "date" : "2016-07-25",
  "rates" : [ {
    "base" : "USD",
    "derived" : false,
    "factor" : 0.9105809506465125,
    "provider" : "ECB",
    "target" : "EUR",
    "type" : "DEFERRED"
  } ]
}
```

Get historical rates from a specific date.
```
GET /api/2016-04-13?target=GBP&target=USD
{
  "base" : "EUR",
  "date" : "2016-04-13",
  "rates" : [ {
    "base" : "EUR",
    "derived" : true,
    "factor" : 0.7929527753214476,
    "provider" : "IMF-HIST",
    "target" : "GBP",
    "type" : "HISTORIC"
  }, {
    "base" : "EUR",
    "derived" : true,
    "factor" : 1.1298043500954777,
    "provider" : "IMF-HIST",
    "target" : "USD",
    "type" : "HISTORIC"
  } ]
}
```

## Links
https://otaviojava.gitbooks.io/money-api
