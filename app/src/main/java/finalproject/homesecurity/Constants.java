package finalproject.homesecurity;

/**
 * Created by Robbie on 04/11/2015.
 * CONTAINS THE URLS FOR THE SEND EMAIL AND UPLOAD VIDEO FUNCTIONALITY AS WELL AS THE APPLICATION
 * KEY REQUIRED FOR A MOBILE SERVICE WHICH AT THIS STAGE IS LEGACY
 */
public class Constants {
    public static final String APPLICATION_KEY = "UBsAzsgZxLVxorsZApsOXwAzHYdKTu35";
    public static final String UPLOADIMAGE_ENDPOINT = "http://homesecurityservice.azurewebsites.net/api/SendEmail";
    public static final String UPLOADVIDEO_ENDPOINT = "http://homesecurityservice.azurewebsites.net/api/UploadVideo";
    //public static final String SENDER_ID = "1017315118157"; //was used for GCM
}
