/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.keeper;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.common.KeepDistanceToBall;
import edu.tigers.sumatra.ai.common.PointChecker;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.kicking.Pass;
import edu.tigers.sumatra.ai.metis.offense.ballinterception.RatedBallInterception;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.pathfinder.TrajectoryGenerator;
import edu.tigers.sumatra.skillsystem.skills.CriticalKeeperSkill;
import edu.tigers.sumatra.skillsystem.skills.ESkillState;
import edu.tigers.sumatra.skillsystem.skills.GetBallContactSkill;
import edu.tigers.sumatra.skillsystem.skills.MoveToSkill;
import edu.tigers.sumatra.skillsystem.skills.MoveWithBallSkill;
import edu.tigers.sumatra.skillsystem.skills.RamboKeeperSkill;
import edu.tigers.sumatra.skillsystem.skills.TouchKickSkill;
import edu.tigers.sumatra.time.TimestampTimer;
import edu.tigers.sumatra.wp.data.ITrackedBot;

import java.util.Map;


/**
 * The Keeper protect the Goal and tries to catch every shoot into the goal.
 */
public class KeeperRole extends ARole
{

	@Configurable(comment = "Lower margin [mm] applied to penalty area. If the ball is inside, no placement necessary", defValue = "-200")
	private static double ballPlacementPAMarginLower = -200;

	@Configurable(comment = "Upper negative margin [mm] applied to penalty area. The ball is placed to a position inside", defValue = "-300")
	private static double ballPlacementPAMarginUpper = -300;

	@Configurable(comment = "Opponent ball dist to chill", defValue = "1000.0")
	private static double minOpponentBotDistToChill = 1000;

	@Configurable(comment = "[m/s] Max speed of ball in PenArea to consider it less dangerous.", defValue = "0.2")
	private static double maxBallSpeedToAllowSlowApproach = 0.2;


	public KeeperRole()
	{
		super(ERole.KEEPER);

		var defendState = new RoleState<>(CriticalKeeperSkill::new);
		var moveInFrontOfBallState = new MoveInFrontOfBallState();
		var getBallContactState = new RoleState<>(GetBallContactSkill::new);
		var moveWithBallState = new MoveWithBallState();
		var passState = new PassState();
		var interceptState = new InterceptRollingBallState();
		var ramboState = new RoleState<>(RamboKeeperSkill::new);
		var stopState = new KeeperStoppedState();
		var preparePenaltyState = new PreparePenaltyState();
		var moveToPenaltyAreaState = new MoveToPenaltyAreaState();

		setInitialState(defendState);

		stopState.addTransition(() -> !isStopped(), defendState);

		preparePenaltyState.addTransition(() -> !isPreparePenalty(), defendState);

		moveToPenaltyAreaState.addTransition(ESkillState.SUCCESS, defendState);
		moveToPenaltyAreaState.addTransition(this::isKeeperWellInsidePenaltyArea, defendState);
		moveToPenaltyAreaState.addTransition(this::isStopped, stopState);
		moveToPenaltyAreaState.addTransition(this::isPreparePenalty, preparePenaltyState);

		defendState.addTransition(this::ballCanBePassedOutOfPenaltyArea, passState);
		defendState.addTransition(this::canGoOut, ramboState);
		defendState.addTransition(this::isBallBetweenGoalyAndGoal, getBallContactState);
		defendState.addTransition("isOutsidePenaltyArea", this::isOutsidePenaltyArea, moveToPenaltyAreaState);
		defendState.addTransition(this::isStopped, stopState);
		defendState.addTransition(this::isPreparePenalty, preparePenaltyState);
		defendState.addTransition(this::canInterceptSafely, interceptState);

		passState.addTransition(this::isBallMoving, defendState);
		passState.addTransition(this::ballPlacementRequired, moveInFrontOfBallState);
		passState.addTransition(this::isStopped, stopState);
		passState.addTransition(this::isPreparePenalty, preparePenaltyState);

		interceptState.addTransition(this::hasInterceptionFailed, defendState);
		interceptState.addTransition(this::ballCanBePassedOutOfPenaltyArea, passState);
		interceptState.addTransition(this::isStopped, stopState);
		interceptState.addTransition(this::isPreparePenalty, preparePenaltyState);

		ramboState.addTransition(() -> isBallInPenaltyArea(0) || isGoalKick(), defendState);
		ramboState.addTransition(this::isStopped, stopState);
		ramboState.addTransition(this::isPreparePenalty, preparePenaltyState);

		moveInFrontOfBallState.addTransition(this::isBallMoving, defendState);
		moveInFrontOfBallState.addTransition(this::ballPlaced, defendState);
		moveInFrontOfBallState.addTransition(ESkillState.SUCCESS, getBallContactState);
		moveInFrontOfBallState.addTransition(this::isStopped, stopState);
		moveInFrontOfBallState.addTransition(this::isPreparePenalty, preparePenaltyState);

		getBallContactState.addTransition(ESkillState.SUCCESS, moveWithBallState);
		getBallContactState.addTransition(ESkillState.FAILURE, moveInFrontOfBallState);
		getBallContactState.addTransition(this::isStopped, stopState);
		getBallContactState.addTransition(this::isPreparePenalty, preparePenaltyState);

		moveWithBallState.addTransition(ESkillState.SUCCESS, defendState);
		moveWithBallState.addTransition(ESkillState.FAILURE, moveInFrontOfBallState);
		moveWithBallState.addTransition(this::isStopped, stopState);
		moveWithBallState.addTransition(this::isPreparePenalty, preparePenaltyState);

	}


