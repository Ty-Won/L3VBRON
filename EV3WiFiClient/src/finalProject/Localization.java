package finalProject;
import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;
/**
 * The class runs before either forward or defense are called and is used to 
 * set an original position for the robot. The first action taken is to perform 
 * an Ultrasonic localization in order to determine a general area for the robot. 
 * Then the robot moves toward the perpendicular cross of the grid lines in order 
 * to be able to clock the grid lines. Once it moves to the perpendicular cross, 
 * the robot rotates until four lines have been crossed, using the light sensor 
 * to determine when this occurs. Once this has happened, the robot uses trigonometry 
 * to calculate its position and angle error and centers itself on the cross with 
 * its direction pointed to zero degrees.
 * 
 * @author Ian Gauthier
 * @author Ilana Haddad
 * @author Tristan Bouchard
 * @author Tyrone Wong
 * @author Alexandre Tessier
 * 
 * @version 3.0
 *
 */
public class Localization {
	public static Odometer odo;
	private Navigation nav;
	private SampleProvider colorSensorF; //stands for color sensor in front
	private SampleProvider usValue;
	private SensorModes usSensor;
	private float[] usData;
	private EV3LargeRegulatedMotor leftMotor, rightMotor;
	private float[] colorData;	
	private float[] colorData2;

	/** The rotational speed of the robot's wheels when moving forward.*/
	private static final int FORWARD_SPEED = WiFiExample.FORWARD_SPEED;
	
	/** The rotational speed of the robot's wheels when turning. */
	private static final int ROTATION_SPEED = WiFiExample.ROTATE_SPEED;
	
	/** The value of the wheel radius is passed on from the main class in WifiExample*/
	double WHEEL_RADIUS = WiFiExample.WHEEL_RADIUS;
	
	/** The value of the track width is passed on from the main class in WifiExample*/
	double TRACK =  WiFiExample.TRACK;
	
	/** The difference between the current heading and the actual zero heading.
	 * dTheta is used during the Ultrasonic Localization */
	private double dTheta; 	//delta theta 
	
	/** Distance from the center of rotation of robot to the front light sensor */
	private double SENSOR_DIST = 6.5;
	
	/** The Y position of the fourth line found during light localization. */
	public static double YTheta_Plus = 0; //Initializing theta variables
	
	/** The Y position of the second line found during light localization. */
	public static double YTheta_Minus = 0;
	
	/** The X position of the first line found during light localization. */
	public static double XTheta_Plus = 0;
	
	/**The X position of the third line found during light localization. */
	public static double XTheta_Minus = 0;
	
	/** The difference between the current heading and the actual zero heading.
	 * deltaTheta is used during the Light Localization */
	public static double deltaTheta = 0;
	
	/** Angles used during falling edge routine of Ultrasonic Localization */
	public static double angleA, angleB;
	
	/** Counter to hold the amount of lines that have already been clocked during light localization. */
	private int line_count = 0; //Used to count the amount of gridlines the sensor has detected
	
	static final double correction = 18;
	boolean moving = true;
	
