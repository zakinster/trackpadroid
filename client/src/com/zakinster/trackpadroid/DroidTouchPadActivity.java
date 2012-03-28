package com.zakinster.trackpadroid;

import java.text.DecimalFormat;

import com.zakinster.droidtouchpad.R;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

public class DroidTouchPadActivity extends Activity {

	
	private DataSender.Data _dataToSend = new DataSender.Data();
	private Mutable.Int _refreshRate = new Mutable.Int();
	private Mutable.Int _scrollSensitivity = new Mutable.Int();
	private Mutable.Double _pointerSensitivity = new Mutable.Double();
	private Mutable.Double _pointerAcceleration = new Mutable.Double();
	private NetProtocol _netProtocol;
	private Handler uiHandler = new Handler();
	

	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	try {
			Class.forName("android.os.AsyncTask");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        super.onCreate(savedInstanceState);
        // hide titlebar of application
        // must be before setting the layout
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // hide statusbar of Android
        // could also be done later
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.main);
        
        final View touchView = findViewById(R.id.touchView);
        final TextView textView = (TextView)findViewById(R.id.textView);  
        final TextView textView2 = (TextView)findViewById(R.id.textView2);  
        final EditText hostText = (EditText)findViewById(R.id.hostText);  
        final Button connectButton = (Button)findViewById(R.id.button1); 
        final SeekBar seekBarRefreshRate = (SeekBar)findViewById(R.id.seekBarRefreshRate); 
        final SeekBar seekBarScrollSensitivity = (SeekBar)findViewById(R.id.seekBarScrollSensitivity);
        final SeekBar seekBarPointerAcceleration = (SeekBar)findViewById(R.id.seekBarPointerAcceleration); 
        final SeekBar seekBarPointerSensitivity = (SeekBar)findViewById(R.id.seekBarPointerSensitivity); 
        final TextView textRefreshRate = (TextView)findViewById(R.id.textRefreshRate);  
        final TextView textPointerSensitivity = (TextView)findViewById(R.id.textPointerSensitivity); 
		final TextView textPointerAcceleration = (TextView)findViewById(R.id.textPointerAcceleration);  
		final TextView textScrollSensitivity = (TextView)findViewById(R.id.textScrollSensitivity);  
        final DecimalFormat decimalFormat = new DecimalFormat("0.00");
        
    	
		_netProtocol = new NetProtocol(uiHandler, textView, connectButton);

		_refreshRate.value = 30;
		textRefreshRate.setText(""+_refreshRate.value+"Hz");
		seekBarRefreshRate.setMax(90);
		seekBarRefreshRate.setProgress(20);
		seekBarRefreshRate.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
    		public void onStartTrackingTouch(SeekBar seekBar) {}
    		public void onStopTrackingTouch(SeekBar seekBar) {}
    		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    			_refreshRate.value = 10+progress;
    			textRefreshRate.setText(""+_refreshRate.value+"Hz");
    		}
        });

		_pointerSensitivity.value = 1.0;
		textPointerSensitivity.setText(decimalFormat.format(_pointerSensitivity.value));
		seekBarPointerSensitivity.setMax(450);
		seekBarPointerSensitivity.setProgress(100);
		seekBarPointerSensitivity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
    		public void onStartTrackingTouch(SeekBar seekBar) {}
    		public void onStopTrackingTouch(SeekBar seekBar) {}
    		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    			_pointerSensitivity.value = 0.5+(double)progress/100; 
    			textPointerSensitivity.setText(decimalFormat.format(_pointerSensitivity.value));
    		}
        });

		_pointerAcceleration.value = 1.4;
		textPointerAcceleration.setText(decimalFormat.format(_pointerAcceleration.value));
		seekBarPointerAcceleration.setMax(50);
		seekBarPointerAcceleration.setProgress(40);
		seekBarPointerAcceleration.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
    		public void onStartTrackingTouch(SeekBar seekBar) {}
    		public void onStopTrackingTouch(SeekBar seekBar) {}
    		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    			_pointerAcceleration.value = 1.0+(double)progress/100; 
    			textPointerAcceleration.setText(decimalFormat.format(_pointerAcceleration.value));
    		}
        });

		_scrollSensitivity.value = 20;
		textScrollSensitivity.setText(""+_scrollSensitivity.value+"px");
		seekBarScrollSensitivity.setMax(39);
		seekBarScrollSensitivity.setProgress(19);
		seekBarScrollSensitivity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
    		public void onStartTrackingTouch(SeekBar seekBar) {}
    		public void onStopTrackingTouch(SeekBar seekBar) {}
    		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    			_scrollSensitivity.value = 1+39-progress;
    			textScrollSensitivity.setText(""+_scrollSensitivity.value+"px");
    		}
        });
        
        connectButton.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
    			connectButton.setEnabled(false);
        		if(!_netProtocol.isConnected()) {
        	        Runnable LaunchDataSender = new Runnable() {
        	            public void run() {
        	            	DataSender ds = new DataSender(_netProtocol, _dataToSend, _refreshRate, _pointerSensitivity, _pointerAcceleration, connectButton, textView);
        	    			ds.start();
        	            }
        	    	};
                	_netProtocol.connect(hostText.getText().toString(), 8500, LaunchDataSender);
        		}
        		else {
                	_netProtocol.disconnect(true);
        		}
        	}
        });
        
		touchView.setBackgroundColor(Color.BLACK);
        touchView.setOnTouchListener(new OnTouchListener(_dataToSend, _scrollSensitivity, _netProtocol, textView2));  
    }
}