package finalProject;
import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.SampleProvider;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.SensorMode;

public class LocalizationFilter {

	/*
	 * First initialize the color sensor so that it can be used as a red mode sensor later on in the code
	 * then one will have the color sensor poll the data (i'm not sure how often we're going to be polling
	 * the data at this point so I just made this a method that could be called at any point necessary.
	 * Once the data is polled, the filter will first check whether this is the first time that it has been called 
	 * after initialization. If so, the filter will simply store the value that it has received into the variable
	 * lastVal. Otherwise it will actually enter the filter. 
	 * I'm not sure the perfect number of jump that should be done to check if the slope is large enough so that will
	 * be something that needs to be tested in some way. However, the filter basically just checks if there has been a large
	 * drop in the polled redMode value and if there has been then the filter returns true meaning that there has been
	 * a line detected. If not, it returns false. I also put in a buzz just to make positives clear in testing. That can
	 * obviously be taken out.
	 * In addition, I used object instances of lastVal so that it could be used by multiple color sensors at once. Don't know
	 * if that's completely perfect yet, but it's just a thought.
	*/
	EV3ColorSensor colorSensor;
	public int lastVal;
	
	public LocalizationFilter(EV3ColorSensor colorSensor)
	{
		this.colorSensor = colorSensor;
		this.lastVal = -1;
	}
	
	public boolean filterData()
	{
		float[] colorSample = {0};
		
		while(true)
		{	Sound.twoBeeps();
			colorSensor.getRedMode().fetchSample(colorSample, 0); 
			int light_val = (int)(colorSample[0]*100);
			Sound.twoBeeps();
			if(this.lastVal == -1)
			{
				this.lastVal = light_val;
				Sound.buzz();
			}
			else if(this.lastVal - light_val > 7)
			{
				this.lastVal = light_val;
				Sound.buzz();
				return true;
			}
			else
			{
				this.lastVal = light_val;
				Sound.buzz();
				return false;
			}
		}
	}
	
}
