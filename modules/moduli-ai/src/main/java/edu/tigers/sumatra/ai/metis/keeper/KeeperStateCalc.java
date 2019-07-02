/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.keeper;

import java.awt.Color;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.math.AiMath;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.TacticalField;
import edu.tigers.sumatra.ai.metis.botdistance.BotDistance;
import edu.tigers.sumatra.ai.metis.offense.OffensiveRedirectorMath;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.Goal;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.line.v2.ILineSegment;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.rectangle.IRectangle;
import edu.tigers.sumatra.math.rectangle.Rectangle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.VectorMath;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.ITrackedObject;



/**
 * @author ChrisC
 */
public class KeeperStateCalc extends ACalculator
{
	@Configurable(comment = "Distance from Foe to Ball where Keeper beliefs Foe could be in possession of the ball", defValue = "450")
	private static double foeBotBallPossessionDistance = 450;
	
	@Configurable(comment = "Additional area around PE where FOE with Ball are very dangerous (GoOutState is triggered)", defValue = "360")
	private static double ballDangerZone = 360;
	
	@Configurable(comment = "Ball Speed where Keeper react on his direction", defValue = "0.1")
	private static double blockDecisionVelocity = 0.1;
	
	@Configurable(comment = "offset to the Sides of the goalposts (BalVelIsDirToGoal State)", defValue = "180.0")
	private static double goalAreaOffset = 180;
	
	@Configurable(comment = "Additional Penalty Area margin where Keeper goes into ChipKickState", defValue = "100")
	private static double chipKickDecisionDistance = 100;
	
	@Configurable(comment = "Speed limit of ball of ChipKickState", defValue = "0.8")
	private static double chipKickDecisionVelocity = 0.8;
	
	@Configurable(comment = "Speed limit of ball of PullBackState", defValue = "0.1")
	private static double pullBackDecisionVelocity = 0.1;
	
	@Configurable(comment = "Speed limit of ball of PullBackState while pulling the ball", defValue = "2.0")
	private static double pullBackDecisionVelocityDuringPull = 2.0;
	
	
	@Configurable(comment = "Ball declared as shooted after kick event", defValue = "500")
	private static long maxKickTime = 500;
	
	@Configurable(comment = "Additional margin to PE where single Attacker is in", defValue = "2500.0")
	private static double singleAttackerPenaltyAreaMargin = 2500;
	
	@Configurable(comment = "Check to use pullback when ball is lying close to goal posts", defValue = "true")
	private static boolean usePullWhenBallAtGoalPost = true;
	
	private boolean isFOENearBall;
	private boolean isFOEUnblocked;
	private long lastTimeKicked = 0;
	
