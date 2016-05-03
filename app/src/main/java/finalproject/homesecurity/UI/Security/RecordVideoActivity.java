package finalproject.homesecurity.UI.Security;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.WindowManager;
import android.widget.FrameLayout;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import finalproject.homesecurity.CameraPreview;
import finalproject.homesecurity.Constants;
import finalproject.homesecurity.R;

/**
 * Created by Robbie on 12/02/2016.
 *
 * THIS CLASS IS STARTED WHEN A USER SELECTS THE TAKE VIDEO OPTION FROM THE CONTROLS PANEL FOR A DEVICE
 * A 30 SECOND LIMIT IS SET ON THE MEDIARECORDER AND THE VIDEO IS SAVED TO THE DEVICE TEMPORARILY
 * ONCE THE VIDEO IS FINISHED RECORDING WE WILL ATTEMPT TO CONVERT THE VIDEO TO A STREAM OF BYTES AND POST THIS STREAM TO OUR WEB API
 * WHERE IT CAN BE SAVED TO AZURE MEDIA SERVICES AND PROCESSED FOR STREAMING
 * ONCE THE VIDEO HAS BEEN SENT TO THE WEB API IT IS DELETED ON THE DEVICE TO SAVE MEMORY
 */

/***************************************************************************************
 *    Title: Saving Files
 *    Author: Android Developers
 *    Date: 12/2/2016
 *    Code version: 1
 *    Availability: http://developer.android.com/training/basics/data-storage/files.html
 *
 ***************************************************************************************/
/***************************************************************************************
 *    Title: Camera
 *    Author: Android Developers
 *    Date: 12/2/2016
 *    Code version: 1
 *    Availability: http://developer.android.com/guide/topics/media/camera.html
 *
 ***************************************************************************************/
