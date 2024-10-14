package minhash;
import AIMatchingAlgo.Matching;
import AIMatchingAlgo.HashTable;
import AIMatchingAlgo.Matching;
import AIMatchingAlgo.ProcessData;
import java.util.HashSet;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import java.util.Random;
import javafx.util.Pair;



public class MinHashing {
    String strLine, AL;
    
    HashMap<Integer,ArrayList<Integer>> hAllHashFunctions = new HashMap<Integer, ArrayList<Integer>>();
    
    

    public void createHashFunctions(int numHashFunc,int totalFeatures)
    { 
        int Universe = totalFeatures * 20;
        ArrayList<Integer> ar = new ArrayList<Integer>();
        for(int i=0;i<Universe;i++){
            ar.add(i);
            //System.out.println(ar.size());
        }
        
        for(int i=0;i<numHashFunc;i++) 
        {
            java.util.Collections.shuffle(ar); 
            ArrayList<Integer> arPermutedList = new ArrayList<Integer>(); 
              for(int ii: ar){
                  arPermutedList.add(ii);
                }
            this.hAllHashFunctions.put(i, arPermutedList);
        }
        ar.clear();
    }
    
    //MinHashing CV
    public ArrayList<Integer> MinHashes_CV(ArrayList<Integer> arInputSet, int TotHashFunc)
    {
       
       ArrayList<Integer> arMinHashedVector = new ArrayList<Integer>();
       int i = 0;
       int Location = 0, Counter = 999999999;
       int val = 0;
       //int j=0;
       int tempVal;
       try
       {
           
           int j = 0;

           for (i = 0; i < TotHashFunc; i++) {
               ArrayList<Integer> arHashFunction = new ArrayList<Integer>();
               arHashFunction = hAllHashFunctions.get(i);
               for (j = 0; j < arInputSet.size(); j++) {
                   tempVal = arInputSet.get(j);

                   Location = arHashFunction.get(tempVal);
                   if (Location < Counter) {
                       Counter = Location;
                   }
               }
               arMinHashedVector.add(Counter);
               Counter = 999999999;
               j = 0;
           }
           
      }
      catch(Exception e)
      {
            System.out.println(e.getMessage());
      }
       return arMinHashedVector;
    }
    
    //MinHashing JD
    public ArrayList<Integer> MinHashes_JD(ArrayList<Pair<Integer,Integer>> arInputSet, int TotHashFunc)
    {
       ArrayList<Integer> arMinHashedVector = new ArrayList<Integer>();
       int i = 0;
       int Location = 0, Counter = 999999999;
       int val = 0;
       int j=0;
       int tempVal = 0;
       double EW = 0;
       int[] arEWArray = null;
        for (i = 0; i < TotHashFunc; i++) {
            ArrayList<Integer> arHashFunction = new ArrayList<Integer>();
            arHashFunction = hAllHashFunctions.get(i);
            for (j = 0; j < arInputSet.size(); j++) {
               tempVal = arInputSet.get(j).getKey();
                Random rand = new Random();
                
                EW = ProcessData.weights.get(arInputSet.get(j).getValue());
                EW/=5;
                arEWArray = new int[20 + 1]; 
                for (int ewCntr = 1; ewCntr <= EW; ewCntr++) {// redundent
                    arEWArray[ewCntr] = 1;
                }

                for (int ewCntr = 1; ewCntr <= EW; ewCntr++) {
                    if (arEWArray[ewCntr] == 0) {
                        break;
                    }
                    int ind = (tempVal * 20) + ewCntr;
                    Location = arHashFunction.get(ind);
                    if (Location < Counter) {
                        Counter = Location;
                    }
                }
            }
            
            arMinHashedVector.add(Counter);
            Counter = 999999999;
            j = 0;
        }
       return arMinHashedVector; 
       
    }
    
    
    public void mergeSimilar(ArrayList<Integer> Minhashes, int Bands, int qry)
    {
        int r = Minhashes.size();
        int interval = r / Bands;
        int i=0;
        
        int curBand = 0;
        try{
            for(i=0;i<r;i+=interval)
            {
                int[] subMinHashList = new int[interval];
                int cntr = i;
                for(int j=0;j<interval;j++)
                {
                    subMinHashList[j] = Minhashes.get(cntr);
                    cntr++;
                }
                
                int combinedHash = hashCode(subMinHashList); 
                
                
                HashTable curHashTable = Matching.arHashTables.get(curBand);
                HashMap<Integer,HashSet<Integer>>  curHashTableBuckets = curHashTable.getcurHashTableBuckets();
                
                
                if(curHashTableBuckets.containsKey(combinedHash))
                {
                    HashSet<Integer> hUpdatedList = new HashSet<Integer>();
                    hUpdatedList = curHashTableBuckets.get(combinedHash);
                    hUpdatedList.add(qry);
                    curHashTableBuckets.put(combinedHash, hUpdatedList);
                    
                    if (!Matching.simTable.containsKey(qry))
                        Matching.simTable.put(qry, new ArrayList<Integer>());

				
                    Matching.simTable.get(qry).add(combinedHash);
                }
                else
                {
                    HashSet<Integer> hNbrList = new HashSet<Integer>();
                    hNbrList.add(qry);
                    curHashTableBuckets.put(combinedHash, hNbrList);
                    if (!Matching.simTable.containsKey(qry))
                        Matching.simTable.put(qry, new ArrayList<Integer>());

				
                    Matching.simTable.get(qry).add(combinedHash);
                    
                }
                curBand++;
            }
        }catch(Exception e)
        {
           System.out.println(e.getMessage());
        }
    }
    
    public HashSet<Integer> Query(int qry,int HashTablesCount,HashMap<Integer,Double> featureSet)
    {
        HashSet<Integer> hSim = new HashSet<Integer>();
        
        Multiset<Integer> msCSSN = HashMultiset.create();
        int collisionCount =0;
        try{
                ArrayList<Integer>  arBucketIds = Matching.simTable.get(qry); 
                
                for(int i=0;i<HashTablesCount;i++)
                {
                    HashTable curHashTable = Matching.arHashTables.get(i);
                    HashMap<Integer,HashSet<Integer>>  curHashTableBuckets = curHashTable.getcurHashTableBuckets();
                    
                    int bktID = arBucketIds.get(i);
                    
                    
                    HashSet<Integer> hCurBucket = curHashTableBuckets.get(bktID);
                    msCSSN.addAll(hCurBucket);
                }
                
                int prvId = 0;
                for(int curId : msCSSN) 
                {
                    if(curId == prvId) 
                       continue;
                    collisionCount = msCSSN.count(curId);
                    if(collisionCount >= Matching.hashCollisionCount){               
                        hSim.add(curId);
                        if(curId!=qry)
                            featureSet.put(curId, (collisionCount/Double.valueOf(HashTablesCount))*100);
                        
                    }
                    prvId = curId;
                }
                
                
                if(hSim.contains(qry))
                    hSim.remove(qry);
            
        }catch(Exception e)
        {
            System.out.println(e.getMessage());
        }
        return hSim;
    }
    
       
    
    public int hashCode(int a[]) {
        double s = 0;
        double x = 0; // contains the fractional part
        double h = 0;
        int result = 0;
        double temp = 0;
        int m = 512; // size of the hash table
        double A = 0.6180339887;
        
        for (int k : a)
        {
            s = k * A;
            x = s % 1;
            h = m * x;
            temp = Math.floor(h);
            result = (int) (result + temp);
        }
            

        return result;
    }
}