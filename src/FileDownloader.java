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

        System.out.print("Directory: ");
        System.out.println(directory);
        System.out.print("Host: ");
        System.out.println(host);

        try {
            Socket socket = new Socket(host, 80);
            System.out.println(InetAddress.getByName(host));

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
            if(!response.substring(9,12).equals("200")) {
                System.out.println("Index file is not found. The message is other than 200 OK!");
                System.out.println("System exits...");
                System.exit(0);
            }

            //WRITE CONSOLE TO SHOW-------------------------------------------------------------
            System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++");
            System.out.println("RESPONSE OF INDEX FILE: ");
            System.out.println(response);
            System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++");

            System.out.println("TXT_FILE_ARR: ");
            for(int i = 0; i < txt_file_arr_index; i++) {
                System.out.print(i);
                System.out.print(". index: ");
                System.out.println(txt_file_arr[i]);
            }
            System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++");
            //WRITE CONSOLE TO SHOW-------------------------------------------------------------


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


            /*

            ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream());

            FileOutputStream fileOutputStream = new FileOutputStream(FILE_NAME);
            FileChannel fileChannel = fileOutputStream.getChannel();

            fileOutputStream.getChannel()
                    .transferFrom(readableByteChannel, 0, Long.MAX_VALUE);



             */
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

}