package com.dabin.mqtttest;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class MainActivity extends AppCompatActivity {
    MqttClient client;
    String sendingData = "";
    Thread sendingDataThread;
    Handler sendingDataHandler;

    public final

    String ipAddress = "tcp://172.30.1.38";
    String topic = "pact/data2";
    private android.widget.TextView tvData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.tvData = (TextView) findViewById(R.id.tvData);

        try {
            client = new MqttClient(ipAddress, client.generateClientId(), new MemoryPersistence());
            client.connect();
//            sendingDataStart();

            client.subscribe("pact/command");
            client.setCallback(new MqttCallbackExtended() {
                @Override
                public void connectComplete(boolean reconnect, String serverURI) {

                }

                @Override
                public void connectionLost(Throwable cause) {

                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    if(new String(message.getPayload()).equals("0x200000")) {

                        client.publish("pact/data1", new MqttMessage("0x200000".getBytes()));
                        Log.d("20000", "2000000");

                    } else if(message.toString().equals("0x11")){

                        final ByteBuffer buffer = ByteBuffer.allocate(10000).order(ByteOrder.LITTLE_ENDIAN);

                        Log.d(topic, message.toString());
                        buffer.putInt(0x042);
                        buffer.putInt(0x07);
                        Log.d("buffer", "putint");

                        /*demodulation test*/

                        for(int i=0; i<8; i++) {

                            int size = (int)(((Math.random() * 128)));
                            buffer.putInt(size);

                            for(int k=0; k<size; k++) {

                                buffer.putInt((int)(((Math.random() * 4) - 2) * 1000));
                                buffer.putInt((int)(((Math.random() * 4) - 2) * 1000));

                            }

                            Log.d("buffer", "in for2");

                        }

                        for(int i=0; i<8; i++) {

                            buffer.putInt((int)(Math.random() * -100 * 100));

                        }

                        for(int i=0; i<4; i++) {

                            buffer.putShort((short)(Math.random() * 8));

                        }

                        buffer.putInt((int)(Math.random() * 8));

                        for(int i=0; i<5; i++) {

                            buffer.putShort((short)(Math.random() * 8));

                        }

                        for(int i=0; i<38; i++) {

                            buffer.putInt((int)(Math.random() * 8));

                        }



//                        for(int j=0; j<4; j++) {
//
//                            for (int i = 0; i < 2002; i++) {
//                                buffer.putInt((int) (-(Math.random() * 20f + (20f * j))) * 1000);
//                            }
//
//                        }

//                        for (int i = 0; i < 129; i++) {
//                            buffer.putInt((int) ((Math.random() * 30f + 30f)) * 1000);
//                        }
//
//                        buffer.putInt((int)(Math.random() * 9999));
//                        buffer.putInt((int)(Math.random() * 9999));
//                        buffer.putInt((int)(Math.random() * 9999));
//                        buffer.putInt((int)(Math.random() * 10 + 1840) * 100);
//                        buffer.putInt((int)(Math.random() * 10 + 1850) * 100);

                        Log.d("bufferSize", buffer.array().length + " ");

                        try {
                            client.publish("pact/data2", new MqttMessage(buffer.array()));

                            buffer.clear();
                            buffer.position(0);
                            Thread.sleep(1000);
                        } catch (MqttException e) {
                            e.printStackTrace();
                        }

                    }
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("HandlerLeak")
    public void sendingDataStart() {

        final ByteBuffer buffer = ByteBuffer.allocate(4 * 129 + 8).order(ByteOrder.LITTLE_ENDIAN);

        if(sendingDataHandler == null) {
            sendingDataHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);

                    if (msg.what == 0) {

                        buffer.putInt(1);
                        buffer.putInt(0);

//                        for(int j=0; j<4; j++) {
//
//                            for (int i = 0; i < 2002; i++) {
//                                buffer.putInt((int) (-(Math.random() * 20f + (20f * j))) * 1000);
//                            }
//
//                        }

                        for (int i = 0; i < 129; i++) {
                            buffer.putInt((int) ((Math.random() * 30f + 30f)) * 1000);
                        }
//
//                        buffer.putInt((int)(Math.random() * 9999));
//                        buffer.putInt((int)(Math.random() * 9999));
//                        buffer.putInt((int)(Math.random() * 9999));
//                        buffer.putInt((int)(Math.random() * 10 + 1840) * 100);
//                        buffer.putInt((int)(Math.random() * 10 + 1850) * 100);

                        Log.d("bufferSize", buffer.array().length + "");

                        try {
                            client.publish(topic, new MqttMessage(buffer.array()));

                            buffer.clear();
                            buffer.position(0);
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

                        Thread.sleep(600);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }
        });

        sendingDataThread.start();
    }
}
