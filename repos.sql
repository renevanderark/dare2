-- MySQL dump 10.13  Distrib 5.7.17, for Linux (x86_64)
--
-- Host: localhost    Database: dare
-- ------------------------------------------------------
-- Server version	5.7.17-0ubuntu0.16.10.1

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `repositories`
--

DROP TABLE IF EXISTS `repositories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `repositories` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `url` varchar(255) DEFAULT NULL,
  `metadataPrefix` varchar(255) DEFAULT NULL,
  `oai_set` varchar(50) DEFAULT NULL,
  `datestamp` varchar(50) DEFAULT NULL,
  `enabled` tinyint(4) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `repositories`
--

LOCK TABLES `repositories` WRITE;
/*!40000 ALTER TABLE `repositories` DISABLE KEYS */;
INSERT INTO `repositories` VALUES
  (1,'Utrecht','http://oai.gharvester.dans.knaw.nl/','nl_didl_norm','uu:dare','2017-04-05T00:26:49Z',0),
  (2,'Nijmegen','http://oai.gharvester.dans.knaw.nl/','nl_didl_norm','ru:col_2066_13799','2017-04-05T00:30:56Z',0),
  (3,'Groningen','http://oai.gharvester.dans.knaw.nl/','nl_didl_norm','rug','2017-04-05T00:26:22Z',0),
  (4,'Delft','http://oai.gharvester.dans.knaw.nl/','nl_didl_norm','tud:A-set','2017-01-04T19:25:00Z',0),
  (5,'Leiden','http://oai.gharvester.dans.knaw.nl/','nl_didl_norm','ul:hdl_1887_4539','2017-04-04T14:33:15Z',0),
  (6,'Maastricht','http://oai.gharvester.dans.knaw.nl/','nl_didl_norm','um','2017-04-05T00:26:42Z',0),
  (7,'Twente','http://oai.gharvester.dans.knaw.nl/','nl_didl_norm','ut:66756C6C746578743D7075626C6963','2017-04-05T00:21:30Z',0),
  (8,'UvA','http://oai.gharvester.dans.knaw.nl/','nl_didl_norm','uva:withfulltext:yes','2016-12-09T11:27:01Z',0),
  (9,'Tilburg','http://oai.gharvester.dans.knaw.nl/','nl_didl_norm','uvt:withfulltext:yes','2015-05-26T16:25:58Z',0),
  (10,'VU','http://oai.gharvester.dans.knaw.nl/','nl_didl_norm','vu','2017-04-05T00:26:50Z',0),
  (11,'Wageningen','http://oai.gharvester.dans.knaw.nl/','nl_didl_norm','wur:publickb','2017-04-05T00:30:52Z',0),
  (12,'Eindhoven','http://oai.gharvester.dans.knaw.nl/','nl_didl_norm','tue:KB','2017-04-04T16:54:21Z',0),
  (13,'Beeld en Geluid','http://oai.gharvester.dans.knaw.nl/','nl_didl_norm','beng','2017-04-10T11:05:43Z',1),
  (14,'CWI','http://oai.gharvester.dans.knaw.nl/','nl_didl_norm','nwo:nwo','2016-12-20T23:05:28Z',1),
  (15,'Rotterdam','http://oai.gharvester.dans.knaw.nl/','nl_didl_norm','eur','2017-04-05T00:28:24Z',1),
  (16,'HBO Lectoraat','http://oai.gharvester.dans.knaw.nl/','nl_didl_norm','hbo:lectormember','2017-04-04T13:10:09Z',1),
  (17,'KNAW','http://oai.gharvester.dans.knaw.nl/','nl_didl_norm','knaw','2017-04-10T12:06:29Z',1);
/*!40000 ALTER TABLE `repositories` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2017-04-10 14:11:16
