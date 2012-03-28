package com.zakinster.trackpadroid;

import java.io.IOException;

import android.graphics.Color;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

public class OnTouchListener implements View.OnTouchListener {
	private long _lastTimeDown = 0;
	private boolean _isMouseDown = false;
	private boolean _lastActionIsClick = false;
    private TextView _textView;
    private NetProtocol _netProtocol;
    private float _x = 0, _y = 0;
    private float _ldx = 0, _ldy = 0;
    private float _lcx = 0, _lcy = 0;
    DataSender.Data _dataToSend;
    private int _toScroll = 0;
    private int _nPointer = 0;
    private Mutable.Int _scrollSensitivity;
    
    public OnTouchListener(DataSender.Data dataToSend, Mutable.Int scrollSensitivity, NetProtocol netProtocol, TextView textView) {
    	_dataToSend = dataToSend;
    	_netProtocol = netProtocol;
    	_textView = textView;
    	_scrollSensitivity = scrollSensitivity;
    }
	
    //@Override
    public boolean onTouch(View v, MotionEvent event) {

    	switch(event.getActionMasked()) {
	    	case MotionEvent.ACTION_POINTER_DOWN:
	    	{
	    		break;
	    	}
	    	case MotionEvent.ACTION_POINTER_UP:
	    	{
	    		break;
	    	}
        	case MotionEvent.ACTION_DOWN:  
        	{
        		long llastTimeDown = _lastTimeDown;
        		_lastTimeDown = System.currentTimeMillis();

        		float dx = Math.abs(event.getX()-_lcx);
        		float dy = Math.abs(event.getY()-_lcy);
        		_isMouseDown = _lastActionIsClick &&  (_lastTimeDown - llastTimeDown) < 200 && dx<50 && dy<50;
        		
        		if(_isMouseDown && _netProtocol.isConnected()){
        			_netProtocol.mouseDown();
        		}
        		_x = _ldx = event.getX();
        		_y = _ldy = event.getY();
        		
        		break;
        	}
        	
        	case MotionEvent.ACTION_MOVE:
        	{
        		_lastActionIsClick = false;
        		int dx = Math.round(event.getX() - _x);
        		int dy = Math.round(event.getY() - _y);
        		
        		if(_nPointer==1) {
	        		synchronized(_dataToSend) {
	        			_dataToSend.moveX += dx;
	        			_dataToSend.moveY += dy;
	        		}
        		}
        		else {
            		_toScroll += dy;
            		while(Math.abs(_toScroll)>_scrollSensitivity.value) {
	    				if(_toScroll>0) {
	    					_netProtocol.scrollUp();
		    				_toScroll -= _scrollSensitivity.value; 
	    				}
	    				else {
	    					_netProtocol.scrollDown();
		    				_toScroll += _scrollSensitivity.value; 
	    				}
            		}
            		//for(int i=0; i<Math.abs(dy)/20; i++) {
            		//}
        		}
        		_x = event.getX();
        		_y = event.getY();
        		
        		break;
        	}
        	
        	case MotionEvent.ACTION_UP:
        	{
        		if(_isMouseDown && _netProtocol.isConnected()){
        			_netProtocol.mouseUp();
        		}

        		float dx = Math.abs(event.getX()-_ldx);
        		float dy = Math.abs(event.getY()-_ldy);
        		_lastActionIsClick = (System.currentTimeMillis()-_lastTimeDown)<100 && dx<5 && dy<5;
        		
        		if(_lastActionIsClick && _netProtocol.isConnected()){

            		_lcx = event.getX();
            		_lcy = event.getY();
        			if(_nPointer==1) {
	        			_netProtocol.mouseLeftClick();
        			}
        			else {
	        			_netProtocol.mouseRightClick();
        			}
        		}
        		
        		break;
        	}
    	}
    	

		_nPointer = event.getPointerCount();
		_textView.setText(""+event.getPointerCount());
    	
        return true;
    }
}
