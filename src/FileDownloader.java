import javax.swing.*;
import java.io.*;
import java.net.*;
import java.nio.Buffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Paths;

public class FileDownloader {

    public static void main(String[] args) throws Exception {
        String index_file = null;
        String lower_endpoint = null;
        String upper_endpoint = null;


        if(args.length == 1) {
            index_file = args[0];
            System.out.print("URL of the index file:");
            System.out.println(index_file);
            System.out.println("No range is given");
            System.out.println("Index file is downloaded");
        }
        else if(args.length == 2) {
            index_file = args[0];
            String range = args[1];
            int separation_index = range.indexOf("-");
            lower_endpoint = range.substring(0,separation_index);
            upper_endpoint = range.substring(separation_index + 1, range.length());
            System.out.print("URL of the index file:");
            System.out.println(index_file);
            System.out.print("Lower endpoint = ");
            System.out.println(lower_endpoint);
            System.out.print("Upper endpoint = ");
            System.out.println(upper_endpoint);
            System.out.println("Index file is downloaded");
        }
        else {
            System.out.println("Argument error!");
        }

        String host = getHost(index_file);
        String directory = getDirectory(index_file);

        try {
            Socket socket = new Socket(host, 80);

            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            writer.print("GET " + directory + " HTTP/1.1\r\n");
            writer.print("Host: " + host +"\r\n");
            writer.print("User-Agent: Simple Http Client\r\n");
            writer.print("Accept: text/html\r\n");
            writer.print("Accept-Language: en-US\r\n");
            writer.print("Connection: close\r\n");
            writer.print("\r\n");
            writer.flush();

            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String line;
            String response = "";
            String txt_file_arr[] = new String[8192];
            int txt_file_arr_index = 0;
            while ((line = br.readLine()) != null) {
                response = response + line + "\n";
                if (line.startsWith("www")) {
                    txt_file_arr[txt_file_arr_index] = line;
                    txt_file_arr_index++;

                }
            }
            System.out.print("There are ");
            System.out.print(txt_file_arr_index);
            System.out.println(" files in the index");



            for(int i = 0; i < txt_file_arr_index; i++) {
                System.out.print(i+1);
                System.out.print(". ");
                System.out.print(txt_file_arr[i]);
                if(isFileFound(txt_file_arr[i]) == false) {
                    System.out.println(" is not found");
                }
                else {
                    System.out.print(" ");
                    getTextContent(txt_file_arr[i], lower_endpoint, upper_endpoint);
                }

            }


            if(!response.substring(9,12).equals("200")) {
                System.out.println("Index file is not found. The message is other than 200 OK!");
                System.out.println("System exits...");
                System.exit(0);
            }

            //BURASI
            //System.out.println(getTextBodyContent(txt_file_arr[3]));

            //FILE
            String pathname = (Paths.get("").toAbsolutePath()).toString() + "\\" + args[0].substring(args[0].lastIndexOf("/") + 1);
            File f = new File(pathname);
            f.createNewFile();

            String yourURLStr = "http://"+args[0];
            java.net.URL myURL = new java.net.URL (yourURLStr);
            InputStream inStream = myURL.openStream();
            try (BufferedInputStream in = new BufferedInputStream(inStream);
                 FileOutputStream fileOutputStream = new FileOutputStream(f)) {
                byte dataBuffer[] = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                    fileOutputStream.write(dataBuffer, 0, bytesRead);
                }
                fileOutputStream.flush();
                fileOutputStream.close();
                inStream.close();
                in.close();

            } catch (IOException e) {
                // handle exception
            }
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
    public static String getHost(String url){
        String host = "";
        int first_slash_index = url.indexOf('/');
        host = url.substring(0, first_slash_index);
        return host;
    }

    public static String getDirectory(String url){
        String directory = "";
        int first_slash_index = url.indexOf('/');
        directory = url.substring(first_slash_index, url.length());
        return directory;
    }

    public static void getTextContent(String url, String lowerBound, String upperBound) throws IOException {
        String host = getHost(url);
        String dir = getDirectory(url);

        Socket s = new Socket(host, 80);

        PrintWriter pw = new PrintWriter(s.getOutputStream(), true);
        pw.print("GET " + dir + " HTTP/1.1\r\n");
        pw.print("Host: " + host +"\r\n");
        pw.print("User-Agent: Simple Http Client\r\n");
        pw.print("Accept: text/html\r\n");
        pw.print("Accept-Language: en-US\r\n");
        pw.print("Connection: close\r\n");
        pw.print("\r\n");
        pw.flush();

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(s.getInputStream()));
        String line;
        String response_head = "";
        String response_body = "";
        int responseBodyLineCount = 0;
        boolean afterContentType = false, firstLineDeleted = true;
        while ((line = bufferedReader.readLine()) != null) {
            if (afterContentType) {
                if(!firstLineDeleted) {
                    response_body = response_body + line + "\n";
                    responseBodyLineCount++;
                }
                else{
                    firstLineDeleted = false;
                }
            }
            else{
                response_head += line+ "\n";

                if(line.startsWith("Content-Type")) {
                    afterContentType = true;
                }
            }
        }
        if(!response_head.substring(9,12).equals("200")) {
            System.out.println("Index file is not found. The message is other than 200 OK!");
            System.out.println("System exits...");
            //System.exit(0);
        }

        downloadFile(url, response_head, response_body, lowerBound, upperBound, responseBodyLineCount);
    }

