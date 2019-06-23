/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 09.04.2011
 * Author(s): Malte
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.standard.freekick;

import java.awt.Color;
import java.util.LinkedList;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.Circlef;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.DrawableCircle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.standard.AStandardPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.defense.ManToManMarkerRole;


/**
 * Handles 2 offensive bots who will block dangerous opponents
 * 
 * @author Malte
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class FreekickMarkerPlay extends AStandardPlay
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final float			BALL_MOVE_TOL				= 50;
	
	private final float					botToBallDistanceStop	= AIConfig.getGeometry().getBotToBallDistanceStop();
	
	private final ManToManMarkerRole	leftMarker;
	private final ManToManMarkerRole	rightMarker;
	
	/** save first pos of ball, so we can check if ball moved away which means, ball is back in play */
	private final IVector2				ballInitialPos;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param aiFrame
	 * @param numAssignedRoles
	 */
	public FreekickMarkerPlay(AIInfoFrame aiFrame, int numAssignedRoles)
	{
		super(aiFrame, numAssignedRoles);
		setTimeout(2);
		ballInitialPos = aiFrame.worldFrame.ball.getPos();
		
		List<TrackedBot> opps = aiFrame.tacticalInfo.getDangerousOpponents();
		if (opps.size() < getNumAssignedRoles())
		{
			// not enough opponents
			changeToFinished();
			leftMarker = new ManToManMarkerRole(new Vector2());
			rightMarker = new ManToManMarkerRole(new Vector2());
		} else
		{
			if (opps.get(0) != null)
			{
				leftMarker = new ManToManMarkerRole(opps.get(0).getPos());
				rightMarker = new ManToManMarkerRole(opps.get(0).getPos());
				// Set the chosen targets as init position!
				addAggressiveRole(leftMarker, leftMarker.getTarget());
				addAggressiveRole(rightMarker, rightMarker.getTarget());
			} else
			{
				changeToFinished();
				leftMarker = new ManToManMarkerRole(new Vector2());
				rightMarker = new ManToManMarkerRole(new Vector2());
			}
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	protected void beforeUpdate(AIInfoFrame f)
	{
		updateTargets(f, rightMarker, leftMarker);
		f.addDebugShape(new DrawableCircle(new Circlef(ballInitialPos, botToBallDistanceStop), Color.red));
	}
	
	
	private void updateTargets(AIInfoFrame f, ManToManMarkerRole rightMarker, ManToManMarkerRole leftMarker)
	{
		// Can this Play be played? Are there enough foe Bots?
		final Circlef c = new Circlef(f.worldFrame.ball.getPos(), AIConfig.getGeometry().getBotRadius());
		final LinkedList<TrackedBot> validFoeBots = new LinkedList<TrackedBot>();
		for (final TrackedBot foeBot : f.worldFrame.foeBots.values())
		{
			if (!c.isPointInShape(foeBot.getPos()))
			{
				validFoeBots.add(foeBot);
			}
		}
		// No, it can not.
		if (validFoeBots.size() <= 1)
		{
			changeToFinished();
			leftMarker.updateTarget(leftMarker.getPos());
			rightMarker.updateTarget(rightMarker.getPos());
		}
		// Yes, it can!
		else
		{
			final TrackedBot leftTarget = f.tacticalInfo.getDangerousOpponents().get(1);
			final TrackedBot rightTarget = f.tacticalInfo.getDangerousOpponents().get(2);
			final Vector2 left = new Vector2(leftTarget.getPos());
			final Vector2 right = new Vector2(rightTarget.getPos());
			left.add(leftTarget.getVel().multiplyNew(botToBallDistanceStop));
			right.add(rightTarget.getVel().multiplyNew(botToBallDistanceStop));
			
			leftMarker.updateTarget(left);
			rightMarker.updateTarget(right);
			
			// The bots are not allowed to drive inside a circle 500mm around the ball!
			final Circlef fc = new Circlef(f.worldFrame.ball.getPos(), botToBallDistanceStop
					+ AIConfig.getGeometry().getBotRadius());
			rightMarker.setForbiddenCircle(fc);
			leftMarker.setForbiddenCircle(fc);
		}
	}
	
	
	@Override
	protected void afterUpdate(AIInfoFrame aiFrame)
	{
		if (!ballInitialPos.equals(aiFrame.worldFrame.ball.getPos(), BALL_MOVE_TOL))
		{
			changeToFinished();
		}
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
