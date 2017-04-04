package finalProject;

import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;


/**
 * This class is the parallel class to Forward which will be called in the event that
 * the robot is told to play defense during a certain round. As with the Forward class
 * this should be called directly after the initial localization of the robot.
 * The class should call navigation to bring the robot to the point on the edge of the 
 * bounce zone such that it is directly in the center of the x coordinates and between
 * the bounce zone and the center point of the forward line. The robot should
 * do this while also avoiding any obstacles that may be found between the start corner
 * and destination.
 * 
 * @author Ilana Haddad
 * @version 1.0
 *
 */
public class Defense {

	private int corner;	//starting corner
	private int w1; //defender zone dimension w1
	private int w2; //defender zone dimension w2
	private final double TILE_LENGTH = 30.48;
	
	// Left motor connected to output A
	// Right motor connected to output D
	public static final EV3LargeRegulatedMotor leftMotor = WiFiExample.leftMotor;
	public static final EV3LargeRegulatedMotor rightMotor = WiFiExample.rightMotor;
	private static final Port usPort = LocalEV3.get().getPort("S1");
	
	/**
	 * The navigation program for the robot 
	 */
	public static Navigation nav = WiFiExample.navigation;
	/** 
	 * THe odometer of robot
	 */
	public static Odometer odo = WiFiExample.odometer;

	
	//Setup ultrasonic sensor
	// 1. Create a port object attached to a physical port (done above)
	// 2. Create a sensor instance and attach to port
	// 3. Create a sample provider instance for the above and initialize operating mode
	// 4. Create a buffer for the sensor data
	@SuppressWarnings("resource")							    	// Because we don't bother to close this resource
	SensorModes usSensor = new EV3UltrasonicSensor(usPort);
	SampleProvider usValue = usSensor.getMode("Distance");			// colorValue provides samples from this instance
	float[] usData = new float[usValue.sampleSize()];				// colorData is the buffer in which data are returned
	
		
	/**
	 * 
	 * @param corner the starting corner of the robot
	 * @param w1 the size of the bounce zone in the x dimension
	 * @param w2 the size of the bounce zone in the y dimension
	 */
	public Defense(int corner, int w1, int w2) {
		this.corner = corner;
		this.w1 = w1;
		this.w2 = w2;
	}

	/**
	 * The method which will direct the robot to the correct position on the field.
	 * Based on the dimensions of the bounce zone, the method will direct the robot
	 * to the point which is directly on the edge of the bounce zone without it entering the
	 * zone and thus breaking the rules of the game. It will then remain there for the
	 * rest of the round. All movements will be performed by calling navigation while ensuring
	 * that the Y dimension is traveled first, ensuring that the robot does not enter the
	 * forward zone.
	 */
	public void startDEF() {
		//already localized
		//step 1 = travel to middle of w1,w2 zone (while avoiding obstacles)!!
		//step 2 = block balls from entering target
		
		if(w2 == 2){ //go to coordinates (5, 7.5) 
			//travel in y first
			nav.travelToYFIRST(5*TILE_LENGTH, 7.5*TILE_LENGTH);
		}
		if(w2 == 3){ //go to coordinates (5, 6.5) 
			nav.travelToYFIRST(5*TILE_LENGTH, 6.5*TILE_LENGTH);
		}
		if(w2 == 4){ //go to coordinates (5, 5.5) 
			nav.travelToYFIRST(5*TILE_LENGTH, 5.5*TILE_LENGTH);
		}
	
		
	}

}
