package dasz.droidRemotePPT;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import dasz.droidRemotePPT.Messages.PPTMessage;
import dasz.droidRemotePPT.Messages.SimpleMessage;
import dasz.droidRemotePPT.Messages.SlideChangedMessage;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

public class BluetoothThread extends Thread {
	private final DataInputStream mmInStream;
	private final DataOutputStream mmOutStream;
	private final Handler mHandler;
	private final BluetoothSocket socket;

	public BluetoothThread(BluetoothSocket s, Handler handler) {
		socket = s;
		mHandler = handler;
		DataInputStream tmpIn = null;
		DataOutputStream tmpOut = null;

		// Get the input and output streams, using temp objects because
		// member streams are final
		try {
			tmpIn = new DataInputStream(s.getInputStream());
			tmpOut = new DataOutputStream(s.getOutputStream());
		} catch (IOException e) {
			Log.e("drPPT", e.toString());
		}

		mmInStream = tmpIn;
		mmOutStream = tmpOut;

	}
	
	public void stopThread() {
		try {
			socket.close();
		} catch (IOException e) {
			// dont care
			e.printStackTrace();
		}
	}

	public void sendSimpleMessage(int msg) {
		try {
			mmOutStream.writeByte(msg);
		} catch (IOException e) {
			Log.e("drPPT", e.toString());
		}
	}

	public void sendMessage(PPTMessage msg) {
		try {
			mmOutStream.writeByte(msg.getMessageId());
			msg.write(mmOutStream);
		} catch (IOException e) {
			Log.e("drPPT", e.toString());
		}
	}

	public void run() {
		// Keep listening to the InputStream until an exception occurs
		while (true) {
			try {
				// Read from the InputStream
				//byte[] buffer = new byte[1];
				//if(mmInStream.read(buffer) == 0) break;
				//byte msgID = buffer[0];
				byte msgID = mmInStream.readByte();
				if(msgID == 0) continue;
				PPTMessage msg;
				switch (msgID) {
				case PPTMessage.MESSAGE_SLIDE_CHANGED:
					msg = new SlideChangedMessage();
					break;
				default:
					msg = new SimpleMessage(msgID);
					break;
				}

				msg.read(mmInStream);
				// Send the obtained bytes to the UI Activity
				mHandler.obtainMessage(1, msg).sendToTarget();
			} catch (IOException e) {
				break;
			}
		}
	}
}
