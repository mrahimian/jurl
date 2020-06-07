package jurl;


import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.*;


public class Request implements Serializable{
    URL url ;
    String method ;
    boolean header ;
    int post ;
    boolean follow ;
    HashMap<String, String> fooBody;
    String json ;
    File binaryFile ;
    boolean saveToFile ;
    File fileToSave ;
    HashMap<String, String> headerToSend;
    boolean addToList;
    String responseBody ;

    /**
     * call makeConnection method
     * @param url to make connection with
     * @param method GET-POST-PUT-DELETE
     * @param header show header or not
     * @param post 1 if body form-data , 2 if json , 3 if binary , 4 for auto follow redirect
     * @param fooBody form-data body
     * @param json body
     * @param binaryFile
     * @param saveToFile true to save in file
     * @param fileToSave file in which request will be save
     * @param headerToSend request headers
     * @param addToList save request information in "list.txt" file
     */
    public Request(URL url , String method , boolean header , int post , boolean follow , HashMap<String, String> fooBody
            , String json , File binaryFile , boolean saveToFile , File fileToSave , HashMap<String, String> headerToSend, boolean addToList){
        this.url = url;this.method = method;this.header = header;this.post = post;this.follow = follow;this.fooBody = fooBody;
        this.json = json;this.binaryFile = binaryFile;this.saveToFile = saveToFile;this.fileToSave = fileToSave;this.headerToSend = headerToSend;
        this.addToList = addToList;
        makeConnection(url, method, header, post, follow , fooBody, json, binaryFile, saveToFile, fileToSave, headerToSend);
    }

