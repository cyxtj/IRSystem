
import pandas as pd
import numpy as np

indexFolder = r'../data/index/content/'
docNoId_filename = indexFolder + 'docNoByDocId'

print 'load docNoId file'
doc_noid_df = pd.read_csv(docNoId_filename, names=['docNo'], header=None)
doc_id = np.arange(doc_noid_df.shape[0])
doc_noid_df['docId'] = doc_id

print 'loading pagerank file'
pr_filename = r'pagerank_in.txt'
pagerank = pd.read_csv(pr_filename, names=['docNo', 'pr'], header=None)

print 'loading urldepth file'
urldepth_filename = r'urldepth.txt'
urldepth = pd.read_csv(urldepth_filename, names=['docNo', 'depth'], header=None)

print 'merging'
new_pagerank = pd.merge(doc_noid_df, pagerank, how='left', on='docNo')
static_scores = pd.merge(new_pagerank, urldepth, how='left', on='docNo')

static_scores.sort_values(by=['docId'], kind='mergsort', inplace=True)
static_scores['pr'].fillna(static_scores['pr'].min(), inplace=True)

print 'writing to file'
static_scores['pr'].to_csv(indexFolder + '/pagerank_in_byid.txt', index=False)
static_scores['depth'].to_csv(indexFolder + '/urldepth_byid.txt', index=False)
