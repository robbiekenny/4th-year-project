package finalproject.homesecurity.model;

/**
 * Created by Robbie on 04/11/2015.
 */
public class Room {
    private String roomName;

    public Room(String name)
    {
        roomName = name;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }
}
