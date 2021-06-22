/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.statistics;

import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.ballpossession.BallPossession;
import edu.tigers.sumatra.ai.metis.botdistance.BotDistance;
import edu.tigers.sumatra.ai.metis.statistics.stats.MatchStats;
import edu.tigers.sumatra.ai.metis.statistics.timeseries.AiRoleNumberTssCalc;
import edu.tigers.sumatra.ai.metis.statistics.timeseries.BallDistTssCalc;
import edu.tigers.sumatra.ai.metis.statistics.timeseries.BallPosTssCalc;
import edu.tigers.sumatra.ai.metis.statistics.timeseries.BallPossessionTssCalc;
import edu.tigers.sumatra.ai.metis.statistics.timeseries.BallVelTssCalc;
import edu.tigers.sumatra.ai.metis.statistics.timeseries.GameEventsTssCalc;
import edu.tigers.sumatra.ai.metis.statistics.timeseries.ITssCalc;
import edu.tigers.sumatra.ai.metis.statistics.timeseries.RefereeTssCalc;
import edu.tigers.sumatra.ai.metis.statistics.timeseries.StatisticsTssCalc;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.referee.proto.SslGcRefereeMessage;
import edu.tigers.sumatra.statistics.StatisticsSaver;
import edu.tigers.sumatra.statistics.TimeSeriesStatsEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;


/**
 * Extract interesting time series data from AI and send it to the {@link StatisticsSaver}.
 * Do nothing, if the module is not loaded.
 */
public class TimeSeriesStatsCalc extends ACalculator
{
	private StatisticsSaver statisticsSaver;

	private SslGcRefereeMessage.Referee.Stage currentStage = SslGcRefereeMessage.Referee.Stage.NORMAL_FIRST_HALF_PRE;
	private long initialStageTime = 0;
	private String identifierSuffix;

	private final List<ITssCalc> tssCalcs = new ArrayList<>();


	public TimeSeriesStatsCalc(
			Supplier<Map<EPlay, Set<BotID>>> desiredBotMap,
			Supplier<List<BotDistance>> opponentsToBallDist,
			Supplier<List<BotDistance>> tigersToBallDist,
			Supplier<BallPossession> ballPossession,
			Supplier<MatchStats> matchStats
	)
	{
		tssCalcs.add(new AiRoleNumberTssCalc(
				desiredBotMap
		));
		tssCalcs.add(new BallDistTssCalc(
				opponentsToBallDist,
				tigersToBallDist
		));
		tssCalcs.add(new BallPossessionTssCalc(
				ballPossession
		));
		tssCalcs.add(new BallPosTssCalc());
		tssCalcs.add(new BallVelTssCalc());
		tssCalcs.add(new GameEventsTssCalc());
		tssCalcs.add(new RefereeTssCalc());
		tssCalcs.add(new StatisticsTssCalc(
				matchStats
		));
	}


	@Override
	public boolean isCalculationNecessary()
	{
		return statisticsSaver != null
				&& statisticsSaver.hasWriter()
				&& !getAiFrame().getWorldFrame().getBots().isEmpty() &&
				getAiFrame().getGameState().isGameRunning();
	}


	@Override
	public void doCalc()
	{
		if (currentStage != getAiFrame().getRefereeMsg().getStage())
		{
			initialStageTime = getAiFrame().getRefereeMsg().getStageTimeLeft();
			currentStage = getAiFrame().getRefereeMsg().getStage();
		}

		if (identifierSuffix == null)
		{
			final String stage = getAiFrame().getRefereeMsg().getStage().name();
			final String teamName = getAiFrame().getRefereeMsg().getTeamInfo(getAiFrame().getTeamColor()).getName()
					.replaceAll(" ", "_");
			final String teamColor = getAiFrame().getTeamColor().name().toLowerCase();
			identifierSuffix = stage + "_" + teamName + "_" + teamColor;
		}

		long timestamp = (initialStageTime - getAiFrame().getRefereeMsg().getStageTimeLeft()) * 1000;

		generateEntries(timestamp);
	}


	private void generateEntries(final long timestamp)
	{
		final List<TimeSeriesStatsEntry> entries = tssCalcs.stream()
				.map(c -> c.createTimeSeriesStatsEntry(getAiFrame(), timestamp))
				.collect(Collectors.toList());

		for (TimeSeriesStatsEntry entry : entries)
		{
			entry.addTag("stage", getAiFrame().getRefereeMsg().getStage().name());
			entry.addTag("team.name",
					getAiFrame().getRefereeMsg().getTeamInfo(getAiFrame().getTeamColor()).getName().replaceAll(" ", "_"));
			entry.addTag("team.color", getAiFrame().getTeamColor().name().toLowerCase());
			SumatraModel.getInstance().getModule(StatisticsSaver.class).add(identifierSuffix, entry);
		}
	}


	@Override
	protected void start()
	{
		super.start();

		statisticsSaver = SumatraModel.getInstance().getModuleOpt(StatisticsSaver.class).orElse(null);
	}


	@Override
	protected void stop()
	{
		super.stop();

		statisticsSaver = null;
	}
}
