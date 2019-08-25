package crop.computer.askey.mqttactivemqpractice.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.greenrobot.eventbus.EventBus;

import java.util.Arrays;

import crop.computer.askey.mqttactivemqpractice.model.mqtt.MqttConnection;
import crop.computer.askey.mqttactivemqpractice.model.mqtt.MqttState;

public class MyMqttService extends Service {

    private boolean mIsMqttConnected;
    private MqttAndroidClient mMqttClient;

    private static final String TAG = MyMqttService.class.getSimpleName();

    public static final String ACTION_CONNECT = "MyMqttService.action.connect";
    public static final String ACTION_DISCONNECT = "MyMqttService.action.disconnect";
    public static final String ACTION_SUBSCRIBE = "MyMqttService.action.subscribe";
    public static final String ACTION_PUBLISH = "MyMqttService.action.publish";

    public static final String KEY_CLIENT_ID = "MyMqttService.key.mqtt.client.id";
    public static final String KEY_USERNAME = "MyMqttService.key.mqtt.username";
    public static final String KEY_PASSWORD = "MyMqttService.key.mqtt.password";
    public static final String KEY_SERVER_URL = "MyMqttService.key.mqtt.server.url";

    public static final String KEY_TOPIC_TO_SUBSCRIBE = "MyMqttService.key.mqtt.topic.to.subscribe";
    public static final String KEY_TOPIC_TO_PUBLISH = "MyMqttService.key.mqtt.topic.to.publish";
    public static final String KEY_PUBLISHED_MESSAGE = "MyMqttService.key.mqtt.published.data";

    public static final String TOPIC_LAST_WILL_TREAMENT = "MyMqttService.topic.last.will.treament";

