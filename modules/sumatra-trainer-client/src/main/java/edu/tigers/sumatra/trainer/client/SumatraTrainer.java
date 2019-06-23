/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.trainer.client;

import static edu.tigers.sumatra.wp.data.BallTrajectoryState.aBallState;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.github.g3force.configurable.ConfigRegistration;

import edu.tigers.autoreferee.engine.IAutoRefEngine;
import edu.tigers.autoreferee.engine.log.GameLogEntry;
import edu.tigers.autoreferee.module.AutoRefModule;
import edu.tigers.moduli.exceptions.InitModuleException;
import edu.tigers.moduli.exceptions.StartModuleException;
import edu.tigers.sumatra.Referee;
import edu.tigers.sumatra.ai.AAgent;
import edu.tigers.sumatra.ai.athena.EAIControlState;
import edu.tigers.sumatra.bot.RobotInfo;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.EAiTeam;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.sim.SimTimeBlocker;
import edu.tigers.sumatra.sim.SimulationHelper;
import edu.tigers.sumatra.sim.SimulationParameters;
import edu.tigers.sumatra.trainer.Result;
import edu.tigers.sumatra.wp.data.ITrackedBall;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.TrackedBall;
import edu.tigers.sumatra.wp.data.TrackedBot;


public class SumatraTrainer
{
	private static final String MODULI_CONFIG = "sumatra_trainer.xml";
	private static Logger logger = Logger.getLogger(SumatraTrainer.class);
	
	
	public List<Result> doTraining(double duration)
	{
		initSimulation();
		runSimulation(duration);
		List<Result> info = collectInfo();
		shutdown();
		return info;
	}
	
	
	private void initSimulation()
	{
		ConfigRegistration.setDefPath("../../config/");
		SumatraModel.getInstance().setCurrentModuliConfig(MODULI_CONFIG);
		SumatraModel.getInstance().loadModulesSafe(MODULI_CONFIG);
		Geometry.refresh();
		
		try
		{
			SumatraModel.getInstance().startModules();
			AutoRefModule autoRefModule = SumatraModel.getInstance().getModule(AutoRefModule.class);
			autoRefModule.start(IAutoRefEngine.AutoRefMode.ACTIVE);
		} catch (InitModuleException | StartModuleException e)
		{
			logger.error(e);
		}
		SimulationHelper.setSimulateWithMaxSpeed(true);
		SumatraModel.getInstance().getModule(AAgent.class).changeMode(EAiTeam.YELLOW, EAIControlState.MATCH_MODE);
		SumatraModel.getInstance().getModule(AAgent.class).changeMode(EAiTeam.BLUE, EAIControlState.MATCH_MODE);
		
	}
	
	
	private List<ITrackedBot> initBots()
	{
		List<ITrackedBot> bots = new ArrayList<>();
		long timestamp = 0;
		int side = -1;
		for (ETeamColor team : new ETeamColor[] { ETeamColor.BLUE, ETeamColor.YELLOW })
		{
			final int numberOfBots = 8;
			for (int i = 0; i < numberOfBots; i++)
			{
				BotID botID = BotID.createBotId(i, team);
				IVector2 pos = Vector2.fromXY(Geometry.getFieldLength() * i / numberOfBots,
						side * Geometry.getFieldWidth() / 2.);
				ITrackedBot bot = TrackedBot.newBuilder()
						.withBotId(botID)
						.withTimestamp(timestamp)
						.withPos(pos)
						.withVel(Vector2.zero())
						.withOrientation(0)
						.withAngularVel(0)
						.withLastBallContact(0)
						.withBotInfo(RobotInfo.stubBuilder(botID, timestamp).build())
						.build();
				bots.add(bot);
			}
			side *= -1;
		}
		return bots;
	}
	
	
	private void runSimulation(double duration)
	{
		ITrackedBall ball = TrackedBall.fromTrajectoryStateVisible(0, aBallState()
				.withPos(Vector2.zero())
				.withVel(Vector2.zero())
				.build());
		
		SimulationParameters params = new SimulationParameters(initBots(), ball);
		params.setRefereeCommand(Referee.SSL_Referee.Command.NORMAL_START);
		SimulationHelper.loadSimulation(params);
		SimulationHelper.stopSimulation();
		SimulationHelper.startSimulation();
		
		SimTimeBlocker simTimeBlocker = new SimTimeBlocker(duration);
		simTimeBlocker.await();
		SimulationHelper.stopSimulation();
	}
	
	
	private List<Result> collectInfo()
	{
		List<Result> results = new ArrayList<>();
		
		AAgent agent = SumatraModel.getInstance().getModule(AAgent.class);
		if (agent.getAi(EAiTeam.YELLOW).isPresent())
		{
			results.add(new Result("yellowStats", agent.getAi(EAiTeam.YELLOW).get()
					.getLatestAiFrame().getTacticalField().getMatchStatistics().toJSON()));
			
		}
		if (agent.getAi(EAiTeam.BLUE).isPresent())
		{
			results.add(new Result("blueStats", agent.getAi(EAiTeam.BLUE).get()
					.getLatestAiFrame().getTacticalField().getMatchStatistics().toJSON()));
		}
		
		results.add(new Result("gamelog",
				SumatraModel.getInstance().getModule(AutoRefModule.class).getEngine().getGameLog().getEntries()
						.stream()
						.map(GameLogEntry::toString)
						.reduce((a, b) -> a + b).orElse("")));
		
		
		return results;
	}
	
	
	private void shutdown()
	{
		AutoRefModule autoRefModule = SumatraModel.getInstance().getModule(AutoRefModule.class);
		autoRefModule.stop();
		SumatraModel.getInstance().stopModules();
	}
	
	
	public static void main(String... args)
	{
		SumatraTrainer trainer = new SumatraTrainer();
		trainer.doTraining(10);
	}
}
