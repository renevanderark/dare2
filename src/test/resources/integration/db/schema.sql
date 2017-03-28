-- noinspection SqlDialectInspectionForFile

DROP TABLE IF EXISTS `harvester_errors`;
CREATE TABLE `harvester_errors` (
  `repository_id` int(11) DEFAULT NULL,
  `url` varchar(255) DEFAULT NULL,
  `message` varchar(255) DEFAULT NULL,
  `stacktrace` text,
  `datestamp` varchar(50) DEFAULT NULL,
  `status_code` int(11) DEFAULT NULL,
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
DROP TABLE IF EXISTS `oai_record_errors`;
CREATE TABLE `oai_record_errors` (
  `record_identifier` varchar(128) DEFAULT NULL,
  `datestamp` varchar(50) DEFAULT NULL,
  `message` varchar(1024) DEFAULT NULL,
  `url` varchar(1024) DEFAULT NULL,
  `stacktrace` text,
  `status_code` int(11) DEFAULT NULL,
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
DROP TABLE IF EXISTS `oai_records`;
CREATE TABLE `oai_records` (
  `identifier` varchar(128) NOT NULL,
  `datestamp` varchar(50) DEFAULT NULL,
  `repository_id` int(11) DEFAULT NULL,
  `oai_status_code` int(11) DEFAULT NULL,
  `process_status_code` int(11) DEFAULT NULL,
  `update_count` int(11) NOT NULL DEFAULT 0,
  `total_file_size` bigint(20) NOT NULL DEFAULT 0,
  PRIMARY KEY (`identifier`),
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
DROP TABLE IF EXISTS `repositories`;
CREATE TABLE `repositories` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) DEFAULT NULL,
  `url` varchar(255) DEFAULT NULL,
  `metadataPrefix` varchar(255) DEFAULT NULL,
  `oai_set` varchar(50) DEFAULT NULL,
  `datestamp` varchar(50) DEFAULT NULL,
  `enabled` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;