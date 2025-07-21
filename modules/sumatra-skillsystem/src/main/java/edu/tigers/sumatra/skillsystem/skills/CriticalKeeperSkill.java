/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.VectorDistanceComparator;
import edu.tigers.sumatra.math.vector.VectorMath;
import edu.tigers.sumatra.pathfinder.EObstacleAvoidanceMode;
import edu.tigers.sumatra.pathfinder.TrajectoryGenerator;
import edu.tigers.sumatra.skillsystem.ESkillShapesLayer;
import edu.tigers.sumatra.skillsystem.skills.DefendingKeeper.ECriticalKeeperStates;
import edu.tigers.sumatra.skillsystem.skills.keeper.GoOutDestinationCalculator;
import edu.tigers.sumatra.skillsystem.skills.keeper.IKeeperDestinationCalculator;
import edu.tigers.sumatra.skillsystem.skills.keeper.InterceptBallCurveDestinationCalculator;
import edu.tigers.sumatra.skillsystem.skills.keeper.InterceptBallLineDestinationCalculator;
import edu.tigers.sumatra.skillsystem.skills.keeper.KeeperDestination;
import edu.tigers.sumatra.skillsystem.skills.keeper.NormalBlockDestinationCalculator;
import edu.tigers.sumatra.skillsystem.skills.util.EDribblerMode;
import edu.tigers.sumatra.skillsystem.skills.util.KickParams;
import edu.tigers.sumatra.statemachine.AState;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import lombok.NonNull;
import lombok.Setter;

import java.awt.Color;


/**
 * Skill Implementation for Time critical keeper skills. Used by CriticalKeeperState
 */
public class CriticalKeeperSkill extends AMoveToSkill
{
	@Configurable(comment = "Use ball curve for intercepts", defValue = "false")
	private static boolean useBallCurveForIntercept = false;
	@Configurable(comment = "Stay in normal blocking in the intercept if spare time is great enough", defValue = "true")
	private static boolean useSpareTimeInIntercept = true;

	@Configurable(comment = "[s] Necessary spare time to delay the movement of the keeper", defValue = "0.3")
	private static double necessarySpareTimeToDelayMovement = 0.3;

	@Configurable(comment = "[s] Time we project the ball position into the future", defValue = "0.3")
	private static double ballLookaheadTime = 0.3;

	@Configurable(comment = "Multiplied with the bots brake distance to determine when to start cornering during catchRedirect", defValue = "6")
	private static double catchRedirectCornerDistanceFactor = 6;

	private ECriticalKeeperStates currentState = ECriticalKeeperStates.NORMAL;
	private DefendingKeeper defendingKeeper = new DefendingKeeper(this);

	@Setter
	@NonNull
	private KickParams ballHandlingKickParams = KickParams.disarm();


	public CriticalKeeperSkill()
	{
		var blockState = new NormalBlock();
		setInitialState(blockState);

		addTransition(ECriticalKeeperStates.HAS_CONTACT, new ComeToAStop());
		addTransition(ECriticalKeeperStates.INTERCEPT_BALL, new InterceptBall());
		addTransition(ECriticalKeeperStates.DEFEND_REDIRECT, new CatchRedirect());
		addTransition(ECriticalKeeperStates.GO_OUT, new GoOut());
		addTransition(ECriticalKeeperStates.NORMAL, blockState);
	}


	@Override
	protected void beforeStateUpdate()
	{
		super.beforeStateUpdate();
		defendingKeeper.update(getWorldFrame(), getBot().getBotId());

		ECriticalKeeperStates nextState = defendingKeeper.calcNextKeeperState();
		if (nextState != currentState)
		{
			currentState = nextState;
			triggerEvent(nextState);
		}
		if (getPos().distanceTo(getBall().getPos()) < Geometry.getBallRadius() + Geometry.getBotRadius() + 500)
		{
			setKickParams(ballHandlingKickParams);
		} else
		{
			setKickParams(KickParams.disarm().withDribblerMode(EDribblerMode.OFF));
		}
	}


	private void prepareMoveCon()
	{
		getMoveCon().setPenaltyAreaOurObstacle(false);
		getMoveCon().setBotsObstacle(false);
		getMoveCon().setBallObstacle(false);
		getMoveCon().setGoalPostsObstacle(false);
		getMoveCon().setObstacleAvoidanceMode(EObstacleAvoidanceMode.AGGRESSIVE);
	}


	private IVector2 getBallPosWithLookahead()
	{
		return getWorldFrame().getBall().getTrajectory().getPosByTime(ballLookaheadTime).getXYVector();
	}


