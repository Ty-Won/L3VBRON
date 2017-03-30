package finalProject;

import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.SampleProvider;

/**
 * This class should fix errors in the navigation program of the robot. The program should use two
 * light sensors located on the back of the robot in order to determine the heading of the robot
 * in relation to the correct heading of the robot. When the sensors pass over a grid
 * line, the program calculates the difference in position of the two sensors based
 * on when the two pass over the grid line in relation to each other. The wheel located on the 
 * side of the robot which the first sensor passed over the grid line should stop rotating until
 * the other sensor crosses over the grid line thus straightening the robot and correcting its
 * heading.
 * In addition, after the robot crosses over four grid lines, the system should perform
 * an additional localization in order to more exactly correct its position and heading.
 * 
 * @author Ilana Haddad
 * @author Tristan Bouchard
 * @author Tyrone Wong
 * @author Alexandre Tessier
 *
 */
public class Correction {

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
	private int timeout=400;


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
	public int gridcount=0;

	static final double correction = 18;

	public boolean correcting = false; 
	public boolean leftline = false;
	public boolean rightline= false; 
	public boolean turning = false;
	public static boolean localizing = false;
	public boolean stop = false;

	/**
	 * 
	 * @param odo the odometer of the robot.
	 * @param nav the navigation system of the robot.
	 * @param colorSensorR the right rear color sensor, located parallel to the right wheel.
	 * @param colorSensorL the left rear color sensor, located parallel to the left wheel.
	 * @param colorSensorF the front color sensor, located in the center front of the robot.
	 * @param leftMotor the left wheel motor.
	 * @param rightMotor the right wheel motor.
	 */
	public Correction(Odometer odo, Navigation nav, SampleProvider colorSensorR, SampleProvider colorSensorL, SampleProvider colorSensorF, EV3LargeRegulatedMotor leftMotor,EV3LargeRegulatedMotor rightMotor) {
		this.odo = odo;
		this.colorSensorR = colorSensorR;
		this.colorSensorL = colorSensorL;
		this.colorSensorF = colorSensorF;
		this.nav = nav;
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor; 
	}

	/**
	 * This method should continuously check the amount of grid lines that the robot has passed
	 * while going forward (the program should halt the check while the robot is turning
	 * as it may pass many lines in this action but not change it's position at all) and after
	 * the robot has crossed over four, should call for the robot to localize to correct its
	 * position and heading.
	 */
	//	public void run(){ 
	//		pauseWhileTurning();
	//		LightCorrection();
	//	}

	/**
	 * Orientation correction: this method should use the two light sensors affixed to the rear
	 * of the robot in order to determine inaccuracies in the heading of the robot. If one of the
	 * two light sensors crosses before the other the motor corresponding to that light sensor
	 * should stop rotating until the other sensor catches up and crosses the grid line, thus returning
	 * the robot to a forward heading.
	 * 
	 */
	public void LightCorrection (){

		//		if(localizing){
		//			return;
		//		}

		correcting = true; 
		//		leftMotor.setSpeed(FORWARD_SPEED);
		//		rightMotor.setSpeed(FORWARD_SPEED);

		//left and right sensors have not yet seen a black line
		leftline = false; 
		rightline= false; 

		Sound.twoBeeps();	//DO NOT REMOVE 
		while(!leftline  && !rightline){
			leftline = lineDetected(colorSensorL, colorDataL);
			rightline = lineDetected(colorSensorR, colorDataR);
			if(leftMotor.isMoving()==false){
				return;
			}

			//if one of them starts seeing a line, this loop exits
			//			pauseWhileTurning();
		}

		if(leftline && rightline){
			updateOdo();
			//			pauseWhileTurning();
			correcting = false;	
			//			run();
		}

		if(leftline){
			do{ 
				leftMotor.setSpeed(10);
				rightline = lineDetected(colorSensorR, colorDataR);
			} while (rightline == false);

			leftMotor.setSpeed(FORWARD_SPEED);
			updateOdo();

			//			pauseWhileTurning();
			gridcount++;
			correcting = false;
			//			run();
		}

		else if(rightline){
			do{ 
				rightMotor.setSpeed(10);
				leftline = lineDetected(colorSensorL, colorDataL);
			} while (leftline == false);

			rightMotor.setSpeed(FORWARD_SPEED);
			updateOdo();

			//			pauseWhileTurning();
			gridcount++;
			correcting = false;
			//			run();

		}

	}

