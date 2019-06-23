/* 
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 08.08.2010
 * Author(s): Gero
 *
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.types;

import edu.dhbw.mannheim.tigers.sumatra.model.data.CamDetectionFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.FrameID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;

/**
 * An interface that declares some methods for measurements purposes.
 * 
 * @author Gero
 */
public interface ITimer
{
	/**
	 * Signals that the following {@link CamDetectionFrame} has been received by {@link ACam}
	 */
	public void time(CamDetectionFrame detnFrame);
	
	
	/**
	 * Signals that the WP started processing a {@link CamDetectionFrame}
	 */
	public void startWP(CamDetectionFrame detnFrame);
	
	/**
	 * Signals that the following {@link WorldFrame} has been predicted by the {@link AWorldPredictor}
	 */
	public void stopWP(WorldFrame wFrame);
	
	
	/**
	 * Signals that the AI started processing a {@link WorldFrame}
	 */
	public void startAI(WorldFrame wFrame);
	
	/**
	 * Signals that the following {@link AIInfoFrame} has been processed by the {@link AAgent}
	 */
	public void stopAI(AIInfoFrame aiFrame);
	
	
	/**
	 * Signals that a skill has been scheduled using information from the {@link WorldFrame} with this {@link FrameID}
	 */
	public void time(FrameID wfID);
}
