package jurl;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class Main {
    static char first = ':';
    static char second = ',';
    static String s1 = "Header";
    public static void main(String[] args) {
        try {
            handleEntry(args);
        }catch (ArrayIndexOutOfBoundsException e){

            System.err.println("you must enter some thing");
        }
    }

    /**
     * handle entered commands
     * @param args commands
     */
    private static void handleEntry(String[] args){
        URL url = null ;
        String method = "GET" ;
        boolean header = false ;
        int post = 0;
        boolean follow = false ;
        HashMap<String, String> fooBody = null;
        String json = null ;
        File binaryFile = null ;
        boolean saveToFile = false ;
        File fileToSave = null;
        HashMap<String, String> headerToSend = null ;
        boolean addToList = false ;
        if (args[0].contains("-") && args.length == 1 && !(args[0].equals("--help") || args[0].equals("-h"))){
            System.out.println("wrong input. enter --help");
            return;
        }
        if (args[0].equals("list")) {
            Request showList = new Request();
        }
        else if(args[0].equals("fire")){
            int[] getRequests = new int[args.length - 1];
            for (int i = 1; i < args.length; i++){
                try {
                    getRequests[i-1] = (Integer.parseInt(args[i])) - 1;

                }catch (ArrayIndexOutOfBoundsException e){
                    System.out.println("hello");
                }catch (NumberFormatException e){
                    System.err.println("wrong input. you must enter numbers");
                    return;
                }
            }
            Request showList = new Request(getRequests);
        }
        else if(args[0].equals("-h") || args[0].equals("--help")){
            printHelp();
        }
        else {
            int[] link = new int[args.length];
            for (int i = 0; i < args.length; i++) {
                if (args[i].equals("-i")) {
                    header = true;
                    link[i]=1;
                } else if (args[i].equals("-M") || args[i].equals("--method")) {
                    link[i] = 1;link[i+1] = 1;
                    method = args[i + 1];
                } else if (args[i].equals("-H") || args[i].equals("--headers")) {
                    link[i] = 1;
                    link[i+1] = 1;
                    first = ':';
                    second = ',';
                    String s1 = "Header";
                    headerToSend = headerProcessor(args[i+1]);
                } else if (args[i].equals("-f")) {
                    link[i] = 1;
                    follow =true ;
                } else if (args[i].equals("-O") || args[i].equals("--output")) {
                    try {
                        if (args[i + 1].contains("-") || args[i + 1].contains("http")) {
                            Date date = new Date();
                            fileToSave = new File(String.format("output_[%d].txt",(int)date.getTime()/1000));
                            saveToFile = true;
                            link[i] = 1;
                        } else {
                            fileToSave = new File(args[i + 1]);
                            saveToFile = true;
                            link[i] = 1;link[i+1] = 1;
                        }
                    }catch (ArrayIndexOutOfBoundsException e){
                        saveToFile = true;
                        try {
                            Date date = new Date();
                            fileToSave = new File(String.format("output_[%d].txt",(int)date.getTime()/1000));
                            link[i] = 1;
                            if (fileToSave.createNewFile()) {
                            } else {
                                System.out.println("File already exists.");
                            }
                        } catch (IOException e1) {
                            System.out.println("An error occurred.");
                            e1.printStackTrace();
                        }
                    }
                } else if (args[i].equals("-S") || args[i].equals("--save")) {
                    addToList = true;
                    link[i] = 1;
                } else if (args[i].equals("-D") || args[i].equals("--data")) {
                    first = '=';
                    second = '&';
                    s1 = "Form Data";
                    fooBody = requestBodyProcessor(args[i+1]);
                    post = 1;
                    link[i] = 1;link[i+1] = 1;
                } else if (args[i].equals("-j") || args[i].equals("--json")) {
                    json = args[i + 1];
                    post = 2;
                    link[i] = 1;link[i+1] = 1;
                } else if (args[i].equals("--upload")) {
                    binaryFile = new File(args[i + 1]);
                    post = 3;
                    link[i] = 1;link[i+1] = 1;
                }
            }
            for (int i = 0; i < link.length ; i++) {
                if (link[i] == 0){
                    link[i] = 1;
                    try {
                        url = new URL(args[i]);
                    } catch (MalformedURLException e) {
                        try {
                            url = new URL("http://"+args[i]);
                        } catch (MalformedURLException ex) {
                            ex.printStackTrace();
                        }
                    }
                    break;
                }
            }
            boolean flag = true;
            for (int i = 0; i < link.length ; i++){
                if (link[i] == 0){
                    flag = false;
                    System.err.println("Unknown Command \"" + args[i] + "\". --help Can Help You.");
                    return;
                }
            }
            Request request = new Request(url,method,header,post,follow,fooBody,json,binaryFile,saveToFile,fileToSave,headerToSend,addToList);
        }
    }

    /**
     * get request header as a string and return as a hash map
     * @param s string header
     * @return header
     */
    private static HashMap<String,String> headerProcessor(String s){
        String key = "";
        String value = "";
        HashMap<String,String> header = new HashMap<>();
        ArrayList<String> keys = new ArrayList<>();
        ArrayList<String> values = new ArrayList<>();
        boolean flag = true;
        for (int i = 0; i < s.length() ; i++) {
            if (flag) {
                if (s.charAt(i) == first) {
                    keys.add(key);
                    key = "";
                    flag = false;
                    continue;
                }
                key += s.charAt(i)+"";
            }else {
                if (s.charAt(i) == second ) {
                    values.add(value);
                    value = "";
                    flag = true;
                    continue;
                }
                if (i+1 == s.length()){
                    value += s.charAt(i)+"";
                    values.add(value);
                    break;
                }
                value += s.charAt(i)+"";
            }
        }
        try {
            for (int i = 0; i <keys.size() || i < values.size() ; i++) {
                header.putIfAbsent(keys.get(i),values.get(i));
            }
        }catch (Exception e){
            System.err.println("Wrong " + s1 + " Input");
        }
        return header;
    }
    /**
     * get request body as a string and return as a hash map
     * @param s string body
     * @return header
     */
    private static HashMap<String,String> requestBodyProcessor(String s){
        return headerProcessor(s);
    }

    /**
     * print all defined commands
     */
    private static void printHelp(){
        System.out.println("-M , --method --> set method\n-H , --headers --> send request headers with front side format : \"key:value,...\"\n-i --> show response headers\n" +
                "-f --> automatic follow redirect\n-O , --output --> save response body in a file ( enter file name otherwise it will be saved in a file with " +
                "\"output_[CurrentDate]\" format )");
        System.out.println("-S , --save --> save current request in \"list.txt\" file\n-D , --data --> send form data to server with POST method with front side format : \"key=value&...\"");
        System.out.println("-j , --json --> send json to server with POST method with front side format : {\"key\":\"value\", ... }");
        System.out.println("--upload --> send binary file to server with POST method");
        System.out.println("list --> show saved requests");
        System.out.println("list an array of ints (e.g : list 1 2 4) --> send requests you have entered" );
    }
}
