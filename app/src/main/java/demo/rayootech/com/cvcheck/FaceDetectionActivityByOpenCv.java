package demo.rayootech.com.cvcheck;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.InstallCallbackInterface;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class FaceDetectionActivityByOpenCv extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private JavaCameraView javaCameraView;

    Mat mRgba, imgGray, imgCanny;

    BaseLoaderCallback baseLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            super.onManagerConnected(status);

            switch (status) {
                case BaseLoaderCallback.SUCCESS:
                    javaCameraView.enableView();
                    break;

                default:
                    super.onManagerConnected(status);
                    break;

            }

        }

        @Override
        public void onPackageInstall(int operation, InstallCallbackInterface callback) {
            super.onPackageInstall(operation, callback);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_preview);

        javaCameraView = (JavaCameraView) findViewById(R.id.javaCameraView);
        javaCameraView.setVisibility(SurfaceView.VISIBLE);


        javaCameraView.setCvCameraViewListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseJavaCV();
    }

    private void releaseJavaCV() {
        if (javaCameraView != null) {
            javaCameraView.disableView();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseJavaCV();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (OpenCVLoader.initDebug()) {
            baseLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        } else {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_11, FaceDetectionActivityByOpenCv.this, baseLoaderCallback);

        }

    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(width, height, CvType.CV_8UC4);
        imgGray = new Mat(width, height, CvType.CV_8UC1);
        imgCanny = new Mat(width, height, CvType.CV_8UC1);
    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        mRgba = inputFrame.rgba();

        Imgproc.cvtColor(mRgba, imgGray, Imgproc.COLOR_RGB2GRAY);

        Imgproc.Canny(imgGray, imgCanny, 50, 150);

        return imgCanny;
    }


}
