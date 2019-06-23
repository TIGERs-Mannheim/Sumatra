/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.view.log;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Level;
import org.apache.log4j.Priority;

import net.miginfocom.swing.MigLayout;


/**
 * Slider for choosing log level
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class SlidePanel extends JPanel
{
	// class variables
	private static final long serialVersionUID = 1L;
	
	private List<ISlidePanelObserver> observers = new CopyOnWriteArrayList<>();
	
	// object variables
	private JSlider slider = null;
	
	private static final List<Level> LOG_LEVELS = new ArrayList<>();
	
	static
	{
		LOG_LEVELS.add(Level.FATAL);
		LOG_LEVELS.add(Level.ERROR);
		LOG_LEVELS.add(Level.WARN);
		LOG_LEVELS.add(Level.INFO);
		LOG_LEVELS.add(Level.DEBUG);
		LOG_LEVELS.add(Level.TRACE);
	}
	
	
	/**
	 * @param initialLevel
	 */
	// Dictionary needed by JSlider
	@SuppressWarnings("squid:S1149")
	public SlidePanel(final Priority initialLevel)
	{
		final Dictionary<Integer, JLabel> levelTable = new Hashtable<>();
		
		setLayout(new MigLayout("fill, inset 0"));
		
		int initialLevelId = -1;
		for (int i = 0; i < LOG_LEVELS.size(); i++)
		{
			if (initialLevel.equals(LOG_LEVELS.get(i)))
			{
				initialLevelId = i;
			}
			levelTable.put(i, new JLabel(LOG_LEVELS.get(i).toString().substring(0, 1)));
		}
		if (initialLevelId == -1)
		{
			throw new IllegalStateException("Could not find default log level");
		}
		
		slider = new JSlider(SwingConstants.HORIZONTAL, 0, LOG_LEVELS.size() - 1, initialLevelId);
		slider.setSnapToTicks(true);
		slider.setMajorTickSpacing(1);
		slider.setLabelTable(levelTable);
		slider.addChangeListener(new LevelChanged());
		slider.setPreferredSize(new Dimension(130, 20));
		
		add(slider);
	}
	
	
	/**
	 * @param o
	 */
	public void addObserver(final ISlidePanelObserver o)
	{
		observers.add(o);
	}
	
	
	/**
	 * @param o
	 */
	public void removeObserver(final ISlidePanelObserver o)
	{
		observers.remove(o);
	}
	
	protected class LevelChanged implements ChangeListener
	{
		@Override
		public void stateChanged(final ChangeEvent e)
		{
			final int level = slider.getValue();
			final Level logLevel = LOG_LEVELS.get(level);
			for (final ISlidePanelObserver o : observers)
			{
				o.onLevelChanged(logLevel);
			}
		}
	}
}
