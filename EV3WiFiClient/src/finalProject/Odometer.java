
package finalProject;

import lejos.hardware.motor.EV3LargeRegulatedMotor;
/**
 * 
 * The odometer is the class that is used to continuously 
 * update the position of the robot based on the movement 
 * across the field of play. Whenever the robot moves the 
 * odometer updates to reflect that change in position and 
 * keep a tally of the current position in relation to the 
 * initial position (which after initial localization should 
 * be set to the initial position).
 * 
 * @author Ian Gauthier
 * @author Ilana Haddad
 * @author Tristan Bouchard
 * @author Tyrone Wong
 * @author Alexandre Tessier
 * 
 * @version 1.0
 *
 */
public class Odometer extends Thread {

	private double x, y; // robot position
	double theta;
	private int leftMotorTachoCount, rightMotorTachoCount;
	private EV3LargeRegulatedMotor leftMotor = WiFiExample.leftMotor;
	private EV3LargeRegulatedMotor rightMotor = WiFiExample.rightMotor;
	private static final long ODOMETER_PERIOD = 25;// odometer update period, in ms
	private static final double WHEEL_RADIUS = WiFiExample.WHEEL_RADIUS;
	private static final double TRACK = WiFiExample.TRACK; //Tried: 8.8, 9.3 ,9.5, 9.8, 10.1, 10.3, 10.4, 10.5

	// lock object for mutual exclusion
	private Object lock;


	/**
	 * Odometer Constructor
	 * @param leftMotor the robot's left wheel motor
	 * @param rightMotor the robot's right wheel motor
	 */
	public Odometer(EV3LargeRegulatedMotor leftMotor,EV3LargeRegulatedMotor rightMotor) {
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		this.x = 0.0;
		this.y = 0.0;
		this.theta = 0.0; //Angle of the cart from starting point.
		this.leftMotorTachoCount = 0;//leftMotor.getTachoCount();
		this.rightMotorTachoCount = 0;//rightMotor.getTachoCount();
		lock = new Object();
	}

	// run method (required for Thread)
	/**
	 * The run method should be performed continuously 
	 * and intake the changes in position of the wheels 
	 * and use those to update the current value of the 
	 * position based on the changes in tacho count.
	 */
	public void run() {
		long updateStart, updateEnd;

		//	leftMotor.resetTachoCount(); //Set the tacho count to zero when thread starts
		//	rightMotor.resetTachoCount();

		int l_lastTacho = leftMotor.getTachoCount(); //Get initial motor tacho count for both left and right
		int r_lastTacho = rightMotor.getTachoCount();

		while (true) {
			updateStart = System.currentTimeMillis();
		

			double d_left, d_right, delta_d, displacement, delta_x, delta_y, theta_h; //Initializing variables

			int temp_left = leftMotor.getTachoCount(); //Create temporary variable to hold current tacho count
			int temp_right = rightMotor.getTachoCount();

			leftMotorTachoCount = (temp_left - l_lastTacho); //Make tacho count equal to current - last
			rightMotorTachoCount = (temp_right - r_lastTacho );
			l_lastTacho = temp_left; //Update last tacho count
			r_lastTacho = temp_right;


			d_left = (WHEEL_RADIUS * Math.PI * leftMotorTachoCount)/180.0; //Set the distance travelled by the wheel according to the formula
			d_right = (WHEEL_RADIUS * Math.PI * rightMotorTachoCount)/180.0;
			delta_d = (d_left - d_right); //Calculate the difference in distance travelled between the two wheels
			theta_h = (delta_d / TRACK) *(360/(2*Math.PI)); //Approximation in radians of sin(theta), changed to degrees.
			displacement = ((d_left + d_right) / 2.0); //Calculate the displacement of the cart

			delta_x = displacement * Math.sin(theta*((2*Math.PI)/360)); //Calculate how much the value of x should be incremented by, depending on the theta of cart
			delta_y = displacement * Math.cos(theta*((2*Math.PI)/360)); //Calculate how much the value of y should be incremented by, depending on the theta of cart



			synchronized (lock) {
				/**
				 * Don't use the variables x, y, or theta anywhere but here!
				 * Only update the values of x, y, and theta in this block. 
				 * Do not perform complex math
				 * 
				 */

				theta = theta + theta_h; //Calculating value of angle of cart

				this.x = this.x + delta_x; // current x position
				this.y = this.y + delta_y; // current y position
				if(theta > 360){ //Code to ensure that the values of theta go from [0,360] inclusively, and don't go negative/ over 360
					theta = theta%360;
				}else if(theta < 0){
					theta = 360-theta;
				}
			}

			// this ensures that the odometer only runs once every period
			updateEnd = System.currentTimeMillis();
			if (updateEnd - updateStart < ODOMETER_PERIOD) {
				try {
					Thread.sleep(ODOMETER_PERIOD - (updateEnd - updateStart));
				} catch (InterruptedException e) {
					// there is nothing to be done here because it is not
					// expected that the odometer will be interrupted by
					// another thread
				}
			}
		}
	}

