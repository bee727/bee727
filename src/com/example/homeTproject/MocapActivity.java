// Partial code migrated from https://github.com/ildoonet/tf-pose-estimation

package com.example.homeTproject;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.media.ImageReader.OnImageAvailableListener;
import android.os.SystemClock;
import android.util.Log;
import android.util.Size;
import android.util.TypedValue;

import com.example.homeTproject.env.BorderedText;
import com.example.homeTproject.env.ImageUtils;
import com.example.homeTproject.env.Logger;
import com.example.homeTproject.OverlayView.DrawCallback;
import com.example.homeTproject.R;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import static java.lang.Math.round;
import static java.sql.Types.NULL;

/**
 * An activity that uses a TensorFlowMultiBoxDetector and ObjectTracker to detect and then track
 * objects.
 */
public class MocapActivity extends CameraActivity implements OnImageAvailableListener {
    private static final Logger LOGGER = new Logger();

    int[] model_output = new int[38];
    float[][] outt = new float[1][5];
    String Pose_result,temp="";
    String Intent_Pose;
    int cnt=0;

    private static final int MP_INPUT_SIZE = 368;
    private static final String MP_INPUT_NAME = "image";
    private static final String MP_OUTPUT_L1 = "Openpose/MConv_Stage6_L1_5_pointwise/BatchNorm/FusedBatchNorm";
    private static final String MP_OUTPUT_L2 = "Openpose/MConv_Stage6_L2_5_pointwise/BatchNorm/FusedBatchNorm";
    private static final String MP_MODEL_FILE = "file:///android_asset/frozen_person_model.pb";

    private static final boolean MAINTAIN_ASPECT = true;
    private static final Size DESIRED_PREVIEW_SIZE = new Size(640, 480);

    private static final boolean SAVE_PREVIEW_BITMAP = false;
    private static final float TEXT_SIZE_DIP = 10;

    private Integer sensorOrientation;

    private Classifier detector;

    private long lastProcessingTimeMs;
    private int lastHumansFound;
    private Bitmap rgbFrameBitmap = null;
    private Bitmap croppedBitmap = null;
    private Bitmap cropCopyBitmap = null;

    private boolean computingDetection = false;

    private long timestamp = 0;

    private Matrix frameToCropTransform;
    private Matrix cropToFrameTransform;

    private byte[] luminanceCopy;

    private BorderedText borderedText;
    private OverlayView trackingOverlay;

    Bitmap SAMPLE_IMAGE;

    @Override
    public void onPreviewSizeChosen(final Size size, final int rotation) {
        final float textSizePx =
                TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, getResources().getDisplayMetrics());
        borderedText = new BorderedText(textSizePx);
        borderedText.setTypeface(Typeface.MONOSPACE);

        int cropSize = MP_INPUT_SIZE;

        // Configure the detector
        detector = TensorFlowPoseDetector.create(
                getAssets(),
                MP_MODEL_FILE,
                MP_INPUT_SIZE,
                MP_INPUT_NAME,
                new String[]{MP_OUTPUT_L1, MP_OUTPUT_L2}
        );

        SAMPLE_IMAGE = BitmapFactory.decodeResource(getResources(), R.drawable.jump2);

        previewWidth = size.getWidth();
        previewHeight = size.getHeight();

        sensorOrientation = rotation - getScreenOrientation();
        LOGGER.i("Camera orientation relative to screen canvas: %d", sensorOrientation);

        LOGGER.i("Initializing at size %dx%d", previewWidth, previewHeight);
        rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Config.ARGB_8888);
        croppedBitmap = Bitmap.createBitmap(cropSize, cropSize, Config.ARGB_8888); //ARGB_8888 : 각 픽셀은 4bytes로 저장된다.

        frameToCropTransform =
                ImageUtils.getTransformationMatrix(
                        previewWidth, previewHeight,
                        cropSize, cropSize,
                        sensorOrientation, MAINTAIN_ASPECT);
        cropToFrameTransform = new Matrix();
        frameToCropTransform.invert(cropToFrameTransform);

        //trackingOverlay = (OverlayView) findViewById(R.id.results);
        // TODO: Draw human bones
        //trackingOverlay.addCallback(
