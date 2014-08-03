import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.TreeSet;


public class BPlusTree{
	
	private final int blockSize = 1024;
	private Block metadata1;
	private Block metadata2;
	private BlockFile file1;
	private BlockFile file2;
	private int keySize = 4;
	private int valueSize = 60;
	private final int headerSize = 8;
	private int size = 0;
	
	/**
	 * Construct a new empty BPlusTree that saves to "BPlusTree.txt"
	 */
	public BPlusTree(){
		try {
			new File("BPlusTree.txt").delete();
			file1 = new BlockFile("BPlusTree.txt",blockSize);
			new File("BPlusTree2.txt").delete();
			file2 = new BlockFile("BPlusTree2.txt",blockSize);
		} catch (IOException e){System.out.println("File Error: "+e);}
		metadata1 = new Block(blockSize);
		metadata1.setInt(blockSize,0);	//block size
		metadata1.setInt(4,4);		//key size
		metadata1.setInt(60,8);		//value size
		metadata1.setInt(0,12);		//root index
		metadata1.setInt(64,16);	//max keys in root nodes
		metadata1.setInt(15,20);		//max keys in leaf nodes
		try {
			file1.write(metadata1);
			file2.write(metadata1);
			metadata2 = file2.read(0);
		} catch (IOException | InvalidBlockFileException e){System.out.println("File Error: "+e);}
	}
	/**
	 * Construct a BPlusTree from a list of host files that maps from IP to Domain 
	 * and the reverse
	 * 
	 * @param filename
	 */
	public BPlusTree(String filename){
		try {
			new File("BPlusTree.txt").delete();
			file1 = new BlockFile("BPlusTree.txt",blockSize);
			new File("BPlusTree2.txt").delete();
			file2 = new BlockFile("BPlusTree2.txt",blockSize);
		} catch (IOException e){System.out.println("File Error: "+e);}
		metadata1 = new Block(blockSize);
		metadata1.setInt(blockSize,0);	//block size
		metadata1.setInt(4,4);		//key size
		metadata1.setInt(60,8);		//value size
		metadata1.setInt(0,12);		//root index
		metadata1.setInt(64,16);	//max keys in root nodes
		metadata1.setInt(15,20);		//max keys in leaf nodes
		try {
			file1.write(metadata1);
			file2.write(metadata1);
			metadata2 = file2.read(0);
		} catch (IOException | InvalidBlockFileException e){System.out.println("File Error: "+e);}
		
		try {
			Scanner sc = new Scanner(new File(filename));
			while(sc.hasNextLine()){
				//System.out.println("Size: "+size);
				String line = sc.nextLine();
				String domain = line.split("	")[0];
				line = line.split("	")[1];
				int IP = ipToInt(line);
				add(Bytes.intToBytes(IP),domain.getBytes());
			}
		} catch (FileNotFoundException e){System.out.println("File Error: "+e);}
		//printContents2();
	}
	
