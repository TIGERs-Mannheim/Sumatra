/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.sumatra.botmanager.bots.ABot;
import edu.tigers.sumatra.botmanager.commands.botskills.BotSkillFastGlobalPosition;
import edu.tigers.sumatra.botmanager.commands.botskills.BotSkillGlobalPosition;
import edu.tigers.sumatra.botmanager.commands.botskills.BotSkillGlobalVelXyPosW;
import edu.tigers.sumatra.botmanager.commands.botskills.BotSkillLocalVelocity;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.AVector3;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.referee.TeamConfig;
import edu.tigers.sumatra.wp.data.ExtendedCamDetectionFrame;
import edu.tigers.sumatra.wp.util.ExportDataContainer;
import edu.tigers.sumatra.wp.util.ExportDataContainer.SkillBot;
import edu.tigers.sumatra.wp.util.IBallWatcherObserver;
import edu.tigers.sumatra.wp.util.VisionWatcher;


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
	
	
	private void saveTrajectory(final ABot bot, final long timestamp, final SkillBot cd)
	{
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
	}
	
	
	@Override
	public void onCommandSent(final ABot bot, final long timestamp)
	{
		SkillBot cd = new SkillBot(bot.getBotId().getNumber(), bot.getColor(), timestamp);
		saveTrajectory(bot, timestamp, cd);
		
		switch (bot.getMatchCtrl().getSkill().getType())
		{
			case GLOBAL_POSITION:
				BotSkillGlobalPosition botSkillGlobalPosition = (BotSkillGlobalPosition) bot.getMatchCtrl().getSkill();
				cd.setSetPos(Vector3.from2d(botSkillGlobalPosition.getPos(), botSkillGlobalPosition.getOrientation()));
				break;
			case LOCAL_VELOCITY:
				BotSkillLocalVelocity botSkillLocalVelocity = (BotSkillLocalVelocity) bot.getMatchCtrl().getSkill();
				cd.setLocalVel(Vector3.fromXYZ(botSkillLocalVelocity.getX(), botSkillLocalVelocity.getY(),
						botSkillLocalVelocity.getW()));
				break;
			case GLOBAL_VEL_XY_POS_W:
				BotSkillGlobalVelXyPosW botSkillGlobalVelXyPosW = (BotSkillGlobalVelXyPosW) bot.getMatchCtrl().getSkill();
				cd.setSetPos(Vector3.fromXYZ(0, 0, botSkillGlobalVelXyPosW.getTargetAngle()));
				cd.setSetVel(Vector3.from2d(botSkillGlobalVelXyPosW.getVel(), 0));
				break;
			case FAST_GLOBAL_POSITION:
				BotSkillFastGlobalPosition skill = (BotSkillFastGlobalPosition) bot.getMatchCtrl().getSkill();
				cd.setSetPos(Vector3.fromXYZ(0, 0, skill.getOrientation()));
				break;
			case BOT_SKILL_SINE:
			case GLOBAL_VELOCITY:
			case MOTORS_OFF:
			case WHEEL_VELOCITY:
			default:
				break;
		}
		
		
		cd.setSetVel(AVector3.ZERO_VECTOR);
		data.put(bot.getBotId(), cd);
	}
	
	
	@Override
	public void onAddCustomData(final ExportDataContainer container, final ExtendedCamDetectionFrame frame)
	{
		for (SkillBot skillBot : data.values())
		{
			container.getSkillBots().add(skillBot);
		}
	}
}
