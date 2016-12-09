/**
 * Created by ram18 on 08/12/16.
 */
public class post {
    private int id;
    private int read;
    private String message;
    private String time;
    private String subject;
    private String author;

    public post(int id, String m, String t, String sub, String author, int read){
        this.message = m;
        this.time = t;
        this.subject = sub;
        this.author = author;
        this.id = id;
        this.read = read;
    }

    public void setpostid(int id) {
        this.id = id;
    }
    public String getmessage() {
        return message;
    }
    public String gettime() {
        return time;
    }
    public boolean isread() {
        if(read == 1) {
            return true;
        }
        else {
            return false;
        }
    }
}
