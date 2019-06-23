/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 04.07.2011
 * Authors:
 * FlorianS
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.general;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.BotDistance;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeam;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedFoeBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.ACalculator;
import edu.dhbw.mannheim.tigers.sumatra.util.collection.BijectiveHashMap;


/**
 * This calculator determines the most dangerous opponent bot. Thereby the opponent ball carrier and the opponent goal
 * keeper are excluded.
 * 
 * @author FlorianS
 */
public class DangerousOpponents extends ACalculator
{
	// --------------------------------------------------------------------------
	// --- variable(s) ----------------------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final int	UNINITIALIZED_ID		= -1;
	private static final float	SPEED_LIMIT				= 0.5f;
	
	private final Vector2f		goalCenterOur			= AIConfig.getGeometry().getGoalOur().getGoalCenter();
	
	private float					relevantX;
	private float					relevantY;
	
	private float					dangerValue;
	
	private final TrackedBot	fakeBot					= new TrackedFoeBot(new BotID(0, ETeam.OPPONENTS),
																			new Vector2(0, 0), new Vector2(0, 0), new Vector2(0, 0), 0,
																			0.0f, 0.0f, 0.0f, 0.0f);
	
	private List<TrackedBot>	dangerousOpponents	= new ArrayList<TrackedBot>();
	
	
	// --------------------------------------------------------------------------
	// --- constructor(s) -------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public DangerousOpponents()
	{
	}
	
	
	// --------------------------------------------------------------------------
	// --- method(s) ------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Calculates all opponent bots sorted descending by their dangerValue
	 * @param curFrame
	 * @param preFrame
	 */
	@Override
	public void doCalc(AIInfoFrame curFrame, AIInfoFrame preFrame)
	{
		final WorldFrame worldFrame = curFrame.worldFrame;
		
		TrackedBot opponentBallGetter = null;
		TrackedBot opponentGoalKeeper = null;
		
		float distanceToBall = UNINITIALIZED_ID;
		float closestDistanceToBall = UNINITIALIZED_ID;
		
		float distanceToGoal = UNINITIALIZED_ID;
		float closestDistanceToGoal = UNINITIALIZED_ID;
		
		final IVector2 ballPos = worldFrame.ball.getPos();
		final IVector2 ballVel = worldFrame.ball.getVel();
		final Vector2f goalCenterTheir = AIConfig.getGeometry().getGoalTheir().getGoalCenter();
		
		final BijectiveHashMap<BotID, TrackedBot> unsortedOpponents = new BijectiveHashMap<BotID, TrackedBot>();
		
		final LinkedList<BotDistance> sortedOpponents = new LinkedList<BotDistance>();
		List<TrackedBot> tmpDangerousOpponents = new ArrayList<TrackedBot>();
		
		if (worldFrame.foeBots.size() != 0)
		{
			// put all opponent bots into bijective HashMap
			final Iterator<BotID> it = worldFrame.foeBots.keySet().iterator();
			while (it.hasNext())
			{
				final BotID i = it.next();
				unsortedOpponents.put(i, worldFrame.foeBots.get(i));
			}
			
			// identify the opponent goal keeper
			for (final TrackedBot currentBot : unsortedOpponents.values())
			{
				distanceToGoal = GeoMath.distancePP(currentBot, goalCenterTheir);
				if ((closestDistanceToGoal == UNINITIALIZED_ID) || (distanceToGoal < closestDistanceToGoal))
				{
					closestDistanceToGoal = distanceToGoal;
					opponentGoalKeeper = currentBot;
				}
			}
			
			// identify the opponent ball getter
			for (final TrackedBot currentBot : unsortedOpponents.values())
			{
				distanceToBall = GeoMath.distancePP(currentBot, ballPos);
				if (((closestDistanceToBall == UNINITIALIZED_ID) || (distanceToBall < closestDistanceToBall))
						&& (currentBot != opponentGoalKeeper))
				{
					closestDistanceToBall = distanceToBall;
					opponentBallGetter = currentBot;
				}
			}
			
			// exclude opponent goal keeper and ball getter from consideration / sorting
			unsortedOpponents.removeValue(opponentGoalKeeper);
			unsortedOpponents.removeValue(opponentBallGetter);
			
			// sort bots by danger value
			
			if ((ballVel.getLength2() <= SPEED_LIMIT) || (preFrame == null))
			{
				for (final TrackedBot currentBot : unsortedOpponents.values())
				{
					relevantX = GeoMath.distancePP(currentBot, goalCenterOur);
					relevantY = Math.abs(currentBot.getPos().y() - curFrame.worldFrame.ball.getPos().y());
					
					dangerValue = (1 / relevantX) * relevantY;
					
					if (currentBot.getPos().x() > (AIConfig.getGeometry().getFieldLength() / 6))
					{
						dangerValue *= 0.01f;
					}
					
					sortedOpponents.add(new BotDistance(currentBot, dangerValue));
				}
				
				Collections.sort(sortedOpponents, BotDistance.DESCENDING);
				
				
				// add opponent ball getter as most dangerous bot
				tmpDangerousOpponents.add(opponentBallGetter);
				
				// add opponent field players in order of their danger value
				for (final BotDistance botValuePair : sortedOpponents)
				{
					tmpDangerousOpponents.add(botValuePair.getBot());
				}
				
				// add opponent goal keeper as least dangerous bot
				tmpDangerousOpponents.add(opponentGoalKeeper);
			} else
			{
				tmpDangerousOpponents = preFrame.tacticalInfo.getDangerousOpponents();
			}
		} else
		{
			tmpDangerousOpponents = new ArrayList<TrackedBot>();
			tmpDangerousOpponents.add(fakeBot);
			tmpDangerousOpponents.add(fakeBot);
			tmpDangerousOpponents.add(fakeBot);
		}
		
		// save list to static variable
		dangerousOpponents.clear();
		dangerousOpponents = tmpDangerousOpponents;
		
		curFrame.tacticalInfo.setDangerousOpponents(dangerousOpponents);
		curFrame.tacticalInfo.setOpponentBallGetter(getOpponentBallGetter());
		curFrame.tacticalInfo.setOpponentPassReceiver(getOpponentPassReceiver());
		curFrame.tacticalInfo.setOpponentKeeper(getOpponentKeeper());
	}
	
	
	/**
	 * Returns the opponent bot being the closest to the ball which will be
	 * the ball getter or the ball carrier
	 * 
	 * @return
	 */
	public TrackedBot getOpponentBallGetter()
	{
		if (!dangerousOpponents.isEmpty())
		{
			return dangerousOpponents.get(0);
		}
		return fakeBot;
	}
	
	
	/**
	 * Return the opponent bot being the most dangerous one after the ball getter
	 * 
	 * @return
	 */
	public TrackedBot getOpponentPassReceiver()
	{
		if (!dangerousOpponents.isEmpty())
		{
			return dangerousOpponents.get(1);
		}
		return fakeBot;
	}
	
	
	/**
	 * Returns the opponent bot being the least dangerous one which will be the keeper
	 * 
	 * @return
	 */
	public TrackedBot getOpponentKeeper()
	{
		if (!dangerousOpponents.isEmpty())
		{
			return dangerousOpponents.get(dangerousOpponents.size() - 1);
		}
		return fakeBot;
	}
	
	
	@Override
	public void fallbackCalc(AIInfoFrame curFrame, AIInfoFrame preFrame)
	{
		curFrame.tacticalInfo.setDangerousOpponents(new ArrayList<TrackedBot>());
		curFrame.tacticalInfo.setOpponentBallGetter(getOpponentBallGetter());
		curFrame.tacticalInfo.setOpponentPassReceiver(getOpponentPassReceiver());
		curFrame.tacticalInfo.setOpponentKeeper(getOpponentKeeper());
	}
}
