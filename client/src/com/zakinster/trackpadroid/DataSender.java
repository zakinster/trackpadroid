package com.zakinster.trackpadroid;

import java.net.Socket;

import android.os.AsyncTask;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;

public class DataSender extends Thread {
	
	public static class Data {
		int moveX = 0; int moveY = 0;
	}

    private Exception _exception = null;
    private NetProtocol _netProtocol;
    private Data _data;
    private Mutable.Int _refreshRate;
    private Mutable.Double _pointerSensitivity;
    private Mutable.Double _pointerAcceleration;
    private Button _connectButton;
    private TextView _textView;
    private int dx, dy;

    public DataSender(NetProtocol netProtocol, Data data, Mutable.Int refreshRate, Mutable.Double pointerSensitivity, Mutable.Double pointerAcceleration, Button connectButton, TextView textView) {
    	_netProtocol = netProtocol;
    	_data = data;
    	_refreshRate = refreshRate;
    	_pointerSensitivity = pointerSensitivity;
    	_pointerAcceleration = pointerAcceleration;
    	_connectButton = connectButton;
    	_textView = textView;
    }

    //@Override
    public void run() {
    	while(_netProtocol.isConnected()) {
    		
    		dx = 0; dy = 0;
    		synchronized(_data) {
    			dx = _data.moveX;
        		dy = _data.moveY;
        		_data.moveX = _data.moveY = 0;
    		}
    		
			if(dx!=0 || dy!=0) {
        		//double norm = Math.sqrt(dx*dx + dy*dy);
        		double acceleration = _pointerSensitivity.value;
        		double scale = 2-_pointerAcceleration.value;
        		int mx = (int)Math.round(Math.pow(Math.abs(dx), _pointerAcceleration.value)*scale*acceleration);
        		int my = (int)Math.round(Math.pow(Math.abs(dy), _pointerAcceleration.value)*scale*acceleration);
        		if(dx<0) mx = -mx;
        		if(dy<0) my = -my;
        		/*
        		if(dx>0) {
            		dx *= acceleration;
            		if(dx==0) dx = 1;
        		}
        		else if (dx<0) {
            		dx *= acceleration;
            		if(dx==0) dx = -1;
        		}
        		
        		if(dy>0) {
        			dy *= acceleration;
            		if(dy==0) dy = 1;
        		}
        		else if(dy<0) {
        			dy *= acceleration;
            		if(dy==0) dy = -1;
        		}*/
        		_netProtocol.mouseMove(mx, my);
			}
    		
    		try {
				Thread.sleep(1000/_refreshRate.value);
			} catch (InterruptedException e) {
			}
    	}
    }
}
