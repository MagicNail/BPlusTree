import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;


public class BPlusTreeTests {

	@Test
	public void testAdd() {
		BPlusTree tree = new BPlusTree();
		for(int i=0;i<2500;i++){
			tree.add(Bytes.intToBytes((int)(Math.random()*99999)),(""+Math.random()*99999999).getBytes());
		}
		tree.add(Bytes.intToBytes(66666),"SPECIAL".getBytes());
		tree.add(Bytes.intToBytes(1337),"LEET".getBytes());
		//Check the bidirectional mappings
		System.out.println("SPECIAL -> "+tree.find("SPECIAL"));
		System.out.println("66666 -> "+tree.find(66666));
		System.out.println("LEET -> "+tree.find("LEET"));
		System.out.println("1337 -> "+tree.find(1337));
		//OPTIONAL: print the structure of both trees, file will be too large to read manually
		//tree.printContents();
		//tree.printContents2();
	}

}
