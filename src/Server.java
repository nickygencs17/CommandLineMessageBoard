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
    ArrayList <User> users;



    Server(Socket csocket, String message, ArrayList<String> ls) {
        this.csocket = csocket;
        this.msg = message;
        this.cmdList = ls;
    }

    public ArrayList<Room> getRooms() {
        return rooms;
    }

    public void setRooms(ArrayList<Room> rooms) {
        this.rooms = rooms;
    }

    public void setUsers(ArrayList<User> users) {
        this.users = users;
    }

    public ArrayList<User> getUsers() {

        return users;
    }

    public void init(){

        try {
            initUsers();
        } catch (FileNotFoundException e){
            System.out.print(e);
        }

        try{
            initRooms();
        }
        catch (FileNotFoundException e){
            System.out.print(e);
        }

    }
    public void initUsers() throws FileNotFoundException {
        StringBuilder sb = new StringBuilder();
        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(new FileReader("JSONdata/users.json"));

            JSONObject jsonObject = (JSONObject) obj;
            JSONArray arr = (JSONArray) jsonObject.get("existingUsers");
            ArrayList<User> Users= new ArrayList<>();
            for (int i = 0; i < arr.size(); i++)
            {
                JSONObject j = (JSONObject)arr.get(i);
                String un = (String) j.get("userName");
                String fn = (String) j.get("userFile");
                Object newobj = parser.parse(new FileReader(fn));

                JSONObject json = (JSONObject) newobj;
                JSONArray array = (JSONArray) json.get("subscriptions");

                User u = new User(un,fn,array);

                Users.add(u);

            }
            setUsers(Users);
            for(int i =0; i<users.size(); i++){

                System.out.println(users.get(i).getUserName());
            }


            //System.out.println(res);

        }

        catch (Exception v){
            System.out.println(v);
        }

    }
    public void initRooms() throws FileNotFoundException{
        StringBuilder sb = new StringBuilder();
        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(new FileReader("JSONdata/ag.json"));

            JSONObject jsonObject = (JSONObject) obj;
            JSONArray arr = (JSONArray) jsonObject.get("ag");
            ArrayList<Room> Rooms = new ArrayList<>();
            for (int i = 0; i < arr.size(); i++)
            {
                JSONObject j = (JSONObject)arr.get(i);
                String rf = (String) j.get("roomFile");
                int index = Integer.parseInt((String)j.get("index"));
                String rn = (String) j.get("roomName");
                Room r = new Room(rn,rf,index);
                Rooms.add(r);
            }
            setRooms(Rooms);
            for(int i =0; i<rooms.size(); i++){
                rooms.get(i).getRoomName();
            }

        }

        catch (Exception v){
            System.out.println(v);
        }


    }

    public void setMsg(String msg) {

        this.msg = msg;
    }

    public void parseArgs(ArrayList commandList){
        init();
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
                agCommand(n);
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
    public void agCommand(int n) {
//        System.out.println("Working Directory = " +
//                System.getProperty("user.dir"));

        String res = "";

        for( int i =0; i<n; i++) {
            if(i==0){
                res += "\n";
            }
            int j = this.rooms.get(i).getIndex()+1;

            res+= j+".  ( )  "+this.rooms.get(i).getRoomName()+"\n";
        }
        setMsg(res);


    }
 

    public void run() {
        try {

            PrintStream pstream = new PrintStream (csocket.getOutputStream());

            parseArgs(this.cmdList);
            System.out.println(this.msg);

            pstream.println(this.msg);
            pstream.close();
            csocket.close();
        }
        catch (IOException e) {
            System.out.println(e);
        }
    }


}
