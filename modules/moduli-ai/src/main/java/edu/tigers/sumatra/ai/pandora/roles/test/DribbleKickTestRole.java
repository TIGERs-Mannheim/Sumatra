package edu.tigers.sumatra.ai.pandora.roles.test;

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
	private IVector2 target;
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
			skill.setMoveCon(skill.getMoveCon().withObstacleAvoidanceMode(EObstacleAvoidanceMode.AGGRESSIVE));
			skill.setStrongDribblerContactNeeded(true);
			skill.setTarget(target);
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
			skill.setMoveBackwards(true);

			IVector2 botStatePos = getBot().getBotState().getPos();
			double botStateOrient = getBot().getBotState().getOrientation();
			IVector2 botStateKickerPos = botStatePos.addNew(Vector2.fromAngle(botStateOrient)
					.scaleTo(getBot().getCenter2DribblerDist()));
			Optional<IRatedTarget> bestGoalKick = getRatedTarget(botStateKickerPos);

			boolean canScoreGoalHere =
					bestGoalKick.isPresent() && canGoalBeScored(bestGoalKick.get(), canShootClearanceTimer);

			skill.setDestination(dribbleToPos);
			if (canScoreGoalHere)
			{
				// set real target + enable kick if target orientation reached
				skill.setTarget(bestGoalKick.get().getTarget());
				skill.setKickIfTargetOrientationReached(true);
			} else
			{
				// set default target if no on the fly target available yet
				skill.setTarget(target);
				skill.setKickIfTargetOrientationReached(false);
			}
			skill.setMoveCon(skill.getMoveCon().withObstacleAvoidanceMode(EObstacleAvoidanceMode.SAFE));
		}


		private Optional<IRatedTarget> getRatedTarget(IVector2 botStateKickerPos)
		{
			var rater = AngleRangeRater.forGoal(Geometry.getGoalTheir());
			rater.setObstacles(getWFrame().getOpponentBots().values());
			rater.setKeeperID(getAiFrame().getKeeperOpponentId());
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
