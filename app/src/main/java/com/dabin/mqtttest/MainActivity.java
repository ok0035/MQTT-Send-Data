package com.dabin.mqtttest;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import com.dabin.mqtttest.databinding.ActivityMainBinding;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.locks.ReentrantLock;

public class MainActivity extends AppCompatActivity {
    MqttClient client;
    String sendingData = "";
    Thread sendingDataThread;
    Handler sendingDataHandler;
    ActivityMainBinding binding;

    int TotalCount = 5;
    int PartialCount = 1;
    int delay = 1000;
    private static ReentrantLock RL = new ReentrantLock();
    String ipAddress = "tcp://172.30.1.6";
    String topic = "pact/data2";

    enum MODE {

        VSWR(0x01),
        DTF(0x02),
        CL(0x03),
        SWEPT_SA(0x00),
        CHANNEL_POWER(0x01),
        OCCUPIED_BW(0x02),
        ACLR(0x03),
        SEM(0x04),
        TRANSMIT_ON_OFF(0x05),
        GATE(0x23),
        DEMODULATION(0x41);

        int value;

        MODE(int val) {
            value = val;
        }

        public int getValue() {
            return value;
        }
    }

    private MODE CurrentMode = MODE.SWEPT_SA;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        binding.btnVswr.setOnClickListener(v -> {

            CurrentMode = MODE.VSWR;

        });

        binding.btnDtf.setOnClickListener(v -> {

            CurrentMode = MODE.DTF;

        });


        binding.btnCl.setOnClickListener(v -> {

            CurrentMode = MODE.CL;

        });


        binding.btnSweptSa.setOnClickListener(v -> {

            CurrentMode = MODE.SWEPT_SA;

        });

        binding.btnChannelPower.setOnClickListener(v -> {

            CurrentMode = MODE.CHANNEL_POWER;

        });

        binding.btnOccupiedBW.setOnClickListener(v -> {

            CurrentMode = MODE.OCCUPIED_BW;

        });

        binding.btnAclr.setOnClickListener(v -> {

            CurrentMode = MODE.ACLR;

        });

        binding.btnSem.setOnClickListener(v -> {

            CurrentMode = MODE.SEM;

        });

        binding.btnTransmitOnOff.setOnClickListener(v -> {

            CurrentMode = MODE.TRANSMIT_ON_OFF;

        });

        binding.btnDemodulation.setOnClickListener(v -> {

            CurrentMode = MODE.DEMODULATION;

        });

        binding.btnGate.setOnClickListener(v -> {

        });


