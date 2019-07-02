package edu.tigers.sumatra.ai.metis.statistics.timeseries;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import edu.tigers.sumatra.Referee;
import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.TacticalField;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.statistics.StatisticsSaver;
import edu.tigers.sumatra.statistics.TimeSeriesStatsEntry;


/**
 * Extract interesting time series data from AI and send it to the {@link StatisticsSaver}.
 * Do nothing, if the module is not loaded.
 */
public class TimeSeriesStatsCalc extends ACalculator
{
	private StatisticsSaver statisticsSaver;
	
	private Referee.SSL_Referee.Stage currentStage = Referee.SSL_Referee.Stage.NORMAL_FIRST_HALF_PRE;
	private long initialStageTime = 0;
	private String identifierSuffix;
	
	private final List<ITssCalc> tssCalcs = new ArrayList<>();
	
	
	public TimeSeriesStatsCalc()
	{
		tssCalcs.add(new AiRoleNumberTssCalc());
		tssCalcs.add(new BallDistTssCalc());
		tssCalcs.add(new BallPossessionTssCalc());
		tssCalcs.add(new BallPosTssCalc());
		tssCalcs.add(new BallVelTssCalc());
		tssCalcs.add(new GameEventsTssCalc());
		tssCalcs.add(new RefereeTssCalc());
		tssCalcs.add(new StatisticsTssCalc());
	}
	
	
	@Override
	public boolean isCalculationNecessary(final TacticalField tacticalField, final BaseAiFrame aiFrame)
	{
		return statisticsSaver != null
				&& statisticsSaver.hasWriter()
				&& !getAiFrame().getWorldFrame().getBots().isEmpty() &&
				getAiFrame().getGamestate().isGameRunning();
	}
	
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
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
				.map(c -> c.createTimeSeriesStatsEntry(getAiFrame(), getNewTacticalField(), timestamp))
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
