/*
 * File: Navigation.java
 * Written by: Sean Lawlor
 * ECSE 211 - Design Principles and Methods, Head TA
 * Fall 2011
 * Ported to EV3 by: Francois Ouellet Delorme
 * Fall 2015
 * 
 * Movement control class (turnTo, travelTo, flt, localize)
 */
import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class Navigation {
	final static int FAST = 200, SLOW = 100, ACCELERATION = 3000;
	final static double DEG_ERR = 1.5, CM_ERR = 3;
	private Odometer odometer;
	private EV3LargeRegulatedMotor leftMotor, rightMotor;
	public static double WHEEL_RADIUS = 2.1;
	public static final double TRACK = 14.2;
	private static final int FORWARD_SPEED = 250;
	private static final int ROTATE_SPEED = 50;

	public Navigation(Odometer odo) {
		this.odometer = odo;

		EV3LargeRegulatedMotor[] motors = this.odometer.getMotors();
		this.leftMotor = motors[0];
		this.rightMotor = motors[1];

		// set acceleration
		this.leftMotor.setAcceleration(ACCELERATION);
		this.rightMotor.setAcceleration(ACCELERATION);
	}

	/*
	 * Functions to set the motor speeds jointly
	 */
	public void setSpeeds(float lSpd, float rSpd) {
		this.leftMotor.setSpeed(lSpd);
		this.rightMotor.setSpeed(rSpd);
		if (lSpd < 0)
			this.leftMotor.backward();
		else
			this.leftMotor.forward();
		if (rSpd < 0)
			this.rightMotor.backward();
		else
			this.rightMotor.forward();
	}

	/*
	 * Float the two motors jointly
	 */
	public void setFloat() {
		this.leftMotor.stop();
		this.rightMotor.stop();
		this.leftMotor.flt(true);
		this.rightMotor.flt(true);
	}

	/*
	 * TravelTo function which takes as arguments the x and y position in cm Will travel to designated position, while
	 * constantly updating it's heading
	 */
	public void travelTo(double x2, double y2) {
		double minAng;
		while (Math.abs(x2 - odometer.getX()) > CM_ERR || Math.abs(y2 - odometer.getY()) > CM_ERR) {
			minAng = (Math.atan2(y2 - odometer.getY(), x2 - odometer.getX())) * (180.0 / Math.PI);
			if (minAng < 0)
				minAng += 360.0;
			
			double[] position = new double[3];
			odometer.getPosition(position);
			double x1 = position[0];
			double y1 = position[1];
			double currentTheta = position[2];
			
			//Calculate the data required for the robot to travel to specific coordinates
			double deltaX = x1-x2;
			double deltaY = y1-y2;
			double travelDist = Math.sqrt(Math.pow(deltaX,2)+Math.pow(deltaY,2));		//Shortest distance to travel calculated using Pythagorean theorem
		
			turnTo(minAng, false);
			goForward(travelDist);
		}
	}

	/*
	 * TurnTo function which takes an angle and boolean as arguments The boolean controls whether or not to stop the
	 * motors when the turn is completed
	 */
	public void turnTo(double angle, boolean stop) {

		double error = angle - this.odometer.getAng();

		while (Math.abs(error) > DEG_ERR) {

			error = angle - this.odometer.getAng();

			if (error < -180.0) {
				this.setSpeeds(-SLOW, SLOW);
			} else if (error < 0.0) {
				this.setSpeeds(SLOW, -SLOW);
			} else if (error > 180.0) {
				this.setSpeeds(SLOW, -SLOW);
			} else {
				this.setSpeeds(-SLOW, SLOW);
			}
		}

		if (stop) {
			this.setSpeeds(0, 0);
		}
	}
	
	// Turn by a given angle
	public void turnBy(double theta){
		leftMotor.setSpeed(ROTATE_SPEED);
		rightMotor.setSpeed(ROTATE_SPEED);
		leftMotor.rotate(convertAngle(WHEEL_RADIUS, TRACK, theta), true);
		rightMotor.rotate(-convertAngle(WHEEL_RADIUS, TRACK, theta), false);
		
	}
	
	
	// Go forward a set distance in cm
	public void goForward(double travelDist) {
		leftMotor.setSpeed(FORWARD_SPEED);
		rightMotor.setSpeed(FORWARD_SPEED);
		leftMotor.rotate(convertDistance(WHEEL_RADIUS, travelDist), true);
		rightMotor.rotate(convertDistance(WHEEL_RADIUS, travelDist), false);	
	}
	
	// Method given in the provided code for lab 2; converts a distance in cm to the number of rotation needed to be performed by the motor
	private static int convertDistance(double radius, double distance) {
		return (int) ((180.0 * distance) / (Math.PI * radius));
	}

	// Method given in the provided code for lab 2; converts a desired rotation (in degrees) to the number of rotation needed to be performed by the motor
	private static int convertAngle(double radius, double width, double angle) {
		return convertDistance(radius, Math.PI * width * angle / 360.0);
	}
	
}