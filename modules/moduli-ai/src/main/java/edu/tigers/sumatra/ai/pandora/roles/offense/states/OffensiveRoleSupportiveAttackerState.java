/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 26, 2014
 * Author(s): Mark Geiger <Mark.Geiger@dlr.de>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.pandora.roles.offense.states;

import edu.tigers.sumatra.ai.data.OffensiveStrategy.EOffensiveStrategy;
import edu.tigers.sumatra.ai.data.math.AiMath;
import edu.tigers.sumatra.ai.pandora.roles.offense.OffensiveRole;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.skillsystem.skills.AMoveToSkill;
import edu.tigers.sumatra.statemachine.IRoleState;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * The Offensive role is always ball oriented.
 * 
 * @author Mark Geiger <Mark.Geiger@dlr.de>
 */
public class OffensiveRoleSupportiveAttackerState extends AOffensiveRoleState implements IRoleState
{
	
	// -------------------------------------------------------------------------- //
	// --- variables and constants ---------------------------------------------- //
	// -------------------------------------------------------------------------- //
	
	// ----------------------------------------------------------------------- //
	// -------------------- functions ---------------------------------------- //
	// ----------------------------------------------------------------------- //
	
	/**
	 * @param role
	 */
	public OffensiveRoleSupportiveAttackerState(final OffensiveRole role)
	{
		super(role);
	}
	
	
	private AMoveToSkill skill = null;
	
	
	@Override
	public void doExitActions()
	{
		
	}
	
	
	@Override
	public void doEntryActions()
	{
		skill = AMoveToSkill.createMoveToSkill();
		setNewSkill(skill);
	}
	
	
	@Override
	public void doUpdate()
	{
		IVector2 movePos = calcMovePosition();
		movePos = AiMath.adjustMovePositionWhenItsInvalid(getWFrame(), getBotID(), movePos);
		skill.getMoveCon().updateDestination(movePos);
	}
	
	
	@Override
	public Enum<? extends Enum<?>> getIdentifier()
	{
		return EOffensiveStrategy.SUPPORTIVE_ATTACKER;
	}
	
	
	private IVector2 calcMovePosition()
	{
		IVector2 ballPos = getWFrame().getBall().getPos();
		IVector2 goal = Geometry.getGoalOur().getGoalCenter();
		IVector2 dir = goal.subtractNew(ballPos).normalizeNew();
		if (!getAiFrame().getWorldFrame().getFoeBots().isEmpty())
		{
			ITrackedBot bot = AiMath.getNearestBot(getAiFrame().getWorldFrame().getFoeBots(), getWFrame().getBall()
					.getPos());
			
			if ((bot != null) && (bot.getPos().x() > ballPos.x()))
			{
				dir = ballPos.subtractNew(bot.getPos()).normalizeNew();
			}
			
			return ballPos.addNew(dir.multiplyNew(Geometry.getBotToBallDistanceStop()
					+ (Geometry.getBotRadius() * 2)));
		}
		return ballPos.addNew(new Vector2(-1, 0).multiplyNew(Geometry.getBotToBallDistanceStop()
				+ (Geometry.getBotRadius() * 2)));
	}
	
}
