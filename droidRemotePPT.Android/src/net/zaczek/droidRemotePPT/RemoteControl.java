package net.zaczek.droidRemotePPT;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import net.zaczek.droidRemotePPT.Messages.PPTMessage;
import net.zaczek.droidRemotePPT.Messages.ScreenSizeMessage;
import net.zaczek.droidRemotePPT.Messages.SlideChangedMessage;
import net.zaczek.droidRemotePPT.Messages.SelectSlideMessage;
import net.zaczek.droidRemotePPT.Messages.VersionMessage;
import net.zaczek.droidRemotePPT.Main;
import net.zaczek.droidRemotePPT.R;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
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
	private Button btnToStart;
	private ToggleButton tgglScrn;
	private ToggleButton tgglNotes;
	private ToggleButton tgglOrienation;
	private ProgressBar progressBar;
	private BluetoothThread btThread;
	private TextView slideCounter;
	private TextView notesView;
	private Button endButton;

	private static int version = 15; // for further development
	private int totalSlides;
	public int currSlide = 0;
	public int markedSlide = 1;
	private String notes = null;

	private static class MessageHandler extends Handler {
		private final WeakReference<RemoteControl> mTarget;

		public MessageHandler(RemoteControl target) {
			mTarget = new WeakReference<RemoteControl>(target);
		}

		@Override
		public void handleMessage(Message arg) {
			super.handleMessage(arg);

			final PPTMessage msg = (PPTMessage) arg.obj;
			final RemoteControl outer = mTarget.get();

			if (outer != null && msg.getClass() == SlideChangedMessage.class) {

				SlideChangedMessage scm = (SlideChangedMessage) msg;
				outer.totalSlides = scm.getTotalSlides();

				if (outer.totalSlides > 0) {
					outer.imgView.setImageBitmap(scm.getBmp());
					outer.progressBar.setMax(outer.totalSlides);
					outer.notesView.scrollTo(0, 0);

					outer.slideCounter.setVisibility(View.VISIBLE);

					if (checkVersion() || !outer.tgglScrn.isChecked()) {

						if (!outer.tgglNotes.isChecked())
							outer.notesView.setVisibility(View.GONE);

						outer.currSlide = scm.getCurrentSlide();
						outer.markSlide(outer.markedSlide);

						if (outer.currSlide <= outer.totalSlides) {
							outer.progressBar.setProgress(outer.currSlide);
							outer.slideCounter.setText(outer.currSlide + "/"
									+ outer.totalSlides);

							outer.notes = scm.getCurrentNotes();

							if (outer.notes != "" && outer.notes != null) {
								outer.notesView.setText(outer.notes);
							}
						} else {
							if (outer.currSlide > 0) {
								outer.notesView.setVisibility(View.VISIBLE);
								outer.notesView.setText("Slideshow finished!");
							}
						}
					}

					if (outer.currSlide == 1) {
						outer.btnPrev.setVisibility(View.GONE);

					} else {
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
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		btnToStart = (Button) findViewById(R.id.toStart);
		slideCounter = (TextView) findViewById(R.id.slideCounter);
		notesView = (TextView) findViewById(R.id.notesView);
		endButton = (Button) findViewById(R.id.endButton);

		tgglScrn = (ToggleButton) findViewById(R.id.toggleScreen);
		tgglNotes = (ToggleButton) findViewById(R.id.toggleNotes);
		tgglOrienation = (ToggleButton)findViewById(R.id.toggleOrientation);

		gestView.addOnGesturePerformedListener(this);

		notesView.setMovementMethod(new ScrollingMovementMethod());

		tgglNotes
				.setOnCheckedChangeListener(new ToggleButton.OnCheckedChangeListener() {

					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						if (isChecked) {
							imgView.setVisibility(View.GONE);
							notesView.setVisibility(View.VISIBLE);

							if (currSlide <= totalSlides)
								notesView.setText(notes);
							else if (currSlide > 0)
								notesView.setText("Slideshow finished!");
						} else {
							if (currSlide <= totalSlides)
								notesView.setVisibility(View.GONE);
							imgView.setVisibility(View.VISIBLE);
						}
					}

				});

		endButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Finish();
			}
		});

		endButton.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				sendEnd();
				return true;
			}
		});

		if (currSlide <= totalSlides)
			tgglScrn.setOnCheckedChangeListener(new ToggleButton.OnCheckedChangeListener() {
				public void onCheckedChanged(CompoundButton buttonView,
						boolean isChecked) {
					if ((checkVersion() || isChecked)) {
						sendBlack();
					} else {
						if (currSlide <= totalSlides)
							sendUnBlack();
					}
				}

			});

		slideCounter.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				markSlide(currSlide);
			}
		});

		slideCounter.setOnLongClickListener(new View.OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
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

		btnNext.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				sendLast();
				return true;
			}
		});

		btnPrev.setOnLongClickListener(new View.OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
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

		btnToStart.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sendSlide(markedSlide);
				markSlide(currSlide);
			}
		});
		
		tgglOrienation.setOnCheckedChangeListener(new ToggleButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if(isChecked) {
					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
				} else {
					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
				}
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
			// Prediction prediction = (Prediction) predictions.get(0);
			// We want at least some confidence in the result
			if (prediction.score > 1.0) {
				if ("LEFT".equals(prediction.name)
						&& (checkVersion() || !tgglScrn.isChecked())) {
					sendNext();
				} else if ("RIGHT".equals(prediction.name)
						&& (checkVersion() || !tgglScrn.isChecked())) {
					sendPrev();
				} else if ("UP".equals(prediction.name)) {
					if (!tgglScrn.isChecked()) {
						System.out.println("false");
						tgglScrn.setChecked(true);
						tgglScrn.setSelected(true);

					} else {
						System.out.println("true");
						tgglScrn.setChecked(false);
						tgglScrn.setSelected(false);

					}
				} else if ("DOWN".equals(prediction.name)) {

					if (!tgglNotes.isChecked()) {
						System.out.println("false");
						tgglNotes.setChecked(true);
						tgglNotes.setSelected(true);

					} else {
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
	}

	private static boolean checkVersion() {
		if (version >= 14)
			return true;

		return false;
	}

	private void sendPrev() {

		if (!tgglScrn.isChecked()) {
			btThread.sendSimpleMessage(PPTMessage.MESSAGE_PREV);
		} else {
			tgglScrn.setSelected(false);
			tgglScrn.setChecked(false);
			if (checkVersion())
				btThread.sendSimpleMessage(PPTMessage.MESSAGE_PREV);
			else
				sendSlide(currSlide - 1);
		}
	}

	private void sendNext() {
		if (!tgglScrn.isChecked()) {
			btThread.sendSimpleMessage(PPTMessage.MESSAGE_NEXT);
		} else {
			tgglScrn.setSelected(false);
			tgglScrn.setChecked(false);
			if (checkVersion())
				btThread.sendSimpleMessage(PPTMessage.MESSAGE_NEXT);
			else
				sendSlide(currSlide + 1);
		}

		if (currSlide > totalSlides)
			toMain();
	}

	private void sendFirst() {
		btThread.sendSimpleMessage(PPTMessage.MESSAGE_FIRST_PAGE);
	}

	private void sendLast() {
		btThread.sendSimpleMessage(PPTMessage.MESSAGE_LAST_PAGE);
	}

	private void markSlide(int slide) {
		if (slide < totalSlides) {
			markedSlide = slide;
			btnToStart.setText("To " + slide + "/" + totalSlides);
			System.out.println("\nmarkedSlide=" + markedSlide);
		}
	}

	protected void sendSlide(int slide) {
		SelectSlideMessage msg = new SelectSlideMessage(slide);
		btThread.sendMessage(msg);
	}

	private void sendBlack() {
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
		btThread.sendSimpleMessage(PPTMessage.MESSAGE_END);
		toMain();
	}

	private void toMain() {
		Intent i = new Intent(this, Main.class);
		startActivity(i);
	}
}
