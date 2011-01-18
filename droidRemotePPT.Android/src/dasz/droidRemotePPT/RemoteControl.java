package dasz.droidRemotePPT;

import java.util.ArrayList;

import dasz.droidRemotePPT.Messages.DrawMessage;
import dasz.droidRemotePPT.Messages.PPTMessage;
import dasz.droidRemotePPT.Messages.ScreenSizeMessage;
import dasz.droidRemotePPT.Messages.SlideChangedMessage;

import android.app.Activity;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.GestureOverlayView.OnGesturePerformedListener;
import android.gesture.Prediction;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ToggleButton;
import android.widget.ImageView.ScaleType;

public class RemoteControl extends Activity implements
		OnGesturePerformedListener {
	public static final String CONTROL_ACTION = "dasz.droidRemotePPT.CONTROL";

	private ImageView imgView;
	private GestureOverlayView gestView;
	private GestureLibrary mGestureLibrary;
	private ToggleButton tbtnDraw;
	private ImageButton btnNext;
	private ImageButton btnPrev;
	private BluetoothThread btThread;

	final Handler mMessageHandler = new Handler() {
		@Override
		public void handleMessage(Message arg) {
			super.handleMessage(arg);

			PPTMessage msg = (PPTMessage) arg.obj;
			if (msg.getClass() == SlideChangedMessage.class) {
				SlideChangedMessage scm = (SlideChangedMessage) msg;
				imgView.setImageBitmap(scm.getBmp());
			}
		}
	};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.remotecontrol);

		mGestureLibrary = GestureLibraries
				.fromRawResource(this, R.raw.gestures);
		if (!mGestureLibrary.load()) {
			Log.e("drPPT", "Unable to load gestures");
			finish();
		}

		imgView = (ImageView) findViewById(R.id.imgPPT);
		gestView = (GestureOverlayView) findViewById(R.id.gestures);
		tbtnDraw = (ToggleButton) findViewById(R.id.tbtnDraw);
		btnNext = (ImageButton) findViewById(R.id.btnNext);
		btnPrev = (ImageButton) findViewById(R.id.btnPrev);

		gestView.addOnGesturePerformedListener(this);

		btnNext.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sendNext();
			}
		});

		btnPrev.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sendPrev();
			}
		});
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		btThread.stopThread();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	@Override
	protected void onStart() {
		super.onStart();

		btThread = new BluetoothThread(Main.getConnectedSocket(),
				mMessageHandler);
		btThread.start();
	}

	@Override
	protected void onResume() {
		super.onResume();

		Bitmap bmp = BitmapFactory.decodeResource(getResources(),
				R.drawable.icon);
		imgView.setScaleType(ScaleType.FIT_XY);
		imgView.setImageBitmap(bmp);
	}
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if(hasFocus) {
			ScreenSizeMessage msg = new ScreenSizeMessage(imgView.getWidth(),
					imgView.getHeight());
			btThread.sendMessage(msg);

			btThread.sendSimpleMessage(PPTMessage.MESSAGE_START);
		}
	}

	@Override
	public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {
		if (tbtnDraw.isChecked()) {
			float[] points = gesture.getStrokes().get(0).points;
			btThread.sendMessage(new DrawMessage(points));
		} else {
			ArrayList<Prediction> predictions = mGestureLibrary
					.recognize(gesture);

			// We want at least one prediction
			if (predictions.size() > 0) {
				Prediction prediction = predictions.get(0);
				// We want at least some confidence in the result
				if (prediction.score > 1.0) {
					if ("LEFT".equals(prediction.name)) {
						sendNext();
					} else if ("RIGHT".equals(prediction.name)) {
						sendPrev();
					} else if ("UP".equals(prediction.name)) {
						btThread.sendSimpleMessage(PPTMessage.MESSAGE_TOGGLE_BLACK_SCREEN);
					} else if ("DOWN".equals(prediction.name)) {
						btThread.sendSimpleMessage(PPTMessage.MESSAGE_CLEAR_DRAWING);
					}
				}
			}
		}
	}

	private void sendPrev() {
		btThread.sendSimpleMessage(PPTMessage.MESSAGE_PREV);
		tbtnDraw.setChecked(false);
	}

	private void sendNext() {
		btThread.sendSimpleMessage(PPTMessage.MESSAGE_NEXT);
		tbtnDraw.setChecked(false);
	}
}
