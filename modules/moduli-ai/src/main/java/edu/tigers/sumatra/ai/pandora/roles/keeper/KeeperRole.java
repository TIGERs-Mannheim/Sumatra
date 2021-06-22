/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.keeper;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.common.KeepDistanceToBall;
import edu.tigers.sumatra.ai.common.PointChecker;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.kicking.Pass;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.line.v2.ILine;
import edu.tigers.sumatra.math.line.v2.LineMath;
import edu.tigers.sumatra.math.line.v2.Lines;
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


/**
 * The Keeper protect the Goal and tries to catch every shoot into the goal.
 */
public class KeeperRole extends ARole
{

	@Configurable(comment = "Lower margin [mm] applied to penalty area. If the ball is inside, no placement necessary", defValue = "-200")
	private static double ballPlacementPAMarginLower = -300;

	@Configurable(comment = "Upper negative margin [mm] applied to penalty area. The ball is placed to a position inside", defValue = "-300")
	private static double ballPlacementPAMarginUpper = -400;

	@Configurable(comment = "Opponent ball dist to chill", defValue = "1000.0")
	private static double minOpponentBotDistToChill = 1000;


	private final KeeperStoppedState stopState = new KeeperStoppedState();
	private final MoveToPenaltyAreaState moveToPenaltyAreaState = new MoveToPenaltyAreaState();
	private final PreparePenaltyState preparePenaltyState = new PreparePenaltyState();


	public KeeperRole()
	{
		super(ERole.KEEPER);

		var defendState = new RoleState<>(CriticalKeeperSkill::new);
		var moveInFrontOfBallState = new MoveInFrontOfBallState();
		var getBallContactState = new RoleState<>(GetBallContactSkill::new);
		var moveWithBallState = new MoveWithBallState();
		var passState = new PassState();
		var ramboState = new RoleState<>(RamboKeeperSkill::new);

		setInitialState(defendState);

		stopState.addTransition(() -> !getAiFrame().getGameState().isStoppedGame(), defendState);
		moveToPenaltyAreaState.addTransition(ESkillState.SUCCESS, defendState);
		preparePenaltyState.addTransition(() -> !getAiFrame().getGameState().isPreparePenaltyForThem(), defendState);

		defendState.addTransition(this::ballCanBePassedOutOfPenaltyArea, passState);
		defendState.addTransition(this::canGoOut, ramboState);
		passState.addTransition(this::isBallMoving, defendState);
		passState.addTransition(this::ballPlacementRequired, moveInFrontOfBallState);

		moveInFrontOfBallState.addTransition(this::isBallMoving, defendState);
		moveInFrontOfBallState.addTransition(ESkillState.SUCCESS, getBallContactState);
		getBallContactState.addTransition(ESkillState.SUCCESS, moveWithBallState);
		getBallContactState.addTransition(ESkillState.FAILURE, moveInFrontOfBallState);
		moveWithBallState.addTransition(ESkillState.SUCCESS, defendState);
		moveWithBallState.addTransition(ESkillState.FAILURE, moveInFrontOfBallState);
	}


	@Override
	protected void beforeUpdate()
	{
		super.beforeUpdate();

		stateTransition();
	}


	private void stateTransition()
	{
		if (getAiFrame().getGameState().isStoppedGame())
		{
			changeState(stopState);
		} else if (isOutsidePenaltyArea())
		{
			changeState(moveToPenaltyAreaState);
		} else if (getAiFrame().getGameState().isPreparePenaltyForThem())
		{
			changeState(preparePenaltyState);
		}
	}


	private boolean ballCanBePassedOutOfPenaltyArea()
	{
		return getAiFrame().getTacticalField().getKeeperPass() != null;
	}


	private boolean isBallMoving()
	{
		return getBall().getVel().getLength() > 0.3;
	}


	private boolean ballPlacementRequired()
	{
		return isOtherBotCloseToBall()
				|| !(isBallInPenaltyArea(ballPlacementPAMarginLower) || isBallDangerous());
	}


	private boolean isOtherBotCloseToBall()
	{
		return getWFrame().getBots().values().stream()
				.filter(bot -> bot.getBotId() != getBotID())
				.anyMatch(bot -> bot.getPos().distanceTo(getBall().getPos()) < 200);
	}


	private boolean isBallInPenaltyArea(final double getBallPenaltyAreaMargin)
	{
		return Geometry.getPenaltyAreaOur().getRectangle().isPointInShape(getBall().getPos(), getBallPenaltyAreaMargin);
	}


	private boolean isOutsidePenaltyArea()
	{
		return !Geometry.getPenaltyAreaOur().withMargin(1000).isPointInShapeOrBehind(getPos());
	}


	private IVector2 getPlacementPos()
	{
		return Geometry.getPenaltyAreaOur().getRectangle()
				.nearestPointInside(getBall().getPos(), ballPlacementPAMarginUpper);
	}


	private boolean isBallDangerous()
	{
		return getAiFrame().getTacticalField().getOpponentClosestToBall().getDist() < minOpponentBotDistToChill
				&& !Geometry.getPenaltyAreaOur().isPointInShape(getWFrame().getBall().getPos());
	}


	private boolean canGoOut()
	{
		if (!getAiFrame().getGameState().isPenalty())
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

		boolean isRamboValid = (isBallCloseToGoal || isKeeperFaster) && isBallInFrontOfKeeper;
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
			skill.updateDestination(Geometry.getGoalOur().getCenter().addNew(Vector2.fromX(Geometry.getBotRadius())));
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
						.add(new DrawableLine(Line.fromPoints(getBall().getPos(), pass.getKick().getTarget())));
			}
		}
	}
}
