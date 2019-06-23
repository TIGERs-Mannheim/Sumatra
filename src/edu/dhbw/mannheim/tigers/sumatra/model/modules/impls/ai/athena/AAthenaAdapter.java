/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 8, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.MetisAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.PlayStrategy;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.APlay;


/**
 * Base class for athena adapter. Adapters are used to react differently
 * depending on the current mode, e.g. Match, Test, Emergency
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public abstract class AAthenaAdapter implements IAthenaAdapterObserver
{
	private final AIControl	aiControl	= new AIControl();
	private final Object		sync			= new Object();
	
	
	/**
	 * @param metisAiFrame
	 * @param playStrategyBuilder
	 */
	public void process(MetisAiFrame metisAiFrame, PlayStrategy.Builder playStrategyBuilder)
	{
		synchronized (sync)
		{
			if (metisAiFrame.getWorldFrame().ball.getPos().equals(GeoMath.INIT_VECTOR))
			{
				// there is no ball, Sumatra would crash in this state!
				clear(playStrategyBuilder);
				return;
			}
			
			// add all previous plays
			playStrategyBuilder.getActivePlays().addAll(metisAiFrame.getPrevFrame().getPlayStrategy().getActivePlays());
			
			// Remove finished plays
			if (!metisAiFrame.getPrevFrame().getPlayStrategy().getFinishedPlays().isEmpty())
			{
				playStrategyBuilder.getActivePlays().removeAll(
						metisAiFrame.getPrevFrame().getPlayStrategy().getFinishedPlays());
			}
			
			doProcess(metisAiFrame, playStrategyBuilder, aiControl);
			aiControl.reset();
		}
	}
	
	
	/**
	 * @param metisAiFrame
	 * @param playStrategyBuilder
	 * @param aiControl
	 */
	public abstract void doProcess(MetisAiFrame metisAiFrame, PlayStrategy.Builder playStrategyBuilder,
			AIControl aiControl);
	
	
	/**
	 * Clear PlayStrategy, i.e. finish plays and remove
	 * 
	 * @param playStrategyBuilder
	 */
	protected void clear(PlayStrategy.Builder playStrategyBuilder)
	{
		for (APlay play : playStrategyBuilder.getActivePlays())
		{
			play.changeToFinished();
		}
		playStrategyBuilder.getActivePlays().clear();
	}
	
	
	/**
	 * @return the aiControl
	 */
	public final AIControl getAiControl()
	{
		synchronized (sync)
		{
			return aiControl;
		}
	}
}
