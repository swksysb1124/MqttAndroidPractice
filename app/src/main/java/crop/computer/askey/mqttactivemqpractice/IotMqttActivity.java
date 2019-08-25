package crop.computer.askey.mqttactivemqpractice;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import crop.computer.askey.mqttactivemqpractice.model.iot.IotMqttModel;
import crop.computer.askey.mqttactivemqpractice.model.iot.DesiredWifiConfig;
import crop.computer.askey.mqttactivemqpractice.model.iot.ReportedWifiConfig;

public class IotMqttActivity extends AppCompatActivity {

    private IotMqttModel mqttModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iot_mqtt);

        mqttModel = new IotMqttModel(this);
        mqttModel.setCallback(new IotMqttModel.Callback() {
            @Override
            public void onConnectionChanged(String connection) {

            }

            @Override
            public void onDesiredChanged(Object desired) {
                updateDesired(desired);
            }

            @Override
            public void onReportedChanged(Object reported) {
                updateReported(reported);
            }
        });

        findViewById(R.id.btnConnect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String serverUrl = getServerURL();
                String clientId = getClientID();
                mqttModel.connect(IotMqttActivity.this,
                        serverUrl,
                        clientId,
                        getUserName(),
                        getPassword());

            }
        });

        findViewById(R.id.btnPublishDesired).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final View view = LayoutInflater
                        .from(IotMqttActivity.this).inflate(R.layout.dialog_pulish_desired, null);

                new AlertDialog.Builder(IotMqttActivity.this)
                        .setTitle("Publish Device Data")
                        .setView(view)
                        .setPositiveButton("Send", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                EditText edtSsid = view.findViewById(R.id.edtDeviceSsid);
                                String ssid = edtSsid.getText().toString().trim();

                                EditText edtPassword = view.findViewById(R.id.edtDevicePassword);
                                String password = edtPassword.getText().toString().trim();

                                mqttModel.publishDesired(getDeviceID(),
                                        new DesiredWifiConfig(ssid, password));
                            }
                        })
                        .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create().show();
            }
        });

        findViewById(R.id.btnPublishReported).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final View view = LayoutInflater
                        .from(IotMqttActivity.this).inflate(R.layout.dialog_publish_reported, null);

                new AlertDialog.Builder(IotMqttActivity.this)
                        .setTitle("Publish Device Data")
                        .setView(view)
                        .setPositiveButton("Send", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                EditText edtSerialNumber = view.findViewById(R.id.edtDeviceSN);
                                String serialNumber = edtSerialNumber.getText().toString().trim();

                                EditText edtSsid = view.findViewById(R.id.edtDeviceSsid);
                                String ssid = edtSsid.getText().toString().trim();

                                EditText edtPassword = view.findViewById(R.id.edtDevicePassword);
                                String password = edtPassword.getText().toString().trim();

                                mqttModel.publishReported(getDeviceID(),
                                        new ReportedWifiConfig(serialNumber, ssid, password));
                            }
                        })
                        .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create().show();

            }
        });

        findViewById(R.id.btnSubscribeDesired).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mqttModel.subscribeDesired(getDeviceID());
            }
        });

        findViewById(R.id.btnSubscribeReported).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mqttModel.subscribeReported(getDeviceID());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mqttModel.disconnect(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mqttModel.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mqttModel.stop();
    }

    public String getServerURL() {
        String scheme = "tcp://";
        EditText edtServerHost = findViewById(R.id.edtServerHost);
        String host = edtServerHost.getText().toString().trim();
        int port = 1883;
        if (host.isEmpty()) {
            return host;
        } else {
            return scheme + host + ":" + port;
        }
    }


    public String getDeviceID() {
        EditText edtTopic = findViewById(R.id.edtDeviceId);
        return edtTopic.getText().toString().trim();
    }


    public String getClientID() {
        EditText edtClientIDName = findViewById(R.id.edtClientIDName);
        String clientIDName = edtClientIDName.getText().toString().trim();
        return clientIDName + ".client.id";
    }


    public String getUserName() {
        return "admin";
    }


    public String getPassword() {
        return "admin";
    }

    public void updateDesired(Object object) {
        TextView txtDesired = findViewById(R.id.txtDesired);
        txtDesired.setText(toPrettyJsonString(object.toString()));
    }

    public void updateReported(Object object) {
        TextView txtReported = findViewById(R.id.txtReported);
        txtReported.setText(toPrettyJsonString(object.toString()));
    }

    private String toPrettyJsonString(String jsonStr) {
        String result = jsonStr;
        try {
            JSONObject json = new JSONObject(jsonStr);
            result = json.toString(4);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }
}
