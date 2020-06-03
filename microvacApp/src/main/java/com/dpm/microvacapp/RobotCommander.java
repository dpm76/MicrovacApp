package com.dpm.microvacapp;

import android.util.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class RobotCommander {

    private final String LOG_TAG = RobotCommander.class.getSimpleName();

    private Socket _socket;

    private boolean _isBound = false;

    public RobotCommander(){

    }

    public void connect(final String address, final int port) {
        new Thread(new Runnable(){
            @Override
            public void run() {

                if(_socket != null)
                {
                    close();
                }

                _socket = new Socket();

                try {
                    _socket.connect(new InetSocketAddress(address, port));
                    _isBound = true;
                }catch (IOException ex)
                {
                    Log.e(LOG_TAG, ex.getMessage(), ex);
                }
            }
        }).start();
    }

    public void close(){

        new Thread(new Runnable() {
            @Override
            public void run() {
                if(_isBound){
                    try {
                        _socket.close();
                        _isBound = false;
                    }catch (IOException ex)
                    {
                        Log.e(LOG_TAG, ex.getMessage(), ex);
                    }
                }
            }
        }).start();
    }

    public void send(final String cmd){

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if(_isBound) {
                        _socket.getOutputStream().write(cmd.getBytes());
                    }
                }catch(IOException ex)
                {
                    _isBound = false;
                    Log.e(LOG_TAG, ex.getMessage(), ex);
                }
            }
        }).start();
    }

    public void sendForwards()
    {
        send("FWD");
    }

    public void sendBackwards()
    {
        send("BAK");
    }

    public void sendTurnLeft()
    {
        send("TLE");
    }

    public void sendTurnRight()
    {
        send("TRI");
    }

    public void sendStop()
    {
        send("STP");
    }

    public void sendExpression(int expressionId){
        send(String.format("EXP:%d", expressionId));
    }

    public boolean isBound() { return _isBound; }
}
