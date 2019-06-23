/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 26, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.replay;

import java.awt.BorderLayout;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.airecord.IRecordFrame;
import edu.dhbw.mannheim.tigers.sumatra.presenter.visualizer.OptionsPanelPresenter;
import edu.dhbw.mannheim.tigers.sumatra.util.NamedThreadFactory;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.OptionsPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.FieldPanel;


/**
 * This is a dedicated window that holds a field and a control panel for replaying captured scenes
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class ReplayWindow extends JFrame implements IReplayControlPanelObserver
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**  */
	private static final long							serialVersionUID	= 1L;
	private static final Logger						log					= Logger.getLogger(ReplayWindow.class.getName());
	private static final int							INITIAL_SPEED		= 20;
	private static final int							SPEED_FACTOR		= 2;
	private final List<IReplayPositionObserver>	positionObservers	= new CopyOnWriteArrayList<IReplayPositionObserver>();
	
	private List<IRecordFrame>							aiFrameBuffer		= new LinkedList<IRecordFrame>();
	private final FieldPanel							fieldPanel;
	private JLabel											timeStepLabel		= new JLabel();
	
	private int												counter				= 0;
	private int												speed					= INITIAL_SPEED;
	private boolean										playing				= true;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * @param aiFrameBuffer
	 */
	public ReplayWindow(final List<IRecordFrame> aiFrameBuffer)
	{
		if ((aiFrameBuffer == null) || (aiFrameBuffer.isEmpty()))
		{
			throw new IllegalArgumentException("aiFrameBuffer must have at least one Graphics");
		}
		
		this.aiFrameBuffer = aiFrameBuffer;
		
		setTitle("Replay");
		setLayout(new BorderLayout());
		setIconImage(new ImageIcon(ClassLoader.getSystemResource("kralle-icon.png")).getImage());
		
		fieldPanel = new FieldPanel();
		fieldPanel.setPanelVisible(true);
		
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
		
		ReplayControlPanel controlPanel = new ReplayControlPanel(aiFrameBuffer.size());
		controlPanel.addObserver(this);
		addPositionObserver(controlPanel);
		
		JPanel timeStepPanel = createTimeStepPanel();
		optionsPanel.add(timeStepPanel);
		
		this.add(fieldPanel, BorderLayout.CENTER);
		this.add(optionsPanel, BorderLayout.EAST);
		this.add(controlPanel, BorderLayout.SOUTH);
		
		pack();
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * TODO Simon, add comment!
	 * 
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
		
		ExecutorService executor = Executors.newSingleThreadExecutor(new NamedThreadFactory("ReplayRefresh"));
		executor.execute(new RefreshThread());
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
	 * 
	 */
	private class RefreshThread implements Runnable
	{
		
		@Override
		public void run()
		{
			updateField();
			while (isVisible())
			{
				try
				{
					Thread.sleep(speed);
				} catch (InterruptedException err)
				{
					log.error("Error while trying to sleep");
					break;
				}
				
				if (playing)
				{
					counter = (counter + 1) % aiFrameBuffer.size();
					updateField();
					updateTimeStep();
				}
			}
			aiFrameBuffer.clear();
			dispose();
		}
		
	}
	
	
	/**
	 * Get the current image and paint it to the field panel
	 */
	private void updateField()
	{
		fieldPanel.setPaths(aiFrameBuffer.get(counter).getPaths());
		fieldPanel.drawRecordFrame(aiFrameBuffer.get(counter));
		for (IReplayPositionObserver o : positionObservers)
		{
			o.onPositionChanged(counter);
		}
		
	}
	
	
	/**
	 * get the current TimeStep
	 */
	private void updateTimeStep()
	{
		SimpleDateFormat timeF = new SimpleDateFormat("HH:mm:ss,SSS");
		Date systime = aiFrameBuffer.get(counter).getRecordWfFrame().getSystemTime();
		String timestep = timeF.format(systime);
		timeStepLabel.setText(timestep);
		
	}
	
	
	@Override
	public void onSlower()
	{
		speed *= SPEED_FACTOR;
	}
	
	
	@Override
	public void onFaster()
	{
		speed /= SPEED_FACTOR;
		if (speed < 1)
		{
			speed = 1;
		}
	}
	
	
	@Override
	public void onPlayStateChanged(boolean playing)
	{
		this.playing = playing;
	}
	
	
	@Override
	public void onPositionChanged(int position)
	{
		if ((position >= 0) && (position < aiFrameBuffer.size()))
		{
			counter = position;
			updateField();
			updateTimeStep();
		}
	}
	
}
