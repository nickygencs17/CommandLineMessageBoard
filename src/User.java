import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.*;
import java.util.StringTokenizer;

/**
 * Created by nicholasgenco on 11/22/16.
 */
public class
User {

    private String userName;
    private ArrayList<String> subscriptions;
    private String fileName;



    public User(String Username, ArrayList<String> Subs){
        this.userName = Username;
        this.subscriptions = Subs;
        //this.fileName = fn;
    }

    public String getFileName() {
        return fileName;
    }

    //public void setFileName(String fileName) {this.fileName = fileName;}

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }

    public ArrayList<String> getSubscriptions() {
        return subscriptions;
    }
    public boolean subscribegroup(int index) {
        if(!isroomsubscribed(index)) {
            subscriptions.add(subscriptions.size(), Integer.toString(index));
            updatejsonusers(index,"add");
            return true;
        }
        return false;
    }
    public void unsubscribegroupwithindex(int i) {
            int index = Integer.parseInt(subscriptions.get(i));
            updatejsonusers(index,"remove");
    }
    public boolean unsubscribegroup(int index) {
        if(isroomsubscribed(index)) {
            subscriptions.remove(Integer.toString(index));
            updatejsonusers(index,"remove");
            return true;
        }
        return false;
    }
    public boolean checksubscribedbyname (String s) {
        ArrayList<post> posts = new ArrayList<>();
        try {
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(new FileReader("./JSONData/ag.json"));
            JSONObject jsonObject = (JSONObject) obj;
            JSONArray arr = (JSONArray) jsonObject.get("ag");
            for (int i = 0; i < arr.size(); i++) {
                JSONObject j = (JSONObject) arr.get(i);
                if (j.get("roomName").toString().equals(s)) {
                    return true;
                }
            }
        }
        catch (Exception v){
            System.out.println("Getting unread posts " + v);
        }
        return false;
    }
    public void updatejsonusers(int index, String option) {
        try {
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(new FileReader("./JSONData/users.json"));
            JSONObject jsonObject = (JSONObject) obj;
            JSONArray arr = (JSONArray) jsonObject.get("existingUsers");
            for (int i = 0; i < arr.size(); i++)
            {
                JSONObject j = (JSONObject) arr.get(i);
                String un = (String) j.get("userName");
                if(un.equals(this.getUserName())) {
                    JSONArray array = (JSONArray) j.get("subscriptions");

                    if(option.equals("add")) {
                        JSONObject sub = new JSONObject();
                        sub.put("index", Integer.toString(index));
                        sub.put("time", "");
                        array.add(sub);
                    }
                    if(option.equals("remove")) {

                        array.remove(getindex(array, Integer.toString(index)));
                    }
                    try {
                        FileWriter files = new FileWriter("./JSONData/users.json");
                        files.write(jsonObject.toJSONString());
                        files.flush();
                        files.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        catch (Exception v){
            System.out.println("Group Subscription " + v);
        }
    }

    public void setSubscriptions(ArrayList<String> subscriptions) {
        this.subscriptions = subscriptions;
    }
    boolean isroomsubscribed(int j)
    {
        for(int i = 0; i<subscriptions.size(); i++)
        {
            if(Integer.parseInt(subscriptions.get(i))== j)
            {
                return true;
            }
        }
        return false;
    }
    public int getindex(JSONArray arr, String s) {
        for(int i=0; i<arr.size();i++) {
            JSONObject temp = (JSONObject) arr.get(i);
            if(temp.get("index").equals(s)) {
                return i;
            }
        }
        return -1;
    }
    public Date getlastaccessed(String jk) {
        try {
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(new FileReader("./JSONData/users.json"));
            JSONObject jsonObject = (JSONObject) obj;
            JSONArray arr = (JSONArray) jsonObject.get("existingUsers");
            for (int i = 0; i < arr.size(); i++)
            {
                JSONObject j = (JSONObject) arr.get(i);
                String un = (String) j.get("userName");
                if(un.equals(this.getUserName())) {
                    JSONArray array = (JSONArray) j.get("subscriptions");
                    for(int k = 0;k<array.size();k++) {
                        JSONObject thissub = (JSONObject) array.get(k);
                        if(thissub.get("index").toString().equals(jk)) {
                            String thistime = thissub.get("time").toString();
                            SimpleDateFormat form = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                            Date texttime = form.parse(thistime);
                            return texttime;
                        }
                    }
                break;
                }
            }

        }
        catch (Exception v){
            System.out.println("Updating json " + v);
        }
        return null;
    }
    public void updategrouptime(String jk) {
        try {
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(new FileReader("./JSONData/users.json"));
            JSONObject jsonObject = (JSONObject) obj;
            JSONArray arr = (JSONArray) jsonObject.get("existingUsers");
            for (int i = 0; i < arr.size(); i++)
            {
                JSONObject j = (JSONObject) arr.get(i);
                String un = (String) j.get("userName");
                if(un.equals(this.getUserName())) {
                    JSONArray array = (JSONArray) j.get("subscriptions");
                    for(int k = 0;k<array.size();k++) {
                        JSONObject thissub = (JSONObject) array.get(k);
                        if(thissub.get("index").toString().equals(jk)) {
                            SimpleDateFormat form = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                            Date now = Calendar.getInstance().getTime();
                            String datestring = form.format(now);
                            array.remove(thissub);
                            JSONObject sub = new JSONObject();
                            sub.put("index", jk);
                            sub.put("time", datestring);
                            array.add(sub);
                        }
                    }
                    try {
                        FileWriter files = new FileWriter("./JSONData/users.json");
                        files.write(jsonObject.toJSONString());
                        files.flush();
                        files.close();

                    } catch (IOException e) {
                        System.out.println(e);
                    }
                    break;
                }
            }

        }
        catch (Exception v){
            System.out.println("Updating group time " + v);
        }
    }
    public int numtextsaftertime(String index, Date dt) {
        int count = 0;
        try {
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(new FileReader("./JSONData/ag.json"));
            JSONObject jsonObject = (JSONObject) obj;
            JSONArray arr = (JSONArray) jsonObject.get("ag");
            JSONObject j = (JSONObject) arr.get(Integer.parseInt(index));
            String filename = (String) j.get("roomFile");
            JSONParser parser1 = new JSONParser();
            Object obj1 = parser1.parse(new FileReader(filename));
            JSONObject jsonObject1 = (JSONObject) obj1;
            JSONArray texts = (JSONArray) jsonObject1.get("messages");
            for (int i = 0; i < texts.size(); i++)
            {
                JSONObject thistext = (JSONObject) texts.get(i);
                String time = (String) thistext.get("time");
                SimpleDateFormat form = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                Date texttime = form.parse(time);
                if(dt.compareTo(texttime) < 0){
                    count++;
                }
            }
        }
        catch (Exception v){
            System.out.println("number of texts" + v);
        }

        return count;
    }

    public JSONObject createreplyjson(String cmd, String text, String group, String author) {
        JSONObject sub = new JSONObject();
        try {
            if(cmd.equals("ag") || cmd.equals("sg") || cmd.equals("rg")) {
                sub.put("type", cmd);
                sub.put("message", text);
                return sub;
            }
            if(cmd.equals("rgp")) {
                sub.put("type", cmd);
                sub.put("author", author);
                sub.put("group", group);
                return sub;
            }
        }
        catch (Exception v){
            System.out.println("Creating message " + v);
        }
        return sub;
    }

    public JSONObject statusReplyJson(String cmd, String status) {
        JSONObject obj = new JSONObject();
        try {
            obj.put("type", cmd);
            obj.put("message", status);
            return obj;
        }
        catch(Exception e) {
            System.out.println("Creating status message " + e);
        }
        return obj;
    }

    public ArrayList <post> getunreadpostsfromgroup (String group) {
        ArrayList<post> posts = new ArrayList<>();
        try {
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(new FileReader("JSONData/rooms/" + group));
            JSONObject jsonObject = (JSONObject) obj;
            JSONArray arr = (JSONArray) jsonObject.get("messages");
            for (int i = arr.size() - 1; i >= 0; i--)
            {
                JSONObject singlemesg = (JSONObject)arr.get(i);
                JSONArray viewed = (JSONArray) singlemesg.get("viewed");
                int exists = 0;
                for(int jk = 0; jk < viewed.size(); jk++) {
                    if(viewed.get(jk).toString().equals(userName)) {
                        exists = 1;
                        break;
                    }
                }
                if(exists == 0) {
                    ArrayList<String> postarray = new ArrayList<>();
                    JSONArray texts = (JSONArray) singlemesg.get("text");
                    for(int q = 0; q < texts.size(); q++) {
                        postarray.add(texts.get(q).toString());
                    }
                    posts.add(new post(Integer.parseInt(singlemesg.get("id").toString()),
                            postarray,
                            singlemesg.get("time").toString(),
                            singlemesg.get("subject").toString(),
                            singlemesg.get("author").toString(), 0));
                }
            }
        }
        catch (Exception v){
            System.out.println("Getting unread posts " + v);
        }
        return posts;
    }
    public ArrayList <post> getreadpostsfromgroup (String group, ArrayList<post> posts) {
        try {
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(new FileReader("JSONData/rooms/" + group));
            JSONObject jsonObject = (JSONObject) obj;
            JSONArray arr = (JSONArray) jsonObject.get("messages");
            for (int i = arr.size() - 1; i >= 0; i--)
            {
                JSONObject singlemesg = (JSONObject)arr.get(i);
                JSONArray viewed = (JSONArray) singlemesg.get("viewed");
                int exists = 0;
                for(int jk = 0; jk < viewed.size(); jk++) {
                    if(viewed.get(jk).toString().equals(userName)) {
                        exists = 1;
                        break;
                    }
                }
                if(exists == 1) {
                    ArrayList<String> postarray = new ArrayList<>();
                    JSONArray texts = (JSONArray) singlemesg.get("text");
                    for(int q = 0; q < texts.size(); q++) {
                        postarray.add(texts.get(q).toString());
                    }
                    posts.add(new post(Integer.parseInt(singlemesg.get("id").toString()),
                            postarray,
                            singlemesg.get("time").toString(),
                            singlemesg.get("subject").toString(),
                            singlemesg.get("author").toString(), 1));
                }
            }
        }
        catch (Exception v){
            System.out.println("Getting read posts " + v);
        }
        return posts;
    }
    public void markpostasread (int ind, String group) {
        try {
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(new FileReader("JSONData/rooms/" + group));
            JSONObject jsonObject = (JSONObject) obj;
            JSONArray arr = (JSONArray) jsonObject.get("messages");
            for (int i = arr.size() - 1; i >= 0; i--)
            {
                JSONObject singlemesg = (JSONObject)arr.get(i);
                if(singlemesg.get("id").toString().equals(Integer.toString(ind))) {
                    JSONArray viewed = (JSONArray) singlemesg.get("viewed");
                    viewed.add(userName);
                    break;
                }
            }
            try {
                FileWriter files = new FileWriter("JSONData/rooms/" + group);
                files.write(jsonObject.toJSONString());
                files.flush();
                files.close();

            } catch (IOException e) {
                System.out.println("writing to json" + e);
            }

        }
        catch (Exception v){
            System.out.println("Marking read posts " + v);
        }
    }

    public void addpostogroup (String mesg, String group) {
        try {
            JSONObject ob = (JSONObject) new JSONParser().parse(mesg);
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(new FileReader("JSONData/rooms/" + group));
            JSONObject jsonObject = (JSONObject) obj;
            JSONArray arr = (JSONArray) jsonObject.get("messages");
            arr.add(ob);

            try {
                FileWriter files = new FileWriter("JSONData/rooms/" + group);
                files.write(jsonObject.toJSONString());
                files.flush();
                files.close();

            } catch (IOException e) {
                System.out.println("adding post to json" + e);
            }

        }
        catch (Exception v){
            System.out.println("adding post to json catch " + v);
        }
    }
}
