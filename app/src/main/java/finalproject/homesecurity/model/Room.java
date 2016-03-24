package finalproject.homesecurity.model;

/**
 * Created by Robbie on 04/11/2015.
 */
public class Room {
    private String roomName;
    private boolean motionDetection,lights,takingVideo;

    public Room(String name)
    {
        roomName = name;
        motionDetection = false;
        lights = false;
        takingVideo = false;
    }

    public String getRoomName() {
        return roomName;
    }

    public boolean isMotionDetection() {
        return motionDetection;
    }

    public void setMotionDetection(boolean motionDetection) {
        this.motionDetection = motionDetection;
    }

    public boolean isLights() {
        return lights;
    }

    public void setLights(boolean lights) {
        this.lights = lights;
    }

    public boolean isTakingVideo() {
        return takingVideo;
    }

    public void setTakingVideo(boolean takingVideo) {
        this.takingVideo = takingVideo;
    }
}
