package finalProject;
import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;

public class Localization {
	private Odometer odo;
	private Navigation nav;
	private SampleProvider colorSensor;
	private SampleProvider usValue;
	private SensorModes usSensor;
	private float[] usData;
	private EV3LargeRegulatedMotor leftMotor, rightMotor;
	private float[] colorData;	
	private float[] colorData2;

	private double SENSOR_DIST = 6.0;
	private double dTheta; 	//delta theta 
	private static final int FORWARD_SPEED = WiFiExample.FORWARD_SPEED;
	private static final int ROTATION_SPEED = WiFiExample.ROTATE_SPEED;
	double WHEEL_RADIUS = WiFiExample.WHEEL_RADIUS;
	double TRACK =  WiFiExample.TRACK;
	public static double YTheta_Plus = 0; //Initializing theta variables
	public static double YTheta_Minus = 0;
	public static double XTheta_Plus = 0;
	public static double XTheta_Minus = 0;
	private static double deltaTheta = 0;

	private int line_count = 0; //Used to count the amount of gridlines the sensor has detected
	static final double correction = 18;
	boolean moving = true;

	public Localization(Odometer odo, Navigation nav, SampleProvider colorSensor, float[] colorData, 
			float[] colorData2, EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor, SampleProvider usValue, SensorModes usSensor, float[] usData) {
		this.odo = odo;
		this.colorSensor = colorSensor;
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
		
//		double [] pos = new double [3];
//		double angleA, angleB;
//		// rotate the robot until it sees no wall
//		while(wallDetected()){ //while robot sees a wall:
//			//rotate until wallDetected is false --> until it sees no wall
//			turnClockwise(); //clockwise rotation
//		}
//		Sound.beep();//make sound to indicate we have successfully rotated away from wall
//
//		// keep rotating until the robot sees a wall, then latch the angle
//		while(!wallDetected()){ //while robot doesn't see a wall:
//			//rotate until robot sees a wall (until !wallDetected is false)
//			turnClockwise(); //clockwise rotation
//		}
//		Sound.beepSequenceUp(); //make sound to indicate we now see a wall
//
//		if(wallDetected()){//stop motors to give it time to latch angle
//			leftMotor.setSpeed(0); //set speeds to zero for both because stop() doesnt do both motors simultaneously
//			rightMotor.setSpeed(0);
//			leftMotor.forward();
//			rightMotor.forward();
//			leftMotor.stop();
//			rightMotor.stop();
//		}
//
//		//latch angle A:
//		angleA = odo.getAng(); //get robot's angle from odometer
//
//		// switch direction and wait until it sees no wall
//		while(wallDetected()){ //while robot sees a wall:
//			//rotate until wallDetected is false --> until it sees no wall
//			turnCounterClockwise();  //switch direction to counterclockwise rotation
//		}
//		Sound.beepSequence();//make sound to indicate we have successfully rotated away from wall
//
//		// keep rotating until the robot sees a wall, then latch the angle
//		while(!wallDetected()){ //while robot doesn't see a wall:
//			//rotate until robot sees a wall (until !wallDetected is false)
//			turnCounterClockwise();  //counterclockwise rotation
//		}
//		Sound.twoBeeps(); //make sound to indicate we now see a wall
//
//		if(wallDetected()){//stop motors to give it time to latch angle
//			leftMotor.setSpeed(0); //set speeds to zero for both because stop() doesnt do both motors simultaneously
//			rightMotor.setSpeed(0);
//			leftMotor.forward();
//			rightMotor.forward();
//			leftMotor.stop();
//			rightMotor.stop();
//		}
//
//		//latch angle B:
//		angleB = odo.getAng(); //get robot's angle from odometer
//
//		// angleA is clockwise from angleB, so assume the average of the
//		// angles to the right of angleB is 45 degrees past 'north'
//
//		//use formula from tutorial to determine delta theta:
//		if(angleA < angleB){
//			dTheta = 230 - ((angleA+angleB)/2);
//		}
//		else if(angleA > angleB){
//			dTheta = 230 - ((angleA+angleB)/2);
//		}
//
//		//dTheta is the angle to be added to the heading reported by odometer:
//		pos[2] = (dTheta) + odo.getAng(); //pos[2] is where we update the angle
//
//		// update the odometer position (example to follow:)
//		boolean[] updates = {false,false,true}; //booleans indicating if x,y,theta are being updated
//		//only theta is being updated so index 2 is true but x and y remain 0
//		odo.setPosition(pos, updates);
//		nav.turnToSmart(45);
//		Sound.buzz();
//		
//
//		//LIGHT:
//		while(moving){
//			leftMotor.rotate(convertDistance(WHEEL_RADIUS, 600), true);
//			rightMotor.rotate(convertDistance(WHEEL_RADIUS, 600), true);
//			this.colorSensor.fetchSample(this.colorData2, 0);
//			int light_val = (int)(this.colorData2[0]*100);
//			if(light_val <= 28){
//				moving = false;
//			}
//		}
//		Sound.beep();
//		
//		//After seeing line, move forward 5
//		leftMotor.rotate(convertDistance(WHEEL_RADIUS,10), true);
//		rightMotor.rotate(convertDistance(WHEEL_RADIUS,10), false);

		//Set robot to rotate through 360 degrees clockwise:
		leftMotor.setSpeed(ROTATION_SPEED); 	
		rightMotor.setSpeed(ROTATION_SPEED); 
		leftMotor.rotate(convertAngle(WHEEL_RADIUS, TRACK, 360), true);
		rightMotor.rotate(-convertAngle(WHEEL_RADIUS, TRACK, 360), true);
		
		//While rotating, get LS data:
		while(line_count < 4){
			// Acquire Color Data from sensor, store it in the array at the position
			// of the line it is currently at.
			this.colorSensor.fetchSample(this.colorData, line_count);
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
		x_pos = -(SENSOR_DIST)*Math.cos(Math.toRadians(theta_y/2)); 
		y_pos = -(SENSOR_DIST)*Math.cos(Math.toRadians(theta_x/2));
		Sound.buzz();
		deltaTheta = 90-((theta_y/2) - (XTheta_Minus-180));
		odo.setX(x_pos);
		odo.setY(y_pos);
		odo.setAng(odo.getAng()+deltaTheta);
		//this.odo.setPosition(new double[] {x_pos,y_pos, deltaTheta+odo.getAng()},new boolean[] {true,true,false});

		// When done, travel to (0,0) and turn to 0 degrees:
		//this doesn't work, fix it:
		nav.travelTo(0, 0); 
		Sound.buzz();
	
		nav.turnToSmart(0);
		Sound.beep();
		


		//		//LIGHT LOCALIZATION:
		//		while(helper == true){ //go to intersection of lines
		//			rightMotor.setSpeed(80);
		//			leftMotor.setSpeed(80);
		//			leftMotor.forward();
		//			rightMotor.forward();
		//			colorSensor.fetchSample(colorData, 0); 
		//			int light_val = (int)(colorData[0]*100);
		//			if(light_val <= 28){
		//				Sound.buzz();
		//				firstLinePos = odo.getX();
		//				helper = false;
		//				break;
		//			}
		//		}
		//		nav.drive(5);		
		//
		//		//start rotating and clock all 4 gridlines
		//		//here we have the robot rotate until it finds four grid lines
		//		//while doing this, it takes account of the position and angle of all of the points
		//		//these numbers will be used to calculate the correct position and angle of the robot when it finishes
		//		while(moreHelp < 4)
		//		{
		//			rightMotor.setSpeed(80);
		//			leftMotor.setSpeed(80);
		//
		//			rightMotor.backward();
		//			leftMotor.forward();
		//			colorSensor.fetchSample(colorData, 0); 
		//			int light_val = (int)(colorData[0]*100);
		//			if(light_val <= 28)
		//			{
		//				Sound.beep();
		//				lineLocationsX[moreHelp] = odo.getX();
		//				lineLocationsY[moreHelp] = odo.getY();
		//				thetaLocations[moreHelp] = odo.getAng();
		//				moreHelp++;
		//			}
		//		}
		//		Sound.beepSequenceUp();
		//		
		//		//we use the functions given in the lecture slides to calculate the values needed for the localization
		//		//we then have the robot move to the position and turn to the correct heading
		//		double deltaY = thetaLocations[0] - thetaLocations[2];
		//		double deltaX = thetaLocations[3] - thetaLocations[1];
		//		double x = -sensorDist*Math.cos(deltaY/2);
		//		double y = -sensorDist*Math.cos(deltaX/2);
		//		double deltaTheta = (90 + -(deltaY/2  - (thetaLocations[3] - 180)));
		//		
		//		odo.setPosition(new double [] {x, y, deltaTheta}, new boolean [] {true, true, true});
		//		nav.travelTo(0, 0);
		//		nav.turnTo(-deltaTheta);
		//

	}
	public void turnClockwise(){//robot turns clockwise 
		leftMotor.setSpeed(ROTATION_SPEED);
		rightMotor.setSpeed(ROTATION_SPEED);	
		leftMotor.forward();
		rightMotor.backward();
	}
	public void turnCounterClockwise(){ //robot turns counterclockwise
		leftMotor.setSpeed(ROTATION_SPEED);
		rightMotor.setSpeed(ROTATION_SPEED);	
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
