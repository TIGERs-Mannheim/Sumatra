/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 26, 2014
 * Author(s): Mark Geiger <Mark.Geiger@dlr.de>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.pandora.roles.offense.states;

import edu.tigers.sumatra.ai.data.BotDistance;
import edu.tigers.sumatra.ai.data.OffensiveStrategy.EOffensiveStrategy;
import edu.tigers.sumatra.ai.data.math.AiMath;
import edu.tigers.sumatra.ai.pandora.roles.offense.OffensiveRole;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.skillsystem.skills.AMoveToSkill;
import edu.tigers.sumatra.skillsystem.skills.InterceptionSkill;
import edu.tigers.sumatra.statemachine.IRoleState;
import edu.tigers.sumatra.wp.data.Geometry;


/**
 * The Offensive role is always ball oriented.
 * 
 * @author Mark Geiger <Mark.Geiger@dlr.de>
 */
public class OffensiveRoleInterceptionState extends AOffensiveRoleState implements IRoleState
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
	public OffensiveRoleInterceptionState(final OffensiveRole role)
	{
		super(role);
	}
	
	
	private InterceptionSkill	intercept			= null;
	private AMoveToSkill			move					= null;
	private boolean				interceptActive	= false;
	
	
	@Override
	public void doExitActions()
	{
		
	}
	
	
	@Override
	public void doEntryActions()
	{
		intercept = new InterceptionSkill();
		move = AMoveToSkill.createMoveToSkill();
		setNewSkill(move);
	}
	
	
	@Override
	public void doUpdate()
	{
		IVector2 movePos = calcMovePosition();
		if ((GeoMath.distancePP(movePos, getPos()) < 50) && (interceptActive == false))
		{
			setNewSkill(intercept);
			interceptActive = true;
		}
		movePos = AiMath.adjustMovePositionWhenItsInvalid(getWFrame(), getBotID(), movePos);
		move.getMoveCon().updateDestination(movePos);
		BotDistance nearestEnemyBot = getAiFrame().getTacticalField().getEnemyClosestToBall();
		if (nearestEnemyBot != null)
		{
			if (nearestEnemyBot.getBot() != null)
			{
				intercept.setNearestEnemyBotPos(nearestEnemyBot.getBot().getPos());
			} else
			{
				intercept.setNearestEnemyBotPos(null);
			}
		} else
		{
			intercept.setNearestEnemyBotPos(null);
		}
	}
	
	
	@Override
	public Enum<? extends Enum<?>> getIdentifier()
	{
		return EOffensiveStrategy.INTERCEPT;
	}
	
	
	private IVector2 calcMovePosition()
	{
		IVector2 ballPos = getWFrame().getBall().getPos();
		IVector2 goal = Geometry.getGoalOur().getGoalCenter();
		IVector2 dir = goal.subtractNew(ballPos).normalizeNew();
		return ballPos.addNew(dir.multiplyNew(Geometry.getBotToBallDistanceStop()
				+ (Geometry.getBotRadius() * 2)));
	}
	
}
