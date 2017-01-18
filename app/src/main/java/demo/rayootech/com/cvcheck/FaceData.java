package demo.rayootech.com.cvcheck;

import org.opencv.core.Mat;
import org.opencv.core.Rect;

/**
 * File Description  :
 *
 * @author : zhanggeng
 * @version : v1.0
 *          **************修订历史*************
 * @email : zhanggengdyx@gmail.com
 * @date : 2017/1/17 23:44
 */

public class FaceData {
    private Mat mat;
    private Rect rects;

    public FaceData(Mat mat, Rect rects) {
        this.mat = mat;
        this.rects = rects;
    }

    public Mat getMat() {
        return mat;
    }

    public void setMat(Mat mat) {
        this.mat = mat;
    }

    public Rect getRects() {
        return rects;
    }

    public void setRects(Rect rects) {
        this.rects = rects;
    }
}
