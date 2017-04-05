package finalProject;

import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;

/**
 * The obstacle avoidance class for the robot.
 * 
 * @author Ian Gauthier
 * @author Ilana Haddad
 * @author Tristan Bouchard
 * @author Tyrone Wong
 * @author Alexandre Tessier
 *
 */
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
	public boolean doit = true;
	public boolean stopSensing = false;

	/**
	 * 
	 * @param leftMotor the motor connected to the left wheel
	 * @param rightMotor the motor connected to the right wheel
	 * @param usPoller the class which continually polls the ultrasonic sensor
	 * @param usValue 
	 * @param usSensor the ultrasonic sensor of the robot
	 * @param usData the array in which the data from the us sensor is held
	 */
	public PController(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor,
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
	
	/**
	 * 
	 */
	public void run(){
//		usMotor.rotate(-30);
		int x = 255;
		while(doit){
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
		

	}

	public void avoidOB(){
		avoidedBlock=false;
		int distance = 255;
//		Sound.buzz();
		double x = 0;
		while(avoidingOb){
			distance = readUSDistance();
			if(stopSensing){
				distance = 255;
				break;
			}
			
			if(distance < 30){
				motorstop();
				nav.turnToSmart(odo.getAng()+90); //turn right
				distance = readUSDistance();
				if(distance<30){//there is another obstacle to the right, so turn back
					//	WiFiExample.navigation.turnToSmart(WiFiExample.odometer.getAng()-180); //turn left
					nav.turnToSmart(odo.getAng()-90); //turn left
					avoiding = true;
					WiFiExample.correction.localizeForAvoidance(); //goes to intersection
					//System.out.println("in the if block");
					nav.turnToSmart(odo.getAng()-90); //turn left
					nav.driveWCorrection(30.48);
					nav.turnToSmart(odo.getAng()+90);
					nav.driveWCorrection(2*30.48);
					nav.turnToSmart(odo.getAng()+90); //turn right
					//check if obstacle is still there
					distance = readUSDistance();
					if(distance <25){ //turn back and go another block
						nav.turnToSmart(odo.getAng()-90); //turn left
						nav.driveWCorrection(30.48);
						nav.turnToSmart(odo.getAng()+90); //turn right
						nav.driveWCorrection(30.48);
						nav.turnToSmart(odo.getAng()-90); //turn left
					}
					else{
						nav.driveWCorrection(30.48);
						nav.turnToSmart(odo.getAng()-90); //turn left
						
					}
					avoidedBlock=true;
				}
				else{
					nav.turnToSmart(odo.getAng()-90); //turn left
					WiFiExample.correction.localizeForAvoidance(); //goes to intersection
					//System.out.println("in the else block");
					nav.turnToSmart(odo.getAng()+90); //turn right
					nav.driveWCorrection(30.48);
					nav.turnToSmart(odo.getAng()-90); //turn left
					nav.driveWCorrection(2*30.48);
					nav.turnToSmart(odo.getAng()-90); //turn left
					distance = readUSDistance();
					if(distance <25){ //turn back and go another block
						nav.turnToSmart(odo.getAng()+90); //turn right
						nav.driveWCorrection(30.48);
						nav.turnToSmart(odo.getAng()-90); //turn left
						nav.driveWCorrection(30.48);
						nav.turnToSmart(odo.getAng()+90); //right
					}
					else{
						nav.driveWCorrection(30.48);
						nav.turnToSmart(odo.getAng()+90); //turn left
						
					}
					avoidedBlock=true;

					motorShift = 0;
				}
			}
			else{
				//Sound.twoBeeps();
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
		leftMotor.setAcceleration(10000);
		rightMotor.setAcceleration(10000);
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
	


}