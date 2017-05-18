package finalProject;

import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;


/**
 * The Forward class is the controlling class which will be called
 * after the initial localization process of the robot has been completed.
 * The robot will be told where the ball dispenser is and told to go to that position
 * and move to an orientation and position from which it can receive a ball.
 * The robot will then lower its arm and receive the ball before traveling
 * to the edge of the forward line and shooting the ball at the target.
 * Once this has been completed, the robot should repeat the process for the
 * duration of the round.
 * During all movements, the correction and obstacle avoidance algorithms
 * should be called continuously to ensure the robot moves to exactly the right
 * position without hitting any obstacles.
 * 
 * @author Ilana Haddad
 * @version 1.0
 *
 */
public class Forward {
	private int corner;	//starting corner
	private int fwdLinePosition;
	private int w1; //defender zone dimension w1
	private int w2; //defender zone dimension w2
	private int bx; //ball dispenser position x
	private int by; //ball dispenser position y
	private String omega; //ball dispenser orientation 
	private final double TILE_LENGTH = 30.48;
	private final double ROBOT_FRONT_TOCENTER_DIST = 8; //distance from front of robot to center of rotation
	private final int FIELD_DIST = 8; //12
	private final int OUTER_TILES = 2;
	private static final int ROTATE_SPEED = WiFiExample.ROTATE_SPEED;
//	private double track =  WiFiExample.TRACK;
	/** The left motor, which is connected to output A */
	public static final EV3LargeRegulatedMotor leftMotor = WiFiExample.leftMotor;
	/**The right motor, which is connected to output D */
	public static final EV3LargeRegulatedMotor rightMotor = WiFiExample.rightMotor;
	public static final EV3LargeRegulatedMotor launcherMotor = WiFiExample.launcherMotor;
	/**The navigation program for the robot */
	public static Navigation nav = WiFiExample.navigation;
	/** Instantiating odometer of robopt*/
	public static Odometer odo = WiFiExample.odometer;
	/** The Ultrasonic Sensor */
	private static final Port usPort = LocalEV3.get().getPort("S1");
	Launcher launcher = WiFiExample.launcher;
	public static SensorModes usSensor = WiFiExample.usSensor;
	
