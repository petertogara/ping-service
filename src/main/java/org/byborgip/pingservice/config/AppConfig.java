package org.byborgip.pingservice.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class AppConfig {

    private Properties properties;
    private static final String CONFIG_FILE = "application.properties";
    private static final String ICMP_COMMAND_KEY = "icmp.command";
    private static final String TCP_COMMAND_KEY = "tcp.command";
    private static final String TRACE_ROUTE_COMMAND_KEY = "trace.route.command";
    private static final String REPORT_URL_KEY = "report.url";
    private static final String HOSTS_KEY = "ping.hosts";

    public AppConfig() {
        loadProperties();
    }

    private void loadProperties() {
        properties = new Properties();
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public String getIcmpCommand() {
        return getProperty(ICMP_COMMAND_KEY);
    }

    public String getTcpCommand() {
        return getProperty(TCP_COMMAND_KEY);
    }

    public String getTraceRouteCommand() {
        return getProperty(TRACE_ROUTE_COMMAND_KEY);
    }

    public String getReportUrl() {
        return getProperty(REPORT_URL_KEY);
    }

    public List<String> getHosts() {
        List<String> hosts = new ArrayList<>();
        String hostsValue = getProperty(HOSTS_KEY);
        if (hostsValue != null && !hostsValue.isEmpty()) {
            String[] hostArray = hostsValue.split(",");
            for (String host : hostArray) {
                hosts.add(host.trim());
            }
        }
        return hosts;
    }
}
