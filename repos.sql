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
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `repositories`
--

LOCK TABLES `repositories` WRITE;
/*!40000 ALTER TABLE `repositories` DISABLE KEYS */;
INSERT INTO `repositories` VALUES
  (1,'Utrecht','http://oai.gharvester.dans.knaw.nl/','nl_didl_norm','uu:dare',NULL,1),
  (2,'Nijmegen','http://oai.gharvester.dans.knaw.nl/','nl_didl_norm','ru:col_2066_13799',NULL,1),
  (3,'Groningen','http://oai.gharvester.dans.knaw.nl/','nl_didl_norm','rug',NULL,0),
  (4,'Delft','http://oai.gharvester.dans.knaw.nl/','nl_didl_norm','tud:A-set',NULL,0),
  (5,'Leiden','http://oai.gharvester.dans.knaw.nl/','nl_didl_norm','ul:hdl_1887_4539',NULL,1),
  (6,'Maastricht','http://oai.gharvester.dans.knaw.nl/','nl_didl_norm','um',NULL,0),
  (7,'Twente','http://oai.gharvester.dans.knaw.nl/','nl_didl_norm','ut:66756C6C746578743D7075626C6963',NULL,1),
  (8,'UvA','http://oai.gharvester.dans.knaw.nl/','nl_didl_norm','uva:withfulltext:yes',NULL,0),
  (9,'Tilburg','http://oai.gharvester.dans.knaw.nl/','nl_didl_norm','uvt:withfulltext:yes',NULL,0),
  (10,'VU','http://oai.gharvester.dans.knaw.nl/','nl_didl_norm','vu',NULL,1),
  (11,'Wageningen','http://oai.gharvester.dans.knaw.nl/','nl_didl_norm','wur:publickb',NULL,1);
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

-- Dump completed on 2017-02-27  9:01:53
