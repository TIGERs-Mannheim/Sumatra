/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 6, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.skillsystem.skills;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.botmanager.commands.other.EKickerDevice;
import edu.tigers.sumatra.skillsystem.ESkill;
import edu.tigers.sumatra.wp.ExportDataContainer;
import edu.tigers.sumatra.wp.IBallWatcherObserver;
import edu.tigers.sumatra.wp.VisionWatcher;
import edu.tigers.sumatra.wp.data.DynamicPosition;
import edu.tigers.sumatra.wp.data.ExtendedCamDetectionFrame;


/**
 * Use custom duration for kicking
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class KickTestSkill extends KickSkill implements IBallWatcherObserver
{
	@SuppressWarnings("unused")
	private static final Logger	log	= Logger.getLogger(KickTestSkill.class.getName());
	private final VisionWatcher	watcher;
	
	
	/**
	 * @param target
	 * @param kickSpeed
	 */
	public KickTestSkill(final DynamicPosition target, final double kickSpeed)
	{
		super(ESkill.KICK_TEST, target, EKickMode.FIXED_SPEED, EKickerDevice.STRAIGHT, EMoveMode.CHILL, kickSpeed);
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		String fileName = "ballKick/" + sdf.format(new Date());
		watcher = new VisionWatcher(fileName);
		watcher.addObserver(this);
		
		getMoveCon().setPenaltyAreaAllowedOur(true);
		getMoveCon().setPenaltyAreaAllowedTheir(true);
	}
	
	
	@Override
	public void onSkillStarted()
	{
		watcher.start();
	}
	
	
	@Override
	public void beforeExport(final Map<String, Object> jsonMapping)
	{
		jsonMapping.put("kickSpeed", getKickSpeed());
	}
	
	
	@Override
	public void onAddCustomData(final ExportDataContainer container, final ExtendedCamDetectionFrame frame)
	{
	}
	
	
	@Override
	public void postProcessing(final String fileName)
	{
	}
}
