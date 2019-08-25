package crop.computer.askey.mqttactivemqpractice.presenter;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Locale;

import crop.computer.askey.mqttactivemqpractice.model.mqtt.MqttConnection;
import crop.computer.askey.mqttactivemqpractice.model.mqtt.MqttMessage;
import crop.computer.askey.mqttactivemqpractice.model.mqtt.MqttState;
import crop.computer.askey.mqttactivemqpractice.service.MyMqttService;
import crop.computer.askey.mqttactivemqpractice.view.MainView;

public class MainPresenter {
    private Activity activity;
    private MainView mainView;

    public MainPresenter(Activity activity, MainView mainView) {
        this.activity = activity;
        this.mainView = mainView;
    }

    public void connect() {
        final String serverUri = mainView.getServerURL();
        final String clientId = mainView.getClientID();
        final String username = mainView.getUserName();
        final String password = mainView.getPassword();

        Log.d("MainPresenter", String.format(Locale.US,
                "url=%s, client_id=%s, username=%s password=%s",
                serverUri, clientId, username, password));

        Intent intent = new Intent(activity, MyMqttService.class);
        intent.setAction(MyMqttService.ACTION_CONNECT);
        intent.putExtra(MyMqttService.KEY_SERVER_URL, serverUri);
        intent.putExtra(MyMqttService.KEY_CLIENT_ID, clientId);
        intent.putExtra(MyMqttService.KEY_USERNAME, username);
        intent.putExtra(MyMqttService.KEY_PASSWORD, password);
        activity.startService(intent);
    }

    public void subscribe() {
        final String topic = mainView.getTopic();

        Intent intent = new Intent(activity, MyMqttService.class);
        intent.setAction(MyMqttService.ACTION_SUBSCRIBE);
        intent.putExtra(MyMqttService.KEY_TOPIC_TO_SUBSCRIBE, topic);
        activity.startService(intent);
    }

    public void publish() {
        final String topic = mainView.getTopic();
        final String message = mainView.getPublishedMessage();

        Intent intent = new Intent(activity, MyMqttService.class);
        intent.setAction(MyMqttService.ACTION_PUBLISH);
        intent.putExtra(MyMqttService.KEY_TOPIC_TO_PUBLISH, topic);
        intent.putExtra(MyMqttService.KEY_PUBLISHED_MESSAGE, message);
        activity.startService(intent);
    }


    public void onStart() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    public void onStop() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMqttConnection(MqttConnection event) {
        mainView.updateConnection(event.connection);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMqttState(MqttState event) {
        mainView.updateState(event.state);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMqttMessage(MqttMessage event) {
        mainView.updateRecord(event.message);
    }
}
