#!/usr/bin/python2.7
from __future__ import print_function

import urllib2
import ast


per_page = 10000
req = urllib2.Request('http://localhost:8983/solr/gettingstarted/select?wt=python&q=*:*&rows=0')
response = urllib2.urlopen(req)
data = ast.literal_eval(response.read())


num_found = data['response']['numFound']

for start in range(0, num_found, per_page):
    req = urllib2.Request('http://localhost:8983/solr/gettingstarted/select?csv.separator=;&q=*:*&rows=' + str(per_page) +
                    '&wt=csv&csv.mv.separator=|&csv.header=' + ('true' if start == 0 else 'false') +
                    '&start=' + str(start))
    resp = urllib2.urlopen(req)
    print(resp.read(), end='')