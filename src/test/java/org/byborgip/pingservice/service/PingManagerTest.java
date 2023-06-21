package org.byborgip.pingservice.service;
import org.byborgip.pingservice.config.AppConfig;
import org.byborgip.pingservice.dto.PingDto;
import org.byborgip.pingservice.dto.ReportDto;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class PingManagerTest {

    @Mock
    private AppConfig config;

    @Mock
    private ReportSender reportSender;

    @InjectMocks
    private PingManager pingManager;

    @Mock
    private ExecutorService executorService;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this); // Initialize the annotated mocks

        when(config.getHosts()).thenReturn(Arrays.asList("host1", "host2")); // Mocking the config.getHosts() method

        pingManager = new PingManager(config, reportSender, executorService);
    }


    @Test
    public void performPing_SendsReportsWithCorrectData() {
        // Mock config.getHosts() to return a list of hosts
        List<String> hosts = new ArrayList<>();
        hosts.add("host1");
        hosts.add("host2");
        when(config.getHosts()).thenReturn(hosts);

        // Mock getPingDataByHost() to return a sample PingDto for each host
        PingDto pingDto1 = new PingDto("host1");
        PingDto pingDto2 = new PingDto("host2");
        doReturn(pingDto1).when(pingManager).getPingDataByHost("host1");
        doReturn(pingDto2).when(pingManager).getPingDataByHost("host2");

        // Invoke performPing()
        pingManager.performPing();

        // Verify that reportSender.sendReport() is called twice with the correct ReportDto objects
        ArgumentCaptor<ReportDto> reportDtoCaptor = ArgumentCaptor.forClass(ReportDto.class);
        verify(reportSender, times(2)).sendReport(reportDtoCaptor.capture());

        List<ReportDto> capturedReports = reportDtoCaptor.getAllValues();
        assertEquals(2, capturedReports.size());

        ReportDto reportDto1 = capturedReports.get(0);
        assertEquals("host1", reportDto1.getHost());
        assertEquals(pingDto1.getIcmpPingResult(), reportDto1.getIcmpPingResult());
        assertEquals(pingDto1.getTcpPingResponseCode(), reportDto1.getTcpPingResult());
        assertEquals(pingDto1.getTraceRouteResult(), reportDto1.getTraceRouteResult());

        ReportDto reportDto2 = capturedReports.get(1);
        assertEquals("host2", reportDto2.getHost());
        assertEquals(pingDto2.getIcmpPingResult(), reportDto2.getIcmpPingResult());
        assertEquals(pingDto2.getTcpPingResponseCode(), reportDto2.getTcpPingResult());
        assertEquals(pingDto2.getTraceRouteResult(), reportDto2.getTraceRouteResult());
    }


}