	private boolean ballMovesTowardsMe()
	{
		var ballDir = getBall().getVel();
		var ballToBotDir = getPos().subtractNew(getBall().getPos());
		var angle = ballDir.angleToAbs(ballToBotDir).orElse(0.0);
		return angle < AngleMath.DEG_090_IN_RAD || getBall().getPos().distanceTo(getPos()) < 2 * Geometry.getBotRadius();
	}


	private boolean hasInterceptionFailed()
	{
		return !ballMovesTowardsMe() || !isBallInPenaltyArea(Geometry.getBallRadius());
	}


	private boolean isBallAimedForGoal()
	{
		return !Geometry.getGoalOur().getLineSegment().withMargin(4 * Geometry.getBotRadius())
				.intersect(getBall().getTrajectory().getTravelLine()).isEmpty();
	}


	private boolean canInterceptSafely()
	{
		var ballInterceptions = getAiFrame().getPrevFrame().getTacticalField().getRollingBallInterceptions();
		return isBallInPenaltyArea(0) && !isBallAimedForGoal() && ballInterceptions.containsKey(getBotID())
				&& canCatchBallInPenArea(ballInterceptions) && ballMovesTowardsMe();
	}


	private boolean canCatchBallInPenArea(Map<BotID, RatedBallInterception> ballInterceptions)
	{
		var botId = getBotID();
		return (ballInterceptions.get(botId).getCorridorLength() > 0.3
				|| ballInterceptions.get(botId).getMinCorridorSlackTime() < -0.3)
				&& Geometry.getPenaltyAreaOur().withMargin(-2 * Geometry.getBotRadius())
				.isPointInShape(ballInterceptions.get(botId).getBallInterception().getPos());
	}


	private boolean isStopped()
	{
		return getAiFrame().getGameState().isStoppedGame();
	}


	private boolean isPreparePenalty()
	{
		return getAiFrame().getGameState().isPreparePenaltyForThem();
	}


	private boolean isBallBetweenGoalyAndGoal()
	{
		boolean ballInPenArea = Geometry.getPenaltyAreaOur()
				.withMargin(ballPlacementPAMarginLower).isPointInShape(getBall().getPos());
		boolean isBallSlow = getBall().getVel().getLength() < maxBallSpeedToAllowSlowApproach;
		boolean isBehindKeeper = getBall().getPos().x() < getBot().getPos().x();
		boolean isStoppedGame = getAiFrame().getGameState().isStoppedGame();
		return ballInPenArea && isBallSlow && isBehindKeeper && !isStoppedGame;
	}


	private boolean ballCanBePassedOutOfPenaltyArea()
	{
		return getAiFrame().getTacticalField().getKeeperPass() != null;
	}


	private boolean isGoalKick()
	{
		double ballVelocity = getBall().getVel().getLength();
		return getWFrame().getKickEvent().isPresent() && ballVelocity > 1.5 &&
				Math.abs(getBall().getVel().getAngle()) > AngleMath.deg2rad(120);
	}


	private boolean isBallMoving()
	{
		return getBall().getVel().getLength() > 0.3;
	}


