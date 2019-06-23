package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.data;

public class OmnibotControl implements IControl
{
	public double vt = 0.0;
	public double vo = 0.0;
	public double omega = 0.0;
	public double eta = 0.0;
	
	public OmnibotControl()
	{
	}
	
	public OmnibotControl(double vt, double vo, double omega, double eta)
	{
		this.vt    = vt;
		this.vo    = vo;
		this.omega = omega;
		this.eta   = eta;
	}
}
