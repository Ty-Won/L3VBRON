import lejos.hardware.*;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.lcd.TextLCD;

public class Lab5 {
	static int buttonChoice;
	// Static Resources:
	// Left motor connected to output A
	// Right motor connected to output D
	// catapult motor connected to output C
	private static final EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
	private static final EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
	private static final EV3LargeRegulatedMotor catapultL = new	EV3LargeRegulatedMotor(LocalEV3.get().getPort("C"));
	
	
	
	public static void main(String[] args) {
		
		Kobe threepointer=new Kobe(leftMotor,rightMotor,catapultL);
		final TextLCD t= LocalEV3.get().getTextLCD();
		
		
			// clear the display
			t.clear();

			// ask the user whether the motors should drive in a square or float
			t.drawString("<L| C |R>", 2, 2);
			

			buttonChoice = Button.waitForAnyPress();
			
			t.clear();
			
			t.drawString("Press for", 0, 1);
			t.drawString("  shot", 0, 2);
		
		
		
		if(buttonChoice==Button.ID_LEFT){
			threepointer.leftShot();
		}
		
		else if(buttonChoice==Button.ID_RIGHT){
			threepointer.rightShot();
		}
		
		else if (buttonChoice==Button.ID_ENTER){
			threepointer.middleShot();
			
		}
		
		
		while (Button.waitForAnyPress() != Button.ID_ESCAPE){
			System.exit(0);
		}
}
}