        try {
            client = new MqttClient(ipAddress, client.generateClientId(), new MemoryPersistence());
            client.connect();
//            sendingDataStart();

            client.subscribe("pact/command");
            client.setCallback(new MqttCallbackExtended() {
                @Override
                public void connectComplete(boolean reconnect, String serverURI) {

                    runOnUiThread(() -> {
                        binding.tvData.setText("Connected!!");

                    });
                }

                @Override
                public void connectionLost(Throwable cause) {
                    runOnUiThread(() -> {
                        binding.tvData.setText("Connection Lost...");

                    });
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    if (new String(message.getPayload()).equals("0x200000")) {

                        client.publish("pact/data1", new MqttMessage("0x200000".getBytes()));
                        Log.d("20000", "2000000");

                        runOnUiThread(() -> {
                            binding.tvData.setText("Connected!!");

                        });

                    } else if (message.toString().equals("0x11")) {

                        new Thread(()-> {

                            RL.lock();

                            for (int i = 0; i < TotalCount; i++) {
                                Log.d("messageArrived", "partial count : " + PartialCount);
                                loadTestData(CurrentMode);
                                PartialCount++;
                            }

                            PartialCount = 1;

                            RL.unlock();
                        }).start();

                    } else if (message.toString().equals("0x23")) {

                        new Thread(()->{

                            RL.lock();

                            for (int i = 0; i < TotalCount; i++) {


                                loadGate(CurrentMode);
                                PartialCount++;

                            }

                            PartialCount = 0;

                            RL.unlock();

                        }).start();



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

    public void loadTestData(MODE mode) {

        switch (mode) {

            case VSWR:
                loadVswr();
                break;
            case DTF:
                loadDtf();
                break;
            case CL:
                loadCl();
                break;
            case SWEPT_SA:
                loadSweptSa();
                break;
            case CHANNEL_POWER:
                loadChannelPower();
                break;
            case OCCUPIED_BW:
                loadOccupiedBw();
                break;
            case ACLR:
                loadAclr();
                break;
            case SEM:
                loadSem();
                break;
            case TRANSMIT_ON_OFF:
                loadTransmit();
                break;
            case DEMODULATION:
                loadDemodulation();
                break;

        }


    }

    public void loadVswr() {

        final ByteBuffer buffer = ByteBuffer.allocate(4 * 2 + 4 * 4 * 129).order(ByteOrder.LITTLE_ENDIAN);

        buffer.putInt(0x01);
        buffer.putInt(0x00);
        Log.d("buffer", "putint");

        /*demodulation test*/
        for (int j = 0; j < 129; j++) {

            buffer.putInt((int) ((Math.random() * 50)) * 100);

        }

        try {
            client.publish("pact/data2", new MqttMessage(buffer.array()));

            buffer.clear();
            buffer.position(0);
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (MqttException e) {
            e.printStackTrace();
        }

    }

    public void loadDtf() {

        final ByteBuffer buffer = ByteBuffer.allocate(4 * 2 + 4 * 4 * 129).order(ByteOrder.LITTLE_ENDIAN);

        buffer.putInt(0x02);
        buffer.putInt(0x00);
        Log.d("buffer", "putint");

        /*demodulation test*/
        for (int j = 0; j < 128; j++) {

            buffer.putInt((int) ((Math.random() * 50)) * 100);

        }

        try {
            client.publish("pact/data2", new MqttMessage(buffer.array()));

            buffer.clear();
            buffer.position(0);
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (MqttException e) {
            e.printStackTrace();
        }


    }


    public void loadCl() {

        final ByteBuffer buffer = ByteBuffer.allocate(4 * 2 + 4 * 4 * 129).order(ByteOrder.LITTLE_ENDIAN);

        buffer.putInt(0x03);
        buffer.putInt(0x00);
        Log.d("buffer", "putint");

        /*demodulation test*/
        for (int j = 0; j < 129; j++) {

            buffer.putInt((int) ((Math.random() * 50)) * 100);

        }

        try {
            client.publish("pact/data2", new MqttMessage(buffer.array()));

            buffer.clear();
            buffer.position(0);
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (MqttException e) {
            e.printStackTrace();
        }


    }


    public void loadSweptSa() {

        final ByteBuffer buffer = ByteBuffer.allocate(4 * 2 + 4 * 4 * 2002 + 4 * 2).order(ByteOrder.LITTLE_ENDIAN);

        buffer.putInt(0x04);
        buffer.putInt(0x00);
        Log.d("buffer", "putint");

        /*demodulation test*/

        for (int i = 0; i < 4; i++) {

            for (int j = 0; j < 2002; j++) {

                buffer.putInt((int) (-25 * (i + 1) - (Math.random() * -20)) * 100);

                Log.d("buffer", "in for2");
            }


        }

        buffer.putInt(TotalCount);
        buffer.putInt(PartialCount);

        Log.d("bufferSize", buffer.position() + " ");

        try {
            client.publish("pact/data2", new MqttMessage(buffer.array()));

            buffer.clear();
            buffer.position(0);
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (MqttException e) {
            e.printStackTrace();
        }


    }

    public void loadChannelPower() {

        final ByteBuffer buffer = ByteBuffer.allocate(4 * 2 + 4 * 2002 + 4 * 2 + 4 * 2).order(ByteOrder.LITTLE_ENDIAN);

        buffer.putInt(0x04);
        buffer.putInt(0x01);
        Log.d("buffer", "putint");

        for (int j = 0; j < 2002; j++) {

            buffer.putInt((int) (-40 - (Math.random() * -30)) * 100);

        }

        buffer.putInt(TotalCount);
        buffer.putInt(PartialCount);

        Log.d("bufferSize", buffer.position() + " ");

        try {
            client.publish("pact/data2", new MqttMessage(buffer.array()));

            buffer.clear();
            buffer.position(0);
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (MqttException e) {
            e.printStackTrace();
        }


    }

    public void loadOccupiedBw() {

        final ByteBuffer buffer = ByteBuffer.allocate(4 * 2 + 4 * 2002 + 4 * 5 + 4 * 2).order(ByteOrder.LITTLE_ENDIAN);

        buffer.putInt(0x04);
        buffer.putInt(0x02);
        Log.d("buffer", "putint");


        for (int j = 0; j < 2002; j++) {

            buffer.putInt((int) (-40 - (Math.random() * -30)) * 100);

        }


        buffer.putInt(TotalCount);
        buffer.putInt(PartialCount);

        Log.d("bufferSize", buffer.position() + " " + (40 - 32 / 2));

        try {
            client.publish("pact/data2", new MqttMessage(buffer.array()));

            buffer.clear();
            buffer.position(0);
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (MqttException e) {
            e.printStackTrace();
        }


    }

    public void loadAclr() {

        final ByteBuffer buffer = ByteBuffer.allocate(4 * 2 + 4 * 2002 + 4 * 34 + 4 * 2).order(ByteOrder.LITTLE_ENDIAN);

        buffer.putInt(0x04);
        buffer.putInt(0x03);
        Log.d("buffer", "putint");


        for (int j = 0; j < 2002; j++) {

            buffer.putInt((int) (-40 - (Math.random() * -30)) * 100);

        }

        for (int i = 0; i < 34; i++) {

            buffer.putInt((int) (40 + (Math.random() * 30)) * 100);

        }

        buffer.putInt(TotalCount);
        buffer.putInt(PartialCount);

        Log.d("bufferSize", buffer.position() + " ");

        try {
            client.publish("pact/data2", new MqttMessage(buffer.array()));

            buffer.clear();
            buffer.position(0);
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (MqttException e) {
            e.printStackTrace();
        }


    }

    public void loadSem() {

        final ByteBuffer buffer = ByteBuffer.allocate(4 * 2 + 4 * 2002 + 4 * 43 + 4 * 2).order(ByteOrder.LITTLE_ENDIAN);

        buffer.putInt(0x04);
        buffer.putInt(0x04);
        Log.d("buffer", "putint");


        for (int j = 0; j < 2002; j++) {

            buffer.putInt((int) (-40 - (Math.random() * -30)) * 100);

        }

        for (int i = 0; i < 43; i++) {

            buffer.putInt((int) (40 + (Math.random() * 30)) * 100);

        }

        buffer.putInt(TotalCount);
        buffer.putInt(PartialCount);

        Log.d("bufferSize", buffer.position() + " ");

        try {
            client.publish("pact/data2", new MqttMessage(buffer.array()));

            buffer.clear();
            buffer.position(0);
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (MqttException e) {
            e.printStackTrace();
        }


    }

    public void loadTransmit() {

        final ByteBuffer buffer = ByteBuffer.allocate(4 * 2 + 4 * 2002 + 4 * 7 + 4 * 2).order(ByteOrder.LITTLE_ENDIAN);

        buffer.putInt(0x04);
        buffer.putInt(0x05);
        Log.d("buffer", "putint");


        for (int j = 0; j < 2002; j++) {

            buffer.putInt((int) (-40 - (Math.random() * -30)) * 100);

        }

        for (int i = 0; i < 7; i++) {

            buffer.putInt((int) (40 + (Math.random() * 30)) * 100);

        }

        buffer.putInt(TotalCount);
        buffer.putInt(PartialCount);

        Log.d("bufferSize", buffer.position() + " ");

        try {
            client.publish("pact/data2", new MqttMessage(buffer.array()));

            buffer.clear();
            buffer.position(0);
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (MqttException e) {
            e.printStackTrace();
        }


    }

    public void loadDemodulation() {

        final ByteBuffer buffer = ByteBuffer.allocate(7000).order(ByteOrder.LITTLE_ENDIAN);

        buffer.putInt(0x042);
        buffer.putInt(0x07);
        Log.d("buffer", "putint");

        /*demodulation test*/

        for (int i = 0; i < 8; i++) {

            int size = (int) (((Math.random() * 128)));
            buffer.putInt(size);

            for (int k = 0; k < size; k++) {

                buffer.putInt((int) (((Math.random() * 4) - 2) * 1000));
                buffer.putInt((int) (((Math.random() * 4) - 2) * 1000));

            }

            Log.d("buffer", "in for2");

        }

        for (int i = 0; i < 8; i++) {

            buffer.putInt((int) (Math.random() * -100 * 100));

        }


        for (int i = 0; i < 45; i++) {

            buffer.putInt((int) (Math.random() * 8));

        }

        buffer.putInt((int) (Math.random() * 200000000));
        buffer.putInt((int) (Math.random() * 200000000));


        Log.d("bufferSize", buffer.position() + " ");

        try {
            client.publish("pact/data2", new MqttMessage(buffer.array()));

            buffer.clear();
            buffer.position(0);
            Thread.sleep(delay);
        } catch (MqttException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }

    public void loadGate(MODE type) {

        final ByteBuffer buffer = ByteBuffer.allocate(4 * 2 + 4 * 2002 + 4 * 2).order(ByteOrder.LITTLE_ENDIAN);

        buffer.putInt(0x23);
        buffer.putInt(type.getValue());
        Log.d("buffer", "putint");

        /*demodulation test*/

        for (int j = 0; j < 2002; j++) {

            buffer.putInt((int) (-40 - (Math.random() * -30)) * 100);

            Log.d("buffer", "in for2");
        }

        buffer.putInt(TotalCount);
        buffer.putInt(PartialCount);

        Log.d("bufferSize", buffer.position() + " ");

        try {
            client.publish("pact/data2", new MqttMessage(buffer.array()));

            buffer.clear();
            buffer.position(0);
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (MqttException e) {
            e.printStackTrace();
        }


    }

}
