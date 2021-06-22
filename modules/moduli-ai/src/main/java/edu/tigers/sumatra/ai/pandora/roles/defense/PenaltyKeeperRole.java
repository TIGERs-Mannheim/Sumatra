/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.pandora.roles.defense;

import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.VectorMath;
import edu.tigers.sumatra.skillsystem.skills.MoveToSkill;
import edu.tigers.sumatra.skillsystem.skills.PenaltyKeeperSkill;
import edu.tigers.sumatra.statemachine.DefaultEvents;
import edu.tigers.sumatra.statemachine.IState;
import edu.tigers.sumatra.wp.data.DynamicPosition;


/**
 * Prepare penalty they -> Keeper to goal center
 * normal start -> keeper left / right on goal line (shooter is allowed to shoot)
 */
public class PenaltyKeeperRole extends ARole
{
	public PenaltyKeeperRole()
	{
		super(ERole.PENALTY_KEEPER);
		IState moveToGoal = new MoveToGoalCenter();
		setInitialState(moveToGoal);
		addTransition(moveToGoal, DefaultEvents.DONE, new BlockShootingLine());
	}


	private BotID getOpponentShooter()
	{
		return getAiFrame().getTacticalField().getOpponentClosestToBall().getBotId();
	}


	private class MoveToGoalCenter extends RoleState<MoveToSkill>
	{
		MoveToGoalCenter()
		{
			super(MoveToSkill::new);
		}


		@Override
		protected void onInit()
		{
			skill.getMoveCon().setPenaltyAreaOurObstacle(false);
			skill.updateDestination(Geometry.getGoalOur().getCenter());
		}


		@Override
		protected void onUpdate()
		{
			if (VectorMath.distancePP(getPos(), skill.getDestination()) < 100
					&& getOpponentShooter().isBot())
			{
				triggerEvent(DefaultEvents.DONE);
			}
		}
	}


	private class BlockShootingLine extends RoleState<PenaltyKeeperSkill>
	{
		BlockShootingLine()
		{
			super(PenaltyKeeperSkill::new);
		}


		@Override
		protected void onInit()
		{
			skill = new PenaltyKeeperSkill();
			setNewSkill(skill);
		}


		@Override
		protected void onUpdate()
		{
			if (getOpponentShooter() != null)
			{
				skill.setShooterPos(new DynamicPosition(getWFrame().getBot(getOpponentShooter()).getPos()));
			}
		}
	}
}
