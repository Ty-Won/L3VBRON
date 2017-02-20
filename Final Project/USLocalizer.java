import lejos.robotics.SampleProvider;

import lejos.robotics.SampleProvider;

public class USLocalizer {
	public enum LocalizationType { FALLING_EDGE, RISING_EDGE };
	public static float rotSpeed = 30;

	private Odometer odo;
	private SampleProvider usSensor;
	public static float[] usData;
	private LocalizationType locType;
	private Navigation navigator;
	private int wallDist=12;
	private int noWall=40;
	

	public USLocalizer(Odometer odo,  SampleProvider usSensor, float[] usData, LocalizationType locType, Navigation navigator) {
		this.odo = odo;
		this.usSensor = usSensor;
		this.usData = usData;
		this.locType = locType;
		this.navigator = navigator;
	}

	public void doLocalization() {
		double [] pos = new double [3];
		double angleA, angleB;
		double deltaTheta;

		if (locType == LocalizationType.FALLING_EDGE) {
			// rotate the robot until it sees no wall
			rotateAwayWall(true);
			// keep rotating until the robot sees a wall, then latch the angle
			rotateToWall(true);
			
			try { Thread.sleep(100); } catch(Exception e){}
			angleA = odo.getAng();
					
			// switch direction and wait until it sees no wall
			rotateAwayWall(false);

			// keep rotating until the robot sees a wall, then latch the angle
			rotateToWall(false);
			
			try { Thread.sleep(100); } catch(Exception e){}
			angleB = odo.getAng();
			
			if (angleA<angleB)
				deltaTheta = 45 - (angleA+angleB)/2;
//				deltaTheta=225-(360-angleB)/2-angleA;
			else
				deltaTheta = 225 - (angleA+angleB)/2;
			
			
			try { Thread.sleep(100); } catch(Exception e){}
			
//			navigator.setSpeeds(0,0);
			// update the odometer position (example to follow:)
			odo.setPosition(new double [] {0.0, 0.0, angleB+deltaTheta}, new boolean [] {true, true, true});
			navigator.turnTo(0, true);
		} else {
			/*
			 * The robot should turn until it sees the wall, then look for the
			 * "rising edges:" the points where it no longer sees the wall.
			 * This is very similar to the FALLING_EDGE routine, but the robot
			 * will face toward the wall for most of it.
			 */
			
			while(usData[0]>=40)
				navigator.turnTo(1,false);
			
			
			
			
			
		}
	}
	
	private void rotateToWall(boolean right){
		
		if(right==true)
		navigator.setSpeeds(rotSpeed, -rotSpeed);
		
		else
		navigator.setSpeeds(-rotSpeed, rotSpeed);
		
		while(usData[0]>wallDist){
			usData[0]=getFilteredData();
		}
		
		navigator.setSpeeds(0, 0);
		
	}
	
	private void rotateAwayWall(boolean right){
		
		if(right==true)
			navigator.setSpeeds(rotSpeed, -rotSpeed);
			
		else
			navigator.setSpeeds(-rotSpeed, rotSpeed);
		
		while(usData[0]<noWall){
			usData[0]=getFilteredData();
		}
		
		navigator.setSpeeds(0, 0);
	}

	private float getFilteredData() {
				
		usSensor.fetchSample(usData, 0);
	
		float distance = usData[0]*100;
		int filterControl=0;
		int FILTER_OUT = 10;
		
//		if(distance >= 21474)
//			distance=50;
//		else if (distance >= wallDist+40 && filterControl < FILTER_OUT) {
//			// bad value, do not set the distance var, however do increment the
//			// filter value
//			filterControl++;
//		} else if (distance >= wallDist+40 && filterControl > FILTER_OUT) {
//			// We have repeated large values, so there must actually be nothing
//			// there: leave the distance alone
//			distance = distance;
//		}	
//		 else {
//			// distance went below 255: reset filter and leave
//			// distance alone.
//			filterControl = 0;
//			distance = distance;
//			
//		}
		return distance;
	}

}
