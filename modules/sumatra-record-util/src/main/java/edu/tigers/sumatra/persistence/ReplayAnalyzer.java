/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.persistence;

import edu.tigers.sumatra.ai.AIInfoFrame;
import edu.tigers.sumatra.ai.Ai;
import edu.tigers.sumatra.ai.VisualizationFrame;
import edu.tigers.sumatra.ai.metis.ballpossession.EBallPossession;
import edu.tigers.sumatra.ai.metis.statistics.stats.MatchStats;
import edu.tigers.sumatra.ids.EAiTeam;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.skillsystem.ASkillSystem;
import edu.tigers.sumatra.skillsystem.GenericSkillSystem;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Paths;
import java.util.EnumMap;
import java.util.Map;


/**
 * This class will provide a way to analyze recorded replays and create plots out of them
 * The base for it's plots will be the MatchStatistics Structure
 *
 * @author Phillipp Mevenkamp <phillippmevenkamp@gmail.com>
 */
@SuppressWarnings("squid:S106") // allow sysos
public class ReplayAnalyzer
{
	@SuppressWarnings("unused")
	private static final Logger log = LogManager.getLogger(ReplayAnalyzer.class
			.getName());

	private final Map<ETeamColor, Ai> agents = new EnumMap<>(ETeamColor.class);

	private MatchStats matchStatistics = null;


	/**
	 * @param args
	 */
	public static void main(final String[] args)
	{
		ReplayAnalyzer analyzer = new ReplayAnalyzer();
		analyzer.processFramesOfReplay();
	}


	/**
	 * This method will process a given replay and creates the needed statistics
	 */
	public void processFramesOfReplay()
	{
		String basePath = "data/record";
		String recordDbPath = "2016-04-08_10-59-59";

		PersistenceDb db = PersistenceDb.withCustomLocation(Paths.get(basePath, recordDbPath));
		db.add(WorldFrameWrapper.class, EPersistenceKeyType.SUMATRA_TIMESTAMP);
		PersistenceTable<WorldFrameWrapper> wfwTable = db.getTable(WorldFrameWrapper.class);

		ASkillSystem skillSystem = GenericSkillSystem.forAnalysis();
		agents.put(ETeamColor.YELLOW, new Ai(EAiTeam.YELLOW, skillSystem));
		agents.put(ETeamColor.BLUE, new Ai(EAiTeam.BLUE, skillSystem));

		Long key = db.getFirstKey();

		do
		{
			WorldFrameWrapper wfw = wfwTable.get(key);

			for (ETeamColor teamColor : ETeamColor.yellowBlueValues())
			{
				processVisFrame(teamColor, wfw);
			}

			Long nextKey = db.getNextKey(key);
			if (nextKey == null)
			{
				break;
			}
			key = nextKey;
		} while (true);

		db.close();

		System.out.println(matchStatistics.getBallPossessionGeneral().get(EBallPossession.NO_ONE)
				.getPercent());
	}


	private void processVisFrame(final ETeamColor teamColor, final WorldFrameWrapper wfw)
	{
		Ai agent = agents.get(teamColor);

		if ((agent.getLatestAiFrame() == null)
				|| (agent.getLatestAiFrame().getWorldFrame().getTimestamp() != wfw.getSimpleWorldFrame()
						.getTimestamp()))
		{
			AIInfoFrame aiFrame = agent.processWorldFrame(wfw);
			if (aiFrame != null)
			{
				VisualizationFrame visFrame = new VisualizationFrame(aiFrame);

				matchStatistics = visFrame.getMatchStats();
			}
		}
	}
}
