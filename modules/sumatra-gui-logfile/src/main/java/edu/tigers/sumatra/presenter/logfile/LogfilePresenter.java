/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.presenter.logfile;

import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.moduli.listenerVariables.ModulesState;
import edu.tigers.sumatra.cam.ACam;
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
import edu.tigers.sumatra.views.ASumatraViewPresenter;
import edu.tigers.sumatra.views.ISumatraView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.Timer;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;


/**
 * @author AndreR
 */
public class LogfilePresenter extends ASumatraViewPresenter implements ILogfilePanelObserver
{
	@SuppressWarnings("unused")
	private static final Logger log = LogManager.getLogger(LogfilePresenter.class.getName());
	private final LogfilePanel logfilePanel = new LogfilePanel();

	private SSLGameLogReader logfile;

	private final DataRefresher dataRefresher = new DataRefresher();


	@Override
	public void onModuliStateChanged(final ModulesState state)
	{
		super.onModuliStateChanged(state);

		switch (state)
		{
			case ACTIVE:
				activateModule();
				break;
			case RESOLVED:
				deactivateModule();
				break;
			default:
				break;

		}
	}


	private void deactivateModule()
	{
		LogfileVisionCam cam;
		logfilePanel.removeObserver(this);

		cam = getLogfileCam();
		if (cam != null)
		{
			dataRefresher.stop();
			cam.removeObserver(dataRefresher);
		}
	}


	private void activateModule()
	{
		LogfileVisionCam cam;
		logfilePanel.addObserver(this);

		cam = getLogfileCam();
		if (cam != null)
		{
			cam.addObserver(dataRefresher);
			dataRefresher.start();
		}
	}


	@Override
	public Component getComponent()
	{
		return logfilePanel;
	}


	@Override
	public ISumatraView getSumatraView()
	{
		return logfilePanel;
	}


	private LogfileVisionCam getLogfileCam()
	{
		try
		{
			ACam cam = SumatraModel.getInstance().getModule(ACam.class);
			if (cam instanceof LogfileVisionCam)
			{
				return (LogfileVisionCam) cam;
			}
		} catch (ModuleNotFoundException e)
		{
			log.debug("Cam module is not of type LogfileVisionCam", e);
		}
		return null;
	}


	@Override
	public void onPause()
	{
		LogfileVisionCam cam = getLogfileCam();
		if (cam != null)
		{
			cam.setPause(true);
		}
	}


	@Override
	public void onResume()
	{
		LogfileVisionCam cam = getLogfileCam();
		if (cam != null)
		{
			cam.setPause(false);
		}
	}


	@Override
	public void onChangeSpeed(final double speed)
	{
		LogfileVisionCam cam = getLogfileCam();

		if (cam != null)
		{
			cam.setSpeed(speed);
		}
	}


	@Override
	public void onStep(final int numSteps)
	{
		LogfileVisionCam cam = getLogfileCam();

		if (cam != null)
		{
			cam.doSteps(numSteps);
		}
	}


	@Override
	public void onLoadLogfile(final String path)
	{
		logfile = new SSLGameLogReader();

		logfile.loadFile(path, success -> {
			LogfileVisionCam cam = getLogfileCam();

			if (cam != null)
			{
				cam.setLogfile(logfile);
			}
		});
	}


	@Override
	public void onChangePosition(final int pos)
	{
		LogfileVisionCam cam = getLogfileCam();

		if (cam != null)
		{
			cam.setPosition(pos);
		}
	}


	@Override
	public void onSeekToRefCmd(final List<Command> commands)
	{
		LogfileVisionCam cam = getLogfileCam();

		if (cam != null)
		{
			cam.seekForwardToRefCommand(commands);
		}
	}


	@Override
	public void onSeekToGameEvent(final List<SslGcGameEvent.GameEvent.Type> gameEventTypes)
	{
		LogfileVisionCam cam = getLogfileCam();

		if (cam != null)
		{
			cam.seekForwardToGameEvent(gameEventTypes);
		}
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
		private static final long serialVersionUID = 8597026135355238868L;

		private SSLGameLogfileEntry lastEntry = null;
		private int lastIndex = 0;


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

			logfilePanel.setNumPackets(logfile.getPackets().size());
			logfilePanel.setPosition(lastIndex);

			if (lastEntry == null)
			{
				return;
			}

			logfilePanel.updateTime(lastEntry.getTimestamp());
		}


		@Override
		public void onNewLogfileEntry(final SSLGameLogfileEntry e, final int index)
		{
			lastEntry = e;
			lastIndex = index;
		}
	}
}
