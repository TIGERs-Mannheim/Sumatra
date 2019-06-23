/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 04.02.2011
 * Author(s):
 * FlorianS
 * MalteM
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.standard.kickoff;

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
 * This play shall be selected if the referee command 'KickOffEnemies' is sent
 * and brings three bots in position for kick off in case we are the defending
 * team. This includes to block one foe bot on each side and move another bot
 * on an line between center of field and center of our goal in a distance of
 * 500 mm to the ball. The term 'left' is supposed to mean left from our
 * keeper's point of view (positive y-coordinates)
 * 
 * @author FlorianS, MalteM
 * 
 */
public class PositioningOnKickOffThemPlay extends APlay
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**  */
	private static final long		serialVersionUID	= -3187865985989256419L;
	private ManToManMarkerRole		leftBlocker;
	private ManToManMarkerRole		rightBlocker;
	
	
	private final float				MAXIMUM_LENGTH		= AIConfig.getPlays().getPositioningOnKickOffThem()
																			.getMaximumLength();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public PositioningOnKickOffThemPlay(AIInfoFrame aiFrame)
	{
		super(EPlay.POSITIONING_ON_KICK_OFF_THEM, aiFrame);
		Vector2 initPos = new Vector2(AIConfig.getGeometry().getCenter());

		
		leftBlocker = new ManToManMarkerRole(EWAI.LEFT);
		leftBlocker.setForbiddenCircle(AIConfig.getGeometry().getCenterCircle());
		leftBlocker.setMaxLength(MAXIMUM_LENGTH);
		addAggressiveRole(leftBlocker, initPos.addNew(new Vector2(0, 800)));
		
		rightBlocker = new ManToManMarkerRole(EWAI.RIGHT);
		rightBlocker.setForbiddenCircle(AIConfig.getGeometry().getCenterCircle());
		rightBlocker.setMaxLength(MAXIMUM_LENGTH);
		addAggressiveRole(rightBlocker, initPos.addNew(new Vector2(0, -800)));
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	protected void beforeUpdate(AIInfoFrame currentFrame)
	{
		super.beforeUpdate(currentFrame);
		TrackedBot enemyLeft = null;
		TrackedBot enemyRight = null;
		
		for (TrackedBot bot : currentFrame.worldFrame.foeBots.values())
		{
			// Initialize enemy bots
			if (enemyLeft == null)
			{
				enemyLeft = bot;
				continue;
			}
			if (enemyRight == null)
			{
				enemyRight = bot;
				continue;
			}
			
			// Switch'em?
			if (enemyLeft.pos.y() < enemyRight.pos.y())
			{
				TrackedBot tmp = enemyLeft;
				enemyLeft = enemyRight;
				enemyRight = tmp;
			}
			
			// Assign new bot
			if (bot.pos.y > enemyLeft.pos.y)
			{
				enemyLeft = bot;
			} else if (bot.pos.y < enemyRight.pos.y)
			{
				enemyRight = bot;
			} else
			{
				// Enemy bot must be somewhere in the middle...do nothing.
			}
		}
		
		leftBlocker.updateTarget(enemyLeft);
		rightBlocker.updateTarget(enemyRight);
	}
	

	@Override
	protected void afterUpdate(AIInfoFrame frame)
	{
		if(frame.tacticalInfo.getBallPossesion().getEBallPossession() == EBallPossession.WE)
		{
			changeToSucceeded();
		}
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
}
