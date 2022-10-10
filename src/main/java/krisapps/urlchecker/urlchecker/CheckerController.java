package krisapps.urlchecker.urlchecker;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.stage.WindowEvent;
import krisapps.urlchecker.urlchecker.enums.ScanResult;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import javax.net.ssl.HttpsURLConnection;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.*;
import java.util.HashMap;

public class CheckerController {
    long start;
    int links_n = 0;
    int videos_n = 0;
    int images_n = 0;
    int loopedVideos_n = 0;
    HashMap<String, String> urlInfo = new HashMap<String, String>();
    ScanResult scanResult = ScanResult.SAFE;
    String infoString;
    double progress = 0;
    // Panels
    @FXML
    private Pane content;
    @FXML
    private Pane setupPanel;
    @FXML
    private Pane status_container;
    @FXML
    private VBox window;
    @FXML
    private Pane scanPanel;
    // Labels
    @FXML
    private Label window_label;
    @FXML
    private Label window_subtitle;
    @FXML
    private Label setup_label;
    @FXML
    private Label label_status;
    @FXML
    private Label label_statusvalue;
    @FXML
    private Label operation_label;
    @FXML
    private Label label_scan;
    // Buttons
    @FXML
    private Button button_start;

    // Code
    @FXML
    private Button button_finish;
    //Checkboxes
    @FXML
    private CheckBox toggle_basicinfo;
    @FXML
    private CheckBox toggle_screamertest;
    @FXML
    private CheckBox toggle_noimages;
    @FXML
    private CheckBox toggle_novideos;
    @FXML
    private CheckBox toggle_createfile;
    //Textfields
    @FXML
    private TextField url_field;
    // Misc.
    @FXML
    private ProgressBar scanProgress;
    @FXML
    private TextArea log;

    public static EventHandler<WindowEvent> onWindowClosed() {
        return event -> {
            System.out.println("Window closed.");
            System.exit(0);
        };
    }

    public static EventHandler<WindowEvent> onWindowShown() {
        return event -> {
            System.out.println("Window shown.");
        };
    }

    public void refreshSetupInfo() {
        if (url_field.getText().isEmpty()) {
            label_statusvalue.setTextFill(Paint.valueOf("red"));
            label_statusvalue.setText("Please provide a URL");
        } else {
            label_statusvalue.setTextFill(Paint.valueOf("lime"));
            label_statusvalue.setText("All good");
        }
    }

    void procedure() {
        start = System.currentTimeMillis();
        scanProgress.setProgress(.1);
        label_scan.setText("Analyzing...");

        log("Checking: " + url_field.getText(), "Validating...");
        label_scan.setText("Validating...");
        URL url = null;
        try {
            url = new URL(url_field.getText().trim());
        } catch (MalformedURLException exception) {
            log.setText("The URL is invalid.");
            label_scan.setText("Invalid URL!");
            scanResult = ScanResult.ERROR;
            return;
        }

        HttpsURLConnection connection = null;
        try {
            connection = (HttpsURLConnection) url.openConnection();
            log("Successfully established a connection to webpage.", "Retrieving data...");
            getInfo(connection);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            connection.connect();
        } catch (IOException e) {
            log("Could not open the connection: " + e.getMessage(), "Failed to check the URL!");
            label_scan.setText("Failed to check the URL!");
            scanResult = ScanResult.POTENTIALLY_UNSAFE;
        }
    }

    private void getInfo(HttpsURLConnection connection) {

        String user = System.getProperty("user.name");
        File file = new File("C:/Users/" + user + "/Desktop/analysys.txt");

        scanProgress.setProgress(.3);
        try {
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                log("Content type: " + connection.getContentType(), "Determined content type");
                if (toggle_screamertest.isSelected()) {
                    log("Getting links and images...", "Checking HTML");

                    Document website = Jsoup.parse(connection.getURL(), 30000);

                    if (toggle_createfile.isSelected()) {
                        FileWriter fw = new FileWriter(file);
                        fw.write(website.html());
                        fw.flush();
                        fw.close();
                    }

                    Elements links = website.select("a[href]");
                    Elements images = website.select("img");
                    Elements videos = website.select("img[src$=.gif]");
                    videos = website.select("img[src$=.mov]");
                    videos = website.select("img[src$=.mp4]");
                    videos = website.select("img[src$=.avi]");
                    videos = website.select("video");
                    Elements loopedVideos = website.select("video[loop]");

                    links_n = links.size();
                    images_n = images.size();
                    videos_n = videos.size();
                    loopedVideos_n = loopedVideos.size();

                    for (String el : links.eachAttr("href")) {
                        log("Link: " + el, "Scanning links...");
                    }
                    for (String el : images.eachAttr("img")) {
                        log("Image: " + el, "Scanning images...");
                    }
                    for (String el : videos.eachAttr("img")) {
                        log("Image: " + el, "Scanning videos...");
                    }

                    int unsafeness_score = 0;

                    if (toggle_noimages.isSelected()) {
                        if (images.size() > 0) {
                            unsafeness_score += 1;
                        }
                        if (links.size() > 5) {
                            unsafeness_score += 1;
                        }
                    }
                    if (toggle_novideos.isSelected()) {
                        if (videos.size() > 0) {
                            unsafeness_score += 1;
                        }
                        if (links.size() > 5) {
                            unsafeness_score += 1;
                        }
                    }

                    if (unsafeness_score > 1 && unsafeness_score < 4) {
                        scanResult = ScanResult.UNSAFE;
                    } else if (unsafeness_score == 1) {
                        scanResult = ScanResult.POTENTIALLY_UNSAFE;
                    } else if (unsafeness_score == 0) {
                        scanResult = ScanResult.SAFE;
                    } else if (unsafeness_score > 2) {
                        scanResult = ScanResult.INCONCLUSIVE;
                    }


                }
                scanProgress.setProgress(.6);

                if (toggle_basicinfo.isSelected()) {
                    urlInfo.put("content_type", connection.getContentType());
                    urlInfo.put("content_encoding", connection.getContentEncoding());
                    urlInfo.put("server", connection.getHeaderField("Server"));
                }
                scanProgress.setProgress(.8);

                log("Evaluating...", "Evaluating results...");
                switch (scanResult) {

                    case SAFE:
                        label_scan.setTextFill(Paint.valueOf("lime"));
                        label_scan.setText("SAFE");
                        break;
                    case INCONCLUSIVE:
                        label_scan.setTextFill(Paint.valueOf("yellow"));
                        label_scan.setText("INCONCLUSIVE (a lot of links)");
                        break;
                    case ERROR:
                        label_scan.setTextFill(Paint.valueOf("blue"));
                        label_scan.setText("ERROR (scan did not complete)");
                        break;
                    case UNSAFE:
                        label_scan.setTextFill(Paint.valueOf("orange"));
                        label_scan.setText("UNSAFE");
                        break;
                    case POTENTIALLY_UNSAFE:
                        label_scan.setTextFill(Paint.valueOf("red"));
                        label_scan.setText("POTENTIALLY UNSAFE");
                        break;
                }
                log("Scan complete.", "Completed!");
                scanProgress.setProgress(1);

            } else {
                log("HTTP returned a non-200 response.", "Website did not respond.");
                scanResult = ScanResult.POTENTIALLY_UNSAFE;
                button_finish.setDisable(false);
            }
        } catch (IOException e) {
            System.out.println("ERR: " + e.getMessage());
            scanResult = ScanResult.ERROR;
        }


