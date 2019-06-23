package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.kalman.oextkal.filter;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.kalman.oextkal.data.AMotionResult;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.kalman.oextkal.data.AWPCamObject;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.kalman.oextkal.data.IControl;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.kalman.oextkal.data.PredictionContext;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.kalman.oextkal.motionModels.IMotionModel;


/**
 *
 */
public interface IFilter
{
	/**
	 * @param motionModel
	 * @param context
	 * @param firstTimestamp
	 * @param firstObservation
	 */
	void init(IMotionModel motionModel, PredictionContext context, long firstTimestamp, AWPCamObject firstObservation);
	
	
	/**
	 * @return
	 */
	long getTimestamp();
	
	
	/**
	 * @param index
	 * @return
	 */
	AMotionResult getLookahead(int index);
	
	
	/**
	 * @param timestamp
	 * @param observation
	 */
	void observation(long timestamp, AWPCamObject observation);
	
	
	/**
	 * @param index
	 */
	void performLookahead(int index);
	
	
	/**
	 * @param control
	 */
	void setControl(IControl control);
	
	
	/**
	 * @return
	 */
	int getId();
	
	
	/**
	 *
	 */
	void keepPositionAliveOnNoObservation();
	
	
	/**
	 * @return
	 */
	boolean positionKeptAlive();
}
