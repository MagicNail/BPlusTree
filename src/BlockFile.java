/* Code for COMP261 Assignment
 * Name:
 * Usercode:
 * ID:
 */

import java.util.*;
import java.io.*;


/** BlockFile
    Manages a random access file that can only be read and written to as
    fixed size blocks of bytes.
 */

public class BlockFile {

	private RandomAccessFile file;
	final int blockSize;  // size of blocks
	private int size = 0;          // current number of blocks in file
	private long countReadWrites = 0;

	/** Construct a new BlockFile with the given name and blocksize.
	 * If the file does not exist, it will be created, otherwise, it will
	 * be opened.
	 * A BlockFile is random access, with both read and write access,
	 * but data can only be read or a written in complete blocks.
	 * A block must be an array of bytes of the specified block size.
	 * A BlockFile will keep track of the size of the file (in blocks) and can write
	 * a new block to the end of the file, or can read or write any existing
	 * block. */
	public BlockFile(String name, int blockSize) throws IOException {
		this.blockSize = blockSize;
		File f = new File(name);
		file = new RandomAccessFile(f, "rw");
		if (f.exists()){
			size  = (int) (f.length() / blockSize);
			if (f.length() % blockSize != 0){
				file.setLength(size*blockSize);
			}
		} 
		else {
			size  = 0;
		}
	}

	/** Reads the index'th block from the file.
	 * @throws InvalidBlockFileException 
	 * */
	public Block read(int index) throws IOException, InvalidBlockFileException {
		if (index<0 || index>= size) throw new IndexOutOfBoundsException();
		file.seek(index*blockSize);
		byte[] bytes = new byte[blockSize];
		int retVal = file.read(bytes);
		countReadWrites++;
		if (retVal != blockSize) throw new InvalidBlockFileException(index);
		return new Block(bytes);
	}

	/** Write a block at the end of the file.
	 * The block must be of the correct size */
	public int write(Block block)throws IOException{
		file.seek(size*blockSize);
		file.write(block.getBytes());
		countReadWrites++;
		return size++;
	}

	public void write(Block block, int index) throws IOException {
		if (index<0 || index> size) throw new IndexOutOfBoundsException();
		file.seek(index*blockSize);
		file.write(block.getBytes());
		countReadWrites++;
	}

	public int getSize(){
		return size;
	}

	public long getReadWrites(){
		return countReadWrites;
	}
	
	public void close() throws IOException {
		file.close();
	}

}

