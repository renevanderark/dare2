LOCK TABLES `repositories` WRITE;
/*!40000 ALTER TABLE `repositories`
  DISABLE KEYS */;
INSERT INTO `repositories`
VALUES (1, 'http://oai.gharvester.dans.knaw.nl/', 'nl_didl_norm', 'uu:dare', NULL),
  (2, 'http://oai.gharvester.dans.knaw.nl/', 'nl_didl_norm', 'ru:col_2066_13799', NULL),
  (3, 'http://oai.gharvester.dans.knaw.nl/', 'nl_didl_norm', 'rug', NULL),
  (4, 'http://oai.gharvester.dans.knaw.nl/', 'nl_didl_norm', 'tud:A-set', NULL),
  (5, 'http://oai.gharvester.dans.knaw.nl/', 'nl_didl_norm', 'ul:hdl_1887_4539', NULL),
  (6, 'http://oai.gharvester.dans.knaw.nl/', 'nl_didl_norm', 'um', NULL),
  (7, 'http://oai.gharvester.dans.knaw.nl/', 'nl_didl_norm', 'ut:66756C6C746578743D7075626C6963', NULL),
  (8, 'http://oai.gharvester.dans.knaw.nl/', 'nl_didl_norm', 'uva:withfulltext:yes', NULL),
  (9, 'http://oai.gharvester.dans.knaw.nl/', 'nl_didl_norm', 'uvt:withfulltext:yes', NULL),
  (10, 'http://oai.gharvester.dans.knaw.nl/', 'nl_didl_norm', 'vu', NULL),
  (11, 'http://oai.gharvester.dans.knaw.nl/', 'nl_didl_norm', 'wur:publickb', NULL);
/*!40000 ALTER TABLE `repositories` ENABLE KEYS */;
UNLOCK TABLES;

