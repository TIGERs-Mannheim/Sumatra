/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.common;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.statistics.StatisticsSaver;
import edu.tigers.sumatra.statistics.TimeSeriesStatsEntry;


public class TimeSeriesStatisticsSaver
{
	private String identifierSuffix;


	private void initIdentifierSuffix(BaseAiFrame aiFrame)
	{
		if (identifierSuffix == null)
		{
			final String stage = aiFrame.getRefereeMsg().getStage().name();
			final String teamName = aiFrame.getRefereeMsg().getTeamInfo(aiFrame.getTeamColor()).getName()
					.replace(" ", "_");
			final String teamColor = aiFrame.getTeamColor().name().toLowerCase();
			identifierSuffix = stage + "_" + teamName + "_" + teamColor;
		}
	}


	public void add(BaseAiFrame aiFrame, TimeSeriesStatsEntry entry)
	{
		initIdentifierSuffix(aiFrame);
		entry.addTag("stage", aiFrame.getRefereeMsg().getStage().name());
		entry.addTag("team.name",
				aiFrame.getRefereeMsg().getTeamInfo(aiFrame.getTeamColor()).getName().replace(" ", "_"));
		entry.addTag("team.color", aiFrame.getTeamColor().name().toLowerCase());
		SumatraModel.getInstance().getModuleOpt(StatisticsSaver.class).ifPresent(ss -> ss.add(identifierSuffix, entry));
	}
}
