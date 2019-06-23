/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 6, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.DynamicPosition;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EBotType;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.TigerBotV3;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillName;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.MergedCamDetectionFrame;
import edu.dhbw.mannheim.tigers.sumatra.util.csvexporter.BallWatcher;
import edu.dhbw.mannheim.tigers.sumatra.util.csvexporter.ExportDataContainer;
import edu.dhbw.mannheim.tigers.sumatra.util.csvexporter.IBallWatcherObserver;


/**
 * Use custom duration for kicking
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class KickTestSkill extends KickSkill implements IBallWatcherObserver
{
	@SuppressWarnings("unused")
	private static final Logger	log					= Logger.getLogger(KickTestSkill.class.getName());
	private final BallWatcher		watcher;
	private int							lastKickCounter	= -1;
	
	
	/**
	 * @param target
	 * @param kickMode
	 * @param moveMode
	 * @param duration
	 */
	public KickTestSkill(final DynamicPosition target, final EKickMode kickMode, final EMoveMode moveMode,
			final int duration)
	{
		super(ESkillName.KICK_TEST, target, kickMode, moveMode);
		setDuration(duration);
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		String fileName = "ballKick/" + sdf.format(new Date());
		watcher = new BallWatcher(fileName);
		watcher.addObserver(this);
	}
	
	
	@Override
	public void doCalcEntryActions(final List<ACommand> cmds)
	{
		super.doCalcEntryActions(cmds);
		watcher.start();
		if (getBotType() == EBotType.TIGER_V3)
		{
			TigerBotV3 botv3 = (TigerBotV3) getBot();
			lastKickCounter = botv3.getLatestFeedbackCmd().getKickCounter();
		}
	}
	
	
	@Override
	public void beforeExport(final Map<String, Object> jsonMapping)
	{
		jsonMapping.put("duration", getDuration());
	}
	
	
	@Override
	public void onAddCustomData(final ExportDataContainer container, final MergedCamDetectionFrame frame)
	{
	}
	
	
	@Override
	public void postProcessing(final String fileName)
	{
	}
	
	
	@Override
	public void doCalcActions(final List<ACommand> cmds)
	{
		super.doCalcActions(cmds);
		if (getBotType() == EBotType.TIGER_V3)
		{
			TigerBotV3 botv3 = (TigerBotV3) getBot();
			int newKickCounter = botv3.getLatestFeedbackCmd().getKickCounter();
			if (newKickCounter != lastKickCounter)
			{
				complete();
			}
			lastKickCounter = newKickCounter;
		}
	}
}
