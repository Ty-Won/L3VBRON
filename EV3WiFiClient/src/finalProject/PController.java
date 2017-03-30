package finalProject;

import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.EV3MediumRegulatedMotor;

public class PController extends Thread{
	private final int motorStraight = 300, FILTER_OUT = 20;
	private EV3LargeRegulatedMotor leftMotor, rightMotor;
	private EV3MediumRegulatedMotor usMotor;
	private int filterControl, motorShift;
	int FilteredDistance;
	private int distance;
	public boolean avoidingOb;
	
	public PController(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor, EV3MediumRegulatedMotor usMotor) {
		//Default Constructor
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		this.usMotor = usMotor;
		//leftMotor.setSpeed(motorStraight);					// Initalize motor rolling forward
		//rightMotor.setSpeed(motorStraight);
		//leftMotor.forward();
		//rightMotor.forward();
		filterControl = 0;
		motorShift = 0;
	}
	public void run()
	{
		int x = 255;
		while(true)
		{
			x = readUSDistance();
			avoidingOb = processUSData(x);
		}
	}
	
	public boolean processUSData(int distance) {

		if (distance >= 255 && filterControl < FILTER_OUT) {
			// bad value, do not set the distance var, however do increment the
			// filter value
			filterControl++;
		} else if (distance >= 255) {
			// We have repeated large values, so there must actually be nothing
			// there: leave the distance alone
			this.distance = distance;
		} else {
			// distance went below 255: reset filter and leave
			// distance alone.
			filterControl = 0;
			this.distance = distance;
		}
		
		if(motorShift > 9 && this.distance > 50)
		{
			if(usMotor.getTachoCount() < 0)
			{
				usMotor.rotate(50);
				motorShift = 0;
				return false;
			}
			else
			{
				usMotor.rotate(-50);
				motorShift = 0;
				return false;
			}
		}
		else if(this.distance > 50)
		{
			motorShift++;
			return false;
		}
		else
		{
			return true;
		}
		
		
		
//		if(distance == 21474)
//			// sensor "error" value was passed, replace this value by the set bandCenter 
//			// so the robot keeps moving in a straight line
//			FilteredDistance=bandCenter;
//		else if (distance >= bandCenter+40 && filterControl < FILTER_OUT) {
//			// abnormal value (in theory, the robot shoudln't deviate that much from the wall)
//			//do not set the distance var, however do increment the filter value
//			filterControl++;
//		} else if (distance >= bandCenter+40 && filterControl > FILTER_OUT) {
//			// We have repeated large values, so there must actually be nothing
//			// there: leave the distance alone
//			FilteredDistance = distance;
//		}	
//		 else {
//			// distance went below 255: reset filter and leave
//			// distance alone.
//			filterControl = 0;
//			FilteredDistance = distance;
//		}
//
//		
//		int distError=FilteredDistance-bandCenter;
//		
//		// Function used to calculate by how much the speed of the robot should be increased or decreased, 
//		// depending on how far the robot is from the target distance from the wall
//		int deltaspeed=Math.abs(distError*15);
//		
//		// Sets a maximum increase or decrease in speed of 200
//		if (deltaspeed>200)
//			deltaspeed=200;
//		
//		// If the distance separating the robot and the wall is between a center margin of error (bandwidth), 
//		// then the robot keeps moving in a straight line
//		if(Math.abs(distError)<=bandwidth){
//			leftMotor.setSpeed(motorStraight);
//			rightMotor.setSpeed(motorStraight);
//			leftMotor.forward();
//			rightMotor.forward();
//		}
//		
//		//If the disterror is positive (meaning the robot is too far from the wall), then the robot must turn left
//		else if(distError>0){
//			leftMotor.setSpeed(motorStraight-deltaspeed);
//			rightMotor.setSpeed(motorStraight+deltaspeed);
//			leftMotor.forward();
//			rightMotor.forward();
//		}
//		
//		//If the disterror is negative (meaning the robot is too close to the wall), then the robot must turn right
//		else if(distError<0){
//			leftMotor.setSpeed(motorStraight+deltaspeed);
//			rightMotor.setSpeed(motorStraight-deltaspeed);
//			leftMotor.forward();
//			rightMotor.forward();
//		}
	}

	public void avoidOB()
	{
		int distance = 255;
		while(avoidingOb = true)
		{
			distance = readUSDistance();
			if(this.distance < 50 && usMotor.getTachoCount() < 0)
			{
				//System.out.println("Obstacle found left!");
				avoidingOb = true;
				rightMotor.setSpeed(distance*4);
				rightMotor.forward();
				motorShift = 0;
			}
			else if(this.distance < 50 && usMotor.getTachoCount() > 0)
			{
				//System.out.println("Obstacle found right!");
				avoidingOb = true;
				leftMotor.setSpeed(distance*4);
				leftMotor.forward();
				motorShift = 0;
			}
			else
			{
				avoidingOb = false;
			}
		}
		
	}
	public int readUSDistance() {
		return FilteredDistance;
	}

}
