import org.apache.hadoop.conf.Configuration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HadoopConfigurationTest {

    @Test
    void testConfig1() {
        Configuration conf = new Configuration();
        conf.addResource("configuration-1.xml");
        assertEquals(conf.get("color"), "yellow");
        assertEquals(conf.getInt("size", 0), 10);
        assertEquals(conf.get("breadth", "wide"), "wide");
    }

    @Test
    void shouldOverrideConfigWhenLoadAnotherConfig() {
        Configuration conf = new Configuration();
        conf.addResource("configuration-1.xml");
        conf.addResource("configuration-2.xml");

        assertEquals(conf.getInt("size", 0), 12);
        assertEquals(conf.get("weight"), "heavy");

        assertEquals(conf.get("size-weight"), "12,heavy");
    }

    @Test
    void systemPropertyShouldPrimaryThenResourceFile() {
        System.setProperty("size", "14");
        Configuration conf = new Configuration();
        conf.addResource("configuration-1.xml");
        conf.addResource("configuration-2.xml");
        assertEquals(conf.get("size-weight"), "14,heavy");
    }
}