	/**
	 * add a new key value pair to the tree expects size 4 bytes and 60 bytes respectively
	 * 
	 * @param key
	 * @param value
	 */
	public void add(byte key[],byte value[]){
		//add to tree 1
		try{
			//root is null, so create root
			if(metadata1.getInt(12) == 0){
				BPlusTreeNode1 root = new BPlusTreeNode1(file1,true,0);
				root.add(key, value);
				root.save();
				metadata1.setInt(1,12);
				file1.write(metadata1,0);
			}
			else{
				//add value to tree
				BPlusTreeNode1 rightChild = (BPlusTreeNode1) add(key,value,new BPlusTreeNode1(file1,metadata1.getInt(12)));
				if(rightChild != null){
					//System.out.println("RCC: "+Bytes.bytesToInt(rightChild.keyAt(0)));
					BPlusTreeNode1 root = new BPlusTreeNode1(file1,false,0);
					root.add(Bytes.intToBytes(Integer.MIN_VALUE),Bytes.intToBytes(metadata1.getInt(12)));
					root.add(rightChild.keyAt(0),Bytes.intToBytes(rightChild.getAddress()));
					//System.out.println("RAC: "+Bytes.bytesToInt(root.keyAt(1)));
					metadata1.setInt(root.getAddress(),12);
					file1.write(metadata1,0);
				}
			}
		} catch (IOException | InvalidBlockFileException e) {e.printStackTrace();}
		//add to tree 2
		try{
			//root is null, so create root
			if(metadata2.getInt(12) == 0){
				BPlusTreeNode2 root = new BPlusTreeNode2(file2,true,0);
				root.add(key, value);
				root.save();
				metadata2.setInt(1,12);
				file2.write(metadata2,0);
			}
			else{
				//add value to tree
				BPlusTreeNode2 rightChild = (BPlusTreeNode2) add(key,value,new BPlusTreeNode2(file2,metadata2.getInt(12)));
				if(rightChild != null){
					//System.out.println("RCC: "+Bytes.bytesToInt(rightChild.keyAt(0)));
					BPlusTreeNode2 root = new BPlusTreeNode2(file2,false,0);
					//System.out.println("RT ADDRESS: "+rightChild.getAddress());
					root.add(new byte[60],Bytes.intToBytes(metadata2.getInt(12)));
					//System.out.println("RV: "+new String(rightChild.valueAt(0)));
					root.add(rightChild.isLeaf()?rightChild.valueAt(0):rightChild.keyAt(0),Bytes.intToBytes(rightChild.getAddress()));
					//System.out.println("ROOT: "+(new BPlusTreeNode2(file2,Bytes.bytesToInt(root.keyAt(0))).isLeaf())+
							//(new BPlusTreeNode2(file2,Bytes.bytesToInt(root.keyAt(1))).isLeaf()));
					//System.out.println("KK: "+Bytes.bytesToInt(root.keyAt(0))+" "+Bytes.bytesToInt(root.keyAt(1))+" "+root.getAddress());
					//System.out.println("RAC: "+Bytes.bytesToInt(root.keyAt(1)));
					metadata2.setInt(root.getAddress(),12);
					file2.write(metadata2,0);
					root = new BPlusTreeNode2(file2,metadata2.getInt(12));
					//System.out.println("CK: "+Bytes.bytesToInt(root.keyAt(0))+" "+Bytes.bytesToInt(root.keyAt(1)));
				}
			}
		} catch (IOException | InvalidBlockFileException e) {e.printStackTrace();}
		size++;
	}
	
	public BPlusTreeNode add(byte key[],byte value[],BPlusTreeNode node) throws IOException, InvalidBlockFileException{
		//if node is a leaf
		if(node.isLeaf()){
			//if node is not full
			if(node.hasRoom()){
				node.add(key, value);
				node.save();
				return null;
			}
			else return splitLeaf(key,value,node);
		}
		else{
			for(int i=0;i<node.keyCount();i++){
				//System.out.println("root "+node.getAddress()+" "+i+" "+Bytes.bytesToInt(key)+" "+Bytes.bytesToInt(node.keyAt(i)));
				if(node instanceof BPlusTreeNode1){
					if(i == node.keyCount()-1 || Bytes.bytesToInt(key)<Bytes.bytesToInt(node.keyAt(i+1))){
						BPlusTreeNode rc = null;
						if(node instanceof BPlusTreeNode1) rc =  add(key,value,new BPlusTreeNode1(file1,Bytes.bytesToInt(node.valueAt(i))));
						else rc =  add(key,value,new BPlusTreeNode2(file2,Bytes.bytesToInt(node.keyAt(i))));
						if(rc == null) return null;
						else return dealWithPromote(rc,node);
					}
				}
				else{
					//if(i<node.keyCount()-1)System.out.println("T6: "+new String(value)+ " " +new String(node.valueAt(i+1)));
					if(i == node.keyCount()-1 || new String(value).compareTo(new String(node.valueAt(i+1)))<0){
						BPlusTreeNode rc = null;
						if(node instanceof BPlusTreeNode1) rc =  add(key,value,new BPlusTreeNode1(file1,Bytes.bytesToInt(node.valueAt(i))));
						else rc =  add(key,value,new BPlusTreeNode2(file2,Bytes.bytesToInt(node.keyAt(i))));
						if(rc == null) return null;
						else return dealWithPromote(rc,node);
					}
				}
			}
			return null;
		}
	}
	
