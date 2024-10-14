package AIMatchingAlgo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import javafx.util.Pair;
import minhash.MinHashing;


 
public final class Matching {
        
    /*********************/
   public static String JDfilePath;
   public static String CVfilePath;
   public static String ResultfilePath;
   public static int numHashFuncs;
   public static int bands; 
    
    
   
    
    public static ArrayList<HashTable> arHashTables = new ArrayList<HashTable>();
    public static HashMap<String,Integer> hBucketsSize = new HashMap<String,Integer>();
    
    public static int totalFeatures;
    public static int totalPersons;
    public static HashMap<Integer, SortedSet> featureSet;
    public static HashMap<Integer, ArrayList<Integer>> simTable=new HashMap<Integer,ArrayList<Integer>>();
    public static boolean flag=true;
    MinHashing m = new MinHashing();
    public static int hashCollisionCount = 2;
    public static HashMap<Integer,Double> hSimCVP;
    static int  hfuncs[]={10,20,50,100};
    static int  band[][]={{5,2,10,1},{5,4,10,2},{25,2,10,5},{2,10,5,4}};
    public long start;
    public static void main(String[] args) throws IOException{
     

    // call the method
    

    // get the end time
    
    new Matching();
    // execution time
        
        
    }
    
    public Matching() throws IOException
    {
        FileWriter writer =null;
        File file =new File("experiment.txt");
        try{
        if (!file.exists()) {
            file.createNewFile();
        }
        writer = new FileWriter(file);
        ProcessData.readConfig();
        featureSet = ProcessData.buildFeatureSet(JDfilePath,CVfilePath);
        for(int a=0;a<hfuncs.length;a++){
            numHashFuncs = hfuncs[a];
            for(int b=0;b<band[a].length;b++){
                bands = band[a][b];
                System.out.println("H =  " + numHashFuncs + "\nB = " + bands + "\nFive Trials");
                 writer.write("H =  " + numHashFuncs + "\nB = " + bands + "\nFive Trials\n");
                 writer.flush();
                for(int c=0;c<1;c++){
                    start = System.nanoTime();
                    for(int i=1;i<=bands;i++)
                    {
                        HashMap<Integer,HashSet<Integer>> hBuckets = new HashMap<Integer,HashSet<Integer>>();
                        HashTable h = new HashTable();
                        h.setHashTable(i, hBuckets);
                        arHashTables.add(h);
                    }
                    Index(bands, totalFeatures);
                    Query(hfuncs[a],band[a][b]);
                    long end = System.nanoTime();
                    long execution = end - start;
                    System.out.println("Execution time: " + execution/1000000000 + "seconds");
                    writer.write("Execution time: " + execution/1000000000 + "seconds\n");
                    writer.flush();
                }
            }
        }
        
        }catch (IOException ioe) {
            ioe.printStackTrace();
	}
//        ProcessData.readConfig();
//        featureSet = ProcessData.buildFeatureSet(JDfilePath,CVfilePath);
//        for(int i=1;i<=bands;i++)
//        {
//            HashMap<Integer,HashSet<Integer>> hBuckets = new HashMap<Integer,HashSet<Integer>>();
//            HashTable h = new HashTable();
//            h.setHashTable(i, hBuckets);
//            arHashTables.add(h);
//        }
//        Index(bands, totalFeatures);
//        Query();
    }
    
    public void Index(int Bands, int totalFeatures)
    {
        
        m.createHashFunctions(numHashFuncs, totalFeatures);
        //System.out.println("HashFunctions Created"); 
       

        for(int n=0;n<totalPersons;n++)
        {
            ArrayList<Pair<Integer,Integer>> arNbrs = new ArrayList<Pair<Integer,Integer>>();
            Iterator<Pair<Integer,Integer>> itr = (featureSet.get(n).iterator());
            
            while (itr.hasNext()) {
                arNbrs.add(itr.next());
            }

            ArrayList<Integer> arMinHashedVector = m.MinHashes_JD(arNbrs, numHashFuncs);
            m.mergeSimilar(arMinHashedVector, Bands, n);
        }
    }
    
    public void Query(int h,int b)
    {
        FileWriter writer =null;
        File file =new File(ResultfilePath+"-"+h+"-"+b+".csv");
        try{
        if (!file.exists()) {
            file.createNewFile();
        }
        writer = new FileWriter(file);
        writer.append("ID");
                writer.append(',');
                writer.append("Percentage");
                writer.append('\n');
        
        HashSet<Integer> hSimilarCVs = new HashSet<Integer>();
        hSimCVP= new HashMap<Integer,Double>();
        
        for(int n=0;n<1;n++)
        {
            hSimilarCVs.clear();
            hSimCVP.clear();
            hSimilarCVs = m.Query(n,bands,hSimCVP); 
            /*
            for(int i : hSimilarCVs){
                System.out.print(i+":");
                System.out.println(hSimCVP.get(i));                
            }*/
        }
       
        Map<Integer, String> map = sortByValues(hSimCVP); 
        //System.out.println("After Sorting:");
        Set set2 = map.entrySet();
        Iterator iterator2 = set2.iterator();
        while(iterator2.hasNext()) {
            Map.Entry me2 = (Map.Entry)iterator2.next();
            /*System.out.print(me2.getKey() + ": ");
            System.out.println(me2.getValue());*/
            writer.append(ProcessData.pmap.get(me2.getKey()));
            writer.append(',');
            writer.append(String.valueOf(me2.getValue()));
            writer.append('\n');
        }
        writer.flush();
        writer.close();
    
        }catch (IOException ioe) {
            ioe.printStackTrace();
	}
        
    }
    
    private static HashMap sortByValues(HashMap map) { 
       List list = new LinkedList(map.entrySet());
       // Defined Custom Comparator here
       Collections.sort(list, new Comparator() {
            public int compare(Object o1, Object o2) {
               return ((Comparable) ((Map.Entry) (o2)).getValue())
                  .compareTo(((Map.Entry) (o1)).getValue());
            }
       });

       // Here I am copying the sorted list in HashMap
       // using LinkedHashMap to preserve the insertion order
       HashMap sortedHashMap = new LinkedHashMap();
       for (Iterator it = list.iterator(); it.hasNext();) {
              Map.Entry entry = (Map.Entry) it.next();
              sortedHashMap.put(entry.getKey(), entry.getValue());
       } 
       return sortedHashMap;
  }
}

