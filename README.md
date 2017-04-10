# Dare 2

Initial setup

[![build status](https://travis-ci.org/renevanderark/dare2.svg?branch=master)](https://travis-ci.org/renevanderark/dare2)

## Setting up the database

This application expects a database with support for jdbc. 

The current development version expects a MySQL or Oracle database to be present as configured in development.yaml

```
+--------------------+         +----------------------+       +-------------------+
|repositories        |         |oai_records           |       |oai_record_errors  |
+--------------------+         +----------------------+       +-------------------+
|*id                 +-1--+    |*identifier           +-1---m-+record_identifier  |
|name                |    +--m-+repository_id         |       |datestamp          |
|url                 |    |    |datestamp             |       |message            |
|metadataPrefix      |    |    |oai_status_code       |       |url                |
|oai_set             |    |    |process_status_code   |       |stacktrace         |
|datestamp           |    |    |update_count          |       |status_code        |
|enabled             |    |    |total_file_size       |       +-------------------+
+--------------------+    |    +----------------------+
                          |
                          |                                   +-------------------+
                          |                                   |harvester_errors   |
                          |                                   +-------------------+
                          +---------------------------------m-+repository_id      |
                                                              |url                |
                                                              |message            |
                                                              |stacktrace         |
                                                              |datestamp          |
                                                              |status_code        |
                                                              +-------------------+

```


## Building and running for development

```sh
mvn clean package
./target/appassembler/bin/run server development.yaml
``` 
