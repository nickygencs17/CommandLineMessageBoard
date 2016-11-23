import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * Created by nicholasgenco on 11/22/16.
 */
public class User {

    private String userName;
    private ArrayList<Integer> subscriptions;
    private String fileName;



    public User(String Username, String fn, ArrayList<Integer> Subs){
        this.userName = Username;
        this.subscriptions = Subs;
        this.fileName = fn;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }

    public ArrayList<Integer> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(ArrayList<Integer> subscriptions) {
        this.subscriptions = subscriptions;
    }
}
