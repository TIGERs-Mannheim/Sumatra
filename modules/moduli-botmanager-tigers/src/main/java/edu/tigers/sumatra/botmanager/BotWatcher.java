/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.botmanager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import edu.tigers.sumatra.botmanager.bots.ITigerBotObserver;
import edu.tigers.sumatra.botmanager.bots.TigerBot;
import edu.tigers.sumatra.botmanager.botskills.EDataAcquisitionMode;
import edu.tigers.sumatra.botmanager.commands.ACommand;
import edu.tigers.sumatra.botmanager.commands.tigerv2.TigerSystemMatchFeedback;
import edu.tigers.sumatra.botmanager.commands.tigerv3.TigerDataAcqBotModel;
import edu.tigers.sumatra.botmanager.commands.tigerv3.TigerDataAcqBotModelV2;
import edu.tigers.sumatra.botmanager.commands.tigerv3.TigerDataAcqDelays;
import edu.tigers.sumatra.botmanager.commands.tigerv3.TigerDataAcqMotorModel;
import edu.tigers.sumatra.botmanager.commands.tigerv3.TigerDataAcqSetMode;
import edu.tigers.sumatra.botmanager.commands.tigerv3.TigerDataAcqVelocity;
import edu.tigers.sumatra.export.CSVExporter;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.math.vector.Vector3f;
import edu.tigers.sumatra.model.SumatraModel;


/**
 * Capture data from a {@link TigerBot} and export it to CSV files
 */
public class BotWatcher implements ITigerBotObserver
{
	private final TigerBot bot;
	private final EDataAcquisitionMode acqMode;
	private CSVExporter exporter = null;
	private long frameId = 0;
	private boolean dataReceived = false;
	
	
	/**
	 * @param bot
	 */
	public BotWatcher(final TigerBot bot)
	{
		this.bot = bot;
		acqMode = EDataAcquisitionMode.NONE;
	}
	
	
	/**
	 * @param bot
	 * @param acqMode
	 */
	public BotWatcher(final TigerBot bot, final EDataAcquisitionMode acqMode)
	{
		this.bot = bot;
		this.acqMode = acqMode;
	}
	
	
	/**
	 * Start bot watcher.
	 */
	public void start()
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		
		switch (acqMode)
		{
			case BOT_MODEL:
				exporter = new CSVExporter("data/botModel/" + sdf.format(new Date()), false);
				break;
			case DELAYS:
				exporter = new CSVExporter("data/delays/" + sdf.format(new Date()), false);
				break;
			case MOTOR_MODEL:
				exporter = new CSVExporter("data/motorModel/" + sdf.format(new Date()), false);
				break;
			case BOT_MODEL_V2:
				exporter = new CSVExporter("data/botModelV2/" + sdf.format(new Date()), false);
				break;
			case NONE:
				exporter = new CSVExporter("data/botstatus/" + sdf.format(new Date()), false);
				break;
			default:
				throw new IllegalStateException();
		}
		bot.execute(new TigerDataAcqSetMode(acqMode));
		
		dataReceived = false;
		
