/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.offense;

import edu.tigers.sumatra.ai.metis.targetrater.AngleRangeRater;
import edu.tigers.sumatra.ai.metis.targetrater.IRatedTarget;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.pathfinder.EObstacleAvoidanceMode;
import edu.tigers.sumatra.skillsystem.skills.ESkillState;
import edu.tigers.sumatra.skillsystem.skills.ProtectiveGetBallSkill;
import edu.tigers.sumatra.skillsystem.skills.dribbling.DribbleKickSkill;
import edu.tigers.sumatra.time.TimestampTimer;
import lombok.Setter;

import java.util.Optional;


public class DribbleKickTestRole extends ARole
{
	@Setter
	private IVector2 approachFrom;
	@Setter
	private IVector2 kickTarget;
	@Setter
	private IVector2 dribbleToPos;


	private final TimestampTimer canShootClearanceTimer = new TimestampTimer(0.10);


	public DribbleKickTestRole()
	{
		super(ERole.DRIBBLE_KICK_TEST);

		var getBallState = new GetBallLikeProtectState();
		var dribbleKickState = new DribbleKickState();

		getBallState.addTransition(ESkillState.SUCCESS, dribbleKickState);
		dribbleKickState.addTransition(ESkillState.FAILURE, getBallState);

		setInitialState(getBallState);
	}


	private class GetBallLikeProtectState extends RoleState<ProtectiveGetBallSkill>
	{
		public GetBallLikeProtectState()
		{
			super(ProtectiveGetBallSkill::new);
		}


		@Override
		protected void onUpdate()
		{
			skill.getMoveCon().setObstacleAvoidanceMode(EObstacleAvoidanceMode.AGGRESSIVE);
			skill.setStrongDribblerContactNeeded(true);
			skill.setProtectionTarget(approachFrom);
		}
	}

	private class DribbleKickState extends RoleState<DribbleKickSkill>
	{
		public DribbleKickState()
		{
			super(DribbleKickSkill::new);
		}


		@Override
		protected void onUpdate()
		{

			IVector2 botStatePos = getBot().getBotState().getPos();
			double botStateOrient = getBot().getBotState().getOrientation();
			IVector2 botStateKickerPos = botStatePos.addNew(Vector2.fromAngle(botStateOrient)
					.scaleTo(getBot().getCenter2DribblerDist()));
			Optional<IRatedTarget> bestGoalKick = getRatedTarget(botStateKickerPos);

			boolean canScoreGoalHere =
					bestGoalKick.isPresent() && canGoalBeScored(bestGoalKick.get(), canShootClearanceTimer);

			if (bestGoalKick.isPresent() && canScoreGoalHere)
			{
				skill.setTarget(bestGoalKick.get().getTarget());
			} else
			{
				skill.setTarget(kickTarget);
			}
			skill.setDestination(dribbleToPos);
			skill.setKickIfTargetOrientationReached(canScoreGoalHere);
		}


		private Optional<IRatedTarget> getRatedTarget(IVector2 botStateKickerPos)
		{
			var rater = AngleRangeRater.forGoal(Geometry.getGoalTheir());
			rater.setObstacles(getWFrame().getOpponentBots().values());
			return rater.rate(botStateKickerPos);
		}


		private boolean canGoalBeScored(IRatedTarget bestGoalKick, TimestampTimer timer)
		{
			var hasGoodScoreChance = bestGoalKick.getScore() > 0.6;
			timer.update(getWFrame().getTimestamp());
			if (!timer.isRunning() && hasGoodScoreChance)
			{
				timer.start(getWFrame().getTimestamp());
			} else if (!hasGoodScoreChance)
			{
				timer.reset();
			}
			return timer.isTimeUp(getWFrame().getTimestamp());
		}
	}
}
