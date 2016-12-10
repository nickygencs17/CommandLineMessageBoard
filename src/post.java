import java.util.ArrayList;

/**
 * Created by ram18 on 08/12/16.
 */
public class post {
    private int id;
    private int read;
    private ArrayList<String> messages;
    private String time;
    private String subject;
    private String author;

    public post(int id, ArrayList<String> m, String t, String sub, String author, int read){
        this.messages = m;
        this.time = t;
        this.subject = sub;
        this.author = author;
        this.id = id;
        this.read = read;
    }

    public int getpostid() {
        return id;
    }
    public String getmessagewithindex(int t) {
        return messages.get(t);
    }
    public ArrayList<String> getmessage() {
        return messages;
    }
    public String gettime() {
        return time;
    }
    public String getsubject() {
        return subject;
    }
    public boolean isread() {
        if(read == 1) {
            return true;
        }
        else {
            return false;
        }
    }

    public String getAuthor() {
        return author;
    }
}