    public MyMqttService() {
        mIsMqttConnected = false;
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate()");
        super.onCreate();

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand()");
        if (intent != null && intent.getAction() != null) {
            String action = intent.getAction();
            switch (action) {
                case ACTION_CONNECT:
                    String serverUrl = intent.getStringExtra(KEY_SERVER_URL);
                    String clientId = intent.getStringExtra(KEY_CLIENT_ID);
                    String username = intent.getStringExtra(KEY_USERNAME);
                    String password = intent.getStringExtra(KEY_PASSWORD);
                    connect(serverUrl, clientId, username, password);
                    break;

                case ACTION_DISCONNECT:
                    disconnect();
                    break;

                case ACTION_SUBSCRIBE:
                    String topicToSubscribe = intent.getStringExtra(KEY_TOPIC_TO_SUBSCRIBE);
                    subscribeToTopic(topicToSubscribe);
                    break;

                case ACTION_PUBLISH:
                    String topicToPublish = intent.getStringExtra(KEY_TOPIC_TO_PUBLISH);
                    String messageToPublish = intent.getStringExtra(KEY_PUBLISHED_MESSAGE);
                    publishMessage(topicToPublish, messageToPublish);
                    break;
            }
        }
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy()");
        super.onDestroy();
        if (mMqttClient != null) {
            try {
                mMqttClient.disconnect();
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    protected void connect(final String serverUri, final String clientId,
                           final String username, final String password) {
        Log.i(TAG, "connect()");

        if (!checkValidString(serverUri) || !checkValidString(clientId)) {
            toast("Please check your client id & server host");
            return;
        }

        if (!checkValidString(username) || !checkValidString(password)) {
            toast("Please check your username & password");
            return;
        }

        Log.d(TAG, "connect to server: " + serverUri + ", client id: " + clientId);

        mMqttClient = new MqttAndroidClient(getApplicationContext(), serverUri, clientId);

        mMqttClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                Log.w(TAG, "Connect Complete");
                updateState("Connect Complete");
                toast("Connect Complete");
            }

            @Override
            public void connectionLost(Throwable cause) {
                String state = "Connect Lost";
                if (cause != null) {
                    state = ",\nCause: " + cause.getMessage();
                }
                Log.e(TAG, state);
                updateState(state);
                toast(state);
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                String info = "Message Arrived," +
                        "\nTopic: " + topic + "," +
                        "\nQos: " + message.getQos() + "," +
                        "\nPayload:" + new String(message.getPayload());
                Log.w(TAG, info);
                updateRecord(topic, new String(message.getPayload()));
                toast(info);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                Log.w(TAG, "Delivery Complete");
                updateState("Delivery Complete");
                toast("Delivery Complete");
            }
        });
        MqttConnectOptions mqttConnectOptions = getMqttConnectOptions(username, password,
                true, false,
                10, 20);

        try {
            // 開啟連線
            mMqttClient.connect(mqttConnectOptions, null,
                    new IMqttActionListener() {
                        @Override
                        public void onSuccess(IMqttToken asyncActionToken) {
                            Log.w(TAG, "Connect Success, asyncActionTokens="
                                    + Arrays.toString(asyncActionToken.getTopics()));

                            setIsMqttConnected(true);
                            updateConnection("CONNECTED");
                            toast("CONNECTED");

                            subscribeToTopic(TOPIC_LAST_WILL_TREAMENT);
                        }

                        @Override
                        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                            Log.w(TAG, "Connect Failure, asyncActionTokens="
                                    + Arrays.toString(asyncActionToken.getTopics()));

                            setIsMqttConnected(false);
                            updateConnection("CONNECT FAILURE");
                            toast("CONNECT FAILURE");
                        }
                    });
        } catch (MqttException ex) {
            ex.printStackTrace();
        }
    }

    private void disconnect() {
        Log.i(TAG, "disconnect()");

        if (mMqttClient != null) {
            if (mMqttClient.isConnected()) {
                try {
                    IMqttToken token = mMqttClient.disconnect();
                    token.setActionCallback(new IMqttActionListener() {
                        @Override
                        public void onSuccess(IMqttToken asyncActionToken) {
                            toast("DISCONNECT SUCCESS");
                        }

                        @Override
                        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                            toast("DISCONNECT FAILURE");
                        }
                    });
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Get MQTT Connection Options
     *
     * @param username
     * @param password
     * @return
     */
    private MqttConnectOptions getMqttConnectOptions(String username,
                                                     String password,
                                                     boolean autoReconnect,
                                                     boolean cleanSession,
                                                     int timeout,
                                                     int aliveInterval) {

        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(autoReconnect); // 自動連線
        mqttConnectOptions.setCleanSession(cleanSession); // 清除Session
        mqttConnectOptions.setConnectionTimeout(timeout); // 逾時時間
        mqttConnectOptions.setKeepAliveInterval(aliveInterval); // 存活時間
        mqttConnectOptions.setWill(TOPIC_LAST_WILL_TREAMENT, "I will offline".getBytes(), 1, true); // 設定LWT
        mqttConnectOptions.setUserName(username); // 使用者名稱
        mqttConnectOptions.setPassword(password.toCharArray()); // 密碼
        return mqttConnectOptions;
    }

    protected void subscribeToTopic(final String topic) {
        Log.i(TAG, "subscribeToTopic()");
        if (!mIsMqttConnected) {
            toast("Connection not existed, you cannot subscribe topic");
            Log.w(TAG, "Connection not existed, you cannot subscribe topic");
            return;
        }

        if (topic.isEmpty()) {
            toast("Empty topic");
            Log.e(TAG, "Empty topic");
            return;
        }

        Log.d(TAG, "subscribe topic: " + topic);

        try {
            mMqttClient.subscribe(topic, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    String success = "Subscribe Success," +
                            "\nTopic: " + topic;

                    Log.w(TAG, success);

                    toast(success);
                    updateState("Subscribe Success");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.w(TAG, "Subscribe Topic: " + topic + " Failure");
                    String error = "Subscribe Failure," +
                            "\nTopic: " + topic;
                    if (exception != null) {
                        error += ",\nCause: " + exception.getMessage();
                    }

                    toast(error);
                    updateState("Subscribe Failure");
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    protected synchronized void setIsMqttConnected(final boolean connected) {
        mIsMqttConnected = connected;
    }

    protected void publishMessage(final String topic, final String message) {
        Log.i(TAG, "publishMessage()");
        if (!mIsMqttConnected) {
            toast("Connection not existed, you cannot publish data");
            return;
        }

        if (topic.isEmpty()) {
            toast("Empty topic");
            Log.e(TAG, "Empty topic");
            return;
        }

        if (message.isEmpty()) {
            toast("Empty data");
            Log.e(TAG, "Empty data");
            return;
        }

        Log.d(TAG, "publish topic: " + topic + " with: " + message);

        try {
            MqttMessage mqttMessage = new MqttMessage();
            mqttMessage.setPayload(message.getBytes());

            mMqttClient.publish(topic, mqttMessage, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    String success = "Publish Success," +
                            "\nTopic: " + topic + "," +
                            "\nMessage: " + message;

                    Log.w(TAG, success);
                    toast(success);
                    updateState("Publish Success");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    String error = "Publish Failure," +
                            "\nTopic: " + topic + "," +
                            "\nMessage: " + message;
                    if (exception != null) {
                        error += ",\nCause: " + exception.getMessage();
                    }

                    Log.e(TAG, error);
                    toast(error);
                    updateState("Publish Failure");
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }

    }

    protected void updateConnection(String connect) {
        EventBus.getDefault().post(new MqttConnection(connect));
    }

    protected void updateState(String state) {
        EventBus.getDefault().post(new MqttState(state));
    }

    protected void updateRecord(String topic, String message) {
        EventBus.getDefault().post(new crop.computer.askey.mqttactivemqpractice.model.mqtt.MqttMessage(topic, message));
    }

    protected void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private boolean checkValidString(String str) {
        return str != null && !str.isEmpty();
    }
}
