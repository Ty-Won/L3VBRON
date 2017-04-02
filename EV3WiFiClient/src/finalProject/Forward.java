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
 * The forward class is a general class that is 
 * used to start up the necessary sensors and other 
 * classes (navigation and odometer). Forward should 
 * be called directly after localization if the robot is 
 * playing the forward role and it tells the robot to move 
 * to the position of the ball dispenser.
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
	private final int CENTER_X_COORD = 10; //x coordinate of center of field we will shoot from
	private final double ROBOT_FRONT_TOCENTER_DIST = 11; //distance from front of robot to center of rotation
	private final int FIELD_DIST = 8; //12
	private final int OUTER_TILES = 2;

	/** The left motor, which is connected to output A */
	public static final EV3LargeRegulatedMotor leftMotor = WiFiExample.leftMotor;
	/**The right motor, which is connected to output D */
	public static final EV3LargeRegulatedMotor rightMotor = WiFiExample.rightMotor;
	public static final EV3LargeRegulatedMotor launcherMotor = WiFiExample.launcherMotor;
	/**The navigation program for the robot */
	public static Navigation nav;
	/** Instantiating odometer of robopt*/
	public static Odometer odo;
	/** The Ultrasonic Sensor */
	private static final Port usPort = LocalEV3.get().getPort("S1");
	Launcher launcher = new Launcher(launcherMotor);
	
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
	 * localization and move the robot to the position of the ball dispenser.
	 */
	public void startFWD() {
		nav.travelTo(0, 7*30.48);
//		int[] field_coord = new int[2]; 	//array that stores field coordinates of the robot's position
//		if(corner==1){
//			field_coord[0] =0;
//			field_coord[1] =0;
//		}
//		if(corner==2){
//			field_coord[0] =10;
//			field_coord[1] =0;
//		}
//		if(corner==3){
//			field_coord[0] =10;
//			field_coord[1] =10;
//		}
//		if(corner==4){
//			field_coord[0] =0;
//			field_coord[1] =10;
//		}
//		//update odometer to correct position on field using the field_coord array
//		double[] position = {TILE_LENGTH*field_coord[0], TILE_LENGTH*field_coord[1], 0};
//		odo.setPosition(position, new boolean[]{true,true,false});//only update x and y
//		
//		//convert bx,by to cm:
//		double bx_cm, by_cm;
//		bx_cm = bx*TILE_LENGTH;
//		by_cm = by*TILE_LENGTH;
//		
//		//travel to ball dispenser cm coordinates:
//		nav.travelTo(bx_cm, by_cm-(2*30.48));
//		correction.localize();
//		nav.travelTo(bx_cm, by_cm); 
//		
//		//face AWAY from disp:
//		if(bx==0){ //disp is on west wall
//			nav.turnToSmart(90); //facing away from disp
//		}
//		if(bx==10){ //disp on east wall:
//			nav.turnToSmart(270);
//		}
//		
//		//localize forward
//		correction.localizeFWD();
//		//drive forward a little to correct angle:
//		nav.driveWCorrection(14);
//		nav.driveWCorrection(-15.5); //drive back to intersection
//		launcher.Enter_Launch_Position();//pulls the arm down
//		
//		//beep to indicate robot is ready to receive ball:
//		Sound.beep();

		
//		nav.travelTo(CENTER_X_COORD*TILE_LENGTH, 0);
//		nav.travelTo(CENTER_X_COORD*TILE_LENGTH, ((FIELD_DIST-OUTER_TILES-fwdLinePosition)*TILE_LENGTH)-ROBOT_FRONT_TOCENTER_DIST);
//		nav.turnToSmart(0); //faceTarget
//		LNCHR.Fire(4);
		
		
		
	}
	
}
