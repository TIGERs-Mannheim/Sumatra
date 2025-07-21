/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.gui.logfile.presenter;

import edu.tigers.sumatra.gamelog.EMessageType;
import edu.tigers.sumatra.gamelog.GameLogCompareResult;
import edu.tigers.sumatra.gamelog.GameLogMessage;
import edu.tigers.sumatra.gamelog.GameLogPlayer;
import edu.tigers.sumatra.gamelog.GameLogPlayerObserver;
import edu.tigers.sumatra.gamelog.GameLogReader;
import edu.tigers.sumatra.gamelog.MergeTool;
import edu.tigers.sumatra.gamelog.filters.MessageTypeFilter;
import edu.tigers.sumatra.gui.logfile.view.LogfilePanel;
import edu.tigers.sumatra.gui.logfile.view.LogfilePanel.ILogfilePanelObserver;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.referee.proto.SslGcGameEvent;
import edu.tigers.sumatra.referee.proto.SslGcRefereeMessage;
import edu.tigers.sumatra.referee.proto.SslGcRefereeMessage.Referee.Command;
import edu.tigers.sumatra.referee.proto.SslGcRefereeMessage.Referee.Stage;
import edu.tigers.sumatra.views.ISumatraViewPresenter;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serial;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;


/**
 * Presenter for log file view.
 */
@Log4j2
public class LogfilePresenter implements ISumatraViewPresenter, ILogfilePanelObserver
{
	private static final String INVALID_SSL_REFBOX_2013_PACKAGE = "Invalid SSL_REFBOX_2013 package.";

	@Getter
	private final LogfilePanel viewPanel = new LogfilePanel();
	private final DataRefresher dataRefresher = new DataRefresher();

	private GameLogReader logfile;


	@Override
	public void onModuliStarted()
	{
		ISumatraViewPresenter.super.onModuliStarted();
		viewPanel.addObserver(this);

		SumatraModel.getInstance().getModuleOpt(GameLogPlayer.class).ifPresent(player -> {
					player.addObserver(dataRefresher);
					dataRefresher.start();
				}
		);
	}


	@Override
	public void onModuliStopped()
	{
		ISumatraViewPresenter.super.onModuliStopped();
		viewPanel.removeObserver(this);

		SumatraModel.getInstance().getModuleOpt(GameLogPlayer.class).ifPresent(player -> {
					dataRefresher.stop();
					player.removeObserver(dataRefresher);
				}
		);
	}


	@Override
	public void onPause()
	{
		SumatraModel.getInstance().getModuleOpt(GameLogPlayer.class).ifPresent(player -> player.setPause(true));
	}


	@Override
	public void onResume()
	{
		SumatraModel.getInstance().getModuleOpt(GameLogPlayer.class).ifPresent(player -> player.setPause(false));
	}


	@Override
	public void onChangeSpeed(final double speed)
	{
		SumatraModel.getInstance().getModuleOpt(GameLogPlayer.class).ifPresent(player -> player.setSpeed(speed));
	}


	@Override
	public void onStep(final int numSteps)
	{
		SumatraModel.getInstance().getModuleOpt(GameLogPlayer.class).ifPresent(player -> player.doSteps(numSteps));
	}


	@Override
	public void onLoadLogfile(final String path)
	{
		logfile = new GameLogReader();
		logfile.addFilter(new MessageTypeFilter(EnumSet.of(EMessageType.SSL_REFBOX_2013, EMessageType.SSL_VISION_2014,
				EMessageType.TIGERS_BASE_STATION_CMD_RECEIVED, EMessageType.TIGERS_BASE_STATION_CMD_SENT)));

		logfile.loadFile(path, success -> SumatraModel.getInstance().getModuleOpt(GameLogPlayer.class)
				.ifPresent(player -> player.setLogfile(logfile)));
	}


	@Override
	public void onChangePosition(final int pos)
	{
		SumatraModel.getInstance().getModuleOpt(GameLogPlayer.class).ifPresent(player -> player.setPosition(pos));
	}


	@Override
	public void onSeekToRefCmd(final List<Command> commands)
	{
		SumatraModel.getInstance().getModuleOpt(GameLogPlayer.class)
				.ifPresent(player -> player.seekTo(msg -> seekToRefCommand(msg, commands)));
	}


	@Override
	public void onSeekToGameEvent(final List<SslGcGameEvent.GameEvent.Type> gameEventTypes)
	{
		SumatraModel.getInstance().getModuleOpt(GameLogPlayer.class)
				.ifPresent(player -> player.seekTo(msg -> seekToGameEvent(msg, gameEventTypes)));
	}


