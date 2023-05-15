package com.autojob.open_cv;

import javafx.util.Pair;
import nu.pattern.OpenCV;
import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
// imports:
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;

import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Rect2d;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import javax.swing.*;

/**
 * Created by OpenYourEyes on 18/04/2023
 */
public class OpenCVResolveCaptcha {

//    public static void main(String[] args) {
//        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
//    }

    public static Point getPoint() {
        OpenCV.loadShared();
        Mat source = null;
        Mat template = null;
        String filePath = System.getProperty("user.dir") + "/image/";
        Pair<Mat, Mat> images = images(filePath);
        source = images.getKey();
        template = images.getValue();
        Mat outputImage = new Mat();
        int machMethod = Imgproc.TM_CCOEFF;
        Imgproc.matchTemplate(source, template, outputImage, machMethod);


        Core.MinMaxLocResult mmr = Core.minMaxLoc(outputImage);
        Point start = mmr.minLoc;
        Point end = new Point(start.x + template.cols(),
                start.y + template.rows());
        Imgproc.rectangle(source, start, end, new Scalar(255, 255, 255));
        Imgcodecs.imwrite(filePath + "result.jpg", source);
        double x = start.x;
        double y = start.y;
        final double value = 552 * 1.0 / 340;
        return new Point(x / value, y / value);

    }


    static Pair<Mat, Mat> images(String filePath) {
        Mat source = Imgcodecs.imread(filePath + "bg.jpeg", Imgcodecs.IMREAD_COLOR);
        Mat template = Imgcodecs.imread(filePath + "img.png", Imgcodecs.IMREAD_COLOR);
        return new Pair<>(source, template);
    }

    static Pair<Mat, Mat> images2(String filePath, int index) {
        Mat source = Imgcodecs.imread(filePath + String.format("bg%s.jpeg", index), Imgcodecs.IMREAD_COLOR);
        Mat template = Imgcodecs.imread(filePath + String.format("img%s.png", index), Imgcodecs.IMREAD_COLOR);
        return new Pair<>(source, template);
    }

    private void matchingMethod(Mat img, Mat templ, int match_method) {
        Mat result = new Mat();
        Mat img_display = new Mat();
        img.copyTo(img_display);
        int result_cols = img.cols() - templ.cols() + 1;
        int result_rows = img.rows() - templ.rows() + 1;
        result.create(result_rows, result_cols, CvType.CV_32FC1);
        Boolean method_accepts_mask = (Imgproc.TM_SQDIFF == match_method || match_method == Imgproc.TM_CCORR_NORMED);
        Imgproc.matchTemplate(img, templ, result, match_method);
//        if (use_mask && method_accepts_mask) {
//            Imgproc.matchTemplate(img, templ, result, match_method, mask);
//        } else {
//            Imgproc.matchTemplate(img, templ, result, match_method);
//        }
        Core.normalize(result, result, 0, 1, Core.NORM_MINMAX, -1, new Mat());
        Point matchLoc;
        Core.MinMaxLocResult mmr = Core.minMaxLoc(result);
        if (match_method == Imgproc.TM_SQDIFF || match_method == Imgproc.TM_SQDIFF_NORMED) {
            matchLoc = mmr.minLoc;
        } else {
            matchLoc = mmr.maxLoc;
        }
        Imgproc.rectangle(img_display, matchLoc, new Point(matchLoc.x + templ.cols(), matchLoc.y + templ.rows()),
                new Scalar(0, 0, 0), 2, 8, 0);
        Imgproc.rectangle(result, matchLoc, new Point(matchLoc.x + templ.cols(), matchLoc.y + templ.rows()),
                new Scalar(0, 0, 0), 2, 8, 0);
        Image tmpImg = HighGui.toBufferedImage(img_display);
        ImageIcon icon = new ImageIcon(tmpImg);
        result.convertTo(result, CvType.CV_8UC1, 255.0);
        tmpImg = HighGui.toBufferedImage(result);
        icon = new ImageIcon(tmpImg);
    }