		TigersBotManager tigersBotManager = (TigersBotManager) SumatraModel.getInstance().getModule(ABotManager.class);
		tigersBotManager.addBotObserver(this);
	}
	
	
	/**
	 * Stop bot watcher.
	 */
	public void stop()
	{
		bot.execute(new TigerDataAcqSetMode(EDataAcquisitionMode.NONE));
		TigersBotManager tigersBotManager = (TigersBotManager) SumatraModel.getInstance().getModule(ABotManager.class);
		tigersBotManager.removeBotObserver(this);
		exporter.close();
	}
	
	
	/**
	 * @return
	 */
	public String getAbsoluteFileName()
	{
		return exporter.getAbsoluteFileName();
	}
	
	
	private void onNewFeedbackCmd(final TigerSystemMatchFeedback cmd)
	{
		if (acqMode != EDataAcquisitionMode.NONE)
		{
			return;
		}
		
		IVector3 pos = Vector3.from2d(cmd.getPosition(), cmd.getOrientation());
		IVector3 vel = Vector3.from2d(cmd.getVelocity(), cmd.getAngularVelocity());
		List<Number> nbrs = new ArrayList<>();
		nbrs.add(bot.getBotId().getNumber());
		nbrs.addAll(bot.getColor().getNumberList());
		nbrs.addAll(pos.getNumberList());
		nbrs.addAll(vel.getNumberList());
		nbrs.addAll(Vector3f.ZERO_VECTOR.getNumberList());
		nbrs.add(frameId);
		nbrs.add(System.nanoTime());
		nbrs.add(cmd.isPositionValid() ? 1 : 0);
		nbrs.add(cmd.isVelocityValid() ? 1 : 0);
		nbrs.add(cmd.isAccelerationValid() ? 1 : 0);
		exporter.addValues(nbrs);
		frameId++;
		
		dataReceived = true;
	}
	
	
	/**
	 * @return
	 */
	public boolean isDataReceived()
	{
		return dataReceived;
	}
	
	
	@Override
	public void onIncomingBotCommand(final TigerBot tigerBot, final ACommand cmd)
	{
		if (tigerBot.getBotId() != bot.getBotId())
		{
			return;
		}
		
		List<Number> nbrs = new ArrayList<>();
		switch (cmd.getType())
		{
			case CMD_SYSTEM_MATCH_FEEDBACK:
				TigerSystemMatchFeedback fdbk = (TigerSystemMatchFeedback) cmd;
				onNewFeedbackCmd(fdbk);
				break;
			case CMD_DATA_ACQ_MOTOR_MODEL:
				handleAcqMotorModel(cmd, nbrs);
				break;
			case CMD_DATA_ACQ_BOT_MODEL:
				handleAcqBotModel(cmd, nbrs);
				break;
			case CMD_DATA_ACQ_DELAYS:
				handleAcqDelays(cmd, nbrs);
				break;
			case CMD_DATA_ACQ_VELOCITY:
				handleAcqVelocity(cmd, nbrs);
				break;
			case CMD_DATA_ACQ_BOT_MODEL_V2:
				handleAcqBotModelV2(cmd, nbrs);
				break;
			default:
				break;
		}
	}
	
	
	private void handleAcqVelocity(final ACommand cmd, final List<Number> nbrs)
	{
		TigerDataAcqVelocity vel = (TigerDataAcqVelocity) cmd;
		nbrs.add(bot.getBotId().getNumberWithColorOffsetBS());
		nbrs.add(vel.getTimestamp());
		nbrs.addAll(vel.getSetAcc().getNumberList());
		nbrs.addAll(vel.getSetVel().getNumberList());
		nbrs.addAll(vel.getOutVel().getNumberList());
		exporter.addValues(nbrs);
		dataReceived = true;
	}
	
	
	private void handleAcqDelays(final ACommand cmd, final List<Number> nbrs)
	{
		TigerDataAcqDelays de = (TigerDataAcqDelays) cmd;
		nbrs.add(bot.getBotId().getNumberWithColorOffsetBS());
		nbrs.add(de.getTimestamp());
		nbrs.add(de.getVisionTime());
		nbrs.add(de.getOutVelocityW());
		nbrs.add(de.getVisionPositionW());
		nbrs.add(de.getGyroVelocityW());
		exporter.addValues(nbrs);
		dataReceived = true;
	}
	
	
	private void handleAcqBotModel(final ACommand cmd, final List<Number> nbrs)
	{
		TigerDataAcqBotModel bm = (TigerDataAcqBotModel) cmd;
		nbrs.add(bot.getBotId().getNumberWithColorOffsetBS());
		nbrs.add(bm.getTimestamp());
		nbrs.add(bm.getVisionTime());
		nbrs.addAll(bm.getOutVelocityList());
		nbrs.addAll(bm.getVisionPositionList());
		exporter.addValues(nbrs);
		dataReceived = true;
	}
	
	
	private void handleAcqBotModelV2(final ACommand cmd, final List<Number> nbrs)
	{
		TigerDataAcqBotModelV2 bm = (TigerDataAcqBotModelV2) cmd;
		nbrs.add(bot.getBotId().getNumberWithColorOffsetBS());
		nbrs.add(bm.getTimestamp());
		nbrs.addAll(bm.getStateVelocityList());
		nbrs.addAll(bm.getEncoderVelocityList());
		nbrs.addAll(bm.getOutputForceList());
		nbrs.add(bm.getEfficiencyXY());
		nbrs.add(bm.getEfficiencyW());
		nbrs.add(bm.getModeXY());
		nbrs.add(bm.getModeW());
		exporter.addValues(nbrs);
		dataReceived = true;
	}
	
	
	private void handleAcqMotorModel(final ACommand cmd, final List<Number> nbrs)
	{
		TigerDataAcqMotorModel mm = (TigerDataAcqMotorModel) cmd;
		nbrs.add(bot.getBotId().getNumberWithColorOffsetBS());
		nbrs.add(mm.getTimestamp());
		nbrs.addAll(mm.getMotorVoltageList());
		nbrs.addAll(mm.getMotorVelocityList());
		exporter.addValues(nbrs);
		dataReceived = true;
	}
	
	
	/**
	 * @return the acqMode
	 */
	public EDataAcquisitionMode getAcqMode()
	{
		return acqMode;
	}
}
