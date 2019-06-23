/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 5, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.util.csvexporter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector3;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.TigerBotV3;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.TigerBotV3.ITigerBotV3Observer;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerSystemConsolePrint;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerSystemMatchFeedback;
import edu.dhbw.mannheim.tigers.sumatra.util.csvexporter.ExportDataContainer.WpBot;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BotWatcher implements ITigerBotV3Observer
{
	private final TigerBotV3	bot;
	private CSVExporter			exporter	= null;
	private long					frameId	= 0;
	
	
	/**
	 * @param bot
	 */
	public BotWatcher(final TigerBotV3 bot)
	{
		this.bot = bot;
	}
	
	
	/**
	 * 
	 */
	public void start()
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		exporter = new CSVExporter("data/botstatus/" + sdf.format(new Date()), false);
		bot.addObserver(this);
	}
	
	
	/**
	 * 
	 */
	public void stop()
	{
		bot.removeObserver(this);
		exporter.close();
	}
	
	
	@Override
	public void onNewFeedbackCmd(final TigerSystemMatchFeedback cmd)
	{
		IVector3 pos = new Vector3(cmd.getPosition(), cmd.getOrientation());
		IVector3 vel = new Vector3(cmd.getVelocity(), cmd.getAngularVelocity());
		IVector3 acc = new Vector3(cmd.getAcceleration(), cmd.getAngularAcceleration());
		WpBot wpBot = new WpBot(pos, vel, acc, bot.getBotID().getNumber(), bot.getColor(), frameId, System.nanoTime());
		List<Number> nbrs = wpBot.getNumberList();
		nbrs.add(cmd.isPositionValid() ? 1 : 0);
		nbrs.add(cmd.isVelocityValid() ? 1 : 0);
		nbrs.add(cmd.isAccelerationValid() ? 1 : 0);
		exporter.addValues(nbrs);
		frameId++;
	}
	
	
	@Override
	public void onConsolePrint(final TigerSystemConsolePrint cmd)
	{
	}
}
