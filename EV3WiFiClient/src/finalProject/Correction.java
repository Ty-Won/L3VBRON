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

		private EV3LargeRegulatedMotor leftMotor, rightMotor;

		private double SENSOR_DIST = 6.5;
        private final double angleThreshold = 40;  	
        private double tilelength = 30.48;

		
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

		public boolean correcting = false; 
		public boolean leftline = false;
		public boolean rightline= false; 
		public boolean turning = Navigation.turning;
		
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

		turning=nav.isTurning();
			while(turning==true){
				try {
					turning=nav.isTurning();
					Sound.beepSequenceUp();
					Thread.sleep(500);
				} catch (InterruptedException e) {}
			}				
		LightCorrection();
			
		}
		
		//Travel orientation correct, uses light sensors on the side of the robot to detect grid lines, if one side detects a line first,
		//robot adjusts motors to correct the orientation of the robot
		public void LightCorrection (){
		correcting = true; 
	    leftMotor.setSpeed(FORWARD_SPEED);
        rightMotor.setSpeed(FORWARD_SPEED);
        
        leftline = false;
        rightline= false; 
		
		Sound.twoBeeps();
		while(leftline == false && rightline == false){
			leftline = lineDetected(colorSensorL, colorDataL);
			rightline = lineDetected(colorSensorR, colorDataR);
			
			turning=nav.isTurning();
			while(turning==true){
				turning=nav.isTurning();
				try {Sound.buzz();
					Thread.sleep(500);
				} catch (InterruptedException e) {}
			}
			}
		
		if(leftline == true && rightline ==true){
			updateOdo();
			
			turning=nav.isTurning();
			while(turning==true){
				turning=nav.isTurning();
				try {Sound.buzz();
					Thread.sleep(500);
				} catch (InterruptedException e) {}
			}
				
			run();
		}
		
		if(leftline == true){
			do{ 
				leftMotor.setSpeed(10);
				rightline = lineDetected(colorSensorR, colorDataR);
			} while (rightline == false);
			
			leftMotor.setSpeed(FORWARD_SPEED);
			updateOdo();
			
			turning=nav.isTurning();
			while(turning==true){
				turning=nav.isTurning();
				try {Sound.buzz();
					Thread.sleep(500);
				} catch (InterruptedException e) {}
			}

			run();
			}
		
		else if(rightline == true){
			do{ 
				rightMotor.setSpeed(10);
				leftline = lineDetected(colorSensorL, colorDataL);
			} while (leftline == false);
			
		    rightMotor.setSpeed(FORWARD_SPEED);
			updateOdo();
			
			turning=nav.isTurning();
			while(turning==true){
				turning=nav.isTurning();
				try {Sound.buzz();
					Thread.sleep(500);
				} catch (InterruptedException e) {}
			}
			
			run();

			}
		
		}
		
		public void updateOdo(){
        	// get the x and y position read by the odometry
        	double x = odo.getX();
        	double y = odo.getY();
        	
        	double line;
        	double position; 
        	
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
		
	public boolean lineDetected(SampleProvider colorSensor, float[] colorData){
		colorSensor.fetchSample(colorData, 0);
		int light_val = (int)((colorData[0])*100);
		if(light_val <= 32){
			return true;
		}
		else
			return false;
	}
	
	
}

