package crop.computer.askey.mqttactivemqpractice.view;

public interface MainView {
    String getServerURL();
    String getTopic();
    String getPublishedMessage();
    String getClientID();
    String getUserName();
    String getPassword();
    void updateRecord(String message);
    void updateConnection(String connection);
    void updateState(String state);
}
