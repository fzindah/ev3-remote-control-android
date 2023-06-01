package com.example.a09_bt;

import static java.lang.Integer.parseInt;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.util.Pair;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.a09_bt.databinding.FragmentBluetoothBinding;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class EV3Service implements Serializable {

    // BT Variables
    private final String MV_ROBOTNAME = "Robot1";
    private BluetoothAdapter mv_btInterface = null;
    private Set<BluetoothDevice> mv_pairedDevices = null;
    private BluetoothDevice mv_btDevice = null;
    private BluetoothSocket mv_btSocket = null;

    // Data stream to/from NXT bluetooth
    private InputStream mv_is = null;
    private OutputStream mv_os = null;

    private ConnectedThread mv_connectedThread = null;

    public Pair<String, String> mf_checkBTPermissions(MainActivity cref_main) {
        String msg1, msg2;
        if (ContextCompat.checkSelfPermission(cref_main,
                Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
            msg1 = "BLUETOOTH_SCAN already granted.\n";
        }
        else {
            msg1 = "BLUETOOTH_SCAN NOT granted.\n";
        }

        if (ContextCompat.checkSelfPermission(cref_main,
                Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_DENIED) {
            msg2 = "BLUETOOTH_CONNECT NOT granted.\n";
        }
        else {
            msg2 = "BLUETOOTH_CONNECT already granted.\n";
        }
        Pair<String, String> msg = new Pair<>(msg1, msg2);
        return msg;
    }


    // https://www.geeksforgeeks.org/android-how-to-request-permissions-in-android-application/
    // https://stackoverflow.com/questions/67722950/android-12-new-bluetooth-permissions
    public void mf_requestBTPermissions(MainActivity cref_main) {
        // We can give any value but unique for each permission.
        final int BLUETOOTH_SCAN_CODE = 100;
        final int BLUETOOTH_CONNECT_CODE = 101;

        // Android version < 12, "android.permission.BLUETOOTH" just fine
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            Toast.makeText(cref_main,
                    "BLUETOOTH granted for earlier Android", Toast.LENGTH_SHORT) .show();
            return;
        }

        // Android 12+ has to go through the process
        if (ContextCompat.checkSelfPermission(cref_main,
                Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(cref_main,
                    new String[]{Manifest.permission.BLUETOOTH_SCAN},
                    BLUETOOTH_SCAN_CODE);
        }
        else {
            Toast.makeText(cref_main,
                    "BLUETOOTH_SCAN already granted", Toast.LENGTH_SHORT) .show();
        }

        if (ContextCompat.checkSelfPermission(cref_main,
                Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(cref_main,
                    new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                    BLUETOOTH_CONNECT_CODE);
        }
        else {
            Toast.makeText(cref_main,
                    "BLUETOOTH_CONNECT already granted", Toast.LENGTH_SHORT) .show();
        }
    }

    // Modify from chap14, pp390 findRobot()
    @SuppressLint("MissingPermission")
    public String mf_locateInPairedBTList(FragmentBluetoothBinding binding) {
        ListView listview = (ListView) binding.bluetoothList;

        BluetoothDevice lv_bd = null;
        String msg = "";
        try {
            mv_btInterface = BluetoothAdapter.getDefaultAdapter();
            mv_pairedDevices = mv_btInterface.getBondedDevices();
            List<String> s = new ArrayList<String>();

            Iterator<BluetoothDevice> lv_it = mv_pairedDevices.iterator();
            while (lv_it.hasNext())  {
                lv_bd = lv_it.next();
                s.add(lv_bd.getName());
            }
            // on the below line we are initializing the adapter for our list view.
            ArrayAdapter adapter = new ArrayAdapter<>(binding.bluetoothList.getContext(), android.R.layout.simple_list_item_1, s);

            // on below line we are setting adapter for our list view.
            listview.setAdapter(adapter);
        }
        catch (Exception e) {
            msg = "Failed in findRobot() " + e.getMessage();
        }
        return msg;
    }


    // Modify from chap14, pp391 connectToRobot()
    @SuppressLint("MissingPermission")
    public String mf_connectToEV3(String robot) {
        BluetoothDevice lv_bd = null;
        Iterator<BluetoothDevice> lv_it = mv_pairedDevices.iterator();
        while (lv_it.hasNext())  {
            lv_bd = lv_it.next();
            if (lv_bd.getName().equalsIgnoreCase(robot.toLowerCase())) mv_btDevice = lv_bd;
        }

        BluetoothDevice bd = mv_btDevice;
        String msg = "";
        try  {
            mv_btSocket = bd.createRfcommSocketToServiceRecord
                    (UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
            mv_btSocket.connect();

            //// HERE
            mv_is = mv_btSocket.getInputStream();
            mv_os = mv_btSocket.getOutputStream();
            msg = "Connect to " + bd.getName() + " at " + bd.getAddress();

            // Start the thread to connect with the given device
            mv_connectedThread = new ConnectedThread(mv_btSocket);
            mv_connectedThread.start();
        }
        catch (Exception e) {
            msg = "Error interacting with remote device [" + e.getMessage() + "]";
        }
        return msg;
    }

    @SuppressLint("MissingPermission")
    public String mf_disconnFromEV3() {
        String msg = "";
        try {
            mv_btSocket.close();
            mv_is.close();
            mv_os.close();
            msg = mv_btDevice.getName() + " is disconnect ";
        } catch (Exception e) {
            msg = "Error in disconnect";
        }
        return msg;
    }

    public String mf_EV3SendReplyCmd(String type, byte ... byteArgs)  throws  Exception {
        // I/Choreographer: Skipped 82 frames!  The application may be doing too much work on its main thread.
        int DELAY = 10;
        int WAIT_BEFORE_READ = 200;
        int MSGCOUNTER = 0x1234;
        String returnVal = "";
        EV3CMD cmd = new EV3CMD(type, byteArgs);

        if (type.equals("detectWallProgram")){
            ActionThread detectWall = new ActionThread(type, mv_connectedThread);
            detectWall.start();
        }
        else {
            try {
                mv_connectedThread.write(cmd.mf_getCmd().mf_getMsg(), DELAY);
                Thread.sleep(WAIT_BEFORE_READ);

            } catch (Exception e) {
                Log.i("EV3 out ", e.getMessage());
                throw new Exception("Error in " + cmd.mf_getType() + "(" + e.getMessage() + ")");
            }

            try {
                returnVal = readBuf(mv_connectedThread.read(MSGCOUNTER, 50), true, false);
                Log.i("InitRead:", returnVal);
            } catch (Exception e) {
                throw new Exception("Error in " + cmd.mf_getType() + "(" + e.getMessage() + ")");
            }
        }
        return returnVal;
    }

    public void mf_EV3SendNoReplyCmd(String type, String letter, int duration, boolean wait)  throws  Exception {
        EV3CMD cmd = new EV3CMD(type, letter, duration, wait);
        int DELAY = 10;
        try {
            mv_connectedThread.write(cmd.mf_getCmd().mf_getMsg(), DELAY);
        }
        catch (Exception e) {
            throw new Exception("Error in " + cmd.mf_getType() + "(" + e.getMessage() + ")");
        }

        mf_consoleOut(cmd.mf_getCmd().mf_getMsg(), "EV3 ==", false);
    }


    public void mf_EV3SendNoReplyCmd(String type, byte ...byteArgs)  throws  Exception {
        EV3CMD cmd = new EV3CMD(type, byteArgs);
        try {
            mv_connectedThread.write(cmd.mf_getCmd().mf_getMsg(), 10);
        }
        catch (Exception e) {
            throw new Exception("Error in " + cmd.mf_getType() + "(" + e.getMessage() + ")");
        }
        mf_consoleOut(cmd.mf_getCmd().mf_getMsg(), "EV3 ==", false);
    }

    public String readBuf( byte[] buf, boolean limit, boolean endian) {
        mf_consoleOut(buf, "EV3Response", true);
        byte[] arr = new byte[buf.length - 3];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = buf[3 + i];
        }
        ByteBuffer val = ByteBuffer.wrap(arr);

        val.order(ByteOrder.LITTLE_ENDIAN);
        String valS = val.getInt() + "";
        if (limit && !endian) valS = valS.substring(0, valS.length() - 1);

        return valS;
    }
    private String mf_consoleOut(byte[] buf, String tag, boolean flag) {
        String str = "";
        for (int i=0; i<buf.length; i++) {
            str += String.format("%02X ", buf[i]);
        }
        Log.i(tag, str);
        if (flag) {
            Log.i("EV3 +>", new String(buf, StandardCharsets.UTF_8));
        }
//        int val = ((buf[3] & 0xff) << 8) | (buf[4] & 0xff);
        ByteBuffer val = ByteBuffer.wrap(new byte[]{buf[3],buf[4],buf[5],buf[6]});
        val.order(ByteOrder.LITTLE_ENDIAN);
        String valS = val.getInt() + "";
        valS = valS.substring(0,valS.length() - 1);
        return valS;
    }

    class ConnectedThread extends Thread {
        private String tag = "EV3 thread ";
        private InputStream mis = null;
        private OutputStream mos = null;

        public ConnectedThread(BluetoothSocket socket) {
            //Log.i(tag, "create ConnectedThread: ");

            // Get the BluetoothSocket input and output streams
            try {
                mis = socket.getInputStream();
                mos = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(tag, "temp sockets not created", e);
            }
        }

        public void run() {
        }

        byte[] read(int command, int delay) {
            // return bytes always 0xll, 0xhh as message counter 0x1234 here, 0x02(good) or 0x04(error msg), then returned values
            int NUMBER_OF_TRIES = 5;
            int INTERVAL = 50;
            try {
                int attempts = 0;
                int bytesReady = 0;
                byte[] sizeBuffer = new byte[2];
                if (delay > 0) {
                    try {
                        Thread.sleep(delay);
                    } catch (Exception e) {}
                }

                while (attempts < NUMBER_OF_TRIES) {
                    Thread.sleep(INTERVAL);
                    bytesReady = mis.available();

                    if (bytesReady == 0) {
                        //Log.i(tag,"\t\tNothing there, try again");
                        attempts++;
                    } else {
                        //Log.i(tag,"\tThere are [" + bytesReady + "] waiting for us!");
                        break;
                    }
                }

                if (bytesReady < 2) {
                    Log.e(tag, "No bytes ready " + bytesReady);
                    return null;
                }

                int bytesRead = mis.read(sizeBuffer, 0, 2);
                if (bytesRead != 2) {
                    Log.e(tag, "Bytes buf error " + bytesReady);
                    return null;
                }

                // calculate response size
                bytesReady = sizeBuffer[0] + (sizeBuffer[1] << 8);
                Log.i(tag, "Bytes to read mis [" + bytesReady + "]");

                byte[] retBuf = new byte[bytesReady];
                bytesRead = mis.read(retBuf);

                if (bytesReady != bytesRead) {
                    Log.e(tag, "Byte 0,1 size mismatch? " + bytesRead);
                    return null;
                }

                int ret = retBuf[0] + (retBuf[1] << 8);
                if (ret != command) {
                    Log.e(tag, "Byte 2,3 msg counter mismatch " + ret);
////                 return null;
                }

                if (retBuf[2] == 0x04) {
                    Log.e(tag, "Byte 4 is 0x04, error return");
////                 return null;
                }

                return retBuf;
            } catch (Exception e) {
                Log.e(tag, "Error in Read Response -> " + e.getMessage());
                return null;
            }
        }

        public void write(byte[] buffer, int delay) {
            //Log.i(tag, "\t\t\tBEGIN mthreadWrite");
            try {
                if (delay > 0) {
                    try {
                        Thread.sleep(delay);
                    }
                    catch (Exception e) {
                        // Do nothing
                    }
                }
                mos.write(buffer);
                mos.flush();
            }
            catch (IOException e) {
                Log.e(tag, "Exception during write", e);
            }
        }

        public void cancel() {
            try {
                mis.close();;
                mos.close();
                mis = null;
                mos = null;
                if (mv_btSocket != null && mv_btSocket.isConnected()) {
                    mv_btSocket.close();
                    ////Log.i(tag, "\tdisconnect (ed) to " + mNXTAddress);
                }
            }
            catch (IOException e) {
                Log.e(tag, "close() of connect socket failed", e);
            }
        }
    }

    class ActionThread extends Thread {
        String type;
        ConnectedThread mv_connected;
        public ActionThread(String type, ConnectedThread mv_connected) {
            this.type = type;
            this.mv_connected = mv_connected;
        }

        @Override
        public void run() {
            Log.i("EV3", "Run method");
            if (type.equals("detectWallProgram")) {
                try {
                    detectWallAndTurn();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }

        public void detectWallAndTurn() throws Exception {

            Log.i("EV3", "detect method");
            String val = "255";

            try {
                EV3CMD cmd = new EV3CMD("ReadSensors_0x99_1c", (byte) 0x03, (byte) 0x1e, (byte) 0x00);
                do {
                    mv_connectedThread.write(cmd.mf_getCmd().mf_getMsg(), 50);
                    Thread.sleep(50);
                    val = readBuf(mv_connectedThread.read(0x1234, 50), false, true);
                    Log.i("EV3DisVal:", val);
                }
                while (parseInt(val) > 200);
                Log.i("EV3Val", val);

                // stop motor
                cmd = new EV3CMD("StopMotor", (byte) 0x06, (byte) 0);
                mv_connectedThread.write(cmd.mf_getCmd().mf_getMsg(), 10);
                Thread.sleep(100);

                // read motor degrees
                cmd = new EV3CMD("ReadSensors_0x99_1c", (byte) 0x11, (byte) 7, (byte) 0);
                mv_connectedThread.write(cmd.mf_getCmd().mf_getMsg(), 10);
                Thread.sleep(100);
                val = readBuf(mv_connectedThread.read(0x1234, 50), false, true);

                // move motor back
                cmd = new EV3CMD("MoveMotorBack", Integer.parseInt(val));
                mv_connectedThread.write(cmd.mf_getCmd().mf_getMsg(), 10);

            } catch (Exception e) {}
        }
    }
}
