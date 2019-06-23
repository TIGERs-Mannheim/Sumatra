/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Feb 2, 2016
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.skillsystem;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.sumatra.botmanager.bots.ABot;
import edu.tigers.sumatra.botmanager.commands.botskills.BotSkillGlobalPosition;
import edu.tigers.sumatra.botmanager.commands.botskills.BotSkillGlobalVelXyPosW;
import edu.tigers.sumatra.botmanager.commands.botskills.BotSkillLocalVelocity;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.Vector3;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.referee.TeamConfig;
import edu.tigers.sumatra.wp.ExportDataContainer;
import edu.tigers.sumatra.wp.ExportDataContainer.SkillBot;
import edu.tigers.sumatra.wp.IBallWatcherObserver;
import edu.tigers.sumatra.wp.VisionWatcher;
import edu.tigers.sumatra.wp.data.ExtendedCamDetectionFrame;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class VisionSkillWatcher extends VisionWatcher implements ISkillSystemObserver, IBallWatcherObserver
{
	@SuppressWarnings("unused")
	private static final Logger			log	= Logger.getLogger(VisionSkillWatcher.class.getName());
	
	private final Map<BotID, SkillBot>	data	= new ConcurrentHashMap<>();
	
	
	/**
	 * @param fileName
	 */
	public VisionSkillWatcher(final String fileName)
	{
		super(fileName);
		addObserver(this);
	}
	
	
	@Override
	public boolean start()
	{
		try
		{
			ASkillSystem ss = (ASkillSystem) SumatraModel.getInstance().getModule(ASkillSystem.MODULE_ID);
			ss.addObserver(this);
		} catch (ModuleNotFoundException err)
		{
			log.error("SkillSystem module not found.", err);
			return false;
		}
		
		return super.start();
	}
	
	
	@Override
	public void stop()
	{
		try
		{
			ASkillSystem ss = (ASkillSystem) SumatraModel.getInstance().getModule(ASkillSystem.MODULE_ID);
			ss.removeObserver(this);
		} catch (ModuleNotFoundException err)
		{
			log.error("SkillSystem module not found.", err);
		}
		
		super.stop();
	}
	
	
	@Override
	public void onCommandSent(final ABot bot, final long timestamp)
	{
		SkillBot cd = new SkillBot(bot.getBotId().getNumber(), bot.getColor(), timestamp);
		if (bot.getCurrentTrajectory().isPresent())
		{
			cd.setTrajPos(bot.getCurrentTrajectory().get().getPositionMM(timestamp));
			cd.setTrajVel(bot.getCurrentTrajectory().get().getVelocity(timestamp));
			
			if (bot.getColor() != TeamConfig.getLeftTeam())
			{
				cd.setTrajPos(cd.getTrajPos().multiplyNew(-1));
				cd.setTrajVel(cd.getTrajVel().multiplyNew(-1));
			}
		}
		
		switch (bot.getMatchCtrl().getSkill().getType())
		{
			case BOT_SKILL_SINE:
			case GLOBAL_VELOCITY:
			case MOTORS_OFF:
			case WHEEL_VELOCITY:
				break;
			case GLOBAL_POSITION:
			{
				BotSkillGlobalPosition skill = (BotSkillGlobalPosition) bot.getMatchCtrl().getSkill();
				cd.setSetPos(new Vector3(skill.getPos(), skill.getOrientation()));
			}
				break;
			case LOCAL_VELOCITY:
			{
				BotSkillLocalVelocity skill = (BotSkillLocalVelocity) bot.getMatchCtrl().getSkill();
				cd.setLocalVel(new Vector3(skill.getX(), skill.getY(), skill.getW()));
			}
				break;
			case GLOBAL_VEL_XY_POS_W:
			{
				BotSkillGlobalVelXyPosW skill = (BotSkillGlobalVelXyPosW) bot.getMatchCtrl().getSkill();
				cd.setSetPos(new Vector3(0, 0, skill.getTargetAngle()));
				cd.setSetVel(new Vector3(skill.getVel(), 0));
				break;
			}
		}
		
		
		cd.setSetVel(bot.getGlobalTargetVelocity(timestamp));
		data.put(bot.getBotId(), cd);
	}
	
	
	@Override
	public void beforeExport(final Map<String, Object> jsonMapping)
	{
	}
	
	
	@Override
	public void onAddCustomData(final ExportDataContainer container, final ExtendedCamDetectionFrame frame)
	{
		for (SkillBot skillBot : data.values())
		{
			container.getSkillBots().add(skillBot);
		}
		
	}
	
	
	@Override
	public void postProcessing(final String fileName)
	{
	}
}
