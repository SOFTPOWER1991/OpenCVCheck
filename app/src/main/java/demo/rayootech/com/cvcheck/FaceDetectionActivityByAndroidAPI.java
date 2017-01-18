package demo.rayootech.com.cvcheck;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.media.FaceDetector;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class FaceDetectionActivityByAndroidAPI extends AppCompatActivity implements OnClickListener {


    private static final int REQUEST_CODE_SELECT_PIC = 120;
    private static final int MAX_FACE_NUM = 10;//最大可以检测出的人脸数量
    private int realFaceNum = 0;//实际检测出的人脸数量

    private Button selectBtn;
    private Button detectBtn;
    private ImageView image;
    private ProgressDialog pd;

    private Bitmap bm;//选择的图片的Bitmap对象
    private Paint paint;//画人脸区域用到的Paint

    private boolean hasDetected = false;//标记是否检测到人脸

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fd2);

        initView();

        paint = new Paint();
        paint.setColor(Color.BLUE);
        paint.setStrokeWidth(2);
        paint.setStyle(Paint.Style.STROKE);//设置话出的是空心方框而不是实心方块

        pd = new ProgressDialog(this);
        pd.setTitle("提示");
        pd.setMessage("正在检测，请稍等");
    }

    /**
     * 控件初始化
     */
    private void initView() {
        selectBtn = (Button) findViewById(R.id.btn_select);
        selectBtn.setOnClickListener(this);
        detectBtn = (Button) findViewById(R.id.btn_detect);
        detectBtn.setOnClickListener(this);
        image = (ImageView) findViewById(R.id.image);
    }

    /**
     * 从图库选择图片
     */
    private void selectPicture() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CODE_SELECT_PIC);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_PIC && resultCode == Activity.RESULT_OK) {
            //获取选择的图片
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String selectedImagePath = cursor.getString(columnIndex);
            bm = BitmapFactory.decodeResource(getResources(), R.mipmap.two2);
            //要使用Android内置的人脸识别，需要将Bitmap对象转为RGB_565格式，否则无法识别
            bm = bm.copy(Bitmap.Config.RGB_565, true);
            cursor.close();
            image.setImageBitmap(bm);
            hasDetected = false;
        }
    }

    /**
     * 检测人脸
     */
    private void detectFace() {
        if (bm == null) {
            Toast.makeText(this, "请先选择图片", Toast.LENGTH_SHORT).show();
            return;
        }
        if (hasDetected) {
            Toast.makeText(this, "已检测出人脸", Toast.LENGTH_SHORT).show();
        } else {
            new FindFaceTask().execute();
        }
    }

    private void drawFacesArea(FaceDetector.Face[] faces) {
        Toast.makeText(this, "图片中检测到" + realFaceNum + "张人脸", Toast.LENGTH_SHORT).show();
        float eyesDistance = 0f;//两眼间距
        Canvas canvas = new Canvas(bm);
        for (int i = 0; i < faces.length; i++) {
            FaceDetector.Face face = faces[i];
            if (face != null) {
                PointF pointF = new PointF();
                face.getMidPoint(pointF);//获取人脸中心点
                eyesDistance = face.eyesDistance();//获取人脸两眼的间距
                //画出人脸的区域
                canvas.drawRect(pointF.x - eyesDistance, pointF.y - eyesDistance, pointF.x + eyesDistance, pointF.y + eyesDistance, paint);
                hasDetected = true;
            }
        }
        //画出人脸区域后要刷新ImageView
        image.invalidate();
    }

    /**
     * 检测图像中的人脸需要一些时间，所以放到AsyncTask中去执行
     *
     * @author yubo
     */
    private class FindFaceTask extends AsyncTask<Void, Void, FaceDetector.Face[]> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd.show();
        }

        @Override
        protected FaceDetector.Face[] doInBackground(Void... arg0) {
            //最关键的就是下面三句代码
            FaceDetector faceDetector = new FaceDetector(bm.getWidth(), bm.getHeight(), MAX_FACE_NUM);
            FaceDetector.Face[] faces = new FaceDetector.Face[MAX_FACE_NUM];
            realFaceNum = faceDetector.findFaces(bm, faces);
            if (realFaceNum > 0) {
                return faces;
            }
            return null;
        }

        @Override
        protected void onPostExecute(FaceDetector.Face[] result) {
            super.onPostExecute(result);
            pd.dismiss();
            if (result == null) {
                Toast.makeText(FaceDetectionActivityByAndroidAPI.this, "抱歉，图片中未检测到人脸", Toast.LENGTH_SHORT).show();
            } else {
                drawFacesArea(result);
            }
        }

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_select://选择图片
                selectPicture();
                break;
            case R.id.btn_detect://检测人脸
                detectFace();
                break;
        }
    }
}