	/**
	 * Localization constructor
	 * @param odo the odometer of the robot
	 * @param nav the navigation system for the robot
	 * @param colorSensor the color sensor to be used in localization
	 * @param colorData the data from the color sensor
	 * @param colorData2
	 * @param leftMotor the robot's left wheel motor
	 * @param rightMotor the robot's right wheel motor
	 * @param usValue
	 * @param usSensor the ultrasonic sensor used for localization
	 * @param usData the data used for the ultrasonic sensor
	 */
	public Localization(Odometer odo, Navigation nav, SampleProvider colorSensorF, float[] colorData, 
			float[] colorData2, EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor, SampleProvider usValue, SensorModes usSensor, float[] usData) {
		this.odo = odo;
		this.colorSensorF = colorSensorF;
		this.colorData = colorData;
		this.colorData2 = colorData2;
		this.usSensor = usSensor;
		this.usData = usData;
		this.usValue = usValue;
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		this.nav = nav;
	}
	/**
	 * The method should first perform a ultrasonic localization to find the
	 *  direction to the perpendicular cross of the grid lines. The robot
	 *  should then move to that cross and past it in order to make the
	 *  robot within reach of all four grid lines that need to be clocked. 
	 *  The robot should then rotate until it finds four grid lines and then 
	 *  take note of all of them using the light sensor. Finally the brick 
	 *  should perform trig to find both the error in position and heading 
	 *  and then move to a heading and position of zero.
	 * 
	 */
	public void doLocalization() {

		double [] pos = new double [3];
		// rotate the robot until it sees no wall
		while(wallDetected()){ //while robot sees a wall:
			//rotate until wallDetected is false --> until it sees no wall
			turnClockwise(); //clockwise rotation
		}
		// keep rotating until the robot sees a wall, then latch the angle
		while(!wallDetected()){ //while robot doesn't see a wall:
			//rotate until robot sees a wall (until !wallDetected is false)
			turnClockwise(); //clockwise rotation
		}
	
		if(wallDetected()){//stop motors to give it time to latch angle
			leftMotor.setSpeed(0); //set speeds to zero for both because stop() doesnt do both motors simultaneously
			rightMotor.setSpeed(0);
			leftMotor.forward();
			rightMotor.forward();
			leftMotor.stop();
			rightMotor.stop();
		}

		//latch angle A:
		angleA = odo.getAng(); //get robot's angle from odometer

		// switch direction and wait until it sees no wall
		while(wallDetected()){ //while robot sees a wall:
			//rotate until wallDetected is false --> until it sees no wall
			turnCounterClockwise();  //switch direction to counterclockwise rotation
		}

		// keep rotating until the robot sees a wall, then latch the angle
		while(!wallDetected()){ //while robot doesn't see a wall:
			//rotate until robot sees a wall (until !wallDetected is false)
			turnCounterClockwise();  //counterclockwise rotation
		}

		if(wallDetected()){//stop motors to give it time to latch angle
			leftMotor.setSpeed(0); //set speeds to zero for both because stop() doesnt do both motors simultaneously
			rightMotor.setSpeed(0);
			leftMotor.forward();
			rightMotor.forward();
			leftMotor.stop();
			rightMotor.stop();
		}

		//latch angle B:
		angleB = odo.getAng(); //get robot's angle from odometer

		// angleA is clockwise from angleB, so assume the average of the
		// angles to the right of angleB is 45 degrees past 'north'

		//use formula from tutorial to determine delta theta:
		if(angleA < angleB){
			dTheta = 230 - ((angleA+angleB)/2);
		}
		else if(angleA > angleB){
			dTheta = 43 - ((angleA+angleB)/2);
		}

		//dTheta is the angle to be added to the heading reported by odometer:
		pos[2] = (dTheta) + odo.getAng(); //pos[2] is where we update the angle

		// update the odometer position (example to follow:)
		boolean[] updates = {false,false,true}; //booleans indicating if x,y,theta are being updated
		//only theta is being updated so index 2 is true but x and y remain 0
		odo.setPosition(pos, updates);
		nav.turnToSmart(55);
//		Sound.buzz();
		
		odo.setAng(45);

		
		//LIGHT:		
		while(moving){
			leftMotor.rotate(convertDistance(WHEEL_RADIUS, 600), true);
			rightMotor.rotate(convertDistance(WHEEL_RADIUS, 600), true);
			this.colorSensorF.fetchSample(this.colorData2, 0);
			int light_val = (int)(this.colorData2[0]*100);
			if(light_val <= 28){
				moving = false;
			}
		}
		
		//After seeing line, move forward 5
		leftMotor.rotate(convertDistance(WHEEL_RADIUS,7), true);
		rightMotor.rotate(convertDistance(WHEEL_RADIUS,7), false);
		odo.setAng(0);
		//Set robot to rotate through 360 degrees clockwise:
		leftMotor.setSpeed(ROTATION_SPEED); 	
		rightMotor.setSpeed(ROTATION_SPEED); 
		leftMotor.rotate(convertAngle(WHEEL_RADIUS, TRACK, 360), true);
		rightMotor.rotate(-convertAngle(WHEEL_RADIUS, TRACK, 360), true);
		
		//While rotating, get LS data:
		while(line_count < 4){
			// Acquire Color Data from sensor, store it in the array at the position
			// of the line it is currently at.
			this.colorSensorF.fetchSample(this.colorData, line_count);
			int light_val = (int)((this.colorData[line_count])*100);

			// Progress through the lines as they are detected and record the angles.
			// As the cart starts at zero from the US localization, it is not necessary
			// for us to consider the angle looping around: Our angles will always
			// be within [0, 360].
		
			if(line_count == 0 && light_val <= 32){
				XTheta_Plus = odo.getAng();
				line_count++; //Increment the line counter
				Sound.beep();
			}
			else if(line_count == 1 && light_val <= 32){
				YTheta_Minus = odo.getAng();
				line_count++;
				Sound.beep();
			}
			else if(line_count == 2 && light_val <= 32){
				XTheta_Minus = odo.getAng();
				line_count++;
				Sound.beep();
			}			
			else if(line_count == 3 && light_val <= 32){
				YTheta_Plus = odo.getAng();
				line_count++;
				Sound.beep();
			}
		}

		// Do trigonometry to compute (x, y) position and fix angle, as specified
		// in the tutorial slides
		double x_pos = 0, y_pos = 0; 
		double theta_y = 0, theta_x = 0;

		//Calculation of total angle subtending the axes
		theta_y = YTheta_Plus-YTheta_Minus;
		theta_x = XTheta_Minus - XTheta_Plus;

		//Calculation of the x and t positions considering that we are in the 3rd quadrant (in negative x and y coords):
		x_pos = (SENSOR_DIST)*Math.cos(Math.toRadians(theta_y/2)); 
		y_pos = (SENSOR_DIST)*Math.cos(Math.toRadians(theta_x/2));
		
		deltaTheta = 90 + (theta_y/2) - (YTheta_Plus - 180);
		
		odo.setX(x_pos);
		odo.setY(y_pos);
		
		/*odo.setAng(odo.getAng()+deltaTheta); is original code*/
		odo.setAng(odo.getAng()+deltaTheta);
				
		// When done, travel to (0,0) and turn to 0 degrees:
		nav.travelToDiag(0, 0); 
		nav.turnToSmart(0);
		
		
		odo.setAng(0);
	}
	/**
	 * Turn the robot clockwise when called until told to stop.
	 */
	public void turnClockwise(){//robot turns clockwise 
		leftMotor.setSpeed(225);
		rightMotor.setSpeed(225);	
		leftMotor.forward();
		rightMotor.backward();
	}
	
