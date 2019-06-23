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

import java.util.LinkedList;

import edu.dhbw.mannheim.tigers.sumatra.model.data.Circlef;
import edu.dhbw.mannheim.tigers.sumatra.model.data.TrackedBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.EBallPossession;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.EWAI;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.defense.ManToManMarkerRole;


/**
 * Handles 2 offensive bots who will block opponents that are free
 * and ready to receive passes from a freekick. Is triggered
 * on FreeKickEnemies Command.
 * 
 * @author Malte
 * 
 */
public class FreekickMarkerPlay extends APlay
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**  */
	private static final long	serialVersionUID	= 993130446021439221L;
	
	private ManToManMarkerRole	leftMarker;
	private ManToManMarkerRole	rightMarker;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public FreekickMarkerPlay(AIInfoFrame aiFrame)
	{
		super(EPlay.FREEKICK_MARKER, aiFrame);
		
		leftMarker = new ManToManMarkerRole(EWAI.LEFT);
		addAggressiveRole(leftMarker);
		
		rightMarker = new ManToManMarkerRole(EWAI.RIGHT);
		addAggressiveRole(rightMarker);
		
		// I initialize my positions myself!!! (Update the targets)
		updateTargets(aiFrame, rightMarker, leftMarker);
		
		// Set the chosen targets as init position!
		rightMarker.initDestination(rightMarker.getTarget());
		leftMarker.initDestination(leftMarker.getTarget());
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	protected void beforeUpdate(AIInfoFrame f)
	{
		updateTargets(f, rightMarker, leftMarker);
	}
	
	
	private void updateTargets(AIInfoFrame f, ManToManMarkerRole rightMarker, ManToManMarkerRole leftMarker)
	{
		// Can this Play be played? Are there enough foe Bots?
		Circlef c = new Circlef(f.worldFrame.ball.pos, AIConfig.getGeometry().getBotRadius());
		LinkedList<TrackedBot> validFoeBots = new LinkedList<TrackedBot>();
		for (TrackedBot foeBot : f.worldFrame.foeBots.values())
		{
			if (!c.isPointInShape(foeBot.pos))
			{
				validFoeBots.add(foeBot);
			}
		}
		// No, it can not.
		if (validFoeBots.size() <= 1)
		{
			changeToFailed();
			leftMarker.updateTarget(leftMarker.getPos(f));
			rightMarker.updateTarget(leftMarker.getPos(f));
		}
		// Yes, it can!
		else
		{
			TrackedBot leftEnemy = null;
			TrackedBot rightEnemy = null;
			for (TrackedBot bot : validFoeBots)
			{
				if (leftEnemy == null)
				{
					leftEnemy = bot;
					continue;
				} else if (rightEnemy == null)
				{
					rightEnemy = bot;
					continue;
				}
				
				// switch
				if (leftEnemy.pos.y < rightEnemy.pos.y)
				{
					TrackedBot tmp = leftEnemy;
					leftEnemy = rightEnemy;
					rightEnemy = tmp;
				}
				
				if (leftEnemy.pos.y < bot.pos.y)
				{
					leftEnemy = bot;
				}
				
				if (rightEnemy.pos.y > bot.pos.y)
				{
					rightEnemy = bot;
				}
			}
			leftMarker.updateTarget(leftEnemy);
			rightMarker.updateTarget(rightEnemy);
			
			//+++++++++++++++HACK+++++++++++++++++
//			leftMarker.updateTarget(f.tacticalInfo.getDangerousOpponents().get(1));
//			rightMarker.updateTarget(f.tacticalInfo.getDangerousOpponents().get(2));
			TrackedBot leftTarget = f.tacticalInfo.getDangerousOpponents().get(1);
			TrackedBot rightTarget = f.tacticalInfo.getDangerousOpponents().get(2);
			Vector2 left = new Vector2(leftTarget.pos);
			Vector2 right = new Vector2(rightTarget.pos);
			left.add(leftTarget.vel.multiplyNew(500));
			right.add(rightTarget.vel.multiplyNew(500));
			
			leftMarker.updateTarget(left);
			rightMarker.updateTarget(right);
			

			//The bots are not allowed to drive inside a circle 500mm around the ball!
			Circlef fc = new Circlef(f.worldFrame.ball.pos, 500+AIConfig.getGeometry().getBotRadius());
			rightMarker.setForbiddenCircle(fc);
			leftMarker.setForbiddenCircle(fc);
		}
	}
	

	@Override
	protected void afterUpdate(AIInfoFrame f)
	{
		if (f.tacticalInfo.getBallPossesion().getEBallPossession() == EBallPossession.WE
				|| f.tacticalInfo.getBallPossesion().getEBallPossession() == EBallPossession.BOTH
				|| f.worldFrame.ball.vel.getLength2() > 0.1)
		{
			changeToSucceeded();
		}
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