    private static List<Mat> checkMat(Mat mat) {
        List<Mat> mats = new ArrayList<>();
        if (CvType.CV_8UC1 == mat.type() || CvType.CV_8UC3 == mat.type()) {
            mats.add(mat);
        } else if (CvType.CV_8UC4 == mat.type()) {
            List<Mat> matsBGRA = new ArrayList<>();
            Core.split(mat, matsBGRA);
            Mat mBGR = new Mat(mat.size(), CvType.CV_8UC3);
            Mat matA = matsBGRA.remove(3);
            Core.merge(matsBGRA, mBGR);
            mats.add(mBGR);
            MatOfDouble mStdDev = new MatOfDouble();
            Core.meanStdDev(matA, new MatOfDouble(), mStdDev);
            if (0 < mStdDev.toArray()[0]) {
                mats.add(matA);
            }
        }
        return mats;
    }

    public void detectObjectOnImage() throws FileNotFoundException {

        //  load the COCO class labels our YOLO model was trained on
        Scanner scan = new Scanner(new FileReader("d:\\Eclipse_2019_06\\myWork\\OpenCV_Demo\\yolo-coco\\coco.names"));
        List<String> cocoLabels = new ArrayList<String>();
        while (scan.hasNextLine()) {
            cocoLabels.add(scan.nextLine());
        }

        //  load our YOLO object detector trained on COCO dataset
        Net dnnNet = Dnn.readNetFromDarknet("d:\\Eclipse_2019_06\\myWork\\OpenCV_Demo\\yolo-coco\\yolov3.cfg",
                "d:\\Eclipse_2019_06\\myWork\\OpenCV_Demo\\yolo-coco\\yolov3.weights");
        // YOLO on GPU:
        dnnNet.setPreferableBackend(Dnn.DNN_BACKEND_CUDA);
        dnnNet.setPreferableTarget(Dnn.DNN_TARGET_CUDA);

        // generate radnom color in order to draw bounding boxes
        Random random = new Random();
        ArrayList<Scalar> colors = new ArrayList<Scalar>();
        for (
                int i = 0; i < cocoLabels.size(); i++) {
            colors.add(new Scalar(new double[]{random.nextInt(255), random.nextInt(255), random.nextInt(255)}));
        }


        // load our input image
        Mat img = Imgcodecs.imread("d:\\Eclipse_2019_06\\myWork\\OpenCV_Demo\\images\\dining_table.jpg", Imgcodecs.IMREAD_COLOR); // dining_table.jpg soccer.jpg baggage_claim.jpg
        //  -- determine  the output layer names that we need from YOLO
        // The forward() function in OpenCV’s Net class needs the ending layer till which it should run in the network.
        //  getUnconnectedOutLayers() vraca indexe za: yolo_82, yolo_94, yolo_106, (indexi su 82, 94 i 106) i to su poslednji layeri
        // u networku:
        List<String> layerNames = dnnNet.getLayerNames();
        List<String> outputLayers = new ArrayList<String>();
        for (
                Integer i : dnnNet.getUnconnectedOutLayers().

                toList()) {
            outputLayers.add(layerNames.get(i - 1));
        }

        HashMap<String, List> result = forwardImageOverNetwork(img, dnnNet, outputLayers);

        ArrayList<Rect2d> boxes = (ArrayList<Rect2d>) result.get("boxes");
        ArrayList<Float> confidences = (ArrayList<Float>) result.get("confidences");
        ArrayList<Integer> class_ids = (ArrayList<Integer>) result.get("class_ids");

        // -- Now , do so-called “non-maxima suppression”
        //Non-maximum suppression is performed on the boxes whose confidence is equal to or greater than the threshold.
        // This will reduce the number of overlapping boxes:
        MatOfInt indices = getBBoxIndicesFromNonMaximumSuppression(boxes,
                confidences);
        //-- Finally, go over indices in order to draw bounding boxes on the image:
        img =

                drawBoxesOnTheImage(img,
                        indices,
                        boxes,
                        cocoLabels,
                        class_ids,
                        colors);
        HighGui.imshow("Test", img);
        HighGui.waitKey(10000);
    }