	public static Correction correction = WiFiExample.correction;
//	/** The motor for the ball launcher */
//	public static Launcher launch =  WiFiExample.launch;
	/** 
	 * The Forward constructor
	 * @param corner the corner which the robot starts in
	 * @param d1 the position of the line separating the forward and defensive zones
	 * @param w1 one of the two coordinates of the bounce zone
	 * @param w2 one of the two coordinates of the bounce zone
	 * @param bx the x coordinate of the ball dispenser
	 * @param by the y coordinate of the ball dispenser
	 * @param omega the orientation of the robot
	 * @param navigation the navigation system of the robot
	 * @param odometer the odometer of the robot
	 */
	public Forward(Navigation navigation, Odometer odometer, int corner, int d1, int w1, int w2, int bx, int by, String omega) {
		this.corner = corner;
		this.fwdLinePosition = d1;
		this.w1 = w1;
		this.w2 = w2;
		this.bx = bx;
		this.by = by;
		this.omega = omega;
		this.nav = navigation;
		this.odo = odometer;
	}
	/**
	 * The start Forward method should begin directly after 
	 * localization and call navigation to move the ball to the dispenser.
	 * At this point, the class should turn the robot to a heading from which
	 * it can receive a ball and lower the arm. Then, when the ball has been received,
	 * the method should call the launcher class to float the launcher arm using the
	 * holding bar to keep it in place. Then, the method should navigate the robot
	 * to the edge of the forward line and call for the ball to be shot toward the 
	 * goal before repeating the process for the remainder of the round.
	 */
	public void startFWD() {

		if(bx==-1){
			bx = 0;
		}
		if(bx == 11){
			bx = 10;
		}
		if(by == -1){
			by = 0;
		}
		int[] field_coord = new int[3]; 	//array that stores field coordinates of the robot's position
		if(corner==1){
			field_coord[0] =0;
			field_coord[1] =0;
			field_coord[2] = 0;
		}
		if(corner==2){
			field_coord[0] =10;
			field_coord[1] =0;
			field_coord[2] = 270;
		}
		if(corner==3){
			field_coord[0] =10;
			field_coord[1] =10;
			field_coord[2] = 180;
		}
		if(corner==4){
			field_coord[0] =0;
			field_coord[1] =10;
			field_coord[2] = 90;
		}
		//update odometer to correct position on field using the field_coord array
		double[] position = {TILE_LENGTH*field_coord[0], TILE_LENGTH*field_coord[1], field_coord[2]};
		odo.setPosition(position, new boolean[]{true,true,true});
		
		//convert bx,by to cm:
		double bx_cm, by_cm;
		bx_cm = bx*TILE_LENGTH;
		by_cm = by*TILE_LENGTH;
			
		//travel to ball dispenser cm coordinates:
		nav.travelTo(bx_cm, by_cm); 

		nav.finishTravel = false;
		if(bx==10){
			nav.turnToSmart(270);
		}
		if(bx == 0){
			nav.turnToSmart(90); //facing away from disp
		}
		if(by==0){
			nav.turnToSmart(0);
		}
		

		//localize forward
		correction.localizeFWD();
		//drive forward a little to correct angle:
		nav.driveWCorrection(16);
		launcher.Enter_Launch_Position();//pulls the arm down
		nav.driveWCorrection(-18.5); //drive back to intersection
		Sound.setVolume(10);
		//beep to indicate robot is ready to receive ball:
		Sound.beep();
		Sound.setVolume(0);
		Sound.pause(5000); 
		nav.driveWCorrection(5);
		launcher.lockArm(); //brings it to middle and locks it
		
		//change track:
		correction.width = 12;
		nav.width = 12;
		odo.TRACK = 12;
		System.out.println(WiFiExample.TRACK);
		nav.driveWCorrection(30.48);

		
		//travel one tile behind forward line IN Y FIRST, localize
		int fwdLine_coord = 10 - fwdLinePosition;
		
		if(fwdLinePosition == 6){
			nav.travelToYFIRST(5*TILE_LENGTH, (fwdLine_coord-2)*TILE_LENGTH);
		}
		else{
			nav.travelToYFIRST(5*TILE_LENGTH, (fwdLine_coord-1)*TILE_LENGTH);
		}
		nav.finishTravel = false;

		nav.travelTo(5*TILE_LENGTH, (fwdLine_coord*TILE_LENGTH) - ROBOT_FRONT_TOCENTER_DIST); //go to forward line
		nav.finishTravel = false;
		
		nav.turnToSmart(0); //face target 
		nav.driveWCorrection(-15);
		
		launcher.prepareToFire();
		Sound.pause(1000);
		launcher.Fire(fwdLinePosition);
		
		correction.width = 10.9;
		nav.width = 10.9;
		WiFiExample.cont.stopSensing = true;
		while(true){
			WiFiExample.correction.localizeForAvoidance(); //goes back until it sees a line and then again 11.6
			
			//travel to ball dispenser cm coordinates:
			nav.travelTo(bx_cm, by_cm); 

			nav.finishTravel = false;
			if(bx==10){
				nav.turnToSmart(270);
			}
			if(bx == 0){
				nav.turnToSmart(90); //facing away from disp
			}
			if(by==0){
				nav.turnToSmart(0);
			}
			

			//localize forward
			correction.localizeFWD();
			//drive forward a little to correct angle:
			nav.driveWCorrection(16);
			launcher.Enter_Launch_Position();//pulls the arm down
			nav.driveWCorrection(-18.5); //drive back to intersection
			Sound.setVolume(10);
			//beep to indicate robot is ready to receive ball:
			Sound.beep();
			Sound.setVolume(0);
			Sound.pause(5000); 
			nav.driveWCorrection(5);
			launcher.lockArm(); //brings it to middle and locks it
			//change track:
			correction.width = 12;
			nav.width = 12;
			odo.TRACK = 12;
			System.out.println(WiFiExample.TRACK);
			nav.driveWCorrection(30.48);
			
			WiFiExample.cont.stopSensing = false;
			//travel one tile behind forward line IN Y FIRST, localize
			fwdLine_coord = 10 - fwdLinePosition;
			
			if(fwdLinePosition == 6){
				nav.travelToYFIRST(5*TILE_LENGTH, (fwdLine_coord-2)*TILE_LENGTH);
			}
			else{
				nav.travelToYFIRST(5*TILE_LENGTH, (fwdLine_coord-1)*TILE_LENGTH);
			}

			nav.finishTravel = false;

			nav.travelTo(5*TILE_LENGTH, (fwdLine_coord*TILE_LENGTH) - ROBOT_FRONT_TOCENTER_DIST); //go to forward line
			nav.finishTravel = false;
			
			nav.turnToSmart(0); //face target 
			nav.driveWCorrection(-15);
	
			launcher.prepareToFire();
			Sound.pause(1000);
			launcher.Fire(fwdLinePosition);
			WiFiExample.cont.stopSensing = true;
		}
			
	}
	
	/**
	 * This method is used to very quickly stop the robot from moving in whatever direction it is
	 * moving in before setting it's speed to be 150 degrees/second shortly afterward.
	 */
	public void motorstop(){
		leftMotor.setAcceleration(7000);
		rightMotor.setAcceleration(7000);
		leftMotor.setSpeed(0);
		rightMotor.setSpeed(0);

		leftMotor.stop(true);
		rightMotor.stop(false);

		leftMotor.setSpeed(ROTATE_SPEED);
		rightMotor.setSpeed(ROTATE_SPEED);
		leftMotor.setAcceleration(1000);
		rightMotor.setAcceleration(1000);
	}
	
}
