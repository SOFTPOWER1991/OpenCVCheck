package demo.rayootech.com.cvcheck;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class SimilarityActivity extends AppCompatActivity {

    public static final String TAG = "OpenCv_compare";

    static {
        if (OpenCVLoader.initDebug()) {
            Log.e(TAG, "OpenCV load success !");
        } else {
            Log.e(TAG, "OpenCV load failed !");
        }
    }

    private Bitmap mBitmap1, mBitmap2;
    private TextView textView;

    private Button btnCompare;

    private ImageView img1, img2;

    private BaseLoaderCallback callback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            super.onManagerConnected(status);
            switch (status) {
                case BaseLoaderCallback.SUCCESS:

                    break;

                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_similarity);

        textView = (TextView) findViewById(R.id.tv);

        mBitmap1 = BitmapFactory.decodeResource(getResources(), R.mipmap.pic1);
        mBitmap2 = BitmapFactory.decodeResource(getResources(), R.mipmap.pic2);


        btnCompare = (Button) findViewById(R.id.btn_compare);

        img1 = (ImageView) findViewById(R.id.img1);

        img1.setImageBitmap(mBitmap1);

        img2 = (ImageView) findViewById(R.id.img2);

        img2.setImageBitmap(mBitmap2);


        btnCompare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

//        // 通过OpenCV引擎服务加载并初始化OpenCV类库，所谓OpenCV引擎服务即是
//        // OpenCV_2.4.9.2_Manager_2.4_*.apk程序包，存在于OpenCV安装包的apk目录中
//        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, this,
//                callback);
//
//        OpenCVLoader.in
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

        textView.setText("相似度:" + target);
    }

}
