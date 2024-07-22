/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.ballpossession;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.botdistance.BotDistance;
import edu.tigers.sumatra.ai.metis.kicking.OngoingPass;
import edu.tigers.sumatra.drawable.DrawableBorderText;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.EFontSize;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.referee.data.GameState;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.awt.Color;
import java.util.Optional;
import java.util.function.Supplier;


/**
 * This calculator determines whether {@link EBallPossession#WE}, {@link EBallPossession#THEY},
 * {@link EBallPossession#BOTH} or {@link EBallPossession#NO_ONE} has the ball.
 * Based on CMDragons TDP 2016.
 */
@RequiredArgsConstructor
public class BallPossessionCalc extends ACalculator
{
	private static final double NEAR_BARRIER_TOLERANCE = 4;

	@Configurable(comment = "Distance below which t_near starts to tick. (Bot to ball, air gap) [mm]", defValue = "40")
	private static double distNear = 40;

	@Configurable(comment = "Distance above which t_far starts to tick. (Bot to ball, air gap) [mm]", defValue = "80")
	private static double distFar = 80;

	@Configurable(comment = "Time until a near ball is considered to belong to a robot. [s]", defValue = "0.1")
	private static double timeThresholdNear = 0.1;

	@Configurable(comment = "Time until a far ball is considered to be lost by a robot. [s]", defValue = "1.5")
	private static double timeThresholdFar = 1.5;

	@Configurable(comment = "Balls above max. height cannot change possession. [mm]", defValue = "120")
	private static double maxHeight = 120;

	private long tLast;
	private double timeWeNear = 0;
	private double timeTheyNear = 0;
	private double timeWeFar = 0;
	private double timeTheyFar = 0;


	private final Supplier<BotDistance> tigerClosestToBall;
	private final Supplier<BotDistance> opponentClosestToBall;
	private final Supplier<ITrackedBot> opponentPassReceiver;
	private final Supplier<Optional<OngoingPass>> ongoingPass;

	@Getter
	private BallPossession ballPossession;


	@Override
	public void doCalc()
	{
		// Which robots are near the ball? (Only closest of each team is considered)
		var closestTiger = tigerClosestToBall.get();
		var closestOpponent = opponentClosestToBall.get();

		// At this point one could check if both BotDistances are equal to BotDistance.NULL_BOT_DISTANCE.
		// As NULL_BOT_DISTANCE.dist == Double.MAX_VALUE, the current decision is that no one has the ball!

		// update tNear and tFar for both teams
		double dt = (getWFrame().getTimestamp() - tLast) * 1e-9;
		updateTimes(closestTiger.getDist(), closestOpponent.getDist(), dt, getAiFrame().getGameState(),
				opponentPassReceiver.get(), ballPossession);

		// set ball possession based on near/far time thresholds
		if (getBall().getPos3().z() < maxHeight)
		{
			var ePossession = determinePossession();
			ballPossession = new BallPossession(ePossession,
					getOpponentBallControl(closestOpponent.getBotId(), ePossession), closestTiger.getBotId(),
					closestOpponent.getBotId());
		}

		draw();
		tLast = getWFrame().getTimestamp();
	}


	@Override
	protected void reset()
	{
		ballPossession = new BallPossession(EBallPossession.NO_ONE, EBallControl.NONE, BotID.noBot(), BotID.noBot());
		tLast = getWFrame().getTimestamp();
	}


	private EBallControl getOpponentBallControl(BotID id, EBallPossession ePossession)
	{
		var bot = getWFrame().getOpponentBot(id);
		if (bot == null)
		{
			return EBallControl.NONE;
		}
		boolean similarBallRobotVel = getBall().getVel().subtractNew(bot.getVel()).getLength() < 1.8;
		if (bot.getBotShape().isPointInKickerZone(getBall().getPos(), Geometry.getBallRadius() + NEAR_BARRIER_TOLERANCE)
				&& similarBallRobotVel)
		{
			return EBallControl.STRONG;
		} else if (ePossession == EBallPossession.THEY)
		{
			return EBallControl.WEAK;
		}
		return EBallControl.NONE;
	}


