import java.io.*;
import java.net.Socket;
import java.util.Date;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;


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

    // CHECK IF USER EXISTS
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
    // INIT USERS FROM USERS.JSON UNDER JSON DATA FOLDER
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
    // INIT ROOMS FROM AG.JSON UNDER JSON DATA FOLDER
    public void initRooms() throws FileNotFoundException{
        StringBuilder sb = new StringBuilder();
        JSONParser parser = new JSONParser();
        try {
            // GET ALL GROUPS INTO AN OBJECT THAT LATER ON DEALED AS JSON OBJECT
            Object obj = parser.parse(new FileReader("JSONData/ag.json"));

            JSONObject jsonObject = (JSONObject) obj;
            JSONArray arr = (JSONArray) jsonObject.get(AG);
            ArrayList<Room> Rooms = new ArrayList<>();
            // CREATE ROOMS AND GIVING ROOM NAMES AND FILE CAPTURED FROM JSON OBJECT
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

    // THIS METHOD IS CALLED WHEN USER DEMANDS TO SUBSCRIBE OR UN-SUBSCRIBE FROM ALL GROUPS
    // IF USER SUBSCRIBES OR UN-SUBSCRIBES TO AN ALREADY SUBSCRIBED GROUP OR FROM AN
    // ALREADY UN SUBSCRIBED GROUP RESPECTIVELY, ERR_FORBIDDEN IS SENT BACK FROM SERVER
    // ELSE SUCCESS_OK IS SENT BACK.
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

    // THIS METHOD IS CALLED WHEN USER DEMANDS TO UN SUBSCRIBE FROM SUBSCRIBED GROUPS
    // IF USER UN SUBSCRIBES FROM AN UN-SUBSCRIBED GROUP,
    // ERR_FORBIDDEN IS SENT BACK FROM SERVER, ELSE SUCCESS_OK IS SENT BACK.
    public void executespecialsg(ArrayList<String> commandList, PrintWriter pstream) {
        if(commandList.get(0).equals(UNSUBSCRIBE) && commandList.size() > 1) {
            JSONArray replyArray = new JSONArray();
            for(int i = 1; i < commandList.size(); i++) {
                int index = Integer.parseInt(commandList.get(i));
                String j = currentuser.getSubscriptions().get(index - 1);
                boolean result = currentuser.unsubscribegroup(Integer.parseInt(j));
                if(!result && i+1 < commandList.size())
                    statusReply(currentuser, UNSUBSCRIBE, ERR_FORBIDDEN, pstream, replyArray, false);
                else if(!result && !(i+1 < commandList.size()))
                    statusReply(currentuser, UNSUBSCRIBE, ERR_FORBIDDEN, pstream, replyArray, true);
                else if(result && i+1 < commandList.size())
                    statusReply(currentuser, UNSUBSCRIBE, SUCCESS_OK, pstream, replyArray, false);
                else
                    statusReply(currentuser, UNSUBSCRIBE, SUCCESS_OK, pstream, replyArray, true);
                init();
            }
        }
    }

    // THIS METHOD TAKES IN THE COMMANDS BEING SENT FROM CLIENT AND CHECKS WHETHER IT'S
    // AG, SG, OR RG
    boolean parseArgs(ArrayList commandList, BufferedReader br, PrintWriter pstream){
        // ALL INITIALIZATIONS OCCUR FROM HERE.
        init();
        // AS USER NEEDS TO LOG IN FIRST, IF COMMANDS BEING SENT IS 0, ERR_FORBIDDEN
        // IS SENT BACK TO THE USER PROMPTING TO LOGIN AGAIN.
        if(commandList.size() == 0) {
            JSONArray replyArray = new JSONArray();
            currentuser = new User(null, null);
            statusReply(currentuser, LOGIN, ERR_FORBIDDEN, pstream, replyArray, true);
        }
        // CHECKS IF USER IS NOT LOGGED IN AND LOGS HIM/HER IN.
        else if(commandList.get(0).equals(LOGIN) && !loggedIn){

            // GET USERNAME FROM COMMANDS AND CALLS USEREXISTS METHOD.
            // IF TRUE, SUCCESS_OK IS SENT BACK, AND A PROMPT STATING SUCCESSFULLY LOGGING IN
            // ELSE ERR_NOTFOUND IS SENT BACK, AND PROMPTING USER AGAIN.
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
        // IF USER SENDS HELP, INSTRUCTIONS ARE SENT BACK TO THE USER
        else if(commandList.get(0).equals(HELP)){
            if(commandList.size()==1){
                String help = commandList.get(1).toString();
                System.out.println(help);
            }
            else{
                System.out.println("Invalid Number of Arguments");
            }

        }
        // CHECKS IF USER IS LOGGED IN, AND IF COMMAND IS AG
        // IF TRUE, SUCCESS_OK IS SENT BACK AND USER CAN NOW USE AG SUB-COMMANDS.
        // ELSE ERR_FORBIDDEN IS SENT BACK, PROMPTING USER AGAIN.
        else if(commandList.get(0).equals(AG) && loggedIn){
            int n = returnN(commandList);
            if (n == 0){
                JSONArray replyArray = new JSONArray();
                statusReply(currentuser, AG, ERR_FORBIDDEN, pstream, replyArray, false);
                String res = "Invalid Number of Arguments";
                JSONObject reply = currentuser.createreplyjson(AG, res, null, null);
                replyArray.add(reply);
                pstream.println(replyArray);pstream.println(END);pstream.flush();

            }
            else{
                int start = 0;
                boolean returnvalue = agCommand(n, pstream, start);
                String message;
                int j = n;
                try {
                    // CHECKS IF BUFFER READER IS STILL NOT EMPTY
                    while((message = br.readLine()) != null){
                    if(message.equals("")){
                        continue;
                    }
                    if(!returnvalue) {
                        break;
                    }
                    ArrayList<String> commands = new ArrayList<>();
                    StringTokenizer tok = new StringTokenizer(message);
                    // IF MULTIPLE COMMANDS NEEDS TO BE EXECUTED,
                    // THIS LOOP PUTS THEM ALL IN ONE COMMAND.
                    while (tok.hasMoreTokens()) {
                        commands.add(tok.nextToken());
                    }
                    // IF USER WANTS TO QUIT OUT OF AG SUB-COMMANDS.
                    if(commands.get(0).equals(QUIT)) {
                        JSONArray replyArray = new JSONArray();
                        statusReply(currentuser, QUIT, SUCCESS_OK, pstream, replyArray, true);
                        break;
                    }
                    // IF USER WANTS NEXT "N" GROUPS FROM ALL GROUPS
                    else if(commands.get(0).equals(NEXT)) {
                        start += n;
                        returnvalue = agCommand(n, pstream, start);
                        if(!returnvalue) {
                            break;
                        }
                    }
                    // IF USER WANTS TO SUBSCRIBE TO OR UN-SUBSCRIBE FROM A GROUP
                    else if(commands.size() > 0 && (commands.get(0).equals(SUBSCRIBE) || commands.get(0).equals(UNSUBSCRIBE))){
                        executespecialag(commands, pstream);
                    }
                    // IF ANY OTHER COMMAND THAT IS NOT A SUB-COMMAND OF AG
                    // ERR_FORBIDDEN IS SENT BACK.
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
        // CHECKS IF USER IS LOGGED IN, AND IF COMMAND IS SG
        // IF TRUE, SUCCESS_OK IS SENT BACK AND USER CAN NOW USE SG SUB-COMMANDS.
        // ELSE ERR_FORBIDDEN IS SENT BACK, PROMPTING USER AGAIN.
        else if(commandList.get(0).equals(SG) && loggedIn){
            int n = returnN(commandList);
            if (n == 0){
                JSONArray replyArray = new JSONArray();
                statusReply(currentuser, SG, ERR_FORBIDDEN, pstream, replyArray, false);
                String res = "Invalid Number of Arguments";
                JSONObject reply = currentuser.createreplyjson(SG, res, null, null);
                replyArray.add(reply);
                pstream.println(replyArray);pstream.println(END);pstream.flush();
            }
            else{
                int start = 0;
                boolean returnvalue1 = sgCommand(n, pstream, start);
                String message;
                int j = n;
                try {
                    // CHECKS IF BUFFER READER IS STILL NOT EMPTY
                    while((message = br.readLine()) != null){
                        if(message.equals("")){
                            continue;
                        }
                        ArrayList<String> commands = new ArrayList<>();
                        StringTokenizer tok = new StringTokenizer(message);
                        // IF MULTIPLE COMMANDS NEEDS TO BE EXECUTED,
                        // THIS LOOP PUTS THEM ALL IN ONE COMMAND.
                        while (tok.hasMoreTokens()) {
                            commands.add(tok.nextToken());
                        }
                        // IF USER WANTS TO QUIT OUT OF SG SUB-COMMANDS.
                        // SUCCESS_OK IS SENT BACK AND EXITS SG SUB-COMMAND.
                        if(commands.get(0).equals(QUIT) && commandList.size() == 1) {
                            JSONArray replyArray = new JSONArray();
                            statusReply(currentuser, QUIT, SUCCESS_OK, pstream, replyArray, true);
                            break;
                        }
                        // IF USER WANTS NEXT "N" GROUPS FROM SUBSCRIBED GROUPS
                        else if(commands.get(0).equals(NEXT)) {
                            start += n;
                            returnvalue1 = sgCommand(n, pstream, start);
                            if(!returnvalue1) {
                                break;
                            }
                        }
                        // IF USER WANTS TO UN-SUBSCRIBE FROM A GROUP
                        else if(commands.size() > 0 && commands.get(0).equals(UNSUBSCRIBE)){
                            executespecialsg(commands, pstream);
                        }
                        // IF ANY OTHER COMMAND THAT IS NOT A SUB-COMMAND OF SG
                        // ERR_FORBIDDEN IS SENT BACK.
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
        // CHECKS IF USER IS LOGGED IN, AND IF COMMAND IS RG
        // IF TRUE, SUCCESS_OK IS SENT BACK AND USER CAN NOW USE RG SUB-COMMANDS.
        // ELSE ERR_FORBIDDEN IS SENT BACK, PROMPTING USER AGAIN.
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
            // SEARCHES THROUGH ALL THE ROOMS TO FIND ROOM THE USER COMMANDED.
            ret = currentuser.checksubscribedbyname(commandList.get(1).toString());
            // IF NOT FOUND, ERR_FORBIDDEN IS SENT BACK AND A DIALOG SAYING INVALID
            // GROUP NAME.
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
                    // CHECKS IF BUFFER READER IS STILL NOT EMPTY
                    while((message = br.readLine()) != null) {
                        ArrayList<String> commands = new ArrayList<>();
                        StringTokenizer tok = new StringTokenizer(message);
                        while (tok.hasMoreTokens()) {
                            commands.add(tok.nextToken());
                        }
                        // IF USER WANTS TO QUIT OUT OF RG SUB-COMMANDS.
                        // SUCCESS_OK IS SENT BACK AND QUITS OUT OF RG SUB-COMMAND
                        if(commands.get(0).equals(QUIT) && commands.size() == 1) {
                            JSONArray replyArray = new JSONArray();
                            statusReply(currentuser, QUIT, SUCCESS_OK, pstream, replyArray, true);
                            break;
                        }
                        // IF USER COMMANDS TO POST TO A ROOM. A MESSAGE IS SENT BACK
                        // WITH A PROMPT TO ENTER SUBJECT, POST TO THE USER.
                        else if(commands.get(0).equals(POST) && commands.size() == 1) {
                            JSONArray replyArray = new JSONArray();
                            JSONObject reply = currentuser.createreplyjson("rgp", null, commandList.get(1).toString(), currentuser.getUserName());
                            replyArray.add(reply);
                            pstream.println(replyArray);
                            String messageobject;
                            // CHECKS IF BUFFER READER IS STILL NOT EMPTY
                            while((messageobject = br.readLine()) != null) {
                                if(messageobject.equals("")) {
                                    continue;
                                }
                                currentuser.addpostogroup(messageobject, commandList.get(1).toString());
                                break;
                            }
                        }
                        // IF USER WANTS NEXT "N" GROUPS FROM AN OPENED ROOM
                        else if(commands.get(0).equals(NEXT)) {
                            start += n;
                            returnvalue1 = rgCommand(n, pstream, start, commandList.get(1).toString());
                            if(!returnvalue1) {
                                break;
                            }
                        }
                        // IF USER COMMANDS TO READ  WITH GROUP NAME
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
                        // IF USER COMMANDS TO READ THE nTH POST IN A SPECIFIC ROOM.
                        else if(commands.size() == 1){
                            int ind = Integer.parseInt(commands.get(0));
                            ind--;
                            ArrayList<post> posts = new ArrayList<>();
                            // FETCHES UNREAD AND READ POSTS FROM A SPECIFIC ROOM
                            // INDICATED BY THE USER
                            posts = currentuser.getunreadpostsfromgroup(commandList.get(1).toString());
                            currentuser.getreadpostsfromgroup(commandList.get(1).toString(), posts);
                            // SERVER SENDS ALL READ AND UN READ POSTS AS A JSON ARRAY
                            // TO THE USER.
                            if(!(ind < posts.size() || ind < 0)) {
                                JSONArray replyArray = new JSONArray();
                                statusReply(currentuser, RG, ERR_FORBIDDEN, pstream, replyArray, false);
                                String res = "Invalid";
                                JSONObject reply = currentuser.createreplyjson(SG, res, null, null);
                                replyArray.add(reply);
                                pstream.println(replyArray);pstream.println(END);pstream.flush();
                            }
                            displaypost(posts.get(ind),commandList.get(1).toString(),pstream);
                            // MARKS NEWLY READ POSTS BY THE USER AS READ.
                            currentuser.markpostasread(posts.get(ind).getpostid(), commandList.get(1).toString());
                            // THESE ARE THE SUB-COMMANDS WITHIN A POST
                            try {
                                // CHECKS IF THE BUFFER IS EMPTY
                                while ((message = br.readLine()) != null) {
                                    if(message.equals("")) {
                                        continue;
                                    }
                                    ArrayList<String> cmdss = new ArrayList<>();
                                    StringTokenizer toks = new StringTokenizer(message);
                                    // COMBINES ALL COMMANDS SENT BY USER INTO ONE COMMAND
                                    while (toks.hasMoreTokens()) {
                                        cmdss.add(toks.nextToken());
                                    }
                                    // QUITS OUT OF A SPECIFIC POST BACK TO THE ROOM MAIN MENU
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
        // CHECKS IF USER IS ALREADY LOGGED OUT
        // IF TRUE, SUCCESS_OK IS SENT BACK AND RETURN TRUE TO CLOSE TO SERVER-CLIENT CONNECTION
        // ELSE IF FALSE, I.E. USER IS NOT LOGGED IN, ERR_FORBIDDEN IS SENT BACK
        // WITH A PROMPT TO LOG IN.
        else if(commandList.get(0).equals(LOGOUT) && loggedIn){

            //if(commandList.size()==1){

                //String userName = commandList.get(2).toString();
                //if(currentuser.getUserName().equals(userName))
                //{
                    loggedIn = false;
                    JSONArray replyArray = new JSONArray();
                    statusReply(currentuser, LOGOUT, SUCCESS_OK, pstream, replyArray, true);
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
        JSONObject statusReplyObj = currentuser.statusReplyJson(type, status);
        replyArray.add(statusReplyObj);
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
            int j = this.rooms.get(i).getIndex();
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
                // pstream.println(END);pstream.flush();
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
