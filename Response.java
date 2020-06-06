package jurl;

import java.io.*;
import java.net.HttpURLConnection;
import java.util.ArrayList;

public class Response {
    /**
     * show header
     * @param connection to connect
     * @return header message as a string
     */
    public String showResponseHeader(HttpURLConnection connection){
        String header = "" ;
        for(String key : connection.getHeaderFields().keySet()){
            String value = connection.getHeaderField(key);
            try {
                if (key.contains("null")) {
                    key = "";
                }
                key += " = ";
            }catch (NullPointerException e){
                key = "";
            }
            header += String.format("%s%s\n", key,value);
        }
        return header ;
    }

    /**
     * @return response code
     * @throws IOException for unexpected exceptions
     */
    public int showResponseCode(HttpURLConnection connection) throws IOException {
        return connection.getResponseCode();
    }

    /**
     * @return response message
     * @throws IOException for unexpected exceptions
     */
    public String showResponseMessage(HttpURLConnection connection) throws IOException {
        return connection.getResponseMessage();
    }

    /**
     * get server response body and return as a string and save in file if wanted
     * @param responseBody server response
     * @param saveToFile save to file if true
     * @param fileToSave
     * @return response body
     */
    public String showResponseBody(BufferedInputStream responseBody , boolean saveToFile , File fileToSave) {
        String body = "";
        FileOutputStream writeInFile = null;
        try{
            if (saveToFile) {
                writeInFile = new FileOutputStream(fileToSave);
                int count;
                while ((count = responseBody.read()) != -1) {
                    body += (char) count;
                    writeInFile.write((char)count);
                }
                System.out.println("response body has been saved in " + fileToSave.getName());
            }
            else {
                while (responseBody.available() > 0) {
                    body += (char) responseBody.read();
                }
            }
        } catch (FileNotFoundException e){
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        } finally {
            try {
                if (responseBody != null)
                    responseBody.close();
            } catch (IOException e) {
                e.printStackTrace(System.err);
            }
        }
        return body;
    }

    /**
     * get response size
     * @return message size as an String with B or kB
     */
    String getSize(HttpURLConnection connection){
        int length = connection.getContentLength();
        String size = "";
        if (length < 1000){
            size = length + " B";
        }
        else {
            size = String.format("%.1f KB",(float) connection.getContentLength()/1000);
        }
        return size ;
    }
}