	public BPlusTreeNode splitLeaf(byte[] key,byte[] value,BPlusTreeNode node) throws IOException{
		BPlusTreeNode right =  node.splitLeaf(key,value);
		return right;
	}
	public BPlusTreeNode dealWithPromote(BPlusTreeNode rc,BPlusTreeNode node){
		if(rc == null) return null;
		if(node.hasRoom()) {
			if(node instanceof BPlusTreeNode1) node.add(rc.keyAt(0),Bytes.intToBytes(rc.getAddress()));
			else {
				//System.out.println(rc.getAddress()+" "+new String(rc.valueAt(0))+" & "+node.keyCount()+" "+size);
				node.add(rc.valueAt(0),Bytes.intToBytes(rc.getAddress()));
			}
			//System.out.println("H: "+Bytes.bytesToInt(node.keyAt(0))+" "+Bytes.bytesToInt(node.keyAt(1))+" "+Bytes.bytesToInt(node.keyAt(2)));
			return null;
		}
		if(node instanceof BPlusTreeNode1) return node.dealWithPromote(rc.keyAt(0),Bytes.intToBytes(rc.getAddress()));
		else return node.dealWithPromote(rc.valueAt(0),Bytes.intToBytes(rc.getAddress()));
	}
	public boolean isEmpty(){
		return metadata1.getByte(12) == 0;
	}
	public String find(int key){
		if(metadata1.getInt(0) == 0) return null;
		return find(Bytes.intToBytes(key),new BPlusTreeNode1(file1,metadata1.getInt(12)));
	}
	public String find(String key){
		if(metadata2.getInt(0) == 0) return null;
		return reverseFind(key.getBytes(),new BPlusTreeNode2(file2,metadata2.getInt(12)));
	}
	/**
	 * finds the domain from an int representing an IP address
	 * @param key
	 * @param node
	 * @return
	 */
	private String find(byte key[],BPlusTreeNode1 node){
		if(node.isLeaf()){
			for(int i=0;i<node.keyCount();i++){
				if(Bytes.bytesToInt(node.keyAt(i)) == Bytes.bytesToInt(key)) return new String(node.valueAt(i));
			}
			return null;
		}
		else{
			for(int i=1;i<node.keyCount();i++){
				if(Bytes.bytesToInt(key) < Bytes.bytesToInt(node.keyAt(i))) return find(key,new BPlusTreeNode1(file1,Bytes.bytesToInt(node.valueAt(i-1))));
			}
			return find(key,new BPlusTreeNode1(file1,Bytes.bytesToInt(node.valueAt(node.keyCount()-1))));
		}
	}
	
	private String reverseFind(byte key[],BPlusTreeNode2 node){
		if(node.isLeaf()){
			for(int i=0;i<node.keyCount();i++){
				if(new String(key).trim().equals(new String(node.valueAt(i)).trim())) return intToIp(Bytes.bytesToInt(node.keyAt(i)));
				//System.out.println(new String(key).length()+"*"+(new String(node.valueAt(i)).trim()).length());
			}
			return null;
		}
		else{
			for(int i=1;i<node.keyCount();i++){
				if(new String(key).compareTo(new String(node.keyAt(i)))<0) return reverseFind(key,new BPlusTreeNode2(file2,Bytes.bytesToInt(node.keyAt(i-1))));
			}
			return reverseFind(key,new BPlusTreeNode2(file2,Bytes.bytesToInt(node.keyAt(node.keyCount()-1))));
		}
	}

	public static int ipToInt(String line){
		return (Integer.parseInt(line.split(" ")[0])<<24) | (Integer.parseInt(line.split(" ")[1])<<16) | 
			   (Integer.parseInt(line.split(" ")[2])<<8) | (Integer.parseInt(line.split(" ")[3]));
	}
	
	public static String intToIp(int num){
		return ((num>>24)&0xff)+" "+((num>>16)&0xff)+" "+((num>>8)&0xff)+" "+(num&0xff);
	}
	