    public static void downloadFile(String url, String response_head, String response_body,
                                    String lower_Bound, String upper_Bound, int lines) throws IOException {

        String sizeStr = response_head.substring(
                (response_head.indexOf("Content-Length") + 16), ((response_head.indexOf("Connection") - 1)));
        //System.out.println(sizeStr);
        int size = Integer.valueOf(sizeStr);
        //System.out.println("sizze: "+size);

        // Check whether the size is higher than lower bound
        // Bytes to be read determined by upper and lower bound
        int startIndex = 0;
        if(lower_Bound != null)
            startIndex = Integer.parseInt(lower_Bound);
        int endIndex = size;
        if(upper_Bound != null)
            endIndex = Integer.parseInt(upper_Bound);
        //System.out.println("size = " + size);
        if(size < startIndex){
            System.out.println("(size = " + size + ") is not downloaded");
            return;
        }

        int availableMax = endIndex;
        if(size < endIndex){
            availableMax = size;
        }

        // Get the target path --> directory of the main class
        String pathname = (Paths.get("").toAbsolutePath()).toString() + "\\" + url.substring(url.lastIndexOf("/") + 1);
        // Create a file named the same at the given directory
        File f = new File(pathname);
        f.createNewFile();

        // If it exceeds the lower bound, then download-
        // Download
        try{
            FileWriter fw = new FileWriter(pathname);
            fw.write(response_body.substring(startIndex, (availableMax - lines)));
            fw.close();
        }catch(Exception e){
            System.out.println(e);
        }

        // Notification
        if(upper_Bound == null) {
            System.out.println("(size = " + size + ") is downloaded");
        }
        else{
            int maximum = size;

            if(Integer.parseInt(upper_Bound) < size){
                maximum = Integer.parseInt(upper_Bound);
            }

            System.out.println("(range = " + startIndex + "-" + maximum + ") is downloaded");
        }

    }

    public static boolean isFileFound(String url) throws IOException {
        String host = getHost(url);
        String dir = getDirectory(url);

        Socket s = new Socket(host, 80);

        PrintWriter pw = new PrintWriter(s.getOutputStream(), true);
        pw.print("HEAD " + dir + " HTTP/1.1\r\n");
        pw.print("Host: " + host +"\r\n");
        pw.print("User-Agent: Simple Http Client\r\n");
        pw.print("Accept: text/html\r\n");
        pw.print("Accept-Language: en-US\r\n");
        pw.print("Connection: close\r\n");
        pw.print("\r\n");
        pw.flush();

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(s.getInputStream()));
        String line;
        String response = "";
        while ((line = bufferedReader.readLine()) != null) {
            response = response + line + "\n";
        }
        if(!response.substring(9,12).equals("200")) {
            //HEAD reponse is other than 200 OK.
            return false;
        }
        return true;
    }
}