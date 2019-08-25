# MqttAndroidPractice
使用 Eclipse Paho Android Service 實現 MQTT Client

## MQTT Client (Android)

### 加入Gradle依賴
```groovy
implementation 'org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.0'
implementation 'org.eclipse.paho:org.eclipse.paho.android.service:1.1.1'
```

### 在Manifest宣告MqttService
```groovy
<application
    android:allowBackup="true"
    android:icon="@mipmap/ic_launcher"
    android:label="@string/app_name"
    android:roundIcon="@mipmap/ic_launcher_round"
    android:supportsRtl="true"
    android:theme="@style/AppTheme">
        
    <service 
        android:name="org.eclipse.paho.android.service.MqttService" />
    ...
        
</application>
```
### 使用 啓動式服務 包裹 MqttAndroidClient 
```java
public class MyMqttService extends Service {

    private boolean mIsMqttConnected;
    private MqttAndroidClient mMqttClient;

    private static final String TAG = MyMqttService.class.getSimpleName();
    ...


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

    ...
}
```
