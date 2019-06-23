package edu.tigers.sumatra.wp.kalman.data;

/**
 */
public class OmnibotControl_V3 implements IControl
{
	private final double		vx, vy, vw;
	private final double		ax, ay, aw;
	private final boolean	useAcc;
									
									
	/**
	 * @param vx
	 * @param vy
	 * @param vw
	 */
	public OmnibotControl_V3(final double vx, final double vy, final double vw)
	{
		super();
		this.vx = vx;
		this.vy = vy;
		this.vw = vw;
		ax = 0;
		ay = 0;
		aw = 0;
		useAcc = false;
	}
	
	
	/**
	 * @param vx
	 * @param vy
	 * @param vw
	 * @param ax
	 * @param ay
	 * @param aw
	 */
	public OmnibotControl_V3(final double vx, final double vy, final double vw, final double ax, final double ay,
			final double aw)
	{
		super();
		this.vx = vx;
		this.vy = vy;
		this.vw = vw;
		this.ax = ax;
		this.ay = ay;
		this.aw = aw;
		useAcc = true;
	}
	
	
	/**
	 * @return the vx
	 */
	public double getVx()
	{
		return vx;
	}
	
	
	/**
	 * @return the vy
	 */
	public double getVy()
	{
		return vy;
	}
	
	
	/**
	 * @return the vw
	 */
	public double getVw()
	{
		return vw;
	}
	
	
	/**
	 * @return the ax
	 */
	public double getAx()
	{
		return ax;
	}
	
	
	/**
	 * @return the ay
	 */
	public double getAy()
	{
		return ay;
	}
	
	
	/**
	 * @return the aw
	 */
	public double getAw()
	{
		return aw;
	}
	
	
	/**
	 * @return the useAcc
	 */
	public boolean isUseAcc()
	{
		return useAcc;
	}
	
}