	public static double getPullBackDecisionVelocity()
	{
		return pullBackDecisionVelocity;
	}
	
	
	public static void setPullBackDecisionVelocity(final double pullBackDecisionVelocity)
	{
		KeeperStateCalc.pullBackDecisionVelocity = pullBackDecisionVelocity;
	}
	
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		updateGlobalFields();
		if (isKeeperSet())
		{
			setNextKeeperState();
		}
	}
	
	
	private void updateGlobalFields()
	{
		isFOENearBall = isFOENearBall();
		isFOEUnblocked = isFOEUnblocked();
	}
	
	
	private boolean isKeeperSet()
	{
		return (getAiFrame().getKeeperId() != null)
				&& getAiFrame().getWorldFrame().getBots().containsKey(getAiFrame().getKeeperId());
	}
	
	
	private void setNextKeeperState()
	{
		// The order in if else represents the priority of the states
		EKeeperState nextState;
		getNewTacticalField().setBotInterferingKeeperChip(isBotInterferingChip());
		if (getNewTacticalField().getGameState().isStoppedGame())
		{
			nextState = EKeeperState.STOPPED;
		} else if (isKeeperOutSideOfPenaltyArea())
		{
			nextState = EKeeperState.MOVE_TO_PENALTY_AREA;
		} else if (isSomeoneShootingAtOurGoal() || isFOERedirecting())
		{
			nextState = EKeeperState.CRITICAL;
		} else if (isPullBackPossible())
		{
			nextState = isChipFastFeasible() ? EKeeperState.CHIP_FAST : EKeeperState.PULL_BACK;
		} else if (isGoOutFeasible())
		{
			nextState = EKeeperState.CRITICAL;
		} else
		{
			nextState = EKeeperState.CRITICAL;
		}
		getNewTacticalField().setKeeperState(nextState);
	}
	
	
	private boolean isGoOutFeasible()
	{
		return (isBallCloseToPenaltyArea(ballDangerZone) && isFOENearBall) || isSingleAttacker();
	}
	
	
	private boolean isChipFastFeasible()
	{
		boolean wasInPullBack = getAiFrame().getPrevFrame().getTacticalField().getKeeperState() == EKeeperState.PULL_BACK;
		final double penAreaMarginForSafeChip = -200;
		boolean ballFarEnoughInsidePenArea = Geometry.getPenaltyAreaOur()
				.withMargin(wasInPullBack ? penAreaMarginForSafeChip : chipKickDecisionDistance)
				.isPointInShape(getBall().getPos());
		boolean pullBackFinished = !wasInPullBack || getBall().getPos()
				.distanceTo(getWFrame().getTiger(getAiFrame().getKeeperId()).getPos()) > 2 * Geometry.getBallRadius()
						+ Geometry.getBotRadius();
		return pullBackFinished && ballFarEnoughInsidePenArea && isChipExecutable()
				&& !getNewTacticalField().isBotInterferingKeeperChip();
	}
	
	
	private boolean isBotNotBetweenBallAndKeeper()
	{
		ILineSegment keeperBallLine = Lines.segmentFromPoints(getWFrame().getTiger(getAiFrame().getKeeperId()).getPos(),
				getBall().getPos());
		return getWFrame().getBots().values().stream()
				.filter(bot -> !bot.getBotId().equals(getAiFrame().getKeeperId()))
				.map(ITrackedObject::getPos)
				.noneMatch(pos -> keeperBallLine.closestPointOnLine(pos).distanceTo(pos) < Geometry.getBotRadius());
	}
	
	
	private boolean isSingleAttacker()
	{
		List<ITrackedBot> nearFoes = getWFrame().getFoeBots().values().stream()
				.filter(bot -> Geometry.getPenaltyAreaOur().isPointInShape(bot.getPos(), singleAttackerPenaltyAreaMargin))
				.collect(Collectors.toList());
		boolean isSingleAttacker = nearFoes.size() < 2;
		boolean hasFOEBall = false;
		if (!nearFoes.isEmpty())
		{
			hasFOEBall = nearFoes.stream().anyMatch(bot -> bot.getPos().distanceTo(getBall().getPos()) < 1000);
		}
		
		return isSingleAttacker && hasFOEBall;
	}
	
	
	private boolean isPullBackPossible()
	
	{
		boolean isFoeDangerous = isFOENearBall && isFOEUnblocked
				&& !Geometry.getPenaltyAreaOur().isPointInShape(getBall().getPos());
		
		double speedLimit = getAiFrame().getPrevFrame().getTacticalField().getKeeperState() == EKeeperState.PULL_BACK
				? pullBackDecisionVelocityDuringPull
				: pullBackDecisionVelocity;
		
		return isBallCloseToPenaltyArea(chipKickDecisionDistance) && !isFoeDangerous
				&& isBallStill(speedLimit) && isBotNotBetweenBallAndKeeper();
	}
	
	
	private boolean isFOENearBall()
	{
		BotDistance foeBot = getNewTacticalField().getEnemyClosestToBall();
		return (foeBot != BotDistance.NULL_BOT_DISTANCE)
				&& (VectorMath.distancePP(foeBot.getBot().getPos(), getBall().getPos()) < foeBotBallPossessionDistance);
	}
	
	
	private boolean isFOEUnblocked()
	{
		if (getNewTacticalField().getEnemyClosestToBall() == BotDistance.NULL_BOT_DISTANCE)
		{
			return false;
		}
		
		IVector2 closestEnemy = getNewTacticalField().getEnemyClosestToBall().getBot().getBotKickerPos();
		boolean unblocked = false;
		
		ILineSegment goalLine = Lines.segmentFromPoints(
				Geometry.getGoalOur().getLeftPost(),
				Geometry.getGoalOur().getRightPost());
		for (IVector2 goalLinePos : goalLine.getSteps(Geometry.getBallRadius() * 2))
		{
			ILineSegment testLine = Lines.segmentFromPoints(goalLinePos, closestEnemy);
			boolean testLineBlocked = false;
			for (ITrackedBot tigerBot : getWFrame().getTigerBotsAvailable().values())
			{
				ICircle botApproximation = Circle.createCircle(tigerBot.getPos(), Geometry.getBotRadius());
				if (tigerBot.getBotId() != getAiFrame().getKeeperId() && botApproximation.isIntersectingWithLine(testLine))
				{
					testLineBlocked = true;
					break;
				}
			}
			
			getNewTacticalField().getDrawableShapes().get(EAiShapesLayer.AI_KEEPER)
					.add(new DrawableLine(testLine, testLineBlocked ? Color.GREEN : Color.RED));
			if (!testLineBlocked)
			{
				unblocked = true;
			}
			
		}
		
		
		return unblocked;
	}
	
	
	private boolean isKeeperOutSideOfPenaltyArea()
	{
		IVector2 keeperPos = getAiFrame().getWorldFrame().getBot(getAiFrame().getKeeperId()).getPos();
		return !(Geometry.getPenaltyAreaOur().withMargin(chipKickDecisionDistance).isPointInShapeOrBehind(keeperPos)
				|| isKeeperPreparingChip());
	}
	
	
	private boolean isKeeperPreparingChip()
	{
		return getAiFrame().getPrevFrame().getTacticalField().getKeeperState() == EKeeperState.CHIP_FAST
				&& Geometry.getPenaltyAreaOur().isPointInShape(getBall().getPos());
	}
	
	
	private boolean isBallCloseToPenaltyArea(double margin)
	{
		return Geometry.getPenaltyAreaOur().isPointInShape(getBall().getPos(),
				margin);
	}
	
	
	private boolean isBallStill(double velocityTolerance)
	{
		
		return getBall().getVel().getLength() < velocityTolerance;
	}
	
	
	private boolean isChipExecutable()
	{
		boolean isBallFarFromGoalPost = true;
		if (usePullWhenBallAtGoalPost)
		{
			isBallFarFromGoalPost = !isBallCloseToGoalPosts(3 * Geometry.getBotRadius());
		}
		return isBallFarFromGoalPost || isKeeperBehindBall();
	}
	
	
	private boolean isBallCloseToGoalPosts(double distance)
	{
		return Geometry.getGoalOur().getRightPost().distanceTo(getBall().getPos()) < distance
				|| Geometry.getGoalOur().getLeftPost().distanceTo(getBall().getPos()) < distance;
	}
	
	
	private boolean isKeeperBehindBall()
	{
		Goal goal = Geometry.getGoalOur();
		IVector2 ballPos = getBall().getPos();
		ITrackedBot keeper = getWFrame().getBot(getAiFrame().getKeeperId());
		IRectangle zoneBehindBall = Rectangle.aroundLine(ballPos.subtractNew(Vector2.fromX(Geometry.getBotRadius())),
				ballPos.addNew(Vector2.fromX(-(Geometry.getFieldLength() / 2
						+ ballPos.x() + goal.getDepth() - Geometry.getBotRadius()))),
				Geometry.getBotRadius());
		
		boolean bothBetweenGoalPosts = Math.abs(getBall().getPos().y()) < goal.getWidth() / 2
				&& Math.abs(keeper.getPos().y()) < goal.getWidth() / 2;
		boolean bothBesideRightPost = ballPos.y() < goal.getRightPost().y()
				&& keeper.getPos().y() < goal.getRightPost().y();
		boolean bothBesideLeftPost = ballPos.y() > goal.getLeftPost().y() && keeper.getPos().y() > goal.getLeftPost().y();
		
		return zoneBehindBall.isPointInShape(keeper.getPos())
				&& (bothBesideRightPost || bothBesideLeftPost || bothBetweenGoalPosts);
	}
	
	
	private boolean isBotInterferingChip()
	{
		double minDistance = getBall().getChipConsultant()
				.getMinimumDistanceToOverChip(getBall().getChipConsultant().getInitVelForDistAtTouchdown(1000, 0), 160);
		List<BotDistance> tigersBotDistance = getNewTacticalField().getTigersToBallDist().stream()
				.filter(botDistance -> !botDistance.getBot().getBotId().equals(getAiFrame().getKeeperId()))
				.filter(botDistance -> botDistance.getDist() - Geometry.getBotRadius() < minDistance)
				.collect(Collectors.toList());
		List<BotDistance> foeBotDistance = getNewTacticalField().getEnemiesToBallDist().stream()
				.filter(botDistance -> (botDistance.getDist() - Geometry.getBotRadius()) < minDistance)
				.collect(Collectors.toList());
		ILine keeperBallLine = Line.fromPoints(getWFrame().getBot(getAiFrame().getKeeperId()).getPos(),
				getWFrame().getBall().getPos());
		boolean isBotInterferingChip = tigersBotDistance.stream().anyMatch(
				botDistance -> keeperBallLine.distanceTo(botDistance.getBot().getPos()) < Geometry.getBotRadius());
		return isBotInterferingChip || !foeBotDistance.isEmpty();
	}
	
	
	private boolean isFOERedirecting()
	{
		BotID redirectFOEBotId = OffensiveRedirectorMath.getBestRedirector(getAiFrame().getWorldFrame(),
				getAiFrame().getWorldFrame().getFoeBots());
		IVector2 redirectFOEBot = null;
		if (redirectFOEBotId.isBot())
		{
			redirectFOEBot = getAiFrame().getWorldFrame().getFoeBot(redirectFOEBotId).getPos();
		}
		
		return (redirectFOEBot != null)
				&& (AiMath.p2pVisibility(getAiFrame().getWorldFrame().getBots().values(), redirectFOEBot,
						Geometry.getGoalOur().getCenter(), getAiFrame().getKeeperId()))
				&& (getBall().getVel().getLength() > chipKickDecisionVelocity);
	}
	
	
	private boolean isSomeoneShootingAtOurGoal()
	{
		Optional<IVector2> intersect = getBall().getTrajectory().getTravelLine().intersectSegment(Geometry.getGoalOur()
				.withMargin(0, Geometry.getGoalOur().getWidth() / 2.0).getLineSegment());
		
		if (intersect.isPresent())
		{
			boolean isBallFastEnough = getBall().getTrajectory()
					.getAbsVelByDist(intersect.get().distanceTo(getBall().getPos())) > blockDecisionVelocity;
			boolean isBallOnOurSite = getBall().getVel().x() < 0;
			boolean isBallVelocityIntersectingTheGoalLine = Math.abs(intersect.get().y()) < Math
					.abs(Geometry.getGoalOur().getLeftPost().y() + goalAreaOffset);
			
			getWFrame().getKickEvent().ifPresent(kickEvent -> lastTimeKicked = kickEvent.getTimestamp());
			
			boolean isBallKicked = (lastTimeKicked - getWFrame().getTimestamp()) < maxKickTime;
			boolean isBallLeavingFOE = !isFOENearBall || isBallKicked;
			boolean wasKeeperInPullBackState = getAiFrame().getPrevFrame().getTacticalField()
					.getKeeperState() == EKeeperState.PULL_BACK;
			boolean isBallDangerous = isBallOnOurSite
					&& isBallVelocityIntersectingTheGoalLine
					&& isBallFastEnough
					&& isBallLeavingFOE;
			return isBallDangerous && !wasKeeperInPullBackState;
		}
		return false;
	}
}
