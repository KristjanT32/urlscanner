package krisapps.urlchecker.urlchecker;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import krisapps.urlchecker.urlchecker.enums.ScanResult;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.net.ssl.HttpsURLConnection;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.*;
import java.util.Date;
import java.util.HashMap;

public class CheckerController {
    long start;
    int links_n = 0;
    int videos_n = 0;
    int images_n = 0;
    int loopedVideos_n = 0;
    int iframes_n = 0;
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
    static Scene sceneInstance;
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
    static Stage stageInstance;
    File file = new File("C:/Users/" + user + "/Desktop/website_html.txt");
    String user = System.getProperty("user.name");
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
    @FXML
    private Label label_progressdetails;
    @FXML
    private CheckBox toggle_reportfile;
    @FXML
    private CheckBox toggle_showlog;

    public static EventHandler<WindowEvent> onWindowClosed() {
        return event -> {
            System.out.println("Stopping all processes...");
            stageInstance.setTitle("Closing program...");
            System.exit(0);
        };
    }

    public static void delay(long millis, Runnable continuation) {
        Task<Void> sleeper = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    Thread.sleep(millis);
                } catch (InterruptedException ignored) {
                }
                return null;
            }
        };
        sleeper.setOnSucceeded(event -> continuation.run());
        new Thread(sleeper).start();
    }

    public void refreshSetupInfo() {
        label_statusvalue.setText("All good!");
        label_statusvalue.setTextFill(Paint.valueOf("lime"));
        if (url_field.getText().isEmpty()) {
            if (!label_statusvalue.getText().contains("No URL provided")) {
                label_statusvalue.setText(label_statusvalue.getText().replace("All good!", "") + "No URL provided\n");
                label_statusvalue.setTextFill(Paint.valueOf("red"));
            } else {
                label_statusvalue.setTextFill(Paint.valueOf("red"));
            }
        }

        if (!toggle_screamertest.isSelected() && toggle_createfile.isSelected()) {
            if (!label_statusvalue.getText().contains("Enable Screamer Test to download HTML")) {
                label_statusvalue.setText(label_statusvalue.getText().replace("All good!", "") + "Enable Screamer Test to download HTML\n");
                label_statusvalue.setTextFill(Paint.valueOf("red"));
            } else {
                label_statusvalue.setTextFill(Paint.valueOf("red"));
            }
        }

        if (toggle_screamertest.isSelected() && !toggle_novideos.isSelected() && !toggle_noimages.isSelected()) {
            if (!label_statusvalue.getText().contains("Select scan options for Screamer Test")) {
                label_statusvalue.setText(label_statusvalue.getText().replace("All good!", "") + "Select scan options for Screamer Test\n");
                label_statusvalue.setTextFill(Paint.valueOf("red"));
            } else {
                label_statusvalue.setTextFill(Paint.valueOf("red"));
            }
        }

        if (!toggle_screamertest.isSelected() && (toggle_novideos.isSelected() || toggle_noimages.isSelected())) {
            if (!label_statusvalue.getText().contains("Enable Screamer Test to use selected options")) {
                label_statusvalue.setText(label_statusvalue.getText().replace("All good!", "") + "Enable Screamer Test to use selected options\n");
                label_statusvalue.setTextFill(Paint.valueOf("red"));
            } else {
                label_statusvalue.setTextFill(Paint.valueOf("red"));
            }
        }

        if (!toggle_screamertest.isSelected() && !toggle_basicinfo.isSelected()) {
            if (!label_statusvalue.getText().contains("No options have been selected.")) {
                label_statusvalue.setText(label_statusvalue.getText().replace("All good!", "") + "No options have been selected.\n");
                label_statusvalue.setTextFill(Paint.valueOf("red"));
            } else {
                label_statusvalue.setTextFill(Paint.valueOf("red"));
            }
        }

        if (!url_field.getText().startsWith("https://")) {
            if (!label_statusvalue.getText().contains("Please include 'https://' in front of your URL.")) {
                label_statusvalue.setText(label_statusvalue.getText().replace("All good!", "") + "Please include 'https://' in front of your URL.\n");
                label_statusvalue.setTextFill(Paint.valueOf("red"));
            } else {
                label_statusvalue.setTextFill(Paint.valueOf("red"));
            }
        }

        if (label_statusvalue.getText().trim().equals("All good!")) {
            button_start.setDisable(false);
            button_start.setText("Test the URL!");
        } else {
            button_start.setDisable(true);
            button_start.setText("Correct the errors to start the test");
        }

    }

    void procedure() {
        log.setVisible(toggle_showlog.isSelected());
        stageInstance.setTitle("Processing your link...");
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
            stageInstance.setTitle("URL Checker");
            scanResult = ScanResult.ERROR;
            return;
        }

        HttpsURLConnection connection = null;
        try {
            connection = (HttpsURLConnection) url.openConnection();
            log("Successfully established a connection to webpage.", "Retrieving data...");
            AnalysysThread analysysThread = new AnalysysThread(connection);
            analysysThread.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            connection.connect();
        } catch (IOException e) {
            log("Could not open the connection: " + e.getMessage(), "Failed to check the URL!");
            label_scan.setText("Failed to check the URL!");
            stageInstance.setTitle("URL Checker");
            scanProgress.setProgress(1);
            scanResult = ScanResult.INCONCLUSIVE;
        }
    }

    void log(String text) {
        log.setText(log.getText() + "\n" + text);
        operation_label.setText(text);
    }

    void log(String text, String labelText) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                log.setText(log.getText() + "\n" + text);
                label_progressdetails.setText(labelText);
            }
        });
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

    private String iframesFound() {
        if (toggle_screamertest.isSelected()) {
            return String.valueOf(iframes_n);
        } else {
            return "N/A";
        }
    }

    public void reset() {
        infoString = "";
        scanProgress.setProgress(0);
        log.clear();
        operation_label.setText("Please wait...");
        label_scan.setText("Working...");
        label_progressdetails.setText("Progress details will appear here...");
        stageInstance.setTitle("URL Checker");
        setupPanel.setVisible(true);
        scanPanel.setVisible(false);
    }


    String listLinks(Elements links) {
        StringBuilder out = new StringBuilder();
        for (Element link : links) {
            out.append("'" + link.absUrl("href") + "'\n");
        }
        return out.toString();
    }

    String listImages(Elements images) {
        StringBuilder out = new StringBuilder();
        for (Element image : images) {
            out.append("'" + image.absUrl("src") + "'\n");
        }
        return out.toString();
    }

    String listVideos(Elements videos) {
        StringBuilder out = new StringBuilder();
        for (Element video : videos) {
            out.append("'" + video.absUrl("src") + "'\n");
        }
        return out.toString();
    }

    String listIframes(Elements iframes) {
        StringBuilder out = new StringBuilder();
        for (Element iframe : iframes) {
            out.append("IFrame '" + iframe.absUrl("src") + "'\n");
        }
        return out.toString();
    }

    public void startTest() {
        setupPanel.setVisible(false);
        scanPanel.setVisible(true);
        delay(500, this::procedure);
    }

    class AnalysysThread extends Thread {
        HttpsURLConnection connection;

        AnalysysThread(HttpsURLConnection connection) {
            this.connection = connection;
        }

        @Override
        public void run() {
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
                        Elements iframes = website.select("iframe");

                        Elements videos = website.select("img[src$=.gif]");
                        videos.addAll(website.select("img[src$=.mov]"));
                        videos.addAll(website.select("img[src$=.mp4]"));
                        videos.addAll(website.select("img[src$=.avi]"));
                        videos.addAll(website.select("video"));

                        Elements loopedVideos = website.select("video[loop]");

                        links_n = links.size();
                        images_n = images.size();
                        videos_n = videos.size();
                        loopedVideos_n = loopedVideos.size();
                        iframes_n = iframes.size();

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

                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            if (toggle_basicinfo.isSelected()) {
                                urlInfo.put("content_type", connection.getContentType());
                                urlInfo.put("content_encoding", connection.getContentEncoding());
                                urlInfo.put("server", connection.getHeaderField("Server"));
                            }
                            scanProgress.setProgress(.8);
                        }
                    });

                    log("Evaluating...", "Evaluating results...");
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {

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
                                    label_scan.setText("ERROR (something may have forced the scan to stop)");
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
                        }
                    });

                    if (toggle_reportfile.isSelected()) {
                        log("Generating a report onto your desktop...", "Generating a report...");
                        Document website = Jsoup.parse(connection.getURL(), 30000);
                        Elements links = website.select("a[href]");
                        Elements images = website.select("img");
                        Elements iframes = website.select("iframe");

                        Elements videos = website.select("img[src$=.gif]");
                        videos.addAll(website.select("img[src$=.mov]"));
                        videos.addAll(website.select("img[src$=.mp4]"));
                        videos.addAll(website.select("img[src$=.avi]"));
                        videos.addAll(website.select("video"));

                        Elements loopedVideos = website.select("video[loop]");

                        links_n = links.size();
                        images_n = images.size();
                        videos_n = videos.size();
                        iframes_n = iframes.size();

                        loopedVideos_n = loopedVideos.size();

                        File report = new File("C:/Users/" + user + "/Desktop/website_report.txt");
                        FileWriter fw = new FileWriter(report);
                        fw.write("# Website Report for '" + connection.getURL() + "' #\n"
                                + "Report took place on " + new Date() + "\n"
                                + "Detected links[" + links_n + "]\n" + listLinks(links) + "\n"
                                + "============================================================\n"
                                + "Detected Images[" + images_n + "]\n" + listImages(images) + "\n"
                                + "============================================================\n"
                                + "Detected Videos (Looped/Not looped)[" + (videos_n + loopedVideos_n) + "]\n" + listVideos(videos) + "\n"
                                + "============================================================\n"
                                + "Detected IFrames (possibly contain a video)[" + iframes_n + "]\n" + listIframes(iframes) + "\n"
                                + "============================================================\n"
                                + "HTTP Status Code: " + connection.getResponseCode() + " (" + connection.getResponseMessage() + ")\n"
                                + "Final Scan Conclusion: " + scanResult.name() + "\n"
                                + "# END REPORT #"
                        );
                        fw.flush();
                        fw.close();
                    }


                    log("Scan complete.", "Completed!");
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            stageInstance.setTitle("URL Checker");
                            scanProgress.setProgress(1);
                        }
                    });

                } else {
                    log("HTTP returned a non-200 response.", "Website did not respond in an expected way.");
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {

                            try {
                                switch (connection.getResponseCode()) {

                                    case 404:
                                        label_scan.setText("Requested content could not be found.");
                                        label_scan.setTextFill(Paint.valueOf("#f5a836"));
                                        scanResult = ScanResult.INCONCLUSIVE;
                                        break;

                                    case 401:
                                    case 403:
                                        label_scan.setText("Website requires authorization or declined the request.");
                                        label_scan.setTextFill(Paint.valueOf("#8142f5"));
                                        scanResult = ScanResult.INCONCLUSIVE;
                                        break;

                                    case 307:
                                    case 308:
                                        label_scan.setText("The website wants to redirect us.");
                                        label_scan.setTextFill(Paint.valueOf("#f542aa"));
                                        scanResult = ScanResult.INCONCLUSIVE;
                                        break;

                                    case 400:
                                        label_scan.setText("Website couldn't understand the gibberish.");
                                        label_scan.setTextFill(Paint.valueOf("#9c1736"));
                                        scanResult = ScanResult.ERROR;
                                        break;

                                    case 408:
                                        label_scan.setText("Website decided not to hurry so we ran out of time.");
                                        label_scan.setTextFill(Paint.valueOf("#2b610f"));
                                        scanResult = ScanResult.INCONCLUSIVE;
                                        break;

                                    default:
                                        label_scan.setText("HTTP " + connection.getResponseCode() + ": " + connection.getResponseMessage());
                                        label_scan.setTextFill(Paint.valueOf("#32b6bf"));
                                        scanResult = ScanResult.ERROR;
                                        log("Scan could not continue because the website server returned " + connection.getResponseCode() + " in regards to " + connection.getURL());
                                        break;

                                }
                            } catch (IOException e) {
                                log("No response code was returned.", "The website did not provide any response.");
                            }
                            stageInstance.setTitle("URL Checker");
                            scanProgress.setProgress(1);
                            button_finish.setDisable(false);
                        }
                    });
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
                            + "\nIframes (could contain a video, check report file): " + iframesFound()
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

            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    operation_label.setText(infoString);
                }
            });
        }

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