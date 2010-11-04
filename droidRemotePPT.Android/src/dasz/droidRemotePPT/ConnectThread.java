package dasz.droidRemotePPT;

import java.io.IOException;
import java.util.UUID;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

public class ConnectThread extends Thread {
	private final BluetoothSocket mmSocket;

	public BluetoothSocket getSocket() {
		return mmSocket;
	}

	public ConnectThread(BluetoothDevice device) {
		// Use a temporary object that is later assigned to mmSocket,
		// because mmSocket is final
		BluetoothSocket tmp = null;

		// Get a BluetoothSocket to connect with the given BluetoothDevice
		try {
			// MY_UUID is the app's UUID string, also used by the server code
			tmp = device.createRfcommSocketToServiceRecord(UUID.fromString("ABF32797-4DAE-4890-A23D-33DC8E3E2111"));
		} catch (IOException e) {
			Log.e("drPPT", e.toString());
		}
		mmSocket = tmp;
	}
	
	private Handler mHandler;
	private Runnable mCallback;
	private Runnable mFailedCallback;
	
	public void connect(Handler handler, Runnable callback, Runnable failedCallback) {
		mHandler = handler;
		mCallback = callback;
		mFailedCallback = failedCallback;
		this.start();
	}

	public void run() {
		try {
			// Connect the device through the socket. This will block
			// until it succeeds or throws an exception
			mmSocket.connect();
		} catch (IOException connectException) {
			// Unable to connect; close the socket and get out
			try {
				mmSocket.close();
			} catch (IOException closeException) {
				Log.e("drPPT", closeException.toString());
			}
			mHandler.post(mFailedCallback);
			return;
		}

		mHandler.post(mCallback);
	}

	/** Will cancel an in-progress connection, and close the socket */
	public void cancel() {
		try {
			mmSocket.close();
		} catch (IOException e) {
			Log.e("drPPT", e.toString());
		}
	}
}
