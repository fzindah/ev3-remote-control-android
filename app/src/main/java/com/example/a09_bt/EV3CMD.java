package com.example.a09_bt;

import android.annotation.SuppressLint;
import android.util.Log;

public class EV3CMD {
    String type;
    CMDMsg msg;

    String mf_getType() {
        return type;
    }

    CMDMsg mf_getCmd() {
        return msg;
    }

    public EV3CMD(String id, int val) throws Exception {
        if (id.equals("MoveMotorBack")) {
            mf_moveMotorBack(val);
        }
    }

    public EV3CMD(String id, String letter, int duration, boolean wait) throws Exception {
        if (id.equals("PlayTone")) {
            mf_EV3PlayTone(letter, duration, wait);
        }
    }

    public EV3CMD(String id, byte ... byteArgs) throws Exception {
        byte index = 0;

        byte port=0, type=0, mode=0;

        if (byteArgs.length == 1) {
            index = byteArgs[0];
        }
        else if (byteArgs.length == 2) {
            port = byteArgs[0];
            type = byteArgs[1];
        }
        else if (byteArgs.length == 3) {
            port = byteArgs[0];
            type = byteArgs[1];
            mode = byteArgs[2];
        }

        if (id.equals("StartMotor")) {
            mf_EV3StartMotor(port, type);
        }
        else if (id.equals("StopMotor")) {
            mf_EV3StopMotor(port);
        }
        else if (id.equals("PlayJingle")) {
            mf_playJingle();
        }
        else if (id.equals("SayHello")) {
            mf_sayHello();
        }
        else if (id.equals("Turn")) {
            mf_EV3Turn(port, type);
        }
        else if (id.equals("GetBattery")) {
            mf_getBatteryLevel();
        }
        else if (id.equals("ReadSensors_0x99_1c")) {
            mf_makeReadSensorsCmd(port, type, mode);
        }
    }
    public void mf_EV3StartMotor(byte speed, byte motor) throws Exception {
        Log.i("EV3", "Method called");
        Log.i("EV3Speed", speed + "");
        Log.i("EV3Motor", motor + "");

        msg = new CMDMsg(15, false, (byte) 0);
        msg.mv_setOPCODE((byte) 0xA5);    // OPCODE - opOUTPUT_SPEED
        msg.mv_setLC0(8, (byte) 0);
        msg.mv_setLC0(9, motor);    // LC0(0) Master layer 0x00
        msg.mv_setLC1(10, speed);    // 50% Speed - CE is -50 0xCE
        msg.mv_setLC0(12, (byte) 0xA6);   // OPCODE - opOUTPUT_START
        msg.mv_setLC0(13,(byte)0);                // LC0(0) Master layer 0x00
        msg.mv_setLC0(14, motor);
    }

    public void mf_EV3Turn(byte speed, byte turn) throws Exception {
        msg = new CMDMsg(21, false, (byte) 0);
        msg.mv_setOPCODE((byte) 0xB0);       // OPCODE - Output_Step_Sync
        msg.mv_setLC0(8, (byte) 0);
        msg.mv_setLC0(9, (byte)0x06);
        msg.mv_setLC1(10, speed);
        msg.mv_setLC1(12, turn);
        msg.mv_setLC2(14, (byte) 0);
        msg.mv_setLC0(17, (byte) 1);  // brake
        msg.mv_setLC0(18, (byte) 0xA6);
        msg.mv_setLC0(19, (byte) 0x06); // motor B and C
    }

    public void mf_EV3StopMotor(byte motor) throws Exception {
        msg = new CMDMsg(11, false, (byte) 0);
        msg.mv_setOPCODE((byte) 0xA3);       // OPCODE - opOUTPUT_STOP
        msg.mv_setLC0(8, (byte) 0);     // LC0 layer 0
        msg.mv_setLC0(9, motor);         // motor B and C
        msg.mv_setLC0(10, (byte) 1);    // LC0 (BREAK) 1
    }

    // 4.2.5 Play a letter tone at level 2 for 1 sec.
    // Wait for previous tones to finish
    public void mf_EV3PlayTone(String letter, int duration, boolean wait) throws  Exception {
        msg = new CMDMsg(wait ? 18 : 17, false, (byte) 0);
        msg.mv_setOPCODE((byte)0x94); // OPCODE ready
        msg.mv_setLC0(8, (byte)1);  // tone
        msg.mv_setLC1(9, (byte)2);  // volume
        msg.mv_setLC2(11, letterToFrequency(letter));    // frequency
        msg.mv_setLC2(14, (short) duration); // duration

        // wait until each note finishes for the next one
        if (wait)
            msg.mv_setLC0(17, (byte)0x96); //ready
    }