	/**
	 * This method is used to stop the robot from performing correction while the robot is turning.
	 */
	//	public void pauseWhileTurning(){
	//		turning = nav.isTurning();
	//		while(turning){ //puts correction thread to sleep while turning
	//			try { Sound.buzz();
	//			turning=nav.isTurning();
	//			Thread.sleep(timeout); //every 500ms, it will run this while loop again
	//			} catch (InterruptedException e) {}
	//		}
	//	}

	/**
	 * The localization program that is to be used during the navigation of the robot. This should be called
	 * after the robot has passed four grid lines since its last localization. Once this occurs, the robot should
	 * back up in order for its center of rotation so be located as close to the intersection of the grid lines
	 * as possible. Then the robot should turn 90 degrees and again move forward until the sensors find the grid line
	 * before backing up so that the center of rotation is directly on top of the intersection.
	 */
	public void localize(){

		//		nav.stop=true;
		localizing = true;

		//synchronize both motors so they can only be accessed by one thread (the Correction thread in this case)
		synchronized(leftMotor){
			synchronized(rightMotor){

				motorstop();
				Sound.twoBeeps();
				leftMotor.synchronizeWith(new RegulatedMotor[] {rightMotor});
				boolean moving = true;
				//				while(moving){ //keep going until line detected

				leftMotor.startSynchronization();
				leftMotor.rotate(-convertDistance(wheel_radius, 600), true);
				rightMotor.rotate(-convertDistance(wheel_radius, 600), true);
				rightMotor.endSynchronization();

				if(lineDetected(colorSensorL, colorDataL)||lineDetected(colorSensorR, colorDataR)){	//at this point, the light sensors at back detected a line so we want to localize
					//						moving = false; //if line detected from back sensors, stop going backward
					motorstop(); //kills all .rotate()
					nav.driveDiag(-11.6); //go backward sensor dist for center of rotation to be at intersection
					//						Sound.twoBeeps();
					nav.turnTo(90);//turn right
					//					}
				}

				boolean moving2 = true;
				//				while(moving2){ //keep going until line detected
				leftMotor.startSynchronization();
				leftMotor.rotate(convertDistance(wheel_radius, 600), true);
				rightMotor.rotate(convertDistance(wheel_radius, 600), true);
				leftMotor.endSynchronization();
				if(lineDetected(colorSensorL, colorDataL)||lineDetected(colorSensorR, colorDataR)){
					//						moving2 = false; //go forward until line from back sensors is detected
					motorstop(); //kills all .rotate()
					nav.driveDiag(-11.6); //drive back sensor dist
					//						Sound.twoBeeps();
					nav.turnTo(-90); //turn back to original heading
					//					}		
				}

				gridcount = 0; //dont remove this
				localizing = false;
				//				nav.stop=false;
			}
		}
	}

