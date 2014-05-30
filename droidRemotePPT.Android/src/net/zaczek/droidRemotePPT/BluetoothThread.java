package net.zaczek.droidRemotePPT;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.zaczek.droidRemotePPT.Messages.PPTMessage;
import net.zaczek.droidRemotePPT.Messages.SimpleMessage;
import net.zaczek.droidRemotePPT.Messages.SlideChangedMessage;
import net.zaczek.droidRemotePPT.Messages.NotesMessage;

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
			// don't care
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
				byte msgID = mmInStream.readByte();
				if (msgID <= 0
						|| msgID >= PPTMessage.FIRST_INVALID_MESSAGE_NUMBER)
					continue;
				PPTMessage msg;
				switch (msgID) {
//				case PPTMessage.MESSAGE_SELECT_PAGE:
				case PPTMessage.MESSAGE_SLIDE_CHANGED:
					msg = new SlideChangedMessage();
					break;
				case PPTMessage.MESSAGE_NOTES:
					msg = new NotesMessage();
					break;
				
				default:
					msg = new SimpleMessage(msgID);
					break;
				}

				msg.read(mmInStream);
				if(msg.isValid() == false) continue;
				// Send the obtained bytes to the UI Activity
				mHandler.obtainMessage(1, msg).sendToTarget();
			} catch (IOException e) {
				break;
			}
		}
	}
}
