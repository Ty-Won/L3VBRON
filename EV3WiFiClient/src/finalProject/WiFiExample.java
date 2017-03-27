package finalProject;

import java.util.Map;

import finalProject.BangBangController;
import finalProject.Defense;
import finalProject.Forward;
import finalProject.Navigation;
import finalProject.Odometer;
import finalProject.Launcher;
import finalProject.UltrasonicPoller;

import wifi.WifiConnection;
import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;


/**
 * Example class using WifiConnection to communicate with a server and receive
 * data concerning the competition such as the starting corner the robot is
 * placed in.
 * 
 * @author Michael Smith
 * @author Ilana Haddad
 * @version 2.0 
 */
public class WiFiExample {
	public static final double WHEEL_RADIUS = 2.0768;
	public static final double TRACK = 10.60; //changed it
	public static final int FORWARD_SPEED = 250;
	public static final int ROTATE_SPEED = 150;
	private static final int bandCenter = 35;			// Offset from the wall (cm)
	private static final int bandWidth = 3;				// Width of dead band (cm)
	private static final int motorLow = 100;			// Speed of slower rotating wheel (deg/sec)
	private static final int motorHigh = 200;			// Speed of the faster rotating wheel (deg/seec)
	private static final int PbandCenter = 32;			// Offset from the wall for PController (cm)
	private static final int PbandWidth = 2;			// Bandwidth for PController (cm)

