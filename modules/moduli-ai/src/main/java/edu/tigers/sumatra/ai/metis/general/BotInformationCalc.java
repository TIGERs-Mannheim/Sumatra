/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 12, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.metis.general;

import java.util.Map;

import edu.tigers.sumatra.ai.data.BotAiInformation;
import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.pandora.plays.APlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.bot.EFeature;
import edu.tigers.sumatra.bot.EFeatureState;
import edu.tigers.sumatra.bot.IBot;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.IBotIDMap;
import edu.tigers.sumatra.skillsystem.driver.IPathDriver;
import edu.tigers.sumatra.statemachine.IState;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrame;


/**
 * Calculates (or collects) information about bots
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BotInformationCalc extends ACalculator
{
	/**
	  * 
	  */
	public BotInformationCalc()
	{
	}
	
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		WorldFrame wFrame = baseAiFrame.getWorldFrame();
		IBotIDMap<ITrackedBot> bots = wFrame.getTigerBotsVisible();
		for (ITrackedBot bot : bots.values())
		{
			BotID botId = bot.getBotId();
			BotAiInformation aiInfo = new BotAiInformation();
			
			aiInfo.setBallContact(bot.hasBallContact());
			aiInfo.setVel(bot.getVel());
			aiInfo.setPos(bot.getPos());
			double preVel = 0;
			if (baseAiFrame.getPrevFrame() != null)
			{
				BotAiInformation preAiInfo = baseAiFrame.getPrevFrame().getTacticalField().getBotAiInformation().get(botId);
				if (preAiInfo != null)
				{
					preVel = preAiInfo.getMaxVel();
				}
			}
			aiInfo.setMaxVel(Math.max(bot.getVel().getLength2(), preVel));
			
			IBot aBot = bot.getBot();
			if (aBot != null)
			{
				aiInfo.setBattery(aBot.getBatteryRelative());
				aiInfo.setKickerCharge(aBot.getKickerLevel());
				String brokenFeatures = "BroFeat: ";
				for (Map.Entry<EFeature, EFeatureState> entry : aBot.getBotFeatures().entrySet())
				{
					if (entry.getValue().equals(EFeatureState.KAPUT))
					{
						brokenFeatures += " " + entry.getKey();
					}
				}
				aiInfo.setBrokenFeatures(brokenFeatures);
				aiInfo.setLimits(aBot.getMoveConstraints().toString());
				aiInfo.setDribbleSpeed(aBot.getDribblerSpeed());
				aiInfo.setKickSpeed(aBot.getKickSpeed());
				aiInfo.setDevice(aBot.getDevice());
			}
			
			play: for (APlay play : baseAiFrame.getPrevFrame().getPlayStrategy().getActivePlays())
			{
				for (ARole role : play.getRoles())
				{
					if (role.getBotID().equals(botId))
					{
						aiInfo.setPlay(play.getType().name());
						aiInfo.setRole(role.getType().name());
						aiInfo.setSkill(role.getCurrentSkill().getType().name());
						aiInfo.setRoleState(role.getCurrentState().name());
						IState skillState = role.getCurrentSkill().getCurrentState();
						aiInfo.setSkillState(skillState == null ? "" : skillState.getName());
						IPathDriver driver = role.getCurrentSkill().getPathDriver();
						aiInfo.setSkillDriver(driver == null ? "" : driver.getType().name());
						aiInfo.setLimits(role.getCurrentSkill().getMoveCon().getMoveConstraints().toString());
						break play;
					}
				}
			}
			
			newTacticalField.getBotAiInformation().put(botId, aiInfo);
		}
	}
}
