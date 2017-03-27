package finalProject;
import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;

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

	private double SENSOR_DIST = 6.5;
	private double dTheta; 	//delta theta 
	private static final int FORWARD_SPEED = WiFiExample.FORWARD_SPEED;
	private static final int ROTATION_SPEED = WiFiExample.ROTATE_SPEED;
	double WHEEL_RADIUS = WiFiExample.WHEEL_RADIUS;
	double TRACK =  WiFiExample.TRACK;
	public static double YTheta_Plus = 0; //Initializing theta variables
	public static double YTheta_Minus = 0;
	public static double XTheta_Plus = 0;
	public static double XTheta_Minus = 0;
	public static double deltaTheta = 0;
	public static double angleA, angleB;
	
	private int line_count = 0; //Used to count the amount of gridlines the sensor has detected
	static final double correction = 18;
	boolean moving = true;
	
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

	public void doLocalization(int fwdCorner) {

		double [] pos = new double [3];
		// rotate the robot until it sees no wall
		while(wallDetected()){ //while robot sees a wall:
			
			//rotate until wallDetected is false --> until it sees no wall
			turnClockwise(); //clockwise rotation
		}
	//	Sound.beep();//make sound to indicate we have successfully rotated away from wall

		// keep rotating until the robot sees a wall, then latch the angle
		while(!wallDetected()){ //while robot doesn't see a wall:
			//rotate until robot sees a wall (until !wallDetected is false)
			turnClockwise(); //clockwise rotation
		}
	//	Sound.beepSequenceUp(); //make sound to indicate we now see a wall

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
//		Sound.beepSequence();//make sound to indicate we have successfully rotated away from wall

		// keep rotating until the robot sees a wall, then latch the angle
		while(!wallDetected()){ //while robot doesn't see a wall:
			//rotate until robot sees a wall (until !wallDetected is false)
			turnCounterClockwise();  //counterclockwise rotation
		}
//		Sound.twoBeeps(); //make sound to indicate we now see a wall

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
	public void turnClockwise(){//robot turns clockwise 
		leftMotor.setSpeed(225);
		rightMotor.setSpeed(225);	
		leftMotor.forward();
		rightMotor.backward();
	}
	public void turnCounterClockwise(){ //robot turns counterclockwise
		leftMotor.setSpeed(225);
		rightMotor.setSpeed(225);	
		leftMotor.backward();
		rightMotor.forward();
	}
	public boolean wallDetected(){
		//if getFilteredData is less than 50, a wall is detected, so return true
		return getFilteredData()<30;
	}
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
	//convertDistance method: It takes the radius of the wheel and the distance required to travel and calculates the required wheel rotation
	private static int convertDistance(double radius, double distance) {
		return (int) ((180.0 * distance) / (Math.PI * radius));
	}
	//convertAngle method: This method takes the radius of wheel, width of cart and the angle required to be turned and calculated the required wheel rotation
	private static int convertAngle(double radius, double width, double angle) {
		return convertDistance(radius, Math.PI * width * angle / 360.0);
	}


}