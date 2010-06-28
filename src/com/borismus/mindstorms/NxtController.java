package com.borismus.mindstorms;

import java.io.IOException;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

/**
 * Provides all low-level communication with the LEGO Mindstorms NXT device
 * 
 * Currently supports messageWrite and setOutputMode commands only.
 * See also: http://bricxcc.sourceforge.net/nbc/nxcdoc/nxcapi/index.html
 * 
 * @author boris
 *
 */
public class NxtController {
	private static final String TAG = NxtController.class.getName();
	// Low-level constants from NXT firmware
	final static byte OUT_A = 0x00;
	final static byte OUT_B = 0x01;
	final static byte OUT_C = 0x02;
	final static byte OUT_AB = 0x03;

	final static byte OUT_MODE_MOTORON = 0x01;
	final static byte OUT_MODE_BRAKE = 0x02;

	final static byte OUT_REGMODE_IDLE = 0x00;
	final static byte OUT_REGMODE_SPEED = 0x01;

	final static byte OUT_MODE_REGULATED = 0x04;

	final static byte OUT_RUNSTATE_RUNNING = 0x20;
	final static byte OUT_RUNSTATE_HOLD = 0x60;

	// Bluetooth setup
	final String deviceId = "00:16:53:09:B4:20";
	BluetoothSocket socket;
	BluetoothAdapter adapter;

	/**
	 * Establishes a connection with the LEGO Mindstorms NXT device.
	 * Relies on a hardcoded device ID
	 * @return true iff the connection is successfully established 
	 */
	public boolean connect() {
		boolean success = false;
		adapter = BluetoothAdapter.getDefaultAdapter();
		if (adapter == null) {
			// Device does not support Bluetooth
			return false;
		}
		BluetoothDevice nxt = adapter.getRemoteDevice(deviceId);
		try {
			socket = nxt.createRfcommSocketToServiceRecord(UUID
					.fromString("00001101-0000-1000-8000-00805F9B34FB"));
			socket.connect();
			success = true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return success;
	}

	/**
	 * Implements the messageWrite API on the NXT device
	 * @param mailbox the mailbox ID on the NXT
	 * @param message the string to send
	 */
	public void messageWrite(int mailbox, String message) {
		byte[] buffer = new byte[message.length() + 5];
		buffer[0] = (byte) 0x80;
		buffer[1] = (byte) 0x09;
		buffer[2] = (byte) mailbox;
		buffer[3] = (byte) (message.length() + 1);
		for (int i = 0; i < message.length(); i++) {
			buffer[i + 4] = (byte) (message.charAt(i) & 0xff);
		}
		buffer[message.length() + 4] = 0;
		sendDirectMessageHelper(buffer);
	}

	/**
	 * Sets output state (ie. servo control) on the NXT device
	 * 
	 * @param port which motor to turn on (OUT_A, OUT_B, etc)
	 * @param power power level (-100 to 100)
	 * @param angle how many degrees to spin before stopping
	 * @param mode OUT_MODE_* running or breaking
	 * @param regmode OUT_REGMODE_*
	 */
	public void setOutputState(byte port, int power, int angle, int mode,
			int regmode) {
		byte[] buffer = new byte[12];
		buffer[0] = (byte) 0x80;
		buffer[1] = (byte) 0x04;
		buffer[2] = port;
		buffer[3] = (byte) power;
		buffer[4] = (byte) mode;
		buffer[5] = (byte) regmode;
		buffer[6] = 0;
		buffer[7] = OUT_RUNSTATE_RUNNING;
		buffer[8] = (byte) (angle & 0xff);
		buffer[9] = (byte) ((angle >> 8) & 0xff);
		buffer[10] = (byte) ((angle >> 16) & 0xff);
		buffer[11] = (byte) ((angle >> 24) & 0xff);
		sendDirectMessageHelper(buffer);
	}	
	
	public void setOutputState(byte port, int power, int angle) {
		setOutputState(port, power, angle, OUT_MODE_MOTORON, OUT_REGMODE_IDLE);
	}

	/**
	 * Helper to establish low level communication stream to the NXT
	 * @param message byte array to send over.
	 */
	private void sendDirectMessageHelper(byte[] message) {
		try {
			socket.getOutputStream().write(message.length & 0xff);
			socket.getOutputStream().write((message.length >> 8) & 0xff);
			socket.getOutputStream().write(message);
			socket.getOutputStream().flush();
		} catch (Exception e) {
			Log.e(TAG, "Error Sending Message: " + e.getMessage());
		}
	}


}
