package com.ltm2022client.application;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.Iterator;
import java.util.ResourceBundle;

import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

public class ImageProcessingController implements Initializable {
    public static File inputImageFile = null;
    public static File outputImageFile = null;

    @FXML
    private Button changeExtensionBtn;

    @FXML
    private Button choosePictureBtn;

    @FXML
    private Button compressBtn;

    @FXML
    private Button findWithGoogleBtn;

    @FXML
    private Button greyScaleBtn;

    @FXML
    private AnchorPane mainPane;

    @FXML
    private Button objectDetectBtn;

    @FXML
    private ImageView originImageView;

    @FXML
    private ImageView processedImageView;

    @FXML
    private Button refreshBtn;
    @FXML
    private AnchorPane subContentPane;

    private Stage primaryStage;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setDefault();
        Handler();
    }

    public void Handler() {
        choosePictureBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
//                FileDialog dialog = new FileDialog((Frame) null, "Chọn ảnh để xử lí");
//                dialog.setMode(FileDialog.LOAD);
//                dialog.setVisible(true);
//                imageFile = dialog.getFiles()[0];
//                dialog.dispose();
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Chọn 1 ảnh để xử lý");
                fileChooser.getExtensionFilters().addAll(
                        new FileChooser.ExtensionFilter("PNG", "*.png"),
                        new FileChooser.ExtensionFilter("JPG", "*.jpg"),
                        new FileChooser.ExtensionFilter("JPEG", "*.jpeg")
                );
                primaryStage = (Stage) mainPane.getScene().getWindow();
                inputImageFile = fileChooser.showOpenDialog(primaryStage);
                if (inputImageFile != null) {
                    Image img = new Image(String.valueOf(inputImageFile));
                    originImageView.setImage(img);
                    setDefault();
                }
            }
        });

        refreshBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                inputImageFile = null;
                outputImageFile = null;
                originImageView.setImage(null);
                processedImageView.setImage(null);
                setDefault();
            }
        });

        greyScaleBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (inputImageFile != null) {
                    try {
                        BufferedImage imgIn = ImageIO.read(inputImageFile);
                        // get image's width and height
                        int width = imgIn.getWidth();
                        int height = imgIn.getHeight();

                        // convert to greyscale
                        for (int y = 0; y < height; y++) {
                            for (int x = 0; x < width; x++) {
                                // Here (x,y)denotes the coordinate of image
                                // for modifying the pixel value.
                                int p = imgIn.getRGB(x, y);
                                int a = (p >> 24) & 0xff;
                                int r = (p >> 16) & 0xff;
                                int g = (p >> 8) & 0xff;
                                int b = p & 0xff;
                                // calculate average
                                int avg = (r + g + b) / 3;
                                // replace RGB value with avg
                                p = (a << 24) | (avg << 16) | (avg << 8) | avg;
                                imgIn.setRGB(x, y, p);
                            }
                        }

                        // write image
                        try {
                            outputImageFile = new File(inputImageFile.getParentFile() + "/" + getFileName(inputImageFile) + "_greyscale." + getFileExtension(inputImageFile));
                            ImageIO.write(imgIn, getFileExtension(inputImageFile), outputImageFile);
                            Image processedImage = new Image(String.valueOf(outputImageFile));
                            processedImageView.setImage(processedImage);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        objectDetectBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
                CascadeClassifier faceDetector = new CascadeClassifier();
                String path = System.getProperty("user.dir");
                faceDetector.load(path + "/src/main/java/com/ltm2022client/application/haarcascade_frontalface_alt.xml");
                Mat image = Imgcodecs.imread(String.valueOf(inputImageFile));
                MatOfRect faceDetections = new MatOfRect();
                faceDetector.detectMultiScale(image, faceDetections);
                for (Rect rect : faceDetections.toArray()) {
                    Imgproc.rectangle(
                            image, new Point(rect.x, rect.y),
                            new Point(rect.x + rect.width,
                                    rect.y + rect.height),
                            new Scalar(0, 255, 0));
                }

                String filename = inputImageFile.getParentFile() + "/" + getFileName(inputImageFile) + "_facedetect." + getFileExtension(inputImageFile);

                Imgcodecs.imwrite(filename, image);
                outputImageFile = new File(filename);
                System.out.print("Face Detected");
                Image processedImage = new Image(String.valueOf(outputImageFile));
                processedImageView.setImage(processedImage);
            }
        });

        changeExtensionBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    Stage newStage = new Stage();
                    ImageFormatMain imageFormat = new ImageFormatMain();
                    //ngăn tương tác với dashboard
                    Stage oldStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    newStage.initModality(Modality.WINDOW_MODAL);
                    newStage.initOwner(oldStage);
                    //chạy newStage
                    imageFormat.start(newStage);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        compressBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {

                try {
                    BufferedImage image = ImageIO.read(inputImageFile);
                    outputImageFile = new File(inputImageFile.getParentFile() + "/" + getFileName(inputImageFile) + "_compress." + getFileExtension(inputImageFile));

                    OutputStream os = new FileOutputStream(outputImageFile);
                    Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(getFileExtension(inputImageFile));
                    ImageWriter writer = (ImageWriter) writers.next();

                    ImageOutputStream ios = ImageIO.createImageOutputStream(os);
                    writer.setOutput(ios);

                    ImageWriteParam param = writer.getDefaultWriteParam();

                    param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                    param.setCompressionQuality(0.5f);
                    writer.write(null, new IIOImage(image, null, null), param);

                    os.close();
                    ios.close();
                    writer.dispose();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

//        findWithGoogleBtn.setOnAction(new EventHandler<ActionEvent>() {
//            @Override
//            public void handle(ActionEvent event) {
//                // Initially assigning null
//                BufferedImage imgA = null;
//                BufferedImage imgB = null;
//
//                // Try block to check for exception
//                try {
//
//                    // Reading file from local directory by
//                    // creating object of File class
//                    File fileA = new File("D:\\Pictures\\SS\\2.jpg");
//                    File fileB = new File("D:\\Pictures\\SS\\2_greyscale.jpg");
//
//                    // Reading files
//                    imgA = ImageIO.read(fileA);
//                    imgB = ImageIO.read(fileB);
//                }
//
//                // Catch block to check for exceptions
//                catch (IOException e) {
//                    // Display the exceptions on console
//                    System.out.println(e);
//                }
//
//                // Assigning dimensions to image
//                int width1 = imgA.getWidth();
//                int width2 = imgB.getWidth();
//                int height1 = imgA.getHeight();
//                int height2 = imgB.getHeight();
//
//                // Checking whether the images are of same size or
//                // not
//                if ((width1 != width2) || (height1 != height2))
//
//                    // Display message straightaway
//                    System.out.println("Error: Images dimensions"
//                            + " mismatch");
//                else {
//
//                    // By now, images are of same size
//
//                    long difference = 0;
//
//                    // treating images likely 2D matrix
//
//                    // Outer loop for rows(height)
//                    for (int y = 0; y < height1; y++) {
//
//                        // Inner loop for columns(width)
//                        for (int x = 0; x < width1; x++) {
//
//                            int rgbA = imgA.getRGB(x, y);
//                            int rgbB = imgB.getRGB(x, y);
//                            int redA = (rgbA >> 16) & 0xff;
//                            int greenA = (rgbA >> 8) & 0xff;
//                            int blueA = (rgbA) & 0xff;
//                            int redB = (rgbB >> 16) & 0xff;
//                            int greenB = (rgbB >> 8) & 0xff;
//                            int blueB = (rgbB) & 0xff;
//
//                            difference += Math.abs(redA - redB);
//                            difference += Math.abs(greenA - greenB);
//                            difference += Math.abs(blueA - blueB);
//                        }
//                    }
//
//                    // Total number of red pixels = width * height
//                    // Total number of blue pixels = width * height
//                    // Total number of green pixels = width * height
//                    // So total number of pixels = width * height *
//                    // 3
//                    double total_pixels = width1 * height1 * 3;
//
//                    // Normalizing the value of different pixels
//                    // for accuracy
//
//                    // Note: Average pixels per color component
//                    double avg_different_pixels
//                            = difference / total_pixels;
//
//                    // There are 255 values of pixels in total
//                    double percentage
//                            = (avg_different_pixels / 255) * 100;
//
//                    // Lastly print the difference percentage
//                    System.out.println("Difference Percentage-->"
//                            + percentage);
//                }
//            }
//        });
    }

    public String getFileName(File file) {
        return file.getName().split("\\.")[0];
    }

    public String getFileExtension(File file) {
        return file.getName().split("\\.")[1];
    }

    public void setDefault() {
        if (inputImageFile == null) {
            changeExtensionBtn.setDisable(true);
            compressBtn.setDisable(true);
            findWithGoogleBtn.setDisable(true);
            greyScaleBtn.setDisable(true);
            objectDetectBtn.setDisable(true);
            refreshBtn.setDisable(true);
            choosePictureBtn.setDisable(false);
        } else {
            changeExtensionBtn.setDisable(false);
            compressBtn.setDisable(false);
            findWithGoogleBtn.setDisable(false);
            greyScaleBtn.setDisable(false);
            objectDetectBtn.setDisable(false);
            refreshBtn.setDisable(false);
            choosePictureBtn.setDisable(true);
        }
    }
}
