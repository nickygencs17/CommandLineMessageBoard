import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;
import java.util.StringTokenizer;

public class Client
{

    private static Socket socket;


    public static void main(String args[])
    {
        boolean loop = true;
        boolean socketcreated = false;

        try
        {
            OutputStream os;
            OutputStreamWriter osw;
            BufferedWriter bw = null;
            while(loop) {
                String host = "localhost";
                int port = 1234;
                InetAddress address = InetAddress.getByName(host);
                if(!socketcreated) {
                    socket = new Socket(address, port);
                    os = socket.getOutputStream();
                    osw = new OutputStreamWriter(os);
                    bw = new BufferedWriter(osw);
                    socketcreated = true;
                }
                //Send the message to the server
                Scanner keyboard = new Scanner(System.in);
                System.out.println("Enter an Command");
                String command = keyboard.nextLine();
                String sendMessage = command + "\n";
                bw.write(sendMessage);
                bw.flush();
                System.out.println("Message sent to the server : " + sendMessage);

                //Get the return message from the server
                InputStream is = socket.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String message ="";
                String line = "";
                while((line = br.readLine()) != null)
                {
                    if(line.equals("end"))
                    {
                        break;
                    }
                    if(line.equals("logout")) { socketcreated = false; socket.close();}
                    message += line + "\n";
                }
                System.out.println("Message received from the server : " + message);
                if(message.equals("logout")){
                    loop = false;
                }
            }

        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
        finally
        {
            //Closing the socket
            try
            {
                socket.close();
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
    }

}
