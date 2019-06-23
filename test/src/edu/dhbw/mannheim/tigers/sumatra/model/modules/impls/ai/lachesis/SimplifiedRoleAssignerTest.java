/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 14, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.lachesis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AthenaAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ITacticalField;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotIDMap;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.TeamConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.DummyBot;
import edu.dhbw.mannheim.tigers.sumatra.util.FrameFactory;
import edu.dhbw.mannheim.tigers.sumatra.util.SumatraSetupHelper;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class SimplifiedRoleAssignerTest
{
	private static final Logger				log;
	private final FrameFactory					frameFactory	= new FrameFactory();
	private final SimplifiedRoleAssigner	roleAssigner	= new SimplifiedRoleAssigner();
	
	
	static
	{
		SumatraSetupHelper.setupSumatra();
		SumatraSetupHelper.changeLogLevel(Level.DEBUG);
		log = Logger.getLogger(SimplifiedRoleAssignerTest.class.getName());
	}
	
	
	private AIInfoFrame generateAiFrame()
	{
		TeamConfig.setKeeperIdBlue(0);
		TeamConfig.setKeeperIdYellow(0);
		AIInfoFrame frame = frameFactory.createFullAiInfoFrame();
		ITacticalField tacticalField = frame.getTacticalField();
		
		RoleFinderInfo keeperInfo = new RoleFinderInfo(1, 1, 1);
		keeperInfo.getDesiredBots().add(frame.getKeeperId());
		tacticalField.getRoleFinderInfos().put(EPlay.KEEPER, keeperInfo);
		
		RoleFinderInfo defenderInfo = new RoleFinderInfo(0, 3, 2);
		tacticalField.getRoleFinderInfos().put(EPlay.DEFENSIVE, defenderInfo);
		
		RoleFinderInfo offenceInfo = new RoleFinderInfo(0, 1, 1);
		tacticalField.getRoleFinderInfos().put(EPlay.OFFENSIVE, offenceInfo);
		
		RoleFinderInfo supporterInfo = new RoleFinderInfo(0, 3, 2);
		tacticalField.getRoleFinderInfos().put(EPlay.SUPPORT, supporterInfo);
		
		return frame;
	}
	
	
	private void checkRoles(final AthenaAiFrame frame, final BotIDMap<TrackedTigerBot> assignees)
	{
		List<APlay> plays = frame.getPlayStrategy().getActivePlays();
		final Map<EPlay, RoleFinderInfo> roleFinderInfos = frame.getTacticalField().getRoleFinderInfos();
		
		Map<BotID, ARole> assignedBots = new LinkedHashMap<BotID, ARole>();
		Map<EPlay, Integer> numRolesPerPlay = new HashMap<EPlay, Integer>();
		for (APlay play : plays)
		{
			numRolesPerPlay.putIfAbsent(play.getType(), 0);
			for (ARole role : play.getRoles())
			{
				Assert.assertTrue("Role has no assigned bot: " + role.getType(), role.getBotID().isBot());
				Assert.assertFalse("BotID already assigned: " + role.getBotID() + " - " + assignedBots,
						assignedBots.keySet().contains(role.getBotID()));
				assignedBots.put(role.getBotID(), role);
				numRolesPerPlay.put(play.getType(), numRolesPerPlay.get(play.getType()) + 1);
			}
		}
		log.trace(assignedBots);
		Assert.assertEquals("Number of assigned bots and assignees not equal", assignees.size(), assignedBots.size());
		
		for (Map.Entry<EPlay, Integer> entry : numRolesPerPlay.entrySet())
		{
			RoleFinderInfo info = roleFinderInfos.get(entry.getKey());
			Assert.assertNotNull(info);
			Assert.assertTrue("Less than minRoles roles assigned for " + entry.getKey() + " " + entry.getValue() + "<"
					+ info.getMinRoles(),
					entry.getValue() >= info.getMinRoles());
			Assert.assertTrue("More than maxRoles roles assigned for " + entry.getKey() + " " + entry.getValue() + ">"
					+ info.getMaxRoles(),
					entry.getValue() <= info.getMaxRoles());
		}
	}
	
	
	private boolean checkDesiredBots(final AthenaAiFrame frame)
	{
		List<APlay> plays = frame.getPlayStrategy().getActivePlays();
		final Map<EPlay, RoleFinderInfo> roleFinderInfos = frame.getTacticalField().getRoleFinderInfos();
		for (APlay play : plays)
		{
			List<BotID> bots = new ArrayList<BotID>();
			for (ARole role : play.getRoles())
			{
				bots.add(role.getBotID());
			}
			for (BotID botId : roleFinderInfos.get(play.getType()).getDesiredBots())
			{
				if (!bots.contains(botId))
				{
					log.debug("Desired bot " + botId + " is not assigned to " + play.getType());
					log.debug("Contains: " + bots);
					return false;
				}
			}
		}
		return true;
	}
	
	
	private void runRoleAssigner(final AIInfoFrame frame, final BotIDMap<TrackedTigerBot> assignees)
	{
		roleAssigner.assignRoles(assignees, frame.getPlayStrategy().getActivePlays(), frame);
	}
	
	
	private void runFullTest(final AIInfoFrame frame)
	{
		final BotIDMap<TrackedTigerBot> assignees = new BotIDMap<TrackedTigerBot>(
				frame.getWorldFrame().getTigerBotsAvailable());
		runRoleAssigner(frame, assignees);
		checkRoles(frame, assignees);
	}
	
	
	private void fillPlays(final AIInfoFrame frame)
	{
		for (APlay play : frame.getPlayStrategy().getActivePlays())
		{
			play.removeRoles(play.getRoles().size(), frame);
			switch (play.getType())
			{
				case OFFENSIVE:
				case KEEPER:
					play.addRoles(1, frame);
					break;
				case SUPPORT:
				case DEFENSIVE:
					play.addRoles(2, frame);
					break;
				default:
					throw new IllegalStateException();
			}
		}
	}
	
	
	private void preAssign(final AIInfoFrame frame)
	{
		fillPlays(frame);
		final List<BotID> assignees = new ArrayList<BotID>(
				frame.getWorldFrame().getTigerBotsAvailable().keySet());
		for (APlay play : frame.getPlayStrategy().getActivePlays())
		{
			for (ARole role : play.getRoles())
			{
				role.assignBotID(assignees.remove(0), frame);
			}
		}
	}
	
	
	private void switchRoles(final AIInfoFrame frame, final ARole role, final BotID newBotId)
	{
		ARole oldRole = frame.getPlayStrategy().getActiveRoles().get(newBotId);
		oldRole.assignBotID(role.getBotID(), frame);
		role.assignBotID(newBotId, frame);
	}
	
	
	private void assignRoles(final AIInfoFrame frame, final EPlay playType, final BotID... botIds)
	{
		for (APlay play : frame.getPlayStrategy().getActivePlays())
		{
			if (play.getType() == playType)
			{
				List<ARole> roles = new ArrayList<ARole>(play.getRoles());
				for (BotID botId : botIds)
				{
					if (roles.isEmpty())
					{
						throw new IllegalStateException("Less roles than botIds to be assigned");
					}
					switchRoles(frame, roles.remove(0), botId);
				}
				break;
			}
		}
	}
	
	
	/**
	 */
	@Test
	public void testRoleBalancing()
	{
		AIInfoFrame frame = generateAiFrame();
		runFullTest(frame);
	}
	
	
	/**
	 */
	@Test
	public void testDesiredBots()
	{
		AIInfoFrame frame = generateAiFrame();
		frame.getTacticalField().getRoleFinderInfos().get(EPlay.OFFENSIVE).getDesiredBots()
				.add(BotID.createBotId(1, frame.getTeamColor()));
		frame.getTacticalField().getRoleFinderInfos().get(EPlay.SUPPORT).getDesiredBots()
				.add(BotID.createBotId(2, frame.getTeamColor()));
		
		runFullTest(frame);
		Assert.assertTrue(checkDesiredBots(frame));
	}
	
	
	/**
	 */
	@Test
	public void testPreAssigment()
	{
		AIInfoFrame frame = generateAiFrame();
		frame.getTacticalField().getRoleFinderInfos().get(EPlay.OFFENSIVE).getDesiredBots()
				.add(BotID.createBotId(1, frame.getTeamColor()));
		frame.getTacticalField().getRoleFinderInfos().get(EPlay.SUPPORT).getDesiredBots()
				.add(BotID.createBotId(2, frame.getTeamColor()));
		preAssign(frame);
		assignRoles(frame, EPlay.OFFENSIVE, BotID.createBotId(1, frame.getTeamColor()));
		assignRoles(frame, EPlay.SUPPORT, BotID.createBotId(2, frame.getTeamColor()));
		runFullTest(frame);
		Assert.assertTrue(checkDesiredBots(frame));
	}
	
	
	/**
	 */
	@Test
	public void testNewDesiredBot()
	{
		AIInfoFrame frame = generateAiFrame();
		frame.getTacticalField().getRoleFinderInfos().get(EPlay.OFFENSIVE).getDesiredBots()
				.add(BotID.createBotId(1, frame.getTeamColor()));
		frame.getTacticalField().getRoleFinderInfos().get(EPlay.SUPPORT).getDesiredBots()
				.add(BotID.createBotId(2, frame.getTeamColor()));
		frame.getTacticalField().getRoleFinderInfos().get(EPlay.SUPPORT).getDesiredBots()
				.add(BotID.createBotId(3, frame.getTeamColor()));
		preAssign(frame);
		assignRoles(frame, EPlay.OFFENSIVE, BotID.createBotId(1, frame.getTeamColor()));
		assignRoles(frame, EPlay.SUPPORT, BotID.createBotId(2, frame.getTeamColor()),
				BotID.createBotId(4, frame.getTeamColor()));
		runFullTest(frame);
		Assert.assertTrue(checkDesiredBots(frame));
	}
	
	
	/**
	 */
	@Test
	public void testDuplicateDesiredBots()
	{
		AIInfoFrame frame = generateAiFrame();
		frame.getTacticalField().getRoleFinderInfos().get(EPlay.OFFENSIVE).getDesiredBots()
				.add(BotID.createBotId(1, frame.getTeamColor()));
		frame.getTacticalField().getRoleFinderInfos().get(EPlay.SUPPORT).getDesiredBots()
				.add(BotID.createBotId(1, frame.getTeamColor()));
		runFullTest(frame);
		Assert.assertFalse(checkDesiredBots(frame));
	}
	
	
	/**
	 */
	@Test
	public void testVanishedBots()
	{
		AIInfoFrame frame = generateAiFrame();
		preAssign(frame);
		final BotIDMap<TrackedTigerBot> assignees = new BotIDMap<TrackedTigerBot>(
				frame.getWorldFrame().getTigerBotsAvailable());
		assignees.remove(BotID.createBotId(1, frame.getTeamColor()));
		runRoleAssigner(frame, assignees);
		checkRoles(frame, assignees);
	}
	
	
	/**
	 */
	@Test
	public void testKickerDischargedAssigned()
	{
		AIInfoFrame frame = generateAiFrame();
		preAssign(frame);
		DummyBot bot = (DummyBot) frame.getWorldFrame().getTiger(BotID.createBotId(1, frame.getTeamColor())).getBot();
		bot.setCurrentCharge(50);
		frame.getTacticalField().getRoleFinderInfos().get(EPlay.OFFENSIVE).getDesiredBots()
				.add(BotID.createBotId(1, frame.getTeamColor()));
		assignRoles(frame, EPlay.OFFENSIVE, BotID.createBotId(1, frame.getTeamColor()));
		runFullTest(frame);
		Assert.assertTrue(checkDesiredBots(frame));
	}
	
	
	/**
	 */
	@Test
	public void testKickerUncharged()
	{
		AIInfoFrame frame = generateAiFrame();
		preAssign(frame);
		DummyBot bot = (DummyBot) frame.getWorldFrame().getTiger(BotID.createBotId(1, frame.getTeamColor())).getBot();
		bot.setCurrentCharge(10);
		frame.getTacticalField().getRoleFinderInfos().get(EPlay.OFFENSIVE).getDesiredBots()
				.add(BotID.createBotId(1, frame.getTeamColor()));
		assignRoles(frame, EPlay.OFFENSIVE, BotID.createBotId(2, frame.getTeamColor()));
		runFullTest(frame);
		Assert.assertFalse(checkDesiredBots(frame));
	}
	
	
	/**
	 */
	@Test
	public void testMoreDesiredBotsThanRoles()
	{
		AIInfoFrame frame = generateAiFrame();
		preAssign(frame);
		RoleFinderInfo offenceInfo = new RoleFinderInfo(1, 1, 1);
		frame.getTacticalField().getRoleFinderInfos().put(EPlay.OFFENSIVE, offenceInfo);
		frame.getTacticalField().getRoleFinderInfos().get(EPlay.OFFENSIVE).getDesiredBots()
				.add(BotID.createBotId(1, frame.getTeamColor()));
		frame.getTacticalField().getRoleFinderInfos().get(EPlay.OFFENSIVE).getDesiredBots()
				.add(BotID.createBotId(2, frame.getTeamColor()));
		assignRoles(frame, EPlay.OFFENSIVE, BotID.createBotId(3, frame.getTeamColor()));
		runFullTest(frame);
		for (APlay play : frame.getPlayStrategy().getActivePlays())
		{
			if (play.getType() == EPlay.OFFENSIVE)
			{
				Assert.assertTrue("Offensive Play must not contain any roles", play.getRoles().size() == 1);
				Assert.assertTrue("Wrong desired bot assigned",
						play.getRoles().get(0).getBotID().equals(BotID.createBotId(1, frame.getTeamColor())));
			}
		}
	}
	
	
	/**
	 */
	@Test
	public void testDesiredBotsButNoRoles()
	{
		AIInfoFrame frame = generateAiFrame();
		preAssign(frame);
		RoleFinderInfo offenceInfo = new RoleFinderInfo(0, 0, 0);
		frame.getTacticalField().getRoleFinderInfos().put(EPlay.OFFENSIVE, offenceInfo);
		frame.getTacticalField().getRoleFinderInfos().get(EPlay.OFFENSIVE).getDesiredBots()
				.add(BotID.createBotId(1, frame.getTeamColor()));
		frame.getTacticalField().getRoleFinderInfos().get(EPlay.OFFENSIVE).getDesiredBots()
				.add(BotID.createBotId(2, frame.getTeamColor()));
		assignRoles(frame, EPlay.OFFENSIVE, BotID.createBotId(3, frame.getTeamColor()));
		runFullTest(frame);
		for (APlay play : frame.getPlayStrategy().getActivePlays())
		{
			if (play.getType() == EPlay.OFFENSIVE)
			{
				Assert.assertTrue("Offensive Play must not contain any roles", play.getRoles().isEmpty());
			}
		}
	}
}
