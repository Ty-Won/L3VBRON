package wallFollower;

import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class PController implements UltrasonicController {
	
	private final int bandCenter, bandwidth;
	private final int motorStraight = 300, FILTER_OUT = 20;
	private EV3LargeRegulatedMotor leftMotor, rightMotor;
	private int filterControl;
	int FilteredDistance;
	
	public PController(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor,
					   int bandCenter, int bandwidth) {
		//Default Constructor
		this.bandCenter = bandCenter;
		this.bandwidth = bandwidth;
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		leftMotor.setSpeed(motorStraight);					// Initalize motor rolling forward
		rightMotor.setSpeed(motorStraight);
		leftMotor.forward();
		rightMotor.forward();
		filterControl = 0;
	}
	
	@Override
	public void processUSData(int distance) {

		if(distance == 21474)
			// sensor "error" value was passed, replace this value by the set bandCenter 
			// so the robot keeps moving in a straight line
			FilteredDistance=bandCenter;
		else if (distance >= bandCenter+40 && filterControl < FILTER_OUT) {
			// abnormal value (in theory, the robot shoudln't deviate that much from the wall)
			//do not set the distance var, however do increment the filter value
			filterControl++;
		} else if (distance >= bandCenter+40 && filterControl > FILTER_OUT) {
			// We have repeated large values, so there must actually be nothing
			// there: leave the distance alone
			FilteredDistance = distance;
		}	
		 else {
			// distance went below 255: reset filter and leave
			// distance alone.
			filterControl = 0;
			FilteredDistance = distance;
		}

		
		int distError=FilteredDistance-bandCenter;
		
		// Function used to calculate by how much the speed of the robot should be increased or decreased, 
		// depending on how far the robot is from the target distance from the wall
		int deltaspeed=Math.abs(distError*15);
		
		// Sets a maximum increase or decrease in speed of 200
		if (deltaspeed>200)
			deltaspeed=200;
		
		// If the distance separating the robot and the wall is between a center margin of error (bandwidth), 
		// then the robot keeps moving in a straight line
		if(Math.abs(distError)<=bandwidth){
			leftMotor.setSpeed(motorStraight);
			rightMotor.setSpeed(motorStraight);
			leftMotor.forward();
			rightMotor.forward();
		}
		
		//If the disterror is positive (meaning the robot is too far from the wall), then the robot must turn left
		else if(distError>0){
			leftMotor.setSpeed(motorStraight-deltaspeed);
			rightMotor.setSpeed(motorStraight+deltaspeed);
			leftMotor.forward();
			rightMotor.forward();
		}
		
		//If the disterror is negative (meaning the robot is too close to the wall), then the robot must turn right
		else if(distError<0){
			leftMotor.setSpeed(motorStraight+deltaspeed);
			rightMotor.setSpeed(motorStraight-deltaspeed);
			leftMotor.forward();
			rightMotor.forward();
		}
	}

	
	@Override
	public int readUSDistance() {
		return FilteredDistance;
	}

}
