package org.byborgip.pingservice.dto;


public class PingDto {
    private final String host;
    private String icmpPingResult;
    private int tcpPingResponseCode;
    private long tcpPingResponseTime;
    private String traceRouteResult;

    public PingDto(String host) {
        this.host = host;
    }

    public String getHost() {
        return host;
    }

    public String getIcmpPingResult() {
        return icmpPingResult;
    }

    public void setIcmpPingResult(String icmpPingResult) {
        this.icmpPingResult = icmpPingResult;
    }

    public int getTcpPingResponseCode() {
        return tcpPingResponseCode;
    }

    public void setTcpPingResponseCode(int tcpPingResponseCode) {
        this.tcpPingResponseCode = tcpPingResponseCode;
    }

    public long getTcpPingResponseTime() {
        return tcpPingResponseTime;
    }

    public void setTcpPingResponseTime(long tcpPingResponseTime) {
        this.tcpPingResponseTime = tcpPingResponseTime;
    }

    public String getTraceRouteResult() {
        return traceRouteResult;
    }

    public void setTraceRouteResult(String traceRouteResult) {
        this.traceRouteResult = traceRouteResult;
    }

    @Override
    public String toString() {
        return "PingData{" +
                "host='" + host + '\'' +
                ", icmpPingResult='" + icmpPingResult + '\'' +
                ", tcpPingResponseCode=" + tcpPingResponseCode +
                ", tcpPingResponseTime=" + tcpPingResponseTime +
                ", traceRouteResult='" + traceRouteResult + '\'' +
                '}';
    }
}

