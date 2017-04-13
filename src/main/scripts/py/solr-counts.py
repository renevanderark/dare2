#!/usr/bin/python2.7
from __future__ import print_function

import urllib2
import ast

req = urllib2.Request('http://localhost:8983/solr/gettingstarted/select?wt=python&q=*:*&rows=0&facet=on&facet.field=source_s')
data = ast.literal_eval(urllib2.urlopen(req).read())
sources = data['facet_counts']['facet_fields']['source_s']

accessRights_d = {
    'open': '&fq=accessRights_ss:%22http://purl.org/eprint/accessRights/OpenAccess%22',
    'closed': '&fq=accessRights_ss:%22http://purl.org/eprint/accessRights/ClosedAccess%22',
    'restricted': '&fq=accessRights_ss:%22http://purl.org/eprint/accessRights/RestrictedAccess%22',
}
accessRights_d['open+closed'] = accessRights_d['open'] + accessRights_d['closed']
accessRights_d['open+restricted'] = accessRights_d['open'] + accessRights_d['restricted']
accessRights_d['closed+restricted'] = accessRights_d['closed'] + accessRights_d['restricted']
accessRights_d['open+closed+restricted'] = accessRights_d['open'] + accessRights_d['closed'] + accessRights_d['restricted']

accessRights_k = [
    'open',
    'closed',
    'restricted',
    'open+closed',
    'open+restricted',
    'closed+restricted',
    'open+closed+restricted'
]

print('source;genre;total;no objects;has objects;' + ';'.join(accessRights_k) + ';genre_uri')
for i in range(0, len(sources), 2):
    req = urllib2.Request('http://localhost:8983/solr/gettingstarted/select?wt=python&q=*:*' +
                          '&fq=source_s:%22' + urllib2.quote(sources[i]) + '%22' +
                          '&rows=0&facet=on&facet.field=genre_ss')
    genres = ast.literal_eval(urllib2.urlopen(req).read())['facet_counts']['facet_fields']['genre_ss']
    for j in range(0, len(genres), 2):
        if genres[j+1] > 0:
            counts_per_genre_per_source = [
                sources[i],
                genres[j].replace('info:eu-repo/semantics/', '').replace('http://purl.org/eprint/type/', ''),
                str(genres[j+1])
            ]

            req = urllib2.Request('http://localhost:8983/solr/gettingstarted/select?rows=0&wt=python&q=*:*' +
                                  '&fq=source_s:%22' + urllib2.quote(sources[i]) + '%22' +
                                  '&fq=genre_ss:%22' + genres[j] + '%22' +
                                  '&fq=objectCount_i:0')
            counts_per_genre_per_source.append(str(ast.literal_eval(urllib2.urlopen(req).read())['response']['numFound']))

            req = urllib2.Request('http://localhost:8983/solr/gettingstarted/select?rows=0&wt=python&q=*:*' +
                                  '&fq=source_s:%22' + urllib2.quote(sources[i]) + '%22' +
                                  '&fq=genre_ss:%22' + genres[j] + '%22' +
                                  '&fq=objectCount_i:[1%20TO%20*]')
            counts_per_genre_per_source.append(str(ast.literal_eval(urllib2.urlopen(req).read())['response']['numFound']))

            for accessRights in accessRights_k:
                req = urllib2.Request('http://localhost:8983/solr/gettingstarted/select?rows=0&wt=python&q=*:*' +
                                      '&fq=objectCount_i:[1%20TO%20*]' +
                                      '&fq=source_s:%22' + urllib2.quote(sources[i]) + '%22' +
                                      '&fq=genre_ss:%22' + genres[j] + '%22' +
                                      accessRights_d[accessRights])

                counts_per_genre_per_source.append(str(ast.literal_eval(urllib2.urlopen(req).read())['response']['numFound']))
            counts_per_genre_per_source.append(genres[j])
            print (';'.join(counts_per_genre_per_source))
