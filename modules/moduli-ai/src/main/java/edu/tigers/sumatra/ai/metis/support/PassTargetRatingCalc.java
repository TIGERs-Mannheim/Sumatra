/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.support;

import static edu.tigers.sumatra.math.SumatraMath.min;

import java.awt.Color;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.TacticalField;
import edu.tigers.sumatra.ai.metis.general.ChipKickReasonableDecider;
import edu.tigers.sumatra.ai.metis.offense.OffensiveMath;
import edu.tigers.sumatra.ai.metis.targetrater.MaxAngleKickRater;
import edu.tigers.sumatra.ai.metis.targetrater.PassInterceptionRater;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.ai.pandora.roles.offense.attacker.AttackerRole;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.BotIDMap;
import edu.tigers.sumatra.ids.IBotIDMap;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * This class rates the PassTargets created by PassTargetGenerationCalc
 */
public class PassTargetRatingCalc extends ACalculator
{
	@Configurable(comment = "Upper x-ball position for Situation weight", defValue = "2500")
	private static double upperBallSituationPosition = 2500;
	
	@Configurable(comment = "Lower x-ball position for situation weight", defValue = "-2500")
	private static double lowerBallSituationPosition = -2500;
	
	@Configurable(comment = "ReceiveWeightBias (guaranteed percentage of receive weight)", defValue = "0.3")
	private static double receiveWeightBias = 0.3;
	
	private ITrackedBot attacker = null;
	private boolean attackerCanKickOrCatchTheBall = false;
	private IVector2 passOrigin;
	
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		attacker = attackerId().map(id -> getWFrame().getBot(id)).orElse(null);
		attackerCanKickOrCatchTheBall = attackerRole().map(AttackerRole::canKickOrCatchTheBall).orElse(false);
		passOrigin = attackerCanKickOrCatchTheBall ? attackerPosition() : getBall().getPos();
		
		for (PassTarget passTarget : newTacticalField.getAllPassTargets())
		{
			final double passScore = passScore(passTarget);
			final double goalKickScore = goalKickScore(passTarget);
			passTarget.setPassScore(passScore);
			passTarget.setGoalKickScore(goalKickScore);
			passTarget.setScore(weightBySituation(passScore, goalKickScore));
		}
	}
	
	
	private double weightBySituation(final double passScore, final double goalkickScore)
	{
		double situationWeight = situationShootWeight(passOrigin);
		return (passScore * (1 - situationWeight)) + (goalkickScore * situationWeight);
	}
	
	
	private Optional<BotID> attackerId()
	{
		return getAiFrame().getPrevFrame().getTacticalField().getOffensiveStrategy().getAttackerBot();
	}
	
	
	private IVector2 attackerPosition()
	{
		IVector2 attackerKickerPos = attacker.getBotKickerPos();
		getNewTacticalField().getDrawableShapes().get(EAiShapesLayer.PASS_TARGETS).add(
				new DrawableCircle(Circle.createCircle(attackerKickerPos, 200), Color.RED));
		getNewTacticalField().getDrawableShapes().get(EAiShapesLayer.PASS_TARGETS).add(
				new DrawableCircle(Circle.createCircle(attackerKickerPos, 220), Color.RED));
		return attackerKickerPos;
	}
	
	
	private Optional<AttackerRole> attackerRole()
	{
		return getAiFrame().getPrevFrame().getPlayStrategy()
				.getActiveRoles(ERole.ATTACKER)
				.stream()
				.map(r -> (AttackerRole) r)
				.findFirst();
	}
	
	
	private double situationShootWeight(IVector2 alternativeBallPos)
	{
		double ballX = alternativeBallPos.x();
		double situationScoreWeight = SumatraMath.relative(ballX, lowerBallSituationPosition, upperBallSituationPosition);
		situationScoreWeight = min(situationScoreWeight, 1 - receiveWeightBias);
		return situationScoreWeight;
	}
	
	
	private double passScore(final IPassTarget passTarget)
	{
		if (attacker == null)
		{
			return 0;
		}
		
		List<ITrackedBot> consideredBots = getWFrame().getFoeBots().values().stream()
				.filter(b -> b.getBotId() != getAiFrame().getKeeperFoeId())
				.collect(Collectors.toList());
		
		if (attackerCanKickOrCatchTheBall && chipKickIsRequiredFor(passTarget))
		{
			return PassInterceptionRater.rateChippedPass(passOrigin, passTarget.getKickerPos(), consideredBots);
		}
		return PassInterceptionRater.rateStraightPass(passOrigin, passTarget.getKickerPos(), consideredBots);
	}
	
	
	private boolean chipKickIsRequiredFor(final IPassTarget passTarget)
	{
		final double distance = passOrigin.distanceTo(passTarget.getKickerPos());
		double passSpeedForChipDetection = OffensiveMath.passSpeedChip(distance);
		
		IBotIDMap<ITrackedBot> obstacles = new BotIDMap<>(getWFrame().getBots());
		obstacles.remove(passTarget.getBotId());
		
		return new ChipKickReasonableDecider(
				passOrigin,
				passTarget.getKickerPos(),
				obstacles.values(),
				passSpeedForChipDetection)
						.isChipKickReasonable();
	}
	
	
	private double goalKickScore(final IPassTarget passTarget)
	{
		return MaxAngleKickRater.getDirectShootScoreChance(getWFrame().getFoeBots().values(), passTarget.getKickerPos());
	}
}
