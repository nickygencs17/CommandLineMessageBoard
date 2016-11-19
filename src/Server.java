import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server implements Runnable {
    Socket csocket;
    String msg;




    Server(Socket csocket, String message) {
        this.csocket = csocket;
        this.msg = message;
    }

    public static void main(String args[])
            throws Exception {

        ServerSocket ssock = new ServerSocket(1234);
        System.out.println("Listening");
        while (true) {
            Socket sock = ssock.accept();
            InputStream is = sock.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String message = br.readLine();
            System.out.println("Message received from client is "+ message);
            String newMessage= message.concat(", World!").toUpperCase();

            System.out.println(newMessage);

            System.out.println("Connected");
            new Thread(new Server(sock,newMessage)).start();
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