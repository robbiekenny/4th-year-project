package finalproject.homesecurity.model;

import java.util.Date;

/**
 * Created by Robbie on 01/04/2016.
 */
public class Video {
    private String video;
    private String roomName;
    private String createdAt;

    public Video(String video, String roomName, String createdAt) {
        this.video = video;
        this.roomName = roomName;
        this.createdAt = createdAt;
    }

    public String getVideo() {
        return video;
    }

    public void setVideo(String video) {
        this.video = video;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
