package finalproject.homesecurity;

import android.app.Activity;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.provider.SyncStateContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.android.internal.http.multipart.MultipartEntity;
import com.android.internal.http.multipart.Part;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.mime.HttpMultipart;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Robbie on 12/02/2016.
 */
public class RecordVideoActivity extends Activity {

    /*
    http://developer.android.com/guide/topics/media/camera.html
     */

    private final String TAG = "RECORDVIDEO";
    private Camera mCamera;
    private CameraPreview mPreview;
    private MediaRecorder mMediaRecorder;
    private boolean isRecording = false;
    public static final int MEDIA_TYPE_VIDEO = 2;
    static String fileName = "/storage/emulated/0/Pictures/HomeSecurity/VID_20160217_102058.mp4";
    private long totalSize = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.record_video_layout);

        // Create an instance of Camera
        mCamera = getCameraInstance();

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);

        // Add a listener to the Capture button
        final Button captureButton = (Button) findViewById(R.id.button_capture);
        captureButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (isRecording) {
                            // stop recording and release camera
                            mMediaRecorder.stop();  // stop the recording
                            releaseMediaRecorder(); // release the MediaRecorder object
                            mCamera.lock();         // take camera access back from MediaRecorder

                            // inform the user that recording has stopped
                            captureButton.setText("Capture");
                            isRecording = false;
                        } else {
                            // initialize video camera
//                            if (prepareVideoRecorder()) {
//                                // Camera is available and unlocked, MediaRecorder is prepared,
//                                // now you can start recording
//                                mMediaRecorder.start();
//                                System.out.println("START RECORDING");
//                                new Handler().postDelayed(new Runnable() {
//                                    public void run() {
//                                        new UploadVideoTask().execute(fileName);
//                                    }
//                                }, 31000);
//                                // inform the user that recording has started
//                                captureButton.setText("Stop");
//                                isRecording = true;
//                            } else {
//                                // prepare didn't work, release the camera
//                                releaseMediaRecorder();
                                // inform user

                            new UploadVideoTask().execute(fileName);
//                            }
                        }
                    }
                }
        );
    }

    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    private boolean prepareVideoRecorder(){
        System.out.println("PREPARING RECORDER");
        //mCamera = getCameraInstance();
        mMediaRecorder = new MediaRecorder();

        // Step 1: Unlock and set camera to MediaRecorder
        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);

        // Step 2: Set sources
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));

        // Step 4: Set output file
        mMediaRecorder.setOutputFile(getOutputMediaFile(MEDIA_TYPE_VIDEO).toString());

        //Max duration of 30 seconds
        mMediaRecorder.setMaxDuration(30000);
        System.out.println("MAX DURATION IS SET");

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

    private void releaseMediaRecorder(){
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();   // clear recorder configuration
            mMediaRecorder.release(); // release the recorder object
            mMediaRecorder = null;
            mCamera.lock();           // lock camera for later use
        }
    }

    private void releaseCamera(){
        if (mCamera != null){
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }

    /** Create a file Uri for saving an image or video */
    private static Uri getOutputMediaFileUri(int type){
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(int type){
        System.out.println("CREATING FILE");
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "HomeSecurity");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("HomeSecurity", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;

        if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");
            System.out.println("FILE CREATED");
             fileName = mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4";
            //uploadFile();
        } else {
            return null;
        }
        return mediaFile;
    }

    private class UploadVideoTask extends AsyncTask<String, Void, Void> {

        protected Void doInBackground(String... filePath) {

            uploadFile(filePath[0]);
            return null;
        }

        public void uploadFile(String filePath)
        {
//            HttpClient httpclient = new DefaultHttpClient();
//            HttpPost httppost = new HttpPost(Constants.UPLOADVIDEO_ENDPOINT);
//            httppost.addHeader("ZUMO-API-VERSION","2.0.0" );
//            httppost.addHeader("Content-Type", "multipart/form-data");
//
//
//            try {
//                AndroidMultipartEntity entity = new AndroidMultipartEntity(
//                        new AndroidMultipartEntity.ProgressListener() {
//
//                            @Override
//                            public void transferred(long num) {
//                                System.out.println(num);
//                            }
//                        });
//
//                File sourceFile = new File(filePath);
//
//                if(sourceFile == null)
//                    System.out.println("FILE IS NULL");
//
//                // Adding file data to http body
//                entity.addPart("videoFile", new FileBody(sourceFile));
//
//                totalSize = entity.getContentLength();
//                httppost.setEntity(entity);
//
//                // Making server call
//                HttpResponse response = httpclient.execute(httppost);
//                HttpEntity r_entity = response.getEntity();
//
//                int statusCode = response.getStatusLine().getStatusCode();
//                if (statusCode == 200) {
//                    // Server response
//                    System.out.println("200 OK");
//                } else {
//                    System.out.println("Error occurred! Http Status Code: "
//                            + statusCode);
//                }
//
//            } catch (ClientProtocolException e) {
//               e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }


            String response = "error";
            Log.i("Image filename", filePath);
            Log.i("url", Constants.UPLOADVIDEO_ENDPOINT);
            HttpURLConnection connection = null;
            DataOutputStream outputStream = null;
            // DataInputStream inputStream = null;

            String pathToOurFile = filePath;
            String urlServer = Constants.UPLOADVIDEO_ENDPOINT;
            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary = "*****";
            DateFormat df = new SimpleDateFormat("yyyy_MM_dd_HH:mm:ss");

            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            int maxBufferSize = 1 * 1024;
            try {
                FileInputStream fileInputStream = new FileInputStream(new File(
                        pathToOurFile));

                URL url = new URL(urlServer);
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
                //connection.setRequestProperty("content-length", fileInputStream.available() + "");
                connection.setRequestProperty("ZUMO-API-VERSION","2.0.0");

                outputStream = new DataOutputStream(connection.getOutputStream());
                outputStream.writeBytes(twoHyphens + boundary + lineEnd);

                String connstr = null;
                connstr = "Content-Disposition: form-data; name=\"uploadedfile\";filename=\""
                        + pathToOurFile + "\"" + lineEnd;
                Log.i("Connstr", connstr);

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
                            response = "outofmemoryerror";
                        }
                        bytesAvailable = fileInputStream.available();
                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                        bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    response = "error";
                }
                outputStream.writeBytes(lineEnd);
                outputStream.writeBytes(twoHyphens + boundary + twoHyphens
                        + lineEnd);

                // Responses from the server (code and message)
                int serverResponseCode = connection.getResponseCode();
                String serverResponseMessage = connection.getResponseMessage();
                Log.i("Server Response Code ", "" + serverResponseCode);
                Log.i("Server Response Message", serverResponseMessage);
                Log.i("Server Response Message", connection.toString());

                if (serverResponseCode == 200) {
                    response = "true";
                }

                String CDate = null;
                Date serverTime = new Date(connection.getDate());
                try {
                    CDate = df.format(serverTime);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("Date Exception", e.getMessage() + " Parse Exception");
                }
                Log.i("Server Response Time", CDate + "");

                fileInputStream.close();
                outputStream.flush();
                outputStream.close();
                outputStream = null;
            } catch (Exception ex) {
                // Exception handling
                response = "error";
                Log.e("Send file Exception", ex.getMessage() + "");
                ex.printStackTrace();
            }
        }
    }

}
