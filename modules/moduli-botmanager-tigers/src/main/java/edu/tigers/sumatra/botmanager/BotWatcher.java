/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.botmanager;

import edu.tigers.sumatra.botmanager.bots.ITigerBotObserver;
import edu.tigers.sumatra.botmanager.bots.TigerBot;
import edu.tigers.sumatra.botmanager.botskills.EDataAcquisitionMode;
import edu.tigers.sumatra.botmanager.commands.ACommand;
import edu.tigers.sumatra.botmanager.commands.tigerv2.TigerSystemConsoleCommand;
import edu.tigers.sumatra.botmanager.commands.tigerv2.TigerSystemMatchFeedback;
import edu.tigers.sumatra.botmanager.commands.tigerv3.TigerDataAcqBotModel;
import edu.tigers.sumatra.botmanager.commands.tigerv3.TigerDataAcqBotModelV2;
import edu.tigers.sumatra.botmanager.commands.tigerv3.TigerDataAcqDelays;
import edu.tigers.sumatra.botmanager.commands.tigerv3.TigerDataAcqMotorModel;
import edu.tigers.sumatra.botmanager.commands.tigerv3.TigerDataAcqSetMode;
import edu.tigers.sumatra.botmanager.commands.tigerv3.TigerDataAcqVelocity;
import edu.tigers.sumatra.botparams.EBotParamLabel;
import edu.tigers.sumatra.data.collector.ITimeSeriesDataCollectorObserver;
import edu.tigers.sumatra.data.collector.TimeSeriesDataCollector;
import edu.tigers.sumatra.export.CSVExporter;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.math.vector.Vector3f;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.wp.util.TimeSeriesDataCollectorFactory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;


/**
 * Capture data from a {@link TigerBot} and export it to CSV files
 */
public class BotWatcher implements ITigerBotObserver
{
	private final BotID botId;
	private final EDataAcquisitionMode acqMode;
	private final String id;
	private CSVExporter exporter = null;
	private long frameId = 0;
	private boolean dataReceived = false;
	private TimeSeriesDataCollector dataCollector;
	private ITimeSeriesDataCollectorObserver timeSeriesDataCollectorObserver;


	public BotWatcher(final BotID botId, final String id)
	{
		this(botId, EDataAcquisitionMode.NONE, id);
	}


	public BotWatcher(final BotID botId, final EDataAcquisitionMode acqMode, final String id)
	{
		this.botId = botId;
		this.acqMode = acqMode;
		this.id = id;
	}


	/**
	 * Start bot watcher.
	 */
	public void start()
	{
		String dateStr = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS").format(new Date());
		String fullId = dateStr + "_" + id;

		SumatraModel.getInstance().getModuleOpt(TigersBotManager.class)
				.flatMap(b -> b.getTigerBot(botId))
				.ifPresent(bot -> {
					final TigerSystemConsoleCommand logFileCmd = new TigerSystemConsoleCommand(
							TigerSystemConsoleCommand.ConsoleCommandTarget.MAIN, "logfile " + fullId);
					bot.execute(logFileCmd);
					bot.execute(new TigerDataAcqSetMode(acqMode));
					bot.getBaseStation().activateDataBurstMode();
				});

		switch (acqMode)
		{
			case BOT_MODEL:
				exporter = new CSVExporter("data/botModel/", fullId, CSVExporter.EMode.EXACT_FILE_NAME);
				break;
			case DELAYS:
				exporter = new CSVExporter("data/delays/", fullId, CSVExporter.EMode.EXACT_FILE_NAME);
				break;
			case MOTOR_MODEL:
				exporter = new CSVExporter("data/motorModel/", fullId, CSVExporter.EMode.EXACT_FILE_NAME);
				break;
			case BOT_MODEL_V2:
				exporter = new CSVExporter("data/botModelV2/", fullId, CSVExporter.EMode.EXACT_FILE_NAME);
				break;
			case NONE:
				exporter = new CSVExporter("data/botstatus/", fullId, CSVExporter.EMode.EXACT_FILE_NAME);
				break;
			default:
				throw new IllegalStateException("Unhandled acqMode: " + acqMode);
		}

		dataCollector = TimeSeriesDataCollectorFactory
				.createFullCollector(id + "/" + exporter.getFileName().replaceAll(".csv", ""));
		dataCollector.setStopAutomatically(false);
		dataCollector.setTimeout(600);
		Optional.ofNullable(timeSeriesDataCollectorObserver).ifPresent(dataCollector::addObserver);
		dataCollector.start();

		dataReceived = false;

		SumatraModel.getInstance().getModuleOpt(TigersBotManager.class).ifPresent(b -> b.addBotObserver(this));
	}


