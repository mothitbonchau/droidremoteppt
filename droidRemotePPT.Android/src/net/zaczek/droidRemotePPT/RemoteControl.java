package net.zaczek.droidRemotePPT;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import net.zaczek.droidRemotePPT.Messages.PPTMessage;
import net.zaczek.droidRemotePPT.Messages.ScreenSizeMessage;
import net.zaczek.droidRemotePPT.Messages.SlideChangedMessage;
import net.zaczek.droidRemotePPT.Messages.SelectSlideMessage;
import net.zaczek.droidRemotePPT.Messages.VersionMessage;
import net.zaczek.droidRemotePPT.Messages.NotesMessage;
import net.zaczek.droidRemotePPT.Main;

import net.zaczek.droidRemotePPT.R;
import net.zaczek.droidRemotePPT.R.id;

import android.app.Activity;
import android.content.Intent;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.GestureOverlayView.OnGesturePerformedListener;
import android.gesture.Prediction;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.CompoundButton;


public class RemoteControl extends Activity implements
		OnGesturePerformedListener {
	public static final String CONTROL_ACTION = "dasz.droidRemotePPT.CONTROL";

	private ImageView imgView;
	private GestureOverlayView gestView;
	private GestureLibrary mGestureLibrary;
	private ImageButton btnNext;
	private ImageButton btnPrev;
	private Button button1;
	private ToggleButton tgglScrn;
	private ToggleButton tgglNotes;
	private ProgressBar progressBar;
	private BluetoothThread btThread;
	private TextView slideCounter;
	private TextView notesView;
	private Button endButton;
	private Button endNext;
//	private ImageView imgGal1; 			// for further development
	
	private static int version = 15; 	// for further development
	private static int totalSlides;
	public static int currSlide = 0;
	public static int markedSlide = 1;
	private static int markedSlideBlack = 1; // for further development
	private static String notes = null;
	
	private static class MessageHandler extends Handler
	{
		private final WeakReference<RemoteControl> mTarget;
		public MessageHandler(RemoteControl target) {
			mTarget = new WeakReference<RemoteControl>(target);
		}
		@Override
		public void handleMessage(Message arg) {
			super.handleMessage(arg);

			PPTMessage msg = (PPTMessage) arg.obj;
			final RemoteControl outer = mTarget.get();
			
			System.out.println("msg.getClass()=" + msg.getClass());
			
			if (outer != null && msg.getClass() == SlideChangedMessage.class) {
	
				SlideChangedMessage scm = (SlideChangedMessage) msg;
				totalSlides = scm.getTotalSlides();
				
				if(totalSlides > 0)
				{
//					if(outer.tgglNotes.isChecked())
					outer.imgView.setImageBitmap(scm.getBmp());
					outer.progressBar.setMax(totalSlides);
					outer.notesView.scrollTo(0, 0);
					
					outer.slideCounter.setVisibility(View.VISIBLE);
					
						if(checkVersion() || !outer.tgglScrn.isChecked())
						{
							
							if(!outer.tgglNotes.isChecked())
								outer.notesView.setVisibility(View.GONE);
							
							currSlide = scm.getCurrentSlide();
							outer.markSlide(markedSlide);
							
							if(outer.currSlide <= outer.totalSlides)
							{
								outer.progressBar.setProgress(currSlide);
								outer.slideCounter.setText(currSlide + "/" + totalSlides);
								
								outer.notes = scm.getCurrentNotes();
				
								if(outer.notes != "" && notes != null) 
								{
									outer.notesView.setText(outer.notes);
								}
							}
							else
							{
								if(outer.currSlide > 0)
								{
									outer.notesView.setVisibility(View.VISIBLE);
									outer.notesView.setText("Slideshow finished!");
								}
							}
						}
					
					if(currSlide == 1 )//|| (checkVersion() && outer.tgglScrn.isChecked()) )
					{
						outer.btnPrev.setVisibility(View.GONE);
						
					}
					else
					{
						outer.btnPrev.setVisibility(View.VISIBLE);
					}
				}
			}
		}
	}

	final Handler mMessageHandler = new MessageHandler(this);

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
		btnNext = (ImageButton) findViewById(R.id.btnNext);
		btnPrev = (ImageButton) findViewById(R.id.btnPrev);
		progressBar = (ProgressBar)findViewById(R.id.progressBar);
		button1 = (Button)findViewById(R.id.toStart);
		slideCounter =  (TextView)findViewById(R.id.slideCounter);
		notesView = (TextView)findViewById(R.id.notesView);
		endNext = (Button)findViewById(R.id.nextEnd);
		endButton = (Button)findViewById(R.id.endButton);
		
		tgglScrn = (ToggleButton)findViewById(R.id.toggleScreen);
		tgglNotes = (ToggleButton)findViewById(R.id.toggleNotes);

		
		gestView.addOnGesturePerformedListener(this);
		
		notesView.setMovementMethod(new ScrollingMovementMethod());
		
		tgglNotes.setOnCheckedChangeListener(new ToggleButton.OnCheckedChangeListener()
		{
			
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
				if(isChecked)
				{
					imgView.setVisibility(View.GONE);
					notesView.setVisibility(View.VISIBLE);
					
					if(currSlide <= totalSlides)
						notesView.setText(notes);
					else
						if(currSlide > 0)
						notesView.setText("Slideshow finished!");
				}
				else
				{
					if(currSlide <= totalSlides)
					notesView.setVisibility(View.GONE);
					imgView.setVisibility(View.VISIBLE);
				}
			}
	
		});
		
		
		endButton.setOnClickListener(new View.OnClickListener()
		{
	        public void onClick(View v) {
	            // TODO Auto-generated method stub	
	    		Finish();
	        }
		});
		
		endButton.setOnLongClickListener(new View.OnLongClickListener()
		{
	        @Override
	        public boolean onLongClick(View v) {
	            // TODO Auto-generated method stub
	            sendEnd();
	            return true;
	        }
		});
		
