import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * These tree nodes sort their information from key to value
 * 
 * @author Jeff
 *
 */
public class BPlusTreeNode1 implements BPlusTreeNode{

	private boolean isLeaf;
	private int keyCount = 0;
	private int address;
	private int nextLeafAddress = 0;
	private BlockFile file;
	private ArrayList<byte[]> keys = new ArrayList<byte[]>();
	private ArrayList<byte[]> values = new ArrayList<byte[]>();
	
	public BPlusTreeNode1(BlockFile file,boolean isLeaf,int nextLeafAddress){
		this.file = file;
		this.isLeaf = isLeaf;
		this.nextLeafAddress = nextLeafAddress;
		try {
			address = file.write(new Block(file.read(0).getInt(0)));
			save();
		} catch (IOException | InvalidBlockFileException e) {e.printStackTrace();}
	}
	public BPlusTreeNode1(BlockFile file,int index){
		this.file = file;
		Block block;
		try {
			Block metadata = file.read(0);
			block = file.read(index);
			isLeaf = block.getInt(0) == 1;
			keyCount = block.getInt(4);
			address = index;
			nextLeafAddress = block.getInt(12);
			for(int i=0;i<keyCount;i++){
				if(isLeaf){
					keys.add(block.getBytes(16+i*(metadata.getInt(4)+metadata.getInt(8)),metadata.getInt(4)));
					values.add(block.getBytes(16+metadata.getInt(4)+i*(metadata.getInt(4)+metadata.getInt(8)),metadata.getInt(8)));
				}
				else{
					keys.add(block.getBytes(16+i*(metadata.getInt(4)+4),metadata.getInt(4)));
					values.add(block.getBytes(16+metadata.getInt(4)+i*(metadata.getInt(4)+4),4));
				}
				//System.out.println("K-V: "+Bytes.bytesToInt(keys.get(i))+" "+new String(values.get(i)));
			}
		} catch (IOException | InvalidBlockFileException e) {e.printStackTrace();}
	}
	
	public BPlusTreeNode1 splitLeaf(byte key[],byte[] value){
		if(!isLeaf) return null;
		BPlusTreeNode1 node = new BPlusTreeNode1(file,true,nextLeafAddress);
		nextLeafAddress = node.address;
		keys.add(key);
		values.add(value);
		for(int i = keys.size()-1;i>0 && Bytes.bytesToInt(keys.get(i-1))>Bytes.bytesToInt(key);i--){
			byte tempKey[] = keys.get(i-1);
			byte tempValue[] = values.get(i-1);
			keys.set(i-1,keys.get(i));
			values.set(i-1,values.get(i));
			keys.set(i,tempKey);
			values.set(i,tempValue);
		}
		int mid = (keyCount()+1)/2;
		while(keys.size() > mid){
			node.add(keys.get(mid),values.get(mid));
			keys.remove(mid);
			values.remove(mid);
		}
		save();
		node.save();
		return node;
	}
	
	public BPlusTreeNode1 dealWithPromote(byte key[],byte[] value){
		if(isLeaf) return null;
		BPlusTreeNode1 node = new BPlusTreeNode1(file,false,nextLeafAddress);
		nextLeafAddress = node.address;
		keys.add(key);
		values.add(value);
		for(int i = keys.size()-1;i>0 && Bytes.bytesToInt(keys.get(i-1))>Bytes.bytesToInt(key);i--){
			byte tempKey[] = keys.get(i-1);
			byte tempValue[] = values.get(i-1);
			keys.set(i-1,keys.get(i));
			values.set(i-1,values.get(i));
			keys.set(i,tempKey);
			values.set(i,tempValue);
		}
		int mid = (keyCount()+1)/2;
		while(keys.size()-1 > mid){
			node.add(keys.get(mid),values.get(mid));
			keys.remove(mid);
			values.remove(mid);
		}
		save();
		node.save();
		return node;
	}
	
	public void add(byte key[],byte value[]){
		if(!hasRoom()) throw new IndexOutOfBoundsException();
		keys.add(key);
		values.add(value);
		for(int i = keys.size()-1;i>0 && Bytes.bytesToInt(keys.get(i-1))>Bytes.bytesToInt(key);i--){
			byte tempKey[] = keys.get(i-1);
			byte tempValue[] = values.get(i-1);
			keys.set(i-1,keys.get(i));
			values.set(i-1,values.get(i));
			keys.set(i,tempKey);
			values.set(i,tempValue);
		}
		save();
	}
	public boolean hasRoom(){
		try {
			return keys.size()<(isLeaf ? file.read(0).getInt(20) : file.read(0).getInt(16));
		} catch (IOException | InvalidBlockFileException e) {}
		return false;
	}
	public void save(){
		try{
			Block metadata = file.read(0);
			Block block = new Block(metadata.getInt(0));
			block.setInt(isLeaf?1:0,0);
			block.setInt(keys.size(),4);
			block.setInt(address,8);
			block.setInt(nextLeafAddress,12);
			for(int i=0;i<keys.size();i++){
				if(isLeaf){
					block.setBytes(keys.get(i),16+i*(metadata.getInt(4)+metadata.getInt(8)));
					block.setBytes(values.get(i),16+metadata.getInt(4)+i*(metadata.getInt(4)+metadata.getInt(8)));
				}
				else{
					block.setBytes(keys.get(i),16+i*(metadata.getInt(4)+4));
					block.setBytes(values.get(i),16+metadata.getInt(4)+i*(metadata.getInt(4)+4));
				}
				//System.out.println(block.getInt(16+i*(metadata.getInt(4)+metadata.getInt(8)))+" : "+
						//(block.getString(16+metadata.getInt(4)+i*(metadata.getInt(4)+metadata.getInt(8)),60)));
			}
			file.write(block,address);
		} catch(IOException | InvalidBlockFileException e){}
	}
	public boolean isLeaf(){
		return isLeaf;
	}
	public int keyCount(){
		return keys.size();
	}
	public byte[] keyAt(int i){
		return keys.get(i);
	}
	public byte[] valueAt(int i){
		return values.get(i);
	}
	public int getAddress(){
		return address;
	}
	public int nextLeafAddress(){
		return nextLeafAddress;
	}
 }
