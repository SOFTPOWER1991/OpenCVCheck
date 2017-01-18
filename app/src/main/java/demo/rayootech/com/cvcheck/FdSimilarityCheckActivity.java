package demo.rayootech.com.cvcheck;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class FdSimilarityCheckActivity extends Activity {

    private static final String TAG = "OCVSample::Activity";
    private static final Scalar FACE_RECT_COLOR = new Scalar(0, 222, 0, 190);
    public static final int JAVA_DETECTOR = 0;
    public static final int NATIVE_DETECTOR = 1;



    private Mat mRgba;
    private Mat mGray;
    private File mCascadeFile;
    private CascadeClassifier mJavaDetector;
    private DetectionBasedTracker mNativeDetector;

    private int mDetectorType = JAVA_DETECTOR;
    private String[] mDetectorName;

    private float mRelativeFaceSize = 0.2f;
    private int mAbsoluteFaceSize = 0;


    private Button btnCheck;

    private ImageView imageView1, imageView2;

    private ArrayList<FaceData> faceDatas = new ArrayList<>();

    private Bitmap mBitmapFace1;
    private Bitmap mBitmapFace2;
    private FaceUtil mFaceUtil;
    private double cmp;


    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");

                    // Load native library after(!) OpenCV initialization
                    System.loadLibrary("detection_based_tracker");

                    try {
                        // load cascade file from application resources
                        InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
                        File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                        mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
                        FileOutputStream os = new FileOutputStream(mCascadeFile);

                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                        is.close();
                        os.close();

                        mJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
                        if (mJavaDetector.empty()) {
                            Log.e(TAG, "Failed to load cascade classifier");
                            mJavaDetector = null;
                        } else
                            Log.i(TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());

                        mNativeDetector = new DetectionBasedTracker(mCascadeFile.getAbsolutePath(), 0);

                        cascadeDir.delete();

                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
                    }


                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    public FdSimilarityCheckActivity() {
        mDetectorName = new String[2];
        mDetectorName[JAVA_DETECTOR] = "Java";
        mDetectorName[NATIVE_DETECTOR] = "Native (tracking)";

        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_find_face_detect3);

        mFaceUtil = new FaceUtil(FdSimilarityCheckActivity.this);

        btnCheck = (Button) findViewById(R.id.btn_check);
        btnCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                checkFace(imageView1, R.mipmap.pic41);
                checkFace(imageView2, R.mipmap.pic31);

                getPareHist(R.mipmap.pic41, R.mipmap.pic31);
            }
        });

        imageView1 = (ImageView) findViewById(R.id.img_face1);
        Bitmap imgtemp = BitmapFactory.decodeResource(getResources(), R.mipmap.pic41);
        imageView1.setImageBitmap(imgtemp);

        imageView2 = (ImageView) findViewById(R.id.img_face2);
        Bitmap imgtemp1 = BitmapFactory.decodeResource(getResources(), R.mipmap.pic31);
        imageView2.setImageBitmap(imgtemp1);

    }

    private void getPareHist(int pic41, int pic31) {

        Bitmap mBitmap1 = BitmapFactory.decodeResource(getResources(), pic41);

        Bitmap mBitmap2 = BitmapFactory.decodeResource(getResources(), pic31);

        Mat mat1 = new Mat();
        Mat mat2 = new Mat();
        Mat mat11 = new Mat();
        Mat mat22 = new Mat();
        Utils.bitmapToMat(mBitmap1, mat1);
        Utils.bitmapToMat(mBitmap2, mat2);

        Imgproc.cvtColor(mat1, mat11, Imgproc.COLOR_BGR2GRAY);
        Imgproc.cvtColor(mat2, mat22, Imgproc.COLOR_BGR2GRAY);
        comPareHist(mat11, mat22);
    }

    /**
     * 比较来个矩阵的相似度
     *
     * @param srcMat
     * @param desMat
     */
    public void comPareHist(Mat srcMat, Mat desMat) {

        srcMat.convertTo(srcMat, CvType.CV_32F);
        desMat.convertTo(desMat, CvType.CV_32F);
        double target = Imgproc.compareHist(srcMat, desMat, Imgproc.CV_COMP_CORREL);

        Toast.makeText(FdSimilarityCheckActivity.this, "相似度:" + target, Toast.LENGTH_SHORT).show();

    }

    private void checkFace(ImageView imageView, int resId) {

        Mat mRgba = new Mat();
        Mat mGray = new Mat();

        Bitmap imgtemp = BitmapFactory.decodeResource(getResources(), resId);

        Utils.bitmapToMat(imgtemp, mRgba);

        Mat mat1 = new Mat();

        Utils.bitmapToMat(imgtemp, mat1);

        Imgproc.cvtColor(mat1, mGray, Imgproc.COLOR_BGR2GRAY);

        if (mAbsoluteFaceSize == 0) {
            int height = mGray.rows();
            if (Math.round(height * mRelativeFaceSize) > 0) {
                mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
            }
            mNativeDetector.setMinFaceSize(mAbsoluteFaceSize);
        }

        MatOfRect faces = new MatOfRect();

        if (mDetectorType == JAVA_DETECTOR) {
            if (mJavaDetector != null)
                mJavaDetector.detectMultiScale(mGray, faces, 1.1, 2, 2, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
                        new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
        } else if (mDetectorType == NATIVE_DETECTOR) {
            if (mNativeDetector != null)
                mNativeDetector.detect(mGray, faces);
        } else {
            Log.e(TAG, "Detection method is not selected!");
        }

        Rect[] facesArray = faces.toArray();
        for (int i = 0; i < facesArray.length; i++) {
            Imgproc.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(), FACE_RECT_COLOR, 3);


            //TODO:
            faceDatas.add(new FaceData(mRgba, facesArray[i]));
        }

        Utils.matToBitmap(mRgba, imgtemp, true);

        imageView.setImageBitmap(imgtemp);
    }


    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_11, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mGray.release();
        mRgba.release();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGray.release();
        mRgba.release();
    }


    private void setMinFaceSize(float faceSize) {
        mRelativeFaceSize = faceSize;
        mAbsoluteFaceSize = 0;
    }

    private void setDetectorType(int type) {
        if (mDetectorType != type) {
            mDetectorType = type;

            if (type == NATIVE_DETECTOR) {
                Log.i(TAG, "Detection Based Tracker enabled");
                mNativeDetector.start();
            } else {
                Log.i(TAG, "Cascade detector enabled");
                mNativeDetector.stop();
            }
        }
    }
}
