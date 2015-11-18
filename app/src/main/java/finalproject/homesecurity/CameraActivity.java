package finalproject.homesecurity;

import finalproject.homesecurity.data.GlobalData;
import finalproject.homesecurity.data.Preferences;
import finalproject.homesecurity.detection.AggregateLumaMotionDetection;
import finalproject.homesecurity.detection.IMotionDetection;
import finalproject.homesecurity.detection.LumaMotionDetection;
import finalproject.homesecurity.detection.RgbMotionDetection;
import finalproject.homesecurity.image.ImageProcessing;

import java.io.File;
import java.io.FileOutputStream;
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
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

public class CameraActivity extends SensorsActivity {
    //https://github.com/phishman3579/android-motion-detection/tree/master/src/com/jwetherell/motion_detection
    private static final String TAG = "CameraActivity";
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private static SurfaceView preview = null;
    private static SurfaceHolder previewHolder = null;
    private static Camera camera = null;
    private static boolean inPreview = false;
    private static long mReferenceTime = 0;
    private static IMotionDetection detector = null;
    private static Context con;
    private static volatile AtomicBoolean processing = new AtomicBoolean(false);
    private static boolean detectMotion = false;
    private ImageView changeCamera;
    private int cameraID = 0; //camera is initially facing back
    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_activity_layout);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        con = CameraActivity.this;
        changeCamera = (ImageView) findViewById(R.id.changeCamera);
        preview = (SurfaceView) findViewById(R.id.preview);
        previewHolder = preview.getHolder();
        previewHolder.addCallback(surfaceCallback);
        previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        prefs = this.getSharedPreferences("PhoneMode", Context.MODE_PRIVATE); //indicates whether phone is security device or personal
        editor = prefs.edit();
        editor.putString("DeviceMode", "Security"); //this device will be listed as security

        Intent intent = getIntent();
        String roomName = intent.getStringExtra("roomName");
        System.out.println("Room Name in Camera Activity: " + roomName);

        editor.putString("RoomName",roomName);
        editor.commit();

        if (Preferences.USE_RGB) {
            detector = new RgbMotionDetection();
        } else if (Preferences.USE_LUMA) {
            detector = new LumaMotionDetection();
        } else {
            // Using State based (aggregate map)
            detector = new AggregateLumaMotionDetection();
        }
    }

    public void changeCamera(View v)
    {
        //if phone has only one camera, hide "switch camera" button
        if(Camera.getNumberOfCameras() == 1){
            changeCamera.setVisibility(View.INVISIBLE);
        }
        else {
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
            //Code snippet for this method from somewhere on android developers, i forget where
            camera.setDisplayOrientation(90);
            try {
                //this step is critical or preview on new camera will no know where to render to
                camera.setPreviewDisplay(previewHolder);
                camera.setPreviewCallback(previewCallback);
            } catch (Exception e) {
                e.printStackTrace();
            }
            camera.startPreview();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPause() {
        super.onPause();

        camera.setPreviewCallback(null);
        if (inPreview) camera.stopPreview();
        inPreview = false;
        camera.release();
        camera = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onResume() {
        super.onResume();

        camera = Camera.open();
    }

    /*
    METHODS ACCESSED BY NOTIFICATIONS HANDLER
     */
    public static void setDetectMotion(boolean motion)
    {
        detectMotion = motion;
    }


    public static Camera getCamera() {
        return camera;
    }

    public static void setCamera(Camera camera) {
        CameraActivity.camera = camera;
    }

    private PreviewCallback previewCallback = new PreviewCallback() {

        /**
         * {@inheritDoc}
         */
        @Override
        public void onPreviewFrame(byte[] data, Camera cam) {
            //my idea here is to allow a user to select when the motion detection starts
            //this might need to be put onto a separate thread
            if(detectMotion) //if detect motion is true then allow this device to detect motion other wise do nothing
            {

                if (data == null) return;
                Camera.Size size = cam.getParameters().getPreviewSize();
                if (size == null) return;

                if (!GlobalData.isPhoneInMotion()) {
                    System.out.println("DETECTING MOTION");
                    DetectionThread thread = new DetectionThread(data, size.width, size.height);
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
            Camera.Parameters parameters = camera.getParameters();
            Camera.Size size = getBestPreviewSize(width, height, parameters);
            if (size != null) {
                parameters.setPreviewSize(size.width, size.height);
                Log.d(TAG, "Using width=" + size.width + " height=" + size.height);
            }
            camera.setParameters(parameters);
            camera.startPreview();
            inPreview = true;
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
        private int width;
        private int height;

        public DetectionThread(byte[] data, int width, int height) {
            this.data = data;
            this.width = width;
            this.height = height;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void run() {
            if (!processing.compareAndSet(false, true)) return;

            // Log.d(TAG, "BEGIN PROCESSING...");
            try {
                // Previous frame
                int[] pre = null;
                if (Preferences.SAVE_PREVIOUS) pre = detector.getPrevious();

                // Current frame (with changes)
                // long bConversion = System.currentTimeMillis();
                int[] img = null;
                if (Preferences.USE_RGB) {
                    img = ImageProcessing.decodeYUV420SPtoRGB(data, width, height);
                } else {
                    img = ImageProcessing.decodeYUV420SPtoLuma(data, width, height);
                }
                // long aConversion = System.currentTimeMillis();
                // Log.d(TAG, "Converstion="+(aConversion-bConversion));

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

                        Log.i(TAG, "Saving.. previous=" + previous + " original=" + original + " bitmap=" + bitmap);
                        Looper.prepare();

                        /*
                        MOTION HAS BEEN DETECTED AT THIS POINT
                        WE NOW HAVE AN IMAGE SHOWING WHAT HAS TRIGGERED THE MOTION DETECTION
                        SEND THIS TO A USER
                         */
                        Intent intent = new Intent(con, Image.class);
                        //test which of original,previous or bitmap is the image with the detected motion
                        intent.putExtra("BitmapImage", original);
                        con.startActivity(intent);


                        //new SavePhotoTask().execute(previous, original, bitmap);
                    } else {
                        Log.i(TAG, "Not taking picture because not enough time has passed since the creation of the Surface");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                processing.set(false);
            }
            // Log.d(TAG, "END PROCESSING...");

            processing.set(false);
        }
    }

    ;

    private static final class SavePhotoTask extends AsyncTask<Bitmap, Integer, Integer> {

        /**
         * {@inheritDoc}
         */
        @Override
        protected Integer doInBackground(Bitmap... data) {
            for (int i = 0; i < data.length; i++) {
                Bitmap bitmap = data[i];
                String name = String.valueOf(System.currentTimeMillis());
                if (bitmap != null) save(name, bitmap);
            }
            return 1;
        }

        private void save(String name, Bitmap bitmap) {
            File photo = new File(Environment.getExternalStorageDirectory(), name + ".jpg");
            if (photo.exists()) photo.delete();

            try {
                FileOutputStream fos = new FileOutputStream(photo.getPath());
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.close();
            } catch (java.io.IOException e) {
                Log.e("PictureDemo", "Exception in photoCallback", e);
            }
        }
    }
}