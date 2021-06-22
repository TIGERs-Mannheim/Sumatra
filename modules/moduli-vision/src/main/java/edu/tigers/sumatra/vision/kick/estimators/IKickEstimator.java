/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.vision.kick.estimators;

import edu.tigers.sumatra.cam.data.CamBall;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.vision.data.FilteredVisionBot;

import java.util.List;
import java.util.Optional;


/**
 * @author AndreR <andre@ryll.cc>
 */
public interface IKickEstimator
{
	/**
	 * Add an internal cam ball to the estimator.
	 * 
	 * @param record
	 */
	void addCamBall(final CamBall record);
	
	
	/**
	 * Get estimator's fit result.
	 * 
	 * @return Empty if this estimator currently does not have an idea what is going on.
	 */
	Optional<KickFitResult> getFitResult();
	
	
	/**
	 * Query the estimator if the kick is done.
	 * 
	 * @param mergedRobots
	 * @param timestamp
	 * @return
	 */
	boolean isDone(List<FilteredVisionBot> mergedRobots, long timestamp);
	
	
	/**
	 * Get drawable shapes for debugging.
	 * 
	 * @return
	 */
	List<IDrawableShape> getShapes();
	
	
	/**
	 * Get estimator type.
	 * 
	 * @return
	 */
	EKickEstimatorType getType();


	/**
	 * Do model identification.
	 *
	 * @return
	 */
	List<IBallModelIdentResult> getModelIdentResult();
}