	private void draw()
	{
		ITrackedBot closestTigerBot = getAiFrame().getWorldFrame().getBot(ballPossession.getTigersId());
		if (closestTigerBot != null)
		{
			getShapes(EAiShapesLayer.AI_BALL_POSSESSION).add(
					new DrawableCircle(Circle.createCircle(closestTigerBot.getPos(), Geometry.getBotRadius() + 20),
							Color.BLACK));
		}

		ITrackedBot closestOpponentBot = getAiFrame().getWorldFrame().getBot(ballPossession.getOpponentsId());
		if (closestOpponentBot != null)
		{
			getShapes(EAiShapesLayer.AI_BALL_POSSESSION).add(
					new DrawableCircle(Circle.createCircle(closestOpponentBot.getPos(), Geometry.getBotRadius() + 20),
							Color.BLACK));
		}

		var hudColor = getWFrame().getTeamColor();
		double posX = 1.0;
		getShapes(EAiShapesLayer.AI_BALL_POSSESSION).add(
				new DrawableBorderText(Vector2.fromXY(posX, hudColor == ETeamColor.BLUE ? 10.3 : 12.0),
						ballPossession.getEBallPossession().toString() + " control" + ballPossession.getOpponentBallControl())
						.setFontSize(EFontSize.MEDIUM)
						.setColor(hudColor.getColor())
		);
	}


	private EBallPossession determinePossession()
	{
		if ((timeWeNear > timeThresholdNear) && (timeTheyNear < timeThresholdNear))
		{
			return EBallPossession.WE;
		} else if ((timeWeNear < timeThresholdNear) && (timeTheyNear > timeThresholdNear))
		{
			return EBallPossession.THEY;
		} else if ((timeWeNear > timeThresholdNear) && (timeTheyNear > timeThresholdNear))
		{
			return EBallPossession.BOTH;
		} else if ((timeWeFar > timeThresholdFar) && (timeTheyFar > timeThresholdFar)
				&& ongoingPass.get().isEmpty())
		{
			return EBallPossession.NO_ONE;
		}
		return ballPossession.getEBallPossession();
	}


	private void updateTimes(final double closeTiger, final double closeOpponent, final double dt,
			final GameState gameState, final ITrackedBot passReceiver, final BallPossession lastPossession)
	{
		final double toleranceNear = Geometry.getBallRadius() + distNear
				+ Geometry.getBotRadius();
		final double toleranceFar = Geometry.getBallRadius() + distFar
				+ Geometry.getBotRadius();

		double bonusTigers = 0;
		if (gameState.isStandardSituationIncludingKickoffForUs())
		{
			bonusTigers = RuleConstraints.getStopRadius();
		}

		double bonusOpponent = 0;
		if (gameState.isStandardSituationIncludingKickoffForThem())
		{
			bonusOpponent = RuleConstraints.getStopRadius();
		} else if ((lastPossession.getEBallPossession() == EBallPossession.THEY) && passReceiver != null)
		{
			bonusOpponent = getBall().getPos().distanceTo(passReceiver.getPos());
		}

		if (closeTiger < (toleranceNear + bonusTigers))
		{
			timeWeNear += dt;
		} else
		{
			timeWeNear = 0;
		}

		if (closeOpponent < (toleranceNear + bonusOpponent))
		{
			timeTheyNear += dt;
		} else
		{
			timeTheyNear = 0;
		}

		if (closeTiger > (toleranceFar + bonusTigers))
		{
			timeWeFar += dt;
		} else
		{
			timeWeFar = 0;
		}

		if (closeOpponent > (toleranceFar + bonusOpponent))
		{
			timeTheyFar += dt;
		} else
		{
			timeTheyFar = 0;
		}
	}
}
