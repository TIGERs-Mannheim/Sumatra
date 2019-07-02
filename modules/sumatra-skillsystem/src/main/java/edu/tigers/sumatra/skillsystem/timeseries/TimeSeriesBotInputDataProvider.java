/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.timeseries;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.Logger;

import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.sumatra.botmanager.bots.ABot;
import edu.tigers.sumatra.botmanager.botskills.BotSkillFastGlobalPosition;
import edu.tigers.sumatra.botmanager.botskills.BotSkillGlobalPosition;
import edu.tigers.sumatra.botmanager.botskills.BotSkillGlobalVelXyPosW;
import edu.tigers.sumatra.botmanager.botskills.BotSkillLocalVelocity;
import edu.tigers.sumatra.data.collector.IExportable;
import edu.tigers.sumatra.data.collector.ITimeSeriesDataProvider;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.skillsystem.ASkillSystem;
import edu.tigers.sumatra.skillsystem.ISkillSystemObserver;


/**
 * Data provider for input to a (real) bot
 */
public class TimeSeriesBotInputDataProvider implements ITimeSeriesDataProvider, ISkillSystemObserver
{
	private static final Logger log = Logger.getLogger(TimeSeriesBotInputDataProvider.class.getName());
	
	private final Map<String, Collection<IExportable>> dataBuffers = new HashMap<>();
	private final Collection<IExportable> botInputs = new ConcurrentLinkedQueue<>();
	
	
	/**
	 * Default constructor
	 */
	public TimeSeriesBotInputDataProvider()
	{
		dataBuffers.put("botInput", botInputs);
	}
	
	
	@Override
	public void stop()
	{
		try
		{
			ASkillSystem ss = SumatraModel.getInstance().getModule(ASkillSystem.class);
			ss.removeObserver(this);
		} catch (ModuleNotFoundException err)
		{
			log.error("SkillSystem module not found.", err);
		}
	}
	
	
	@Override
	public void start()
	{
		try
		{
			ASkillSystem ss = SumatraModel.getInstance().getModule(ASkillSystem.class);
			ss.addObserver(this);
		} catch (ModuleNotFoundException err)
		{
			log.error("SkillSystem module not found.", err);
		}
	}
	
	
	@Override
	public boolean isDone()
	{
		return true;
	}
	
	
	@Override
	public Map<String, Collection<IExportable>> getExportableData()
	{
		return dataBuffers;
	}
	
	
	@Override
	public void onCommandSent(final ABot bot, final long timestamp)
	{
		long tSent = System.nanoTime();
		ExportableBotInput botInput = new ExportableBotInput(bot.getBotId().getNumber(), bot.getColor(), timestamp);
		botInput.settSent(tSent);
		saveTrajectory(bot, timestamp, botInput);
		
		switch (bot.getMatchCtrl().getSkill().getType())
		{
			case GLOBAL_POSITION:
				BotSkillGlobalPosition botSkillGlobalPosition = (BotSkillGlobalPosition) bot.getMatchCtrl().getSkill();
				botInput
						.setSetPos(Vector3.from2d(botSkillGlobalPosition.getPos(), botSkillGlobalPosition.getOrientation()));
				break;
			case LOCAL_VELOCITY:
				BotSkillLocalVelocity botSkillLocalVelocity = (BotSkillLocalVelocity) bot.getMatchCtrl().getSkill();
				botInput.setLocalVel(Vector3.fromXYZ(botSkillLocalVelocity.getX(), botSkillLocalVelocity.getY(),
						botSkillLocalVelocity.getW()));
				break;
			case GLOBAL_VEL_XY_POS_W:
				BotSkillGlobalVelXyPosW botSkillGlobalVelXyPosW = (BotSkillGlobalVelXyPosW) bot.getMatchCtrl().getSkill();
				botInput.setSetPos(Vector3.fromXYZ(0, 0, botSkillGlobalVelXyPosW.getTargetAngle()));
				botInput.setSetVel(Vector3.from2d(botSkillGlobalVelXyPosW.getVel(), 0));
				break;
			case FAST_GLOBAL_POSITION:
				BotSkillFastGlobalPosition skill = (BotSkillFastGlobalPosition) bot.getMatchCtrl().getSkill();
				botInput.setSetPos(Vector3.fromXYZ(0, 0, skill.getOrientation()));
				break;
			case BOT_SKILL_SINE:
			case GLOBAL_VELOCITY:
			case MOTORS_OFF:
			case WHEEL_VELOCITY:
			default:
				break;
		}
		
		botInput.setKickSpeed(bot.getMatchCtrl().getSkill().getKickSpeed());
		botInput.setKickDevice(bot.getMatchCtrl().getSkill().getDevice().getValue());
		botInput.setDribbleRpm(bot.getMatchCtrl().getSkill().getDribbleSpeed());
		botInput.setKickMode(bot.getMatchCtrl().getSkill().getMode().getId());
		botInput.setMoveConstraints(bot.getMatchCtrl().getSkill().getMoveConstraints());
		
		botInputs.add(botInput);
	}
	
	
	private void saveTrajectory(final ABot bot, final long timestamp, final ExportableBotInput cd)
	{
		if (bot.getCurrentTrajectory().isPresent())
		{
			cd.setTrajPos(bot.getCurrentTrajectory().get().getPositionMM(timestamp));
			cd.setTrajVel(bot.getCurrentTrajectory().get().getVelocity(timestamp));
			
			if (bot.getColor() != Geometry.getNegativeHalfTeam())
			{
				cd.setTrajPos(cd.getTrajPos().multiplyNew(-1));
				cd.setTrajVel(cd.getTrajVel().multiplyNew(-1));
			}
		}
	}
}