	// accessors
	/**
	 * The method is called and based on which of the values of x pos, y pos and angle
	 * are asked for, those should be returned in the given array. 
	 * 
	 * @param position an array into which the positions of the robot can be input
	 * @param update an array of booleans to say which of the positions should be returned
	 */
	public void getPosition(double[] position, boolean[] update) {
		// ensure that the values don't change while the odometer is running
		synchronized (lock) {
			if (update[0])
				position[0] = x;
			if (update[1])
				position[1] = y;
			if (update[2])
				position[2] = theta;
		}
	}


	/**
	 * Getter for X position
	 * @return the robot's X position
	 */
	public double getX() {
		double result;

		synchronized (lock) {
			result = x;
		}

		return result;
	}
	/**
	 * Getter for Y position
	 * @return the robot's Y position
	 */
	public double getY() {
		double result;

		synchronized (lock) {
			result = y;
		}

		return result;
	}
	
	/**
	 * Getter method for the current angle of the robot
	 * @return the position of the angle
	 */
	public double getAng() {
		double result;

		synchronized (lock) {
			result = theta;
		}

		return result;
	}

	// mutators
	/**
	 * The method should intake an array of values for the position of the robot and a 
	 * second array that says which of the values should be updated. It then updates
	 * the selected values to be equal to the input values.
	 * 
	 * @param position the robot's x, y and theta positions
	 * @param update an array of booleans that are true for each of the parameters to be change and false for all others
	 */
	public void setPosition(double[] position, boolean[] update) {
		// ensure that the values don't change while the odometer is running
		synchronized (lock) {
			if (update[0])
				x = position[0];
			if (update[1])
				y = position[1];
			if (update[2])
				theta = position[2];
		}
	}
	/**
	 * Updates the X position of the robot in the odometer.
	 * @param x the new X position
	 */
	public void setX(double x) {
		synchronized (lock) {
			this.x = x;
		}
	}
	/**
	 * Updates the Y position of the robot in the odometer.
	 * @param y the new Y position
	 */
	public void setY(double y) {
		synchronized (lock) {
			this.y = y;
		}
	}
	/**
	 * Updates the angle value of the robot in the odometer.
	 * @param theta the new angle value of the robot
	 */
	public void setAng(double theta) {
		synchronized (lock) {
			this.theta = theta;
		}
	}

	/**
	 * Return the value of the amount of degrees which the left motor has 
	 * rotated.
	 * @return the leftMotorTachoCount
	 */
	public int getLeftMotorTachoCount() {
		return leftMotorTachoCount;
	}

	/**
	 * Sets the amount of degrees which the left motor has rotated.
	 * @param leftMotorTachoCount the leftMotorTachoCount to set
	 */
	public void setLeftMotorTachoCount(int leftMotorTachoCount) {
		synchronized (lock) {
			this.leftMotorTachoCount = leftMotorTachoCount;	
		}
	}

	/**
	 * Return the value of the amount of degrees which the right motor has 
	 * rotated.
	 * @return the rightMotorTachoCount
	 */
	public int getRightMotorTachoCount() {
		return rightMotorTachoCount;
	}

	/**
	 * Sets the amount of degrees which the right motor has rotated.
	 * @param rightMotorTachoCount the rightMotorTachoCount to set
	 */
	public void setRightMotorTachoCount(int rightMotorTachoCount) {
		synchronized (lock) {
			this.rightMotorTachoCount = rightMotorTachoCount;	
		}
	}
}