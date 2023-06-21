package org.byborgip.pingservice.service;

import com.google.gson.Gson;
import org.byborgip.pingservice.config.AppConfig;
import org.byborgip.pingservice.dto.ReportDto;
import java.io.IOException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ReportSender {
    private final AppConfig config;

    public ReportSender(AppConfig config) {
        this.config = config;
    }

    public CompletableFuture<Void> sendReportAsync(ReportDto reportData){
        return CompletableFuture.runAsync(() -> sendReport(reportData));

    }
    public void sendReport(ReportDto reportData) {
        String reportUrl = config.getProperty("report.url");

        try {
            URL url = new URL(reportUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            sendReport(reportData, connection);
            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    public void sendReport(ReportDto reportData, HttpURLConnection connection) {
//        String json = convertToJson(reportData);
//
//        try {
//            connection.setRequestMethod("POST");
//            connection.setRequestProperty("Content-Type", "application/json");
//            connection.setDoOutput(true);
//
//            // Send the request
//            connection.getOutputStream().write(json.getBytes());
//
//            int responseCode = connection.getResponseCode();
//
//            // Handle the response
//            System.out.println("------------------RESPONSE(Inbound)-----------------------------");
//            System.out.println("Response Code: " + responseCode);
//            System.out.println("Response headers:");
//            Map<String, List<String>> responseHeaders = connection.getHeaderFields();
//            for (Map.Entry<String, List<String>> entry : responseHeaders.entrySet()) {
//                System.out.println(entry.getKey() + ": " + entry.getValue());
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }


    void sendReport(ReportDto reportData, HttpURLConnection connection) {
        String json = convertToJson(reportData);

        try {

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            System.out.println("--------------------REQUEST(Outbound)---------------------------");
            System.out.println("Sending POST request to URL: " + config.getProperty("report.url"));
            System.out.println("Request headers:");
            Map<String, List<String>> requestHeaders = connection.getRequestProperties();
            for (Map.Entry<String, List<String>> entry : requestHeaders.entrySet()) {
                System.out.println(entry.getKey() + ": " + entry.getValue());
            }
            System.out.println("Request body:");
            System.out.println(json);

            // Send the request

         connection.getOutputStream().write(json.getBytes());

            int responseCode = connection.getResponseCode();

            System.out.println("------------------RESPONSE(Inbound)-----------------------------");
            System.out.println("Response Code: " + responseCode);
            System.out.println("Response headers:");
            Map<String, List<String>> responseHeaders = connection.getHeaderFields();
            for (Map.Entry<String, List<String>> entry : responseHeaders.entrySet()) {
                System.out.println(entry.getKey() + ": " + entry.getValue());
            }

            connection.disconnect();
        } catch (IOException e) {
            System.out.println(e.getMessage());
            //e.printStackTrace();
        }
    }


    private String convertToJson(ReportDto reportData) {
        Gson gson = new Gson();
        return gson.toJson(reportData);
    }
}