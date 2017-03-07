package finalProject;

import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;


/**
 * @author ilanahaddad
 * @version 1.0
 *
 */
public class Defense {

	private int corner;	//starting corner
	private int w1; //defender zone dimension w1
	private int w2; //defender zone dimension w2
	
	// Left motor connected to output A
	// Right motor connected to output D
	// Ball Launcher Motor connected to output B
	public static final EV3LargeRegulatedMotor leftMotor = WiFiExample.leftMotor;
	public static final EV3LargeRegulatedMotor rightMotor = WiFiExample.rightMotor;
	public static final EV3LargeRegulatedMotor launcherMotor = WiFiExample.launcherMotor;;
	private static final Port usPort = LocalEV3.get().getPort("S1");

	
	public static Odometer odometer = WiFiExample.odometer;
	public static Navigation navigation = WiFiExample.navigation;
	public static ballLauncher launch =  WiFiExample.launch;
	
	//Setup ultrasonic sensor
	// 1. Create a port object attached to a physical port (done above)
	// 2. Create a sensor instance and attach to port
	// 3. Create a sample provider instance for the above and initialize operating mode
	// 4. Create a buffer for the sensor data
	@SuppressWarnings("resource")							    	// Because we don't bother to close this resource
	SensorModes usSensor = new EV3UltrasonicSensor(usPort);
	SampleProvider usValue = usSensor.getMode("Distance");			// colorValue provides samples from this instance
	float[] usData = new float[usValue.sampleSize()];				// colorData is the buffer in which data are returned
	
		
	public Defense(int corner, int w1, int w2) {
		this.corner = corner;
		this.w1 = w1;
		this.w2 = w2;
	}

	public void startDEF() {
		//already localized
		//step 1 = travel to middle of w1,w2 zone (while avoiding obstacles)!!
		//step 2 = block balls from entering target
		
	
		
	}

}
