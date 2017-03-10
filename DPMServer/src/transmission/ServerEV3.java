package transmission;

import java.util.ArrayList;
import java.util.Map;

import gui.MainWindow;

/*
 * Handles communication between this server and the EV3 robots
 * 
 * Basic flow:
 * - Server starts and listens on a specified port
 * - 1 thread is waiting for new connections (ServerThreadHandler, run() method)
 * - When a new connection occurs, a new thread is spawned to handle the request (ServerThread, call() method)
 * - The request specifies the team number, so we record it and print it to the screen to inform the user
 *   that a specific team has connected
 * - The call() method in ServerThread waits
 * - Pressing the "start" button calls the transmit() method in this class, which passes the call onto
 *   the ServerThreadHandler transmit() method
 * - When the "start" button in the GUI is pressed, each connection thread (ServerThreadHandler, run() method) 
 *   is woken up and data is sent to all teams with team numbers that match what is in the team number boxes
 * - Robots with team numbers not in the team number boxes get sent an error message informing them
 *   their team number is invalid
 */

public class ServerEV3 {

	private ServerThreadHandler serv;

	// Create thread to handle incoming requests
	public ServerEV3(int portNumber, MainWindow mw) {
		serv = new ServerThreadHandler(portNumber, mw);
		new Thread(serv).start();
	}

	// Pass transmit requests to server thread handler
	@SuppressWarnings("rawtypes")
	public boolean transmit(ArrayList<Integer> teams, Map data) throws InterruptedException {
		return serv.transmit(teams, data);
	}

}
