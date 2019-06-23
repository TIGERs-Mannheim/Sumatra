/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 9, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.standard.kickoff;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AiMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.Circlef;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.standard.AStandardPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.defense.ManToManMarkerRole;


/**
 * This play shall be selected if the referee command 'KickOffEnemies' is sent
 * and brings n bots in position for kick off in case we are the defending
 * team.
 * 
 * @author Frieder
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class PositioningOnKickOffThem extends AStandardPlay
{
	private static final Logger				log								= Logger.getLogger(PositioningOnKickOffThem.class
																									.getName());
	
	/** how far must the ball be away from mid point to finish this play */
	private static final float					BALL_MIDPOINT_TOL				= 150;
	/**
	 * Additional distance whcich is added to the center circle. This should ensure, that no bot will touch the circle.
	 * We got a warning that we touch the cirle.
	 */
	private static final float					SECURITY_CIRCLE_DISTANCE	= 200;
	private static final List<Float>			BLOCKER_POSITIONS				= new LinkedList<Float>();
	
	
	private final List<ManToManMarkerRole>	roles								= new ArrayList<ManToManMarkerRole>();
	
	
	static
	{
		BLOCKER_POSITIONS.add(AIConfig.getGeometry().getCenter().y());
		BLOCKER_POSITIONS.add((AIConfig.getGeometry().getFieldWidth() / 4) * 3);
		BLOCKER_POSITIONS.add((AIConfig.getGeometry().getFieldWidth() / 4) * -3);
		BLOCKER_POSITIONS.add((AIConfig.getGeometry().getFieldWidth() / 4) * 1);
		BLOCKER_POSITIONS.add((AIConfig.getGeometry().getFieldWidth() / 4) * -1);
	}
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param aiFrame
	 * @param numAssignedRoles
	 */
	public PositioningOnKickOffThem(AIInfoFrame aiFrame, int numAssignedRoles)
	{
		super(aiFrame, numAssignedRoles);
		final float maximumLength = AIConfig.getGeometry().getCenter().x();
		
		Iterator<Float> it = BLOCKER_POSITIONS.iterator();
		for (int i = 0; i < getNumAssignedRoles(); i++)
		{
			ManToManMarkerRole role = new ManToManMarkerRole(new Vector2());
			role.setForbiddenCircle(new Circlef(AIConfig.getGeometry().getCenter(), AIConfig.getGeometry()
					.getCenterCircleRadius() + SECURITY_CIRCLE_DISTANCE));
			role.setMaxLength(maximumLength);
			addAggressiveRole(role, new Vector2(0, it.next()));
			roles.add(role);
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	protected void beforeUpdate(AIInfoFrame currentFrame)
	{
		Iterator<Float> it = BLOCKER_POSITIONS.iterator();
		final List<TrackedBot> blockedBots = new LinkedList<TrackedBot>();
		
		for (ManToManMarkerRole role : roles)
		{
			final IVector2 pos = new Vector2(0, it.next());
			List<BotID> foesSorted = AiMath.getFoeBotsNearestToPointSorted(currentFrame, pos);
			for (BotID botId : foesSorted)
			{
				TrackedBot foeBot = currentFrame.worldFrame.foeBots.get(botId);
				if (!blockedBots.contains(foeBot))
				{
					role.updateTarget(foeBot);
					blockedBots.add(foeBot);
					break;
				}
			}
		}
	}
	
	
	@Override
	protected void afterUpdate(AIInfoFrame frame)
	{
		if (GeoMath.distancePP(frame.worldFrame.ball.getPos(), AIConfig.getGeometry().getCenter()) > BALL_MIDPOINT_TOL)
		{
			log.info("Ball left middle point. Play finished.");
			changeToFinished();
		}
	}
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
