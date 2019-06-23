package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.filter;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.data.AMotionResult;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.data.AWPCamObject;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.data.IControl;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.data.PredictionContext;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.motionModels.IMotionModel;


/**
 *
 */
public interface IFilter
{
	/**
	 * 
	 * @param motionModel
	 * @param context
	 * @param firstTimestamp
	 * @param firstObservation
	 */
	void init(IMotionModel motionModel, PredictionContext context, double firstTimestamp, AWPCamObject firstObservation);
	
	
	/**
	 * 
	 * @return
	 */
	double getTimestamp();
	
	
	/**
	 * 
	 * @param index
	 * @return
	 */
	double getLookaheadTimestamp(int index);
	
	
	/**
	 * 
	 * @param index
	 * @return
	 */
	AMotionResult getLookahead(int index);
	
	
	/**
	 * 
	 * @param timestamp
	 * @param observation
	 */
	void observation(double timestamp, AWPCamObject observation);
	
	
	/**
	 * 
	 * @param timestamp
	 */
	void updateOffset(double timestamp);
	
	
	/**
	 * 
	 * @param index
	 */
	void performLookahead(int index);
	
	
	/**
	 * 
	 * @param index
	 * @param effect
	 */
	void handleCollision(int index, IControl effect);
	
	
	/**
	 * 
	 * @param control
	 */
	void setControl(IControl control);
	
	
	/**
	 * 
	 * @return
	 */
	int getId();
	
	
	/**
	 *
	 */
	void keepPositionAliveOnNoObservation();
	
	
	/**
	 * 
	 * @return
	 */
	boolean positionKeptAlive();
}
