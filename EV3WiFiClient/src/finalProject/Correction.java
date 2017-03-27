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
		private float[] colorDataF = {0};

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

		turning=nav.isTurning();
			while(turning==true){
				try {
					turning=nav.isTurning();
					Sound.beepSequenceUp();
					Thread.sleep(500);
				} catch (InterruptedException e) {}
			}
		Sound.beepSequenceUp();
		if(gridcount==2){
			localize();
			gridcount=0;	
		}
		Sound.beepSequenceUp();	
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
		gridcount++;
		
		if(leftline == true && rightline ==true){
			updateOdo();
			
			turning=nav.isTurning();
			while(turning==true){
				turning=nav.isTurning();
				try {Sound.buzz();
					Thread.sleep(500);
				} catch (InterruptedException e) {}
			}
			correcting = false;	
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
			correcting = false;
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
			correcting = false;
			run();

			}
		
		}
		
		public void localize(){

//			while(moving){
//				leftMotor.rotate(convertDistance(WHEEL_RADIUS, 600), true);
//				rightMotor.rotate(convertDistance(WHEEL_RADIUS, 600), true);
//				this.colorSensorF.fetchSample(this.colorDataF, 0);
//				int light_val = (int)(this.colorDataF[0]*100);
//				if(light_val <= 28){
//					moving = false;
//				}
//			}
			int line_count=0;
			double X_ini=0;
			double Y_ini=0;
			double Ang_ini=0;
			double Dest_ini[]={0};
			
			Ang_ini=odo.getAng();
			X_ini=odo.getX();
			Y_ini=odo.getY();
			Dest_ini=nav.getDest();
			
			nav.turnToSmart(45);
			
			localizing = true;
			
			//After seeing line, move forward 4
			leftMotor.rotate(convertDistance(WHEEL_RADIUS,4), true);
			rightMotor.rotate(convertDistance(WHEEL_RADIUS,4), false);
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
				this.colorSensorF.fetchSample(this.colorDataF, line_count);
				int light_val = (int)((this.colorDataF[line_count])*100);

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
			theta_x = XTheta_Minus-XTheta_Plus;

			//Calculation of the x and t positions considering that we are in the 3rd quadrant (in negative x and y coords):
			x_pos = (SENSOR_DIST)*Math.cos(Math.toRadians(theta_y/2)); 
			y_pos = (SENSOR_DIST)*Math.cos(Math.toRadians(theta_x/2));
			
			deltaTheta = 90 + (theta_y/2) - (YTheta_Plus - 180);
			
			odo.setX(x_pos);
			odo.setY(y_pos);
			
			/*odo.setAng(odo.getAng()+deltaTheta); is original code*/
			odo.setAng(odo.getAng()+deltaTheta);
					
			// When done, travel to (0,0) and turn to 0 degrees:
			nav.travelToDiag(0,0); 
			nav.turnToSmart(0);
			
			double[] nearestIntersection={0};
			nearestIntersection=getIntersection(X_ini, Y_ini);
			odo.setPosition(nearestIntersection, new boolean[]{true, true, true});
			localizing = false;
			nav.travelTo(Dest_ini[0], Dest_ini[1]);
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
		
	public boolean islocalizing(){
		return localizing;
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
	
	public double[] getIntersection(double x, double y){
		double[] intersection={0.0,0.0,0.0};
		double lineX = (int)(x+tilelength/2)/tilelength;
		double lineY = (int)(x+tilelength/2)/tilelength;
		
		intersection[0]=lineX*tilelength;
		intersection[1]=lineY*tilelength;
		intersection[2]=0.0;
		
		return intersection;
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

