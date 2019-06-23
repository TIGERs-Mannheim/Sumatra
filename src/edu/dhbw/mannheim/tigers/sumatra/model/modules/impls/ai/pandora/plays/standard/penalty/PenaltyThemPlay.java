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

import edu.dhbw.mannheim.tigers.sumatra.model.data.AVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.ERefereeCommand;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Line;
import edu.dhbw.mannheim.tigers.sumatra.model.data.TrackedBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.defense.PassiveDefenderRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.standards.penalty.KeeperPenaltyThemRole;


/**
 * Play which is choosen, when opponent team gets a penalty.<br>
 * Requires:<li>1 {@link KeeperPenaltyThemRole}</li> <li>4 {@link PassiveDefenderRole}</li> TODO: Handle alignment of
 * the shooter!
 * 
 * @author Malte
 */
public class PenaltyThemPlay extends APlay
{
	/**  */
	private static final long		serialVersionUID	= -8454158178552382074L;
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private KeeperPenaltyThemRole	keeper;
	private PassiveDefenderRole	defender1;
	private PassiveDefenderRole	defender2;
	private PassiveDefenderRole	defender3;
	private PassiveDefenderRole	defender4;
	
	private Line						defenderLine;
	
	private boolean					ready;
	private final int[]				initPositions = new int[] {600, 300, -300, -600};
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public PenaltyThemPlay(AIInfoFrame aiFrame)
	{
		super(EPlay.PENALTY_THEM, aiFrame);
		keeper = new KeeperPenaltyThemRole();
//		Vector2f pp = AIConfig.getGeometry().getPenaltyMarkOur();
		/**
		 * Line parallel to our goal line shifted 500mm.
		 * "The robots other than the kicker are located:
		 * - inside the field of play
		 * - behind a line parallel to the goal line and 400 mm behind the penalty mark"
		 */
		defenderLine = new Line(AIConfig.getGeometry().getPenaltyMarkOur(), AVector2.Y_AXIS);
		defenderLine.setSupportVector(new Vector2(defenderLine.supportVector().x() + 1000, defenderLine.supportVector()
				.y()));
		
		addDefensiveRole(keeper, AIConfig.getGeometry().getGoalOur().getGoalCenter());
		defender1 = new PassiveDefenderRole();
		addDefensiveRole(defender1);
		defender2 = new PassiveDefenderRole();
		addDefensiveRole(defender2);
		defender3 = new PassiveDefenderRole();
		addDefensiveRole(defender3);
		defender4 = new PassiveDefenderRole();
		addDefensiveRole(defender4);

		setDest(defender1, defender2, defender3, defender4);
		ready = false;

	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	protected void beforeUpdate(AIInfoFrame currentFrame)
	{
		TrackedBot shooter = null;
		for (TrackedBot bot : currentFrame.worldFrame.foeBots.values())
		{
			if (shooter == null)
			{
				shooter = bot;
			}
			else
			{
				if (bot.pos.x < shooter.pos.x)
				{
					shooter = bot;
				}
			}
		}
		keeper.setShooter(shooter);
		
		if (currentFrame.refereeMsg != null && currentFrame.refereeMsg.cmd == ERefereeCommand.Ready)
		{
			ready = true;
			keeper.setReady(ready);
		}
		setDest(defender1, defender2, defender3, defender4);
		defender1.setTarget(new Vector2(currentFrame.worldFrame.ball.pos));
		defender2.setTarget(new Vector2(currentFrame.worldFrame.ball.pos));
		defender3.setTarget(new Vector2(currentFrame.worldFrame.ball.pos));
		defender4.setTarget(new Vector2(currentFrame.worldFrame.ball.pos));
		
	}
	
	
	private void setDest(PassiveDefenderRole... defenderRoles)
	{
		int i = 0;
		for (PassiveDefenderRole defRole : defenderRoles)
		{
			defRole.setDest(new Vector2(defenderLine.supportVector().x(), initPositions[i]));
			i++;
		}
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