public class RecordVideoActivity extends Activity implements MediaRecorder.OnInfoListener {
    private final String TAG = "RECORDVIDEO";
    private Camera mCamera;
    private CameraPreview mPreview;
    private MediaRecorder mMediaRecorder;
    private MyFileObserver fb;
    private String filePath = "";
    private SharedPreferences sharedPref,settings;
    private String roomName;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.record_video_layout);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        sharedPref = getSharedPreferences("AuthenticatedUserDetails", Context.MODE_PRIVATE);
        settings = getSharedPreferences("PhoneMode", Context.MODE_PRIVATE);
        String[] room_name = settings.getString("RoomName",null).split("@");
        roomName = room_name[0];
        // Create an instance of Camera
        mCamera = getCameraInstance();

        if (mCamera != null) {
            // Create our Preview view and set it as the content of our activity.
            mPreview = new CameraPreview(this, mCamera);

            FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
            preview.addView(mPreview);


            // initialize video camera
            //delay is necessary to avoid crash because preview layout will not be ready
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    if (prepareVideoRecorder()) {
                        // Camera is available and unlocked, MediaRecorder is prepared,
                        // now you can start recording
                        mMediaRecorder.start();

                        //if the flashlight has been enabled then we will try to turn it on
                        //the default value of the lights boolean is false
                        System.out.println("FLASH " + settings.getBoolean("lights",false));
                        if(settings.getBoolean("lights",false))
                        {
                            enableFlashlight();
                        }

                        System.out.println("START RECORDING");
                        fb = new MyFileObserver(filePath, FileObserver.CLOSE_WRITE);
                        fb.startWatching();
                    } else {
                        // prepare didn't work, release the camera
                        System.out.println("NOT RECORDING");
                        releaseMediaRecorder();
                        // inform user
                    }
                }
            }, 1000);
        } else {

            Log.i(TAG,"UNABLE TO GET CAMERA");
        }

    }

    private void enableFlashlight() {
        System.out.println("FLASH");
        Camera.Parameters p = mCamera.getParameters();
        p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        mCamera.setParameters(p);
    }

    /**
     * A safe way to get an instance of the Camera object.
     * WE MUST ALSO TAKE INTO ACCOUNT THE CAMERA THAT THE USER HAS DECIDED TO USE FOR THIS DEVICE I.E FRONT OR BACK
     */
    public Camera getCameraInstance() {
        Camera c = null;
        try {
            // attempt to get a Camera instance using the shared preferences that saves which camera the user was using.
            // 0 will be used as the default
            c = Camera.open(settings.getInt("Camera",0));
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    private boolean prepareVideoRecorder() {
        System.out.println("PREPARING RECORDER");

        mMediaRecorder = new MediaRecorder();

        // Step 1: Unlock and set camera to MediaRecorder
        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);

        // Step 2: Set sources
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
        mMediaRecorder.setProfile(CamcorderProfile.get(settings.getInt("Camera",0),CamcorderProfile.QUALITY_HIGH));

        // Step 4: Set output file
        mMediaRecorder.setOutputFile(getOutputMediaFile().toString());

        //Max duration of 30 seconds
        mMediaRecorder.setMaxDuration(30000);
        mMediaRecorder.setOnInfoListener(this);

        // Step 5: Set the preview output
        mMediaRecorder.setPreviewDisplay(mPreview.getHolder().getSurface());

        // Step 6: Prepare configured MediaRecorder
        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseMediaRecorder();       // if you are using MediaRecorder, release it first
        releaseCamera();              // release the camera immediately on pause event
    }

    private void releaseMediaRecorder() {
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();   // clear recorder configuration
            mMediaRecorder.release(); // release the recorder object
            mMediaRecorder = null;
            mCamera.lock();           // lock camera for later use
        }
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }

    /* Checks if external storage is available*/
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /**
     * Create a File for saving a video
     */
    private File getOutputMediaFile() {
        System.out.println("CREATING FILE");
        File mediaFile = null;
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        /*
        TRY TO SAVE THE FILE TO PUBLIC EXTERNAL STORAGE (E.G SD CARD) FIRST
        IF THAT FAILS THEN TRY TO SAVE THE FILE TO INTERNAL STORAGE
         */
        if (isExternalStorageWritable()) {

            try {
                File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_MOVIES), "HomeSecurity");

                // Create the storage directory if it does not exist
                if (!mediaStorageDir.exists()) {
                    if (!mediaStorageDir.mkdirs()) {
                        Log.d("HomeSecurity", "failed to create directory");
                        return null;
                    }
                }

                // Create a media file name
                mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                        "VID_" + timeStamp + ".mp4");

                System.out.println("FILE CREATED IN EXTERNAL STORAGE");

                filePath = mediaStorageDir.getPath() + File.separator +
                        "VID_" + timeStamp + ".mp4";
            } catch (Exception e) {
                e.printStackTrace();
                Log.i("RECORDVIDEOACTIVITY", "Unable to create video");
                try {
                    //TRY CREATE THE VIDEO IN INTERNAL STORAGE AS EXTERNAL STORAGE MAY NOT BE MOUNTED OR NOT ENOUGH SPACE
                    mediaFile = new File(getApplicationContext().getFilesDir() + File.separator +
                            "VID_" + timeStamp + ".mp4");
                    filePath = getApplicationContext().getFilesDir() + File.separator +
                            "VID_" + timeStamp + ".mp4";
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

        } else //INTERNAL STORAGE
        {
            try {
                mediaFile = new File(getApplicationContext().getFilesDir() + File.separator +
                        "VID_" + timeStamp + ".mp4");
                filePath = getApplicationContext().getFilesDir() + File.separator +
                        "VID_" + timeStamp + ".mp4";
                System.out.println("FILE CREATED IN INTERNAL STORAGE");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return mediaFile;
    }

    @Override
    public void onInfo(MediaRecorder mr, int what, int extra) {
        if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
            Log.i(TAG, "Maximum Duration Reached");
            mr.stop();
           releaseMediaRecorder();
            mCamera.lock();
            fb.stopWatching();
        }
    }

    private class UploadVideoTask extends AsyncTask<String, Void, Void> {

        protected Void doInBackground(String... filePath) {

            uploadFile(filePath[0]);
            return null;
        }

        protected void onPostExecute(Void result) {
            File file = new File(filePath);
            boolean deleted = file.delete();
            if (deleted)
                System.out.println("DELETED");
            finish();
        }

        /***************************************************************************************
         *    Title: Upload large file in Android without outofmemory error
         *    Author: Andro Selva
         *    Date: 12/1/2016
         *    Code version: 1
         *    Availability: http://stackoverflow.com/questions/9630430/upload-large-file-in-android-without-outofmemory-error
         *
         ***************************************************************************************/

        public void uploadFile(String filePath) {
            Log.i("Image filename", filePath);
            Log.i("url", Constants.UPLOADVIDEO_ENDPOINT);
            HttpURLConnection connection = null;
            DataOutputStream outputStream = null;

            String urlServer = Constants.UPLOADVIDEO_ENDPOINT;
            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary = "*****";

            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            int maxBufferSize = 1 * 1024;
            try {
                FileInputStream fileInputStream = new FileInputStream(new File(
                        filePath));

                URL url = new URL(urlServer + "?email=" + sharedPref.getString("userId",null) + "&roomName=" + roomName);
                connection = (HttpURLConnection) url.openConnection();

                // Allow Inputs & Outputs
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setUseCaches(false);
                connection.setChunkedStreamingMode(1024);
                // Enable POST method
                connection.setRequestMethod("POST");

                connection.setRequestProperty("Connection", "Keep-Alive");
                connection.setRequestProperty("Content-Type",
                        "multipart/form-data;boundary=" + boundary);
                connection.setRequestProperty("ZUMO-API-VERSION", "2.0.0");

                outputStream = new DataOutputStream(connection.getOutputStream());
                outputStream.writeBytes(twoHyphens + boundary + lineEnd);

                String connstr = null;
                connstr = "Content-Disposition: form-data; name=\"uploadedfile\";filename=\""
                        + filePath + "\"" + lineEnd;

                outputStream.writeBytes(connstr);
                outputStream.writeBytes(lineEnd);

                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                // Read file
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                Log.e("Image length", bytesAvailable + "");
                try {
                    while (bytesRead > 0) {
                        try {
                            outputStream.write(buffer, 0, bufferSize);
                        } catch (OutOfMemoryError e) {
                            e.printStackTrace();
                        }
                        bytesAvailable = fileInputStream.available();
                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                        bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                outputStream.writeBytes(lineEnd);
                outputStream.writeBytes(twoHyphens + boundary + twoHyphens
                        + lineEnd);

                Log.i("Server Response Code ", "" + connection.getResponseCode());

//                if (serverResponseCode == 200) {
//
//                }

                fileInputStream.close();
                outputStream.flush();
                outputStream.close();
                outputStream = null;
            } catch (Exception ex) {
                // Exception handling
                Log.e("Send file Exception", ex.getMessage() + "");
                ex.printStackTrace();
            }
        }
    }

    /**
     * THIS CLASS WILL BE USED TO CHECK WHEN A VIDEO RECORDING HAS STOPPED AND THE FILE IS READY TO USE
     */

    /***************************************************************************************
     *    Title: How to know when MediaRecorder has finished writing data to file
     *    Author: userSeven7s
     *    Date: 15/3/2016
     *    Code version: 1
     *    Availability: http://stackoverflow.com/questions/7418446/how-to-know-when-mediarecorder-has-finished-writing-data-to-file
     *
     ***************************************************************************************/
    private class MyFileObserver extends FileObserver {
        public MyFileObserver(String path, int mask) {
            super(path, mask);
        }

        @Override
        public void onEvent(int event, String path) {
            if(event == FileObserver.CLOSE_WRITE)
            {
                new UploadVideoTask().execute(filePath); //upload video
            }

        }
    }

}
