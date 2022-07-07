/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.presenter.logfile;

import edu.tigers.sumatra.cam.LogfileVisionCam;
import edu.tigers.sumatra.cam.LogfileVisionCam.ILogfileVisionCamObserver;
import edu.tigers.sumatra.gamelog.MergeTool;
import edu.tigers.sumatra.gamelog.SSLGameLogReader;
import edu.tigers.sumatra.gamelog.SSLGameLogReader.SSLGameLogfileEntry;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.referee.proto.SslGcGameEvent;
import edu.tigers.sumatra.referee.proto.SslGcRefereeMessage.Referee.Command;
import edu.tigers.sumatra.view.logfile.LogfilePanel;
import edu.tigers.sumatra.view.logfile.LogfilePanel.ILogfilePanelObserver;
import edu.tigers.sumatra.views.ISumatraViewPresenter;
import lombok.Getter;

import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serial;
import java.util.List;


/**
 * Presenter for log file view.
 */
public class LogfilePresenter implements ISumatraViewPresenter, ILogfilePanelObserver
{
	@Getter
	private final LogfilePanel viewPanel = new LogfilePanel();
	private final DataRefresher dataRefresher = new DataRefresher();

	private SSLGameLogReader logfile;


	@Override
	public void onStartModuli()
	{
		ISumatraViewPresenter.super.onStartModuli();
		viewPanel.addObserver(this);

		SumatraModel.getInstance().getModuleOpt(LogfileVisionCam.class).ifPresent(cam -> {
					cam.addObserver(dataRefresher);
					dataRefresher.start();
				}
		);
	}


	@Override
	public void onStopModuli()
	{
		ISumatraViewPresenter.super.onStopModuli();
		viewPanel.removeObserver(this);

		SumatraModel.getInstance().getModuleOpt(LogfileVisionCam.class).ifPresent(cam -> {
					dataRefresher.stop();
					cam.removeObserver(dataRefresher);
				}
		);
	}


	@Override
	public void onPause()
	{
		SumatraModel.getInstance().getModuleOpt(LogfileVisionCam.class).ifPresent(cam -> cam.setPause(true));
	}


	@Override
	public void onResume()
	{
		SumatraModel.getInstance().getModuleOpt(LogfileVisionCam.class).ifPresent(cam -> cam.setPause(false));
	}


	@Override
	public void onChangeSpeed(final double speed)
	{
		SumatraModel.getInstance().getModuleOpt(LogfileVisionCam.class).ifPresent(cam -> cam.setSpeed(speed));
	}


	@Override
	public void onStep(final int numSteps)
	{
		SumatraModel.getInstance().getModuleOpt(LogfileVisionCam.class).ifPresent(cam -> cam.doSteps(numSteps));
	}


	@Override
	public void onLoadLogfile(final String path)
	{
		logfile = new SSLGameLogReader();

		logfile.loadFile(path, success -> SumatraModel.getInstance().getModuleOpt(LogfileVisionCam.class)
				.ifPresent(cam -> cam.setLogfile(logfile)));
	}


	@Override
	public void onChangePosition(final int pos)
	{
		SumatraModel.getInstance().getModuleOpt(LogfileVisionCam.class).ifPresent(cam -> cam.setPosition(pos));
	}


	@Override
	public void onSeekToRefCmd(final List<Command> commands)
	{
		SumatraModel.getInstance().getModuleOpt(LogfileVisionCam.class)
				.ifPresent(cam -> cam.seekForwardToRefCommand(commands));
	}


	@Override
	public void onSeekToGameEvent(final List<SslGcGameEvent.GameEvent.Type> gameEventTypes)
	{
		SumatraModel.getInstance().getModuleOpt(LogfileVisionCam.class)
				.ifPresent(cam -> cam.seekForwardToGameEvent(gameEventTypes));
	}


	@Override
	public void onMergeFiles(final List<String> inputs, final String output, final boolean removeIdle)
	{
		new MergeTool()
				.withInputFiles(inputs)
				.withOutputFile(output)
				.withRemoveIdleFrames(removeIdle)
				.merge();
	}


	private class DataRefresher extends Timer implements ActionListener, ILogfileVisionCamObserver
	{
		@Serial
		private static final long serialVersionUID = 8597026135355238868L;

		private transient SSLGameLogfileEntry lastEntry;
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

			viewPanel.setNumPackets(logfile.getPackets().size());
			viewPanel.setPosition(lastIndex);

			if (lastEntry == null)
			{
				return;
			}

			viewPanel.updateTime(lastEntry.getTimestamp());
		}


		@Override
		public void onNewLogfileEntry(final SSLGameLogfileEntry e, final int index)
		{
			lastEntry = e;
			lastIndex = index;
		}
	}
}
