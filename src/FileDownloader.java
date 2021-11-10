import javax.swing.*;
import java.io.*;
import java.net.*;
import java.nio.Buffer;

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

        String host = "";
        String directory = "";
        int first_slash_index = index_file.indexOf('/');
        host = index_file.substring(0, first_slash_index);
        directory = index_file.substring(first_slash_index, index_file.length());
        System.out.print("Directory: ");
        System.out.println(directory);

        try {
            Socket socket = new Socket(host, 80);
            System.out.println(InetAddress.getByName(host));
            System.out.println(host);

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
            while ((line = br.readLine()) != null) {
                response = response + line + "\n";
            }
            System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++");
            System.out.println(response);
            System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++");

            if(!response.substring(9,12).equals("200")) {
                System.out.println("Index file is not found. The message is other than 200 OK!");
                System.out.println("System exits...");
                System.exit(0);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }



    }

}