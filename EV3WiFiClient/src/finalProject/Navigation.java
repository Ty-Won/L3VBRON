package finalProject;
import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.SampleProvider;

/**
 * The navigator is used to change either the robot's position 
 * or heading in a controlled manner. The navigator intakes values 
 * from the odometer continuously and takes a value for either 
 * angle or position from another class and computes the difference 
 * in heading before using the difference in wheel angle count of the 
 * robot to find the amount of change needed to have the correct 
 * heading. Once the robot has the correct heading, if the robot 
 * needs to move to another position the robot calculates the 
 * amount of distance it needs to travel to reach the correct 
 * position before going there and stopping.
 * 
 * 
 * 
 * @author Ian Gauthier
 * @author Ilana Haddad
 * @author Tristan Bouchard
 * @author Tyrone Wong
 * @author Alexandre Tessier
 * 
 * @version 2.0
 *
 */

public class Navigation{

	double wheel_radius = WiFiExample.WHEEL_RADIUS;
	double width =  WiFiExample.TRACK;
	private static final int FORWARD_SPEED = WiFiExample.FORWARD_SPEED;
	private static final int ROTATE_SPEED = WiFiExample.ROTATE_SPEED;

	/**The odometer value of the X position */
	public double odo_x;
	/** The odometer value of the Y position of the robot. */
	public double odo_y;
	/** The odometer value of the angle of the robot. */
	public double odo_theta;

	/** The X coordinate of the destination point. */
	public double x_dest;
	/** The Y coordinate of the destination point. */
	public double y_dest;
	/** The desired heading of the robot. */
	public double theta_dest;

	/** The left wheel's motor. Initialized in main class WiFiExample and passed on in Navigation.*/
	private EV3LargeRegulatedMotor leftMotor = WiFiExample.leftMotor;
	/** The right wheel's motor. Initialized in main class WiFiExample and passed on in Navigation.*/
	private EV3LargeRegulatedMotor rightMotor = WiFiExample.rightMotor;

	/** Meant to store the value of the R and L light sensors to determine if a black line is detected*/
	private float[] correctionLine;

	/** Boolean to store whether the robot is turning. Initialized to false. */
	public static boolean turning=false; 

	public boolean localizing=false;
	public boolean stop = false;

	/**The Odometer of the robot */
	public Odometer odometer = WiFiExample.odometer;

	/** Correction to correct heading of robot when navigating, 
	 * using two light sensors at the back of the robot. 
	 * Instantiated in WiFiExample and passed on in Navigation.
	 */
	//	public Correction correction;

	/**
	 * Constructor for Navigation
	 * @param odometer the odometer of the robot
	 */

	public Navigation(Odometer odometer){ //constructor
		this.odometer = odometer;
//		leftMotor.synchronizeWith(new RegulatedMotor[] {rightMotor});
	}

	/**
	 * The method moves the robot to the position that is inputed into the 
	 * method by first traveling to (x,0) and then to (x,y), breaking it up into two steps.
	 * Therefore, this method travels in straight lines rather than diagonally to final coordinates.
	 * 
	 * @param x the X coordinate that should be moved to
	 * @param y the X coordinate that should be moved to
	 */
	public void travelTo(double x, double y){
		//this method causes robot to travel to the absolute field location (x,y)

		if(stop){
			return;
		}
		odo_x = odometer.getX();
		odo_y = odometer.getY();
		odo_theta = odometer.getAng();
		x_dest = x;
		y_dest = y;

		//calculate the distance we want the robot to travel in x and y 
		double delta_y = y_dest-odo_y;
		double delta_x = x_dest-odo_x;

		drive(delta_x,delta_y);

		odo_x = odometer.getX();
		odo_y = odometer.getY();
		odo_theta = odometer.getAng();
		x_dest = x;
		y_dest = y;

		//calculate the distance we want the robot to travel in x and y 
		delta_y = y_dest-odo_y;
		delta_x = x_dest-odo_x;

		drive(delta_x,delta_y);
	}