    public void mf_playJingle() throws Exception{
        String[] tune = {"E", "E", "E", "E", "E", "G", "C", "D", "E1", "F",
                "F", "F", "F", "F", "E3", "E3", "E", "D", "D", "E", "D1", "G1",
                "E", "E", "E1", "E", "E", "E", "E1", "G", "C", "D", "E1", "F", "F",
                "F", "F", "E", "E", "E", "E", "F", "D", "C1"};

        for(int i = 0; i < tune.length; i++) {
            if (tune[i].indexOf("1") >= 0)
                mf_EV3PlayTone("" + tune[i].charAt(0), 1000, true);
            else if (tune[i].indexOf("3") >= 0)
                mf_EV3PlayTone("" + tune[i].charAt(0), 300, true);
            else
                mf_EV3PlayTone(tune[i], 500, true);
        }
    }

    private short letterToFrequency(String letter) {
        //A5-G5
        short freq = 0;
        switch(letter) {
            case "A": freq = 880; break;
            case "B": freq = 987; break;
            case "C": freq = 523; break;
            case "D": freq = 587; break;
            case "E": freq = 659; break;
            case "F": freq = 698; break;
            case "G": freq = 783; break;
        }
        return freq;
    }

    public void mf_sayHello() throws Exception {
        msg = new CMDMsg(25, false, (byte) 0);
        msg.mv_setOPCODE((byte)0x94); // OPCODE sound
        msg.mv_setLC0(8, (byte)0x02); // play
        msg.mv_setLC1(9, (byte)50); // volume
        msg.mv_setLC0(11, (byte)0x84); // filename
        msg.mv_setLC0(12, (byte)'.'); // filename
        msg.mv_setLC0(13, (byte)'/'); // filename
        msg.mv_setLC0(14, (byte)'u'); // filename
        msg.mv_setLC0(15, (byte)'i'); // filename
        msg.mv_setLC0(16, (byte)'/'); // filename
        msg.mv_setLC0(17, (byte)'S'); // filename
        msg.mv_setLC0(18, (byte)'t'); // filename
        msg.mv_setLC0(19, (byte)'a'); // filename
        msg.mv_setLC0(20, (byte)'r'); // filename
        msg.mv_setLC0(21, (byte)'t'); // filename
        msg.mv_setLC0(22, (byte)'u'); // filename
        msg.mv_setLC0(23, (byte)'p'); // filename
        msg.mv_setLC0(24, (byte)0x00); // filename
    }

    public void mf_getBatteryLevel() {
        msg = new CMDMsg(10, true, (byte) 4);
        msg.mv_setOPCODE((byte) 0x81);
        msg.mv_setOPCMD((byte) 0x12);
        msg.mv_setGV0(9, (byte) 0x60);
    }

    private void mf_moveMotorBack(int val) {
        int degrees = val;
        Log.i("EV3MoveBack", val+"");

        int degrees2 = 0;

        if (degrees > 360) {
            degrees = degrees - 180;
            degrees2 = 180;
        }
        msg = new CMDMsg(20, false, (byte) 0);
        msg.mv_setOPCODE((byte) 0xae);
        msg.mv_setOPCMD((byte) 0x00);
        msg.mv_setLC0(9, (byte) 6);
        msg.mv_setLC1(10, (byte) -50);
        msg.mv_setLC0(12, (byte) 0);
        msg.mv_setLC2(13, (short) degrees); // these 2 add up
        msg.mv_setLC2(16, (short) degrees2); // to rotations
    }

    // port 1-4 0x00-0x03, port A-D 0x10-0x13
    // sensor
    //  7,0 EV3-Large-Motor-Degree
    //  7,1 EV3-Large-Motor-Rotation
    // 16,0 EV3-Touch
    // 29,0 EV3-Color-Reflected
    // 30,0 EV3-Ultrasonic-Cm
    public void mf_makeReadSensorsCmd(byte port, byte type, byte mode) {
        msg = new CMDMsg(15, true, (byte) 4);
        msg.mv_setOPCODE((byte) 0x99);
        msg.mv_setOPCMD((byte) 0x1c);
        msg.mv_setLC0(9, (byte) 0); // LAYER
        msg.mv_setLC0(10, port);
        msg.mv_setLC0(11, type);
        msg.mv_setLC0(12, mode);
        msg.mv_setLC0(13, (byte) 0x01); // return 1 value
        msg.mv_setGV0(14, (byte) 0x60);
    }
}
