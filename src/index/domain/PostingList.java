package index.domain;

import java.util.ArrayList;

public class PostingList {
	public byte[] c_pl;
	public ArrayList<PostingItem> list;
	
	public PostingList(){
		list = new ArrayList<PostingItem>();
	}
	
	public boolean addPostingItem(PostingItem pi){
		list.add(pi);
		return true;
	}
	
	public PostingItem lastItem(){
		return list.get(list.size()-1);
	}
	
	public PostingItem getItem(int index){
		return list.get(index);
	}
	
	public boolean removeItem(int index){
		list.remove(index);
		return true;
	}
	
	public int getSize(){
		return list.size();
	}
	
	public int getDF(){
		return list.size();
	}
	
	public String toString(){
		String tmp = "";
		for(PostingItem pi : list)
			tmp += pi.docid + " " + pi.tf + " ";
		return tmp.substring(0, tmp.length()-1);
	}
	
	/**
	 * pl2׷�ӵ������ż�¼��
	 **/
	public boolean append(PostingList pl2){
		int i = 0;
		if(lastItem().docid == pl2.getItem(0).docid){
			lastItem().tf += pl2.getItem(0).tf;
			i++;
		}
		for(; i<pl2.getSize(); i++)
			list.add(pl2.getItem(i));
		return true;
	}
	
	/**
	 * ��ĳ�����ż�¼�����gamma����
	 **/
	public static ArrayList<Byte> encode(PostingList list){
		ArrayList<Byte> bytelist = new ArrayList<Byte>();
		ArrayList<Boolean> booleanlist = new ArrayList<Boolean>();
		ArrayList<Boolean> tmp = gammaCode(list.getSize()); //df����1
		for(Boolean b : tmp) booleanlist.add(b);
		int preDocid = 0;
		for(int i=0; i<list.getSize(); i++){
			PostingItem item = list.getItem(i);
			if(i==0){
				tmp = gammaCode(item.docid + 1); //�ĵ�id����1
			}else{
				tmp = gammaCode(item.docid - preDocid); //�ĵ���಻��1
			}
			for(Boolean b : tmp) booleanlist.add(b);
			tmp = gammaCode(item.tf); //tf����1
			for(Boolean b : tmp) booleanlist.add(b);
			preDocid = item.docid;
		}
		while(booleanlist.size()%8 != 0) booleanlist.add(false);
		Byte bt = 0;
		for(int i=0; i<booleanlist.size(); i++){
			bt = (byte) ((bt<<1) + (booleanlist.get(i)?1:0));
			if((i+1)%8==0){
				bytelist.add(bt);
				bt = 0;
			}
		}
		return bytelist;
	}
	
	/**
	 * ��bytelist���������Ϊdf��һ�����ż�¼��
	 */
	public static PostingList decode(byte[] bytelist, MyInteger index){
		PostingList list = new PostingList();
		ArrayList<Integer> intlist = new ArrayList<Integer>();
		int k = 7;
		int df = 1;
		for(int i=0; i<2*df+1; i++){
			if(index.value == bytelist.length) return null;
			//����Gamma����ĳ���
			int cnt = 0;
			while((bytelist[index.value]&(1<<k)) != 0){
				cnt++;
				k--;
				if(k==-1) {
					index.value++;
					if(index.value==bytelist.length)return null;
					k = 7;
				}
			}
			//����Gamma�����ƫ��
			int tmp = 1;
			for(int z=-1; z<cnt; z++){
				if(z>=0) tmp = (tmp<<1) + ((bytelist[index.value]&(1<<k))==0?0:1);
				k--;
				if(k==-1) {
					index.value++;
					if(index.value==bytelist.length && z<cnt-1) return null; //�Ѿ�û��bitλ�����һ���Ҫ
					k = 7;
				}
			}
			if(i == 0){df = tmp; continue;}
			intlist.add(tmp);
		}
		int docid = 0;
		for(int i=0; i<df; i++){
			if(i==0){
				docid = intlist.get(0) - 1;
				list.addPostingItem(new PostingItem(docid, intlist.get(1)));
			}else{
				docid += intlist.get(i<<1);
				list.addPostingItem(new PostingItem(docid, intlist.get((i<<1)+1)));
			}
		}
		if(k != 7) index.value++;
		list.c_pl = bytelist;
		return list;
	}
	
	/**
	 * gamma����
	 **/
	public static ArrayList<Boolean> gammaCode(int x){
		ArrayList<Boolean> booleanlist = new ArrayList<Boolean>();
		while(x!=0){
			booleanlist.add(0, (x&1)==1);
			x >>= 1;
		}
		booleanlist.remove(0);
		booleanlist.add(0, false);
		for(int i=booleanlist.size()-1; i>=1 ;i--) booleanlist.add(0, true);
		return booleanlist;
	}
}
