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
 * This will be done in a different way than the original localization. The robot
 * will first back up to find a grid line with its back two sensors before turning ninety degrees
 * and doing the same in the other dimension. It will then use these findings to recalibrate the robot's
 * heading and position.
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
	private float[] colorDataL = {0};	
	private float[] colorDataR = {0};	
	public static double Dest_ini[]={0,0,0};


	private EV3LargeRegulatedMotor leftMotor, rightMotor;
	private final double angleThreshold = 40;  	
	private double tilelength = 30.48;


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
		this.nav = nav;
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor; 
	}

	/**
	 * Orientation correction: this method should use the two light sensors affixed to the rear
	 * of the robot in order to determine inaccuracies in the heading of the robot. If one of the
	 * two light sensors crosses before the other the motor corresponding to that light sensor
	 * should stop rotating until the other sensor catches up and crosses the grid line, thus returning
	 * the robot to a forward heading.
	 * 
	 */
	public void LightCorrection (){
		correcting = true; 

		//left and right sensors have not yet seen a black line
		leftline = false; 
		rightline= false; 

		Sound.twoBeeps();	//DO NOT REMOVE 
		while(!leftline  && !rightline){
			leftline = lineDetected(colorSensorL, colorDataL);
			rightline = lineDetected(colorSensorR, colorDataR);
			if(leftMotor.isMoving()==false||rightMotor.isMoving()==false){
				return;
			}
			if(WiFiExample.cont.avoidingOb){
				gridcount = 0;

			}
		}

		if(leftline && rightline){
			updateOdo();
			correcting = false;	
		}

		if(leftline){
			do{ 
				leftMotor.setSpeed(1);
				rightMotor.setSpeed(350);
				rightline = lineDetected(colorSensorR, colorDataR);
			} while (rightline == false);

			leftMotor.setSpeed(FORWARD_SPEED);
			rightMotor.setSpeed(FORWARD_SPEED);
			updateOdo();

			gridcount++;
			correcting = false;

		}

		else if(rightline){
			do{ 
				rightMotor.setSpeed(10);
				leftline = lineDetected(colorSensorL, colorDataL);
			} while (leftline == false);

			rightMotor.setSpeed(FORWARD_SPEED);
			updateOdo();

			gridcount++;
			correcting = false;
		}
	}



	/**
	 * The localization program that is to be used during the navigation of the robot. This should be called
	 * after the robot has passed four grid lines since its last localization. Once this occurs, the robot should
	 * back up in order for its center of rotation so be located as close to the intersection of the grid lines
	 * as possible. Then the robot should turn 90 degrees and again move forward until the sensors find the grid line
	 * before backing up so that the center of rotation is directly on top of the intersection.
	 */
	public void localize(){
		localizing = true;

		//synchronize both motors so they can only be accessed by one thread (the Correction thread in this case)
		synchronized(leftMotor){
			synchronized(rightMotor){

				leftMotor.rotate(-convertDistance(wheel_radius, 100), true);
				rightMotor.rotate(-convertDistance(wheel_radius, 100), true);

				boolean left = false; 
				boolean right= false; 
				while(!left&&!right){
					left = lineDetected(colorSensorL, colorDataL);
					right = lineDetected(colorSensorR, colorDataR);	//at this point, the light sensors at back detected a line so we want to localize
				}
				
				motorstop(); //kills all .rotate()
				nav.driveDiag(-11.6); //go backward sensor dist for center of rotation to be at intersection
				
				nav.turnTo(90);//turn right

				leftMotor.rotate(convertDistance(wheel_radius, 100), true);
				rightMotor.rotate(convertDistance(wheel_radius, 100), true);

				boolean left2 = false; 
				boolean right2= false; 
				while(!left2&&!right2){
					left2 = lineDetected(colorSensorL, colorDataL);
					right2 = lineDetected(colorSensorR, colorDataR);	//at this point, the light sensors at back detected a line so we want to localize
				}

				motorstop(); //kills all .rotate()
				nav.driveDiag(-11.6); //drive back sensor dist
				
				nav.turnTo(-90); //turn back to original heading
			
				double X_ini = odo.getX();
				double Y_ini = odo.getY();

				if(X_ini<-tilelength/2){
					X_ini = X_ini-tilelength/2;
				}
				else{
					X_ini = X_ini+tilelength/2;
				}

				if(Y_ini<-tilelength/2){
					Y_ini = Y_ini-tilelength/2;
				}
				else{
					Y_ini = Y_ini+tilelength/2;
				}

				//calculate the position of the gridline intersection the robot just crossed
				double[] nearestIntersection={0,0,0};
				nearestIntersection=getIntersection(X_ini, Y_ini);
				odo.setPosition(nearestIntersection, new boolean[]{true, true, false});

				gridcount = 0; //dont remove this
				localizing = false;
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
			line = (int)(((x)+19) / tilelength); //MAKE SURE 19 IS OKAY
			// multiply by the length of a tile to know the y-position
			position = (line*tilelength)-11.6;
	
			odo.setPosition(new double [] {position, 0.0 , 270}, new boolean [] {true, false, true});	
		}

		// if the robot is going (decreasing) along the y-direction, update the y-position and the heading
		else if (odo.getAng()>180-angleThreshold && odo.getAng()<180+angleThreshold) {
			// determine which line the robot has crossed by dividing the y-position returned by the odometer
			line = (int)(((y)+(19)) / tilelength);	//MAKE SURE 19 IS OKAY	
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

		if(light_val <= 28){
			return true;
		}
		else
			return false;
	}

	/**
	 * This method is used to very quickly stop the robot from moving in whatever direction it is
	 * moving in before setting it's speed to be 150 degrees/second shortly afterward.
	 */
	public void motorstop(){
		leftMotor.setSpeed(0);
		rightMotor.setSpeed(0);
		leftMotor.forward();
		rightMotor.forward();

		leftMotor.stop(true);
		rightMotor.stop(false);

		leftMotor.setSpeed(ROTATE_SPEED);
		rightMotor.setSpeed(ROTATE_SPEED);
		leftMotor.setAcceleration(1000);
		rightMotor.setAcceleration(1000);
	}

	/**
	 * Method which returns the intersection closest to the current position of the
	 * robot.
	 * 
	 * @param x the current position in the x direction
	 * @param y the current position in the y direction
	 * @return the location of the intersection closest to the current position
	 */
	public double[] getIntersection(double x, double y){
		double[] intersection={0.0,0.0,0.0};
		double lineX = (int)((x)/tilelength);
		double lineY = (int)((y)/tilelength);

		intersection[0]=lineX*tilelength;
		intersection[1]=lineY*tilelength;
		intersection[2]=0.0;

		return intersection;
	}

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

	/**
	 * This method is very similar to the normal localization method in this class but will
	 * be executed only when the robot has reached the ball dispenser and is almost ready to 
	 * receive the ball. The robot should perform a normal localization but it should ensure that
	 * the robot not turn into the wall that will be present as the robot will be very close
	 * to the wall the dispenser is connected to. 
	 */
	public void localizeFWD(){

	
		localizing = true;

		//synchronize both motors so they can only be accessed by one thread (the Correction thread in this case)
		synchronized(leftMotor){
			synchronized(rightMotor){
				leftMotor.rotate(convertDistance(wheel_radius, 100), true);
				rightMotor.rotate(convertDistance(wheel_radius, 100), true);
				boolean left = false; 
				boolean right= false; 
				while(!left&&!right){
					left = lineDetected(colorSensorL, colorDataL);
					right = lineDetected(colorSensorR, colorDataR);	//at this point, the light sensors at back detected a line so we want to localize
				}
				
				motorstop(); //kills all .rotate()
				nav.driveDiag(-11.6); //go backward sensor dist for center of rotation to be at intersection
				
				nav.turnTo(90);//turn right
				
				leftMotor.rotate(convertDistance(wheel_radius, 100), true);
				rightMotor.rotate(convertDistance(wheel_radius, 100), true);
				

				boolean left2 = false; 
				boolean right2= false; 
				while(!left2&&!right2){
					left2 = lineDetected(colorSensorL, colorDataL);
					right2 = lineDetected(colorSensorR, colorDataR);	//at this point, the light sensors at back detected a line so we want to localize
				}

				motorstop(); //kills all .rotate()
				nav.driveDiag(-11.6); //drive back sensor dist

				nav.turnTo(-90); //turn back to original heading


				double X_ini = odo.getX();
				double Y_ini = odo.getY();

				if(X_ini<-tilelength/2){
					X_ini = X_ini-tilelength/2;
				}
				else{
					X_ini = X_ini+tilelength/2;
				}

				if(Y_ini<-tilelength/2){
					Y_ini = Y_ini-tilelength/2;
				}
				else{
					Y_ini = Y_ini+tilelength/2;
				}

				//calculate the position of the gridline intersection the robot just crossed
				double[] nearestIntersection={0,0,0};
				nearestIntersection=getIntersection(X_ini, Y_ini);
				odo.setPosition(nearestIntersection, new boolean[]{true, true, false});

				gridcount = 0; //dont remove this
				localizing = false;

			}
		}
	}

	/**
	 * This method is very similar to the normal localization method found within this class.
	 * However, this method will only be called directly after the robot has finished with
	 * avoiding an obstacle. In this method, the robot will only localize in one direction to reset the 
	 * position of the angle in the direction which it will be driving following 
	 * the avoidance of an obstacle.
	 */
	public void localizeForAvoidance(){
		// goes back until line detected, goes back 11.6
		localizing = true;

		//synchronize both motors so they can only be accessed by one thread (the Correction thread in this case)
		synchronized(leftMotor){
			synchronized(rightMotor){
				leftMotor.rotate(-convertDistance(wheel_radius, 100), true);
				rightMotor.rotate(-convertDistance(wheel_radius, 100), true);
				boolean left = false; 
				boolean right= false; 
				while(!left&&!right){
					left = lineDetected(colorSensorL, colorDataL);
					right = lineDetected(colorSensorR, colorDataR);	//at this point, the light sensors at back detected a line so we want to localize
				}

				motorstop(); //kills all .rotate()
				nav.driveDiag(-11.6); //go backward sensor dist for center of rotation to be at intersection


				gridcount = 0; //dont remove this
				localizing = false;

			}
		}

	}


}

