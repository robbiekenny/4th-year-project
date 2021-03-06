package finalproject.homesecurity.UI.Security;

import finalproject.homesecurity.Constants;
import finalproject.homesecurity.R;
import finalproject.homesecurity.SensorsActivity;
import finalproject.homesecurity.data.GlobalData;
import finalproject.homesecurity.data.Preferences;
import finalproject.homesecurity.detection.AggregateLumaMotionDetection;
import finalproject.homesecurity.detection.IMotionDetection;
import finalproject.homesecurity.detection.LumaMotionDetection;
import finalproject.homesecurity.detection.RgbMotionDetection;
import finalproject.homesecurity.image.ImageProcessing;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.atomic.AtomicBoolean;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

/*
THIS CLASS IS RESPONSIBLE FOR DETECTING MOTION
I USE THE CLASSES PROVIDED BY JUSTION WHTHERELL FOR DETECTING MOTION BUT CHANGED IT SLIGHTLY SO THAT THE DEVICE IS ONLY DETECTION MOTITON
WHEN A BOOLEAN IS SET TO TRUE AND INSTEAD OF SAVING A PHOTO WHEN MOTION DETECTION HAS BEEN TRIGGERED
I SEND IT TO MY WEB API AS A BASE64 ENCODED STRING WHERE IT WILL BE SAVED TO A BLOB AND SENT TO A USER IN AN EMAIL
 */

/***************************************************************************************
 *    Title: Android-Motion-Detection
 *    Author: Justin Wetherell
 *    Date: 17/10/2015
 *    Code version: 1
 *    Availability: https://github.com/phishman3579/android-motion-detection/tree/master/src/com/jwetherell/motion_detection
 *
 ***************************************************************************************/