//		public boolean onTouchEvent(View v, MotionEvent event) {
//		       float x = event.getX();
//		       float y = event.getY();
//		       return true;
//		    }

		
		//For further implementations
//		endNext.setOnClickListener(new View.OnClickListener()
//		{
//	        public void onClick(View v) {
//	            // TODO Auto-generated method stub
//	        	Finish();
//	        }
//		});
//		
//		endNext.setOnLongClickListener(new View.OnLongClickListener()
//		{
//
//	        @Override
//	        public boolean onLongClick(View v) {
//	            // TODO Auto-generated method stub
//	            sendEnd();
//	            return true;
//	        }
//       });
		if(currSlide <= totalSlides)
		tgglScrn.setOnCheckedChangeListener(new ToggleButton.OnCheckedChangeListener()
		{
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
			{
					if((checkVersion() || isChecked))
					{
						sendBlack();
					}
					else
					{
						if(currSlide <= totalSlides)
							sendUnBlack();
					}
			}
		
		});
				
		slideCounter.setOnClickListener(new View.OnClickListener()
		{

	        @Override
	        public void onClick(View v) {
	            // TODO Auto-generated method stub
	            markSlide(currSlide);		
	        }
		});
		
		slideCounter.setOnLongClickListener(new View.OnLongClickListener()
		{

	        @Override
	        public boolean onLongClick(View v) {
	            // TODO Auto-generated method stub
	            sendFirst();
	            markSlide(currSlide);
	            return true;
	        }
       });

		btnNext.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sendNext();
				
			}
		});
		
		btnNext.setOnLongClickListener(new View.OnLongClickListener()
		{
	        @Override
	        public boolean onLongClick(View v) {
	            // TODO Auto-generated method stub
	        	sendLast();
	            return true;
	        }
       });
		
		btnPrev.setOnLongClickListener(new View.OnLongClickListener()
		{

	        @Override
	        public boolean onLongClick(View v) {
	            // TODO Auto-generated method stub
	            sendFirst();
	            return true;
	        }
       });

		btnPrev.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sendPrev();
			}
		});
		
		button1.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sendSlide( markedSlide );
				markSlide( currSlide );
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
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus) {
			ScreenSizeMessage msg = new ScreenSizeMessage(imgView.getWidth(),
					imgView.getHeight());
			
			btThread.sendMessage(msg);

			btThread.sendSimpleMessage(PPTMessage.MESSAGE_START);
		}
	}

	@Override
	public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {
		ArrayList<Prediction> predictions = mGestureLibrary.recognize(gesture);

		// We want at least one prediction
		if (predictions.size() > 0) {
			
			Prediction prediction = predictions.get(0);
			//Prediction prediction = (Prediction) predictions.get(0);
			System.out.println("-----------------------\n prediction.name="+prediction.name+ ", tgglScrn.isChecked=" +tgglScrn.isChecked()+"\n-----------------------");
			// We want at least some confidence in the result
			if (prediction.score > 1.0) {
				if ("LEFT".equals(prediction.name) && (checkVersion() || !tgglScrn.isChecked()) ) 
				{
					sendNext();
				} else if ("RIGHT".equals(prediction.name) && (checkVersion() || !tgglScrn.isChecked()) ) 
				{
					sendPrev();
				} else if ("UP".equals(prediction.name)) {
					if(!tgglScrn.isChecked())
					{
						System.out.println("false");
						tgglScrn.setChecked(true);
						tgglScrn.setSelected(true);
						
					}
					else
					{
						System.out.println("true");
						tgglScrn.setChecked(false);
						tgglScrn.setSelected(false);
						
					} 
					
//					btThread.sendSimpleMessage(PPTMessage.MESSAGE_TOGGLE_BLACK_SCREEN);
				} else if ("DOWN".equals(prediction.name)) {
					
					if(!tgglNotes.isChecked())
					{
						System.out.println("false");
						tgglNotes.setChecked(true);
						tgglNotes.setSelected(true);
						
					}
					else
					{
						System.out.println("true");
						tgglNotes.setChecked(false);
						tgglNotes.setSelected(false);
						
					} 
				}
			}
		}
	}
	

	protected void getVersion() // not implemented, yet
	{
		VersionMessage msg = new VersionMessage();
		btThread.sendMessage(msg);
//		btThread.sendSimpleMessage(PPTMessage.MESSAGE_VERSION);
	}

	private static boolean checkVersion()
	{
		if(version >= 14)
			return true;
		
		return false;
	}
	
	
	private void sendPrev() {
		
		if(!tgglScrn.isChecked())
		{
			btThread.sendSimpleMessage(PPTMessage.MESSAGE_PREV);
		}
		else
		{
			tgglScrn.setSelected(false);
			tgglScrn.setChecked(false);
			if(checkVersion())
				btThread.sendSimpleMessage(PPTMessage.MESSAGE_PREV);
			else
				sendSlide(currSlide - 1);
		}
	}

	private void sendNext() {
		if(!tgglScrn.isChecked())
		{
			btThread.sendSimpleMessage(PPTMessage.MESSAGE_NEXT);
		}
		else
		{
			tgglScrn.setSelected(false);
			tgglScrn.setChecked(false);
			if(checkVersion())
				btThread.sendSimpleMessage(PPTMessage.MESSAGE_NEXT);
			else
				sendSlide(currSlide + 1);
		}
		
		if(currSlide > totalSlides)
			toMain();
	}

	private void sendFirst() {
		btThread.sendSimpleMessage(PPTMessage.MESSAGE_FIRST_PAGE);
	}

	private void sendLast() {
		btThread.sendSimpleMessage(PPTMessage.MESSAGE_LAST_PAGE);
	}

	private void markSlide(int slide)
	{
		if(slide < totalSlides)
		{
			markedSlide = slide;
			button1.setText("To " + slide + "/" + totalSlides);
			System.out.println("\nmarkedSlide="+markedSlide);
		}
	
	}
	
	protected void sendSlide(int slide) {
		// TODO Auto-generated method stub
		
		SelectSlideMessage msg = new SelectSlideMessage(slide);
		btThread.sendMessage(msg);
	}
	
	private void sendBlack() {
		markedSlideBlack = currSlide;
		btThread.sendSimpleMessage(PPTMessage.MESSAGE_BLACK);
	}
	
	private void sendUnBlack() {
		btThread.sendSimpleMessage(PPTMessage.MESSAGE_UNBLACK);
	}
	
	private void Finish() {
		int curr = currSlide;
		sendLast();
		sendNext();
		currSlide = curr;
	}	
	
	private void sendEnd() {
//		imgView.setVisibility(View.GONE);
		btThread.sendSimpleMessage(PPTMessage.MESSAGE_END);
		toMain();
	}
	
	private void toMain()
	{
		Intent i  = new Intent(this,Main.class);
		startActivity(i);
	}
	
	private void getNotes() {
		//btThread.sendSimpleMessage(PPTMessage.MESSAGE_NOTES);
		NotesMessage msg = new NotesMessage();
		btThread.sendMessage(msg);
	}
}

