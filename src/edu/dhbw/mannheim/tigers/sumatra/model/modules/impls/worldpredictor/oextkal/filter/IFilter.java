package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.filter;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.data.AMotionResult;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.data.AWPCamObject;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.data.IControl;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.data.PredictionContext;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.motionModels.IMotionModel;

public interface IFilter {
 
	public void init(IMotionModel motionModel, PredictionContext context, double firstTimestamp, AWPCamObject firstObservation);
	public abstract double getTimestamp();
	public abstract double getLookaheadTimestamp(int index);
	public abstract AMotionResult getLookahead(int index);
	public abstract void observation(double timestamp, AWPCamObject observation);
	public abstract void updateOffset(double timestamp);
	public abstract void performLookahead(int index);
	public abstract void handleCollision(int index, IControl effect);
	public abstract void setControl(IControl control);
	public abstract int getId();
	
	public abstract void keepPositionAliveOnNoObservation();
	public abstract boolean positionKeptAlive();
}
 