	/**
	 * Stop bot watcher.
	 */
	public void stop()
	{
		SumatraModel.getInstance().getModuleOpt(TigersBotManager.class)
				.flatMap(b -> b.getTigerBot(botId))
				.ifPresent(bot -> {
					bot.execute(new TigerDataAcqSetMode(EDataAcquisitionMode.NONE));
					final TigerSystemConsoleCommand stopLogCmd = new TigerSystemConsoleCommand(
							TigerSystemConsoleCommand.ConsoleCommandTarget.MAIN, "stoplog");
					stopLogCmd.setReliable(true);
					bot.execute(stopLogCmd);
					bot.getBaseStation().activateDefaultConfig();

				});

		SumatraModel.getInstance().getModuleOpt(TigersBotManager.class).ifPresent(b -> b.removeBotObserver(this));
		Optional.ofNullable(exporter).ifPresent(CSVExporter::close);
		Optional.ofNullable(dataCollector).ifPresent(TimeSeriesDataCollector::stopExport);
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
		nbrs.add(botId.getNumber());
		nbrs.addAll(botId.getTeamColor().getNumberList());
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
	 * @return true, if any data was received
	 */
	public boolean isDataReceived()
	{
		return dataReceived;
	}


	public void setTimeSeriesDataCollectorObserver(
			final ITimeSeriesDataCollectorObserver timeSeriesDataCollectorObserver)
	{
		this.timeSeriesDataCollectorObserver = timeSeriesDataCollectorObserver;
	}


	@Override
	public void onIncomingBotCommand(final TigerBot tigerBot, final ACommand cmd)
	{
		if (!tigerBot.getBotId().equals(botId))
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
		nbrs.add(botId.getNumberWithColorOffsetBS());
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
		nbrs.add(botId.getNumberWithColorOffsetBS());
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
		nbrs.add(botId.getNumberWithColorOffsetBS());
		nbrs.add(bm.getTimestamp());
		nbrs.add(bm.getVisionTime());
		nbrs.addAll(bm.getOutVelocityList());
		nbrs.addAll(bm.getVisionPositionList());
		exporter.addValues(nbrs);
		dataReceived = true;
	}


	private void handleAcqBotModelV2(final ACommand cmd, final List<Number> nbrs)
	{
		int botParamsLabelNbr = SumatraModel.getInstance().getModuleOpt(TigersBotManager.class)
				.flatMap(b -> b.getTigerBot(botId))
				.map(TigerBot::getBotParamLabel)
				.map(l -> l == EBotParamLabel.TIGER_V2016 ? 2016 : 2020)
				.orElse(0);
		TigerDataAcqBotModelV2 bm = (TigerDataAcqBotModelV2) cmd;
		nbrs.add(botId.getNumberWithColorOffsetBS());
		nbrs.add(bm.getTimestamp());
		nbrs.addAll(bm.getStateVelocityList());
		nbrs.addAll(bm.getEncoderVelocityList());
		nbrs.addAll(bm.getOutputForceList());
		nbrs.add(bm.getEfficiencyXY());
		nbrs.add(bm.getEfficiencyW());
		nbrs.add(bm.getModeXY());
		nbrs.add(bm.getModeW());
		nbrs.add(botParamsLabelNbr);
		exporter.addValues(nbrs);
		dataReceived = true;
	}


	private void handleAcqMotorModel(final ACommand cmd, final List<Number> nbrs)
	{
		TigerDataAcqMotorModel mm = (TigerDataAcqMotorModel) cmd;
		nbrs.add(botId.getNumberWithColorOffsetBS());
		nbrs.add(mm.getTimestamp());
		nbrs.addAll(mm.getMotorVoltageList());
		nbrs.addAll(mm.getMotorVelocityList());
		exporter.addValues(nbrs);
		dataReceived = true;
	}
}
