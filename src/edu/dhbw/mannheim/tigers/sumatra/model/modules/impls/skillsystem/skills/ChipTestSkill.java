/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 15, 2014
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
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.devices.ChipParams;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.MergedCamDetectionFrame;
import edu.dhbw.mannheim.tigers.sumatra.util.csvexporter.BallWatcher;
import edu.dhbw.mannheim.tigers.sumatra.util.csvexporter.ExportDataContainer;
import edu.dhbw.mannheim.tigers.sumatra.util.csvexporter.IBallWatcherObserver;


/**
 * Test the fast chip skill
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class ChipTestSkill extends ChipSkill implements IBallWatcherObserver
{
	@SuppressWarnings("unused")
	private static final Logger	log					= Logger.getLogger(ChipTestSkill.class.getName());
	private final int					duration;
	private final int					dribble;
	private final BallWatcher		watcher;
	private int							lastKickCounter	= -1;
	
	
	/**
	 * @param target
	 * @param duration
	 * @param dribble
	 */
	public ChipTestSkill(final DynamicPosition target, final int duration, final int dribble)
	{
		super(ESkillName.CHIP_FAST_TEST, target);
		this.duration = duration;
		this.dribble = dribble;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		String fileName = "ballChip/" + sdf.format(new Date());
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
	protected ChipParams calcChipParams(final float kickLength)
	{
		return new ChipParams(duration, dribble);
	}
	
	
	@Override
	public void postProcessing(final String fileName)
	{
	}
	
	
	@Override
	public void beforeExport(final Map<String, Object> jsonMapping)
	{
		jsonMapping.put("duration", duration);
		jsonMapping.put("dribble", dribble);
	}
	
	
	@Override
	public void onAddCustomData(final ExportDataContainer container, final MergedCamDetectionFrame frame)
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
