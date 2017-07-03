package oxchains.fabric;

/**
 * Test
 *
 * @author liuruichao
 * Created on 2017/7/1 23:31
 */
public class Test {
    public static void main(String[] args) {
        String str = "saveSensorData,{\"SensorNumber\": \"cgq20170330001\",\"SensorType\": \"HD-3K1\",     \"EquipmentNumber\": \"sb20170330000\",     \"EquipmentType\": \"what\",     \"Time\": 1490155871000,     \"Temperature\": [         12.2,         12.3     ],     \"Humidity\": [         20.3,         20.4     ],     \"GPSLongitude\": 113.653056,\"GPSLatitude\": 34.860076,\"Address\": \"what\"}";
        System.out.println(str.replaceAll(" ", ""));
    }
}
