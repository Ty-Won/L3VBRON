import lejos.hardware.Button;
import lejos.hardware.motor.EV3LargeRegulatedMotor;


public class Kobe {
	public static int ROTATE_SPEED = 150;
	private EV3LargeRegulatedMotor leftMotor;
	private EV3LargeRegulatedMotor rightMotor;
	
	private EV3LargeRegulatedMotor catapultL;
	int launchSpeed;
	int launchAngle=130;
	public static double WHEEL_RADIUS = 2.1;
	public static final double TRACK = 14.2;
	int buttonChoice;
	
	
	//angle orientation
	double Orientation;
	
	public Kobe(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor,EV3LargeRegulatedMotor catapultL) {
		this.leftMotor=leftMotor;
		this.rightMotor=rightMotor;
		this.catapultL=catapultL;
		
		
	}
	
	
	
	//methods for shooting
	public void middleShot(){
		catapultL.setSpeed(2000);
		
		leftMotor.setSpeed(ROTATE_SPEED);
		rightMotor.setSpeed(ROTATE_SPEED);
		do{
			buttonChoice = Button.waitForAnyPress();
			if(buttonChoice==Button.ID_ENTER){
				//throw
				catapultL.rotate(-launchAngle);
			
				//return to launch position
				catapultL.rotate(launchAngle);
		
				
			}
			
			
			
		}while(buttonChoice != Button.ID_ESCAPE);
		
	}
	
	public void leftShot(){
		Orientation=Math.atan2(-1, 3);
				
		
		leftMotor.setSpeed(ROTATE_SPEED);
		rightMotor.setSpeed(ROTATE_SPEED);

		
		leftMotor.rotate(convertAngle(WHEEL_RADIUS, TRACK,Math.toDegrees(Orientation)), true);
		rightMotor.rotate(-convertAngle(WHEEL_RADIUS, TRACK,Math.toDegrees(Orientation)), false);
		
		catapultL.setSpeed(2000);
		
		do{
			buttonChoice = Button.waitForAnyPress();
			if(buttonChoice==Button.ID_ENTER){
				//throw
				catapultL.rotate(-launchAngle);
			
				//return to launch position
				catapultL.rotate(launchAngle);
		
				
			}
			
			
			
		}while(buttonChoice != Button.ID_ESCAPE);
		
	}
	
	
	public void rightShot(){
		
		Orientation=Math.atan2(1, 3);;
		leftMotor.setSpeed(ROTATE_SPEED);
		rightMotor.setSpeed(ROTATE_SPEED);
		leftMotor.rotate(convertAngle(WHEEL_RADIUS, TRACK, Math.toDegrees(Orientation)), true);
		rightMotor.rotate(-convertAngle(WHEEL_RADIUS, TRACK, Math.toDegrees(Orientation)), false);
		catapultL.setSpeed(2000);
		
		
		
		do{
			buttonChoice = Button.waitForAnyPress();
			if(buttonChoice==Button.ID_ENTER){
				//throw
				catapultL.rotate(-launchAngle);
			
				//return to launch position
				catapultL.rotate(launchAngle);
		
				
			}
			
			
			
		}while(buttonChoice != Button.ID_ESCAPE);
		
		
		
	}
	
	
	//Angle and distance conversion methods
	
	private static int convertDistance(double radius, double distance) {
		return (int) ((180.0 * distance) / (Math.PI * radius));
		
	}

	private static int convertAngle(double radius, double width, double angle) {
		return convertDistance(radius, Math.PI * width * angle / 360.0);
	}
	


}