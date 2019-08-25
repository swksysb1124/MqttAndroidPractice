package crop.computer.askey.mqttactivemqpractice.model.iot;

import android.app.Activity;
import android.util.Log;

import com.google.gson.Gson;

import crop.computer.askey.mqttactivemqpractice.model.mqtt.MqttConnection;
import crop.computer.askey.mqttactivemqpractice.model.mqtt.MqttMessage;
import crop.computer.askey.mqttactivemqpractice.model.mqtt.MqttModel;
import crop.computer.askey.mqttactivemqpractice.model.mqtt.MqttState;

public class IotMqttModel extends MqttModel {

    private Gson gson = new Gson();
    private Activity activity;

    private Callback callback;

    public IotMqttModel(Activity activity) {
        this.activity = activity;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public void subscribeDesired(String deviceId) {
        subscribe(activity, "/" + deviceId + "/desired");
    }

    public void subscribeReported(String deviceId) {
        subscribe(activity, "/" + deviceId + "/reported");
    }

    public void publishDesired(String deviceId, Object object) {
        publish(activity, "/" + deviceId + "/desired", gson.toJson(object));
    }

    public void publishReported(String deviceId, Object object) {
        publish(activity, "/" + deviceId + "/reported", gson.toJson(object));
    }

    @Override
    public void onMqttConnection(MqttConnection event) {
        super.onMqttConnection(event);
    }

    @Override
    public void onMqttState(MqttState event) {
        super.onMqttState(event);
    }

    @Override
    public void onMqttMessage(MqttMessage event) {
        super.onMqttMessage(event);
        Log.i("IotMqttModel", "event.topic="+event.topic+", event.message="+event.message);
        if (event.topic.endsWith("/desired")) {
            callback.onDesiredChanged(event.message);
        }
        if (event.topic.endsWith("/reported")) {
            callback.onReportedChanged(event.message);
        }
    }

    public interface Callback {
        void onConnectionChanged(String connection);

        void onDesiredChanged(Object desired);

        void onReportedChanged(Object reported);
    }
}
