package finalProject;
import lejos.hardware.Button;
import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class ballLauncher {
	private static final int FAR_SPEED = 950; //1050 for low batt	//speed for when we are facing the left or right targets
	private static final int CLOSE_SPEED = 700; //900 for low batt	//speed for when we are facing the middle target
	private static final int ACCELERATION = 5000;
	private EV3LargeRegulatedMotor launcherMotor;
	private Odometer odo;
	private Navigation nav;
	
	
	public ballLauncher(EV3LargeRegulatedMotor launcherMotor, Odometer odometer, Navigation navigation){
		//constructor 
		this.launcherMotor = launcherMotor;
		this.odo = odometer;
		this.nav = navigation;
	}

	public void faceTarget(int targetDirection) {
		//method that turns the robot to face a target we choose
		
		if(targetDirection == -1){//turn to (-1,+3)
			nav.travelTo(-30.48, 91.44);
			//we modified travelTo so that it only turns and doesn't start moving
			throwBall(-30.48,91.44); //94.87
			nav.travelTo(30.48, 91.44); //faces middle again after ball has been thrown
		}
		else if(targetDirection == 0){//turn to (0,+3)
			throwBall(0,91.44); //90
		}
		else if(targetDirection == 1){//turn to (+1,+3)
			nav.travelTo(30.48, 91.44); //
			throwBall(30.48,91.44);
			nav.travelTo(-30.48, 90); //faces middle again after ball has been thrown
		}
		
	}
	public void throwBall(double x, double y){
		double distance = Math.sqrt((x*x) +(y*y));
		int buttonChoice = Button.waitForAnyPress();
		while(buttonChoice == Button.ID_UP){
			if(distance > 91){ //we are facing the left or right targets
				launcherMotor.setSpeed(FAR_SPEED);
				launcherMotor.setAcceleration(ACCELERATION);
				launcherMotor.rotate(90, false);
				launcherMotor.rotate(-90, false);
				buttonChoice = 0;
			}
			else{ //we are facing the middle target (0,+3)
				launcherMotor.setSpeed(CLOSE_SPEED);
				launcherMotor.rotate(90, false);
				launcherMotor.rotate(-90, false);
				buttonChoice = 0;

			}
		}
		
	}

}
