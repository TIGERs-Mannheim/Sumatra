/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 26, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.replay;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.airecord.IRecordFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.airecord.IRecordWfFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.SimpleWorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeamColor;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotIDMap;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.IBotIDMap;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.fieldPrediction.FieldPredictor;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.fieldPrediction.WorldFramePrediction;
import edu.dhbw.mannheim.tigers.sumatra.model.persistance.BerkeleyLogEvent;
import edu.dhbw.mannheim.tigers.sumatra.model.persistance.IRecordPersistence;
import edu.dhbw.mannheim.tigers.sumatra.presenter.log.LogPresenter;
import edu.dhbw.mannheim.tigers.sumatra.presenter.visualizer.OptionsPanelPresenter;
import edu.dhbw.mannheim.tigers.sumatra.util.NamedThreadFactory;
import edu.dhbw.mannheim.tigers.sumatra.view.botoverview.BotOverviewPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.EVisualizerOptions;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.OptionsPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.FieldPanel;


/**
 * This is a dedicated window that holds a field and a control panel for replaying captured scenes
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class ReplayWindow extends JFrame implements IReplayControlPanelObserver
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**  */
	private static final long							serialVersionUID	= 1L;
	private static final Logger						log					= Logger
																								.getLogger(ReplayWindow.class.getName());
	
	private static final int							BUFFER_SIZE			= 2000;
	/** fetch new frames, if only FETCH_THRESHOLD frames left */
	private static final int							FETCH_THRESHOLD	= 100;
	
	private static final long							LOG_BUFFER_BEFORE	= 100;
	private static final long							LOG_BUFFER_AFTER	= 100;
	private static final int							WINDOW_HEIGHT		= 800;
	private static final int							WINDOW_WIDTH		= 1000;
	
	private final List<IReplayPositionObserver>	positionObservers	= new CopyOnWriteArrayList<IReplayPositionObserver>();
	
	private IRecordPersistence							persistance			= null;
	private List<IRecordFrame>							aiFrameBuffer		= new ArrayList<IRecordFrame>(BUFFER_SIZE);
	private List<BerkeleyLogEvent>					logEventBuffer		= null;
	private final FieldPanel							fieldPanel;
	private final LogPresenter							logPresenter;
	private final BotOverviewPanel					botOverviewPanel;
	private final JLabel									timeStepLabel		= new JLabel();
	
	private int												position				= 0;
	/** fps */
	private int												speed					= 0;
	private int												speedCtr				= 0;
	private boolean										playing				= true;
	private final int										maxFrames;
	
	private final Object									frameBufferLock	= new Object();
	private final Fetcher								fetcher				= new Fetcher();
	private volatile int									currentStart		= 0;
	private boolean										firstUpdate			= true;
	
	private final ScheduledExecutorService			executor				= Executors
																								.newSingleThreadScheduledExecutor(new NamedThreadFactory(
																										"Replay"));
	
	private long											timePositionLock	= System.nanoTime();
	
	private final long									sleepTime;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @param buffer
	 * @param logEvents
	 */
	public ReplayWindow(final List<IRecordFrame> buffer, final List<BerkeleyLogEvent> logEvents)
	{
		this(buffer, buffer.size());
		logEventBuffer = logEvents;
	}
	
	
	/**
	 * @param persistance
	 */
	public ReplayWindow(final IRecordPersistence persistance)
	{
		this(persistance.load(0, BUFFER_SIZE), persistance.size());
		this.persistance = persistance;
	}
	
	
	private ReplayWindow(final List<IRecordFrame> buffer, final int maxFrames)
	{
		this.maxFrames = maxFrames;
		if ((buffer.isEmpty()))
		{
			throw new IllegalArgumentException("aiFrameBuffer must have at least one Frame");
		}
		aiFrameBuffer = buffer;
		
		setTitle("Replay");
		setLayout(new BorderLayout());
		setIconImage(new ImageIcon(ClassLoader.getSystemResource("kralle-icon.png")).getImage());
		addWindowListener(new MyWindowListener());
		setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
		
		fieldPanel = new FieldPanel();
		fieldPanel.setPanelVisible(true);
		
		botOverviewPanel = new BotOverviewPanel();
		
		logPresenter = new LogPresenter();
		logPresenter.setAutoScrolling(false);
		// appender is added to root logger in presenter
		// and it is hard to do it anywhere else for the main log presenter, so just remove it here
		Logger.getRootLogger().removeAppender(logPresenter);
		
		final OptionsPanel optionsPanel = new OptionsPanel();
		OptionsPanelPresenter optionsPanelPresenter = new OptionsPanelPresenter(fieldPanel, optionsPanel);
		optionsPanelPresenter.setSaveOptions(false);
		optionsPanel.addObserver(optionsPanelPresenter);
		optionsPanel.setInitialButtonState();
		optionsPanel.setButtonsEnabled(true);
		
		for (JCheckBox cb : optionsPanel.getCheckBoxes().values())
		{
			optionsPanelPresenter.reactOnActionCommand(cb.getActionCommand(), cb.isSelected());
		}
		
		ReplayControlPanel controlPanel = new ReplayControlPanel(maxFrames);
		controlPanel.addObserver(this);
		addPositionObserver(controlPanel);
		
		JPanel timeStepPanel = createTimeStepPanel();
		optionsPanel.add(timeStepPanel);
		
		JPanel mainPanel = new JPanel(new BorderLayout());
		final JSplitPane splitPane2 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, logPresenter.getComponent(),
				botOverviewPanel);
		final JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, mainPanel, splitPane2);
		
		mainPanel.add(fieldPanel, BorderLayout.CENTER);
		mainPanel.add(optionsPanel, BorderLayout.EAST);
		mainPanel.add(controlPanel, BorderLayout.SOUTH);
		this.add(splitPane, BorderLayout.CENTER);
		
		pack();
		
		fieldPanel.addComponentListener(new FieldPanelComponentListener());
		
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				splitPane.setDividerLocation(0.7);
				splitPane2.setDividerLocation(0.6);
			}
		});
		
		if (!aiFrameBuffer.isEmpty())
		{
			long t1 = aiFrameBuffer.get(0).getWorldFrame().getSystemTime().getTime();
			long t2 = aiFrameBuffer.get(aiFrameBuffer.size() - 1).getWorldFrame().getSystemTime().getTime();
			long timeNs = (long) ((t2 - t1) * 1e6);
			sleepTime = timeNs / aiFrameBuffer.size();
		} else
		{
			sleepTime = 16000000;
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @return
	 */
	private JPanel createTimeStepPanel()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.setBorder(BorderFactory.createTitledBorder("TimeStep"));
		panel.add(Box.createHorizontalGlue());
		panel.add(timeStepLabel);
		
		return panel;
	}
	
	
	/**
	 * Activate this window by setting it visible and start refresh thread
	 */
	public void activate()
	{
		setVisible(true);
		requestFocus();
		
		executor.scheduleAtFixedRate(new RefreshThread(), 100, (sleepTime), TimeUnit.NANOSECONDS);
	}
	
	
	/**
	 * @param o
	 */
	public final void addPositionObserver(final IReplayPositionObserver o)
	{
		positionObservers.add(o);
	}
	
	
	/**
	 * @param o
	 */
	public final void removePositionObserver(final IReplayPositionObserver o)
	{
		positionObservers.remove(o);
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * This thread will update the field periodically according to the speed
	 * 
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 */
	private class RefreshThread implements Runnable
	{
		private int	lastPos	= 0;
		
		
		@Override
		public void run()
		{
			try
			{
				synchronized (frameBufferLock)
				{
					int pos = position;
					if (playing)
					{
						if (speed < 0)
						{
							speedCtr--;
							if (speedCtr <= 0)
							{
								pos++;
								speedCtr = -speed + 1;
							}
						} else if (speed >= 0)
						{
							pos += speed + 1;
						}
						
						if ((pos < 0) || (pos >= (maxFrames - 1)))
						{
							pos = 0;
						}
					}
					if (pos != lastPos)
					{
						update(pos);
						for (IReplayPositionObserver o : positionObservers)
						{
							o.onPositionChanged(pos);
						}
					}
					if (TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - timePositionLock) > 500)
					{
						position = pos;
					}
					lastPos = pos;
				}
			} catch (Exception err)
			{
				log.error("Error in RefreshThread.", err);
			}
		}
	}
	
	
	private void update(final int pos)
	{
		updateField(pos);
		updateTimeStep(pos);
		updateLogPresenter(pos);
		botOverviewPanel.onNewAIInfoFrame(getCurrentRecordFrame(pos));
		
		if (firstUpdate)
		{
			fieldPanel.onOptionChanged(EVisualizerOptions.RESET_FIELD, true);
			firstUpdate = false;
		}
	}
	
	
	/**
	 * Get the current image and paint it to the field panel
	 */
	private void updateField(final int position)
	{
		if (persistance != null)
		{
			fetcher.newPosition(position);
		}
		IRecordWfFrame wf = getCurrentRecordFrame(position).getWorldFrame();
		IBotIDMap<TrackedTigerBot> bots = new BotIDMap<TrackedTigerBot>();
		for (TrackedTigerBot bot : wf.getTigerBotsVisible().values())
		{
			bots.put(bot.getId(), bot);
		}
		for (TrackedTigerBot bot : wf.getFoeBots().values())
		{
			bots.put(bot.getId(), bot);
		}
		WorldFramePrediction wfp = new FieldPredictor(bots.values(), wf.getBall()).create();
		if (wf.getTeamColor() == ETeamColor.YELLOW)
		{
			SimpleWorldFrame swf = new SimpleWorldFrame(bots, wf.getBall(), 0, 0, 0, wfp);
			if (wf.isInverted())
			{
				swf = swf.mirrorNew();
			}
			fieldPanel.updateWFrame(swf);
		}
		fieldPanel.updateAiFrame(getCurrentRecordFrame(position));
	}
	
	
	/**
	 * 
	 */
	private void updateLogPresenter(final int position)
	{
		Date systime = getCurrentRecordFrame(position).getWorldFrame().getSystemTime();
		long timeStamp = systime.getTime();
		logPresenter.clearEventStorage();
		
		if (logEventBuffer == null)
		{
			if (persistance != null)
			{
				logEventBuffer = persistance.loadLogEvents();
			} else
			{
				logEventBuffer = new ArrayList<BerkeleyLogEvent>(0);
			}
		}
		
		boolean appendLine = true;
		for (BerkeleyLogEvent event : logEventBuffer)
		{
			if ((event.getTimeStamp() >= (timeStamp - LOG_BUFFER_BEFORE))
					&& (event.getTimeStamp() <= (timeStamp + LOG_BUFFER_AFTER)))
			{
				if (appendLine && (event.getTimeStamp() >= timeStamp))
				{
					appendLine = false;
					logPresenter.appendLine();
				}
				logPresenter.append(event.getLoggingEvent());
			}
		}
	}
	
	
	private IRecordFrame getCurrentRecordFrame(final int position)
	{
		int pos = position - currentStart;
		if ((pos < 0) || (pos >= aiFrameBuffer.size()))
		{
			log.error("invalid position-currentStart combination. " + position + " " + currentStart + " " + pos);
		}
		return aiFrameBuffer.get(position - currentStart);
	}
	
	
	/**
	 * get the current TimeStep
	 */
	private void updateTimeStep(final int position)
	{
		SimpleDateFormat timeF = new SimpleDateFormat("HH:mm:ss,SSS");
		Date systime = getCurrentRecordFrame(position).getWorldFrame().getSystemTime();
		String timestep = timeF.format(systime);
		timeStepLabel.setText(timestep);
		
	}
	
	
	@Override
	public void onSlower()
	{
		speed--;
	}
	
	
	@Override
	public void onFaster()
	{
		speed++;
	}
	
	
	@Override
	public void onPlayStateChanged(final boolean playing)
	{
		this.playing = playing;
	}
	
	
	@Override
	public void onPositionChanged(final int position)
	{
		if ((position >= 0) && (position < maxFrames))
		{
			this.position = position;
			timePositionLock = System.nanoTime();
		}
	}
	
	private class Fetcher implements Runnable
	{
		private boolean	fetching	= false;
		private int			pos		= 0;
		
		
		@Override
		public void run()
		{
			fetch(pos);
		}
		
		
		public void fetch(final int pos)
		{
			int start = pos - (BUFFER_SIZE / 2);
			if (start <= 0)
			{
				start = 0;
			}
			List<IRecordFrame> frames = persistance.load(start, BUFFER_SIZE);
			log.debug("New start: " + start);
			synchronized (frameBufferLock)
			{
				currentStart = start;
				aiFrameBuffer = frames;
			}
			fetching = false;
		}
		
		
		/**
		 * @param position
		 */
		public void newPosition(final int position)
		{
			pos = position;
			if (((position - currentStart) >= BUFFER_SIZE) || ((position - currentStart) < 0))
			{
				// position is out of range, fetch and block
				fetch(position);
			}
			
			if (!fetching)
			{
				int upper = (currentStart + BUFFER_SIZE) - FETCH_THRESHOLD;
				int lower = currentStart + FETCH_THRESHOLD;
				if (lower < (BUFFER_SIZE / 2))
				{
					lower = 0;
				}
				if (((position > upper) || (position < lower)))
				{
					startFetch();
				}
			}
		}
		
		
		private void startFetch()
		{
			// reload in background
			fetching = true;
			executor.execute(fetcher);
		}
	}
	
	private class MyWindowListener implements WindowListener
	{
		
		@Override
		public void windowOpened(final WindowEvent e)
		{
		}
		
		
		@Override
		public void windowClosing(final WindowEvent e)
		{
			playing = false;
			executor.shutdown();
			try
			{
				executor.awaitTermination(5, TimeUnit.SECONDS);
			} catch (InterruptedException err)
			{
				log.error("Interrupted while waiting for refresh executer", err);
			}
			if (persistance != null)
			{
				persistance.close();
			}
			synchronized (frameBufferLock)
			{
				for (IRecordFrame rf : aiFrameBuffer)
				{
					rf.cleanUp();
				}
				aiFrameBuffer.clear();
			}
		}
		
		
		@Override
		public void windowClosed(final WindowEvent e)
		{
		}
		
		
		@Override
		public void windowIconified(final WindowEvent e)
		{
		}
		
		
		@Override
		public void windowDeiconified(final WindowEvent e)
		{
		}
		
		
		@Override
		public void windowActivated(final WindowEvent e)
		{
		}
		
		
		@Override
		public void windowDeactivated(final WindowEvent e)
		{
		}
	}
	
	private class FieldPanelComponentListener implements ComponentListener
	{
		
		@Override
		public void componentResized(final ComponentEvent e)
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					update(position);
				}
			});
		}
		
		
		@Override
		public void componentMoved(final ComponentEvent e)
		{
		}
		
		
		@Override
		public void componentShown(final ComponentEvent e)
		{
		}
		
		
		@Override
		public void componentHidden(final ComponentEvent e)
		{
		}
	}
	
	
	@Override
	public void onSetSpeed(final int speed)
	{
		this.speed = speed;
	}
	
	
	@Override
	public void onChangeRelPos(final int relPos)
	{
		onPositionChanged(position + relPos);
	}
}
