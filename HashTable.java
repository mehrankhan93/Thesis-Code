package AIMatchingAlgo;

import java.util.HashMap;
import java.util.HashSet;


public class HashTable {

    public HashMap<Integer,HashSet<Integer>> HashTable; 
    
    public int HashTableId;
    
    public void setHashTable(int id,HashMap<Integer,HashSet<Integer>> HashTable)
    {
        this.HashTableId = id;
        this.HashTable = HashTable;
    }
    public HashMap<Integer,HashSet<Integer>> getcurHashTableBuckets()
    {
        return this.HashTable;
    }
}