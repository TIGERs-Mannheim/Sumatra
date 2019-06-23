/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 12, 2016
 * Author(s): Phillipp Mevenkamp <phillippmevenkamp@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.persistance;

import java.util.EnumMap;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.ai.Agent;
import edu.tigers.sumatra.ai.data.MatchStatistics;
import edu.tigers.sumatra.ai.data.ballpossession.EBallPossession;
import edu.tigers.sumatra.ai.data.frames.AIInfoFrame;
import edu.tigers.sumatra.ai.data.frames.VisualizationFrame;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.wp.data.ShapeMap;
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
	private static final Logger				log					= Logger.getLogger(ReplayAnalyzer.class
																						.getName());
	
	private final Map<ETeamColor, Agent>	agents				= new EnumMap<>(ETeamColor.class);
	
	private MatchStatistics						matchStatistics	= null;
	
	
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
		
		RecordBerkeleyPersistence db = new RecordBerkeleyPersistence(basePath + "/" + recordDbPath, true);
		
		agents.put(ETeamColor.YELLOW, new Agent(ETeamColor.YELLOW));
		agents.put(ETeamColor.BLUE, new Agent(ETeamColor.BLUE));
		
		Long key = db.getFirstKey();
		
		do
		{
			RecordFrame recFrame = db.getRecordFrame(key);
			
			for (VisualizationFrame vF : recFrame.getVisFrames())
			{
				Agent agent = agents.get(vF.getTeamColor());
				WorldFrameWrapper wfw = new WorldFrameWrapper(
						vF.getWorldFrameWrapper().getSimpleWorldFrame(),
						vF.getWorldFrameWrapper().getRefereeMsg(), new ShapeMap());
				
				if ((agent.getLatestAiFrame() == null)
						|| (agent.getLatestAiFrame().getWorldFrame().getTimestamp() != wfw.getSimpleWorldFrame()
								.getTimestamp()))
				{
					AIInfoFrame aiFrame = agent.processWorldFrame(wfw);
					if (aiFrame != null)
					{
						VisualizationFrame visFrame = new VisualizationFrame(aiFrame);
						
						matchStatistics = visFrame.getMatchStatistics();
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