        if (toggle_basicinfo.isSelected()) {
            if (loopedVideos_n > 0) {
                operation_label.setText("Some looped videos were detected - the website may contain a jumpscare/screamer.");
                infoString = ""
                        + "Analysis Results\n"
                        + "\nWebsite Content Type: " + urlInfo.get("content_type")
                        + "\nWebsite Content Length: " + urlInfo.get("content_length")
                        + "\nWebsite Server: " + urlInfo.get("server")
                        + "\nLinks found: " + links_n
                        + "\nImages found: " + imagesFound()
                        + "\nVideos found (non-looped): " + videosFound()
                        + " of which " + loopedVideosFound() + " were looped."
                        + "\nConclusion: " + scanResult.name()
                ;
            } else {
                infoString = ""
                        + "Analysis Results\n"
                        + "\nWebsite Content Type: " + urlInfo.get("content_type")
                        + "\nWebsite Content Length: " + urlInfo.get("content_length")
                        + "\nWebsite Server: " + urlInfo.get("server")
                        + "\nLinks found: " + links_n
                        + "\nImages found: " + imagesFound()
                        + "\nVideos found (non-looped): " + videosFound()
                        + "\nConclusion: " + scanResult.name()
                ;
            }
        } else {
            if (loopedVideos_n > 0) {
                infoString = ""
                        + "Analysis Results\n"
                        + "\nLinks found: " + links_n
                        + "\nImages found: " + imagesFound()
                        + "\nVideos found (non-looped): " + videosFound()
                        + " of which " + loopedVideosFound() + " were looped."
                        + "\nConclusion: " + scanResult.name()
                ;
            } else {
                infoString = ""
                        + "Analysis Results\n"
                        + "\nLinks found: " + links_n
                        + "\nImages found: " + imagesFound()
                        + "\nVideos found (non-looped): " + videosFound()
                        + "\nConclusion: " + scanResult.name()
                ;
            }
        }

        operation_label.setText(infoString);
    }

    void log(String text) {
        log.setText(log.getText() + "\n" + text);
        operation_label.setText(text);
    }

    void log(String text, String labelText) {
        log.setText(log.getText() + "\n" + text);
        operation_label.setText(labelText);
    }

    String imagesFound() {
        if (toggle_screamertest.isSelected()) {
            return String.valueOf(images_n);
        } else {
            return "N/A";
        }
    }

    String videosFound() {
        if (toggle_screamertest.isSelected()) {
            return String.valueOf(videos_n);
        } else {
            return "N/A";
        }
    }

    String loopedVideosFound() {
        if (toggle_screamertest.isSelected()) {
            return String.valueOf(loopedVideos_n);
        } else {
            return "N/A";
        }
    }

    public void startTest() {
        setupPanel.setVisible(false);
        scanPanel.setVisible(true);
        procedure();
    }

    public void reset() {
        infoString = "";
        scanProgress.setProgress(0);
        log.clear();
        operation_label.setText("Waiting...");
        label_scan.setText("Waiting...");
        setupPanel.setVisible(true);
        scanPanel.setVisible(false);
    }


    private String checkSetupState() {
        if (url_field.getText().trim().isEmpty()) {
            return "No URL has been specified.";
        } else {
            if (!isValidURL(url_field.getText().trim())) {
                return "The specified URL is not valid.";
            }
        }
        return "All good.";
    }

    private boolean isValidURL(String url) {
        try {
            new URI(url_field.getText().trim()).parseServerAuthority();
            return true;
        } catch (URISyntaxException e) {
            return false;
        }
    }
}