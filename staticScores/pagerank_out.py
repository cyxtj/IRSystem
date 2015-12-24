# pagerank on in_link and out_link
# each html file is a page

# see slides from Shen Huawei
# The update formula is x:= c * x*S + (1-c) * e/n
# Here we set S = G + D/n, g is original transition matrix, d is dangling matrix. d[i, :]=1, where node i has no out link
# the update formula become x:= c * x*(G+D/n) + (1-c) * e/n
# and become x:= c * (x*G + x*D/n) + (1-c) * e/n
# since each row of d is the same, if set d=D[:, 0], k=x*d/n, then x*D/n = [k, k, ..., k]

import numpy as np
import scipy.sparse as sp

fname_out_link = r'i:\kuaipan\graduateCourses\IR\program\data\WT10G\WT10G\info\out_links\out_links' 

# first, record all unique docids
# and give them a unique number which is the index in a matrix
f_out_link = open(fname_out_link)
all_docid = []
for l in f_out_link:
    docids = l.split()
    all_docid.extend(docids)

all_docid = np.array(all_docid)
unique_docid = np.unique(all_docid)
docid2matid = {docid:matid for matid, docid in enumerate(unique_docid)}


# second compute the transition matrix
f_out_link = open(fname_out_link)
links = []
# vi = 1/di where di is the degree of node i
v = []
for i, l in enumerate(f_out_link):
    docids = l.split()
    doc_out = docids[0]
    doc_out_mid = docid2matid[doc_out]
    prob = 1./(len(docids)-1)
    for doc_in in docids[1:]:
        doc_in_mid = docid2matid[doc_in]
        links.append([doc_out_mid, doc_in_mid])
        v.append(prob)

links = np.array(links)



n = unique_docid.shape[0]
m = links.shape[0]
no_outlink = np.ones(n)
not_dangling_nodes = np.unique(links[:, 0])
no_outlink[not_dangling_nodes] = 0
d = no_outlink.reshape([-1, 1])

G = sp.coo_matrix((v, (links[:, 0], links[:, 1])), shape=(n, n)).tocsr()

# test data from slide
# unique_docid = np.array(range(5))
# links = np.array([[1, 2], [2, 1], [3, 2], [3, 4], [5, 3], [5, 4]])-1
# v = [1, 1, .5, .5, .5, .5]
# n = unique_docid.shape[0]
# m = links.shape[0]
# no_outlink = np.ones(n)
# not_dangling_nodes = np.unique(links[:, 0])
# no_outlink[not_dangling_nodes] = 0
# d = no_outlink.reshape([-1, 1])
# 
# G = sp.coo_matrix((v, (links[:, 0], links[:, 1])), shape=(n, n)).tocsr()

pr = np.ones([1, n])
e = np.ones([1, n])
pr.fill(1./n)

c = 0.85
for i in range(200):
    print i, 
    xd = pr.dot(d)[0, 0] / n
    xD = np.ones(n)
    xD.fill(xd)
    # pr_new = c * (pr.dot(G) + xD) + (1-c)/n * e
    pr_new = c * (pr * G + xD) + (1-c)/n * e
    err = np.abs(pr_new-pr).sum()
    print err
    if err < 1e-7:
        break
    pr = pr_new

# write result to file
fw = r'i:\kuaipan\graduateCourses\IR\program\result.txt'
f = open(fw, 'w')
prn = pr * n
for i in range(n):
    f.write('%s, %.3f\n'%(unique_docid[i], prn[0, i]))

f.close()
