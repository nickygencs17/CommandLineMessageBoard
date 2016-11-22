/**
 * Created by nicholasgenco on 11/21/16.
 */
public class Room {



        private String roomName;     // first name
        private String roomFile;      // last name
        private int index;     // email address


        // construct a new student with given fields
        public Room(String RoomName, String RoomFile, int Index) {
            this.roomName  =  RoomName;
            this.roomFile  = RoomFile;
            this.index   = Index;
        }

    public int getIndex() {
        return index;
    }

    public String getRoomFile() {
        return roomFile;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomFile(String roomFile) {
        this.roomFile = roomFile;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public void setSubscribed(int Index) {
        this.index = Index;
    }
}
