/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 17, 2013
 * Author(s): Dirk Klostermann <klostermannn@googlemail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.fieldPrediction;

import java.util.Map;

import net.sf.oval.constraint.AssertValid;
import net.sf.oval.constraint.NotNull;

import com.sleepycat.persist.model.Persistent;

import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotIDMap;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotIDMapConst;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.IBotIDMap;


/**
 * a Metis calculator to do a prediction for all elements on the field where they are in the future
 * 
 * @author Dirk Klostermann <klostermannn@googlemail.com>
 * 
 */
@Persistent(version = 2)
public class WorldFramePrediction
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	@NotNull
	@AssertValid
	private final IBotIDMap<FieldPredictionInformation>	bots;
	
	
	@NotNull
	private final FieldPredictionInformation					ball;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@SuppressWarnings("unused")
	private WorldFramePrediction()
	{
		ball = null;
		bots = null;
	}
	
	
	/**
	 * @param bots
	 * @param ball
	 */
	public WorldFramePrediction(IBotIDMap<FieldPredictionInformation> bots, FieldPredictionInformation ball)
	{
		this.bots = BotIDMapConst.unmodifiableBotIDMap(bots);
		this.ball = ball;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * Returns a mirrored instance of this
	 * @return
	 */
	public WorldFramePrediction mirrorNew()
	{
		IBotIDMap<FieldPredictionInformation> newBots = new BotIDMap<FieldPredictionInformation>();
		for (Map.Entry<BotID, FieldPredictionInformation> entry : bots.entrySet())
		{
			newBots.put(entry.getKey(), entry.getValue().mirror());
		}
		
		return new WorldFramePrediction(newBots, ball.mirror());
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @return the ball
	 */
	public FieldPredictionInformation getBall()
	{
		return ball;
	}
	
	
	/**
	 * get the FieldPredictionInformation for one tiger
	 * 
	 * @param bot
	 * @return
	 */
	public FieldPredictionInformation getBot(BotID bot)
	{
		return bots.get(bot);
	}
	
	
	/**
	 * @return the tigers
	 */
	public IBotIDMap<FieldPredictionInformation> getBots()
	{
		return bots;
	}
}
