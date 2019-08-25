package crop.computer.askey.mqttactivemqpractice.view;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import crop.computer.askey.mqttactivemqpractice.presenter.MainPresenter;
import crop.computer.askey.mqttactivemqpractice.R;

public class BasicActivity extends AppCompatActivity implements MainView {

    private MainPresenter presenter;
    private static final String TAG = BasicActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic);
        initView();
        presenter = new MainPresenter(this, this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        presenter.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        presenter.onStop();
    }

    private void initView() {
        findViewById(R.id.btnConnect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.connect();
            }
        });
        findViewById(R.id.btnSubscribe).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.subscribe();
            }
        });
        findViewById(R.id.btnPublish).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.publish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.iot_mqtt:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
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

    @Override
    public String getTopic() {
        EditText edtTopic = findViewById(R.id.edtTopic);
        return edtTopic.getText().toString().trim();
    }

    @Override
    public String getPublishedMessage() {
        EditText edtPublishedMessage = findViewById(R.id.edtPublishedMessage);
        return edtPublishedMessage.getText().toString().trim();
    }

    @Override
    public String getClientID() {
        EditText edtClientIDName = findViewById(R.id.edtClientIDName);
        String clientIDName = edtClientIDName.getText().toString().trim();
        return clientIDName + ".client.id";
    }

    @Override
    public String getUserName() {
        return "admin";
    }

    @Override
    public String getPassword() {
        return "admin";
    }

    @Override
    public void updateRecord(String message) {
        TextView txtRecord = findViewById(R.id.txtMessage);
        txtRecord.append(message+'\n');
    }

    @Override
    public void updateConnection(String connection) {
        TextView txtConnection = findViewById(R.id.txtConnection);
        txtConnection.setText(connection);
    }

    @Override
    public void updateState(String state) {
        TextView txtState = findViewById(R.id.txtState);
        txtState.setText(state);
    }
}