	private boolean isKeeperWellInsidePenaltyArea()
	{
		return Geometry.getPenaltyAreaOur().withMargin(ballPlacementPAMarginUpper).isPointInShape(getPos());
	}


	private boolean ballPlacementRequired()
	{
		return isOtherBotCloseToBall()
				|| !(isBallInPenaltyArea(ballPlacementPAMarginLower) || isBallDangerous());
	}


	private boolean ballPlaced()
	{
		return getPlacementPos().distanceTo(getBall().getPos()) < 1;
	}


	private boolean isOtherBotCloseToBall()
	{
		return getWFrame().getBots().values().stream()
				.filter(bot -> !bot.getBotId().equals(getBotID()))
				.anyMatch(bot -> bot.getPos().distanceTo(getBall().getPos()) < 200);
	}


	private boolean isBallInPenaltyArea(final double getBallPenaltyAreaMargin)
	{
		return Geometry.getPenaltyAreaOur().withMargin(getBallPenaltyAreaMargin).isPointInShape(getBall().getPos());
	}


	private boolean isOutsidePenaltyArea()
	{
		return !Geometry.getPenaltyAreaOur().withMargin(1000).isPointInShapeOrBehind(getPos());
	}


	private IVector2 getPlacementPos()
	{
		return Geometry.getPenaltyAreaOur().getRectangle()
				.withMargin(ballPlacementPAMarginUpper)
				.nearestPointInside(getBall().getPos());
	}


	private boolean isBallDangerous()
	{
		return getAiFrame().getTacticalField().getOpponentClosestToBall().getDist() < minOpponentBotDistToChill
				&& !Geometry.getPenaltyAreaOur().isPointInShape(getWFrame().getBall().getPos());
	}


	private boolean canGoOut()
	{
		if (!getAiFrame().getGameState().isPenalty() || isBallInPenaltyArea(0) || isGoalKick())
		{
			return false;
		}

		boolean isBallCloseToGoal = isBallInPenaltyArea(getAiFrame().getTacticalField().getKeeperRamboDistance());
		boolean isKeeperOnLine = false;

		if (getBall().getVel().getLength2() > 0.3)
		{
			ILine line = Lines.lineFromDirection(getBall().getPos(), getBall().getVel());
			isKeeperOnLine = line.distanceTo(getPos()) < Geometry.getBotRadius() / 2.;
		}

		ITrackedBot opponent = getWFrame().getBot(getAiFrame().getTacticalField().getOpponentClosestToBall().getBotId());
		if (opponent == null)
		{
			return false;
		}
		double timeOpponent = TrajectoryGenerator.generatePositionTrajectory(opponent, getBall().getPos()).getTotalTime();
		double timeKeeper = TrajectoryGenerator.generatePositionTrajectory(getBot(), getBall().getPos()).getTotalTime();
		boolean isKeeperFaster = timeOpponent > timeKeeper;
		boolean isBallInFrontOfKeeper = getPos().x() < getWFrame().getBall().getPos().x();
		boolean isBallGoingTowardsGoal = Math.abs(getBall().getVel().getAngle()) > AngleMath.PI_HALF;

		boolean isRamboValid = (isBallCloseToGoal || isKeeperFaster) && isBallInFrontOfKeeper && isBallGoingTowardsGoal;
		return isRamboValid && isKeeperOnLine;
	}


	private class KeeperStoppedState extends RoleState<MoveToSkill>
	{
		private final KeepDistanceToBall keepDistanceToBall = new KeepDistanceToBall(new PointChecker()
				.checkBallDistances()
				.checkInsideField()
				.checkPointFreeOfBots());


		public KeeperStoppedState()
		{
			super(MoveToSkill::new);
		}


		@Override
		protected void onUpdate()
		{
			skill.getMoveCon().physicalObstaclesOnly();
			var dest = keepDistanceToBall
					.findNextFreeDest(getAiFrame(), Geometry.getPenaltyAreaOur().getRectangle().center(), getBotID());
			skill.updateDestination(dest);
			skill.updateLookAtTarget(getWFrame().getBall());
		}
	}

	private class MoveToPenaltyAreaState extends RoleState<MoveToSkill>
	{
		public MoveToPenaltyAreaState()
		{
			super(MoveToSkill::new);
		}