    /**
     * show saved requests
     */
    public Request(){
        try(FileInputStream is = new FileInputStream("list.txt");
            ObjectInputStream ois = new ObjectInputStream(is)){
            ArrayList<Request> list = (ArrayList) ois.readObject();
            for (Request request : list){
                System.out.println(request);
            }
        } catch (EOFException e){
            System.out.println("Nothing has been saved yet");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * send entered requests
     * @param reqs requests which want to send
     */
    public Request(int... reqs){
         try(FileInputStream is = new FileInputStream("list.txt");
             ObjectInputStream ois = new ObjectInputStream(is)) {
             ArrayList<Request> list = (ArrayList) ois.readObject();
             for(int i : reqs){
                 System.out.println(list.get(i));
                 new Request(list.get(i).url,list.get(i).method,list.get(i).header,list.get(i).post,list.get(i).follow,list.get(i).fooBody,list.get(i).json
                 ,list.get(i).binaryFile,false,null,list.get(i).headerToSend,false);
             }
         } catch (EOFException e){
             System.err.println("Nothing has been saved yet");
         }catch (IndexOutOfBoundsException e){
             System.err.println("Invalid Index");
         }
         catch (ClassNotFoundException | IOException e) {
             e.printStackTrace();
         }
    }

    /**
     * make connection with server and call method
     */
    public void makeConnection(URL url , String method , boolean header , int post, boolean follow, HashMap<String, String> fooBody
    , String json , File binaryFile , boolean saveToFile , File fileToSave , HashMap<String, String> headerToSend ){
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            switch (method){
                case "GET":
                    getMethod(connection,header,saveToFile,fileToSave,headerToSend);
                    break;
                case "DELETE":
                    deleteMethod(connection,header,saveToFile,fileToSave,headerToSend);
                    break;
                case "POST":
                case "PUT":
                    switch (post){
                        case 1:
                            formData(connection,method,header,fooBody,saveToFile,fileToSave,headerToSend);
                            break;
                        case 2 :
                            jsonPost(connection,method,header,json,saveToFile,fileToSave,headerToSend);
                            break;
                        case 3 :
                            uploadBinary(connection,method,header,binaryFile,saveToFile,fileToSave,headerToSend);
                            break;
                    }
                    break;
            }
            if (follow){
                followRedirect(connection,header,saveToFile,fileToSave,headerToSend);
            }
            if (this.addToList){
                addToList();
            }
        } catch (UnknownHostException | NullPointerException e ){

        } catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * print response body
     * @param connection with server
     */
    public void getConnectionStream(HttpURLConnection connection , boolean saveToFile , File fileToSave ){
        InputStream inputStream;
        try {
            if (connection.getResponseCode() / 100 == 4 || connection.getResponseCode() / 100 == 5) {
                inputStream = connection.getErrorStream();
            } else {
                inputStream = connection.getInputStream();
            }
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
            Response response = new Response();
            responseBody = response.showResponseBody(bufferedInputStream , saveToFile , fileToSave) ;
            System.out.println(responseBody);
        } catch (UnknownHostException e){
            System.err.println("Invalid URL or No Connection. Please Check Your Connection And Try Again.");
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * do GET method actions
     */
    public void getMethod(HttpURLConnection connection , boolean header, boolean saveToFile,File fileToSave, HashMap<String, String> headerToSend){
        try {
            connection.setRequestMethod("GET");
            sendHeaders(headerToSend,connection);
            showData(connection,header,saveToFile,fileToSave);
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    /**
     * do DELETE method actions
     */
    public void deleteMethod(HttpURLConnection connection , boolean header , boolean saveToFile , File fileToSave , HashMap<String, String> headerToSend){
        try {
            connection.setRequestMethod("DELETE");
            sendHeaders(headerToSend,connection);
            showData(connection,header,saveToFile,fileToSave);
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    /**
     * do PUT method actions
     */
    public void putMethod(HttpURLConnection connection , boolean header, boolean saveToFile,File fileToSave, HashMap<String, String> headerToSend){
        try {
            connection.setRequestMethod("PUT");
            sendHeaders(headerToSend,connection);
            showData(connection,header,saveToFile,fileToSave);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * send form-data body with POST method
     */
    public void formData(HttpURLConnection connection , String method , boolean header , HashMap<String, String> fooBody , boolean saveToFile,File fileToSave, HashMap<String, String> headerToSend){
        try {
            String boundary = System.currentTimeMillis() + "";
            connection.setRequestMethod(method);
            sendHeaders(headerToSend,connection);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            connection.setRequestProperty("Content-Language", "en-US");
            BufferedOutputStream request = new BufferedOutputStream(connection.getOutputStream());
            bufferOutFormData(fooBody, boundary, request);
            showData(connection,header,saveToFile,fileToSave);
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    /**
     * handle form-data body
     * @param body to send
     * @param boundary to format the message
     * @param bufferedOutputStream connection send body to server
     * @throws IOException for unexpected exception
     */
    public void bufferOutFormData(HashMap<String, String> body, String boundary, BufferedOutputStream bufferedOutputStream) throws IOException {
        for (String key : body.keySet()) {
            bufferedOutputStream.write(("--" + boundary + "\r\n").getBytes());
            if (key.contains("file")) {
                bufferedOutputStream.write(("Content-Disposition: form-data; filename=\"" + (new File(body.get(key))).getName() + "\"\r\nContent-Type: Auto\r\n\r\n").getBytes());
                try {
                    BufferedInputStream tempBufferedInputStream = new BufferedInputStream(new FileInputStream(new File(body.get(key))));
                    int count;
                    byte[] buffer = new byte[4096];
                    while (tempBufferedInputStream.available() > 0) {
                        count = tempBufferedInputStream.read(buffer);
                        bufferedOutputStream.write(buffer, 0, count);
                    }
                    bufferedOutputStream.write("\r\n".getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                bufferedOutputStream.write(("Content-Disposition: form-data; name=\"" + key + "\"\r\n\r\n").getBytes());
                bufferedOutputStream.write((body.get(key) + "\r\n").getBytes());
            }
        }
        bufferedOutputStream.write(("--" + boundary + "--\r\n").getBytes());
        bufferedOutputStream.flush();
        bufferedOutputStream.close();
    }
    /**
     * send json body with POST method
     */
    public void jsonPost(HttpURLConnection connection , String method , boolean header , String json, boolean saveToFile,File fileToSave, HashMap<String, String> headerToSend) throws IOException{
        BufferedOutputStream bufferedOutputStream = null;
        try {
            connection.setRequestMethod(method);
            sendHeaders(headerToSend,connection);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);
            bufferedOutputStream = new BufferedOutputStream(connection.getOutputStream());
            bufferedOutputStream.write(json.getBytes());
            System.out.println(json);
            showData(connection,header,saveToFile,fileToSave);
        }catch (IOException e){
            e.printStackTrace();
        }
        bufferedOutputStream.flush();
        bufferedOutputStream.close();
    }
    /**
     * send binary file with POST method
     */
    public void uploadBinary(HttpURLConnection connection , String method , boolean header , File binary , boolean saveToFile,File fileToSave, HashMap<String, String> headerToSend) throws IOException{
        BufferedOutputStream bufferedOutputStream = null;
        try{
            connection.setRequestMethod(method);
            sendHeaders(headerToSend,connection);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/octet-stream");
            bufferedOutputStream = new BufferedOutputStream(connection.getOutputStream());
            BufferedInputStream fileInputStream = new BufferedInputStream(new FileInputStream(binary));
            int count;
            byte[] buffer = new byte[4096];
            while (fileInputStream.available() > 0) {
                count = fileInputStream.read(buffer);
                bufferedOutputStream.write(buffer, 0, count);
            }
            showData(connection,header,saveToFile,fileToSave);
        }catch (IOException e){
            System.err.println("file doesn't exists");
        }
        bufferedOutputStream.flush();
        bufferedOutputStream.close();
    }
    /**
     * follow redirect
     */
    public void followRedirect(HttpURLConnection connection , boolean header , boolean saveToFile,File fileToSave, HashMap<String, String> headerToSend ){
        try {
            URL url = new URL(connection.getHeaderFields().get("Location").get(0));
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            getMethod(con,header,saveToFile,fileToSave,headerToSend);
        }catch (IOException e){
            e.printStackTrace();
        }catch (NullPointerException e){
            return;
        }
    }
    /**
     * show response info
     */
    public void showData(HttpURLConnection connection , boolean header , boolean saveToFile , File fileToSave) {
        try {
            Response response = new Response();
            getConnectionStream(connection, saveToFile, fileToSave);
            if (header) {
                System.out.println(response.showResponseHeader(connection));
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public void sendHeaders(HashMap<String,String> headerToSend , HttpURLConnection connection){
        try {
            for (String key : headerToSend.keySet()) {
                connection.setRequestProperty(key, headerToSend.get(key));
            }
        }catch (NullPointerException e){

        }
    }

    /**
     * add specified request to list
     */
    public void addToList(){
        FileOutputStream fos = null;
        ObjectOutputStream oos = null  ;
        try(FileInputStream is = new FileInputStream("list.txt");
            ObjectInputStream ois = new ObjectInputStream(is)) {
            ArrayList<Request> requests = (ArrayList<Request>) ois.readObject();
            requests.add(this);
            fos = new FileOutputStream("list.txt");
            oos = new ObjectOutputStream(fos);
            oos.writeObject(requests);
        } catch (EOFException | StreamCorruptedException e){
            try{
                fos = new FileOutputStream("list.txt");
                oos = new ObjectOutputStream(fos);
                ArrayList<Request> requests = new ArrayList<>();
                requests.add(this);
                oos.writeObject(requests);
            }catch (IOException e1){
                e1.printStackTrace();
            }
        } catch (ClassNotFoundException | IOException e){
            e.printStackTrace();
        } finally {
            try {
                if (oos!=null){
                    oos.close();
                }
                if (fos!=null){
                    fos.close();
                }
            }catch (IOException e){
                e.printStackTrace();
            }

        }
    }

    @Override
    public String toString() {
        return String.format("URL : %s | Method : %s | Headers : %s | Show Response Header : %s | Follow Redirect : %s " +
                "| Save Response Body In File : %s %s %s %s\n",url,method,headerToSend == null ? "Nothing To Send" : headerToSend
                ,header==true ? "YES" : "NO"
                ,post == 4 ? "YES" : "NO",saveToFile==true ? String.format("Yes File Name : %s",fileToSave.getName()) : "NO"
                ,fooBody == null ? "" : String.format("| Response Body = %s",fooBody)
                ,json == null ? "" : String.format("| Json Post = %s",json)
                ,binaryFile == null ? "" : String.format("| Binary File = %s",binaryFile));
    }
}
