/*
 * Copyright (c) 2009 - 2019, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.movementlimits;

import static edu.tigers.sumatra.ids.ETeamColor.YELLOW;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.bot.params.BotMovementLimits;
import edu.tigers.sumatra.bot.params.BotParams;
import edu.tigers.sumatra.botparams.BotParamsManager;
import edu.tigers.sumatra.botparams.EBotParamLabel;
import edu.tigers.sumatra.filter.iir.ExponentialMovingAverageFilter;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.thread.GeneralPurposeTimer;
import edu.tigers.sumatra.wp.IWorldFrameObserver;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;


/**
 * Observes bot velocities and stores them in a database.
 *
 * @author Dominik Engelhardt
 */
public class MovementObserver implements IWorldFrameObserver
{
	private static final Logger log = LogManager.getLogger(MovementObserver.class);

	@Configurable(defValue = "0.5", comment = "alpha used in ExponentialMovingAverageFilter")
	private static double alpha = 0.5;

	private Map<BotID, ExponentialMovingAverageFilter> history = new HashMap<>();
	private EnumMap<ETeamColor, Double> maxObservedVelocities = new EnumMap<>(ETeamColor.class);

	private BotParamsManager botParamsManager;

	private boolean publishScheduled = false;


	/**
	 * Constructor.
	 *
	 * @param botParamsManager
	 */
	public MovementObserver(BotParamsManager botParamsManager)
	{
		this.botParamsManager = botParamsManager;

		for (ETeamColor color : ETeamColor.yellowBlueValues())
		{
			final BotParams selectedParams = botParamsManager.getDatabase().getSelectedParams(teamToLabel(color));
			double vel = selectedParams.getMovementLimits().getVelMax();
			maxObservedVelocities.put(color, vel);
		}
	}


	@Override
	public void onNewWorldFrame(final WorldFrameWrapper wFrameWrapper)
	{
		if (wFrameWrapper.getGameState().isRunning())
		{
			for (ITrackedBot bot : wFrameWrapper.getSimpleWorldFrame().getBots().values())
			{
				calcPerBot(bot);
			}
		}
	}


	private void calcPerBot(final ITrackedBot bot)
	{
		history.putIfAbsent(bot.getBotId(), new ExponentialMovingAverageFilter(alpha, 0));
		ExponentialMovingAverageFilter filter = history.get(bot.getBotId());

		updateAverage(bot, filter);
		updateMaximum(bot, filter);
	}


	private void updateAverage(ITrackedBot bot, ExponentialMovingAverageFilter filter)
	{
		final double currentVelocity = bot.getVel().getLength();
		filter.update(currentVelocity);
	}


	private void updateMaximum(final ITrackedBot bot, ExponentialMovingAverageFilter filter)
	{
		if (filter.getState() > maxObservedVelocities.get(bot.getTeamColor()))
		{
			maxObservedVelocities.put(bot.getTeamColor(), filter.getState());
			scheduleVelocityPublish();
		}
	}


	private void scheduleVelocityPublish()
	{
		if (publishScheduled)
		{
			return;
		}
		GeneralPurposeTimer.getInstance().schedule(new TimerTask()
		{
			@Override
			public void run()
			{
				for (ETeamColor color : ETeamColor.yellowBlueValues())
				{
					publishNewVelocity(color);
				}
				publishScheduled = false;
			}
		}, 1000L);
		publishScheduled = true;
	}


	private void publishNewVelocity(final ETeamColor teamColor)
	{
		final Double velMax = maxObservedVelocities.get(teamColor);
		updateDatabase(teamColor, velMax);
		log.debug("Observed new velocity of " + velMax + " m/s for team " + teamColor);
	}


	private void updateDatabase(final ETeamColor teamColor, double velMax)
	{
		EBotParamLabel label = teamToLabel(teamColor);
		BotParams currentParams = botParamsManager.getDatabase().getSelectedParams(label);
		BotMovementLimits currentLimits = (BotMovementLimits) currentParams.getMovementLimits();
		currentLimits.setVelMax(velMax);
		String teamName = botParamsManager.getDatabase().getTeamStringForLabel(label);
		botParamsManager.onEntryUpdated(teamName, currentParams);
	}


	private EBotParamLabel teamToLabel(ETeamColor color)
	{
		if (color == YELLOW)
		{
			return EBotParamLabel.YELLOW_LIVE;
		} else
		{
			return EBotParamLabel.BLUE_LIVE;
		}
	}
}
