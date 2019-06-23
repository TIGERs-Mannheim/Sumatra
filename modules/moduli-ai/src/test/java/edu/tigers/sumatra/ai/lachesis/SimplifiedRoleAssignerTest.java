/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 14, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.lachesis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.tigers.sumatra.ai.FrameFactory;
import edu.tigers.sumatra.ai.data.ITacticalField;
import edu.tigers.sumatra.ai.data.frames.AIInfoFrame;
import edu.tigers.sumatra.ai.data.frames.AthenaAiFrame;
import edu.tigers.sumatra.ai.pandora.plays.APlay;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.BotIDMap;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.referee.TeamConfig;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class SimplifiedRoleAssignerTest
{
	private static final Logger	log;
	private static FrameFactory	frameFactory;
	
	
	static
	{
		SumatraModel.changeLogLevel(Level.DEBUG);
		log = Logger.getLogger(SimplifiedRoleAssignerTest.class.getName());
		TeamConfig.setKeeperIdBlue(0);
		TeamConfig.setKeeperIdYellow(0);
	}
	
	
	private AIInfoFrame generateAiFrame()
	{
		AIInfoFrame frame = frameFactory.createFullAiInfoFrame(ETeamColor.YELLOW, 0, 0);
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
	
	
	private void checkRoles(final AthenaAiFrame frame, final BotIDMap<ITrackedBot> assignees)
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
	
	
	private void runRoleAssigner(final AIInfoFrame frame, final BotIDMap<ITrackedBot> assignees)
	{
		SimplifiedRoleAssigner roleAssigner = new SimplifiedRoleAssigner();
		roleAssigner.assignRoles(assignees, frame.getPlayStrategy().getActivePlays(), frame);
	}
	
	
	private void runFullTest(final AIInfoFrame frame)
	{
		final BotIDMap<ITrackedBot> assignees = new BotIDMap<ITrackedBot>(
				frame.getWorldFrame().getTigerBotsAvailable());
		runRoleAssigner(frame, assignees);
		checkRoles(frame, assignees);
	}
	
	
	private void fillPlays(final AIInfoFrame frame, final int keeper, final int off, final int def, final int sup)
	{
		for (APlay play : frame.getPlayStrategy().getActivePlays())
		{
			play.removeRoles(play.getRoles().size(), frame);
			switch (play.getType())
			{
				case OFFENSIVE:
					play.addRoles(off, frame);
					break;
				case KEEPER:
					play.addRoles(keeper, frame);
					break;
				case SUPPORT:
					play.addRoles(sup, frame);
					break;
				case DEFENSIVE:
					play.addRoles(def, frame);
					break;
				default:
					throw new IllegalStateException();
			}
		}
	}
	
	
	private void preAssign(final AIInfoFrame frame)
	{
		preAssign(frame, 1, 1, 2, 2);
	}
	
	
	private void preAssign(final AIInfoFrame frame, final int keeper, final int off, final int def, final int sup)
	{
		fillPlays(frame, keeper, off, def, sup);
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
		final BotIDMap<ITrackedBot> assignees = new BotIDMap<ITrackedBot>(
				frame.getWorldFrame().getTigerBotsAvailable());
		assignees.remove(BotID.createBotId(1, frame.getTeamColor()));
		runRoleAssigner(frame, assignees);
		checkRoles(frame, assignees);
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
	
	
	/**
	 */
	@Test
	public void test1()
	{
		TeamConfig.setKeeperIdBlue(1);
		TeamConfig.setKeeperIdYellow(1);
		AIInfoFrame frame = generateAiFrame();
		preAssign(frame, 1, 1, 3, 1);
		
		assignRoles(frame, EPlay.KEEPER, BotID.createBotId(1, frame.getTeamColor()));
		assignRoles(frame, EPlay.OFFENSIVE, BotID.createBotId(4, frame.getTeamColor()));
		assignRoles(frame, EPlay.DEFENSIVE, BotID.createBotId(2, frame.getTeamColor()));
		assignRoles(frame, EPlay.DEFENSIVE, BotID.createBotId(0, frame.getTeamColor()));
		assignRoles(frame, EPlay.DEFENSIVE, BotID.createBotId(5, frame.getTeamColor()));
		assignRoles(frame, EPlay.SUPPORT, BotID.createBotId(3, frame.getTeamColor()));
		
		printAssignment(frame);
		
		// 6 -> 3
		// 7 -> 5
		RoleFinderInfo keeperInfo = new RoleFinderInfo(1, 1, 1);
		frame.getTacticalField().getRoleFinderInfos().put(EPlay.KEEPER, keeperInfo);
		frame.getTacticalField().getRoleFinderInfos().get(EPlay.KEEPER).getDesiredBots()
				.add(BotID.createBotId(1, frame.getTeamColor()));
		
		RoleFinderInfo offenceInfo = new RoleFinderInfo(1, 1, 1);
		frame.getTacticalField().getRoleFinderInfos().put(EPlay.OFFENSIVE, offenceInfo);
		
		RoleFinderInfo defenseInfo = new RoleFinderInfo(0, 3, 3);
		frame.getTacticalField().getRoleFinderInfos().put(EPlay.DEFENSIVE, defenseInfo);
		frame.getTacticalField().getRoleFinderInfos().get(EPlay.DEFENSIVE).getDesiredBots()
				.add(BotID.createBotId(4, frame.getTeamColor()));
		frame.getTacticalField().getRoleFinderInfos().get(EPlay.DEFENSIVE).getDesiredBots()
				.add(BotID.createBotId(3, frame.getTeamColor()));
		frame.getTacticalField().getRoleFinderInfos().get(EPlay.DEFENSIVE).getDesiredBots()
				.add(BotID.createBotId(2, frame.getTeamColor()));
		frame.getTacticalField().getRoleFinderInfos().get(EPlay.DEFENSIVE).getDesiredBots()
				.add(BotID.createBotId(0, frame.getTeamColor()));
		frame.getTacticalField().getRoleFinderInfos().get(EPlay.DEFENSIVE).getDesiredBots()
				.add(BotID.createBotId(5, frame.getTeamColor()));
		
		RoleFinderInfo supportInfo = new RoleFinderInfo(0, 6, 2);
		frame.getTacticalField().getRoleFinderInfos().put(EPlay.SUPPORT, supportInfo);
		
		runFullTest(frame);
		
		printAssignment(frame);
		
		// Assert.assertTrue(containsBotId(frame, ERole.OFFENSIVE, 5));
		Assert.assertTrue(containsBotId(frame, ERole.DEFENDER, 4));
		Assert.assertTrue(containsBotId(frame, ERole.DEFENDER, 3));
		Assert.assertTrue(containsBotId(frame, ERole.DEFENDER, 2));
		// Assert.assertTrue(containsBotId(frame, ERole.SUPPORT, 0));
		Assert.assertTrue(containsBotId(frame, ERole.KEEPER, 1));
	}
	
	
	/**
	 */
	@Test
	public void test2()
	{
		TeamConfig.setKeeperIdBlue(0);
		TeamConfig.setKeeperIdYellow(0);
		AIInfoFrame frame = generateAiFrame();
		preAssign(frame, 1, 1, 3, 1);
		
		assignRoles(frame, EPlay.KEEPER, BotID.createBotId(0, frame.getTeamColor()));
		assignRoles(frame, EPlay.OFFENSIVE, BotID.createBotId(1, frame.getTeamColor()));
		assignRoles(frame, EPlay.DEFENSIVE, BotID.createBotId(2, frame.getTeamColor()));
		assignRoles(frame, EPlay.DEFENSIVE, BotID.createBotId(3, frame.getTeamColor()));
		assignRoles(frame, EPlay.DEFENSIVE, BotID.createBotId(5, frame.getTeamColor()));
		assignRoles(frame, EPlay.SUPPORT, BotID.createBotId(4, frame.getTeamColor()));
		printAssignment(frame);
		
		RoleFinderInfo keeperInfo = new RoleFinderInfo(1, 1, 1);
		frame.getTacticalField().getRoleFinderInfos().put(EPlay.KEEPER, keeperInfo);
		frame.getTacticalField().getRoleFinderInfos().get(EPlay.KEEPER).getDesiredBots()
				.add(BotID.createBotId(0, frame.getTeamColor()));
		
		RoleFinderInfo offenceInfo = new RoleFinderInfo(0, 1, 1);
		frame.getTacticalField().getRoleFinderInfos().put(EPlay.OFFENSIVE, offenceInfo);
		
		RoleFinderInfo defenseInfo = new RoleFinderInfo(0, 3, 3);
		frame.getTacticalField().getRoleFinderInfos().put(EPlay.DEFENSIVE, defenseInfo);
		frame.getTacticalField().getRoleFinderInfos().get(EPlay.DEFENSIVE).getDesiredBots()
				.add(BotID.createBotId(2, frame.getTeamColor()));
		frame.getTacticalField().getRoleFinderInfos().get(EPlay.DEFENSIVE).getDesiredBots()
				.add(BotID.createBotId(5, frame.getTeamColor()));
		frame.getTacticalField().getRoleFinderInfos().get(EPlay.DEFENSIVE).getDesiredBots()
				.add(BotID.createBotId(4, frame.getTeamColor()));
		
		RoleFinderInfo supportInfo = new RoleFinderInfo(0, 6, 2);
		frame.getTacticalField().getRoleFinderInfos().put(EPlay.SUPPORT, supportInfo);
		
		runFullTest(frame);
		printAssignment(frame);
		
		// Assert.assertTrue(containsBotId(frame, ERole.OFFENSIVE, 5));
		Assert.assertTrue(containsBotId(frame, ERole.DEFENDER, 2));
		Assert.assertTrue(containsBotId(frame, ERole.DEFENDER, 5));
		Assert.assertTrue(containsBotId(frame, ERole.DEFENDER, 4));
		// Assert.assertTrue(containsBotId(frame, ERole.SUPPORT, 0));
		Assert.assertTrue(containsBotId(frame, ERole.KEEPER, 0));
	}
	
	
	private void printAssignment(final AIInfoFrame frame)
	{
		
		for (APlay play : frame.getPlayStrategy().getActivePlays())
		{
			for (ARole role : play.getRoles())
			{
				System.out.println(role.getType() + " " + role.getBotID());
			}
		}
		System.out.println();
	}
	
	
	private boolean containsBotId(final AIInfoFrame frame, final ERole eRole, final int botId)
	{
		return frame.getPlayStrategy().getActiveRoles(eRole).stream().map(r -> r.getBotID())
				.collect(Collectors.toList()).contains(BotID.createBotId(botId, frame.getTeamColor()));
	}
	
	
	/**
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 */
	@BeforeClass
	public static void beforeClass()
	{
		frameFactory = new FrameFactory();
	}
	
	
	/**
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 */
	@AfterClass
	public static void afterClass()
	{
		frameFactory.close();
	}
}