//                new DrawCallback() {
//                    @Override
//                    public void drawCallback(final Canvas canvas) {
//                        // tracker.draw(canvas);
//                    }
//                });

        // TODO: Debug information ( to remove)
        addCallback(
                new DrawCallback() {
                    @Override
                    public void drawCallback(final Canvas canvas) {
                        if (!isDebug()) {
                            return;
                        }
                        final Bitmap copy = cropCopyBitmap;
                        if (copy == null) {
                            return;
                        }

                        final int backgroundColor = Color.rgb(0, 0, 255);
                        //canvas.drawColor(backgroundColor);
                        Paint pp = new Paint();
                        pp.setColor(backgroundColor);
                        final float scaleFactor = 2;
                        canvas.drawRect(new Rect(5, 5,
                                15 + copy.getWidth() * (int)scaleFactor,
                                15 + copy.getHeight() * (int)scaleFactor), pp);

                        final Matrix matrix = new Matrix();

                        matrix.postScale(scaleFactor, scaleFactor);

                        // RT: Position of the preview canvas
                        matrix.postTranslate(10, 10);
                        //matrix.postTranslate(
                        //        canvas.getWidth() - copy.getWidth() * scaleFactor,
                        //        canvas.getHeight() - copy.getHeight() * scaleFactor);
                        canvas.drawBitmap(copy, matrix, new Paint());

                        final Vector<String> lines = new Vector<String>();
                        if (detector != null) {
                            final String statString = detector.getStatString();
                            final String[] statLines = statString.split("\n");
                            for (final String line : statLines) {
                                lines.add(line);
                            }
                        }
                        lines.add("");
                        Intent intent = getIntent();

                        Intent_Pose = intent.getStringExtra("pose");  // Home에서 넘긴 포즈 정보 받기

                        // 화면 상단 우측 정보
                        lines.add("Frame: " + previewWidth + "x" + previewHeight);
                        lines.add("Crop: " + copy.getWidth() + "x" + copy.getHeight());
                        lines.add("View: " + canvas.getWidth() + "x" + canvas.getHeight());
                        lines.add("Rotation: " + sensorOrientation);
                        lines.add("Inference time: " + lastProcessingTimeMs + "ms");
                        lines.add("Humans found: " + lastHumansFound);
                        lines.add("포즈: " + Pose_result);
                        lines.add("넘겨받은 포즈: " + Intent_Pose);
//                        if(temp != Pose_result){
//                            cnt=0;
//                            temp=Pose_result;
//                        }
//                        else{
//                            cnt++;
//                        }
//                        lines.add("count:" + cnt);

                        //borderedText.drawLines(canvas, 10, canvas.getHeight() - 10, lines); // bottom
                        borderedText.drawLinesTop(canvas, copy.getWidth() * scaleFactor + 30, 10, lines); // top-right
                    }
                });
    }


    @Override
    protected void processImage() {
        ++timestamp;
        final long currTimestamp = timestamp;
//        byte[] originalLuminance = getLuminance();
        //trackingOverlay.postInvalidate();

        // No mutex needed as this method is not reentrant.
        if (computingDetection) {
            readyForNextImage();
            return;
        }
        computingDetection = true;
        LOGGER.i("Preparing image " + currTimestamp + " for detection in bg thread.");

        rgbFrameBitmap.setPixels(getRgbBytes(), 0, previewWidth, 0, 0, previewWidth, previewHeight);


        //rgbFrameBitmap = SAMPLE_IMAGE;
        // TODO: Use real camera image

//        if (luminanceCopy == null) {
//            luminanceCopy = new byte[originalLuminance.length];
//        }
//        System.arraycopy(originalLuminance, 0, luminanceCopy, 0, originalLuminance.length);
        readyForNextImage();

        final Canvas canvas = new Canvas(croppedBitmap);

        canvas.drawBitmap(rgbFrameBitmap, frameToCropTransform, null); // paint the cropped image

        //canvas.drawBitmap(rgbFrameBitmap,
        //        new Rect(0, 0, rgbFrameBitmap.getWidth(), rgbFrameBitmap.getWidth()),
        //        new Rect(0, 0, MP_INPUT_SIZE, MP_INPUT_SIZE), null); // RT: Paint the background

//        // For examining the actual TF input.
//        if (SAVE_PREVIEW_BITMAP) {
//            ImageUtils.saveBitmap(croppedBitmap);
//        }

        runInBackground(
                new Runnable() {
                    @Override
                    public void run() {
                        LOGGER.i("Running detection on image " + currTimestamp);
                        final long startTime = SystemClock.uptimeMillis();

                        final List<Classifier.Recognition> results = detector.recognizeImage(croppedBitmap);

                        lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime;
                        lastHumansFound = results.get(0).humans.size();
                        LOGGER.i("Running detection on image (DONE) in " + lastProcessingTimeMs);

                        //cropCopyBitmap = Bitmap.createBitmap(results.get(0).heat);
                        //cropCopyBitmap = Bitmap.createBitmap(results.get(0).pose);

                        cropCopyBitmap = Bitmap.createBitmap(croppedBitmap);
                        final Canvas canvas = new Canvas(cropCopyBitmap);
                        draw_humans(canvas, results.get(0).humans);

//                        final Paint paint = new Paint();
//                        paint.setColor(Color.RED);
//                        paint.setStyle(Style.STROKE);
//                        paint.setStrokeWidth(2.0f);
//
//                        final List<Classifier.Recognition> mappedRecognitions =
//                                new LinkedList<Classifier.Recognition>();
//
//                        for (final Classifier.Recognition result : results) {
//                            final RectF location = result.getLocation();
//                            if (location != null) {
//                                canvas.drawRect(location, paint);
//
//                                cropToFrameTransform.mapRect(location);
//                                result.setLocation(location);
//                                mappedRecognitions.add(result);
//                            }
//                        }

                        //trackingOverlay.postInvalidate();

                        requestRender();
                        computingDetection = false;
                    }
                });
    }

    private Integer HUMAN_RADIUS = 3;

    private void draw_humans(Canvas canvas, List<TensorFlowPoseDetector.Human> human_list) {
        //def draw_humans(img, human_list):
        // image_h, image_w = img_copied.shape[:2]
        int cp = Common.CocoPart.values().length;
        int image_w = canvas.getWidth();
        int image_h = canvas.getHeight();

        //    for human in human_list:
        for (TensorFlowPoseDetector.Human human : human_list) {
            Point[] centers = new Point[cp]; // 포즈 좌표
            //part_idxs = human.keys()
            Set<Integer> part_idxs = human.parts.keySet();

            float[] pose_point = new float[38]; // 포즈 좌표 저장할 배열

            LOGGER.i("COORD =====================================");
            //# draw point
            //for i in range(CocoPart.Background.value):
            for (Common.CocoPart i : Common.CocoPart.values()) {
                //if i not in part_idxs:
                if (!part_idxs.contains(i.index)) {
                    LOGGER.w("COORD %s, NULL, NULL", i.toString());
                    continue;
                }
                //part_coord = human[i][1]
                TensorFlowPoseDetector.Coord part_coord = human.parts.get(i.index);
                //center = (int(part_coord[0] * image_w + 0.5), int(part_coord[1] * image_h + 0.5))
                Point center = new Point((int) (part_coord.x * image_w + 0.5f), (int) (part_coord.y * image_h + 0.5f));


                // 검출된 값 저장
                if(part_coord.x == NULL) {
                    pose_point[i.index*2] = 0;
                    pose_point[i.index*2 + 1] = 0;
                }
                else {
                    pose_point[i.index*2] = round(part_coord.x * image_w + 0.5f);
                    pose_point[i.index*2 + 1] = round(part_coord.y * image_h + 0.5f);
                }

                //centers[i] = center
                centers[i.index] = center;

                //cv2.circle(img_copied, center, 3, CocoColors[i], thickness=3, lineType=8, shift=0)
                Paint paint = new Paint();
                paint.setColor(Color.rgb(Common.CocoColors[i.index][0], Common.CocoColors[i.index][1], Common.CocoColors[i.index][2]));
                paint.setStyle(Style.FILL);
                canvas.drawCircle(center.x, center.y, HUMAN_RADIUS, paint);

                LOGGER.i("COORD %s, %f, %f", i.toString(), part_coord.x, part_coord.y);
            }
            System.out.println("maybe "+ Arrays.toString(pose_point)); // 확인용 출력코드

            // 모델 구동
//            Interpreter tflite = getTfliteInterpreter("simple_1.tflite");
            Interpreter tflite = getTfliteInterpreter("converted_model2.tflite");
//            tflite.run(pose_point, model_output);
            float[] input_value;
//            input_value = new float[]{232,88,208,136,184,136,160,168,176,168,224,152,224,200,224,240,168,176,168,216,160,280,200,184,216,208,200,272,192,160};
            input_value = Arrays.copyOfRange(pose_point, 0, 28);
            tflite.run(input_value, outt);

            // outt에 예측값 중 최댓값의 인덱스 찾기
            float[] outt_sort = outt[0];
            float max = outt_sort[0];
            int maxIndex = 0;
            for (int i = 0; i < outt_sort.length; i++) {
                if (outt_sort[i] > max) {
                    max = outt_sort[i];
                    maxIndex = i;
                }
            }
            // 최댓값 인덱스에 해당하는 포즈를 Pose_result에 저장
            String[] classes = {"Stand","Squat","Lying","LegUp","Plank"};
            Pose_result = classes[maxIndex];

            //# draw line
            //for pair_order, pair in enumerate(CocoPairsRender):
            for (int pair_order = 0; pair_order < Common.CocoPairsRender.length; pair_order++) {
                int[] pair = Common.CocoPairsRender[pair_order];
                //if pair[0] not in part_idxs or pair[1] not in part_idxs:
                if (!part_idxs.contains(pair[0]) || !part_idxs.contains(pair[1])) {
                    continue;
                }

                //img_copied = cv2.line(img_copied, centers[pair[0]], centers[pair[1]], CocoColors[pair_order], 3)
                Paint paint = new Paint();
                paint.setColor(Color.rgb(Common.CocoColors[pair_order][0], Common.CocoColors[pair_order][1], Common.CocoColors[pair_order][2]));
                paint.setStrokeWidth(HUMAN_RADIUS);
                paint.setStyle(Style.STROKE);

                canvas.drawLine(centers[pair[0]].x, centers[pair[0]].y, centers[pair[1]].x, centers[pair[1]].y, paint);
            }
        }
        //    return img_copied
    }

    // tflite 때문에 추가한 함수 1
    private Interpreter getTfliteInterpreter(String modelPath) {
        try {
            return new Interpreter(loadModelFile(MocapActivity.this, modelPath));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // tflite 때문에 추가한 함수 2
    private MappedByteBuffer loadModelFile(Activity activity, String modelPath) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(modelPath);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }



    @Override
    protected int getLayoutId() {
        return R.layout.camera_connection_fragment;
    }

    @Override
    protected Size getDesiredPreviewFrameSize() {
        return DESIRED_PREVIEW_SIZE;
    }

    @Override
    public void onSetDebug(final boolean debug) {
        detector.enableStatLogging(debug);
    }
}
