BEGIN
   EXECUTE IMMEDIATE 'DROP TABLE harvester_errors';
EXCEPTION
   WHEN OTHERS THEN NULL;
END;

CREATE TABLE harvester_errors (
  repository_id number(10) DEFAULT NULL,
  url varchar2(255) DEFAULT NULL,
  message varchar2(255) DEFAULT NULL,
  stacktrace clob,
  datestamp varchar2(50) DEFAULT NULL,
  status_code number(10) DEFAULT NULL
)

CREATE INDEX harvester_error_index ON harvester_errors (datestamp,repository_id)

CREATE INDEX harvester_error_index1 ON harvester_errors (status_code)

BEGIN
   EXECUTE IMMEDIATE 'DROP TABLE oai_record_errors';
EXCEPTION
   WHEN OTHERS THEN NULL;
END;

CREATE TABLE oai_record_errors (
  record_identifier varchar2(128) DEFAULT NULL,
  datestamp varchar2(50) DEFAULT NULL,
  message varchar2(1024) DEFAULT NULL,
  url varchar2(1024) DEFAULT NULL,
  stacktrace clob,
  status_code number(10) DEFAULT NULL
)

CREATE INDEX oai_record_error_index ON oai_record_errors (datestamp,record_identifier)

CREATE INDEX oai_error_index1 ON oai_record_errors (status_code)

BEGIN
   EXECUTE IMMEDIATE 'DROP TABLE oai_records';
EXCEPTION
   WHEN OTHERS THEN NULL;
END;

CREATE TABLE oai_records (
  identifier varchar2(128) NOT NULL,
  datestamp varchar2(50) DEFAULT NULL,
  repository_id number(10) DEFAULT NULL,
  oai_status_code number(10) DEFAULT NULL,
  process_status_code number(10) DEFAULT NULL,
  update_count number(10) DEFAULT 0 NOT NULL,
  total_file_size NUMBER(19) DEFAULT 0 NOT NULL,
  PRIMARY KEY (identifier)
)

CREATE INDEX oai_record_index ON oai_records (datestamp,repository_id)

CREATE INDEX oai_record_index2 ON oai_records (oai_status_code)

CREATE INDEX oai_record_index3 ON oai_records (process_status_code)

BEGIN
   EXECUTE IMMEDIATE 'DROP TABLE repositories';
EXCEPTION
   WHEN OTHERS THEN NULL;
END;

CREATE TABLE repositories (
  id number(10) NOT NULL,
  name VARCHAR2(255) DEFAULT NULL,
  url varchar2(255) DEFAULT NULL,
  metadataPrefix varchar2(255) DEFAULT NULL,
  oai_set varchar2(50) DEFAULT NULL,
  datestamp varchar2(50) DEFAULT NULL,
  enabled NUMBER(3) DEFAULT 0 NOT NULL,
  PRIMARY KEY (id)
)

BEGIN
  EXECUTE IMMEDIATE 'DROP SEQUENCE repositories_seq';
EXCEPTION
  WHEN OTHERS THEN NULL;
END;

CREATE SEQUENCE repositories_seq START WITH 1 INCREMENT BY 1

CREATE OR REPLACE TRIGGER repositories_seq_tr
 BEFORE INSERT ON repositories FOR EACH ROW
 WHEN (NEW.id IS NULL)
BEGIN
 SELECT repositories_seq.NEXTVAL INTO :NEW.id FROM DUAL;
END;
