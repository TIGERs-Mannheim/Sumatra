package edu.tigers.sumatra.wp.kalman.data;

/**
 */
public class OmnibotControl_V2 implements IControl
{
	/** */
	public final double	vt;
	/** */
	public final double	vo;
	/** positive is clockwise */
	public final double	omega;
	/** positive is clockwise */
	public final double	eta;
								
	/** */
	public final double	at;
	/** */
	public final double	ao;
								
	/**  */
	public final boolean	useAcc;
								
								
	/**
	 * @param vt
	 * @param vo
	 * @param omega
	 * @param eta
	 * @param at
	 * @param ao
	 */
	public OmnibotControl_V2(final double vt, final double vo, final double omega, final double eta, final double at,
			final double ao)
	{
		this.vt = vt;
		this.vo = vo;
		this.omega = omega;
		this.eta = eta;
		this.at = at;
		this.ao = ao;
		useAcc = true;
	}
	
	
	/**
	 * @param vt
	 * @param vo
	 * @param omega
	 * @param eta
	 */
	public OmnibotControl_V2(final double vt, final double vo, final double omega, final double eta)
	{
		this.vt = vt;
		this.vo = vo;
		this.omega = omega;
		this.eta = eta;
		at = 0;
		ao = 0;
		useAcc = false;
	}
}
