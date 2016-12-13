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
    //USER COMMANDS
    String LOGIN = "login";
    String LOGOUT = "logout";
    String HELP = "help";
    String QUIT = "q";
    String NEXT = "n";
    String END = "end";
    String AG = "ag";
    String SG = "sg";
    String RG = "rg";
    String NEW = "N";
    String READ = "r";
    String POST = "p";
    String SUBSCRIBE = "s";
    String UNSUBSCRIBE = "u";
    // SERVER STATUS REPLY
    String SUCCESS_OK = "OK 200";
    String ERR_NOTFOUND = "Not Found 404";
    String ERR_FORBIDDEN = "Forbidden 403";


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
            Object obj = parser.parse(new FileReader("./JSONData/users.json"));

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
            Object obj = parser.parse(new FileReader("./JSONData/ag.json"));

            JSONObject jsonObject = (JSONObject) obj;
            JSONArray arr = (JSONArray) jsonObject.get(AG);
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

    public void executespecialag(ArrayList <String> commandlist, PrintWriter pstream) {
        if(commandlist.get(0).equals(SUBSCRIBE) && commandlist.size() > 1) {
            JSONArray replyArray = new JSONArray();
            for(int i =1; i < commandlist.size(); i++) {
                int index = Integer.parseInt(commandlist.get(i));
                boolean result = currentuser.subscribegroup(index);
                if(!result && i+1 < commandlist.size())
                    statusReply(currentuser, SUBSCRIBE, ERR_FORBIDDEN, pstream, replyArray, false);
                else if (!result && !(i+1 < commandlist.size()))
                    statusReply(currentuser, SUBSCRIBE, ERR_FORBIDDEN, pstream, replyArray, true);
                else if(result && i+1 < commandlist.size())
                    statusReply(currentuser, SUBSCRIBE, SUCCESS_OK, pstream, replyArray, false);
                else
                    statusReply(currentuser, SUBSCRIBE, SUCCESS_OK, pstream, replyArray, true);
                init();
            }
        }
        if(commandlist.get(0).equals(UNSUBSCRIBE) && commandlist.size() > 1) {
            JSONArray replyArray = new JSONArray();
            for(int i =1; i < commandlist.size(); i++) {
                int index = Integer.parseInt(commandlist.get(i));
                boolean result = currentuser.unsubscribegroup(index);
                if(!result && i+1 < commandlist.size())
                    statusReply(currentuser, UNSUBSCRIBE, ERR_FORBIDDEN, pstream, replyArray, false);
                else if (!result && !(i+1 < commandlist.size()))
                    statusReply(currentuser, UNSUBSCRIBE, ERR_FORBIDDEN, pstream, replyArray, true);
                else if(result && i+1 < commandlist.size())
                    statusReply(currentuser, UNSUBSCRIBE, SUCCESS_OK, pstream, replyArray, false);
                else
                    statusReply(currentuser, UNSUBSCRIBE, SUCCESS_OK, pstream, replyArray, true);
                init();
            }
        }

    }

    boolean parseArgs(ArrayList commandList, BufferedReader br, PrintWriter pstream){
        init();
        if(commandList.size() == 0) {
            JSONArray replyArray = new JSONArray();
            currentuser = new User(null, null);
            statusReply(currentuser, LOGIN, ERR_FORBIDDEN, pstream, replyArray, true);
            //return false;
        }
        else if(commandList.get(0).equals(LOGIN) && !loggedIn){

            if(commandList.size()==2){
                String userName = commandList.get(1).toString();
                JSONArray replyArray = new JSONArray();
                if(userexists(userName))
                {
                    loggedIn = true;
                    System.out.println(userName + " Logged In");
                    statusReply(currentuser, LOGIN, SUCCESS_OK, pstream, replyArray, true);
                } else {
                    currentuser = new User(null, null);
                    statusReply(currentuser, LOGIN, ERR_NOTFOUND, pstream, replyArray, true);
                }
            }
            else{
                System.out.println("Invalid Number of Arguments");
            }

        }
        else if(commandList.get(0).equals(HELP)){
            if(commandList.size()==1){
                String help = commandList.get(1).toString();
                System.out.println(help);
            }
            else{
                System.out.println("Invalid Number of Arguments");
            }

        }
        else if(commandList.get(0).equals(AG) && loggedIn){
            int n = returnN(commandList);
            if (n == 0){
                System.out.println("Invalid Number of Arguments");
                JSONArray replyArray = new JSONArray();
                statusReply(currentuser, AG, ERR_FORBIDDEN, pstream, replyArray, true);
            }
            else{
                int start = 0;
                boolean returnvalue = agCommand(n, pstream, start);
                String message;
                int j = n;
                try {
                while((message = br.readLine()) != null){
                    if(message.equals("")){
                        continue;
                    }
                    if(!returnvalue) {
                        break;
                    }
                    ArrayList<String> commands = new ArrayList<>();
                    StringTokenizer tok = new StringTokenizer(message);
                    while (tok.hasMoreTokens()) {
                        commands.add(tok.nextToken());
                    }
                    if(commands.get(0).equals(QUIT)) {
                        JSONArray replyArray = new JSONArray();
                        statusReply(currentuser, QUIT, SUCCESS_OK, pstream, replyArray, true);
                        break;
                    }
                    else if(commands.get(0).equals(NEXT)) {
                        start += n;
                        returnvalue = agCommand(n, pstream, start);
                        if(!returnvalue) {
                            break;
                        }
                    }
                    else if(commands.size() > 0 && (commands.get(0).equals(SUBSCRIBE) || commands.get(0).equals(UNSUBSCRIBE))){
                        executespecialag(commands, pstream);
                    }
                    else {
                        JSONArray replyArray = new JSONArray();
                        statusReply(currentuser, AG, ERR_FORBIDDEN, pstream, replyArray, true);
                    }
                }}
                catch (IOException e)
                {
                    System.out.println("Exception : " + e);
                }
            }

        }
        else if(commandList.get(0).equals(SG) && loggedIn){
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
                        if(message.equals("")){
                            continue;
                        }
                        ArrayList<String> commands = new ArrayList<>();
                        StringTokenizer tok = new StringTokenizer(message);
                        while (tok.hasMoreTokens()) {
                            commands.add(tok.nextToken());
                        }
                        if(commands.get(0).equals(QUIT) && commandList.size() == 1) {
                            JSONArray replyArray = new JSONArray();
                            statusReply(currentuser, QUIT, SUCCESS_OK, pstream, replyArray, true);
                            break;
                        }
                        if(commands.get(0).equals(POST) && commands.size() > 1) {

                        }
                        else if(commands.get(0).equals(NEXT)) {
                            start += n;
                            returnvalue1 = sgCommand(n, pstream, start);
                            if(!returnvalue1) {
                                break;
                            }
                        }
                        else if(commands.size() > 0 && commands.get(0).equals(UNSUBSCRIBE)){
                            //executespecialsg(commands);
                            pstream.println(END);pstream.flush();
                        }
                        else {
                            JSONArray replyArray = new JSONArray();
                            statusReply(currentuser, SG, ERR_FORBIDDEN, pstream, replyArray, true);
                        }
                    }}
                catch (IOException e)
                {
                    System.out.println("Exception : " + e);
                }
            }


        }
        else if(commandList.get(0).equals(RG) && loggedIn){
            int n = returnNforrg(commandList);
            if (n == 0 || commandList.size() <= 1 || commandList.size() >= 4){
                JSONArray replyArray = new JSONArray();
                statusReply(currentuser, RG, ERR_FORBIDDEN, pstream, replyArray, false);
                String res = "Invalid Number of Arguments";
                JSONObject reply = currentuser.createreplyjson(SG, res, null, null);
                replyArray.add(reply);
                pstream.println(replyArray);pstream.println(END);pstream.flush();
                return false;
            }
            boolean ret = true;
            ret = currentuser.checksubscribedbyname(commandList.get(1).toString());
            if (!ret) {
                JSONArray replyArray = new JSONArray();
                statusReply(currentuser, RG, ERR_FORBIDDEN, pstream, replyArray, false);
                String res = "Invalid group name";
                JSONObject reply = currentuser.createreplyjson(SG, res, null, null);
                replyArray.add(reply);
                pstream.println(replyArray);pstream.println(END);pstream.flush();
                return false;
            }
            if(ret){
                int start = 0;
                boolean returnvalue1 = rgCommand(n, pstream, start, commandList.get(1).toString());
                String message;
                int j = n;
                try {
                    while((message = br.readLine()) != null) {
                        ArrayList<String> commands = new ArrayList<>();
                        StringTokenizer tok = new StringTokenizer(message);
                        while (tok.hasMoreTokens()) {
                            commands.add(tok.nextToken());
                        }
                        if(commands.get(0).equals(QUIT) && commands.size() == 1) {
                            JSONArray replyArray = new JSONArray();
                            statusReply(currentuser, QUIT, SUCCESS_OK, pstream, replyArray, true);
                            break;
                        }
                        else if(commands.get(0).equals(POST) && commands.size() == 1) {
                            JSONObject reply = currentuser.createreplyjson("rgp", null, commandList.get(1).toString(), currentuser.getUserName());
                            pstream.println(reply);
                            String messageobject;
                            while((messageobject = br.readLine()) != null) {
                                if(messageobject.equals("")) {
                                    continue;
                                }
                                currentuser.addpostogroup(messageobject, commandList.get(1).toString());
                                break;
                            }
                        }
                        else if(commands.get(0).equals(NEXT)) {
                            start += n;
                            returnvalue1 = rgCommand(n, pstream, start, commandList.get(1).toString());
                            if(!returnvalue1) {
                                break;
                            }
                        }
                        else if(commands.get(0).equals(READ) && commands.size() == 2) {
                            String[] inds = commands.get(1).toString().split("-");
                            int begind = Integer.parseInt(inds[0]);
                            int endind = begind + 1;
                            endind = Integer.parseInt(inds[1]);
                            ArrayList<post> posts = new ArrayList<>();
                            posts = currentuser.getunreadpostsfromgroup(commandList.get(1).toString());
                            currentuser.getreadpostsfromgroup(commandList.get(1).toString(), posts);
                            for(int w=begind - 1; w < endind - 1; w++){
                                currentuser.markpostasread(posts.get(w).getpostid(), commandList.get(1).toString());
                            }
                            String res = "";
                            JSONArray replyArray = new JSONArray();
                            statusReply(currentuser, RG, ERR_FORBIDDEN, pstream, replyArray, false);
                            JSONObject reply = currentuser.createreplyjson(SG, res, null, null);
                            replyArray.add(reply);
                            pstream.println(replyArray);pstream.println(END);pstream.flush();
                        }
                        else if(commands.size() == 1){
                            int ind = Integer.parseInt(commands.get(0));
                            ind--;
                            ArrayList<post> posts = new ArrayList<>();
                            posts = currentuser.getunreadpostsfromgroup(commandList.get(1).toString());
                            currentuser.getreadpostsfromgroup(commandList.get(1).toString(), posts);
                            if(!(ind < posts.size() || ind < 0)) {
                                JSONArray replyArray = new JSONArray();
                                statusReply(currentuser, RG, ERR_FORBIDDEN, pstream, replyArray, false);
                                String res = "Invalid";
                                JSONObject reply = currentuser.createreplyjson(SG, res, null, null);
                                replyArray.add(reply);
                                pstream.println(replyArray);pstream.println(END);pstream.flush();
                            }
                            displaypost(posts.get(ind),commandList.get(1).toString(),pstream);
                            currentuser.markpostasread(posts.get(ind).getpostid(), commandList.get(1).toString());
                            try {
                                while ((message = br.readLine()) != null) {
                                    if(message.equals("")) {
                                        continue;
                                    }
                                    ArrayList<String> cmdss = new ArrayList<>();
                                    StringTokenizer toks = new StringTokenizer(message);
                                    while (toks.hasMoreTokens()) {
                                        cmdss.add(toks.nextToken());
                                    }
                                    if(cmdss.get(0).equals(QUIT) && cmdss.size() == 1){
                                        String res = "";
                                        JSONArray replyArray = new JSONArray();
                                        statusReply(currentuser, RG, SUCCESS_OK, pstream, replyArray, false);
                                        JSONObject reply = currentuser.createreplyjson(SG, res, null, null);
                                        replyArray.add(reply);
                                        pstream.println(replyArray);pstream.println(END);pstream.flush();
                                        break;
                                    }
                                }
                            }
                            catch(IOException e) {
                                System.out.println("read post number : " + e);
                            }
                        }
                    }}
                catch (IOException e)
                {
                    System.out.println("Exception : " + e);
                }
                System.out.println("not here 2");
            }
            System.out.println("not here 3");

        }
        else if(commandList.get(0).equals(LOGOUT) && loggedIn){

            //if(commandList.size()==1){

                //String userName = commandList.get(2).toString();
                //if(currentuser.getUserName().equals(userName))
                //{
                    loggedIn = false;
                    JSONArray replyArray = new JSONArray();
                    JSONObject reply = currentuser.statusReplyJson(LOGOUT, SUCCESS_OK);
                    replyArray.add(reply);
                    pstream.println(replyArray);pstream.flush();
                    this.setmessage(LOGOUT);
                    return true;
                //}
            //}
            //else{
                //System.out.println("Invalid Number of Arguments");
            //}
            //return false;
        }
        else {
            if (!loggedIn) {

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

    public void displaypost(post thispost, String group, PrintWriter pstream) {
        String res = "";
        res = "Group : " + group + "\n";
        res += "Subject : " + thispost.getsubject() + "\n";
        res += "Author : " + thispost.getAuthor() + "\n";
        res += "Date : " + thispost.gettime() + "\n\n";
        for(int i = 0; i < thispost.getmessage().size(); i++ ) {
            res += thispost.getmessagewithindex(i) + "\n";
        }
        JSONArray replyArray = new JSONArray();
        statusReply(currentuser, RG, SUCCESS_OK, pstream, replyArray, false);
        JSONObject reply = currentuser.createreplyjson(SG, res, null, null);
        replyArray.add(reply);
        pstream.println(replyArray);pstream.println(END);pstream.flush();
    }

    public void statusReply(User currentuser, String type, String status, PrintWriter pstream, JSONArray replyArray, boolean doEnd) {
        JSONObject reply = currentuser.statusReplyJson(type, status);
        replyArray.add(reply);
        if(doEnd) {
            pstream.println(replyArray);
            pstream.println(END);pstream.flush();
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
                sub = SUBSCRIBE;
            }
            res+= j+".  (" + sub + ")  "+this.rooms.get(i).getRoomName()+"\n";
        }
        // SEND STATUS OK AND DATA
        JSONArray replyArray = new JSONArray();
        statusReply(currentuser, AG, SUCCESS_OK, pstream, replyArray, false);

        JSONObject reply = currentuser.createreplyjson(AG, res, null, null);
        replyArray.add(reply);
        pstream.println(replyArray);pstream.println(END);pstream.flush();
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
        // SEND STATUS OK AND DATA
        JSONArray replyArray = new JSONArray();
        statusReply(currentuser, SG, SUCCESS_OK, pstream, replyArray, false);

        JSONObject reply = currentuser.createreplyjson(SG, res, null, null);
        replyArray.add(reply);
        pstream.println(replyArray);pstream.println(END);pstream.flush();
        return returns;
    }

    public boolean rgCommand(int n, PrintWriter pstream, int start, String group) {

        String res = "";
        boolean returns = true;
        ArrayList<post> posts = new ArrayList<>();
        posts = currentuser.getunreadpostsfromgroup(group);
        currentuser.getreadpostsfromgroup(group, posts);
        for(int i = start; i < n+start; i++) {
            if(posts.size() <= i){ returns = false;break;}
            if(i==start){
                res += "\n";
            }
            String j = posts.get(i).gettime() + "    " + posts.get(i).getmessagewithindex(0);
            String sub = " ";
            if(!posts.get(i).isread()) {
                sub = NEW;
            }
            res+= Integer.toString(i+1)+".  " + sub + "  "+ j +"\n";
        }
        JSONArray replyArray = new JSONArray();
        statusReply(currentuser, RG, SUCCESS_OK, pstream, replyArray, false);

        JSONObject reply = currentuser.createreplyjson(RG, res, group, currentuser.getUserName());
        replyArray.add(reply);
        pstream.println(replyArray);pstream.println(END);pstream.flush();
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
                logout = parseArgs(this.cmdList, br, pstream);
                pstream.println(END);pstream.flush();
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
