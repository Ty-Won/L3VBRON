package finalProject;

import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;

public class PController extends Thread{
	private final int motorStraight = 300, FILTER_OUT = 20;
	private EV3LargeRegulatedMotor leftMotor, rightMotor;
	private EV3MediumRegulatedMotor usMotor;
	private int filterControl, motorShift;
	int FilteredDistance;
	private UltrasonicPoller usPoller ;
	private int distance;
	public boolean avoidingOb;
	boolean avoidedBlock=false;
	private static final int FORWARD_SPEED = WiFiExample.FORWARD_SPEED;
	private SampleProvider usValue;
	private static SensorModes usSensor;
	private static float[] usData;
	double wheel_radius = WiFiExample.WHEEL_RADIUS;
	private static final int ROTATE_SPEED = WiFiExample.ROTATE_SPEED;
	public boolean avoiding = false;
	Navigation nav = WiFiExample.navigation;
	Odometer odo = WiFiExample.odometer;

	public PController(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor, EV3MediumRegulatedMotor usMotor,
			UltrasonicPoller usPoller, SampleProvider usValue, SensorModes usSensor, float[] usData) {
		//Default Constructor
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		this.usMotor = usMotor;
		this.usPoller = usPoller;
		//leftMotor.setSpeed(motorStraight);					// Initalize motor rolling forward
		//rightMotor.setSpeed(motorStraight);
		//leftMotor.forward();
		//rightMotor.forward();
		filterControl = 0;
		motorShift = 0;
		this.usValue=usValue;
		this.usSensor = usSensor;
		this.usData = usData;
	}
	public void run(){
//		usMotor.rotate(-30);
		int x = 255;
		while(true){
			if(avoiding){
				break;
			}
			x = readUSDistance();
			avoidingOb =processUSData(x);
			try { Thread.sleep(50); } catch(Exception e){}	
		}
	}

	public boolean processUSData(int distance) {
		if(distance>=25){
			return false;
		}
		else{
			return true;
		}
		
// I COMMENTED THIS OUT starts from here***************************

		
//		if (distance >= 255 && filterControl < FILTER_OUT) {
//			// bad value, do not set the distance var, however do increment the
//			// filter value
//			filterControl++;
//		} else if (distance >= 255) {
//			// We have repeated large values, so there must actually be nothing
//			// there: leave the distance alone
//			this.distance = distance;
//		} else {
//			// distance went below 255: reset filter and leave
//			// distance alone.
//			filterControl = 0;
//			this.distance = distance;
//		}
//
//		if(motorShift > 9 && this.distance > 25)
//		{
//			if(usMotor.getTachoCount() < 0)
//			{
//		//		usMotor.rotate(80);
//				motorShift = 0;
//				return false;
//			}
//			else
//			{
//		//		usMotor.rotate(-80);
//				motorShift = 0;
//				return false;
//			}
//		}
//		else if(this.distance > 25)
//		{
//			motorShift++;
//			return false;
//		}
//		else
//		{
//			return true;
//		}

//ends here *********************************************************

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
	{	avoidedBlock=false;
		int distance = 255;
		Sound.buzz();
		double x = 0;
		while(avoidingOb){
			
			distance = readUSDistance();
			
//			if(distance>0 && distance < 25 && usMotor.getTachoCount() < 0){
//			if(distance>0 && distance < 25 ){
//				
//				System.out.println("Obstacle found left!");
////				rightMotor.stop();
////				leftMotor.stop();
////				x = WiFiExample.odometer.getAng();
////				WiFiExample.navigation.turnTo(x + 90);
////				avoidingOb = false;
//				
//				
//				avoidingOb = true;
//			//	motorstop();
//				leftMotor.setSpeed(200);
//				rightMotor.setSpeed(distance*8);
//				leftMotor.rotate(convertDistance(wheel_radius, 20), true);
//				rightMotor.rotate(convertDistance(wheel_radius, 20), true);
//				motorShift = 0;
//			}
//			else if(distance>0 && distance < 25 && usMotor.getTachoCount() > 0){
			if(distance<10 && distance < 20){	
				//System.out.println("Obstacle found right!");
//				rightMotor.stop();
//				leftMotor.stop();
//				x = WiFiExample.odometer.getAng();
//				WiFiExample.navigation.turnTo(x - 90);


//				motorstop();
				nav.turnToSmart(odo.getAng()+90); //turn right
				distance = readUSDistance();
				if(distance<30){//there is another obstacle to the right, so turn back
				//	WiFiExample.navigation.turnToSmart(WiFiExample.odometer.getAng()-180); //turn left
					nav.turnToSmart(odo.getAng()-90); //turn left
					avoiding = true;
					WiFiExample.correction.localizeForAvoidance(); //goes to intersection
					System.out.println("in the if block");
					nav.turnToSmart(odo.getAng()-90); //turn left
					nav.driveWCorrection(30.48);
					nav.turnToSmart(odo.getAng()+90);
					nav.driveWCorrection(2*30.48);
					nav.turnToSmart(odo.getAng()+90); //turn right
					nav.driveWCorrection(30.48);
					nav.turnToSmart(odo.getAng()-90); //turn left
					avoidedBlock=true;
				}
				else{
					nav.turnToSmart(odo.getAng()-90); //turn left
					WiFiExample.correction.localizeForAvoidance(); //goes to intersection
					System.out.println("in the else block");
					nav.turnToSmart(odo.getAng()+90); //turn right
					
					nav.driveWCorrection(30.48);
					nav.turnToSmart(odo.getAng()-90); //turn left
					nav.driveWCorrection(2*30.48);
					nav.turnToSmart(odo.getAng()-90); //turn left
					nav.driveWCorrection(30.48);
					nav.turnToSmart(odo.getAng()+90); //turn right
					avoidedBlock=true;
					
//					WiFiExample.navigation.turnToSmart(-90);
//					WiFiExample.navigation.driveDiag(30);
//					WiFiExample.navigation.turnToSmart(0);
//				rightMotor.setSpeed(200);
//				leftMotor.setSpeed(distance*8);
//				rightMotor.forward();
//				leftMotor.forward();
//				leftMotor.rotate(convertDistance(wheel_radius, 20), true);
//				rightMotor.rotate(convertDistance(wheel_radius, 20), true);
				motorShift = 0;
				}
			}
			
			else{
				Sound.twoBeeps();
				avoidingOb = false;
			}
		}
		avoiding = false;
	}
	
	
	
	
	public static int readUSDistance() {
		usSensor.fetchSample(usData, 0);
		float distance = usData[0];
		distance = (int)(usData[0]*100.0);
		// Rudimentary filter
		if (distance > 50){
			distance = 255;
		}
		return (int)distance;

	}
	public void motorstop(){

		leftMotor.setSpeed(0);
		rightMotor.setSpeed(0);
		leftMotor.forward();
		rightMotor.forward();
//		leftMotor.startSynchronization();
		leftMotor.stop(true);
		rightMotor.stop(false);
//		leftMotor.endSynchronization();
//		leftMotor.setSpeed(ROTATE_SPEED);
//		rightMotor.setSpeed(ROTATE_SPEED);
		leftMotor.setAcceleration(1000);
		rightMotor.setAcceleration(1000);
	}
	
	private static int convertDistance(double radius, double distance) {
		return (int) ((180.0 * distance) / (Math.PI * radius));
	}

}