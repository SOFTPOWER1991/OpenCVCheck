package demo.rayootech.com.cvcheck;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button btnSimilarity, btnCamera, btnFaceDetection, btnFdOpenCv, btnFdOpenCvPic, btnFdSc;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSimilarity = (Button) findViewById(R.id.btn_similarity);
        btnSimilarity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myStartActivity(SimilarityActivity.class);
            }
        });

        btnCamera = (Button) findViewById(R.id.btn_camera);
        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myStartActivity(CameraPreviewActivity.class);
            }
        });

        btnFaceDetection = (Button) findViewById(R.id.btn_face_detection);
        btnFaceDetection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myStartActivity(FaceDetectionActivityByAndroidAPI.class);
            }
        });

        btnFdOpenCv = (Button) findViewById(R.id.btn_fd_opencv);
        btnFdOpenCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myStartActivity(FdActivity.class);
            }
        });

        btnFdOpenCvPic = (Button) findViewById(R.id.btn_fd_pic);
        btnFdOpenCvPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myStartActivity(FdStaticPicActivity.class);
            }
        });

        btnFdSc = (Button) findViewById(R.id.btn_fd_sc);
        btnFdSc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myStartActivity(FdSimilarityCheckActivity.class);
            }
        });

    }

    private void myStartActivity(Class<?> cls) {
        Intent intent = new Intent();
        intent.setClass(MainActivity.this, cls);
        startActivity(intent);
    }


}