	private boolean isSpareTimeLeft(IVector2 interestingPosOnBallTrajectory, IVector2 defendPosition, double extraTime)
	{
		var timeLeft = getBall().getTrajectory().getTimeByPos(interestingPosOnBallTrajectory);
		var drivingTime =
				TrajectoryGenerator.generatePositionTrajectory(getTBot(), defendPosition).getTotalTime() + extraTime;
		var spareTime = timeLeft - drivingTime;

		var shapes = getShapes().get(ESkillShapesLayer.KEEPER);
		shapes.add(new DrawableCircle(defendPosition, Geometry.getBotRadius(), Color.RED));
		shapes.add(new DrawableAnnotation(defendPosition, String.format("%.2f", spareTime), Color.RED));
		return spareTime > necessarySpareTimeToDelayMovement;
	}


	private class InterceptBall extends AState
	{
		private double targetAngle;
		private boolean spareTimeOver = false;

		private IKeeperDestinationCalculator destCalculator;
		private IKeeperDestinationCalculator normalBlockCalculator;


		@Override
		public void doEntryActions()
		{
			targetAngle = getAngle();
			spareTimeOver = false;
			prepareMoveCon();
			if (useBallCurveForIntercept)
			{
				destCalculator = new InterceptBallCurveDestinationCalculator();
			} else
			{
				destCalculator = new InterceptBallLineDestinationCalculator();
			}
			normalBlockCalculator = new NormalBlockDestinationCalculator();
			super.doEntryActions();
		}


		@Override
		public void doUpdate()
		{
			if (getBall().getVel().isZeroVector())
			{
				// skill is not designed for lying balls
				return;
			}
			var destination = destCalculator.calcDestination(getWorldFrame(), getShapes(), getTBot(), null);
			if (isSpareTimeLeft(destination.pos(), destination.pos(), 0.0) && !spareTimeOver && useSpareTimeInIntercept)
			{
				var normalBlockDestination = normalBlockCalculator.calcDestination(getWorldFrame(), getShapes(), getTBot(),
						getBallPosWithLookahead());
				updateDestination(normalBlockDestination.pos());
				setComeToAStop(normalBlockDestination.isComeToAStopFaster(getMoveConstraints()));
			} else
			{
				updateDestination(destination.pos());
				setComeToAStop(destination.isComeToAStopFaster(getMoveConstraints()));
				spareTimeOver = true;
			}
			updateTargetAngle(calcTargetAngle());

			super.doUpdate();
		}


		public double calcTargetAngle()
		{
			if (VectorMath.distancePP(getBall().getPos(), getTBot().getPos()) < (Geometry.getBotRadius() / 2))
			{
				return getWorldFrame().getBall().getPos().subtractNew(getPos()).getAngle();
			}
			return targetAngle;
		}
	}

	private class CatchRedirect extends AState
	{
		private boolean stayInGoOut = false;
		private boolean spareTimeOver = false;
		private IKeeperDestinationCalculator goOutCalculator;
		private IKeeperDestinationCalculator normalBlockCalculator;


		@Override
		public void doEntryActions()
		{
			prepareMoveCon();
			getMoveCon().setFieldBorderObstacle(false);
			stayInGoOut = false;
			spareTimeOver = false;

			goOutCalculator = new GoOutDestinationCalculator();
			normalBlockCalculator = new NormalBlockDestinationCalculator();

			super.doEntryActions();
		}


		@Override
		public void doUpdate()
		{
			var redirectPos = getRedirectBotPosition();
			getShapes().get(ESkillShapesLayer.KEEPER)
					.add(new DrawableCircle(Circle.createCircle(redirectPos, 50), Color.MAGENTA));
			var normalBlockingPos = normalBlockCalculator.calcDestination(getWorldFrame(), getShapes(), getTBot(),
					redirectPos);
			var goOutFeasible = isRedirectGoOutFeasible(redirectPos);

			var destination = normalBlockingPos;
			double extraDriveTime = 0.0;

			getShapes().get(ESkillShapesLayer.KEEPER).add(new DrawableLine(redirectPos, destination.pos(), Color.CYAN));

			if ((isKeeperBetweenRedirectAndNormalBlock(redirectPos, destination.pos()) || stayInGoOut) && goOutFeasible)
			{
				stayInGoOut = true;
				destination = goOutCalculator.calcDestination(getWorldFrame(), getShapes(), getTBot(), redirectPos);
			} else
			{
				stayInGoOut = false;
				if (goOutFeasible)
				{
					var goOutPos = goOutCalculator.calcDestination(getWorldFrame(), getShapes(), getTBot(), redirectPos);
					extraDriveTime =
							goOutPos.pos().distanceTo(normalBlockingPos.pos()) / (getMoveConstraints().getVelMax() * 500);
				}
			}
			if (isSpareTimeLeft(redirectPos, destination.pos(), extraDriveTime) && !spareTimeOver)
			{
				destination = normalBlockCalculator.calcDestination(getWorldFrame(), getShapes(), getTBot(),
						getBallPosWithLookahead());
			} else
			{
				spareTimeOver = true;
			}
			updateLookAtTarget(redirectPos);
			updateDestination(destination.pos());
			setComeToAStop(destination.isComeToAStopFaster(getMoveConstraints()));
			super.doUpdate();
		}


