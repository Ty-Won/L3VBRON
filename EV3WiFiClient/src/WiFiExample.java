

import java.util.Map;

import finalProject.Defense;
import finalProject.Forward;
import finalProject.Navigation;
import finalProject.Odometer;
import finalProject.ballLauncher;

import wifi.WifiConnection;
import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;

/**
 * Example class using WifiConnection to communicate with a server and receive
 * data concerning the competition such as the starting corner the robot is
 * placed in.
 * 
 * @author Michael Smith
 * @author Ilana Haddad
 * @version 2.0 
 * 
 *
 */
public class WiFiExample {
	public static final double WHEEL_RADIUS = 2.2;
	public static final double TRACK = 11.5; 
	public static final int TRAVERSE_SPEED = 100;

	// Left motor connected to output A
	// Right motor connected to output D
	// Ball Launcher Motor connected to output B
	public static final EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
	public static final EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
	public static final EV3LargeRegulatedMotor launcherMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("B"));

	//Initialization of odometer and navigation objects.
	public static Odometer odometer = new Odometer(leftMotor, rightMotor,30,true);
	public static Navigation navigation = new Navigation(odometer);

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
	private static final String SERVER_IP = "192.168.2.3";
	private static final int TEAM_NUMBER = 3;

	// Enable/disable printing of debug info from the WiFi class
	private static final boolean ENABLE_DEBUG_WIFI_PRINT = true;

	@SuppressWarnings("rawtypes")
	public static void main(String[] args) {

		System.out.println("Running..");

		// Initialize WifiConnection class
		WifiConnection conn = new WifiConnection(SERVER_IP, TEAM_NUMBER, ENABLE_DEBUG_WIFI_PRINT);
	
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
			System.out.println("Map:\n" + data);

			// Example 2 : Print out specific values
			int fwdTeam = ((Long) data.get("FWD_TEAM")).intValue();
			System.out.println("Forward Team: " + fwdTeam);
			
			int defTeam = ((Long) data.get("DEF_TEAM")).intValue();
			System.out.println("Defense Team: " + defTeam);
			
			int fwdCorner = ((Long) data.get("FWD_CORNER")).intValue();
			System.out.println("Forward Start Corner: " + fwdCorner);
			
			int defCorner = ((Long) data.get("DEF_CORNER")).intValue();
			System.out.println("Defense Start Corner: " + defCorner);
			
			int w1 = ((Long) data.get("w1")).intValue();
			int w2 = ((Long) data.get("w2")).intValue();
			System.out.println("Defender zone dimmensions (w1,w2): (" + w1 + ", " + w2 +")");
			
			int d1 = ((Long) data.get("d1")).intValue();
			System.out.println("Forward line position d1: " + d1);
			
			int bx = ((Long) data.get("bx")).intValue();
			int by = ((Long) data.get("by")).intValue();
			System.out.println("Ball dispenser position (bx,by): (" + bx + ", " + by +")");
			
			
			// Example 3: Compare value
			String orientation = (String) data.get("omega");
			if (orientation.equals("N")) {
				System.out.println("Orientation is North");
			}
			else {
				System.out.println("Orientation is not North");
			}
			
			//pass all these values to start the game:
			if(fwdTeam == 3){ //play forward:
				Forward forward = new Forward(fwdCorner, d1, w1, w2, bx, by, orientation);
				forward.startFWD();
			}
			
			if(defTeam == 3){//play defense:
				Defense defense = new Defense(defCorner, w1, w2);
				defense.startDEF();
			}

		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}

		// Wait until user decides to end program
		Button.waitForAnyPress();
	}
}
