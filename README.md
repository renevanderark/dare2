# Dare 2

Initial setup

## Setting up the database

This application expects a database with support for jdbc. 

The current development version expects a MySQL database to be present as configured in development.yaml

sql dump files will follow.


## Building and running for development

```sh
mvn clean package
./target/appassembler/bin/run server development.yaml
```