/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 27, 2013
 * Author(s): Dirk Klostermann <klostermannn@googlemail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus;

import java.util.Map;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.move.MovementCon;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.data.Path;


/**
 * data holder for the input data passed to the path planning
 * 
 * @author Dirk Klostermann <klostermannn@googlemail.com>
 * 
 */
public class PathFinderInput
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private boolean						active	= true;
	
	private final WorldFrame			wFrame;
	private final BotID					botId;
	private final IVector2				target;
	private final float					dstOrient;
	private final Map<BotID, Path>	existingPathes;
	private final long					timestamp;
	private final float					currentTimeOnSpline;
	
	private final FieldInformation	fieldInfo;
	
	private final MovementCon			moveCon;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param wFrame current worldframe
	 * @param botId id of bot
	 * @param existingPathes a list of the pathes from the last frame (all pathes are null at the beginning)
	 * @param currentTimeOnSpline
	 * @param moveCon
	 */
	public PathFinderInput(WorldFrame wFrame, BotID botId, Map<BotID, Path> existingPathes, float currentTimeOnSpline,
			MovementCon moveCon)
	{
		super();
		this.wFrame = wFrame;
		this.botId = botId;
		this.existingPathes = existingPathes;
		this.currentTimeOnSpline = currentTimeOnSpline;
		timestamp = System.nanoTime();
		this.moveCon = moveCon;
		
		TrackedTigerBot bot = wFrame.getTiger(botId);
		if (moveCon.getAngleCon().isActive())
		{
			dstOrient = moveCon.getAngleCon().getTargetAngle();
		} else
		{
			dstOrient = bot.getAngle();
		}
		
		if (moveCon.getDestCon().isActive())
		{
			target = moveCon.getDestCon().getDestination();
		} else
		{
			target = bot.getPos();
		}
		
		Vector2f goal = new Vector2f(target);
		fieldInfo = new FieldInformation(wFrame, botId, moveCon.isBallObstacle(), moveCon.isBotsObstacle(),
				moveCon.isPenaltyAreaAllowed(), moveCon.isGoalPostObstacle(), goal);
		fieldInfo.setIgnoredBots(moveCon.getIgnoredBots());
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * @return the wFrame
	 */
	public final WorldFrame getwFrame()
	{
		return wFrame;
	}
	
	
	/**
	 * @return the botId
	 */
	public BotID getBotId()
	{
		return botId;
	}
	
	
	/**
	 * @return the dstOrient
	 */
	public float getDstOrient()
	{
		return dstOrient;
	}
	
	
	/**
	 * @return the existingPathes
	 */
	public Map<BotID, Path> getExistingPathes()
	{
		return existingPathes;
	}
	
	
	/**
	 * @return the timestamp
	 */
	public long getTimestamp()
	{
		return timestamp;
	}
	
	
	/**
	 * @return the fieldInfo
	 */
	public FieldInformation getFieldInfo()
	{
		return fieldInfo;
	}
	
	
	/**
	 * @return the currentTimeOnSpline
	 */
	public float getCurrentTimeOnSpline()
	{
		return currentTimeOnSpline;
	}
	
	
	/**
	 * @return the target
	 */
	public IVector2 getTarget()
	{
		return target;
	}
	
	
	/**
	 * @return the active
	 */
	public boolean isActive()
	{
		return active;
	}
	
	
	/**
	 * @param active the active to set
	 */
	public void setActive(boolean active)
	{
		this.active = active;
	}
	
	
	/**
	 * @return the moveCon
	 */
	public final MovementCon getMoveCon()
	{
		return moveCon;
	}
	
	
	/**
	 * @return the avoidedCollisionsSeries
	 */
	public int getAvoidedCollisionsSeries()
	{
		return fieldInfo.getAvoidedCollisons();
	}
	
	
	/**
	 * @param avoidedCollisionsSeries the avoidedCollisionsSeries to set
	 */
	public void setAvoidedCollisionsSeries(int avoidedCollisionsSeries)
	{
		fieldInfo.setAvoidedCollisons(avoidedCollisionsSeries);
	}
	
	
	/**
	 * @param obstacleFoundByLastSpline the obstacleFoundByLastSpline to set
	 */
	public void setObstacleFoundByLastSpline(IVector2 obstacleFoundByLastSpline)
	{
		fieldInfo.putBotsInList(obstacleFoundByLastSpline);
	}
	
	
}