package crop.computer.askey.mqttactivemqpractice.model.mqtt;

import android.app.Activity;
import android.content.Intent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import crop.computer.askey.mqttactivemqpractice.service.MyMqttService;

public class MqttModel {

    private boolean isConnected = false;

    public void start() {
        registerEvent();
    }

    public void stop() {
        unregisterEvent();
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void connect(Activity activity, String serverUri, String clientId, String username, String password) {
        Intent intent = new Intent(activity, MyMqttService.class);
        intent.setAction(MyMqttService.ACTION_CONNECT);
        intent.putExtra(MyMqttService.KEY_SERVER_URL, serverUri);
        intent.putExtra(MyMqttService.KEY_CLIENT_ID, clientId);
        intent.putExtra(MyMqttService.KEY_USERNAME, username);
        intent.putExtra(MyMqttService.KEY_PASSWORD, password);
        activity.startService(intent);
    }

    public void disconnect(Activity activity) {
        Intent intent = new Intent(activity, MyMqttService.class);
        intent.setAction(MyMqttService.ACTION_DISCONNECT);
        activity.startService(intent);
    }

    public void subscribe(Activity activity, String topic) {
        Intent intent = new Intent(activity, MyMqttService.class);
        intent.setAction(MyMqttService.ACTION_SUBSCRIBE);
        intent.putExtra(MyMqttService.KEY_TOPIC_TO_SUBSCRIBE, topic);
        activity.startService(intent);
    }

    public void publish(Activity activity, String topic, String message) {
        Intent intent = new Intent(activity, MyMqttService.class);
        intent.setAction(MyMqttService.ACTION_PUBLISH);
        intent.putExtra(MyMqttService.KEY_TOPIC_TO_PUBLISH, topic);
        intent.putExtra(MyMqttService.KEY_PUBLISHED_MESSAGE, message);
        activity.startService(intent);
    }

    public void registerEvent() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    public void unregisterEvent() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMqttConnection(MqttConnection event) {
        if (event != null) {
            isConnected = event.connection.equals("CONNECTED");
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMqttState(MqttState event) {
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMqttMessage(MqttMessage event) {
    }
}
