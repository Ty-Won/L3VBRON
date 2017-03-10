
/*
 * Main entry point for final project server.
 * @author Michael Smith
 * @date November 27, 2016
 * @class ECSE 211 - Design Principles and Methods
 */
import gui.MainWindow;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import static java.util.Arrays.asList;

import java.io.IOException;

public class FinalProject {
	// simply start the main window
	public static void main(String[] args) {

		// Define allowed options, associated default values, types and
		// descriptions
		OptionParser parser = new OptionParser();
		OptionSpec<String> layout = parser
				.acceptsAll(asList("layout", "l"),
						"Path to XML file specifying parameters to be used in the competition.")
				.withOptionalArg().ofType(String.class).defaultsTo("layout.xml");
		parser.acceptsAll(asList("help", "h", "?"), "This help text").forHelp();
		OptionSpec<Integer> portNumber = parser.acceptsAll(asList("port", "p"), "Port number to run server on").withOptionalArg().ofType(Integer.class).defaultsTo(49287);

		// Parse options
		try {
			try {
				OptionSet options = parser.parse(args);

				// Print help and exit if requested
				if (options.has("help")) {
					parser.printHelpOn(System.out);
				} else {
					// Get XML filename, port, create GUI
					String filename = options.valueOf(layout);
					Integer port = options.valueOf(portNumber);
					if (!options.has(layout)) {
						System.out.println("No layout specified; assuming default of " + filename);
					}
					if (!options.has(portNumber)) {
						System.out.println("No port number specified; assuming default of " + port);
					}
					new MainWindow(filename, port);
				}
			} catch (OptionException e) {
				System.out.println(e.getLocalizedMessage());
				parser.printHelpOn(System.out);
			}
		}
		// Handle possible exception from printing help text to System.out
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}
