package com.dabin.mqtttest;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MainActivity extends AppCompatActivity {
    MqttClient client;
    String sendingData = "";
    Thread sendingDataThread;
    Handler sendingDataHandler;

    String ipAddress = "tcp://172.30.1.60";
    String topic = "Dabin";
    private android.widget.TextView tvData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.tvData = (TextView) findViewById(R.id.tvData);

        try {
            client = new MqttClient(ipAddress, MqttClient.generateClientId(), new MemoryPersistence());
            client.connect();
            sendingDataStart();

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void sendingDataStart() {

        if(sendingDataHandler == null) {
            sendingDataHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);

                    if (msg.what == 0) {
                        for (int i = 0; i < 2048; i++) {
                            sendingData += (float) (Math.random() * 30f + 30f);
                            if(i < 2047) sendingData += "/";
                        }
//                        Log.d("sendingData", sendingData);
                        try {
                            client.publish(topic, new MqttMessage(new String(sendingData).getBytes()));
                            Log.d(MqttClient.generateClientId(), sendingData);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    tvData.setText(sendingData);
                                }
                            });
                            sendingData = "";
                        } catch (MqttException e) {
                            e.printStackTrace();
                        }
                    }

                }
            };
        }

        sendingDataThread = new Thread(new Runnable() {
            @Override
            public void run() {

                while (true) {
                    if (sendingDataHandler != null)
                        sendingDataHandler.sendEmptyMessage(0);
                    try {
                        Thread.sleep(1000);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }

            }
        });

        sendingDataThread.start();
    }
}