	public List<String> getAllData(){
		List<String> contents = new ArrayList<String>();
		BPlusTreeNode1 node = new BPlusTreeNode1(file1,metadata1.getInt(12));
		while(!node.isLeaf()){
			node = new BPlusTreeNode1(file1,Bytes.bytesToInt(node.valueAt(0)));
		}
		while(node.nextLeafAddress() != 0){
			for(int i=0;i<node.keyCount();i++){
				contents.add(Bytes.bytesToInt(node.keyAt(i))+" "+new String(node.valueAt(i)));
			}
			node = new BPlusTreeNode1(file1,node.nextLeafAddress());
		}
		return contents;
	}

	public void printContents(){
		BufferedWriter b = null;
		try {
			b = new BufferedWriter(new FileWriter("output IP - Domain.txt"));
			if(metadata1.getInt(12) == 0) return;
			printContents(new BPlusTreeNode1(file1,metadata1.getInt(12)),b);
		} catch (IOException e) { e.printStackTrace();}
		try {
			b.flush();
			b.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void printContents2(){
		//System.out.println("ROOT: "+(new BPlusTreeNode2(file2,Bytes.bytesToInt(new BPlusTreeNode2(file2,metadata2.getInt(12)).keyAt(0))).isLeaf())+
				//(new BPlusTreeNode2(file2,Bytes.bytesToInt(new BPlusTreeNode2(file2,metadata2.getInt(12)).keyAt(1))).isLeaf()));
		BufferedWriter b = null;
		try {
			b = new BufferedWriter(new FileWriter("output Domain - IP.txt"));
			if(metadata2.getInt(12) == 0) return;
			printContents2(new BPlusTreeNode2(file2,metadata2.getInt(12)),b);
		} catch (IOException e) { e.printStackTrace();}
		try {
			b.flush();
			b.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void printContents(BPlusTreeNode1 node,BufferedWriter b) throws IOException{
		System.out.println("Node "+(node.isLeaf()?"Leaf":"Internal"));
		b.write("Node "+(node.isLeaf()?"Leaf\n":"Internal\n"));
		if(!node.isLeaf()){
			for(int i=0;i<node.keyCount();i++){
				printContents(new BPlusTreeNode1(file1,Bytes.bytesToInt(node.valueAt(i))),b);
			}
		}
		else{
			for(int i=0;i<node.keyCount();i++){
				System.out.println(intToIp(Bytes.bytesToInt(node.keyAt(i)))+" : "+new String(node.valueAt(i)));
				b.write(intToIp(Bytes.bytesToInt(node.keyAt(i)))+" : "+new String(node.valueAt(i))+"\n");
			}
		}
		//System.out.println("C: "+new BPlusTreeNode(file,metadata.getInt(12)).keyCount());
	}
	public void printContents2(BPlusTreeNode2 node,BufferedWriter b) throws IOException{
		System.out.println("Node "+(node.isLeaf()?"Leaf":"Internal")+" "+node.getAddress());
		b.write("Node "+(node.isLeaf()?"Leaf\n":"Internal\n"));
		if(!node.isLeaf()){
			for(int i=0;i<node.keyCount();i++){
				//System.out.println("N.V->"+Bytes.bytesToInt(node.valueAt(i)));
				printContents2(new BPlusTreeNode2(file2,Bytes.bytesToInt(node.keyAt(i))),b);
			}
		}
		else{
			for(int i=0;i<node.keyCount();i++){
				System.out.println(new String(node.valueAt(i))+": "+intToIp(Bytes.bytesToInt(node.keyAt(i))));
				b.write(new String(node.valueAt(i))+": "+intToIp(Bytes.bytesToInt(node.keyAt(i)))+"\n");
			}
		}
		//System.out.println("C: "+new BPlusTreeNode(file,metadata.getInt(12)).keyCount());
	}
	
	public static void main(String args[]){
		BPlusTree tree = new BPlusTree(args[0]);
		//System.out.println(tree.find(ipToInt("173 244 164 35")));
		tree.printContents();
		tree.printContents2();
	}
}
