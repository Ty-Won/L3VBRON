
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.SampleProvider;

public class LightLocalizer {
	private Odometer odo;
	private SampleProvider colorSensor;
	private float[] colorData = new float[10] ;	
	public static int ROTATION_SPEED = 60;
	public static int FORWARD_SPEED = 100;
	public static int DISTANCE_FROM_EDGE = 18;
	public static int ACCELERATION = 600;
	private EV3LargeRegulatedMotor leftMotor, rightMotor;
	public double centerToLightSensor=13.8;
	public Navigation navigator;
	
	public LightLocalizer(Odometer odo, SampleProvider colorSensor, float[] colorData, Navigation navigator) {
		this.odo = odo;
		this.colorSensor = colorSensor;
		this.colorData = colorData;
		this.navigator=navigator;
//		this.leftMotor=leftMotor;
//		this.rightMotor=rightMotor;
	}
	
	public void doLocalization() {
		// drive to location listed in tutorial
		// start rotating and clock all 4 gridlines
		// do trig to compute (0,0) and 0 degrees
		// when done travel to (0,0) and turn to 0 degrees
		
		boolean travelling=true;
		
		//keeps track of lines detected when we clock the 4 gridlines
		int gridLinesDetected=0;
		double gridLineAngles[]={0,0,0,0};
		
		colorSensor.fetchSample(colorData, 0);
		
		//finding the difference between the initial value and black
		while(travelling){
			colorSensor.fetchSample(colorData, 0);
			navigator.setSpeeds(100,100);
			if(colorData[0]+colorData[1]+colorData[2]<0.30){
				navigator.setSpeeds(0, 0);
				break;
			}
		}
		
//		try { Thread.sleep(5000); } catch(Exception e){}
		
		//distance between the center and the light sensor is 13.2 cm
		//moves the EV3 center back on the black line
		this.centerOverLine();
		
		
		//rotating towards the (0,0)
		navigator.turnBy(-90);
		
		

		
		//stop when near (0,0)
		while(travelling){
			colorSensor.fetchSample(colorData, 0);
			navigator.setSpeeds(100,100);
			if(colorData[0]+colorData[1]+colorData[2]<0.30){
				navigator.setSpeeds(0, 0);
				break;
			}
		}
		
		//brings the center near the (0,0) mark
		this.centerOverLine();
	
		
		
		/*					90Deg:pos y-axis (3rd line theta Y2)
		 * 							|
		 * 							|
		 * 							|
		 * 							|
		 * 180Deg:neg x-axis------------------0Deg:pos x-axis (2nd line theta X1)
		 *(4th line theta X2)		|
		 * 							|
		 * 							|
		 * 							|
		 * 					270Deg:neg y-axis (1st line theta Y1)
		*/
		
		//rotate 
		while(gridLinesDetected<4){
			colorSensor.fetchSample(colorData, 0);
			navigator.setSpeeds(50,-50);
			if(colorData[0]+colorData[1]+colorData[2]<0.30){
				//stores angles for each line detection in the gridLineAngles array
				gridLineAngles[gridLinesDetected]=odo.getAng();
				gridLinesDetected+=1;
				navigator.turnBy(10);
			}
		}
		
		//stop after 4 lines are detected
		navigator.setSpeeds(0,0);
		
		//Y2-Y1 is the 3rd element of array gridLineAngles[2] - 1st element of array gridLineAngles[0]
		double deltaY=gridLineAngles[2]-gridLineAngles[0];
		
		//X2-X1 is the 4th element of array gridLineAngles[3] - 2nd element of array gridLineAngles[1]
		double deltaX=gridLineAngles[3]-gridLineAngles[1];
		
		
		//TESTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT
		//using the math of the tutorial,not sure if they want degrees or radians............................................
		double x=-centerToLightSensor*Math.cos(Math.toRadians(deltaY/2));
		double y=-centerToLightSensor*Math.cos(Math.toRadians(deltaX/2));
		
		
		
		//check if need radians or degrees
		//storing values for position to be used with the set position method
		double position []={x,y,odo.getAng()};
		
		
/*		//tutorial correction angle to return to 0
		double correctionAngle= (gridLineAngles[0]-180)-90-(deltaY/2);
		
		//turn towards (0,0)
		navigator.turnTo(odo.getAng()+correctionAngle,true);
		
		//alternative, turn to 0,set the odometer to current x and y and set the travelTo straight away
*/		
//		try { Thread.sleep(5000); } catch(Exception e){}
		odo.setPosition(position,new boolean []{true,true,true});
//		try { Thread.sleep(5000); } catch(Exception e){}
		navigator.travelTo(0, 0);
		try { Thread.sleep(5000); } catch(Exception e){}
		navigator.turnTo(0, true);
		
	}
	
	//returns a float value for the color it has detected
	public float getColor(){
		colorSensor.fetchSample(colorData, 0);
		float color=colorData[0];
		return color;
		
	}
	
	
	//retrieves distance travelled by robot and returns in degrees how much the wheels have turned
	public int distanceToAngle(double radius,double dist){
		return (int)((180*dist)/(Math.PI*radius));
		
	}
	
	//places the EV3 center over any detected black line (since the light sensor is on the back)
	public void centerOverLine(){
		double radius= 2.1;
		navigator.goForward(-16);
	
	}
	
}