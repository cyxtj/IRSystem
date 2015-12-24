#coding=utf8

# This script dig out information from the url of each page
# such as url depth, url top level domain (usually .edu is better than .org)
import numpy as np

url_filename = r'i:\kuaipan\graduateCourses\IR\program\data\WT10G\info\docid_to_url\docid_to_url'

f = open(url_filename)
docnos = []
depths = []
domains = []
for line in f:
    [docno, url] = line.split(' ')
    url = url[7:] # http:// is useless

    url_depth = url.count('/')
    if '/' not in url:
        first_slash = len(url) -1
    else:
        first_slash = url.index('/')
    start = url.rindex('.', 1, first_slash)
    if ':' in url:
        url_top_domain = url[start+1: url.index(':')]
    else:
        url_top_domain = url[start+1: first_slash]
    docnos.append(docno)
    depths.append(url_depth)
    domains.append(url_top_domain)


## write the file in order defined in docNoId_filename
'''
docNoId_dic = {}
docNoId_filename = r'i:\kuaipan\graduateCourses\IR\program\Search\WT10G\docNoByDocId'
f = open(docNoId_filename)
for i, line in enumerate(f):
    docNo = line[:-1]
    docNoId_dic[docNo] = i

f.close()

depths_byid = np.ones(len(docNoId_dic))
depths_byid.fill(-1)
for i in xrange(len(depths)):
    if docNoId_dic.has_key(docnos[i]):
        docid = docNoId_dic[docnos[i]]
        depths_byid[docid] = depths[i]

fwname = r'i:\kuaipan\graduateCourses\IR\program\staticScores\urldepth_byid.txt'
fw = open(fwname, 'w')
for i in xrange(len(depths_byid)):
    fw.write('%i\n'%(depths_byid[i]))

fw.close()
'''

import numpy as np
depths = np.array(depths)
domains = np.array(domains) # some domain are numbers because it is IP adresses, and some maybe url is wrong.
fwname = r'i:\kuaipan\graduateCourses\IR\program\staticScores\urldepth.txt'
fw = open(fwname, 'w')
for i in xrange(depths.shape[0]):
    fw.write('%s, %i\n'%(docnos[i], depths[i]))

fw.close()
