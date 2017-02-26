package wallFollower;

import java.util.concurrent.TimeUnit;

import lejos.hardware.motor.*;

public class BangBangController implements UltrasonicController{
	private final int bandCenter, bandwidth;
	private final int motorLow, motorHigh;
	private EV3LargeRegulatedMotor leftMotor, rightMotor;
	private int filterControl;
	private final int FILTER_OUT = 30;
	private int FilteredDistance;
	
	public BangBangController(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor,
							  int bandCenter, int bandwidth, int motorLow, int motorHigh) {
		//Default Constructor
		this.bandCenter = bandCenter;
		this.bandwidth = bandwidth;
		this.motorLow = motorLow;
		this.motorHigh = motorHigh;
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		leftMotor.setSpeed(motorHigh);				// Start robot moving forward
		rightMotor.setSpeed(motorHigh);
		leftMotor.forward();
		rightMotor.forward();
	}
	
	@Override
	public void processUSData(int distance) {
		
		//Basic filter implemented in order to reduce the amount of false positives/negatives
		
		if(distance == 21474){
			// sensor "error" value was passed, replace this value by the set bandCenter 
			// so the robot keeps moving in a straight line
			FilteredDistance=bandCenter;	
		} else if (distance >= bandCenter+40 && filterControl < FILTER_OUT) {
			// abnormal value (in theory, the robot shoudln't deviate that much from the wall)
			//do not set the distance var, however do increment the filter value
			filterControl++;
		} else if (distance >= bandCenter+40 && filterControl > FILTER_OUT) {
			// We have repeated large values, so there must actually be nothing
			// there: leave the distance alone
			FilteredDistance = distance;
		} else {
			// distance went below 255: reset filter and leave
			// distance alone.
			filterControl = 0;
			FilteredDistance = distance;
		}

		int distError=FilteredDistance-bandCenter;
		
		//  If the distance is more than 120, the robot must be going past a convex corner or past a gap
		if(FilteredDistance>=120){
			
			// In the event that the reading referred to a gap, the robot will keep going straight for a fixed amount of time (750 ms)
			leftMotor.setSpeed(motorHigh);
			rightMotor.setSpeed(motorHigh);
			leftMotor.forward();
			rightMotor.forward();
			
				try {
					TimeUnit.MILLISECONDS.sleep(750);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			
				//If the distance is still more that 120, it means that the robot needs to turn in order to go around the convex corner for a certain amount of time (1000ms)
				if(FilteredDistance>=120) {
					
						leftMotor.setSpeed(motorLow);
						rightMotor.setSpeed(motorHigh);
						leftMotor.forward();
						rightMotor.forward();
						
				
				try {
					TimeUnit.MILLISECONDS.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				}	
		}
		
		// If the distance separating the robot and the wall is between a center margin of error (bandwidth),
		// then the robot keeps moving in a straight line
		if(Math.abs(distError)<=bandwidth){
			leftMotor.setSpeed(motorHigh);
			rightMotor.setSpeed(motorHigh);
			leftMotor.forward();
			rightMotor.forward();
		}
		
		//If the disterror is positive (meaning the robot is too far from the wall), then the robot must turn left
		else if(distError>0){
			leftMotor.setSpeed(motorLow);
			rightMotor.setSpeed(motorHigh);
			leftMotor.forward();
			rightMotor.forward();
		}
		
		//If the disterror is negative (meaning the robot is too close to the wall), then the robot must turn right
		else if(distError<0){
			leftMotor.setSpeed(motorHigh);
			rightMotor.setSpeed(motorLow);
			leftMotor.forward();
			rightMotor.forward();
		}
	}

	@Override
	public int readUSDistance() {
		return FilteredDistance;
	}
}
