package finalProject;

import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class Launcher {
	public static final EV3LargeRegulatedMotor launcherMotor = WiFiExample.launcherMotor;

	
	public static void Enter_Launch_Position(){
		launcherMotor.setAcceleration(1000);
		launcherMotor.setSpeed(1000);
		launcherMotor.rotate(120,false);
	}
	
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
