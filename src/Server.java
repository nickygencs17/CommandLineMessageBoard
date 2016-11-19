import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class Server implements Runnable {
    Socket csocket;
    String msg;
    ArrayList <String> cmdList ;




    Server(Socket csocket, String message, ArrayList<String> ls) {
        this.csocket = csocket;
        this.msg = message;
        this.cmdList = ls;
    }

    public static void main(String args[]) throws Exception {

        ServerSocket ssock = new ServerSocket(1234);
        System.out.println("Listening");
        while (true) {
            ArrayList<String> commmandList = new ArrayList<>();
            Socket sock = ssock.accept();
            InputStream is = sock.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String message = br.readLine();
            System.out.println("Message received from client is "+ message);
            String newMessage= message.concat(", World!").toUpperCase();

            StringTokenizer tok = new StringTokenizer(message);
            while (tok.hasMoreTokens()) {

                commmandList.add(tok.nextToken());
            }
            System.out.println(newMessage);

            System.out.println("Connected");

            new Thread(new Server(sock,newMessage, commmandList)).start();
        }
    }

    public void run() {
        try {

            PrintStream pstream = new PrintStream (csocket.getOutputStream());
            pstream.println(msg );
            pstream.close();
            csocket.close();
        }
        catch (IOException e) {
            System.out.println(e);
        }
    }
}