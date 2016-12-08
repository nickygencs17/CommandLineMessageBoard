import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class Server extends Thread{
    Socket csocket;
    String msg;
    ArrayList <String> cmdList;
    boolean loggedIn = false;
    User currentuser = null;
    ArrayList <Room> rooms;
    ArrayList <User> users;
    String currentstate = "";


    Server(Socket csocket, String message, ArrayList<String> ls) {
        this.csocket = csocket;
        this.msg = message;
        this.cmdList = ls;
    }
    Server(Socket csocket) {
        this.csocket = csocket;
    }
    public void setCmdList(ArrayList<String> ls){
        this.cmdList = ls;
    }
    public void setmessage(String message){
        this.msg = message;
    }
    public void setCurrentstate(String state)
    {
        currentstate = state;
    }
    public void resetcurrentstate()
    {
        currentstate = "";
    }

    boolean userexists(String username)
    {
        for(int i = 0; i < users.size(); i++)
        {
            if(users.get(i).getUserName().equals(username))
            {
                currentuser = users.get(i);
                return true;
            }
        }
        return false;
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
            Object obj = parser.parse(new FileReader("JSONData/users.json"));

            JSONObject jsonObject = (JSONObject) obj;
            JSONArray arr = (JSONArray) jsonObject.get("existingUsers");
            ArrayList<User> Users= new ArrayList<>();
            for (int i = 0; i < arr.size(); i++)
            {
                JSONObject j = (JSONObject)arr.get(i);
                String un = (String) j.get("userName");
                JSONArray array = (JSONArray) j.get("subscriptions");
                ArrayList <String> subslist = new ArrayList<>();
                for(int k = 0; k<array.size();k++){
                    JSONObject subs = (JSONObject)array.get(k);
                    subslist.add(k,subs.get("index").toString());
                }
                User u = new User(un, subslist);

                Users.add(u);
            }
            setUsers(Users);
            for(int i =0; i<users.size(); i++){

               // System.out.println(users.get(i).getUserName());
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
            Object obj = parser.parse(new FileReader("JSONData/ag.json"));

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

    public void executespecialag(ArrayList <String> commandlist) {
        if(commandlist.get(0).equals("s") && commandlist.size() > 1) {
            for(int i =1; i < commandlist.size(); i++) {
                int index = Integer.parseInt(commandlist.get(i));
                currentuser.subscribegroup(index);
                init();
            }
        }
        if(commandlist.get(0).equals("u") && commandlist.size() > 1) {
            for(int i =1; i < commandlist.size(); i++) {
                int index = Integer.parseInt(commandlist.get(i));
                currentuser.unsubscribegroup(index);
                init();
            }
        }

    }

    boolean parseArgs(ArrayList commandList, BufferedReader br, PrintWriter pstream) throws Exception{
        init();
        if(commandList.get(0).equals("login") && loggedIn == false){

            if(commandList.size()==2){
                String userName = commandList.get(1).toString();
                if(userexists(userName))
                {
                    loggedIn = true;
                    System.out.println(userName + " Logged In");
                }
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
                int start = 0;
                boolean returnvalue = agCommand(n, pstream, start);
                String message;
                int j = n;
                try {
                while((message = br.readLine()) != null){
                    if(!returnvalue) {
                        break;
                    }
                    ArrayList<String> commands = new ArrayList<>();
                    StringTokenizer tok = new StringTokenizer(message);
                    while (tok.hasMoreTokens()) {
                        commands.add(tok.nextToken());
                    }
                    if(commands.get(0).equals("q") && commandList.size() == 1) {
                        break;
                    }
                    else if(commands.get(0).equals("n")) {
                        start += n;
                        returnvalue = agCommand(n, pstream, start);
                        if(!returnvalue) {
                            break;
                        }
                    }
                    else if(commands.size() > 0){
                        executespecialag(commands);
                        pstream.println("end");pstream.flush();
                    }
                }}
                catch (IOException e)
                {
                    System.out.println("Exception : " + e);
                }
            }

        }
        else if(commandList.get(0).equals("sg") && loggedIn == true){
            int n = returnN(commandList);
            if (n == 0){
                System.out.println("Invalid Number of Arguments");
            }
            else{
                int start = 0;
                boolean returnvalue1 = sgCommand(n, pstream, start);
                String message;
                int j = n;
                try {
                    while((message = br.readLine()) != null){
                        ArrayList<String> commands = new ArrayList<>();
                        StringTokenizer tok = new StringTokenizer(message);
                        while (tok.hasMoreTokens()) {
                            commands.add(tok.nextToken());
                        }
                        if(commands.get(0).equals("q") && commandList.size() == 1) {
                            break;
                        }
                        if(commands.get(0).equals("u") && commands.size() > 1) {
                            for(int i =1; i < commands.size(); i++) {
                                int index = Integer.parseInt(commands.get(i));
                                int ind = Integer.parseInt(currentuser.getSubscriptions().get(index - 1));
                                currentuser.unsubscribegroup(ind);
                                pstream.println("end");pstream.flush();
                                init();
                            }
                        }
                        else if(commands.get(0).equals("n")) {
                            start += n;
                            returnvalue1 = sgCommand(n, pstream, start);
                            if(!returnvalue1) {
                                break;
                            }
                        }
                        else if(commands.size() > 0){
                            //executespecialsg(commands);
                            pstream.println("end");pstream.flush();
                        }
                    }}
                catch (IOException e)
                {
                    System.out.println("Exception : " + e);
                }
            }


        }
        else if(commandList.get(0).equals("rg") && loggedIn == true){
            int n = returnNforrg(commandList);
            if (n == 0 ){
                System.out.println("Invalid Number of Arguments");
            }
            if (commandList.size() <= 1 || commandList.size() >= 4){
                System.out.println("Invalid Number of Arguments");
            }
            boolean ret = true;
            try {
                ret = currentuser.checksubscribedbyname(commandList.get(1).toString());
                if (!ret) {
                    System.out.println("Invalid group name");
                }
            }
            catch(IOException e){
                System.out.println("ERROR : " + e);
            }
            if(ret){
                int start = 0;
                boolean returnvalue1 = rgCommand(n, pstream, start, commandList.get(1).toString());
                String message;
                int j = n;
                try {
                    while((message = br.readLine()) != null){
                        ArrayList<String> commands = new ArrayList<>();
                        StringTokenizer tok = new StringTokenizer(message);
                        while (tok.hasMoreTokens()) {
                            commands.add(tok.nextToken());
                        }
                        if(commands.get(0).equals("q") && commandList.size() == 1) {
                            break;
                        }
                        if(commands.get(0).equals("u") && commands.size() > 1) {
                            for(int i =1; i < commands.size(); i++) {
                                int index = Integer.parseInt(commands.get(i));
                                int ind = Integer.parseInt(currentuser.getSubscriptions().get(index - 1));
                                currentuser.unsubscribegroup(ind);
                                pstream.println("end");pstream.flush();
                                init();
                            }
                        }
                        else if(commands.get(0).equals("n")) {
                            start += n;
                            returnvalue1 = rgCommand(n, pstream, start, commandList.get(1).toString());
                            if(!returnvalue1) {
                                break;
                            }
                        }
                        else if(commands.size() > 0){
                            //executespecialsg(commands);
                            pstream.println("end");pstream.flush();
                        }
                    }}
                catch (IOException e)
                {
                    System.out.println("Exception : " + e);
                }
            }


        }
        else if(commandList.get(0).equals("logout") && loggedIn == true){

            if(commandList.size()==2){

                String userName = commandList.get(2).toString();
                if(currentuser.getUserName().equals(userName))
                {
                    loggedIn = false;
                    pstream.println("logout");pstream.flush();
                    this.setmessage("logout");
                    return true;
                }
            }
            else{
                System.out.println("Invalid Number of Arguments");
            }
            return false;
        }
        else {
            if (loggedIn == false) {

                System.out.println("Please Log In");
            } else {

                System.out.println("Invalid ");
            }
        }
        return false;
    }


    public int returnN(ArrayList commandList){
        if(commandList.size()==2){
            String n = commandList.get(1).toString();
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
    public int returnNforrg(ArrayList commandList){
        if(commandList.size()==3){
            String n = commandList.get(2).toString();
            int number = Integer.parseInt(n);
            return number;
        }
        else if(commandList.size() == 2){
            return 5;
        }
        else{
            return 0;
        }

    }
    public boolean agCommand(int n, PrintWriter pstream, int start) {

        String res = "";
        boolean returns = true;
        for( int i =start; i< n+start; i++) {
            if(rooms.size() <= i){ returns = false;break;}
            if(i==start){
                res += "\n";
            }
            int j = this.rooms.get(i).getIndex()+1;
            String sub = " ";
            if(currentuser.isroomsubscribed(j))
            {
                sub = "s";
            }
            res+= j+".  (" + sub + ")  "+this.rooms.get(i).getRoomName()+"\n";
        }
        JSONObject reply = currentuser.createreplyjson("ag", res, null, null);
        pstream.println(reply);pstream.println("end");pstream.flush();
          return returns;
    }

    public boolean sgCommand(int n, PrintWriter pstream, int start) {

        String res = "";
        boolean returns = true;
        for(int i = start; i < n+start; i++) {
            if(currentuser.getSubscriptions().size() <= i){ returns = false;break;}
            if(i==start){
                res += "\n";
            }
            String j = currentuser.getSubscriptions().get(i);
            String sub = " ";
            int num = 0;
            Date dt;
            dt = currentuser.getlastaccessed(j);
            num = currentuser.numtextsaftertime(j,dt);
            if(num > 0)
            {
                sub = Integer.toString(num);
            }
            res+= Integer.toString(i+1)+".  (" + sub + ")  "+this.rooms.get(Integer.parseInt(j)).getRoomName()+"\n";
            currentuser.updategrouptime(j);
        }
        JSONObject reply = currentuser.createreplyjson("sg", res, null, null);
        pstream.println(reply);pstream.println("end");pstream.flush();
        return returns;
    }

    public boolean rgCommand(int n, PrintWriter pstream, int start, String group) {

        String res = "";
        boolean returns = true;
//        for(int i = start; i < n+start; i++) {
//            if(currentuser.getSubscriptions().size() <= i){ returns = false;break;}
//            if(i==start){
//                res += "\n";
//            }
//            String j = currentuser.getSubscriptions().get(i);
//            String sub = " ";
//            int num = 0;
//            Date dt;
//            dt = currentuser.getlastaccessed(j);
//            num = currentuser.numtextsaftertime(j,dt);
//            if(num > 0)
//            {
//                sub = Integer.toString(num);
//            }
//            System.out.println("");
//            res+= Integer.toString(i+1)+".  (" + sub + ")  "+this.rooms.get(Integer.parseInt(j)).getRoomName()+"\n";
//            currentuser.updategrouptime(j);
//        }
        JSONObject reply = currentuser.createreplyjson("rg", res, group, currentuser.getUserName());
        System.out.println("reply : " + reply);
        pstream.println(reply);pstream.println("end");pstream.flush();
        return returns;
    }
 

    public void run(){
        PrintWriter pstream = null;
        BufferedReader br;
        InputStreamReader isr;
        InputStream is;
        boolean logout = false;
        try {
            pstream = new PrintWriter(new OutputStreamWriter(this.csocket.getOutputStream()));
            is = this.csocket.getInputStream();
            isr = new InputStreamReader(is);
            br = new BufferedReader(isr);
            String message;
            while (!logout && (message = br.readLine()) != null) {
                ArrayList<String> commmandList = new ArrayList<>();
                //String message = br.readLine();
                this.setmessage(message);
                StringTokenizer tok = new StringTokenizer(message);
                while (tok.hasMoreTokens()) {
                    commmandList.add(tok.nextToken());
                }
                this.setCmdList(commmandList);
                try {
                    try
                    {
                        logout = parseArgs(this.cmdList, br, pstream);
                    }
                    catch (Exception e){
                        System.out.println("ERROR : " + e);
                    }
                }
                catch(Exception e) {
                    System.out.println("ERROR : " + e);
                }
                //pstream.println(this.msg);pstream.println("end");pstream.flush();
                pstream.println("end");pstream.flush();
                System.out.println("Message received from server is " + this.msg);
                //pstream.close();
                if(logout)
                {
                    pstream.close();
                    csocket.close();
                }
            }System.out.println("Server thread Stopped");
        }
        catch (IOException e)
        {
            System.out.println("Exception in thread : " + e);
        }
        finally
        {
            try
            {
                //pstream.close();
                csocket.close();
                System.out.println("Server thread Stopped");
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
        }

    }
}
