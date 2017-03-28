package finalProject;

import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;

public class Correction extends Thread {

	public static Odometer odo;
	private Navigation nav;
	private SampleProvider colorSensorR; 
	private SampleProvider colorSensorL; 
	private SampleProvider colorSensorF;
	private float[] colorDataL = {0};	
	private float[] colorDataR = {0};	
	private float[] colorDataF = {0,0,0,0,0};
	public static double Dest_ini[]={0,0,0};


	private EV3LargeRegulatedMotor leftMotor, rightMotor;

	private double SENSOR_DIST = 6.5;
	private final double angleThreshold = 40;  	
	private double tilelength = 30.48;
	private int timeout=600;


	private static final int FORWARD_SPEED = WiFiExample.FORWARD_SPEED;
	private static final int ROTATE_SPEED = WiFiExample.ROTATE_SPEED;
	double wheel_radius = WiFiExample.WHEEL_RADIUS;
	double width =  WiFiExample.TRACK;
	public static double YTheta_Plus = 0; //Initializing theta variables
	public static double YTheta_Minus = 0;
	public static double XTheta_Plus = 0;
	public static double XTheta_Minus = 0;
	public static double deltaTheta = 0;
	public static double angleA, angleB;
	public static int gridcount=0;

	static final double correction = 18;

	public boolean correcting = false; 
	public boolean leftline = false;
	public boolean rightline= false; 
	public boolean turning = false;
	public static boolean localizing = false;

	public Correction(Odometer odo, Navigation nav, SampleProvider colorSensorR, SampleProvider colorSensorL, SampleProvider colorSensorF, EV3LargeRegulatedMotor leftMotor,EV3LargeRegulatedMotor rightMotor) {
		this.odo = odo;
		this.colorSensorR = colorSensorR;
		this.colorSensorL = colorSensorL;
		this.colorSensorF = colorSensorF;
		this.nav = nav;
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor; 
	}

	public void run(){ 
		pauseWhileTurning();
		if(gridcount==4){
			localize();
			gridcount=0;	
		}
		LightCorrection();
		
	}
	
	/**
	 * Travel orientation correct, uses light sensors on the side of the robot to detect grid lines, 
	 * if one side detects a line first, robot adjusts motors to correct the orientation of the robot
	 */
	public void LightCorrection (){
		
		if(localizing){
			return;
		}
		
		correcting = true; 
		leftMotor.setSpeed(FORWARD_SPEED);
		rightMotor.setSpeed(FORWARD_SPEED);

		//left and right sensors have not yet seen a black line
		leftline = false; 
		rightline= false; 

		Sound.twoBeeps();	//DO NOT REMOVE 
		while(!leftline  && !rightline){
			leftline = lineDetected(colorSensorL, colorDataL);
			rightline = lineDetected(colorSensorR, colorDataR);
			//if one of them starts seeing a line, this loop exits
			pauseWhileTurning();
		}
		
		if(leftline && rightline){
			updateOdo();
			pauseWhileTurning();
			correcting = false;	
			run();
		}

		if(leftline){
			do{ 
				leftMotor.setSpeed(10);
				rightline = lineDetected(colorSensorR, colorDataR);
			} while (rightline == false);

			leftMotor.setSpeed(FORWARD_SPEED);
			updateOdo();

			pauseWhileTurning();
			gridcount++;
			correcting = false;
			run();
		}

		else if(rightline){
			do{ 
				rightMotor.setSpeed(10);
				leftline = lineDetected(colorSensorL, colorDataL);
			} while (leftline == false);

			rightMotor.setSpeed(FORWARD_SPEED);
			updateOdo();

			pauseWhileTurning();
			gridcount++;
			correcting = false;
			run();

		}

	}
	
	public void pauseWhileTurning(){
		turning = nav.isTurning();
		while(turning){ //puts correction thread to sleep while turning
			try { Sound.buzz();
				turning=nav.isTurning();
				Thread.sleep(timeout); //every 500ms, it will run this while loop again
			} catch (InterruptedException e) {}
		}
	}
	
	public void localize(){

		Dest_ini=nav.getDest();

		nav.stop=true;
		localizing = true;

		double X_ini=odo.getX();
		double Y_ini=odo.getY();

		//synchronize both motors so they can only be accessed by one thread (the Correction thread in this case)
		synchronized(leftMotor){
			synchronized(rightMotor){
				
				boolean moving = true;
				while(moving){ //keep going until line detected
					leftMotor.rotate(-convertDistance(wheel_radius, 600), true);
					rightMotor.rotate(-convertDistance(wheel_radius, 600), true);
					if(lineDetected(colorSensorL, colorDataL)||lineDetected(colorSensorR, colorDataR)){
						moving = false; //if line detected from back sensors, stop going backward
					}
				}
						
				//at this point, the light sensors at back detected a line so we want to localize
				drive(-11.6); //go backward sensor dist for center of rotation to be at intersection
				
				turnTo(90); //turn right

				boolean moving2 = true;
				while(moving2){ //keep going until line detected
					leftMotor.rotate(convertDistance(wheel_radius, 600), true);
					rightMotor.rotate(convertDistance(wheel_radius, 600), true);
					if(lineDetected(colorSensorL, colorDataL)||lineDetected(colorSensorR, colorDataR)){
						moving2 = false; //go forward until line from back sensors is detected
					}
				}
				
				drive(-11.6); //drive back sensor dist
				turnTo(-90); //turn back to original heading
			}		
		}
		
		//calculate the position of the gridline intersection the robot just crossed
		double[] nearestIntersection={0,0,0};
		nearestIntersection=getIntersection(X_ini, Y_ini);
		odo.setPosition(nearestIntersection, new boolean[]{false, false, false});
		
		//FIGURE OUT HOW TO GIVE CONTROL BACK TO NAVIGATION so the robot can travel to the original x and y... 
		//it needs to be in navigation since we need to exit the localize method for the LightCorrection loop to regain control of the thread
		//this is the only way to have correction while navigating back to the original coordinates
		gridcount = 0; //dont remove this
		localizing = false;
		nav.stop=false;
		run();
	}

