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
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.TrackedBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.AIMath;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.BotDistance;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.Goal;
import edu.dhbw.mannheim.tigers.sumatra.util.collection.BijectiveHashMap;


/**
 * This calculator determines the most dangerous opponent bot. Thereby the opponent ball carrier and the opponent goal
 * keeper are excluded.
 * 
 * @author FlorianS
 */
public class DangerousOpponents
{
	// --------------------------------------------------------------------------
	// --- variable(s) ----------------------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final int	UNINITIALIZED_ID		= -1;
	private final float			SPEED_LIMIT				= 0.5f;
	
	private final Goal			goal						= AIConfig.getGeometry().getGoalOur();
	private final Vector2f		goalCenterOur			= goal.getGoalCenter();
	
	private float					relevantX;
	private float					relevantY;
	
	private float					dangerValue;
	
	private TrackedBot			fakeBot					= new TrackedBot(0, new Vector2(0, 0), new Vector2(0, 0),
																			new Vector2(0, 0), 0, 0.0f, 0.0f, 0.0f, 0.0f);
	
	private List<TrackedBot>	dangerousOpponents	= new ArrayList<TrackedBot>();
	
	
	// --------------------------------------------------------------------------
	// --- constructor(s) -------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public DangerousOpponents()
	{
	}
	
	
	// --------------------------------------------------------------------------
	// --- method(s) ------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Returns a list of all opponent bots sorted descending by their dangerValue
	 */
	public List<TrackedBot> calculate(AIInfoFrame curFrame, AIInfoFrame preFrame)
	{
		WorldFrame worldFrame = curFrame.worldFrame;
		
		TrackedBot opponentBallGetter = null;
		TrackedBot opponentGoalKeeper = null;
		
		float distanceToBall = UNINITIALIZED_ID;
		float closestDistanceToBall = UNINITIALIZED_ID;
		
		float distanceToGoal = UNINITIALIZED_ID;
		float closestDistanceToGoal = UNINITIALIZED_ID;
		
		Vector2f ballPos = worldFrame.ball.pos;
		Vector2f ballVel = worldFrame.ball.vel;
		Vector2f goalCenterTheir = AIConfig.getGeometry().getGoalTheir().getGoalCenter();
		
		BijectiveHashMap<Integer, TrackedBot> unsortedOpponents = new BijectiveHashMap<Integer, TrackedBot>();
		
		LinkedList<BotDistance> sortedOpponents = new LinkedList<BotDistance>();
		List<TrackedBot> dangerousOpponents = new ArrayList<TrackedBot>();
		
		if (worldFrame.foeBots.size() != 0)
		{
			// put all opponent bots into bijective HashMap
			Iterator<Integer> it = worldFrame.foeBots.keySet().iterator();
			while (it.hasNext())
			{
				int i = it.next();
				unsortedOpponents.put(i, worldFrame.foeBots.get(i));
			}
			
			// identify the opponent goal keeper
			for (TrackedBot currentBot : unsortedOpponents.values())
			{
				distanceToGoal = AIMath.distancePP(currentBot, goalCenterTheir);
				if (closestDistanceToGoal == UNINITIALIZED_ID || distanceToGoal < closestDistanceToGoal)
				{
					closestDistanceToGoal = distanceToGoal;
					opponentGoalKeeper = currentBot;
				}
			}
			
			// identify the opponent ball getter
			for (TrackedBot currentBot : unsortedOpponents.values())
			{
				distanceToBall = AIMath.distancePP(currentBot, ballPos);
				if ((closestDistanceToBall == UNINITIALIZED_ID || distanceToBall < closestDistanceToBall)
						&& currentBot != opponentGoalKeeper)
				{
					closestDistanceToBall = distanceToBall;
					opponentBallGetter = currentBot;
				}
			}
			
			// exclude opponent goal keeper and ball getter from consideration / sorting
			unsortedOpponents.removeValue(opponentGoalKeeper);
			unsortedOpponents.removeValue(opponentBallGetter);
			
			// sort bots by danger value
			
			if (ballVel.getLength2() <= SPEED_LIMIT || preFrame == null)
			{
//				System.out.println("speed " + ballVel.getLength2());
				for (TrackedBot currentBot : unsortedOpponents.values())
				{
					relevantX = AIMath.distancePP(currentBot, goalCenterOur);
					relevantY = Math.abs(currentBot.pos.y - curFrame.worldFrame.ball.pos.y);
					
					dangerValue = (1 / relevantX) * relevantY;
					
					if (currentBot.pos.x > AIConfig.getGeometry().getFieldLength() / 6)
					{
						dangerValue *= 0.01f;
					}
					
					sortedOpponents.add(new BotDistance(currentBot, dangerValue));
				}
				
				Collections.sort(sortedOpponents, BotDistance.DESCENDING);
				
				
				// add opponent ball getter as most dangerous bot
				dangerousOpponents.add(opponentBallGetter);
				
				// add opponent field players in order of their danger value
				for (BotDistance botValuePair : sortedOpponents)
				{
					dangerousOpponents.add(botValuePair.bot);
				}
				
				// add opponent goal keeper as least dangerous bot
				dangerousOpponents.add(opponentGoalKeeper);
			} else
			{
				dangerousOpponents = preFrame.tacticalInfo.getDangerousOpponents();
			}
		} else
		{
			dangerousOpponents = new ArrayList<TrackedBot>();
			dangerousOpponents.add(fakeBot);
			dangerousOpponents.add(fakeBot);
			dangerousOpponents.add(fakeBot);
		}
		
		// save list to static variable
		this.dangerousOpponents.clear();
		this.dangerousOpponents = dangerousOpponents;
		
		return dangerousOpponents;
	}
	
	
	/**
	 * Returns the opponent bot being the closest to the ball which will be
	 * the ball getter or the ball carrier
	 * 
	 * @return
	 */
	public TrackedBot getOpponentBallGetter()
	{
		if (dangerousOpponents.size() != 0)
		{
			return dangerousOpponents.get(0);
		} else
		{
			return fakeBot;
		}
	}
	
	
	/**
	 * Return the opponent bot being the most dangerous one after the ball getter
	 * 
	 * @return
	 */
	public TrackedBot getOpponentPassReceiver()
	{
		if (dangerousOpponents.size() != 0)
		{
			return dangerousOpponents.get(1);
		} else
		{
			return fakeBot;
		}
	}
	
	
	/**
	 * Returns the opponent bot being the least dangerous one which will be the keeper
	 * 
	 * @return
	 */
	public TrackedBot getOpponentKeeper()
	{
		if (dangerousOpponents.size() != 0)
		{
			return dangerousOpponents.get(dangerousOpponents.size() - 1);
		} else
		{
			return fakeBot;
		}
	}
}
