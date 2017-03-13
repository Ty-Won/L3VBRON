package finalProject;
import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;

public class LightLocalizer {
	private Odometer odo;
	private Navigation nav;
	private SampleProvider colorSensor;
	private SampleProvider usValue;
	private SensorModes usSensor;
	private float[] usData;
	private EV3LargeRegulatedMotor leftMotor, rightMotor;
	private float[] colorData;	
	private double firstLinePos;
	private double secondLinePos;
	private boolean helper = true;
	private int moreHelp = 0;
	private double[] lineLocationsX;
	private double[] lineLocationsY;
	private double[] thetaLocations;
	private double sensorDist = 9;
	private double dTheta; 	//delta theta 
	private static final int FORWARD_SPEED = WiFiExample.FORWARD_SPEED;
	private static final int ROTATION_SPEED = WiFiExample.ROTATE_SPEED;
	
	public LightLocalizer(Odometer odo, Navigation nav, SampleProvider colorSensor, float[] colorData, 
			EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor, SampleProvider usValue, SensorModes usSensor, float[] usData) {
		this.odo = odo;
		this.colorSensor = colorSensor;
		this.colorData = colorData;
		this.usSensor = usSensor;
		this.usData = usData;
		this.usValue = usValue;
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		this.nav = nav;
		this.lineLocationsX = new double [4];
		this.lineLocationsY = new double [4];
		this.thetaLocations = new double [4];
	}
	
	public void doLocalization(int fwdCorner) {
		double [] pos = new double [3];
		double angleA, angleB;
		// rotate the robot until it sees no wall
		while(wallDetected()){ //while robot sees a wall:
			//rotate until wallDetected is false --> until it sees no wall
			turnClockwise(); //clockwise rotation
		}
		Sound.beep();//make sound to indicate we have successfully rotated away from wall
		
		// keep rotating until the robot sees a wall, then latch the angle
		while(!wallDetected()){ //while robot doesn't see a wall:
			//rotate until robot sees a wall (until !wallDetected is false)
			turnClockwise(); //clockwise rotation
		}
		Sound.beepSequenceUp(); //make sound to indicate we now see a wall
		
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
		Sound.beepSequence();//make sound to indicate we have successfully rotated away from wall
		
		// keep rotating until the robot sees a wall, then latch the angle
		while(!wallDetected()){ //while robot doesn't see a wall:
			//rotate until robot sees a wall (until !wallDetected is false)
			turnCounterClockwise();  //counterclockwise rotation
		}
		Sound.twoBeeps(); //make sound to indicate we now see a wall
		
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
			dTheta = 45 - ((angleA+angleB)/2);
		}
		else if(angleA > angleB){
			dTheta = 225 - ((angleA+angleB)/2);
		}
		
		//dTheta is the angle to be added to the heading reported by odometer:
		pos[2] = dTheta + odo.getAng(); //pos[2] is where we update the angle
		
		// update the odometer position (example to follow:)
		boolean[] updates = {false,false,true}; //booleans indicating if x,y,theta are being updated
		//only theta is being updated so index 2 is true but x and y remain 0
		odo.setPosition(pos, updates);
		nav.turnTo(0);
		Sound.buzz();
		nav.turnTo(-45);
		
		
		//LIGHT LOCALIZATION:
		while(helper == true){ //go to intersection of lines
			rightMotor.setSpeed(80);
			leftMotor.setSpeed(80);
			leftMotor.forward();
			rightMotor.forward();
			colorSensor.fetchSample(colorData, 0); 
			int light_val = (int)(colorData[0]*100);
			if(light_val <= 28){
				Sound.buzz();
				firstLinePos = odo.getX();
				helper = false;
				break;
			}
		}
		nav.drive(5);

		// start rotating and clock all 4 gridlines
		//here we have the robot rotate until it finds four grid lines
		//while doing this, it takes account of the position and angle of all of the points
		//these numbers will be used to calculate the correct position and angle of the robot when it finishes
		while(moreHelp < 4)
		{
			rightMotor.setSpeed(80);
			leftMotor.setSpeed(80);

			leftMotor.backward();
			rightMotor.forward();
			colorSensor.fetchSample(colorData, 0); 
			int light_val = (int)(colorData[0]*100);
			if(light_val <= 28)
			{
				Sound.beep();
				lineLocationsX[moreHelp] = odo.getX();
				lineLocationsY[moreHelp] = odo.getY();
				thetaLocations[moreHelp] = odo.getAng();
				moreHelp++;
			}
		}
		Sound.beepSequenceUp();
		
		//we use the functions given in the lecture slides to calculate the values needed for the localization
		//we then have the robot move to the position and turn to the correct heading
		double deltaY = thetaLocations[0] - thetaLocations[2];
		double deltaX = thetaLocations[3] - thetaLocations[1];
		double x = -sensorDist*Math.cos(deltaY/2);
		double y = -sensorDist*Math.cos(deltaX/2);
		double deltaTheta = (90 + -(deltaY/2  - (thetaLocations[3] - 180)));
		odo.setPosition(new double [] {x, y, deltaTheta}, new boolean [] {true, true, true});
		nav.travelTo(0, 0);
		nav.turnTo(-deltaTheta);

		
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

}
