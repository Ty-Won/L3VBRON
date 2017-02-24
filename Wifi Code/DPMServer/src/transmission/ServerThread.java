package transmission;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import gui.MainWindow;

public class ServerThread implements Callable<Integer> {

	private int timeout;

	// Default timeout is 15 minutes in milliseconds
	private static final int defaultTimeout = 60000 * 15;

	// Integer returned instead of team number when an error occurs
	public static final int COMM_ERROR = -1;

	private Socket socket;

	private MainWindow mw;

	// Lock and condition to wait for user to press GUI "start" button
	private final Lock lock = new ReentrantLock();
	private final Condition waitForStart = lock.newCondition();

	@SuppressWarnings("rawtypes")
	// Data to be received from GUI
	private Map dataToSend;
	private ArrayList<Integer> expectedTeams;

	public ServerThread(Socket socket, int timeout, MainWindow mw) {
		this.mw = mw;
		this.socket = socket;
		this.timeout = timeout;
	}

	public ServerThread(Socket socket, MainWindow mw) {
		this(socket, defaultTimeout, mw);
	}

	@SuppressWarnings("rawtypes")
	// This function receives the data to be sent to the robots and informs
	// the thread waiting in call() it can go ahead and send the data along
	public void send(ArrayList<Integer> teams, Map data) {
		this.dataToSend = data;
		this.expectedTeams = teams;
		try {
			lock.lock();
			waitForStart.signal();
		} finally {
			lock.unlock();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Integer call() throws InterruptedException, IOException {

		this.socket.setSoTimeout(timeout);

		// Define input/output handlers. Note use of a PrintWriter as the data
		// is transmitted astext
		PrintWriter out = new PrintWriter(this.socket.getOutputStream(), true);
		BufferedReader in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));

		System.out.println("New request from: " + this.socket.getRemoteSocketAddress());

		// We send the data as JSON, with new lines indicating the end of a
		// transmission
		String inputLine = in.readLine();

		System.out.println("Read data:\n" + inputLine);

		// Parse the JSON
		Object obj = JSONValue.parse(inputLine);
		JSONObject decoded = (JSONObject) obj;

		JSONObject jsonToSend = new JSONObject();
		jsonToSend.put("Type", "RESP");

		// If the request is missing key parts, send them an error message and
		// indicate failure
		if (!decoded.containsKey("Type") || !decoded.containsKey("Team Number")) {
			jsonToSend.put("Status", "BAD REQUEST");

			sendToClient(jsonToSend, out);
			return COMM_ERROR;
		}

		// Decode the JSON now that we have confirmed necessary fields exist
		String typ = (String) decoded.get("Type");
		Integer teamNumber = ((Long) decoded.get("Team Number")).intValue();

		// If the data is not a request, send an error message and indicate
		// failure
		if (!typ.equals("REQ")) {
			jsonToSend.put("Status", "BAD REQUEST");
			sendToClient(jsonToSend, out);
			return COMM_ERROR;
		}

		this.mw.displayOutput("Team " + teamNumber + " connected.", false);

		// Wait for the user to press the GUI "start" button and provide the
		// data we need to send
		try {
			lock.lockInterruptibly();
			waitForStart.await();
		} finally {
			lock.unlock();
		}

		// If the team number transmitted by the robot is not one of the
		// competing teams, send an error message
		// and indicate failure
		if (!expectedTeams.contains(teamNumber)) {
			jsonToSend.put("Status", "TEAM NUMBER ERROR");
			sendToClient(jsonToSend, out);
			this.mw.displayOutput("Transmitted invalid team number message to team " + teamNumber, false);
			return COMM_ERROR;
		}
		// If the team number is OK, go ahead and transmit all data
		// We return the team number to indicate that data was successfully send
		// to this team
		else {
			jsonToSend.put("Status", "OK");
			jsonToSend.putAll(dataToSend);
			sendToClient(jsonToSend, out);
			System.out.println("Sent data to team " + teamNumber + ":\n" + jsonToSend.toJSONString());
			return teamNumber;
		}

	}

	// Small wrapper that takes care of sending data to the client and
	// subsequently closing the connection
	// Note that println() is used to append a new line, indicating end of
	// message
	private void sendToClient(JSONObject data, PrintWriter out) throws IOException {
		out.println(data.toJSONString());
		socket.close();
	}
}
