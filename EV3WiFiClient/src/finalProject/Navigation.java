package finalProject;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class Navigation extends Thread{
	
	double wheel_radius = WiFiExample.WHEEL_RADIUS;
	double width =  WiFiExample.TRACK;
	private static final int FORWARD_SPEED = WiFiExample.FORWARD_SPEED;
	private static final int ROTATE_SPEED = WiFiExample.ROTATE_SPEED;
	public double odo_x,odo_y, odo_theta;
	public double x_dest, y_dest, theta_dest;
	private EV3LargeRegulatedMotor leftMotor = WiFiExample.leftMotor;
	private EV3LargeRegulatedMotor rightMotor = WiFiExample.rightMotor;
	
	//instantiate odometer:
	public Odometer odometer = WiFiExample.odometer;
	public Navigation(Odometer odometer){ //constructor
		this.odometer = odometer;
	}
	public void run(){
		travelTo(60,30);
		travelTo(30,30);
		travelTo(30,60);
		travelTo(60,0);
	}
	
	public void travelTo(double x, double y){
		//this method causes robot to travel to the absolute field location (x,y)

		odo_x = odometer.getX();
		odo_y = odometer.getY();
		odo_theta = odometer.getTheta();
		
		x_dest = x;
		y_dest = y;
		
		//calculate the distance we want the robot to travel in x and y 
		double delta_y = y_dest-odo_y;
		double delta_x = x_dest-odo_x;
		
		//calculate desired theta heading: theta = arctan(x/y)
		theta_dest = Math.toDegrees(Math.atan2(delta_y,delta_x));
		
		//distance to travel: d = sqrt(x^2+y^2)
		double travelDist = Math.hypot(delta_x,delta_y);
		//Math.hypot calculates the hypotenuse of its arguments (distance we want to find)
		
		//subtract odo_theta from theta_dest:
		double theta_corr = theta_dest - odo_theta;
		
		//DIRECTING ROBOT TO CORRECT ANGLE: 
		if(theta_corr < -180){ //if theta_dest is between angles [-180,-360] 
			//add 360 degrees to theta_dest in order for the robot to turn the smallest angle
			turnTo(theta_corr + 360);
		}
		else if(theta_corr > 180){ //if theta_dest is between angles [180,360]
			//subtract 360 degrees from theta_dest in order for the robot to turn the smallest angle
			turnTo(theta_corr - 360);
		}
		else{
			turnTo(theta_corr);
		}
		drive(travelDist);

	}
	public void drive(double distance){
		//set both motors to forward speed desired
		leftMotor.setSpeed(FORWARD_SPEED);
		rightMotor.setSpeed(FORWARD_SPEED);
						
		leftMotor.rotate(convertDistance(wheel_radius, distance), true);
		rightMotor.rotate(convertDistance(wheel_radius, distance), false);
	}
	
	public void turnTo(double theta){
		//this method causes the robot to turn (on point) to the absolute heading theta
		
		//make robot turn to angle theta:
		leftMotor.setSpeed(ROTATE_SPEED);
		rightMotor.setSpeed(ROTATE_SPEED);
		
		leftMotor.rotate(convertAngle(wheel_radius, width, theta), true);
		rightMotor.rotate(-convertAngle(wheel_radius, width, theta), false);
	}

	private static int convertDistance(double radius, double distance) {
		return (int) ((180.0 * distance) / (Math.PI * radius));
	}
	private static int convertAngle(double radius, double width, double angle) {
		return convertDistance(radius, Math.PI * width * angle / 360.0);
	}
}
