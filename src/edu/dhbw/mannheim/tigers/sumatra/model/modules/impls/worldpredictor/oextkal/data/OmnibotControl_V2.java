package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.data;

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
	
	
	/**
	 * @param vt
	 * @param vo
	 * @param omega
	 * @param eta
	 */
	public OmnibotControl_V2(double vt, double vo, double omega, double eta)
	{
		this.vt = vt;
		this.vo = vo;
		this.omega = omega;
		this.eta = eta;
	}
}