	// Left motor connected to output A
	// Right motor connected to output D
	// Ball Launcher Motor connected to output B
	public static final EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
	public static final EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
	public static final EV3LargeRegulatedMotor launcherMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("B"));
	
	//colorPorts front, left side and right side of the EV3
	private static final Port colorPortF = LocalEV3.get().getPort("S2");	
	private static final Port colorPortL = LocalEV3.get().getPort("S3");	
	private static final Port colorPortR = LocalEV3.get().getPort("S4");	
	
	private static final Port usPort = LocalEV3.get().getPort("S1");
	
	
	
	//Initialization of odometer and navigation objects.
	public static EV3ColorSensor colorSensorF = new EV3ColorSensor(colorPortF);
	public static EV3ColorSensor colorSensorL = new EV3ColorSensor(colorPortL);
	public static EV3ColorSensor colorSensorR = new EV3ColorSensor(colorPortR);
	public static Odometer odometer = new Odometer(leftMotor, rightMotor);
	public static Navigation navigation = new Navigation(odometer,colorSensorL,colorSensorR);
	public static Correction correction;

	//	public static ballLauncher launch = new ballLauncher(launcherMotor,odometer,navigation);
	BangBangController bangbang = new BangBangController(leftMotor, rightMotor,
			bandCenter, bandWidth, motorLow, motorHigh);

	/*
	 * We use System.out.println() instead of LCD printing so that full debug
	 * output (e.g. the very long string containing the transmission) can be
	 * read on the screen OR a remote console such as the EV3Control program via
	 * Bluetooth or WiFi
	 * 
	 * 
	 * 					****
	 *** INSTRUCTIONS ***
	 ****
	 * There are two variables each team MUST set manually below:
	 *  
	 * 1. SERVER_IP: the IP address of the computer running the server
	 * application. This will be your own laptop, until the beta beta demo or
	 * competition where this is the TA or professor's laptop. In that case, set
	 * the IP to 192.168.2.3. 
	 * 
	 * 2. TEAM_NUMBER: your project team number
	 */
	private static final String SERVER_IP = "192.168.2.13";
	private static final int TEAM_NUMBER = 3;

	// Enable/disable printing of debug info from the WiFi class
	private static final boolean ENABLE_DEBUG_WIFI_PRINT = true;

	@SuppressWarnings("rawtypes")
	public static void main(String[] args) {

		System.out.println("Running..");

		//Setup color sensor
		// 1. Create a port object attached to a physical port (done above)
		// 2. Create a sensor instance and attach to port
		// 3. Create a sample provider instance for the above and initialize operating mode
		// 4. Create a buffer for the sensor data
		@SuppressWarnings("resource")

		SampleProvider colorValueF = colorSensorF.getMode("Red");			// colorValue provides samples from this instance
		SampleProvider colorValueR = colorSensorR.getMode("Red");
		SampleProvider colorValueL = colorSensorL.getMode("Red");
		
		float[] colorData = new float[100];			// colorData is the buffer in which data are returned
		float[] colorData2 = new float[100];

		//Setup ultrasonic sensor
		// 1. Create a port object attached to a physical port (done above)
		// 2. Create a sensor instance and attach to port
		// 3. Create a sample provider instance for the above and initialize operating mode
		// 4. Create a buffer for the sensor data
		@SuppressWarnings("resource")							    	// Because we don't bother to close this resource
		SensorModes usSensor = new EV3UltrasonicSensor(usPort);
		SampleProvider usValue = usSensor.getMode("Distance");			// colorValue provides samples from this instance
		float[] usData = new float[usValue.sampleSize()];				// colorData is the buffer in which data are returned
		
		// Initialize WifiConnection class
		WifiConnection conn = new WifiConnection(SERVER_IP, TEAM_NUMBER, ENABLE_DEBUG_WIFI_PRINT);
		Sound.beep();
		// Connect to server and get the data, catching any errors that might occur
		try {
			/*
			 * getData() will connect to the server and wait until the user/TA
			 * presses the "Start" button in the GUI on their laptop with the
			 * data filled in. Once it's waiting, you can kill it by
			 * pressing the upper left hand corner button (back/escape) on the EV3.
			 * getData() will throw exceptions if it can't connect to the server
			 * (e.g. wrong IP address, server not running on laptop, not connected
			 * to WiFi router, etc.). It will also throw an exception if it connects 
			 * but receives corrupted data or a message from the server saying something 
			 * went wrong. For example, if TEAM_NUMBER is set to 1 above but the server expects
			 * teams 17 and 5, this robot will receive a message saying an invalid team number 
			 * was specified and getData() will throw an exception letting you know.
			 */
			Map data = conn.getData();

			// Example 1: Print out all received data
//			System.out.println("Map:\n" + data);

			// Example 2 : Print out specific values
			int fwdTeam = ((Long) data.get("FWD_TEAM")).intValue();
//			System.out.println("Forward Team: " + fwdTeam);

			int defTeam = ((Long) data.get("DEF_TEAM")).intValue();
//			System.out.println("Defense Team: " + defTeam);

			int fwdCorner = ((Long) data.get("FWD_CORNER")).intValue();
//			System.out.println("Forward Start Corner: " + fwdCorner);

			int defCorner = ((Long) data.get("DEF_CORNER")).intValue();
//			System.out.println("Defense Start Corner: " + defCorner);

			int w1 = ((Long) data.get("w1")).intValue();
			int w2 = ((Long) data.get("w2")).intValue();
//			System.out.println("Defender zone dimensions (w1,w2): (" + w1 + ", " + w2 +")");

			int d1 = ((Long) data.get("d1")).intValue();
//			System.out.println("Forward line position d1: " + d1);

			int bx = ((Long) data.get("bx")).intValue();
			int by = ((Long) data.get("by")).intValue();
//			System.out.println("Ball dispenser position (bx,by): (" + bx + ", " + by +")");


			// Example 3: Compare value
			String orientation = (String) data.get("omega");
//			if (orientation.equals("N")) {
//				System.out.println("Orientation is North");
//			}
//			else {
//				System.out.println("Orientation is not North");
//			}

			

			Localization lsl = new Localization(odometer,navigation, colorValueF, colorData, 
					colorData2, leftMotor,rightMotor, usValue, usSensor, usData);
			final TextLCD t = LocalEV3.get().getTextLCD();
			t.clear();
			OdometryDisplay odometryDisplay = new OdometryDisplay(odometer,t);
			//pass all these values to start the game:
			if(fwdTeam == 3){ //play forward:
				System.out.println();
				System.out.println();
				System.out.println();
				System.out.println();
				System.out.println();
				System.out.println();
				System.out.println();
				System.out.println();
				
				odometer.start();
				navigation.start();
				odometryDisplay.start();
				lsl.doLocalization(fwdCorner);
				Sound.beep();
				Launcher.Enter_Launch_Position(); //PULLS ARM DOWN
//				Button.waitForAnyPress();
				
				correction = new Correction(odometer, navigation, colorValueR, colorValueL, colorValueF, leftMotor, rightMotor);
				correction.start();
				navigation.travelTo(304.8, 60.96);
				
//				t.drawString(Double.toString(finalProject.Localization.deltaTheta), 0, 2);
//				t.drawString(Double.toString(odometer.theta), 0, 3);
//				t.drawString(Double.toString(finalProject.Localization.angleA), 0, 4);
//				t.drawString(Double.toString(finalProject.Localization.angleB), 0, 5);
//				t.drawString(Double.toString(finalProject.Localization.XTheta_Plus), 0, 6);
//				t.drawString(Double.toString(finalProject.Localization.XTheta_Minus), 0, 7);
//				Forward forward = new Forward(navigation, fwdCorner, d1, w1, w2, bx, by, orientation);
//				forward.startFWD(); 
				
			}
			if(defTeam == 3){//play defense:
				lsl.doLocalization(defCorner);
				Defense defense = new Defense(defCorner, w1, w2);
				defense.startDEF();
			}
		}
		catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}

		// Wait until user decides to end program
		Button.waitForAnyPress();
	}
}