
/**
 * Interface for the 2 kinds of tree nodes, Key-Value and Value-Key
 * @author Jeff
 *
 */
public interface BPlusTreeNode {
	/**
	 * Split this node in 2, adding the new value to whichever node it should be sorted into. Should only
	 * be called if the node is too full to fit an additional value
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public BPlusTreeNode splitLeaf(byte key[],byte[] value);
	/**
	 * handles splitting of the internal nodes in the tree
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public BPlusTreeNode dealWithPromote(byte key[],byte[] value);
	/**
	 * Add the following key and value to the node. kkey and value should be the size specified in the
	 * metadata of the tree (4 and 60 in this implementation)
	 * 
	 * @param key
	 * @param value
	 */
	public void add(byte key[],byte value[]);
	/**
	 * Returns true if there is room to add another key value pair to this node
	 * 
	 * @return
	 */
	public boolean hasRoom();
	/**
	 * Write the node to the file it is stored in. Called automatically whenever a change is made
	 */
	public void save();
	/**
	 * returns true if node is a leaf
	 * @return
	 */
	public boolean isLeaf();
	/**
	 * returns the number of key/value pairs in this node
	 * @return
	 */
	public int keyCount();
	/**
	 * returns the key at a given index in the node
	 * @param i
	 * @return
	 */
	public byte[] keyAt(int i);
	/**
	 * returns the value at a given index in the node
	 * @param i
	 * @return
	 */
	public byte[] valueAt(int i);
	/**
	 * returns the address that this node is being written to in the file
	 * @return
	 */
	public int getAddress();
	/**
	 * returns the address of the next leaf in the chain, or 0 if this is an internal node or the last leaf node
	 * @return
	 */
	public int nextLeafAddress();
}
