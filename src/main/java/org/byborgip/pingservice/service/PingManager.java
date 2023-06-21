package org.byborgip.pingservice.service;

import org.byborgip.pingservice.config.AppConfig;
import org.byborgip.pingservice.dto.PingDto;
import org.byborgip.pingservice.dto.ReportDto;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public class PingManager {
    private final AppConfig config;
    private final List<PingDto> pingDataList;
    private final ExecutorService executorService;
    private final ReportSender reportSender;

    public PingManager(AppConfig config, ReportSender reportSender, ExecutorService executorService) {
        this.config = config;
        this.pingDataList = new ArrayList<>();
        this.executorService = executorService;
        this.reportSender = reportSender;
    }
    public void start() {
        int delay = Integer.parseInt(config.getProperty("ping.delay"));

        while (true) {
            performPing();
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void performPing() {
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (String host : config.getHosts()) {
            CompletableFuture<Void> icmpFuture = CompletableFuture.runAsync(() -> performICMPPing(host), executorService)
                    .thenRun(() -> {
                        PingDto icmpPingData = getPingDataByHost(host);
                        System.out.println("ICMP Ping Result for host " + host + ":");
                        System.out.println(icmpPingData.getIcmpPingResult());
                        System.out.println("------------------------------");
                    })
                    .exceptionally(ex -> {
                        System.err.println("Exception occurred during ICMP ping:");
                        ex.printStackTrace();
                        return null;
                    });

            CompletableFuture<Void> tcpFuture = CompletableFuture.runAsync(() -> performTCPPing(host), executorService)
                    .thenRun(() -> {
                        PingDto tcpPingData = getPingDataByHost(host);
                        System.out.println("TCP/IP Ping Result for host " + host + ":");
                        System.out.println(tcpPingData.getTcpPingResponseCode());
                        System.out.println("------------------------------");
                    })
                    .exceptionally(ex -> {
                        System.err.println("Exception occurred during TCP/IP ping:");
                        ex.printStackTrace();
                        return null;
                    });

            CompletableFuture<Void> traceFuture = CompletableFuture.runAsync(() -> performTraceRoute(host), executorService)
                    .thenRun(() -> {
                        PingDto traceData = getPingDataByHost(host);
                        System.out.println("Trace Route Result for host " + host + ":");
                        System.out.println(traceData.getTraceRouteResult());
                        System.out.println("------------------------------");
                    })
                    .exceptionally(ex -> {
                        System.err.println("Exception occurred during trace route:");
                        ex.printStackTrace();
                        return null;
                    });

            futures.add(icmpFuture);
            futures.add(tcpFuture);
            futures.add(traceFuture);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenRunAsync(this::sendReports, executorService)
                .exceptionally(ex -> {
                    System.err.println("Exception occurred while sending reports:");
                    ex.printStackTrace();
                    return null;
                })
                .join(); // Wait for completion of sendReports


    }



    public void performICMPPing(String host) {
        try {

            Process process = Runtime.getRuntime().exec(config.getIcmpCommand() + " " + host);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            StringBuilder pingResult = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                pingResult.append(line).append("\n");
            }

            process.waitFor();
            int exitValue = process.exitValue();

            if (exitValue == 0) {
                updatePingData(host, pingResult.toString(), LocalDateTime.now());
            } else {

                reportAsync(host);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void performTCPPing(String host) {
        try {

            Process process = Runtime.getRuntime().exec(config.getTcpCommand() + " " + host);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            StringBuilder pingResult = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                pingResult.append(line).append("\n");
            }

            process.waitFor();
            int exitValue = process.exitValue();

            if (exitValue == 0) {
                String[] lines = pingResult.toString().split("\n");
                String[] responseCodeLine = lines[lines.length - 1].split(" ");
                int responseCode = 0;

                try {
                    responseCode = Integer.parseInt(responseCodeLine[responseCodeLine.length - 1]);
                } catch (NumberFormatException e) {
                    System.err.println("Failed to parse response code for host: " + host);
                    // e.printStackTrace();
                }

                updatePingData(host, responseCode, LocalDateTime.now());
            } else {

                reportAsync(host);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void performTraceRoute(String host) {
        try {

            Process process = Runtime.getRuntime().exec(config.getTraceRouteCommand() + " " + host);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            StringBuilder traceResult = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                traceResult.append(line).append("\n");
            }

            process.waitFor();
            int exitValue = process.exitValue();

            if (exitValue == 0) {
                updatePingData(host, traceResult.toString(), LocalDateTime.now());
            } else {

                reportAsync(host);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void updatePingData(String host, String icmpPingResult, LocalDateTime timestamp) {
        synchronized (pingDataList) {
            PingDto pingDto = getPingData(host);
            pingDto.setIcmpPingResult(icmpPingResult);
        }
    }

    public void updatePingData(String host, int responseCode, LocalDateTime timestamp) {
        synchronized (pingDataList) {
            PingDto pingData = getPingData(host);
            pingData.setTcpPingResponseCode(responseCode);
        }
    }

    public PingDto getPingData(String host) {
        synchronized (pingDataList) {
            return pingDataList.stream()
                    .filter(pingData -> pingData.getHost().equals(host))
                    .findFirst()
                    .orElseGet(() -> {
                        PingDto newPingData = new PingDto(host);
                        pingDataList.add(newPingData);
                        return newPingData;
                    });
        }

    }
    public void sendReports() {
        synchronized (pingDataList) {
            for (PingDto pingData : pingDataList) {
                String host = pingData.getHost();
                String icmpPingResult = pingData.getIcmpPingResult();
                int tcpPingResponseCode = pingData.getTcpPingResponseCode();
                String traceRouteResult = pingData.getTraceRouteResult();

                // Prepare the report data
                ReportDto reportData = new ReportDto();
                reportData.setHost(host);
                reportData.setIcmpPingResult(icmpPingResult);
                reportData.setTcpPingResult("Response Code: " + tcpPingResponseCode);
                reportData.setTraceRouteResult(traceRouteResult);

                // Log the report
                logReport(reportData);

                // Send the report
                reportSender.sendReport(reportData);
            }
        }
    }

    public PingDto getPingDataByHost(String host) {
        synchronized (pingDataList) {
            return pingDataList.stream()
                    .filter(pingData -> pingData.getHost().equals(host))
                    .findFirst()
                    .orElse(null);
        }
    }

    private String getTraceResult(PingDto pingData) {
        // Return the trace route result from the PingDto object
        return pingData.getTraceRouteResult();
    }
    public CompletableFuture<Void> reportAsync(String host){
        return CompletableFuture.runAsync(() -> report(host));

    }

    private void report(String host) {
        synchronized (pingDataList) {
            PingDto pingData = getPingData(host);

            // Prepare report data
            ReportDto reportData = new ReportDto();
            reportData.setHost(host);
            reportData.setIcmpPingResult(pingData.getIcmpPingResult());
            reportData.setTcpPingResult(
                    "Response Code: " + pingData.getTcpPingResponseCode()
            );
            reportData.setTraceRouteResult(pingData.getTraceRouteResult());

            // Log the report
            logReport(reportData);

            // Send the report
            reportSender.sendReportAsync(reportData);

        }
    }

    private void logReport(ReportDto reportData) {
        if (reportData != null) {
            String log = "[WARNING] Report: " + reportData.toString();
            String logFileName = config.getProperty("log.file");

            try (FileWriter fileWriter = new FileWriter(logFileName +".log", true)) {
                fileWriter.write(log + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