	public void updateOdo(){
		// get the x and y position read by the odometry
		double x = odo.getX();
		double y = odo.getY();

		double line;
		double position; 

		if(localizing){
			return;
		}
		
		// if the robot is going (increasing) along the x-direction, update the x-position and the heading
		if ((odo.getAng()>90-angleThreshold && odo.getAng()<90+angleThreshold)){

			line = (int)((x) / tilelength); 
			position = (line*tilelength)+11.6;
			odo.setPosition(new double [] {position, 0.0 , 90}, new boolean [] {true, false, true});	

		}
		// if the robot is going (decreasing) along the x-direction, update the x-position and the heading
		else if (odo.getAng()>270-angleThreshold && odo.getAng()<270+angleThreshold){
			// determine which line the robot has crossed by dividing the y-position returned by the odometer
			line = (int)((x) / tilelength); 
			// multiply by the length of a tile to know the y-position
			position = (line*tilelength)-11.6;
			odo.setPosition(new double [] {position, 0.0 , 270}, new boolean [] {true, false, true});	
		}

		// if the robot is going (decreasing) along the y-direction, update the y-position and the heading
		else if (odo.getAng()>180-angleThreshold && odo.getAng()<180+angleThreshold) {
			// determine which line the robot has crossed by dividing the y-position returned by the odometer
			line = (int)((y) / tilelength); 
			// multiply by the length of a tile to know the y-position
			position = (line*tilelength)-11.6;
			odo.setPosition(new double [] {0.0, position , 180}, new boolean [] {false, true, true});	
		}

		// if the robot is (increasingly) going along the y-direction, update the y-position and the heading
		else {
			// determine which line the robot has crossed by dividing the y-position returned by the odometer
			line = (int)((y) / tilelength); 
			// multiply by the length of a tile to know the y-position
			position = (line*tilelength)+11.6;
			odo.setPosition(new double [] {0.0, position , 0}, new boolean [] {false, true, true});
		}
	}

	public boolean iscorrecting(){
		return correcting;
	}

	public boolean islocalizing(){
		return localizing;
	}

	public boolean lineDetected(SampleProvider colorSensor, float[] colorData){
		colorSensor.fetchSample(colorData, 0);
		int light_val = (int)((colorData[0])*100);
		
		if(light_val <= 34){
			return true;
		}
		else
			return false;
	}

	public double[] getIntersection(double x, double y){
		double[] intersection={0.0,0.0,0.0};
		double lineX = (int)(x)/tilelength;
		double lineY = (int)(y)/tilelength;

		intersection[0]=lineX*tilelength;
		intersection[1]=lineY*tilelength;
		intersection[2]=0.0;

		return intersection;
	}

	public void turnTo(double theta){
		turning = true;
		Sound.twoBeeps(); //DONT REMOVE THIS
	
		//make robot turn to angle theta:
		leftMotor.setSpeed(ROTATE_SPEED);
		leftMotor.setAcceleration(2000);
		rightMotor.setSpeed(ROTATE_SPEED);
		rightMotor.setAcceleration(2000);
		
		leftMotor.rotate(convertAngle(wheel_radius, width, theta), true);
		rightMotor.rotate(-convertAngle(wheel_radius, width, theta), false);
		//returns default acceleration values after turn
		leftMotor.setAcceleration(6000);
		rightMotor.setAcceleration(6000);
		turning = false;
//		//this method causes the robot to turn (on point) to the absolute heading theta
////		leftMotor.setAcceleration(4000);
////		rightMotor.setAcceleration(4000);
//		//make robot turn to angle theta:
//		leftMotor.setSpeed(ROTATE_SPEED);
//		rightMotor.setSpeed(ROTATE_SPEED);
//
//		leftMotor.rotate(convertAngle(wheel_radius, width, theta), true);
//		rightMotor.rotate(-convertAngle(wheel_radius, width, theta), false);
//		
////		leftMotor.setAcceleration(6000);
////		rightMotor.setAcceleration(6000);
	}

	public void drive(double travelDist){
		//set both motors to forward speed desired
//		leftMotor.setAcceleration(4000);
//		rightMotor.setAcceleration(4000);
		
//		leftMotor.setSpeed(FORWARD_SPEED);
//		rightMotor.setSpeed(FORWARD_SPEED);

		leftMotor.rotate(convertDistance(wheel_radius, travelDist), true);
		rightMotor.rotate(convertDistance(wheel_radius, travelDist), false);
		
//		leftMotor.setAcceleration(6000);
//		rightMotor.setAcceleration(6000);
	}

	//convertDistance method: It takes the radius of the wheel and the distance required to travel and calculates the required wheel rotation
	private static int convertDistance(double radius, double distance) {
		return (int) ((180.0 * distance) / (Math.PI * radius));
	}
	//convertAngle method: This method takes the radius of wheel, width of cart and the angle required to be turned and calculated the required wheel rotation
	private static int convertAngle(double radius, double width, double angle) {
		return convertDistance(radius, Math.PI * width * angle / 360.0);
	}


}

