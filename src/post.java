/**
 * Created by ram18 on 08/12/16.
 */
public class post {
    private int id;
    private String message;
    private String time;
    private String subject;
    private String author;

    public post(String m, String t, String sub, String author){
        this.message = m;
        this.time = t;
        this.subject = sub;
        this.author = author;
    }

    public void setpostid(int id) {
        this.id = id;
    }
}
