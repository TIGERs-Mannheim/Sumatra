/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 13.02.2011
 * Author(s): Malte
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.standard.penalty;

import edu.dhbw.mannheim.tigers.sumatra.model.data.Referee.SSL_Referee.Command;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.defense.PassiveDefenderRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.standard.penalty.KeeperPenaltyThemRole;


/**
 * Play which is chosen, when opponent team gets a penalty.<br>
 * Requires:<li>1 {@link KeeperPenaltyThemRole}</li> <li>{@link PassiveDefenderRole}s</li> of
 * the shooter!
 * 
 * @author Malte
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class APenaltyThemPlay extends APenaltyPlay
{
	private static final float				DIST_BALL_PENMARK_TRESHOLD	= 150f;
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private final KeeperPenaltyThemRole	keeper;
	private boolean							ready;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param aiFrame
	 * @param numAssignedRoles
	 */
	public APenaltyThemPlay(AIInfoFrame aiFrame, int numAssignedRoles)
	{
		super(aiFrame, numAssignedRoles);
		
		keeper = new KeeperPenaltyThemRole();
		addDefensiveRole(keeper, AIConfig.getGeometry().getGoalOur().getGoalCenter());
		
		ready = false;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	protected void beforeUpdate(AIInfoFrame currentFrame)
	{
		TrackedBot shooter = null;
		for (final TrackedBot bot : currentFrame.worldFrame.foeBots.values())
		{
			if (shooter == null)
			{
				shooter = bot;
			} else
			{
				if (bot.getPos().x() < shooter.getPos().x())
				{
					shooter = bot;
				}
			}
		}
		keeper.setShooter(shooter);
		
		if ((currentFrame.refereeMsg != null) && (currentFrame.refereeMsg.getCommand() == Command.NORMAL_START))
		{
			ready = true;
			keeper.setReady(ready);
		}
	}
	
	
	@Override
	protected void afterUpdate(AIInfoFrame currentFrame)
	{
		if (ready
				&& (GeoMath.distancePP(AIConfig.getGeometry().getPenaltyMarkOur(), currentFrame.worldFrame.ball.getPos()) > DIST_BALL_PENMARK_TRESHOLD))
		{
			finish();
		}
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
