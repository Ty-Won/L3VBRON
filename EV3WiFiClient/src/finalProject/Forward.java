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
 * @author ilanahaddad
 * @version 1.0
 *
 */
public class Forward {
	private int corner;	//starting corner
	private int fwdLinePosition;
	private int w1; //defender zone dimension w1
	private int w2; //defender zone dimension w2
	private int disp_x; //ball dispenser position x
	private int disp_y; //ball dispenser position y
	private String omega; //ball dispenser orientation 
	private final double TILE_LENGTH = 30.48;
	private final int CENTER_X_COORD = 10; //x coordinate of center of field we will shoot from
	private final double ROBOT_FRONT_TOCENTER_DIST = 11; //distance from front of robot to center of rotation
	private final int FIELD_DIST = 8; //12
	private final int OUTER_TILES = 2;

	// Left motor connected to output A
	// Right motor connected to output D
	// Ball Launcher Motor connected to output B
	public static final EV3LargeRegulatedMotor leftMotor = WiFiExample.leftMotor;
	public static final EV3LargeRegulatedMotor rightMotor = WiFiExample.rightMotor;
//	public static final EV3LargeRegulatedMotor launcherMotor = WiFiExample.launcherMotor;;


	public static Navigation nav;
//	public static ballLauncher launch =  WiFiExample.launch;

	
		
	public Forward(Navigation navigation, int corner, int d1, int w1, int w2, int bx, int by, String omega) {
		this.corner = corner;
		this.fwdLinePosition = d1;
		this.w1 = w1;
		this.w2 = w2;
		this.disp_x = bx;
		this.disp_y = by;
		this.omega = omega;
		this.nav = navigation;
	}
	
	public void startFWD() {
		//already localized
		//DEMO:
		//step 1 = travel to shooting zone (under fwdLinePosition)
		//step 2 = shoot ball 
		
		//travel to shooting zone
//		nav.travelTo(CENTER_X_COORD*TILE_LENGTH, 0);
		nav.travelTo(CENTER_X_COORD*TILE_LENGTH, 0);
		nav.travelTo(CENTER_X_COORD*TILE_LENGTH, ((FIELD_DIST-OUTER_TILES-fwdLinePosition)*TILE_LENGTH)-ROBOT_FRONT_TOCENTER_DIST);
		nav.turnToSmart(0); //faceTarget
		Launcher.Fire(4);
		
		// travel to: (5*30.48, (10-d1)*(30.48-7))
		
		
	}
	
}
