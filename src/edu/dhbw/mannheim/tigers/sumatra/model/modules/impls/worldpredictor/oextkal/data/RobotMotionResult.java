package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.data;

/**
 */
public class RobotMotionResult extends AMotionResult
{
	/** */
	public final double	orientation;
	/** */
	public final double	movementAngle;
	/** */
	public final double	vt;
	/** */
	public final double	vo;
	/** */
	public final double	trackSpeed;
	/** */
	public final double	angularVelocity;
	
	
	/**
	 * @param x
	 * @param y
	 * @param orientation
	 * @param movementAngle
	 * @param vt
	 * @param vo
	 * @param trackSpeed
	 * @param angularVelocity
	 * @param confidence
	 * @param onCam
	 */
	public RobotMotionResult(double x, double y, double orientation, double movementAngle, double vt, double vo,
			double trackSpeed, double angularVelocity, double confidence, boolean onCam)
	{
		super(x, y, confidence, onCam);
		this.orientation = orientation;
		this.movementAngle = movementAngle;
		this.vt = vt;
		this.vo = vo;
		this.trackSpeed = trackSpeed;
		this.angularVelocity = angularVelocity;
	}
}