		@Override
		public void onInit()
		{
			skill.getMoveCon().physicalObstaclesOnly();
			// if game is running, get into penArea as fast as possible (in STOP, we are limited to stop speed anyway)
			skill.getMoveConstraints().setFastMove(true);
			skill.updateDestination(Geometry.getPenaltyAreaOur().getRectangle().center());
			skill.updateLookAtTarget(Geometry.getCenter());
		}
	}


	private class PreparePenaltyState extends RoleState<MoveToSkill>
	{
		PreparePenaltyState()
		{
			super(MoveToSkill::new);
		}


		@Override
		protected void onInit()
		{
			skill.getMoveCon().physicalObstaclesOnly();
			skill.updateDestination(Geometry.getGoalOur().getCenter().addNew(Vector2.fromX(Geometry.getBotRadius() - 20)));
		}
	}


	private class InterceptRollingBallState extends RoleState<MoveToSkill>
	{
		private IVector2 interceptPos = Geometry.getGoalOur().getCenter();


		public InterceptRollingBallState()
		{
			super(MoveToSkill::new);
		}


		@Override
		protected void onUpdate()
		{
			skill.updateLookAtTarget(getBall());
			var ballInterceptions = getAiFrame().getPrevFrame().getTacticalField().getRollingBallInterceptions();
			if (!ballInterceptions.isEmpty())
			{
				interceptPos = ballInterceptions.get(getBotID()).getBallInterception().getPos();
				if (getBall().getTrajectory().getTravelLine().distanceTo(interceptPos) > Geometry.getBotRadius())
				{
					skill.updateDestination(interceptPos);
				}
			}
		}


		@Override
		protected void onInit()
		{
			var ballInterceptions = getAiFrame().getPrevFrame().getTacticalField().getRollingBallInterceptions();
			interceptPos = ballInterceptions.get(getBotID()).getBallInterception().getPos();
			skill.getMoveCon().physicalObstaclesOnly();
			skill.getMoveCon().setBallObstacle(false);
			skill.updateDestination(interceptPos);
		}

	}

	private class MoveInFrontOfBallState extends RoleState<MoveToSkill>
	{
		public MoveInFrontOfBallState()
		{
			super(MoveToSkill::new);
		}


		@Override
		protected void onUpdate()
		{
			IVector2 placementPos = getPlacementPos();
			skill.getMoveCon().physicalObstaclesOnly();
			skill.updateDestination(LineMath.stepAlongLine(getBall().getPos(), placementPos, 200));
			skill.updateLookAtTarget(getBall());
		}
	}

	private class MoveWithBallState extends RoleState<MoveWithBallSkill>
	{
		public MoveWithBallState()
		{
			super(MoveWithBallSkill::new);
		}


		@Override
		protected void onInit()
		{
			updateTargetPose(getPlacementPos());
		}


		@Override
		protected void onUpdate()
		{
			IVector2 placementPos = getPlacementPos();
			if (placementPos.distanceTo(getBall().getPos()) > 300)
			{
				updateTargetPose(placementPos);
			}
		}


		private void updateTargetPose(final IVector2 placementPos)
		{
			double dist2Ball = Geometry.getBallRadius() + getBot().getCenter2DribblerDist();
			skill.setFinalDest(LineMath.stepAlongLine(placementPos, getBall().getPos(), -dist2Ball));
		}
	}

	/**
	 * If the ball is near the penalty area, the keeper should chip it to the best pass target.
	 */
	private class PassState extends RoleState<TouchKickSkill>
	{
		private final TimestampTimer timeoutTimer = new TimestampTimer(5);


		PassState()
		{
			super(TouchKickSkill::new);
		}


		@Override
		protected void onInit()
		{
			skill.getMoveCon().physicalObstaclesOnly();
			timeoutTimer.start(getWFrame().getTimestamp());
		}


		@Override
		protected void onUpdate()
		{
			Pass pass = getAiFrame().getTacticalField().getKeeperPass();
			if (!timeoutTimer.isTimeUp(getWFrame().getTimestamp()) && pass != null)
			{
				skill.setTarget(pass.getKick().getTarget());
				skill.setPassRange(pass.getKick().getAimingTolerance());
				skill.setDesiredKickParams(pass.getKick().getKickParams());
				getAiFrame().getShapeMap().get(EAiShapesLayer.AI_KEEPER)
						.add(new DrawableLine(getBall().getPos(), pass.getKick().getTarget()));
			}
		}
	}
}
