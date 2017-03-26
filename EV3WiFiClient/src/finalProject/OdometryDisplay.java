
	/*
	 * OdometryDisplay.java
	 */

	package finalProject;

	import lejos.hardware.lcd.TextLCD;
	/**
	 * 
	 * The odometry display should display to the screen what 
	 * the current position of the robot is (based off of the 
	 * odometer at the current moment). The class should be called 
	 * continuously in order to output a current appraisal of the 
	 * position of the robot.
	 * 
	 * @author Ian Gauthier
	 * @author Ilana Haddad
	 * @author Tristan Bouchard
	 * @author Tyrone Wong
	 * @author Alexandre Tessier
	 * 
	 * @version 1.0
	 */
	public class OdometryDisplay extends Thread {
		private static final long DISPLAY_PERIOD = 250;
		private Odometer odometer;
		private TextLCD t;

		// constructor
		/**
		 * 
		 * @param odometer the odometer of the robot
		 * @param t the screen of the brick to be drawn on
		 */
		public OdometryDisplay(Odometer odometer, TextLCD t) {
			this.odometer = odometer;
			this.t = t;
		}

		// run method (required for Thread)
		/**
		 * The method should output the current value of the odometer at the specific moment in time
		 */
		public void run() {
			long displayStart, displayEnd;
			double[] position = new double[3];

			// clear the display once
			t.clear();

			while (true) {
				displayStart = System.currentTimeMillis();

				// clear the lines for displaying odometry information
				t.drawString("X:              ", 0, 0);
				t.drawString("Y:              ", 0, 1);
				t.drawString("T:              ", 0, 2);
//				t.drawString("US:"+ Lab3.sample[0]*100, 0, 3);

				// get the odometry information
				boolean[] update = {true,true,true};
				odometer.getPosition(position, update);

				// display odometry information
				for (int i = 0; i < 3; i++) {
					t.drawString(formattedDoubleToString(position[i], 2), 3, i);
				}

				// throttle the OdometryDisplay
				displayEnd = System.currentTimeMillis();
				if (displayEnd - displayStart < DISPLAY_PERIOD) {
					try {
						Thread.sleep(DISPLAY_PERIOD - (displayEnd - displayStart));
					} catch (InterruptedException e) {
						// there is nothing to be done here because it is not
						// expected that OdometryDisplay will be interrupted
						// by another thread
					}
				}
			}
		}
		
		/**
		 * The method should intake a double value and then format it so that is can be displayed on the screen.
		 * This entails placing a negative sign in front of the number if it is negative,
		 * placing a zero in front of the number if it is between -1 and 1 and 
		 * placing the decimal point in the right position for the given value.
		 * 
		 * 
		 * @param x The double value to be formatted for the screen
		 * @param places The place where the decimal point should be
		 * @return The newly formated double which can be displayed on the screen
		 */
		private static String formattedDoubleToString(double x, int places) {
			String result = "";
			String stack = "";
			long t;
			
			// put in a minus sign as needed
			if (x < 0.0)
				result += "-";
			
			// put in a leading 0
			if (-1.0 < x && x < 1.0)
				result += "0";
			else {
				t = (long)x;
				if (t < 0)
					t = -t;
				
				while (t > 0) {
					stack = Long.toString(t % 10) + stack;
					t /= 10;
				}
				
				result += stack;
			}
			
			// put the decimal, if needed
			if (places > 0) {
				result += ".";
			
				// put the appropriate number of decimals
				for (int i = 0; i < places; i++) {
					x = Math.abs(x);
					x = x - Math.floor(x);
					x *= 10.0;
					result += Long.toString((long)x);
				}
			}
			
			return result;
		}

}

