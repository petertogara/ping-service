package org.byborgip.pingservice;


import org.byborgip.pingservice.config.AppConfig;
import org.byborgip.pingservice.service.PingManager;
import org.byborgip.pingservice.service.ReportSender;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class App {
    public static void main(String[] args) {

        AppConfig config = new AppConfig();
        ReportSender reportSender =  new ReportSender(config);
        ExecutorService executorService = Executors.newFixedThreadPool(config.getHosts().size());
        PingManager pingManager = new PingManager(config, reportSender, executorService);

        pingManager.start();
    }

}