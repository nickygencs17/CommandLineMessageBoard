import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.*;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class Client {

    private static Socket socket;
    private static boolean inputChange = false;
    private static String resMessage = "";


    public static void main(String args[]) {
        boolean loop = true;
        boolean socketcreated = false;

        try {
            String sendMessage="";
            OutputStream os;
            OutputStreamWriter osw;
            BufferedWriter bw = null;
            while (loop) {
                String host = "localhost";
                int port = 1234;
                InetAddress address = InetAddress.getByName(host);
                if (!socketcreated) {
                    socket = new Socket(address, port);
                    os = socket.getOutputStream();
                    osw = new OutputStreamWriter(os);
                    bw = new BufferedWriter(osw);
                    socketcreated = true;
                }
                //Send the message to the server
                if(inputChange == false ) {
                    Scanner keyboard = new Scanner(System.in);
                    System.out.println("Enter a Command");
                    String command = keyboard.nextLine();
                    sendMessage = command + "\n";
                }
                else{
                    sendMessage = resMessage;
                }
                bw.write(sendMessage);
                bw.flush();

                System.out.println("Message sent to the server : " + sendMessage);

                //Get the return message from the server
                InputStream is = socket.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String message = "";
                String line = "";
                while ((line = br.readLine()) != null) {
                    if (line.equals("end")) {
                        break;
                    }
                    else{

                    }
                    message += line + "\n";
                }

                resMessage = readMessage(message);
                if(message.equals(resMessage)|| resMessage.equals("") || resMessage.equals("logout")){
                    inputChange = false;

                }
                else {
                    inputChange = true;
                }


                //System.out.println("Message received from the server : " );

                if (resMessage.equals("logout")) {
                    socketcreated = false;
                    socket.close();
                    loop = false;
                }
            }

        } catch (Exception exception) {
            exception.printStackTrace();
        } finally {
            //Closing the socket
            try {
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static String readMessage(String message) {
        String returnMessage = "";
        JSONParser parser = new JSONParser();
        if(message.length() !=0 ){
            System.out.println(message);
            try {
                JSONArray obj = (JSONArray) parser.parse(message);

                for(int i = 0; i < obj.size(); i++) {
                    JSONObject jsonObject = (JSONObject) obj.get(i);
                    String s = jsonObject.get("type").toString();

                    if (s.equals("rgp")) {
                        returnMessage = readPost(message);
                    } else if(s.equals("logout")) {
                        returnMessage = "logout";
                        System.out.println(jsonObject.get("message").toString());
                    } else {
                        System.out.println(jsonObject.get("message").toString());
                        returnMessage = "";
                    }
                }
            } catch (Exception v) {
                System.out.println(v);
            }
            return returnMessage;
        }
        else{
            return message;
        }
    }

    public static String readPost(String message) throws IOException {

        String returnMessage = "";
        JSONParser parser = new JSONParser();
        String group = "";
        String author = "";
        SimpleDateFormat form = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date now = Calendar.getInstance().getTime();
        String datestring = form.format(now);


        try {
            Object obj = parser.parse(message);

            JSONObject jsonObject = (JSONObject) obj;
            //group = jsonObject.get("group").toString();
            author = jsonObject.get("author").toString();

        } catch (Exception v) {
            System.out.println("read post : " + v);
        }

        BufferedReader stdin2 = new BufferedReader(new InputStreamReader(System.in));

        System.out.println("Subject:");
        String subject = "";

        Scanner keyboard = new Scanner(System.in);
        subject= keyboard.nextLine();

        JSONArray messageArray = new JSONArray();

        System.out.println("Post:");
        String TERMINATOR_STRING = ".";

        java.util.Scanner a = new java.util.Scanner(System.in);
        StringBuilder post = new StringBuilder();
        String strLine;
        while (!(strLine = a.nextLine()).equals(TERMINATOR_STRING)) {
            //strLine.concat("\n");
            if(strLine.length()==0){
                messageArray.add("\n");
            }
            messageArray.add(strLine);
        }


        JSONArray array = new JSONArray();
        array.add(author.toString());

        JSONObject sub = new JSONObject();
        sub.put("subject", subject);
        sub.put("author", author);
        sub.put("text", messageArray);
        sub.put("time", datestring);
        sub.put("viewers", array);

        returnMessage = sub.toJSONString();

        System.out.println("what i am sending back \n" +returnMessage);
        return returnMessage;

    }
}
