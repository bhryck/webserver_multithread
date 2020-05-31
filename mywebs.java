import java.io.*;
import java.net.*;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
class mywebs {  //create a websocket and keep checking if it is necessary to make another socket
    public static void main(String argv[]) throws Exception {

        ServerSocket listenSocket = new ServerSocket(8080);
        boolean serverstate = true;
        while (serverstate) {
            try {
                Socket a = listenSocket.accept();
                new threadconnect(a);

            } catch (Exception e) {

            }
        }
    }

}

        class threadconnect extends Thread {//class to create connection socket
            private Socket connectionSocket;
            String gettime() { //retrieve local time when called in format appropriate for HTTP and in GMT
                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat Format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
                Format.setTimeZone(TimeZone.getTimeZone("GMT"));
                return Format.format(calendar.getTime());
            }

            public void WritetoFile(String s){//writes to log.txt the time, ip address, request line and any errors
                try
                {
                    FileWriter writer = new FileWriter("log.txt",true);
                    String timef = gettime();
                    writer.write(timef + s);

                    writer.close();
                }
                catch (IOException a){
                    System.out.println("error in accessing log file");
                }
            }

            public threadconnect(Socket s) {
                connectionSocket = s;
                System.out.println("new socket created");
                start();
            }

            public void run() {
                try {
                    String requestMessageLine;
                    String fileName;

                    //initialize socket and connection

                    BufferedReader inFromClient =
                            new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                    DataOutputStream outToClient =
                            new DataOutputStream(connectionSocket.getOutputStream());
                    requestMessageLine = inFromClient.readLine();

                    String line =inFromClient.readLine();
                    String[] temp;
                    String ifmod = "";
                    boolean isifmod = false;

                    //iterates the request ot find if-modified-since header and parse the time
                    while ((line = inFromClient.readLine()) != null && (line.length() != 0)){
                        temp =line.split(" ");
                        if(temp[0].equals("If-Modified-Since:")){
                            try {
                                String newarray = temp[1] + " " + temp[2] + " " + temp[3] + " " + temp[4] + " " + temp[5] + " " + temp[6];
                                ifmod = newarray;
                                isifmod = true;
                                System.out.println(newarray);
                            }
                            catch (ArrayIndexOutOfBoundsException e){ //if if-modified-since not found , exception caught here
                                isifmod = false;
                            }
                        }
                    }

                    StringTokenizer tokenizedLine = new StringTokenizer(requestMessageLine);

                    try {
                        //request and filename retrieved from first line of headers
                        String request =tokenizedLine.nextToken();
                        fileName = tokenizedLine.nextToken();

                        if (request.equals("GET")) {
                            //parse the request

                            if (fileName.startsWith("/") == true)
                                fileName = fileName.substring(1);
                            File file = new File(fileName);
                            System.out.println(fileName);
                            int numOfBytes = (int) file.length();
                            FileInputStream inFile = new FileInputStream(fileName);
                            byte[] fileInBytes = new byte[numOfBytes];
                            inFile.read(fileInBytes);

                            //formatting of last modified
                                SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
                                sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
                                String lastmod = (sdf.format(file.lastModified()));

                                if(isifmod) { // if there is a if-modified -since
                                    //parse dates so that they are comparable
                                    Date lm = sdf.parse(lastmod);
                                    Date ims = sdf.parse(ifmod);
                                    if(lm.compareTo(ims) >0) {//file is modified after ims

                                        //filename format

                                        outToClient.writeBytes("HTTP/1.1 200 Document Follows\r\n");
                                        if (fileName.endsWith(".jpg"))
                                            outToClient.writeBytes("Content-Type: image/jpeg\r\n");
                                        if (fileName.endsWith(".gif"))
                                            outToClient.writeBytes("Content-Type: image/gif\r\n");
                                        if (fileName.endsWith(".txt"))
                                            outToClient.writeBytes("Content-Type: text/plain\r\n");
                                        if (fileName.endsWith(".html"))
                                            outToClient.writeBytes("Content-Type: text/html\r\n");
                                        outToClient.writeBytes("Content-Length: " + numOfBytes + "\r\n");
                                        outToClient.writeBytes("status: 200 \r\n");

                                        //write last-mod header

                                        sdf = new SimpleDateFormat("EEE, dd MMM YYYY HH:mm:ss z");
                                        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
                                        lastmod = (sdf.format(file.lastModified()));
                                        outToClient.writeBytes("Last-Modified: " + lastmod + "\r\n");
                                        outToClient.writeBytes("\r\n");
                                        System.out.println(fileInBytes);

                                        //send content
                                        outToClient.write(fileInBytes, 0, numOfBytes);

                                        //parse data for logging
                                        String outstring = "";
                                        outstring = outstring + "," + connectionSocket.getRemoteSocketAddress().toString();
                                        outstring = outstring + ", 200, " + requestMessageLine + '\n';
                                        WritetoFile(outstring);

                                        connectionSocket.close();
                                    }
                                    else{// send header only, if-modified-since is after last-modified
                                        outToClient.writeBytes("HTTP/1.1 304 OK\r\n"); //note 304 status code is sent
                                        if (fileName.endsWith(".jpg"))
                                            outToClient.writeBytes("Content-Type: image/jpeg\r\n");
                                        if (fileName.endsWith(".gif"))
                                            outToClient.writeBytes("Content-Type: image/gif\r\n");
                                        if (fileName.endsWith(".txt"))
                                            outToClient.writeBytes("Content-Type: text/plain\r\n");
                                        if (fileName.endsWith(".html"))
                                            outToClient.writeBytes("Content-Type: text/html\r\n");
                                        outToClient.writeBytes("Content-Length: " + numOfBytes + "\r\n");
                                        outToClient.writeBytes("status: 304 \r\n");

                                        //parse date and time in a way thats readeble in http

                                        sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
                                        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
                                        lastmod = (sdf.format(file.lastModified()));
                                        outToClient.writeBytes("Last-Modified: "+ lastmod+"\r\n");


                                        String outstring = "";
                                        outstring=outstring+","+ connectionSocket.getRemoteSocketAddress().toString();
                                        outstring = outstring + ", 304, " +requestMessageLine+'\n';
                                        WritetoFile(outstring);

                                        connectionSocket.close();

                                    }
                                }

                            else{//no if-modified-since, similar to code in line 115

                                outToClient.writeBytes("HTTP/1.1 200 Document Follows\r\n");
                                if (fileName.endsWith(".jpg"))
                                    outToClient.writeBytes("Content-Type: image/jpeg\r\n");
                                if (fileName.endsWith(".gif"))
                                    outToClient.writeBytes("Content-Type: image/gif\r\n");
                                if (fileName.endsWith(".txt"))
                                    outToClient.writeBytes("Content-Type: text/plain\r\n");
                                if (fileName.endsWith(".html"))
                                    outToClient.writeBytes("Content-Type: text/html\r\n");
                                outToClient.writeBytes("Content-Length: " + numOfBytes + "\r\n");
                                    outToClient.writeBytes("status: 200 \r\n");

                                sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
                                sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
                                lastmod = (sdf.format(file.lastModified()));
                                outToClient.writeBytes("Last-Modified: " + lastmod + "\r\n");

                                outToClient.writeBytes("\r\n");
                                outToClient.write(fileInBytes, 0, numOfBytes);

                                String outstring = "";
                                outstring = outstring + "," + connectionSocket.getRemoteSocketAddress().toString();
                                outstring = outstring + ", 200, " + requestMessageLine + '\n';
                                WritetoFile(outstring);

                                connectionSocket.close();
                            }

                        }
                        else if((request.equals("HEAD"))){


                            if (fileName.startsWith("/") == true)
                                fileName = fileName.substring(1);
                            File file = new File(fileName);
                            System.out.println(fileName);
                            int numOfBytes = (int) file.length();
                            FileInputStream inFile = new FileInputStream(fileName);
                            byte[] fileInBytes = new byte[numOfBytes];
                            inFile.read(fileInBytes);

                            SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
                            sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
                            String lastmod = (sdf.format(file.lastModified()));
                            Date lm = sdf.parse(lastmod);
                            Date ims = sdf.parse(ifmod);


                            if(isifmod && ims.compareTo(lm) >0) {//if last modified is bfore if-since-modified, send 304 instead of 200
                                outToClient.writeBytes("HTTP/1.1 304\r\n");
                                outToClient.writeBytes("status: 304 \r\n");

                            }
                            else outToClient.writeBytes("HTTP/1.1 200 OK\r\n");
                            outToClient.writeBytes("status: 200 \r\n");

                            if (fileName.endsWith(".jpg"))
                                outToClient.writeBytes("Content-Type: image/jpeg\r\n");
                            if (fileName.endsWith(".gif"))
                                outToClient.writeBytes("Content-Type: image/gif\r\n");
                            if (fileName.endsWith(".txt"))
                                outToClient.writeBytes("Content-Type: text/plain\r\n");
                            if (fileName.endsWith(".html"))
                                outToClient.writeBytes("Content-Type: text/html\r\n");
                            outToClient.writeBytes("Content-Length: " + numOfBytes + "\r\n");

                            sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
                            sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
                            lastmod = (sdf.format(file.lastModified()));
                            outToClient.writeBytes("Last-Modified: "+ lastmod+"\r\n");

                            outToClient.writeBytes("\r\n");
                            outToClient.write(fileInBytes, 0, numOfBytes);

                            String outstring = "";
                            outstring=outstring+","+ connectionSocket.getRemoteSocketAddress().toString();



                            if(isifmod && ims.compareTo(lm) >0) { //for logging
                                outstring = outstring + ", 304, " +requestMessageLine+'\n';
                            }
                            else {
                                outstring = outstring + ", 200, " + requestMessageLine + '\n';
                            }

                            WritetoFile(outstring);
                            connectionSocket.close();
                        }
                        else{//bad request
                            outToClient.writeBytes("HTTP/1.1 400\r\n");
                            outToClient.writeBytes("\r\n");
                            outToClient.writeBytes("status: 400 \r\n");
                            outToClient.writeBytes("\r\n <html><head></head><body>bad request error: 400</body></html>\r\n");

                            String outstring = "";
                            outstring=outstring+","+ connectionSocket.getRemoteSocketAddress().toString();
                            outstring = outstring + ", 400, " +requestMessageLine+'\n';
                            WritetoFile(outstring);

                            connectionSocket.close();

                        }

                    } catch (FileNotFoundException e) { //404 error catch

                        System.out.println("file not found");
                        outToClient.writeBytes("HTTP/1.1 404 Not Found\r\n");
                        outToClient.writeBytes("status: 404 \r\n");
                        outToClient.writeBytes("\r\n <html><head></head><body>file not found error: 404</body></html>\r\n");


                        connectionSocket.close();

                        String outstring = "";
                        outstring=outstring+",client IP:"+ connectionSocket.getRemoteSocketAddress().toString();
                        outstring = outstring + ", 404, " +requestMessageLine+'\n';
                        WritetoFile(outstring);
                    }

                } catch (Exception e) {//something very wrong, should not happen in normal circumstance
                    System.out.println("Catastrophic error");
                    System.out.println(e);
                    e.printStackTrace();

                }

            }
        }

