/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.general;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.Referee;
import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.TacticalField;
import edu.tigers.sumatra.bot.RobotInfo;
import edu.tigers.sumatra.export.CSVExporter;
import edu.tigers.sumatra.ids.BotIDMap;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.trajectory.ITrajectory;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


/**
 * This calculator exports CSV data that is relevant for various ML applications.
 *
 * @author Mark Geiger <MarkGeiger@posteo.de>
 */
public class MLDataCalc extends ACalculator
{
	private static final Logger log = Logger.getLogger(MLDataCalc.class.getName());
	
	private CSVExporter exporter = null;
	
	private BotIDMap<ITrackedBot> botMap = new BotIDMap<>();
	
	@Configurable(defValue = "false")
	private static boolean activateRecording = false;
	
	
	public MLDataCalc()
	{
		// nothing
	}
	
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		if (!activateRecording && exporter == null)
		{
			return;
		} else if (!activateRecording)
		{
			log.info("Closing ML recorder");
			exporter.close();
			botMap = new BotIDMap<>();
			exporter = null;
			return;
		}
		
		if (exporter == null)
		{
			initExporter(baseAiFrame);
		}
		updateBotMap();
		fillAndWriteData(baseAiFrame);
	}
	
	
	private void fillAndWriteData(final BaseAiFrame baseAiFrame)
	{
		List<Number> values = new ArrayList<>();
		values.add(getWFrame().getTimestamp());
		values.add(getNewTacticalField().getGameState().getState().ordinal());
		
		values.add(getWFrame().getBall().getPos().x());
		values.add(getWFrame().getBall().getPos().y());
		values.add(getWFrame().getBall().getVel().x());
		values.add(getWFrame().getBall().getVel().y());
		for (ITrackedBot bot : botMap.values())
		{
			values.add(bot.getPos().x());
			values.add(bot.getPos().y());
			values.add(bot.getOrientation());
			
			// destinations
			RobotInfo oldRobotInfo = baseAiFrame.getPrevFrame().getWorldFrame().getBot(bot.getBotId()).getRobotInfo();
			Optional<ITrajectory<IVector3>> traj = oldRobotInfo.getTrajectory();
			if (traj.isPresent())
			{
				values.add(traj.get().getFinalDestination().x());
				values.add(traj.get().getFinalDestination().y());
				values.add(traj.get().getFinalDestination().z());
			} else
			{
				values.add(bot.getPos().x());
				values.add(bot.getPos().y());
				values.add(bot.getOrientation());
			}
			
			// kicker armed
			values.add(oldRobotInfo.isArmed() ? 1 : 0);
			values.add(oldRobotInfo.getKickSpeed());
		}
		
		int goalYellow = 0;
		int goalBlue = 0;
		if (getAiFrame().isNewRefereeMsg())
		{
			if (getAiFrame().getRefereeMsg().getCommand() == Referee.SSL_Referee.Command.GOAL_YELLOW)
			{
				goalYellow = 1;
			} else if (getAiFrame().getRefereeMsg().getCommand() == Referee.SSL_Referee.Command.GOAL_BLUE)
			{
				goalBlue = 1;
			}
		}
		
		values.add(goalBlue);
		values.add(goalYellow);
		
		exporter.addValues(values);
	}
	
	
	private void updateBotMap()
	{
		// update bot Map
		for (ITrackedBot bot : getWFrame().getBots().values())
		{
			if (botMap.containsKey(bot.getBotId()))
			{
				botMap.put(bot.getBotId(), bot);
			}
		}
	}
	
	
	private void initExporter(final BaseAiFrame baseAiFrame)
	{
		String name = "./ml_data_" + getWFrame().getTeamColor().toString();
		log.info("Started recording ML-Data: " + name);
		exporter = new CSVExporter(name, true, true);
		
		List<String> headers = new ArrayList<>();
		headers.add("timestamp");
		headers.add("GameState");
		
		headers.add("Ball_X");
		headers.add("Ball_Y");
		headers.add("Ball_Vel_X");
		headers.add("Ball_Vel_Y");
		
		for (ITrackedBot bot : baseAiFrame.getWorldFrame().getBots().values())
		{
			botMap.put(bot.getBotId(), bot);
			headers.add("X_" + bot.getBotId());
			headers.add("Y_" + bot.getBotId());
			headers.add("W_" + bot.getBotId());
			
			// destinations
			headers.add("D_X_" + bot.getBotId());
			headers.add("D_Y_" + bot.getBotId());
			headers.add("D_W_" + bot.getBotId());
			
			// kicker armed
			headers.add("Kicker_Armed_" + bot.getBotId());
			headers.add("Kick_Speed__" + bot.getBotId());
		}
		headers.add("Goal_B");
		headers.add("Goal_Y");
		exporter.setHeader(headers);
	}
}