	@Override
	public void onMergeFiles(final List<String> inputs, final String output, final boolean removeIdle)
	{
		new MergeTool()
				.withInputFiles(inputs)
				.withOutputFile(output)
				.withFilter(removeIdle ? LogfilePresenter::isIdle : null)
				.merge();
	}


	private class DataRefresher extends Timer implements ActionListener, GameLogPlayerObserver
	{
		@Serial
		private static final long serialVersionUID = 8597026135355238868L;

		private transient GameLogMessage lastMessage;
		private int lastIndex;


		public DataRefresher()
		{
			super(20, null);
			addActionListener(this);
		}


		@Override
		public void actionPerformed(final ActionEvent e)
		{
			if (logfile == null)
			{
				return;
			}

			viewPanel.setNumPackets(logfile.getMessages().size());
			viewPanel.setPosition(lastIndex);

			if (lastMessage == null)
			{
				return;
			}

			viewPanel.updateTime(lastMessage.getTimestampNs());
		}


		@Override
		public void onNewGameLogMessage(GameLogMessage message, int index)
		{
			lastMessage = message;
			lastIndex = index;
		}


		@Override
		public void onGameLogTimeJump()
		{
			// don't care about time jumps here
		}
	}


	private static GameLogCompareResult seekToRefCommand(final GameLogMessage msg, final List<Command> commands)
	{
		if (msg.getType() != EMessageType.SSL_REFBOX_2013)
			return GameLogCompareResult.IGNORE;

		try
		{
			var ref = SslGcRefereeMessage.Referee.parseFrom(msg.getData());
			return commands.contains(ref.getCommand()) ? GameLogCompareResult.MATCH : GameLogCompareResult.MISMATCH;
		} catch (Exception err)
		{
			log.error(INVALID_SSL_REFBOX_2013_PACKAGE, err);
			return GameLogCompareResult.IGNORE;
		}
	}

	private static GameLogCompareResult seekToGameEvent(final GameLogMessage msg, final List<SslGcGameEvent.GameEvent.Type> gameEventTypes)
	{
		if (msg.getType() != EMessageType.SSL_REFBOX_2013)
			return GameLogCompareResult.IGNORE;

		try
		{
			var ref = SslGcRefereeMessage.Referee.parseFrom(msg.getData());

			boolean match = ref.getGameEventsCount() > 0
					&& ref.getGameEventsList().stream()
					.map(SslGcGameEvent.GameEvent::getType)
					.anyMatch(gameEventTypes::contains);

			return match ? GameLogCompareResult.MATCH : GameLogCompareResult.MISMATCH;
		} catch (Exception err)
		{
			log.error(INVALID_SSL_REFBOX_2013_PACKAGE, err);
			return GameLogCompareResult.IGNORE;
		}
	}

	private static GameLogCompareResult isIdle(final GameLogMessage msg)
	{
		if (msg.getType() != EMessageType.SSL_REFBOX_2013)
			return GameLogCompareResult.IGNORE;

		List<Stage> idleStages = new ArrayList<>();
		idleStages.add(Stage.NORMAL_FIRST_HALF_PRE);
		idleStages.add(Stage.NORMAL_SECOND_HALF_PRE);
		idleStages.add(Stage.EXTRA_FIRST_HALF_PRE);
		idleStages.add(Stage.EXTRA_SECOND_HALF_PRE);
		idleStages.add(Stage.EXTRA_TIME_BREAK);
		idleStages.add(Stage.NORMAL_HALF_TIME);
		idleStages.add(Stage.PENALTY_SHOOTOUT_BREAK);
		idleStages.add(Stage.POST_GAME);

		List<Command> idleCmds = new ArrayList<>();
		idleCmds.add(Command.HALT);
		idleCmds.add(Command.TIMEOUT_BLUE);
		idleCmds.add(Command.TIMEOUT_YELLOW);

		try
		{
			var ref = SslGcRefereeMessage.Referee.parseFrom(msg.getData());

			return idleStages.contains(ref.getStage()) || idleCmds.contains(ref.getCommand()) ? GameLogCompareResult.MATCH : GameLogCompareResult.MISMATCH;

		} catch (Exception err)
		{
			log.error(INVALID_SSL_REFBOX_2013_PACKAGE, err);
			return GameLogCompareResult.IGNORE;
		}
	}
}
