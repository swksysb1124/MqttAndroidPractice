package crop.computer.askey.mqttactivemqpractice.model.iot;

public class ReportedWifiConfig {

    public String serialNumber;
    public String ssid;
    public String password;

    public ReportedWifiConfig(String serialNumber, String ssid, String password) {
        this.serialNumber = serialNumber;
        this.ssid = ssid;
        this.password = password;
    }
}