	/**
	 * This method will travel to the coordinates x and y diagonally rather than split into x and y.
	 * This should call the turnTo method to turn to the correct heading 
	 * of the robot before finding the distance and moving that distance in 
	 * a straight heading.
	 * @param x the X coordinate that should be moved to
	 * @param y the X coordinate that should be moved to
	 */
	public void travelToDiag(double x, double y){
		//this method causes robot to travel to the absolute field location (x,y)

		if(stop){
			return;
		}

		odo_x = odometer.getX();
		odo_y = odometer.getY();
		odo_theta = odometer.getAng();
		x_dest = x;
		y_dest = y;

		//calculate the distance we want the robot to travel in x and y 
		double delta_y = y_dest-odo_y;
		double delta_x = x_dest-odo_x;

		//calculate desired theta heading: theta = arctan(y/x)

		//theta_dest = Math.toDegrees(Math.atan2(delta_x,delta_y));

		//distance to travel: d = sqrt(x^2+y^2)
		double travelDist = Math.hypot(delta_x,delta_y);
		//Math.hypot calculates the hypotenuse of its arguments (distance we want to find)

		//subtract odo_theta from theta_dest:
		double theta_corr = (theta_dest - odo_theta);

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

		driveDiag(travelDist);
	}

	/**
	 * The method should convert the distance into an angle in terms of
	 * the radius of the wheel and then travel forward that amount.
	 * Insert x and y coordinates and the EV3 travels on the x,y planes to reach the destination
	 * @param distance the distance to be converted in terms of cm
	 */
	public void drive(double delta_x,double delta_y){

		synchronized(leftMotor){
			synchronized(rightMotor){

//				if(stop){
//					return;
//				}
				//set both motors to forward speed desired
				leftMotor.setSpeed(FORWARD_SPEED);
				rightMotor.setSpeed(FORWARD_SPEED);
				leftMotor.setAcceleration(1000);
				rightMotor.setAcceleration(1000);

				//X-travel
				if(Math.abs(delta_x)<1){
					delta_x=0;
				}	
				if(Math.abs(delta_x)>1){
					if(delta_x>1)
						turnToSmart(90);
					else{
						turnToSmart(270);
					}
				}

//				leftMotor.startSynchronization();
				leftMotor.rotate(convertDistance(wheel_radius, Math.abs(delta_x)), true);
				rightMotor.rotate(convertDistance(wheel_radius, Math.abs(delta_x)), true);
//				leftMotor.endSynchronization();

				//might need to add a travel to after while loop to make sure it's in the right location
				while(leftMotor.isMoving()&&rightMotor.isMoving()){
					WiFiExample.correction.LightCorrection();
					if(WiFiExample.correction.gridcount==3){
						motorstop();
						localize();

					}
				}

				motorstop();

				//Y-travel
				if(Math.abs(delta_y)<1){
					delta_y=0;
				}	
				if(Math.abs(delta_y)>1){
					if(delta_y>1)
						turnToSmart(0);
					else{
						turnToSmart(180);
					}
				}

//				leftMotor.startSynchronization();
				leftMotor.rotate(convertDistance(wheel_radius, Math.abs(delta_y)), true);
				rightMotor.rotate(convertDistance(wheel_radius, Math.abs(delta_y)), true);
//				leftMotor.endSynchronization();

				//might need to add a travel to after while loop to make sure it's in the right location
				while(leftMotor.isMoving()&&rightMotor.isMoving()){
					WiFiExample.correction.LightCorrection();
					if(WiFiExample.correction.gridcount==3){
						motorstop();
						localize();
					}
				}

				motorstop();
			}
		}
	}
	/**
	 * This method travels to distance inputed diagonally.
	 * @param travelDist
	 */
	public void driveDiag(double travelDist){

		synchronized(leftMotor){
			synchronized(rightMotor){
				//		stopNav();
//				if(stop){
//					return;
//				}
				motorstop();
				
				//set both motors to forward speed desired
				leftMotor.setSpeed(FORWARD_SPEED);
				rightMotor.setSpeed(FORWARD_SPEED);
				leftMotor.setAcceleration(1000);
				rightMotor.setAcceleration(1000);

//				leftMotor.startSynchronization();
				leftMotor.rotate(convertDistance(wheel_radius, travelDist), true);
				rightMotor.rotate(convertDistance(wheel_radius, travelDist), true);
//				leftMotor.endSynchronization();
				
				while(leftMotor.isMoving()||rightMotor.isMoving()){
					}
					motorstop();
//				}

			}
		}
	}

