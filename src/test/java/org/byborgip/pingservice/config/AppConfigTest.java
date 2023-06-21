package org.byborgip.pingservice.config;



import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class AppConfigTest {

    @Test
    void getProperty_ExistingKey_ReturnsValue() {
        AppConfig appConfig = new AppConfig();
        String value = appConfig.getProperty("icmp.command");
        assertEquals("ping -c 4", value);
    }

    @Test
    void getProperty_NonexistentKey_ReturnsNull() {
        AppConfig appConfig = new AppConfig();
        String value = appConfig.getProperty("nonexistent.key");
        assertNull(value);
    }

}
