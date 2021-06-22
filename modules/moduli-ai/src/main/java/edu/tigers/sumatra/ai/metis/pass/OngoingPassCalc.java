/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.pass;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.kicking.OngoingPass;
import edu.tigers.sumatra.ai.metis.kicking.Pass;
import edu.tigers.sumatra.ai.metis.offense.action.OffensiveAction;
import edu.tigers.sumatra.data.TimeLimitedBuffer;
import edu.tigers.sumatra.drawable.DrawableArrow;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.wp.data.BallKickFitState;
import lombok.RequiredArgsConstructor;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;


/**
 * Track the ongoing pass by matching passes with the ball kick fit state.
 */
@RequiredArgsConstructor
public class OngoingPassCalc extends ACalculator
{
	@Configurable(defValue = "0.5", comment = "Time horizon [s] of the pass buffer")
	private static double passBufferHorizon = 0.5;

	@Configurable(defValue = "10", comment = "Max passes to keep in buffer")
	private static int maxPassesToRemember = 10;

	@Configurable(defValue = "30.0", comment = "min Angle to accept new KickEvent direction [deg]")
	private static double maxAcceptableKickEventAngleChange = 30.0;

	@Configurable(defValue = "0.5", comment = "Min score to accept pass")
	private static double minBallKickWithPassFitScore = 0.5;

	@Configurable(defValue = "1.0", comment = "Min velocity [m/s] of the ball to consider")
	private static double minKickVelocity = 1.0;

	@Configurable(defValue = "0.3", comment = "Min kick fit age [s] after which passes can be rejected")
	private static double minKickFitAge = 0.3;

	private final TimeLimitedBuffer<Pass> passBuffer = new TimeLimitedBuffer<>();

	private OngoingPass ongoingPass;


	@Override
	public void doCalc()
	{
		passBuffer.setMaxElements(maxPassesToRemember);
		passBuffer.setMaxDuration(passBufferHorizon);
		getActivePass().ifPresent(pass -> passBuffer.add(getWFrame().getTimestamp(), pass));

		Optional<BallKickFitState> kickFitStateOpt = getWFrame().getKickFitState()
				.filter(b -> b.getAbsoluteKickSpeed() > minKickVelocity);
		if (noKickEventForUs() || kickFitStateOpt.isEmpty())
		{
			ongoingPass = null;
			return;
		}
		var kickFitState = kickFitStateOpt.get();

		List<Pass> consideredPasses = new ArrayList<>(passBuffer.getElements());
		getOngoingPass().map(OngoingPass::getPass).ifPresent(consideredPasses::add);

		var bestMatchingPass = consideredPasses.stream()
				.max(Comparator.comparing(p -> ballKickWithPassFitScore(kickFitState, p)));
		ongoingPass = bestMatchingPass
				.filter(pass -> acceptPass(kickFitState, pass))
				.map(this::updatePass)
				.orElse(null);

		getOngoingPass().map(OngoingPass::getPass).ifPresent(this::drawPass);
	}


	private boolean acceptPass(BallKickFitState kickFitState, Pass pass)
	{
		double age = (getWFrame().getTimestamp() - kickFitState.getKickTimestamp()) / 1e9;
		if (age < minKickFitAge)
		{
			return true;
		}
		return ballKickWithPassFitScore(kickFitState, pass) > minBallKickWithPassFitScore;
	}


	public Optional<OngoingPass> getOngoingPass()
	{
		return Optional.ofNullable(ongoingPass);
	}


	private OngoingPass updatePass(Pass pass)
	{
		if (ongoingPass != null && ongoingPass.getPass().equals(pass))
		{
			return ongoingPass;
		}
		long startTime = getWFrame().getTimestamp();
		return OngoingPass.builder().pass(pass).kickStartTime(startTime).build();
	}


	private double ballKickWithPassFitScore(BallKickFitState ballKickFitState, Pass pass)
	{
		double angleDiff = ballKickFitState.getKickVel().getXYVector()
				.angleToAbs(pass.getKick().getKickVel().getXYVector())
				.orElse(AngleMath.PI_TWO);

		return SumatraMath.relative(angleDiff, AngleMath.deg2rad(maxAcceptableKickEventAngleChange), 0);
	}


	private boolean noKickEventForUs()
	{
		return getWFrame().getKickEvent()
				.filter(e -> e.getKickingBot().getTeamColor() == getWFrame().getTeamColor())
				.isEmpty();
	}


	private Optional<Pass> getActivePass()
	{
		var keeperPass = getAiFrame().getPrevFrame().getTacticalField().getKeeperPass();
		if (keeperPass != null)
		{
			return Optional.of(keeperPass);
		}
		return getAiFrame().getPrevFrame().getTacticalField().getOffensiveStrategy()
				.getAttackerBot()
				.map(e -> getAiFrame().getPrevFrame().getTacticalField().getOffensiveActions().get(e))
				.map(OffensiveAction::getPass);
	}


	private void drawPass(Pass pass)
	{
		IVector2 kickLine = pass.getKick().getTarget().subtractNew(pass.getKick().getSource());
		getShapes(EAiShapesLayer.OFFENSIVE_ONGOING_PASS).add(
				new DrawableArrow(pass.getKick().getSource(), kickLine)
						.setColor(new Color(0, 255, 191, 100))
		);
	}
}