	/**
	 * The method should intake an angle and then turn the robot to that angle.
	 * 
	 * @param theta the angle which should be turned to
	 */
	public void turnTo(double theta){
		//this method causes the robot to turn (on point) to the absolute heading theta


		synchronized(leftMotor){
			synchronized(rightMotor){

				if(stop){
					return;
				}

				turning = true;
				Sound.twoBeeps(); //DONT REMOVE THIS
				motorstop();
				//make robot turn to angle theta:
				leftMotor.setSpeed(ROTATE_SPEED);
				leftMotor.setAcceleration(1000);
				rightMotor.setSpeed(ROTATE_SPEED);
				rightMotor.setAcceleration(1000);

//				leftMotor.startSynchronization();
				leftMotor.rotate(convertAngle(wheel_radius, width, theta), true);
				rightMotor.rotate(-convertAngle(wheel_radius, width, theta), true);
//				leftMotor.endSynchronization();

				while(leftMotor.isMoving()||rightMotor.isMoving()){
					if(stop){
						turning = false;
						return;
					}
				}
				motorstop();

				//returns default acceleration values after turn
				leftMotor.setAcceleration(1000);
				rightMotor.setAcceleration(1000);
				leftMotor.setSpeed(FORWARD_SPEED);
				rightMotor.setSpeed(FORWARD_SPEED);
				Sound.twoBeeps();
			}
		}

		turning = false;
	}

	public void localize(){
		//		leftMotor.setSpeed(0);
		//		rightMotor.setSpeed(0);
		motorstop();
		WiFiExample.correction.localize();
		travelTo(x_dest,y_dest);
	}


	/**
	 * The method should convert the input distance into a form that is equal to
	 * the amount of rotation that a wheel of the given radius must rotate
	 * in order to move that distance
	 * 
	 * @param radius the radius of the wheels of the robot
	 * @param distance the distance which will be converted
	 * @return the converted distance
	 */
	private static int convertDistance(double radius, double distance) {
		return ((int) (100*(180.0 * distance) / (Math.PI * radius)))/100;
	}

	/**
	 * The method should convert the input angle into a form that can be performed
	 * by the robot with the given wheel radius and width.
	 * 
	 * 
	 * @param radius the radius of the wheel
	 * @param width the width of the robot
	 * @param angle the angle to be converted
	 * @return the angle now in the form of amount of rotation needed by the robot's wheel to perform that angle of turn
	 */
	private static int convertAngle(double radius, double width, double angle) {
		return convertDistance(radius, Math.PI * width * angle / 360.0);
	}

	/**
	 * The method causes the robot to turn to an angle in relation to its current
	 * heading meaning that if 150 is input, the robot should turn 150 degrees
	 * 
	 * @param angle the amount to be turned
	 */
	public void turnToSmart(double angle){
		if(stop){
			return;
		}
		odo_theta = odometer.getAng();
		
		//subtract odo_theta from theta_dest:
		double theta_corr = angle - odo_theta;
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

	}

	public double[] getDest(){
		double[] coordinates = {0.0,0.0,0.0};
		coordinates[0]=x_dest;
		coordinates[1]=y_dest;
		coordinates[2]=theta_dest;

		return coordinates;
	}

	/**
	 * 
	 * @return a boolean that is true if the robot it turning and false if it not
	 */
	public boolean isTurning(){
		return turning; 
	}

	public void motorstop(){

		leftMotor.setSpeed(0);
		rightMotor.setSpeed(0);
//		leftMotor.startSynchronization();
		leftMotor.stop();
		rightMotor.stop();
//		leftMotor.endSynchronization();
		leftMotor.setSpeed(ROTATE_SPEED);
		rightMotor.setSpeed(ROTATE_SPEED);
		leftMotor.setAcceleration(1000);
		rightMotor.setAcceleration(1000);
	}

	/**
	 * This method should return an exception when the robot's navigation has been interrupted
	 * to alert the system of the interruption.
	 */
	public void stopNav(){

		while(stop==true){
			try {
				localizing=WiFiExample.correction.islocalizing();
				Thread.sleep(500);
			} catch (InterruptedException e) {}
		}
	}

}

