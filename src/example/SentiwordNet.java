package example;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

public class SentiwordNet {
	private String pathToSWN = "res/SentiWordNet.txt";
    private HashMap<String, Double> _dict;
    String somethingwrong = "";
    
    public SentiwordNet(InputStream inputStream){

        _dict = new HashMap<String, Double>();
        HashMap<String, Vector<Double>> _temp = new HashMap<String, Vector<Double>>();
        try{
        	/*
        	 * Check if the input stream in null if it is then try to read from local file
        	 * if not empty then read form the s3 amazon 
        	 * */
        	BufferedReader csv = null;
        	if(inputStream == null)
        		csv =  new BufferedReader(new FileReader(pathToSWN));
        	else
        		csv = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
        	
            String line = "";
            while((line = csv.readLine()) != null)
            {
                if(line.startsWith("#") || line.startsWith(" "))
                    continue;
                String[] data = line.split("\t");
                Double score = Double.parseDouble(data[2])-Double.parseDouble(data[3]);
                String[] words = data[4].split(" ");
                for(String w:words)
                {
                    String[] w_n = w.split("#");
                    w_n[0] += "#"+data[0];
                    int index = Integer.parseInt(w_n[1])-1;
                    if(_temp.containsKey(w_n[0]))
                    {
                        Vector<Double> v = _temp.get(w_n[0]);
                        if(index>v.size())
                            for(int i = v.size();i<index; i++)
                                v.add(0.0);
                        v.add(index, score);
                        _temp.put(w_n[0], v);
                    }
                    else
                    {
                        Vector<Double> v = new Vector<Double>();
                        for(int i = 0;i<index; i++)
                            v.add(0.0);
                        v.add(index, score);
                        _temp.put(w_n[0], v);
                    }
                }
            }
            Set<String> temp = _temp.keySet();
            for (Iterator<String> iterator = temp.iterator(); iterator.hasNext();) {
                String word = (String) iterator.next();
                Vector<Double> v = _temp.get(word);
                double score = 0.0;
                double sum = 0.0;
                for(int i = 0; i < v.size(); i++)
                    score += ((double)1/(double)(i+1))*v.get(i);
                for(int i = 1; i<=v.size(); i++)
                    sum += (double)1/(double)i;
                score /= sum;
                String sent = "";
                if(score>=0.75)
                    sent = "strong_positive";
                else
                if(score > 0.25 && score<=0.5)
                    sent = "positive";
                else
                if(score > 0 && score>=0.25)
                    sent = "weak_positive";
                else
                if(score < 0 && score>=-0.25)
                    sent = "weak_negative";
                else
                if(score < -0.25 && score>=-0.5)
                    sent = "negative";
                else
                if(score<=-0.75)
                    sent = "strong_negative";
                _dict.put(word, score);
            }
        }
        catch(Exception e){
        	somethingwrong=e.toString();
            e.printStackTrace();
        }

    }

    public Double extract(String word)
    {
        Double total = new Double(0);
        if(_dict.get(word+"#n") != null)
            total = _dict.get(word+"#n") + total;
        if(_dict.get(word+"#a") != null)
            total = _dict.get(word+"#a") + total;
        if(_dict.get(word+"#r") != null)
            total = _dict.get(word+"#r") + total;
        if(_dict.get(word+"#v") != null)
            total = _dict.get(word+"#v") + total;
        return total;
    }
    
    public Double calcSentimentForSentence(String sentence){
    	String[] words = sentence.split("\\s+");
        double totalScore = 0;
        for(String word : words) {
            if(word.length() <= 0)
                return 0.0;
            //String tail = word.substring(word.length() - 1);
            word = word.replaceAll("([^a-zA-Z\\s])", "");
            if (extract(word) == null)
                continue;
            totalScore += extract(word);
        }
        //return somethingwrong+":"+totalScore;
        return totalScore;
        /*if(totalScore == 0)
        {
            System.out.println("Neutral Statement :" + totalScore);
        } else if(totalScore > 0) {
            System.out.println("Postive Statement :" + totalScore);
        } else {
            System.out.println("Negative Statement :" + totalScore);
        }*/
    }
    
    /*public static void main(String[] args) {
        
        String topic = "This is good bot";
        ArrayList<String> st = new ArrayList<>();
        st.add("1");
        st.add("2");
        st.add("3");
        st.add("4");
        st.add("5");
        st.add("6");
        st.add("7");
        st.add("8");
        st.add("9");
        st.add("10");
        //st.add("11");
        //st.add("12");
        if(st.size() > 10){
        	st.removeAll(st.subList(0, st.size()-10));
        }
		for(String s:st){
        	System.out.println(s);
        }
        //System.out.println("setiment:"+new SentiwordNet().calcSentimentForSentence(topic));
    }*/
       

}
