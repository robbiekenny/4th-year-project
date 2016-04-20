package finalproject.homesecurity.model;

/**
 * Created by Robbie on 04/11/2015.
 */
public class Room {
    private String roomName;
    private boolean motionDetection,lights,takingVideo;
    private int batteryLife;

    public Room(String name,String battery)
    {
        roomName = name;
        motionDetection = false;
        lights = false;
        takingVideo = false;

        //battery life will be a string in the range from 0 -1
        //to display to the user I will convert battery to an int and multiply it by 100 so battery life will be in the range 0 -100
        batteryLife = (int) (Double.parseDouble(battery) * 100);
    }

    public int getBatteryLife() {
        return batteryLife;
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
