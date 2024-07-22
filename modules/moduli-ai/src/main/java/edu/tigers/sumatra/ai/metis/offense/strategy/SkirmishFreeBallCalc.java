/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.strategy;

import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.botdistance.BotDistance;
import edu.tigers.sumatra.ai.metis.general.ESkirmishStrategy;
import edu.tigers.sumatra.ai.metis.general.SkirmishInformation;
import edu.tigers.sumatra.drawable.DrawableBorderText;
import edu.tigers.sumatra.drawable.EFontSize;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.time.TimestampTimer;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import lombok.RequiredArgsConstructor;

import java.awt.Color;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;


/**
 * Activate free ball if skirmish was detected
 */
@RequiredArgsConstructor
public class SkirmishFreeBallCalc extends ACalculator
{
	private final TimestampTimer timer = new TimestampTimer(0.25);

	private final Supplier<SkirmishInformation> skirmishInformation;
	private final Supplier<BotDistance> opponentClosestToBall;
	private final Supplier<List<BotID>> ballHandlingBots;
	private final Supplier<List<BotID>> supportiveAttackers;

	private ITrackedBot attacker;
	private boolean fail = false;
	private String info = "";


	@Override
	protected boolean isCalculationNecessary()
	{
		return !ballHandlingBots.get().isEmpty()
				&& skirmishInformation.get().getStrategy() == ESkirmishStrategy.FREE_BALL;
	}


	@Override
	public void doCalc()
	{
		attacker = getWFrame().getBot(ballHandlingBots.get().stream().findFirst().orElse(BotID.noBot()));
		fail = false;
		info = "";

		if (getAiFrame().getPrevFrame().getTacticalField().getSkirmishInformation().isStartCircleMove())
		{
			// Keep going
			skirmishInformation.get().setStartCircleMove(startCircleMove());
			return;
		}

		checkAngle();

		// check if secondary supp has arrived
		ITrackedBot bot = getSupportiveAttacker();
		if (bot == null)
		{
			fail = true;
			info += "No Secondary | ";
		} else
		{
			double dist = bot.getPos().distanceTo(skirmishInformation.get().getSupportiveCircleCatchPos());
			if (dist > 100)
			{
				fail = true;
				info += "dist error | ";
			}
		}

		// check if hasContact()
		if (!attacker.getBallContact().hadRecentContact())
		{
			info += "ballContact error | ";
			fail = true;
		}

		// check if opponent bot is actually close to ball
		IVector2 opponentPos = getWFrame().getBot(opponentClosestToBall.get().getBotId()).getPos();
		IVector2 ballPos = getBall().getPos();
		if (opponentPos.distanceTo(ballPos) > Geometry.getBotRadius() + Geometry.getBallRadius() + 35)
		{
			info += "opponent dist fail | ";
			fail = true;
		}

		if (!Geometry.getField().withMargin(-400).isPointInShape(ballPos))
		{
			info += "field Border |";
			fail = true;
		}

		if (Geometry.getPenaltyAreaOur().withMargin(1000).isPointInShape(ballPos))
		{
			info += "Geometry Area";
			fail = true;
		}

		if (!fail)
		{
			info = "Activate !!!";
			// init turn move timer here.
			timer.start(getWFrame().getTimestamp());
			skirmishInformation.get().setStartCircleMove(true);
		}

		getShapes(EAiShapesLayer.AI_SKIRMISH_DETECTOR)
				.add(new DrawableBorderText(Vector2.fromXY(10, 70), info)
						.setFontSize(EFontSize.LARGE)
						.setColor(Color.red));
	}


	private void checkAngle()
	{
		// check if frontal combat
		IVector2 ballPos = getBall().getPos();
		IVector2 ballToMe = attacker.getPos().subtractNew(ballPos);
		// opponent bot cannot be null, else there would be no skirmish
		IVector2 opponentPos = getWFrame().getBot(opponentClosestToBall.get().getBotId()).getPos();
		IVector2 ballToOpponent = opponentPos.subtractNew(ballPos);
		Optional<Double> angle = ballToOpponent.angleTo(ballToMe);
		if (angle.isPresent())
		{
			double degress = Math.abs(AngleMath.rad2deg(angle.get()));
			info += degress + " | ";
			if (180 - degress > 30)
			{
				fail = true;
				info += "degree fail | ";
			}
		} else
		{
			fail = true;
		}
	}


	private boolean startCircleMove()
	{
		timer.update(getWFrame().getTimestamp());
		if (timer.isTimeUp(getWFrame().getTimestamp()))
		{
			return false;
		}

		// set actual turn command here
		getShapes(EAiShapesLayer.AI_SKIRMISH_DETECTOR)
				.add(new DrawableBorderText(Vector2.fromXY(10, 110), "TURN !")
						.setFontSize(EFontSize.LARGE)
						.setColor(Color.red));
		return true;
	}


	private ITrackedBot getSupportiveAttacker()
	{
		return supportiveAttackers.get()
				.stream()
				.map(b -> getWFrame().getBot(b))
				.findAny()
				.orElse(null);
	}
}
