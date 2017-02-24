import java.util.Map;

import wifi.WifiConnection;
import lejos.hardware.Button;

/**
 * Example class using WifiConnection to communicate with a server and receive
 * data concerning the competition such as the starting corner the robot is
 * placed in.
 * 
 * @author Michael Smith
 *
 */
public class WiFiExample {
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
	private static final int TEAM_NUMBER = 1;

	// Enable/disable printing of debug info from the WiFi class
	private static final boolean ENABLE_DEBUG_WIFI_PRINT = true;

	@SuppressWarnings("rawtypes")
	public static void main(String[] args) {

		System.out.println("Running..");

		// Initialize WifiConnection class
		WifiConnection conn = new WifiConnection(SERVER_IP, TEAM_NUMBER, ENABLE_DEBUG_WIFI_PRINT);

		// Connect to server and get the data, catching any errors that might
		// occur
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

			int w1 = ((Long) data.get("w1")).intValue();
			System.out.println("Defender zone size w1: " + w1);
			
			// Example 3: Compare value
			String orientation = (String) data.get("omega");
			if (orientation.equals("N")) {
				System.out.println("Orientation is North");
			}
			else {
				System.out.println("Orientation is not North");
			}

		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}

		// Wait until user decides to end program
		Button.waitForAnyPress();
	}
}
