import java.io.*;
import java.net.*;
public class FileDownloader {

    public static void main(String[] args) {
        String index_file = null;
        String lower_endpoint = null;
        String upper_endpoint = null;

        System.out.print("URL of the index file:");

        if(args.length == 1) {
            index_file = args[0];
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










    }




}