/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.support.behaviors.repulsive;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.offense.action.RatedOffensiveAction;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.bot.BotState;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.circle.IArc;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.wp.data.ITrackedBot;

import java.awt.Color;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;


/**
 * Determine a good mid-field position.
 */
public class MidfieldRepulsiveBehavior extends ARepulsiveBehavior
{
	@Configurable(comment = "Defines whether this behavior is active of not", defValue = "true")
	private static boolean enabled = true;
	@Configurable(comment = "Whether force field should be drawn or not", defValue = "false")
	private static boolean drawing = false;

	@Configurable(comment = "[mm]", defValue = "5000.0")
	private static double magnitudeCenteringForce = 5000;

	@Configurable(comment = "[mm]", defValue = "3000.0")
	private static double sigmaCenteringForce = 3000;

	@Configurable(comment = "[mm]", defValue = "1500.0")
	private static double pointOfInterestOffset = 1500;

	@Configurable(comment = "[mm]", defValue = "-2000.0")
	private static double xPosMidfieldNotViable = -2000;

	private final Supplier<Map<EPlay, Set<BotID>>> desiredBots;
	private final Supplier<List<IArc>> offensiveShadows;
	private final Supplier<Map<BotID, RatedOffensiveAction>> offensiveActions;


	public MidfieldRepulsiveBehavior(
			Supplier<Map<EPlay, Set<BotID>>> desiredBots,
			Supplier<Map<BotID, RatedOffensiveAction>> offensiveActions,
			Supplier<List<IArc>> offensiveShadows)
	{
		super(Color.cyan, desiredBots);
		this.desiredBots = desiredBots;
		this.offensiveShadows = offensiveShadows;
		this.offensiveActions = offensiveActions;
	}


	@Override
	List<Force> collectForces(
			BotState affectedBot,
			Collection<ITrackedBot> supporter,
			Collection<ITrackedBot> opponents
	)
	{
		var forceGenerator = new RepulsivePassReceiverForceGenerator(
				this.offensiveShadows.get(), this.offensiveActions.get(), this.desiredBots.get(), getWFrame());
		var forces = forceGenerator.getRepulsivePassReceiverForces(getWFrame(), affectedBot, supporter, opponents);
		double pointOfInterest =
				pointOfInterestOffset * getWFrame().getBall().getPos().x() / (Geometry.getFieldLength() / 2.0);

		Vector2 position = Vector2.fromXY(Geometry.getGoalTheir().getCenter().x() + pointOfInterest,
				affectedBot.getPos().y());
		forces.add(new Force(position, sigmaCenteringForce, magnitudeCenteringForce, 0, true));

		position = Vector2.fromXY(Geometry.getGoalOur().getCenter().x() + pointOfInterest, affectedBot.getPos().y());
		forces.add(new Force(position, sigmaCenteringForce, magnitudeCenteringForce, 0, true));
		return forces;
	}


	@Override
	public boolean isEnabled()
	{
		return enabled;
	}


	@Override
	public boolean isDrawing()
	{
		return drawing;
	}


	@Override
	public double getViability(BotID botID)
	{
		if (getWFrame().getBall().getPos().x() > xPosMidfieldNotViable)
		{
			return 0;
		}
		return 1;
	}
}
