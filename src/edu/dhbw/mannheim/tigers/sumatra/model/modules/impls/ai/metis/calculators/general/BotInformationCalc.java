/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 12, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.general;

import java.util.Map;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.BaseAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.BotAiInformation;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.DrawablePath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.TacticalField;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.IBotIDMap;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.ACalculator;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.move.MovementCon;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.ABot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeatureState;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ISkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.MoveToSkill;


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
		IBotIDMap<TrackedTigerBot> bots = wFrame.tigerBotsAvailable;
		for (TrackedTigerBot bot : bots.values())
		{
			BotID botId = bot.getId();
			BotAiInformation aiInfo = new BotAiInformation();
			
			aiInfo.setBallContact(bot.hasBallContact());
			aiInfo.setVel(bot.getVel());
			aiInfo.setPos(bot.getPos());
			float preVel = 0;
			if (baseAiFrame.getPrevFrame() != null)
			{
				BotAiInformation preAiInfo = baseAiFrame.getPrevFrame().getTacticalField().getBotAiInformation().get(botId);
				if (preAiInfo != null)
				{
					preVel = preAiInfo.getMaxVel();
				}
			}
			aiInfo.setMaxVel(Math.max(bot.getVel().getLength2(), preVel));
			
			DrawablePath path = baseAiFrame.getPrevFrame().getAresData().getLatestPaths().get(botId);
			aiInfo.setPathPlanning((path != null) && (path.getPath() != null));
			aiInfo.setNumPaths(baseAiFrame.getPrevFrame().getAresData().getNumberOfPaths(botId));
			
			ABot aBot = bot.getBot();
			if (aBot != null)
			{
				aiInfo.setBattery(aBot.getBatteryLevel());
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
			}
			
			play: for (APlay play : baseAiFrame.getPrevFrame().getPlayStrategy().getActivePlays())
			{
				for (ARole role : play.getRoles())
				{
					if (role.getBotID().equals(botId))
					{
						aiInfo.setPlay(play.getType().name());
						aiInfo.setRole(role.getType().name());
						aiInfo.setSkill(String.valueOf(baseAiFrame.getPrevFrame().getAresData().getSkills()
								.get(role.getBotID())));
						aiInfo.setRoleState(role.getCurrentState().name());
						
						ISkill moveSkill = role.getCurrentSkill();
						if (moveSkill instanceof MoveToSkill)
						{
							MovementCon moveCon = ((MoveToSkill) moveSkill).getMoveCon();
							aiInfo.addCondition(moveCon.getDestCon().getCondition());
							aiInfo.addCondition(moveCon.getAngleCon().getCondition());
						}
						
						break play;
					}
				}
			}
			
			newTacticalField.getBotAiInformation().put(botId, aiInfo);
		}
	}
}
