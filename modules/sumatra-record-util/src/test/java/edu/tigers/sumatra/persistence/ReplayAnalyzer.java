/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.persistence;

import java.util.EnumMap;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.ai.Ai;
import edu.tigers.sumatra.ai.data.MatchStats;
import edu.tigers.sumatra.ai.data.MultiTeamMessage;
import edu.tigers.sumatra.ai.data.ballpossession.EBallPossession;
import edu.tigers.sumatra.ai.data.frames.AIInfoFrame;
import edu.tigers.sumatra.ai.data.frames.VisualizationFrame;
import edu.tigers.sumatra.ids.EAiTeam;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.skillsystem.ASkillSystem;
import edu.tigers.sumatra.skillsystem.GenericSkillSystem;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;


/**
 * This class will provide a way to analyze recorded replays and create plots out of them
 * The base for it's plots will be the MatchStatistics Structure
 * 
 * @author Phillipp Mevenkamp <phillippmevenkamp@gmail.com>
 */
public class ReplayAnalyzer
{
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(ReplayAnalyzer.class
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
		String basePath = "modules/sumatra-main/data/record";
		String recordDbPath = "2016-04-08_10-59-59";
		
		AiBerkeleyPersistence db = new AiBerkeleyPersistence(basePath + "/" + recordDbPath);
		db.open();
		
		ASkillSystem skillSystem = new GenericSkillSystem();
		agents.put(ETeamColor.YELLOW, new Ai(EAiTeam.YELLOW_PRIMARY, skillSystem));
		agents.put(ETeamColor.BLUE, new Ai(EAiTeam.BLUE_PRIMARY, skillSystem));
		
		Long key = db.getFirstKey();
		
		do
		{
			RecordFrame recFrame = db.getRecordFrame(key);
			
			for (VisualizationFrame vF : recFrame.getVisFrames())
			{
				Ai agent = agents.get(vF.getTeamColor());
				WorldFrameWrapper wfw = new WorldFrameWrapper(
						vF.getWorldFrameWrapper().getSimpleWorldFrame(),
						vF.getWorldFrameWrapper().getRefereeMsg(),
						vF.getWorldFrameWrapper().getGameState());
				
				if ((agent.getLatestAiFrame() == null)
						|| (agent.getLatestAiFrame().getWorldFrame().getTimestamp() != wfw.getSimpleWorldFrame()
								.getTimestamp()))
				{
					AIInfoFrame aiFrame = agent.processWorldFrame(wfw, MultiTeamMessage.DEFAULT);
					if (aiFrame != null)
					{
						VisualizationFrame visFrame = new VisualizationFrame(aiFrame);
						
						matchStatistics = visFrame.getMatchStats();
					}
				}
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
}
