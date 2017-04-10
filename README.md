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
$ mvn clean package
$ ./target/appassembler/bin/run server development-mysql.yaml # for mysql config
$ ./target/appassembler/bin/run server development.yaml # for oracle config
``` 

## Loading database schemae

Mysql (based on development-mysql.yaml). First create database named 'dare' and grant all priviliges to user named 
daredev having password daredev
```sh
$ mysql -udaredev -pdaredev dare < schema.sql
```

Oracle (based on development.yaml)
```sh
$ ./target/appassembler/bin/run server development.yaml (start server)
$ curl -X POST http://localhost:8081/tasks/create-oracle-schema
```
This setup was tested with Oracle XE in a docker:
[wnameless/docker-oracle-xe-11g](https://github.com/wnameless/docker-oracle-xe-11g)