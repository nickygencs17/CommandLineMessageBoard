import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by ram18 on 26/11/16.
 */
public class mainserver {

    boolean loop = true;
    ServerSocket ssock;
    public mainserver()
    {
        try {
            ssock = new ServerSocket(1235);
        }
        catch (IOException e)
        {
            System.out.println("Exception : " + e);
        }
        System.out.println("Main server Listening");
        while (true) {
            try {
                Socket sock = ssock.accept();
                System.out.println("New server thread Connected");
                new Thread(new Server(sock)).start();
            }
            catch (IOException e)
            {
                System.out.println("Exception : " + e);
            }
        }
    }
    public static void main(String args[]) throws Exception {

        new mainserver();
    }

}


