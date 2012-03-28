package com.zakinster.trackpadroid;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import android.os.AsyncTask;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;

public class NetProtocol {
	private Handler _uiHandler;
	private Socket _socket;
    private DataOutputStream _socketOutStream = null;
    private TextView _textView;
    private Button _connectButton;
	String _host; 
	int _port;
	
	class ChangeText implements Runnable {
		private String _text;
		public ChangeText(String text) { _text = text; }
        public void run() {
    		_textView.setText(_text);
        }
	}
	
	class ChangeButton implements Runnable {
		private String _text;
		private boolean _enabled;
		public ChangeButton(String text, boolean enabled) { _text = text; _enabled = enabled; }
        public void run() {
        	_connectButton.setText(_text);
        	_connectButton.setEnabled(_enabled);
        }
	}

	
	public NetProtocol(Handler uiHandler, TextView textView, Button connectButton) {
		_uiHandler = uiHandler;
		_socket = null;
		_textView = textView;
		_connectButton = connectButton;
	}

    class Connect extends Thread {
    	private Runnable _toRunWhenConnected;
    	public Connect(Runnable toRunWhenConnected) {_toRunWhenConnected = toRunWhenConnected;}
		@Override
		public void run() {
        	try {
        		if(_socketOutStream!=null) _socketOutStream.close();
        		if(_socket!=null) _socket.close();
    			_socket = new Socket(_host, _port);
    			if(_socket.isConnected()) {
        			_socketOutStream = new DataOutputStream(_socket.getOutputStream());
	            	_uiHandler.post(new ChangeText("Connected successfully to "+_socket.getInetAddress()+" on port "+_socket.getPort()));
	            	_uiHandler.post(new ChangeButton("Disconnect", true));
	            	_uiHandler.post(_toRunWhenConnected);
    			}
    			else {
    				try {
    					if(_socketOutStream!=null)_socketOutStream.close();
    					if(_socket!=null) _socket.close();
    				} catch (IOException e) {}
    	    		_socketOutStream = null;
    	    		_socket = null;
    	        	_uiHandler.post(new ChangeButton("Connect", true));
	            	_uiHandler.post(new ChangeText("Unexpected disconnection."));
    			}
    		} catch (UnknownHostException e) {
        		handleException(e);
    		} catch (IOException e) {
        		handleException(e);
    		}
        	return;
		}
    }
    
    class Disconnect extends Thread {
    	private boolean _willing;
    	public Disconnect(boolean willing) { _willing = willing; }
		@Override
		public void run() {
			try {
				if(_socketOutStream!=null)_socketOutStream.close();
				if(_socket!=null) _socket.close();
			} catch (IOException e) {}
    		_socketOutStream = null;
    		_socket = null;
        	_uiHandler.post(new ChangeButton("Connect", true));
        	if(_willing) _uiHandler.post(new ChangeText("Disconnected."));
    		return;
		}
    }
    
	public void connect(String host, int port, Runnable toRunWhenConnected) {
		_host = host;
		_port = port;
    	_uiHandler.post(new ChangeText("Connecting..."));
    	_uiHandler.post(new ChangeButton("Connecting...", false));
		(new Connect(toRunWhenConnected)).start();
	}
	public void disconnect(boolean willing) {
		(new Disconnect(willing)).start();
	}
    
    public boolean isConnected() {
    	return (_socket!=null && _socket.isConnected() && _socketOutStream!=null);
    }
	
	private void handleException(Exception e) {
    	_uiHandler.post(new ChangeText("NetProtocolError : " + e.getMessage()));
		disconnect(false);
	}
	
	public void sendData(byte[] data, int length) {
		if(isConnected()) {
			try {
				_socketOutStream.write(data, 0, length);
			} catch (IOException e) {
				handleException(e);
			}
		}
		else {
	    	_uiHandler.post(new ChangeText("NetProtocolError : sendData while not connected."));
		}
	}
	
    public byte[] encode(int dx, int dy) {
    	byte[] result = new byte[6];
    	byte[] base = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    	
    	if(dx > 2047) dx = 2047;
    	if(dx < -2048) dx = -2048;
    	if(dx < 0) dx = 4096 + dx;
    	result[0] = base[dx/256];
    	result[1] = base[(dx%256)/16];
    	result[2] = base[dx%16];

    	if(dy > 2047) dy = 2047;
    	if(dy < -2048) dy = -2048;
    	if(dy < 0) dy = 4096 + dy;
    	result[3] = base[dy/256];
    	result[4] = base[(dy%256)/16];
    	result[5] = base[dy%16];
    	
    	return result;
    }
    
    public void mouseMove(int dx, int dy) {
    	sendData(encode(dx, dy), 6);
    }
    
    public void mouseLeftClick() {
		byte[] msg = {'m', 'c', 'l', 'l', 'l', 'l'};
    	sendData(msg, 6);
    }
    
    public void mouseRightClick() {
		byte[] msg = {'m', 'c', 'r', 'r', 'r', 'r'};
    	sendData(msg, 6);
    }
    
    public void mouseDown() {
    	byte[] msg = {'m', 'b', 'd', 'd', 'd', 'd'};
    	sendData(msg, 6);
    }
    
    public void mouseUp() {
    	byte[] msg = {'m', 'b', 'u', 'u', 'u', 'u'};
    	sendData(msg, 6);
    }
    
    public void scrollUp() {
    	byte[] msg = {'m', 's', 'u', 'u', 'u', 'u'};
    	sendData(msg, 6);
    }
    
    public void scrollDown() {
    	byte[] msg = {'m', 's', 'd', 'd', 'd', 'd'};
    	sendData(msg, 6);
    }
    
    

}
