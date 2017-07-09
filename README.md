# coinwatch

You get to watch coins

## Usage

### Run the application locally

`lein ring server-headless`

### Run the tests

`lein test`

### Packaging and running as standalone jar

```
lein do clean, ring uberjar
java -jar target/server.jar
```

### Packaging as war

`lein ring uberwar`