		private boolean isRedirectGoOutFeasible(IVector2 redirectPos)
		{
			return defendingKeeper.isPositionCloseToPenaltyArea(redirectPos) || defendingKeeper.isGoOutFeasible();
		}


		private IVector2 getRedirectBotPosition()
		{
			var redirectBotId = defendingKeeper.getBestRedirector(getWorldFrame().getOpponentBots());
			if (redirectBotId.isPresent())
			{
				return defendingKeeper.getRedirectPosition(getWorldFrame().getOpponentBot(redirectBotId.get()).getPos());
			} else
			{
				var ballTravelLine = Lines.halfLineFromDirection(getBall().getPos(), getBall().getVel());
				return getWorldFrame().getOpponentBots().values().stream()
						.map(ITrackedBot::getBotKickerPos)
						.min(new VectorDistanceComparator(getWorldFrame().getBall().getPos()))
						.map(ballTravelLine::closestPointOnPath)
						.orElse(Vector2.fromXY(0, 0));
			}
		}


		private boolean isKeeperBetweenRedirectAndNormalBlock(IVector2 redirectPos, IVector2 normalBlockPosition)
		{
			var vel = getTBot().getVel().getLength();
			var acc = getMoveConstraints().getAccMax();
			var breakDistance = 1_000 * 0.5 * vel * (vel / acc);
			return Lines.segmentFromPoints(redirectPos, normalBlockPosition).distanceTo(getPos())
					< catchRedirectCornerDistanceFactor * breakDistance;
		}
	}

	private class GoOut extends AState
	{
		private IKeeperDestinationCalculator goOutCalculator;
		private IKeeperDestinationCalculator normalBlockCalculator;


		@Override
		public void doEntryActions()
		{
			prepareMoveCon();
			goOutCalculator = new GoOutDestinationCalculator();
			normalBlockCalculator = new NormalBlockDestinationCalculator();
			super.doEntryActions();
		}


		@Override
		public void doUpdate()
		{
			var ballPos = getBallPosWithLookahead();
			var normalBlockingPos = normalBlockCalculator.calcDestination(getWorldFrame(), getShapes(), getTBot(),
					ballPos);
			KeeperDestination targetPosition;
			if (isBallInsidePenaltyArea())
			{
				targetPosition = normalBlockingPos;
			} else
			{
				targetPosition = goOutCalculator.calcDestination(getWorldFrame(), getShapes(), getTBot(), ballPos);
			}
			getShapes().get(ESkillShapesLayer.KEEPER)
					.add(new DrawableLine(targetPosition.pos(), normalBlockingPos.pos(), Color.CYAN));
			updateDestination(targetPosition.pos());
			setComeToAStop(targetPosition.isComeToAStopFaster(getMoveConstraints()));
			updateTargetAngle(getPos().subtractNew(Geometry.getGoalOur().getCenter()).getAngle());

			super.doUpdate();
		}


		private boolean isBallInsidePenaltyArea()
		{
			return Geometry.getPenaltyAreaOur()
					.withMargin(-2 * Geometry.getBotRadius())
					.isPointInShape(getWorldFrame().getBall().getPos());
		}
	}

	private class ComeToAStop extends AState
	{
		IVector2 destination;


		@Override
		public void doEntryActions()
		{
			prepareMoveCon();
			var vel = getVel();
			var velLength = vel.getLength2();
			var breakDistance = 1000 * (0.5f * velLength * velLength / getMoveConstraints().getAccMax());
			destination = Geometry.getPenaltyAreaOur().getRectangle().withMargin(-Geometry.getBotRadius())
					.nearestPointInside(getPos().addNew(vel.scaleToNew(breakDistance)));
			updateDestination(destination);
			super.doEntryActions();
		}


		@Override
		public void doUpdate()
		{
			getShapes().get(ESkillShapesLayer.KEEPER)
					.add(new DrawableCircle(Circle.createCircle(destination, 100), Color.MAGENTA));
			updateDestination(destination);
			super.doUpdate();
		}
	}

	private class NormalBlock extends AState
	{
		IKeeperDestinationCalculator destCalculator;


		@Override
		public void doEntryActions()
		{
			prepareMoveCon();
			destCalculator = new NormalBlockDestinationCalculator();
			super.doEntryActions();
		}


		@Override
		public void doUpdate()
		{
			var destination = destCalculator.calcDestination(getWorldFrame(), getShapes(), getTBot(),
					getBallPosWithLookahead());
			updateDestination(destination.pos());
			setComeToAStop(destination.isComeToAStopFaster(getMoveConstraints()));
			updateLookAtTarget(getBall());

			super.doUpdate();
		}


	}


}
