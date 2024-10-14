package AIMatchingAlgo;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;                   
import javafx.util.Pair;
public class ProcessData
{

    public static HashMap<Integer,String> pmap;
    private static HashMap<String,Integer> voc = new HashMap<String,Integer>();
    public static HashMap<String,Integer> weights=new HashMap<String,Integer>();
    private static int pid,fid;
    private static HashMap<Integer, SortedSet> featureSet = new HashMap<Integer,SortedSet>();
        
    private static void readMapping(){
            try {
                FileInputStream fileIn = new FileInputStream("vocmap.ser");
                ObjectInputStream in = new ObjectInputStream(fileIn);
                voc = (HashMap<String,Integer>) in.readObject();
                in.close();
                fileIn.close();
            } catch (IOException i) {
                i.printStackTrace();
            } catch (ClassNotFoundException c) {
                
                c.printStackTrace();
            }
        }
    private static void writeMapping(){
        try {
            FileOutputStream fileOut = new FileOutputStream("vocmap.ser");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(voc);
            out.close();
            fileOut.close();
           // System.out.printf("Serialized data is saved");
        } catch (IOException i) {
            i.printStackTrace();
        }
    }
    private static void addSingleEntry(String x, String y) {
        if (voc.containsKey(x)) {
            featureSet.get(pid).add(new Pair<>(voc.get(x), y));
        } else {
            voc.put(x, fid);
            featureSet.get(pid).add(new Pair<>(fid, y));
            fid++;
        }
    }
    private static String expBin(String exp){
            Float temp=Float.parseFloat(exp);
            if(temp<2)
            {
                return "FRESH";
            }else if(temp>=2&&temp<=4)
            {
                return "ENTRY";
            }else if(temp>=5&&temp<=8)
            {
                return "SENIOR";
            }else if(temp>=9&&temp<=12)
            {
                return "EXPERT";
            }else if(temp>=13&&temp<=16)
            {
                return "EXECUTIVE";
            }else if(temp>16)
            {
                return "PRINCIPAL";
            }
            return null;
        }
    
    private static String expSkillBin(String skillExp){
             int exp=Integer.parseInt(skillExp);
             if (exp <= 2) {
                 return "NOV";
             } else if (exp > 2 && exp <= 4) {
                 return "INT";
             } else if (exp > 4 && exp <= 6) {
                 return "MED";
                 
             } else if (exp > 6) {
                 return "EXP";
             }
             return null;
         }
    private static void addJDSkill(String skills,String gsW,String esW){
            String[] skill=skills.split(":");
            for (int i = 0; i < skill.length; i++  /*= i + 3*/) {
                addSingleEntry(skill[i],gsW);
                //String bin=expSkillBin(skill[i+1]);
                //if (skill[i+2].equals("M")) {
                  //  addSingleEntry(skill[i] + bin,esW);
                //}else if (skill[i+2].equals("N")) {
                  //  addSingleEntry(skill[i] + bin,"N");
                //}

            }
            
        }; 
    private static void addGSkill(String skills,String gsW){
        String[] skill = skills.split(":");
        for (String skill1 : skill) {
            addSingleEntry(skill1, gsW);
        }
    }
    private static void addESkill(String skills,String esW){
        String[] skill = skills.split(":");
        for (int i = 0; i < skill.length; i+=2) {
            String bin = expSkillBin(skill[i + 1]);
            addSingleEntry(skill[i] + bin, esW);
        }
    }
    public static HashMap<Integer, SortedSet> buildFeatureSet(String JDfile, String CVfile) {

        BufferedReader input = null;
        String[] tokens;
        String line = null;
        pmap = new HashMap<Integer,String>();

        //readMapping();

        pid = 0;
        fid = voc.size();

        try {

            input = new BufferedReader(new FileReader(new File(JDfile)));

            line = input.readLine();
            line = input.readLine();
            //while ((line = input.readLine()) != null)
            {
                tokens = line.split(",");
                pmap.put(pid,tokens[0]);
                featureSet.put(pid, new TreeSet<Pair<Integer, String>>((Pair<Integer, String> p1, Pair<Integer, String> p2) -> {
                    return p1.getKey().compareTo(p2.getKey());
                }));

                addSingleEntry(tokens[2], "EDU");
                weights.put("EDU", Integer.parseInt(tokens[1]));
                addSingleEntry(tokens[4], "JT");
                weights.put("JT", Integer.parseInt(tokens[3]));
                addSingleEntry(tokens[6], "CITY");
                weights.put("CITY", Integer.parseInt(tokens[5]));
                addSingleEntry(expBin(tokens[8]), "EXP");
                weights.put("EXP", Integer.parseInt(tokens[7]));
                int gsW = Integer.parseInt(tokens[9]);
                weights.put("GSKILL", Integer.parseInt(tokens[9]));
                //int esW = Integer.parseInt(tokens[10]);
                //weights.put("ESKILL", Integer.parseInt(tokens[10]));
                addJDSkill(tokens[10], "GSKILL", "ESKILL");
               // weights.put("N", 1);
                
                pid++;
            }
            input = new BufferedReader(new FileReader(new File(CVfile)));

            line = input.readLine();
            while ((line = input.readLine()) != null) {
                tokens = line.split("\t");
                pmap.put(pid,tokens[0]);
                featureSet.put(pid, new TreeSet<Pair<Integer, Integer>>((Pair<Integer, Integer> p1, Pair<Integer, Integer> p2) -> {
                    return p1.getKey().compareTo(p2.getKey());
                }));

                addSingleEntry(tokens[1], "EDU");

                String[] jtitles=tokens[2].split(":");
                for(int i = 0 ; i < jtitles.length ; i++){
                    addSingleEntry(jtitles[i], "JT");
                }
                        
                

                addSingleEntry(tokens[3], "CITY");

                addSingleEntry(expBin(tokens[4]), "EXP");
                
                addGSkill(tokens[5],"GSKILL");
                
               // addESkill(tokens[6],"ESKILL");
                pid++;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Matching.totalPersons=pid;
        Matching.totalFeatures=fid;
        
        //writeMapping();
        
        return featureSet;
    }
    public static void readConfig(){
        BufferedReader input = null;
        String line;
        String[] tokens;
        try{
            input = new BufferedReader(new FileReader(new File("config.txt")));
            line = input.readLine();
            tokens = line.split(":");
            Matching.numHashFuncs = Integer.parseInt(tokens[1]);
            line = input.readLine();
            tokens = line.split(":");
            Matching.bands = Integer.parseInt(tokens[1]);
            line = input.readLine();
            tokens = line.split(":");
            Matching.JDfilePath = tokens[1];
            line = input.readLine();
            tokens = line.split(":");
            Matching.CVfilePath = tokens[1];
            line = input.readLine();
            tokens = line.split(":");
            Matching.ResultfilePath = tokens[1];
            
            
           
            
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}