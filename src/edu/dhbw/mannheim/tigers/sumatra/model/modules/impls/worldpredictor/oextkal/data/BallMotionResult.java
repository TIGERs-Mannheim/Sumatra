package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.data;

/**
 */
public class BallMotionResult extends AMotionResult
{
	/** */
	public final double	z;
	/** */
	public final double	vx;
	/** */
	public final double	vy;
	/** */
	public final double	vz;
	/** */
	public final double	ax;
	/** */
	public final double	ay;
	/** */
	public final double	az;
	
	
	/**
	 * @param x
	 * @param y
	 * @param z
	 * @param vx
	 * @param vy
	 * @param vz
	 * @param ax
	 * @param ay
	 * @param az
	 * @param confidence
	 * @param onCam
	 */
	public BallMotionResult(double x, double y, double z, double vx, double vy, double vz, double ax, double ay,
			double az, double confidence, boolean onCam)
	{
		super(x, y, confidence, onCam);
		this.z = z;
		
		this.vx = vx;
		this.vy = vy;
		this.vz = vz;
		this.ax = ax;
		this.ay = ay;
		this.az = az;
	}
}
