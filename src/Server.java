import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

import java.util.ArrayList;
import java.util.StringTokenizer;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class Server implements Runnable{
    Socket csocket;
    String msg;
    ArrayList <String> cmdList ;
    boolean loggedIn = false;
    ArrayList <Room> rooms;



    Server(Socket csocket, String message, ArrayList<String> ls) {
        this.csocket = csocket;
        this.msg = message;
        this.cmdList = ls;
    }

    public void setMsg(String msg) {
        this.msg = msg;
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
        else if(commandList.get(0).equals("ag")){
            int n = returnN(commandList);
            if (n == 0){
                System.out.println("Invalid Number of Arguments");
            }
            else{
                try{
                    agCommand(n);
                }
                catch(Exception j){
                    System.out.print(j);
                }
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
        else {
            if (loggedIn == false) {

                System.out.println("Please Log In");
            } else {

                System.out.println("Invalid ");
            }
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
        if(commandList.size()==2){
            String n = commandList.get(3).toString();
            int number = Integer.parseInt(n);
            return number;
        }
        else if(commandList.size()==1){
            return 5;
        }
        else{
           return 0;
        }

    }
    public void agCommand(int n) throws IOException, ParseException{
//        System.out.println("Working Directory = " +
//                System.getProperty("user.dir"));
        StringBuilder sb = new StringBuilder();
        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(new FileReader("JSONdata/ag.json"));

            JSONObject jsonObject = (JSONObject) obj;
            JSONArray arr = (JSONArray) jsonObject.get("ag");
            ArrayList<Room> rooms = new ArrayList<>();
            for (int i = 0; i < arr.size(); i++)
            {
                JSONObject j = (JSONObject)arr.get(i);
                String rf = (String) j.get("roomFile");
                int index = Integer.parseInt((String)j.get("index"));
                String rn = (String) j.get("roomName");
                Room r = new Room(rn,rf,index);
                rooms.add(r);
            }


            for( int i =0; i<n; i++) {
                if(i==0){
                    sb.append("\n");
                }

                sb.append( rooms.get(i).getIndex()+1+".  ( )  "+rooms.get(i).getRoomName()+"\n");
            }

        }
        catch (Exception v){
            System.out.println(v);
        }

        setMsg(sb.toString());



    }
 

    public void run() {
        try {

            PrintStream pstream = new PrintStream (csocket.getOutputStream());
            parseArgs(this.cmdList);
            pstream.println(msg);
            pstream.close();
            csocket.close();
        }
        catch (IOException e) {
            System.out.println(e);
        }
    }


}
