
package wifi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Map;

import lejos.hardware.Button;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

/**
 * Handles all aspects of communicating with a server (TA's computer) to get
 * competition data as specified by the professors.  Works over any connection method
 * supported by the EV3 and the included Linux OS, including Bluetooth, USB and WiFi.
 * 
 * @author Michael Smith
 *
 */
public class WifiConnection {

	// Port number of server to connect to
	private static final int portNumber = 49287;
	// Disable timeout on waiting for response from server because a team may
	// have to wait a few minutes for
	// another team to get the robot working in the competition
	private static final int timeout = 0;

	// Whether or not to print debug messages
	private boolean debugPrint;
	// Team number to transmit to server
	private int teamNumber;
	// Server IP address
	private String serverIP;

	/**
	 * Constructor; requires server IP address, team number and a boolean
	 * specifying whether debug information should be printed. Does not actually
	 * connect to the server.
	 * 
	 * @param serverIP
	 * @param teamNumber
	 * @param debugPrint
	 */
	public WifiConnection(String serverIP, int teamNumber, boolean debugPrint) {

		this.serverIP = serverIP;
		this.teamNumber = teamNumber;
		this.debugPrint = debugPrint;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	/**
	 * Connects to the server and downloads information as specified in the GUI.
	 * Will block until data is received but can be stopped by pressing the 
	 * back (escape) button. Throws various Exceptions if errors occur such as not 
	 * being able to connect to the server.
	 * 
	 * @return A Map containing all information specified by the professors.
	 * @throws IOException
	 *             If there is an error communicating with the server.
	 * @throws UnknownHostException
	 *             If the server cannot be found.
	 * @throws ParseException
	 *             If an error occurs in parsing data from the server.
	 * @see java.util.Map
	 */
	public Map getData() throws IOException, UnknownHostException, ParseException {

		if (this.debugPrint)
			System.out.println("Connecting...");

		// Connect to server, set timeout
		Socket conn = new Socket(serverIP, portNumber);
		conn.setSoTimeout(timeout);

		// Create a thread to monitor the escape (back) button in case user
		// wants to exit
		Thread buttonChk = new Thread(new exitButtonChecker(this.debugPrint, conn));
		buttonChk.start();

		BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		PrintWriter writer = new PrintWriter(conn.getOutputStream(), true);

		if (this.debugPrint)
			System.out.println("Connected. Sending request.");

		// Create data to send to server
		JSONObject obj = new JSONObject();
		obj.put("Type", "REQ");
		obj.put("Team Number", new Integer(this.teamNumber));

		// Send data to server
		writer.println(obj.toJSONString());

		if (this.debugPrint)
			System.out.println("Request sent; waiting for response");

		// Wait for and read response from server
		String response = reader.readLine();

		// Process response from server
		JSONObject rJSONObject = (JSONObject) JSONValue.parse(response);

		if (!rJSONObject.containsKey("Type") || !rJSONObject.get("Type").equals("RESP")
				|| !rJSONObject.containsKey("Status")) {
			throw new IOException("Corrupted data received");
		} else if (rJSONObject.containsKey("Status") && !rJSONObject.get("Status").equals("OK")) {
			throw new IOException("Bad server status: " + rJSONObject.get("Status"));
		} else if (this.debugPrint) {
			System.out.println("Response received OK.");
		}

		// Remove type and status as they are not needed by the user
		rJSONObject.remove("Type");
		rJSONObject.remove("Status");

		// Clean up: close connection, terminate button watch thread, return
		// data to caller
		conn.close();

		buttonChk.interrupt();

		return rJSONObject;
	}

	/**
	 * Watches the back (escape) button and closes the connection when pressed.
	 * 
	 * @author Michael Smith
	 *
	 */
	private class exitButtonChecker implements Runnable {

		private boolean debugPrint;
		private Socket connection;

		public exitButtonChecker(boolean debugPrint, Socket connection) {
			this.connection = connection;
			this.debugPrint = debugPrint;
		}

		@Override
		public void run() {
			if (Button.waitForAnyEvent() == Button.ID_ESCAPE) {
				try {
					this.connection.close();
				} catch (IOException e) {
					System.err.println("Error when closing connection: " + e.getMessage());
				}
				if (this.debugPrint)
					System.out.println("Closing connection...");
			}
		}

	}

}