    private HashMap<String, List> forwardImageOverNetwork(Mat img,
                                                          Net dnnNet,
                                                          List<String> outputLayers) {
        // --We need to prepare some data structure  in order to store the data returned by the network  (ie, after Net.forward() call))
        // So, Initialize our lists of detected bounding boxes, confidences, and  class IDs, respectively
        // This is what this method will return:
        HashMap<String, List> result = new HashMap<String, List>();
        result.put("boxes", new ArrayList<Rect2d>());
        result.put("confidences", new ArrayList<Float>());
        result.put("class_ids", new ArrayList<Integer>());

        // -- The input image to a neural network needs to be in a certain format called a blob.
        //  In this process, it scales the image pixel values to a target range of 0 to 1 using a scale factor of 1/255.
        // It also resizes the image to the given size of (416, 416) without cropping
        // Construct a blob from the input image and then perform a forward  pass of the YOLO object detector,
        // giving us our bounding boxes and  associated probabilities:

        Mat blob_from_image = Dnn.blobFromImage(img, 1 / 255.0, new Size(416, 416), // Here we supply the spatial size that the Convolutional Neural Network expects.
                new Scalar(new double[]{0.0, 0.0, 0.0}), true, false);
        dnnNet.setInput(blob_from_image);

        // -- the output from network's forward() method will contain a List of OpenCV Mat object, so lets prepare one
        List<Mat> outputs = new ArrayList<Mat>();

        // -- Finally, let pass forward throught network. The main work is done here:
        dnnNet.forward(outputs, outputLayers);

        // --Each output of the network outs (ie, each row of the Mat from 'outputs') is represented by a vector of the number
        // of classes + 5 elements.  The first 4 elements represent center_x, center_y, width and height.
        // The fifth element represents the confidence that the bounding box encloses the object.
        // The remaining elements are the confidence levels (ie object types) associated with each class.
        // The box is assigned to the category corresponding to the highest score of the box:

        for (Mat output : outputs) {
            //  loop over each of the detections. Each row is a candidate detection,
            System.out.println("Output.rows(): " + output.rows() + ", Output.cols(): " + output.cols());
            for (int i = 0; i < output.rows(); i++) {
                Mat row = output.row(i);
                List<Float> detect = new MatOfFloat(row).toList();
                List<Float> score = detect.subList(5, output.cols());
                int class_id = argmax(score); // index maximalnog elementa liste
                float conf = score.get(class_id);
                if (conf >= 0.5) {
                    int center_x = (int) (detect.get(0) * img.cols());
                    int center_y = (int) (detect.get(1) * img.rows());
                    int width = (int) (detect.get(2) * img.cols());
                    int height = (int) (detect.get(3) * img.rows());
                    int x = (center_x - width / 2);
                    int y = (center_y - height / 2);
                    Rect2d box = new Rect2d(x, y, width, height);
                    result.get("boxes").add(box);
                    result.get("confidences").add(conf);
                    result.get("class_ids").add(class_id);
                }
            }
        }
        return result;
    }

    /**
     * Returns index of maximum element in the list
     */
    private int argmax(List<Float> array) {

        float max = array.get(0);
        int re = 0;
        for (
                int i = 1; i < array.size(); i++) {
            if (array.get(i) > max) {
                max = array.get(i);
                re = i;
            }
        }
        return re;
    }

    private MatOfInt getBBoxIndicesFromNonMaximumSuppression(ArrayList<Rect2d> boxes,
                                                             ArrayList<Float> confidences) {
        MatOfRect2d mOfRect = new MatOfRect2d();
        mOfRect.fromList(boxes);
        MatOfFloat mfConfs = new MatOfFloat(Converters.vector_float_to_Mat(confidences));
        MatOfInt result = new MatOfInt();
        Dnn.NMSBoxes(mOfRect, mfConfs, (float) (0.6), (float) (0.5), result);
        return result;
    }

    private Mat drawBoxesOnTheImage(Mat img,
                                    MatOfInt indices,
                                    ArrayList<Rect2d> boxes,
                                    List<String> cocoLabels,
                                    ArrayList<Integer> class_ids,
                                    ArrayList<Scalar> colors) {
        //Scalar color = new Scalar( new double[]{255, 255, 0});
        List indices_list = indices.toList();
        for (int i = 0; i < boxes.size(); i++) {
            if (indices_list.contains(i)) {
                Rect2d box = boxes.get(i);
                Point x_y = new Point(box.x, box.y);
                Point w_h = new Point(box.x + box.width, box.y + box.height);
                Point text_point = new Point(box.x, box.y - 5);
                Imgproc.rectangle(img, w_h, x_y, colors.get(class_ids.get(i)), 1);
                String label = cocoLabels.get(class_ids.get(i));
                Imgproc.putText(img, label, text_point, Imgproc.FONT_HERSHEY_SIMPLEX, 1, colors.get(class_ids.get(i)), 2);
            }
        }
        return img;
    }

}
