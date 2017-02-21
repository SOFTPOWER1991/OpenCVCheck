# OpenCVCheck
OpenCV人脸识别、图片相似度检测


## 检测任意两张图片的相似度思路

1. 加载两张图片为bitmap进入内存
2. 将内存中的两张图片bitmap转换为Mat矩阵(Mat类是OpenCV最基本的一个数据类型，它可以表示一个多维的多通道的数组。Mat常用来存储图像，包括单通道二维数组——灰度图，多通道二维数组——彩色图)
3. 把Mat矩阵的type转换为Cv_8uc1(1通道8位矩阵)类型，然后转换为Cv_32F，
因为在c++代码中会判断他的类型。
4. 通过OpenCv 来进行俩个矩阵的比较（俩个矩阵必须一样大小的高宽）


## 识别图片中是否有人脸思路

1. 需要一个人脸的Haar特征分类器就是一个XML文件，该文件中会描述人脸的Haar特征值，CascadeClassifier人脸探测器将该特征值集合加载入内存
2. 加载图片为bitmap进入内存，将bitmap转换为Mat矩阵。
3. 有了Mat矩阵，然后通过调用OpenCV的Native方法，人脸探测器CascadeClassifier在该Mat矩阵中检测当前是否有人脸。
4. 如果有，我们会获取到一个Rect数组，里面会有人脸数据，然后将人脸画在屏幕上，方框或者圆形
	
## 识别两张图片中的人脸是否是同一个人脸思路

1. 识别出人脸后会得到两个人脸的Rect数组，然后比较这两个Rect数组的相似度即可！


# 实现步骤

## 工程目录准备

1. 新建Android Studio项目 *OpenCVCheck*
2. 导入OpenCVLibrary320
3. 在module下的build.gradle中引入OpenCVLibrary的编译：

    ```
     compile project(':openCVLibrary320')
    ```

## 检测任意两张图片的相似度的实现步骤
1. 初始化OpenCV：

    ```
    static {
        if (OpenCVLoader.initDebug()) {
            Log.e(TAG, "OpenCV load success !");
        } else {
            Log.e(TAG, "OpenCV load failed !");
        }
    }
    ```

2. 加载两张图片进入内存

    ```
    Bitmap mBitmap1 = BitmapFactory.decodeResource(getResources(), R.mipmap.pic1);
    Bitmap mBitmap2 = BitmapFactory.decodeResource(getResources(), R.mipmap.pic2);
    ```
3. 将内存中的两张图片bitmap转换为Mat矩阵

    ```
    Mat mat1 = new Mat();
    Mat mat2 = new Mat();
    Mat mat11 = new Mat();
    Mat mat22 = new Mat();
    Utils.bitmapToMat(mBitmap1, mat1);
    Utils.bitmapToMat(mBitmap2, mat2);

    Imgproc.cvtColor(mat1, mat11, Imgproc.COLOR_BGR2GRAY);
    Imgproc.cvtColor(mat2, mat22, Imgproc.COLOR_BGR2GRAY);
    ```

4. 把Mat矩阵的type转换为Cv_8uc1(1通道8位矩阵)类型，然后转换为Cv_32F,通过OpenCV来进行俩个矩阵的比较

    ```
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
    ```

## 识别图片中是否有人脸步骤

1. 初始化OpenCV

	```
	if (!OpenCVLoader.initDebug()) {
	   Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
	   OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_11, this, mLoaderCallback);
	} else {
	   Log.d(TAG, "OpenCV library found inside package. Using it!");
	   mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
	}
	```
2. 编译 .so 库

   通过ndk 来编译 jni文件下的.cpp 文件，生成.so库，以备程序使用

3. 加载.so 库

	```
	     // 在Opencv初始化完成后，调用Native库
	     System.loadLibrary("detection_based_tracker");
	```

4. 加载需要的人脸的Haar特征分类器就是一个XML文件，该文件中会描述人脸的Haar特征值

	```
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
	```

5. 加载图片进入内存，得到Mat矩阵，有了Mat矩阵，然后通过调用OpenCV的Native方法，人脸探测器CascadeClassifier在该Mat矩阵中检测当前是否有人脸

	 ```
	         Bitmap imgtemp = BitmapFactory.decodeResource(getResources(), R.mipmap.twop);
	
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
	 ```

6. 如果有，我们会获取到一个Rect数组，里面会有人脸数据，然后将人脸画在屏幕上，方框或者圆形

	 ```
	 Rect[] facesArray = faces.toArray();
	 for (int i = 0; i < facesArray.length; i++)
	     Imgproc.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(), FACE_RECT_COLOR, 3);
	
	 Utils.matToBitmap(mRgba, imgtemp, true);
	
	 imageView.setImageBitmap(imgtemp);
	 ```
	
	
	## 识别两张图片中的人脸是否是同一个人脸步骤
	
	 这个功能前面的步骤跟检测人脸的一样，唯一不同的就是：检测出两个人脸的Rect数组后，进行相似度比较：
	
	 ```
	 /**
	      * 特征对比
	      *
	      * @param file1 人脸特征
	      * @param file2 人脸特征
	      * @return 相似度
	      */
	     public double CmpPic(String file1, String file2) {
	         try {
	             int l_bins = 256;
	             int hist_size[] = {l_bins};
	             float v_ranges[] = {0, 255};
	             float ranges[][] = {v_ranges};
	             opencv_core.IplImage Image1 = cvLoadImage(getFilePath(file1), CV_LOAD_IMAGE_GRAYSCALE);
	             opencv_core.IplImage Image2 = cvLoadImage(getFilePath(file2), CV_LOAD_IMAGE_GRAYSCALE);
	             opencv_core.IplImage imageArr1[] = {Image1};
	             opencv_core.IplImage imageArr2[] = {Image2};
	             opencv_imgproc.CvHistogram Histogram1 = opencv_imgproc.CvHistogram.create(1, hist_size, CV_HIST_ARRAY, ranges, 1);
	             opencv_imgproc.CvHistogram Histogram2 = opencv_imgproc.CvHistogram.create(1, hist_size, CV_HIST_ARRAY, ranges, 1);
	             cvCalcHist(imageArr1, Histogram1, 0, null);
	             cvCalcHist(imageArr2, Histogram2, 0, null);
	             cvNormalizeHist(Histogram1, 100.0);
	             cvNormalizeHist(Histogram2, 100.0);
	
	             double c1 = cvCompareHist(Histogram1, Histogram2, CV_COMP_CORREL) * 100;
	             double c2 = cvCompareHist(Histogram1, Histogram2, CV_COMP_INTERSECT);
	             return (c1 + c2) / 2;
	         } catch (Exception e) {
	             e.printStackTrace();
	             return -1;
	         }
	     }
	 ```

