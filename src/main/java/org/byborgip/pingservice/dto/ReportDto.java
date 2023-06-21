package org.byborgip.pingservice.dto;


public class ReportDto {
    private String host;
    private String icmpPingResult;
    private String tcpPingResult;
    private String traceRouteResult;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getIcmpPingResult() {
        return icmpPingResult;
    }

    public void setIcmpPingResult(String icmpPingResult) {
        this.icmpPingResult = icmpPingResult;
    }

    public String getTcpPingResult() {
        return tcpPingResult;
    }

    public void setTcpPingResult(String tcpPingResult) {
        this.tcpPingResult = tcpPingResult;
    }

    public String getTraceRouteResult() {
        return traceRouteResult;
    }

    public void setTraceRouteResult(String traceRouteResult) {
        this.traceRouteResult = traceRouteResult;
    }

    @Override
    public String toString() {
        return "ReportData{" +
                "host='" + host + '\'' +
                ", icmpPingResult='" + icmpPingResult + '\'' +
                ", tcpPingResult='" + tcpPingResult + '\'' +
                ", traceRouteResult='" + traceRouteResult + '\'' +
                '}';
    }
}
