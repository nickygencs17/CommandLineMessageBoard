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
    boolean loggedIn = false;




    Server(Socket csocket, String message, ArrayList<String> ls) {
        this.csocket = csocket;
        this.msg = message;
        this.cmdList = ls;
    }

    public void parseArgs(ArrayList commandList){
        if(commandList.get(0).equals("login") && loggedIn == false){

            if(commandList.size()==2){
                loggedIn = true;
                String userName = commandList.get(1).toString();
                System.out.println(userName);
            }
            else{
                System.out.println("Invalid Number of Arguments");
            }

        }
        else if(commandList.get(0).equals("help")){
            if(commandList.size()==1){
                String help = commandList.get(1).toString();
                System.out.println(help);
            }
            else{
                System.out.println("Invalid Number of Arguments");
            }

        }
        else if(commandList.get(0).equals("ag") && loggedIn == true){
            int n = returnN(commandList);
            if (n == 0){
                System.out.println("Invalid Number of Arguments");
            }
            else{
                //run ag command
            }

        }
        else if(commandList.get(0).equals("sg") && loggedIn == true){
            int n = returnN(commandList);
            if (n == 0){
                System.out.println("Invalid Number of Arguments");
            }
            else{
                //run sg command
            }

        }
        else if(commandList.get(0).equals("rg") && loggedIn == true){
            int n = returnN(commandList);
            if (n == 0){
                System.out.println("Invalid Number of Arguments");
            }
            else{
                //run rg command
            }

        }
        else if(commandList.get(0).equals("logout") && loggedIn == true){

            if(commandList.size()==2){
                loggedIn = false;
                String userName = commandList.get(2).toString();
                System.out.println(userName);
            }
            else{
                System.out.println("Invalid Number of Arguments");
            }
        }
        else{
            System.out.println("Invalid Argument");
        }


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


            StringTokenizer tok = new StringTokenizer(message);
            while (tok.hasMoreTokens()) {

                commmandList.add(tok.nextToken());
            }


            System.out.println("Connected");
            //parseArgs(commmandList);
            new Thread(new Server(sock,message, commmandList)).start();
        }
    }


    public int returnN(ArrayList commandList){
        if(commandList.size()==3){
            String n = commandList.get(3).toString();
            int number = Integer.parseInt(n);
            return number;
        }
        else if(commandList.size()==2){
            return 5;
        }
        else{
           return 0;
        }

    }

    public void run() {
        try {

            PrintStream pstream = new PrintStream (csocket.getOutputStream());
            parseArgs(this.cmdList);
            pstream.println(msg );
            pstream.close();
            csocket.close();
        }
        catch (IOException e) {
            System.out.println(e);
        }
    }

}