	/**
	 * The method should update the x and y position of the robot to correspond with the corrections made
	 * in other methods in the code.
	 */
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
			line = (int)(((x)+(tilelength/2)) / tilelength); 
			// multiply by the length of a tile to know the y-position
			position = (line*tilelength)-11.6;
			//			if(position<0){
			//				return;
			//			}
			odo.setPosition(new double [] {position, 0.0 , 270}, new boolean [] {true, false, true});	
		}

		// if the robot is going (decreasing) along the y-direction, update the y-position and the heading
		else if (odo.getAng()>180-angleThreshold && odo.getAng()<180+angleThreshold) {
			// determine which line the robot has crossed by dividing the y-position returned by the odometer
			line = (int)(((y)+(tilelength/2)) / tilelength);		
			// multiply by the length of a tile to know the y-position
			position = (line*tilelength)-11.6;
			//			if(position<0){
			//				return;
			//			}
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

	/**
	 * 
	 * @return a boolean which is true if the robot is correcting and false if not
	 */
	public boolean iscorrecting(){
		return correcting;
	}

	/**
	 * 
	 * @return a boolean which is true if the robot is localizing and false if not
	 */
	public boolean islocalizing(){
		return localizing;
	}

	/**
	 * Determines whether the color sensor has detected a line or not.
	 * 
	 * @param colorSensor the color sensor of the robot
	 * @param colorData the array which will hold the color sensor data
	 * @return true if a grid line has been detected and false if not
	 */
	public boolean lineDetected(SampleProvider colorSensor, float[] colorData){
		colorSensor.fetchSample(colorData, 0);
		int light_val = (int)((colorData[0])*100);

		if(light_val <= 34){
			return true;
		}
		else
			return false;
	}

	public void motorstop(){
		leftMotor.setSpeed(0);
		rightMotor.setSpeed(0);
		leftMotor.stop();
		rightMotor.stop();
		leftMotor.setSpeed(ROTATE_SPEED);
		rightMotor.setSpeed(ROTATE_SPEED);
	}

	/**
	 * 
	 * @param x the current position in the x direction
	 * @param y the current position in the y direction
	 * @return the location of the intersection closest to the current postion
	 */
	//	public double[] getIntersection(double x, double y){
	//		double[] intersection={0.0,0.0,0.0};
	//		double lineX = (int)(x)/tilelength;
	//		double lineY = (int)(y)/tilelength;
	//
	//		intersection[0]=lineX*tilelength;
	//		intersection[1]=lineY*tilelength;
	//		intersection[2]=0.0;
	//
	//		return intersection;
	//	}

	/**
	 * This method should turn the robot to the given heading. Very similar to the method of the same name in 
	 * navigation.
	 * 
	 * @param theta the angle to which the robot should turn.
	 */
	//	public void turnTo(double theta){
	//		turning = true;
	//		Sound.twoBeeps(); //DONT REMOVE THIS
	//
	//		//make robot turn to angle theta:
	//		leftMotor.setSpeed(ROTATE_SPEED);
	//		leftMotor.setAcceleration(2000);
	//		rightMotor.setSpeed(ROTATE_SPEED);
	//		rightMotor.setAcceleration(2000);
	//
	//		leftMotor.rotate(convertAngle(wheel_radius, width, theta), true);
	//		rightMotor.rotate(-convertAngle(wheel_radius, width, theta), false);
	//		//returns default acceleration values after turn
	//		leftMotor.setAcceleration(6000);
	//		rightMotor.setAcceleration(6000);
	//		turning = false;
	//		//		//this method causes the robot to turn (on point) to the absolute heading theta
	//		////		leftMotor.setAcceleration(4000);
	//		////		rightMotor.setAcceleration(4000);
	//		//		//make robot turn to angle theta:
	//		//		leftMotor.setSpeed(ROTATE_SPEED);
	//		//		rightMotor.setSpeed(ROTATE_SPEED);
	//		//
	//		//		leftMotor.rotate(convertAngle(wheel_radius, width, theta), true);
	//		//		rightMotor.rotate(-convertAngle(wheel_radius, width, theta), false);
	//		//		
	//		////		leftMotor.setAcceleration(6000);
	//		////		rightMotor.setAcceleration(6000);
	//	}

	/**
	 * This method should move the robot forward by the given amount. Very similar to the method of the 
	 * same name located in navigation.
	 * 
	 * @param travelDist the distance by which the robot should travel straight
	 */
	//	public void drive(double travelDist){
	//		//set both motors to forward speed desired
	//		//		leftMotor.setAcceleration(4000);
	//		//		rightMotor.setAcceleration(4000);
	//
	//		//		leftMotor.setSpeed(FORWARD_SPEED);
	//		//		rightMotor.setSpeed(FORWARD_SPEED);
	//
	//		leftMotor.rotate(convertDistance(wheel_radius, travelDist), true);
	//		rightMotor.rotate(convertDistance(wheel_radius, travelDist), false);
	//
	//		//		leftMotor.setAcceleration(6000);
	//		//		rightMotor.setAcceleration(6000);
	//	} s

	//convertDistance method: It takes the radius of the wheel and the distance required to travel and calculates the required wheel rotation
	/**
	 * The method should convert the input distance into a form that is equal to
	 * the amount of rotation that a wheel of the given radius must rotate
	 * in order to move that distance
	 * 
	 * @param radius the radius of the wheels of the robot
	 * @param distance the distance which will be converted
	 * @return the converted distance
	 */
	private static int convertDistance(double radius, double distance) {
		return (int) ((180.0 * distance) / (Math.PI * radius));
	}
	//convertAngle method: This method takes the radius of wheel, width of cart and the angle required to be turned and calculated the required wheel rotation
	/**
	 * The method should convert the input angle into a form that can be performed
	 * by the robot with the given wheel radius and width.
	 * 
	 * 
	 * @param radius the radius of the wheel
	 * @param width the width of the robot
	 * @param angle the angle to be converted
	 * @return the angle now in the form of amount of rotation needed by the robot's wheel to perform that angle of turn
	 */
	//	private static int convertAngle(double radius, double width, double angle) {
	//		return convertDistance(radius, Math.PI * width * angle / 360.0);
	//	}


}

