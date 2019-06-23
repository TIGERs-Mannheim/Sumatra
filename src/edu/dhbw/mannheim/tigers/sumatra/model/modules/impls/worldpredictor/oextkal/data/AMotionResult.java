package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.data;

public abstract class AMotionResult
{
	public final double x;
	public final double y;
	
	public final double confidence;
	public final boolean onCam;
	
	AMotionResult(double x, double y, double confidence, boolean onCam)
	{
		this.x = x;
		this.y = y;
		this.confidence = confidence;
		this.onCam = onCam;
	}
}
