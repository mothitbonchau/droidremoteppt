package net.zaczek.droidRemotePPT;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.zaczek.droidRemotePPT.R;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class Main extends ActionBarActivity {
	private BluetoothAdapter mBluetoothAdapter;
	private List<DeviceListViewItem> lstDevices;
	private static ConnectThread connectThread;
	private final Handler mHandler = new Handler();
	
	final Runnable mConnectedToServer = new Runnable() {
        public void run() {
            onConnectedToServer();
        }
    };
	final Runnable mConnectedToServerFailed = new Runnable() {
        public void run() {
            onConnectedToServerFailed();
        }
    };
    
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		setTitle(R.string.app_title);
		
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
			Log.e("drPPT", "No bluetooth support");
			Toast.makeText(getApplicationContext(), "Device does not support Bluetooth", Toast.LENGTH_LONG).show();
		} else {
			fillDevices();
		}
	}

	protected void onConnectedToServerFailed() {
		Toast.makeText(getApplicationContext(), "Unable to connect to Server", Toast.LENGTH_LONG).show();
	}

	protected void onConnectedToServer() {
		startActivity(new Intent(net.zaczek.droidRemotePPT.RemoteControl.CONTROL_ACTION));
	}

	private class DeviceListViewItem {
		private BluetoothDevice mDevice;
		
		public BluetoothDevice getDevice() {
			return mDevice;
		}
		public DeviceListViewItem(BluetoothDevice device) {
			mDevice = device;
		}
		
		@Override
		public String toString() {
			return mDevice.getName();
		}
	}

	private void fillDevices() {
		
		Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
		lstDevices = new ArrayList<DeviceListViewItem>();
		// If there are paired devices
		if (pairedDevices.size() > 0) {
			// Loop through paired devices
			for (BluetoothDevice device : pairedDevices) {
				Log.v("drPPT", device.getName());
				lstDevices.add(new DeviceListViewItem(device));
			}
		}
		ArrayAdapter<DeviceListViewItem> adapter = new ArrayAdapter<DeviceListViewItem>(getApplicationContext(), R.layout.devicelist_item, lstDevices);
		
		ListView lstView = (ListView) findViewById(R.id.lstDevices);
		lstView.setAdapter(adapter);
		lstView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				connectToDevice(lstDevices.get(position).getDevice());				
			}
		});
	}

	protected void connectToDevice(BluetoothDevice device) {
		String text = "Connecting to " + device.getName();
		Log.i("drPPT", text);
		Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
		
		connectThread = new ConnectThread(device);
		connectThread.connect(mHandler, mConnectedToServer, mConnectedToServerFailed);
	}
	
	public static BluetoothSocket getConnectedSocket() {
		if(connectThread == null) return null;
		return connectThread.getSocket();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		switch (itemId) {
		case R.id.action_about:
			startActivity(new Intent(this, About.class));
			return true;
		case R.id.action_exit:
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
}