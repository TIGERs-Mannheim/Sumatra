/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.keeper;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.ballinterception.BallInterceptor;
import edu.tigers.sumatra.ai.metis.ballinterception.InterceptionFinderParameters;
import edu.tigers.sumatra.ai.metis.ballinterception.RatedBallInterception;
import edu.tigers.sumatra.ball.trajectory.IBallTrajectory;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.vector.IVector2;
import lombok.Getter;

import java.awt.Color;
import java.util.List;
import java.util.Optional;
import java.util.Set;


public class KeeperBallInterceptionCalc extends ACalculator
{
	@Configurable(defValue = "-0.3", comment = "[s] Time difference between keeper reaching the intercept pos vs. ball travel time")
	private static double slackTime = -0.3;
	@Configurable(defValue = "0.1", comment = "[s]")
	private static double oldTargetInterceptionBaseBonus = 0.1;

	private BallInterceptor ballInterceptor;
	private IVector2 previousInterceptionPosition = null;
	@Getter
	private RatedBallInterception keeperBallInterception;
	private IBallTrajectory ballTrajectory;
	private BotID lastKeeper;


	public KeeperBallInterceptionCalc()
	{
		ballInterceptor = new BallInterceptor(
				botID -> Optional.empty(),
				this::getPreviousInterceptionTarget,
				this::findBallTrajectory,
				Color.BLUE
		);
	}


	@Override
	protected boolean isCalculationNecessary()
	{
		return getWFrame().getTigerBotsAvailable().containsKey(getAiFrame().getKeeperId())
				&& ballInterceptor.ballIsNotUnderOurControl(getWFrame())
				&& Geometry.getField().isPointInShape(getBall().getPos())
				&& getWFrame().getTiger(getAiFrame().getKeeperId()) != null;
	}


	@Override
	protected void reset()
	{
		ballTrajectory = null;
		keeperBallInterception = null;
		lastKeeper = BotID.noBot();
	}


	@Override
	public void doCalc()
	{
		ballInterceptor.setOldTargetInterceptionBonus(oldTargetInterceptionBaseBonus);
		ballInterceptor.setAreaOfInterest(Geometry.getField());
		ballInterceptor.setConsideredBots(Set.of(getAiFrame().getKeeperId()));
		ballInterceptor.setExclusionAreas(List.of(
				Geometry.getPenaltyAreaOur().withMargin(Geometry.getBotRadius() + Geometry.getBallRadius()),
				Geometry.getPenaltyAreaTheir().withMargin(Geometry.getBotRadius() + Geometry.getBallRadius())
		));
		ballInterceptor.setFinderParams(List.of(
				new InterceptionFinderParameters(Math.min(slackTime, -0.1), false, 0),
				new InterceptionFinderParameters(-0.1, false, 0)
		));

		var result = ballInterceptor.processFrame(getWFrame());

		keeperBallInterception = result.interceptionMap().get(getAiFrame().getKeeperId());
		getShapes(EAiShapesLayer.KEEPER_INTERCEPT).addAll(ballInterceptor.getShapes());

		if (keeperBallInterception != null)
		{
			previousInterceptionPosition = keeperBallInterception.getBallInterception().getPos();
		} else
		{
			previousInterceptionPosition = null;
		}

		lastKeeper = getAiFrame().getKeeperId();
	}


	private IBallTrajectory findBallTrajectory()
	{
		return getWFrame().getBall().getTrajectory();
	}


	private Optional<IVector2> getPreviousInterceptionTarget(BotID botID)
	{
		if (botID != lastKeeper)
		{
			return Optional.empty();
		}

		if (getAiFrame().getPrevFrame().getTacticalField().getKeeperBehavior() != EKeeperActionType.INTERCEPT_PASS)
		{
			return Optional.empty();
		}
		var ballDir = ballTrajectory.getTravelLine().directionVector();
		return Optional.ofNullable(previousInterceptionPosition)
				.filter(p -> (ballDir.angleToAbs(p.subtractNew(getBall().getPos())).orElse(0.0) < AngleMath.deg2rad(15)));
	}
}
