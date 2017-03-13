package finalProject;

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
	public static double WHEEL_RADIUS = WiFiExample.WHEEL_RADIUS;
	public static final double TRACK = WiFiExample.TRACK;
	private static final int FORWARD_SPEED = 250;
	private static final int ROTATE_SPEED = 100;
	public double odo_x,odo_y, odo_theta;
	public double x_dest, y_dest, theta_dest;

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

	// TravelTo method receives a destination specified by x2 and y2 and sets the robot to travel to that destination
	public void travelTo (double x2, double y2){ //ALEX'S
		
		//Get the robot's current position from the odometer
		double[] position = new double[3];
		odometer.getPosition(position);
		double x1 = position[0];
		double y1 = position[1];
		double currentTheta = position[2];
		
		//Calculate the data required for the robot to travel to specific coordinates
		double deltaX = x1-x2;
		double deltaY = y1-y2;
		double turnTheta = calculateTheta(x1, x2, y1, y2,currentTheta);				//Calculate the angle the robot needs to turn to
		double travelDist = Math.sqrt(Math.pow(deltaX,2)+Math.pow(deltaY,2));		//Shortest distance to travel calculated using Pythagorean theorem
	

		// Turn at the calculated angle and then travel in the direction of the coordinates

		turnBy(turnTheta);
		goForward(travelDist);

	}

	// Method used to calculate the angle at which the robot needs to turn in order to drive to given coordinates
	// To do so, it must be given the robot's current position (x1, y1, theta) and the robot's desired final position (x2, y2)
	public double calculateTheta (double x1, double x2, double y1, double y2, double currentTheta){
	
		double newTheta=0;
		double deltaTheta;
		double displacementX = x2-x1;
		double displacementY = y2-y1;
		
		//Calculates the angle the robot needs to drive at based on basic trigonometry
		if(displacementX>0 && displacementY>0)								//Needs to drive in the first quadrant 
			newTheta = Math.atan(displacementX/displacementY);
		
		else if(displacementX>0 && displacementY<0)							//Needs to drive in the fourth quadrant
			newTheta = Math.atan(displacementX/displacementY)+Math.PI;
		
		else if (displacementX<0 && displacementY>0)						//Needs to drive in the second quadrant
			newTheta = Math.atan(displacementX/displacementY);		
		
		else if (displacementX<0 && displacementY<0)						//Needs to drive in the third quadrant
			newTheta = Math.atan(displacementX/displacementY)-Math.PI;
			
		deltaTheta = newTheta-currentTheta;
		
		//Make sure the angle calculated is the smallest angle possible
		if(deltaTheta>=-Math.PI && deltaTheta<=Math.PI)						//If the angle is between -pi and pi, it is the smallest angle				
			return deltaTheta;
		
		else if (deltaTheta<-Math.PI)										//If the angle is smaller than -pi, add 2pi to make it the smallest angle possible
			return deltaTheta+(2*Math.PI);
		
		else if (deltaTheta>Math.PI)										//If the angle is bigger than pi, substract 2pi to make it the smallest angle possible
			return deltaTheta-(2*Math.PI);
		else
			return 0;
	}
	/*
	 * TurnTo function which takes an angle and boolean as arguments The boolean controls whether or not to stop the
	 * motors when the turn is completed
	 */
	public void turnTo(double angle, boolean stop) {

		double error = angle - this.odometer.getAng();
		turnBy(-error);

//		while (Math.abs(error) > DEG_ERR) {
//
//			error = angle - this.odometer.getAng();
//
//			if (error < -180.0) {
//				this.setSpeeds(-SLOW, SLOW);
//			} else if (error < 0.0) {
//				this.setSpeeds(SLOW, -SLOW);
//			} else if (error > 180.0) {
//				this.setSpeeds(SLOW, -SLOW);
//			} else {
//				this.setSpeeds(-SLOW, SLOW);
//			}
//		}

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
