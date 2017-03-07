package finalProject;
import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.SampleProvider;

public class LightLocalizer {
	private Odometer odo;
	private Navigation nav;
	private SampleProvider colorSensor;
	private EV3LargeRegulatedMotor leftMotor, rightMotor;
	private float[] colorData;	
	private double firstLinePos;
	private double secondLinePos;
	private boolean helper = true;
	private int moreHelp = 0;
	private double[] lineLocationsX;
	private double[] lineLocationsY;
	private double[] thetaLocations;
	private double d = 6.0;
	
	public LightLocalizer(Odometer odo, Navigation nav, SampleProvider colorSensor, float[] colorData, EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor) {
		this.odo = odo;
		this.colorSensor = colorSensor;
		this.colorData = colorData;
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		this.nav = nav;
		this.lineLocationsX = new double [4];
		this.lineLocationsY = new double [4];
		this.thetaLocations = new double [4];
	}
	
	public void doLocalization() 
	{
		// drive to location listed in tutorial
		float[] colorSample = {0};
		//first we need the robot to find a place in which it's sensor can see all three of the lines on the grid
		//we do this by finding a line with the sensor, turning right, then findin a second one
		//we then get the robot to go to the intersection of those lines and then move forward past it
		//this places the sensor in a position where it can hit all four lines
		while(helper = true)
		{
			rightMotor.setSpeed(60);
			leftMotor.setSpeed(60);
			
			leftMotor.forward();
			rightMotor.forward();
			colorSensor.fetchSample(colorSample, 0); 
			int light_val = (int)(colorSample[0]*100);
			if(light_val <= 28)
			{
				Sound.buzz();
				firstLinePos = odo.getX();
				helper = false;
				break;
			}
		}
		nav.travelTo(odo.getX(), odo.getY() + .05);
		helper = true;
		nav.turnTo(90.0, true);
		while(helper = true)
		{
			rightMotor.setSpeed(60);
			leftMotor.setSpeed(60);
			
			leftMotor.forward();
			rightMotor.forward();
			colorSensor.fetchSample(colorSample, 0); 
			int light_val = (int)(colorSample[0]*100);
			if(light_val <= 28)
			{
				Sound.buzz();
				secondLinePos = odo.getY();
				helper = false;
				break;
			}
		}
		nav.travelTo(firstLinePos, secondLinePos);
		nav.turnTo(40.0, true);
		nav.goForward(15);
		
		
		
		// start rotating and clock all 4 gridlines
		//here we have the robot rotate until it finds four grid lines
		//while doing this, it takes account of the position and angle of all of the points
		//these numbers will be used to calculate the correct position and angle of the robot when it finishes
		while(moreHelp < 4)
		{
			rightMotor.setSpeed(60);
			leftMotor.setSpeed(60);
		
			leftMotor.backward();
			rightMotor.forward();
			colorSensor.fetchSample(colorSample, 0); 
			int light_val = (int)(colorSample[0]*100);
			if(light_val <= 28)
			{
				Sound.beep();
				lineLocationsX[moreHelp] = odo.getX();
				lineLocationsY[moreHelp] = odo.getY();
				thetaLocations[moreHelp] = odo.getAng();
				moreHelp++;
			}
		}
			nav.turnTo(0.0, true);
			//we use the functions given in the lecture slides to calculate the values needed for the localization
			//we then have the robot move to the position and turn to the correct heading
			double deltaY = thetaLocations[0] - thetaLocations[2];
			double deltaX = thetaLocations[3] - thetaLocations[1];
			double x = -d*Math.cos(deltaY/2);
			double y = -d*Math.cos(deltaX/2);
			double deltaTheta = (90 + -(deltaY/2  - (thetaLocations[3] - 180)));
			odo.setPosition(new double [] {x, y, deltaTheta}, new boolean [] {true, true, true});
			nav.travelTo(0, 0);
			nav.turnTo(0.0, true);
		
		
		
		// do trig to compute (0,0) and 0 degrees
		// when done travel to (0,0) and turn to 0 degrees
	}

}
