import numpy as np
import scipy.sparse as sp

'''
# out
fname_out_link = r'i:\kuaipan\graduateCourses\IR\program\data\WT10G\WT10G\info\out_links\out_links' 

f_out_link = open(fname_out_link)
links = []
for l in f_out_link:
    docids = l.split()
    doc_from = docids[0]
    for doc_to in docids[1:]:
        links.append([doc_from, doc_to])

links = np.array(links)

# in
fname_in_link = r'i:\kuaipan\graduateCourses\IR\program\data\WT10G\WT10G\info\in_links\in_links' 
f_in_link = open(fname_in_link)

links2 = []
for l in f_in_link:
    docids = l.split()
    doc_to = docids[0]
    for doc_from in docids[1:]:
        links2.append([doc_from, doc_to])
links2 = np.array(links2)

links = np.load('links.npf.npy')
links2 = np.load('links2.npf.npy')

ls1 = links[links[:, 1].argsort(), :]
ls1s0 = ls1[ls1[:, 0].argsort(), :]

l2s1 = links2[links2[:, 1].argsort(), :]
l2s1s0 = l2s1[l2s1[:, 0].argsort(), :]
np.save('links', ls1s0)
np.save('links2', l2s1s0)

# suppose e1 < e2 at first
L1 = []
L2 = []
j = 0
for i in range(ls1s0.shape[0]):
    e1 = ls1s0[i, :]
    e2 = l2s1s0[j, :]
    if (e1==e2).all():
        j += 1
        continue
    elif e1[0] > e2[0] or (e1[0]==e2[0] and e1[1]>e2[1]):
        L2.append(e2)
        j += 1
    elif e1[0] < e2[0] or (e1[0]==e2[0] and e1[1]<e2[1]):
        L1.append(e1)

'''
links = np.load('links.npy')
links2 = np.load('links2.npy')
doc1, cnt1 = np.unique(links, return_counts=True)
doc2, cnt2 = np.unique(links2, return_counts=True)
count1 = np.vstack([doc1, cnt1]).T
count2 = np.vstack([doc2, cnt2]).T
np.save('count1', count1)
np.save('count1', np.vstack([doc1, cnt1]).T)
