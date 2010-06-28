package com.borismus.mindstorms;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.Camera.PictureCallback;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.Window;
import android.widget.Toast;

public class MindstormsActivity extends Activity {
	private static final String TAG = MindstormsActivity.class.getName();
	
	// command thread
	final Thread mCommandThread = new Thread(new CommandLoop());
	
	// The current message to be sent to twitter
	String message;

	// Sensors
	SensorManager mSensorManager;

	// Camera stuff
	CameraPreview preview = null;
	
	// Nxt Controller
	NxtController nxt = new NxtController();
	
	// Robot Controller
	RobotController robot = new RobotController(nxt);
	
	// Twitter Controller
	TwitterController twitter = new TwitterController();

	enum Command {
		FACE_NORTH, FACE_EAST, FACE_SOUTH, FACE_WEST, LOOK_UP, LOOK_DOWN, LOOK_STRAIGHT, MOVE, TAKE_PHOTO
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hide the window title.
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        // Create our Preview view and set it as the content of our activity.
        if (preview == null) {
        	preview = new CameraPreview(this);
        	setContentView(preview);
        }
		
		// make sure bluetooth is on
		if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
			// bluetooth isn't enabled. error
			showError("Enable bluetooth and try again.");
			return;
		}

		// connect to the NXT
		if (!nxt.connect()) {
			// can't connect to NXT. show error
			showError("Can't connect to the Mindstorms NXT");
			return;
		}
		// make sure Internet is on
		ConnectivityManager conn = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		if (conn.getNetworkInfo(0).getState() != NetworkInfo.State.CONNECTED
				&& conn.getNetworkInfo(1).getState() != NetworkInfo.State.CONNECTED) {
			// not connected to the Internet. show error
			showError("Connect to the internet and try again.");
			return;
		}

		// set up all of the sensors
		enableSensors();
		
		// start the command loop in a separate thread
		mCommandThread.start();
		
		Toast.makeText(this, "Waiting for commands!", Toast.LENGTH_SHORT);
	}

	protected void onResume() {
        super.onResume();
        enableSensors();
    }

    protected void onStop() {
    	disableSensors();
        super.onStop();
    }

	public void onDestroy() {
		mCommandThread.stop();
    	disableSensors();
        super.onDestroy();
	}
	
	public void enableSensors() {
		mSensorManager = (SensorManager) this.getSystemService(Service.SENSOR_SERVICE);
		Sensor orientSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
		mSensorManager.registerListener(robot, orientSensor,
				SensorManager.SENSOR_DELAY_FASTEST);
		
	}
	public void disableSensors() {
		mSensorManager.unregisterListener(robot);
	}
    
    public void takePicture(String message) {
    	this.message = message;
    	preview.capture(jpegCallback);
    }
	 
	PictureCallback jpegCallback = new PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {
			twitter.uploadPicture(data, message);
	  }
	};

	private void showError(String message) {
		AlertDialog alertDialog = new AlertDialog.Builder(this).create();
		alertDialog.setTitle("Error");
		alertDialog.setMessage(message);
		alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				System.exit(1);

			}
		});
		alertDialog.show();
	}

	private class CommandLoop implements Runnable {

		@Override
		public void run() {
			// Wait a bit
			SystemClock.sleep(1000);
			
			// orient the camera so that it's looking straight
			robot.lookAngle(RobotController.ANGLE_STRAIGHT);
			takePicture("Main screen turn on!");
			
			while (true) {
				// wait for commands from twitter
				String commandString = twitter.getLatestCommand();
				if (commandString != null) {
					// parse command
					Command command = parseCommand(commandString);
					// execute command
					dispatchCommand(command);
				}
				// wait a bit...
				SystemClock.sleep(1000);
			}
		}
	}

	private void dispatchCommand(Command command) {
		switch (command) {
		case FACE_NORTH:
			robot.faceAngle(RobotController.ANGLE_NORTH);
			takePicture("Turned to face North.");
			break;
		case FACE_EAST:
			robot.faceAngle(RobotController.ANGLE_EAST);
			takePicture("Turned to face East.");
			break;
		case FACE_SOUTH:
			robot.faceAngle(RobotController.ANGLE_SOUTH);
			takePicture("Turned to face South.");
			break;
		case FACE_WEST:
			robot.faceAngle(RobotController.ANGLE_WEST);
			takePicture("Turned to face West.");
			break;
		case LOOK_UP:
			robot.lookAngle(RobotController.ANGLE_UP);
			takePicture("Looking up.");
			break;
		case LOOK_STRAIGHT:
			robot.lookAngle(RobotController.ANGLE_STRAIGHT);
			takePicture("Looking straight.");
			break;
		case LOOK_DOWN:
			robot.lookAngle(RobotController.ANGLE_DOWN);
			takePicture("Looking down.");
			break;
		case MOVE:
			robot.move(50);
			// wait a bit... since move is asynchronous
			SystemClock.sleep(3000);
			takePicture("Moved forward.");
			break;
		}
	}

	private Command parseCommand(String commandString) {
		commandString = commandString.toLowerCase();
		if (commandString.contains("north")) {
			return Command.FACE_NORTH;
		} else if (commandString.contains("east")) {
			return Command.FACE_EAST;
		} else if (commandString.contains("south")) {
			return Command.FACE_SOUTH;
		} else if (commandString.contains("west")) {
			return Command.FACE_WEST;
		} else if (commandString.contains("up")) {
			return Command.LOOK_UP;
		} else if (commandString.contains("straight")) {
			return Command.LOOK_STRAIGHT;
		} else if (commandString.contains("down")) {
			return Command.LOOK_DOWN;
		} else if (commandString.contains("forward") || commandString.contains("straight")) {
			return Command.MOVE;
		} 
		return null;
	}
}