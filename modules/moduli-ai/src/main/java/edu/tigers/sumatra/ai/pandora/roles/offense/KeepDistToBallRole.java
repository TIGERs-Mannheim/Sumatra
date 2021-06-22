/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.offense;

import edu.tigers.sumatra.ai.common.KeepDistanceToBall;
import edu.tigers.sumatra.ai.common.PointChecker;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.math.line.v2.LineMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.skillsystem.skills.MoveToSkill;


/**
 * Keep distance to the ball while preferring to stand between ball and own goal center.
 */
public class KeepDistToBallRole extends ARole
{
	private final KeepDistanceToBall keepDistanceToBall = new KeepDistanceToBall(new PointChecker()
			.checkBallDistances()
			.checkInsideField()
			.checkNotInPenaltyAreas()
			.checkPointFreeOfBots());


	public KeepDistToBallRole()
	{
		super(ERole.KEEP_DIST_TO_BALL);

		setInitialState(new DefaultState());
	}


	private class DefaultState extends RoleState<MoveToSkill>
	{
		public DefaultState()
		{
			super(MoveToSkill::new);
		}


		@Override
		protected void onUpdate()
		{
			double distance = RuleConstraints.getStopRadius() + Geometry.getBotRadius() + Geometry.getBallRadius() + 10;
			IVector2 dest = LineMath.stepAlongLine(getBall().getPos(), Geometry.getGoalOur().getCenter(), distance);
			skill.updateLookAtTarget(getBall());
			skill.updateDestination(keepDistanceToBall.findNextFreeDest(getAiFrame(), dest, getBotID()));
		}
	}
}
