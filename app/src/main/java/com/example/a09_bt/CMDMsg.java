package com.example.a09_bt;

public class CMDMsg {
    byte[] mv_buffer;

    public CMDMsg(int len, boolean b, byte size) {
        mv_buffer = new byte[len];
        mv_buffer[0] = (byte) ((len-2) & 0x00FF);
        mv_buffer[1] = (byte) (((len-2) & 0xFF00) >> 8);
        mv_buffer[2] = (byte) 0x34;
        mv_buffer[3] = (byte) 0x12;
        mv_buffer[4] = b? (byte)0x00 : (byte)0x80;;
        mv_buffer[5] = b? size : (byte)0x00;
        mv_buffer[6] = (byte) 0x00;
    }

    public byte[] mf_getMsg() {
        return mv_buffer;
    }

    public void mv_setOPCODE(byte b) {
        mv_buffer[7] = b;
    }

    public void mv_setOPCMD(byte b) {
        mv_buffer[8] = b;
    }
    public void mv_setLC0(int index, byte b) {
        mv_buffer[index] = b;
    }

    public void mv_setLC1(int index, byte value) {
        mv_buffer[index] = (byte) 0x81;
        mv_buffer[index+1] = value;
    }
    //http://www.java2s.com/example/android/file-input-output/write-short-to-littleendian-byte-array.html
    public void mv_setLC2(int index, short value) {
        mv_buffer[index] = (byte) 0x82;
        mv_buffer[index+1] = (byte) (value & 0x00FF);
        mv_buffer[index+2] = (byte) ((value & 0xFF00) >> 8);
    }
    public void mv_setLCS(int index, String str) {
        byte[] byteArrray = str.getBytes();
        mv_buffer[index] = (byte) 0x84;
        for (int i=0; i<str.length(); i++) {
            mv_buffer[index+1+i] = byteArrray[i];
        }
        mv_buffer[index+str.length()+1] = (byte) 0x00;
    }
    public void mv_setGV0(int index, byte b) {
        mv_buffer[index] =  b;
    }
}
