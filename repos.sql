LOCK TABLES `repositories` WRITE;
/*!40000 ALTER TABLE `repositories`
  DISABLE KEYS */;
INSERT INTO `repositories`
VALUES (1, 'Utrecht', 'http://oai.gharvester.dans.knaw.nl/', 'nl_didl_norm', 'uu:dare', '2017-02-01T00:00:00Z', 0),
  (2, 'Nijmegen', 'http://oai.gharvester.dans.knaw.nl/', 'nl_didl_norm', 'ru:col_2066_13799', '2017-02-01T00:00:00Z', 0),
  (3, 'Groningen', 'http://oai.gharvester.dans.knaw.nl/', 'nl_didl_norm', 'rug', '2017-02-01T00:00:00Z', 0),
  (4, 'Delft', 'http://oai.gharvester.dans.knaw.nl/', 'nl_didl_norm', 'tud:A-set', '2017-02-01T00:00:00Z', 0),
  (5, 'Leiden', 'http://oai.gharvester.dans.knaw.nl/', 'nl_didl_norm', 'ul:hdl_1887_4539', '2017-02-01T00:00:00Z', 0),
  (6, 'Maastricht', 'http://oai.gharvester.dans.knaw.nl/', 'nl_didl_norm', 'um', '2017-02-01T00:00:00Z', 0),
  (7, 'Twente', 'http://oai.gharvester.dans.knaw.nl/', 'nl_didl_norm', 'ut:66756C6C746578743D7075626C6963', '2017-02-01T00:00:00Z', 0),
  (8, 'UvA', 'http://oai.gharvester.dans.knaw.nl/', 'nl_didl_norm', 'uva:withfulltext:yes', '2017-02-01T00:00:00Z', 0),
  (9, 'Tilburg', 'http://oai.gharvester.dans.knaw.nl/', 'nl_didl_norm', 'uvt:withfulltext:yes', '2017-02-01T00:00:00Z', 0),
  (10, 'VU', 'http://oai.gharvester.dans.knaw.nl/', 'nl_didl_norm', 'vu', '2017-02-01T00:00:00Z', 0),
  (11, 'Wageningen', 'http://oai.gharvester.dans.knaw.nl/', 'nl_didl_norm', 'wur:publickb', '2017-02-01T00:00:00Z', 0);
/*!40000 ALTER TABLE `repositories` ENABLE KEYS */;
UNLOCK TABLES;

