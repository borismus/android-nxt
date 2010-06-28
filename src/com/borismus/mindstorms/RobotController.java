package com.borismus.mindstorms;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.SystemClock;
import android.util.Log;

/**
 * Class for high level control of the Mindstorms NXT rover built for the 
 * CrowdBot.
 * 
 * @author boris
 *
 */
public class RobotController implements SensorEventListener {
	private static final String TAG = RobotController.class.getName();

	// Application-level constants
	final int DEFAULT_POWER = 80;

	final static int ANGLE_NORTH = 0;
	final static int ANGLE_EAST = 90;
	final static int ANGLE_SOUTH = 180;
	final static int ANGLE_WEST = 270;

	final static int ANGLE_UP = -120;
	final static int ANGLE_STRAIGHT = -90;
	final static int ANGLE_DOWN = -50;

	// current heading
	int azimuth;
	// camera slant
	int pitch;
	
	NxtController nxt;
	
	RobotController(NxtController nxt) {
		this.nxt = nxt;
	}
	
	/**
	 * Turns the robot to face an angle corresponding to a cardinal direction 
	 * @param angle in degrees; 0 is north, 90 is east
	 */
	public void faceAngle(int angle) {
		// first lower the camera to the low position to get better compass results
		lookAngle(ANGLE_DOWN);
		
		// until the difference between the angle and the desired target
		// is small enough,
		while (Math.abs(azimuth - angle) > 20) {
			// compute difference between current azimuth and desired angle
			// NOTE: 0 < azimuth, angle < 360
			int diff = azimuth - angle;

			// see if going the other way is faster
			if (diff > 180) {
				diff -= 360;
			} else if (diff < -180) {
				diff += 360;
			}
			int direction = (int) Math.signum(diff);
			int amount = (int) (Math.pow(Math.abs(diff) * 0.3, 1.5));

			nxt.setOutputState(NxtController.OUT_A, DEFAULT_POWER * direction, amount);
			nxt.setOutputState(NxtController.OUT_B, -DEFAULT_POWER * direction, amount);

			// wait a bit...
			SystemClock.sleep(500);
		}
		
		// end by looking straight
		lookAngle(ANGLE_STRAIGHT);
	}
	
	/**
	 * Tilts the camera to look a certain direction
	 * @param angle to tilt, -90 straight, -180 flat 
	 */
	public void lookAngle(int angle) {
		// until the difference between the angle and the desired target
		// is small enough,
		while (Math.abs(pitch - angle) > 10) {
			// compute difference between current pitch and desired angle
			// NOTE: -180 < angle, angle < 180
			int diff = angle - pitch;

			int direction = (int) Math.signum(diff);
			int amount = (int) (Math.abs(diff) * 0.3);

			nxt.setOutputState(NxtController.OUT_C, DEFAULT_POWER * direction, amount);
			Log.i(TAG, "moving by diff: " + String.valueOf(diff));

			// wait a bit...
			SystemClock.sleep(300);
			
			// brake the motor
			nxt.setOutputState(NxtController.OUT_C, 0, 0, 
					NxtController.OUT_MODE_MOTORON + NxtController.OUT_MODE_BRAKE + NxtController.OUT_MODE_REGULATED, 
					NxtController.OUT_REGMODE_SPEED);
			
			// wait a bit more in the braked state so that the azimuth sensor 
			// can settle down
			SystemClock.sleep(1000);
		}
	}

	final float DISTANCE_MULTIPLIER = 15.0f;

	/**
	 * Moves the robot forward in the currently facing direction
	 * @param distance in centimetres (roughly)
	 */
	public void move(int distance) {
		nxt.setOutputState(NxtController.OUT_A, DEFAULT_POWER, (int) (distance * DISTANCE_MULTIPLIER));
		nxt.setOutputState(NxtController.OUT_B, DEFAULT_POWER, (int) (distance * DISTANCE_MULTIPLIER));
	}

	/**
	 * Handler for sensors. Updates azimuth and pitch
	 * TODO: understand why this sometimes mysteriously turns off. 
	 */
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
			azimuth = (int) event.values[0];
			pitch = (int) event.values[1];

			Log.i(TAG, "azimuth: " + String.valueOf(azimuth) + "\n" +
					   "pitch: " + String.valueOf(pitch));
		}
	}

	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub

	}

}
