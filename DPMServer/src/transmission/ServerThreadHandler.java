package transmission;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import gui.MainWindow;

/**
 * Handles server creation and passing of data from the GUI to all clients
 * @author Michael Smith
 *
 */
public class ServerThreadHandler implements Runnable {

	// How long to wait (in seconds) for a thread to finish
	private static final int THREAD_TIMEOUT = 5;

	// Server port number
	private int portNumber;

	// Main GUI window, used for printing info
	private MainWindow mw;

	// Used to keep track of running threads and status of each
	private LinkedBlockingQueue<ServerThread> serverThreads;
	private LinkedBlockingQueue<Future<Integer>> results;

	// Lock on accessing queues to prevent a mismatch between threads and results
	private Object lock;

	public ServerThreadHandler(int portNumber, MainWindow mw) {
		this.mw = mw;
		this.portNumber = portNumber;
		this.serverThreads = new LinkedBlockingQueue<>();
		this.results = new LinkedBlockingQueue<>();
		this.lock = new Object();
	}

	@Override
	public void run() {
		boolean listening = true;

		// Create server on specified port
		try (ServerSocket serverSocket = new ServerSocket(portNumber)) {

			ExecutorService pool = Executors.newCachedThreadPool();

			System.out.println("Server running on port " + portNumber);

			// Listen for incoming connections, spawn new threads to handle them
			// as they arrive
			while (listening) {
				ServerThread th = new ServerThread(serverSocket.accept(), this.mw);

				// Keep track of each connection (thread & status) so that we
				// can send
				// the data to each robot. At the moment the ServerThread is
				// created,
				// the data to send is not yet available from the GUI
				synchronized (this.lock) {
					serverThreads.add(th);
					results.add(pool.submit(th));
				}

			}
		} catch (IOException e) {
			System.err.println("Could not listen on port " + portNumber);
			System.exit(-1);
		}
	}

	@SuppressWarnings("rawtypes")
	// Passes data from GUI to appropriate threads and verifies data sent to
	// appropriate teams successfully
	public boolean transmit(ArrayList<Integer> teams, Map data) throws InterruptedException {

		// Error checks
		if (results.size() != serverThreads.size()) {
			System.out.println("Error: mismatch between number of connections and respective status flags");
			System.out.println(
					"Number of threads: " + serverThreads.size() + "\nNumber of status flags: " + results.size());
			this.mw.displayOutput("Warning: error in connection handling", true);
		}

		// Display useful info for user
		if (serverThreads.size() == 0) {
			this.mw.displayOutput("No active connections - nothing to transmit to!", false);
		} else {
			this.mw.displayOutput("Transmitting to " + serverThreads.size() + " connection(s).", false);
		}

		// Track successful transmissions to different teams
		ArrayList<Integer> successfulTransmissions = new ArrayList<>();

		synchronized (this.lock) {
			// Go through all connections and give them the data to send to each team
			while (!serverThreads.isEmpty()) {
				ServerThread t = serverThreads.take();
				t.send(teams, data);
			}

			// Now that all data has been sent, check to see what happened with each team
			while (!results.isEmpty()) {
				try {
					Future<Integer> task = results.take();
					try {
						Integer res = task.get(THREAD_TIMEOUT, TimeUnit.SECONDS);
						if (res != ServerThread.COMM_ERROR) {
							// Team # res was OK, add it to list
							successfulTransmissions.add(res);
						}
					} catch (TimeoutException e) {
						System.err.println("Terminating deadlocked connection thread");
						task.cancel(true);
					}

				} catch (ExecutionException e) {
					System.err.println("Exception occured in connection:\n");
					e.printStackTrace();
				}
			}
		}

		// Go through list of teams we were supposed to send data to and see if we actually did
		boolean success = true;
		for (Integer i : teams) {
			if (successfulTransmissions.contains(i)) {
				this.mw.displayOutput("Successfully transmitted to team " + i, false);
			} else {
				this.mw.displayOutput("Failed to transmit to team " + i, false);
				success = false;
			}
		}

		return success;
	}

}