	/**
	 * Turn the robot counter clockwise when called until told to stop.
	 */
	public void turnCounterClockwise(){ //robot turns counterclockwise
		leftMotor.setSpeed(225);
		rightMotor.setSpeed(225);	
		leftMotor.backward();
		rightMotor.forward();
	}
	/**
	 * Test whether or not the Ultrasonic sensor has seen the wall or not.
	 * @return true if there has been a walled detected and false if it has not
	 */
	public boolean wallDetected(){
		//if getFilteredData is less than 50, a wall is detected, so return true
		return getFilteredData()<30;
	}
	
	/**
	 * Should first poll the ultrasonic sensor for data and then perform a filtering
	 * of it which sets the distance to be equal to 255 cm if the polled distance is
	 * larger than 50 cm.
	 * 
	 * @return the filtered ultrasonic sensor distance
	 */
	private float getFilteredData() {
		usSensor.fetchSample(usData, 0);
		float distance = usData[0];
		distance = (int)(usData[0]*100.0);
		// Rudimentary filter
		if (distance > 50){
			distance = 255;
		}
		return distance;
	}

	/**
	 * The method should convert the input distance into a form that is equal to
	 * the amount of rotation that a wheel of the given radius must rotate
	 * in order to move that distance
	 * @param radius the radius of the wheels of the robot
	 * @param distance the distance which will be converted
	 * @return the converted distance
	 */
	private static int convertDistance(double radius, double distance) {
		//convertDistance method: It takes the radius of the wheel and the 
		//distance required to travel and calculates the required wheel rotation
		return (int) ((180.0 * distance) / (Math.PI * radius));
	}

	/**
	 * The method should convert the input angle into a form that can be performed
	 * by the robot with the given wheel radius and width.
	 * 
	 * @param radius the radius of the wheel
	 * @param width the width of the robot
	 * @param angle the angle to be converted
	 * @return the angle now in the form of amount of rotation needed by the robot's wheel to perform that angle of turn
	 */
	private static int convertAngle(double radius, double width, double angle) {
		//convertAngle method: This method takes the radius of wheel, width of cart 
		//and the angle required to be turned and calculated the required wheel rotation
		return convertDistance(radius, Math.PI * width * angle / 360.0);
	}


}