package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.data;

public class RobotMotionResult_V2 extends AMotionResult
{
	public final double orientation;
	public final double movementAngle;
	public final double v;
	public final double trackSpeed;
	public final double angularVelocity;
	
	public RobotMotionResult_V2(double x, double y, double orientation, 
			double movementAngle, double v,double trackSpeed, 
			double angularVelocity, double confidence, boolean onCam)
	{
		super(x, y, confidence, onCam);
		this.orientation = orientation;
		this.movementAngle = movementAngle;
		this.v = v;
		this.trackSpeed = trackSpeed;
		this.angularVelocity = angularVelocity;
	}
}