public class MotionDetectionActivity extends SensorsActivity {
    private String email; //we use the users email in this activity so that we can send them an email if motion is detected
    private SharedPreferences prefs, sharedPref;
    private SharedPreferences.Editor editor;
    private static SurfaceView preview = null;
    private static SurfaceHolder previewHolder = null;
    private static Camera camera = null;
    private static boolean inPreview = false;
    private static long mReferenceTime = 0;
    private static IMotionDetection detector = null;
    private static volatile AtomicBoolean processing = new AtomicBoolean(false);
    private static boolean detectMotion = false;
    private ImageView changeCamera;
    private int cameraID = 0; //camera is initially facing back

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_activity_layout);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        changeCamera = (ImageView) findViewById(R.id.changeCamera);
        preview = (SurfaceView) findViewById(R.id.preview);
        previewHolder = preview.getHolder();
        previewHolder.addCallback(surfaceCallback);
        previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        sharedPref = getSharedPreferences("AuthenticatedUserDetails", Context.MODE_PRIVATE);
        email = sharedPref.getString("userId", null);

        //indicates whether phone is security device or personal,room name of device and the current camera in use
        prefs = this.getSharedPreferences("PhoneMode", Context.MODE_PRIVATE);
        editor = prefs.edit();
        editor.putString("DeviceMode", "Security"); //this device will be listed as security

        Intent intent = getIntent();
        String roomName = intent.getStringExtra("roomName");
        System.out.println("Room Name in Camera Activity: " + roomName);

        editor.putString("RoomName",roomName);
        editor.putInt("Camera",cameraID); //camera is the back camera initially and will only change in the chageCamera method
        editor.apply();

        if (Preferences.USE_RGB) {
            detector = new RgbMotionDetection();
        } else if (Preferences.USE_LUMA) {
            detector = new LumaMotionDetection();
        } else {
            // Using State based (aggregate map)
            detector = new AggregateLumaMotionDetection();
        }

        //if phone has only one camera, hide "switch camera" button
        if(Camera.getNumberOfCameras() == 1){
            changeCamera.setVisibility(View.INVISIBLE);
        }
    }

    public void changeCamera(View v)
    {
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
            camera = null;

            //swap the id of the camera to be used
            if (cameraID == Camera.CameraInfo.CAMERA_FACING_BACK) {
                cameraID = Camera.CameraInfo.CAMERA_FACING_FRONT;
            } else {
                cameraID = Camera.CameraInfo.CAMERA_FACING_BACK;
            }
            camera = Camera.open(cameraID);
            camera.setDisplayOrientation(90);
            try {
                //this step is critical or preview on new camera will no know where to render to
                camera.setPreviewDisplay(previewHolder);
                camera.setPreviewCallback(previewCallback);
            } catch (Exception e) {
                e.printStackTrace();
            }
            camera.startPreview();

        //save the state of the camera i.e whether its front or back facing
        //this is to allow the record video activity to know what camera to use
        editor.putInt("Camera",cameraID); //camera is the back camera initially and will only change in the changeCamera method
        editor.apply();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onPause() {
        super.onPause();

        camera.setPreviewCallback(null);
        if (inPreview) camera.stopPreview();
        inPreview = false;
        camera.release();
        camera = null;
    }

    @Override
    public void onResume() {
        super.onResume();

        try {
            camera = Camera.open(cameraID);
        }catch(Exception e)
        {
            e.printStackTrace();
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        /*
        Remove this device as a security device and set the lights boolean back to its default value of false
        Don't need to worry about removing the room name because the room name will never be used if the device isn't a security device
         */
        prefs.edit().putBoolean("lights",false).apply();
        editor.putString("DeviceMode", null);
        editor.apply();
        System.out.println("OnDestroy: " + prefs.getString("DeviceMode", null));
    }

    @Override
    public void onStop() {
        super.onStop();
        editor.putString("DeviceMode", null);
        editor.apply();
        System.out.println("OnStop: " + prefs.getString("DeviceMode", null));
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        editor.putString("DeviceMode", "Security");
        editor.apply();
        System.out.println("OnRestart: " + prefs.getString("DeviceMode",null));
    }

    /*
                METHODS ACCESSED BY PUBNUB MESSAGING CLASS
                 */
    public static void setDetectMotion(boolean motion)
    {
        detectMotion = motion;
    }

    public static boolean getMotionDetection(){return detectMotion;}

    public static Camera getCamera() {
        return camera;
    }

    public static void setCamera(Camera camera) {
        MotionDetectionActivity.camera = camera;
    }


    private PreviewCallback previewCallback = new PreviewCallback() {

        /**
         * {@inheritDoc}
         */
        @Override
        public void onPreviewFrame(byte[] data, Camera cam) {
            //my idea here is to allow a user to select when the motion detection starts
            if(detectMotion) //if detect motion is true then allow this device to detect motion other wise do nothing
            {

                if (data == null) return;
                Camera.Size size = cam.getParameters().getPreviewSize();
                if (size == null) return;

                if (!GlobalData.isPhoneInMotion()) {
                    DetectionThread thread = new DetectionThread(data, size.width, size.height,email);
                    thread.start();
                }

            }
            else {
                //System.out.println("MOTION DETECTION IS DISABLED");
            }
        }
    };

    private SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {

        /**
         * {@inheritDoc}
         */
        @Override
        public void surfaceCreated(SurfaceHolder holder) {

            try {
                camera.setPreviewDisplay(previewHolder);
                camera.setDisplayOrientation(90);
                camera.setPreviewCallback(previewCallback);
            } catch (Throwable t) {
                System.out.println("Exception in setPreviewDisplay()");
            }
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            if(camera != null) {
                Camera.Parameters parameters = camera.getParameters();
                Camera.Size size = getBestPreviewSize(width, height, parameters);
                if (size != null) {
                    parameters.setPreviewSize(size.width, size.height);
                    Log.d("MotionDetectionActivity", "Using width=" + size.width + " height=" + size.height);
                }
                camera.setParameters(parameters);
                camera.startPreview();
                inPreview = true;
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            // Ignore
        }
    };

    private static Camera.Size getBestPreviewSize(int width, int height, Camera.Parameters parameters) {
        Camera.Size result = null;

        for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
            if (size.width <= width && size.height <= height) {
                if (result == null) {
                    result = size;
                } else {
                    int resultArea = result.width * result.height;
                    int newArea = size.width * size.height;

                    if (newArea > resultArea) result = size;
                }
            }
        }

        return result;
    }

    private static final class DetectionThread extends Thread {

        private byte[] data;
        private int width,height;
        private String email;

        public DetectionThread(byte[] data, int width, int height,String email) {
            this.data = data;
            this.width = width;
            this.height = height;
            this.email = email;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void run() {
            if (!processing.compareAndSet(false, true)) return;


            try {
                // Previous frame
                int[] pre = null;
                if (Preferences.SAVE_PREVIOUS) pre = detector.getPrevious();

                // Current frame (with changes)

                int[] img = null;
                if (Preferences.USE_RGB) {
                    img = ImageProcessing.decodeYUV420SPtoRGB(data, width, height);
                } else {
                    img = ImageProcessing.decodeYUV420SPtoLuma(data, width, height);
                }

                // Current frame (without changes)
                int[] org = null;
                if (Preferences.SAVE_ORIGINAL && img != null) org = img.clone();

                if (img != null && detector.detect(img, width, height)) {
                    // The delay is necessary to avoid taking a picture while in
                    // the
                    // middle of taking another. This problem can causes some
                    // phones
                    // to reboot.
                    long now = System.currentTimeMillis();
                    if (now > (mReferenceTime + Preferences.PICTURE_DELAY)) {
                        mReferenceTime = now;

                        Bitmap previous = null;
                        if (Preferences.SAVE_PREVIOUS && pre != null) {
                            if (Preferences.USE_RGB)
                                previous = ImageProcessing.rgbToBitmap(pre, width, height);
                            else previous = ImageProcessing.lumaToGreyscale(pre, width, height);
                        }

                        Bitmap original = null;
                        if (Preferences.SAVE_ORIGINAL && org != null) {
                            if (Preferences.USE_RGB)
                                original = ImageProcessing.rgbToBitmap(org, width, height);
                            else original = ImageProcessing.lumaToGreyscale(org, width, height);
                        }

                        Bitmap bitmap = null;
                        if (Preferences.SAVE_CHANGES) {
                            if (Preferences.USE_RGB)
                                bitmap = ImageProcessing.rgbToBitmap(img, width, height);
                            else bitmap = ImageProcessing.lumaToGreyscale(img, width, height);
                        }

                        Log.i("MotionDetectionActivity", "Saving.. previous=" + previous + " original=" + original + " bitmap=" + bitmap);
                        Looper.prepare();
                        System.out.println("SENDING IMAGE TO USER");
                        /*
                        MOTION HAS BEEN DETECTED AT THIS POINT
                        WE NOW HAVE AN IMAGE SHOWING WHAT HAS TRIGGERED THE MOTION DETECTION
                        SEND THIS TO A USER
                         */

                        new SendPhotoToUser(email).execute(original);
                    } else {
                        Log.i("MotionDetectionActivity", "Not taking picture because not enough time has passed since the creation of the Surface");
                    }
                }
            } catch (Exception e) {
                Log.i("MotionDetectionActivity", "FAILED TO PROCESS IMAGE");
                e.printStackTrace();
            } finally {
                processing.set(false);
            }
            processing.set(false);
        }
    }

    /***************************************************************************************
     *    Title: From Bitmap To Base64 And Back With Native Android
     *    Author: Nic Raboy
     *    Date: 10/2/2016
     *    Code version: 1
     *    Availability: https://blog.nraboy.com/2015/06/from-bitmap-to-base64-and-back-with-native-android/
     *
     ***************************************************************************************/

    //ASYNCHRONOUSLY CONVERT THE IMAGE TO A BASE64 STRING AND SEND THIS STRING IN THE BODY OF A POST REQUEST
    private static final class SendPhotoToUser extends AsyncTask<Bitmap, Void, Void> {
        private String user;

        public SendPhotoToUser(String email)
        {
            user = email;
        }

        @Override
        protected Void doInBackground(Bitmap... data) {
                Bitmap bitmap = data[0];
                if (bitmap != null) convertImageToBase64String(bitmap);
            return null;
        }

        private void convertImageToBase64String(Bitmap bitmap) { //encode bitmap to string
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream .toByteArray();
            sendPhoto(Base64.encodeToString(byteArray, Base64.DEFAULT));
        }

        private void sendPhoto(final String b64String) { //send the encoded string to the web api to process
            try {
                System.out.println("SENDING PHOTO");
                HttpPost request = new HttpPost(Constants.UPLOADIMAGE_ENDPOINT + "?email=" + user);
                request.addHeader("ZUMO-API-VERSION","2.0.0" );
                request.addHeader("Content-Type", "application/json");
                request.setEntity(new StringEntity("\"" + b64String + "\"")); //encoded image passed to the controller
                                                                                // via the body of the request
                HttpResponse response = new DefaultHttpClient().execute(request);

                if (response.getStatusLine().getStatusCode() != HttpStatus.SC_CREATED) {
                    Log.e("Sending Photo Error",
                            response.getStatusLine().toString());

                }
                else
                    Log.i("Photo sent","Image was sent to the API successfully");
            } catch (Exception e) {
                Log.e("Sending photo Error2", e.getMessage());
            }
        }
    }
}