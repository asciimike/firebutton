package com.firebase.firebutton;

import android.app.Activity;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbRequest;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity {

    private TextView mCounter;
    private Button mButton;
    private UsbManager mUsbManager;
    private UsbDevice mDevice;
    private UsbDeviceConnection mConnection;
    private UsbEndpoint mEndpointIntr;
    private UsbEndpoint mEndpointOut;
    private Firebase mRef;
    private Timer mTimer;
    private Long mCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCounter = (TextView) findViewById(R.id.counter_label);
        mCount = 60L;
        mCounter.setTextColor(Color.parseColor("#820080"));

        mButton = (Button) findViewById(R.id.button);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRef.child("timer").setValue(60L);
                mCounter.setTextColor(Color.parseColor("#820080"));
            }
        });

        Firebase.setAndroidContext(this);

        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = getIntent();
        String action = intent.getAction();

        UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
        if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
            setDevice(device);
        } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
            if (mDevice != null && mDevice.equals(device)) {
                setDevice(null);
                mTimer.cancel();
            }
        }

        mRef = new Firebase("https://iot-test.firebaseio-demo.com");
        if (mDevice != null) {
            //We're the master , so we get to set the clock
            mTimer = new Timer();
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (mCount > 0) {
                        mCount--;
                        mRef.child("timer").setValue(mCount);
                        final int c = getColorForCount(mCount);
                        HashMap<String, Integer> colors = new HashMap<String, Integer>() {{
                            put("r", Color.red(c));
                            put("g", Color.green(c));
                            put("b", Color.blue(c));
                        }};
                        mRef.child("devices/color").setValue(colors);
                    }
                }
            }, 1000, 1000);
        }

        mRef.child("devices/color").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                LEDColor c = dataSnapshot.getValue(LEDColor.class);
                mCounter.setTextColor(Color.rgb(c.getR(), c.getG(), c.getB()));
                if (mDevice != null) {
                    String colors = "Color:" + c.getR() + ";" + c.getG() + ";" + c.getB() + "\n";
                    sendCommand(colors);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                // Do nothing here
            }
        });

        mRef.child("timer").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mCount = (Long) dataSnapshot.getValue();
                mCounter.setText(mCount.toString());
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                // Do nothing here
            }
        });
    }

    private int getColorForCount(Long count) {
        if (count > 50) {
            return Color.parseColor("#820080");
        } else if (count > 40) {
            return Color.parseColor("#0083c7");
        } else if (count > 30) {
            return Color.parseColor("#02be01");
        } else if (count > 20) {
            return Color.parseColor("#e5d900");
        } else if (count > 10) {
            return Color.parseColor("#e59500");
        } else if (count > 0) {
            return Color.parseColor("#e50000");
        } else {
            return Color.BLACK;
        }
    }

    private void setDevice(UsbDevice device) {
        // Step 1: Open the interface
        // bInterfaceNumber=0 is the DFU bootloader
        // bInterfaceNumber=1 is the usb-serial firmware interface descriptor
        UsbInterface intf = device.getInterface(1);

        // Step 2: Get the correct endpoint
        // out is 0
        // in is 1
        mEndpointOut = intf.getEndpoint(0);
        mEndpointIntr = intf.getEndpoint(1);

        // Step 3: Set the device (assuming everything is correct above)
        mDevice = device;
        if (device != null) {
            // Step 4: Open the connection
            UsbDeviceConnection connection = mUsbManager.openDevice(device);

            // Step 5: Claim the interface
            if (connection != null && connection.claimInterface(intf, true)) {
                mConnection = connection;
                new Thread(new Runnable() {
                    public void run() {
                        // Step 6: Set up USB to Serial converter
                        // Trust the magic, see: http://android.serverbox.ch/?p=427
                        mConnection.controlTransfer(0x21, 34, 0, 0, null, 0, 0);
                        mConnection.controlTransfer(0x21, 32, 0, 0, new byte[] { (byte) 0x80,
                                0x25, 0x00, 0x00, 0x00, 0x00, 0x08 }, 7, 0);
                    }
                }).start();
            } else {
                mConnection = null;
            }
        }

        if (device != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    ByteBuffer buffer = ByteBuffer.allocate(10);
                    UsbRequest request = new UsbRequest();
                    request.initialize(mConnection, mEndpointIntr);
                    while (true) {
                        mRef.child("devices/button").setValue(false);
                        // queue a request on the interrupt endpoint
                        request.queue(buffer, 10);
                        // wait for status event
                        try {
                            if (mConnection.requestWait() == request) {
                                mRef.child("devices/button").setValue(true);
                                mRef.child("timer").setValue(60L);
                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException e) {
                                    // No-op
                                }
                            } else {
                                break;
                            }
                        } catch (Exception e) {
                            mRef.child("exception").setValue(e.toString());
                        }
                    }
                }
            }).start();
        }
    }

    private void sendCommand(String command) {
        synchronized (this) {
            if (mConnection != null) {
                final byte[] message = command.getBytes();
                new Thread(new Runnable() {
                    public void run() {
                        mConnection.bulkTransfer(mEndpointOut, message, message.length, 0);
                    }
                }).start();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
