package finalProject;

import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;

/**
 * This class is used to perform the launching of the ball toward the goal
 * once it has reached the forward line and is situated directly in front
 * of the target. Based on the distance that is passed to the class the
 * class will calculate how quickly the launching motor needs to move
 * in order for the ball to be able to pass through the goal. The launcher
 * then finds the angle to rotate to before accelerating to that speed and
 * launching the ball.
 * 
 * @author Tristan Bouchard
 * @version 1.0
 */
public class Launcher {
	public static final EV3LargeRegulatedMotor launcherMotor = WiFiExample.launcherMotor;

	/**
	 * This should move the launch into a position where it can launch the
	 * ball. The arm should be pulled back, stretching the rubber bands and 
	 * increasing the available energy to fire the ball.
	 */
	public static void Enter_Launch_Position(){
		launcherMotor.setAcceleration(1000);
		launcherMotor.setSpeed(1000);
		launcherMotor.rotate(120,false);
	}
	
	/**
	 * The overarching fire method, should call the other methods to get the speed
	 * and angle to fire from and then perform the actual action of rotating the
	 * arm and firing the ball.
	 * 
	 * @param dist the distance to the goal in number of tiles
	 */
	public static void Fire(int dist){
		// Fire Ball at speed calculated by Calculate_Speed method.
		int speed = Calculate_Speed(dist);
		// Release Ball at angle calculated by Calculate_Angle method.
		int angle = (int)Calculate_Angle(dist);
		launcherMotor.setSpeed(speed);
		launcherMotor.setAcceleration(200000);
		launcherMotor.rotate(-angle, false);
		
		// Return to firing position:
		launcherMotor.setSpeed(1000);
		launcherMotor.rotate(angle, false);
		
		
	}
	//This method calculates the correct angle of release depending on the distance given to the 
	/**
	 * This method calculates the correct angle of release depending on the distance given.
	 * 
	 * @param dist the distance to the goal in number of tiles.
	 * @return the angle to which the launching arm should rotate to fire the ball.
	 */
	public static int Calculate_Angle(int dist){
		int angle = 0;
		//angle = 5*(dist) + 40;
		if(dist > 7){
			angle = 90;
		}
		else{
			angle = 78;
		}
		return angle;
	}
	
	/**
	 * Calculates the speed at which the arm must rotate in order to reach the goal.
	 * 
	 * @param dist the distance to the goal in number of tiles
	 * @return the speed at which the robot should rotate the launching arm to reach the target
	 */
	public static int Calculate_Speed(int dist){
		int launch_speed = 0;
		if(dist == 8){
			launch_speed = 7950;
		}
		else if(dist == 7){
			launch_speed = 5450;
		}
		else if(dist == 6){
			launch_speed = 2600;
		}
		else if(dist == 5){
			launch_speed = 400;
		}
		else if(dist == 4){
			launch_speed = 355;
		}
		else if(dist == 3){
			launch_speed = 310;
		}
//		else{	
//			launch_speed = 2500*(dist) - 12050; 
//		}
		return launch_speed;
		
	}
}
