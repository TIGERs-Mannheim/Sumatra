package edu.dhbw.mannheim.tigers.sumatra.view.log.internals;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Level;
import org.apache.log4j.lf5.LogLevel;

/**
 * 
 * *********************************************************
 * Copyright (c) 2010 DHBW Mannheim - Tigers Mannheim
 * Project: tigers - central software - ai
 * Date: 17.05.2010
 * Author(s): MichaelS, AndreR
 * 
 * Set Level for Log4j.
 *
 * *********************************************************
 */
public class SlidePanel extends JPanel
{
	// class variables
	private static final long serialVersionUID = 1L;
	
	ArrayList<ISlidePanelObserver> observers = new ArrayList<ISlidePanelObserver>();
	
	// object variables
	private JSlider slider = null;
	
	public SlidePanel()
	{		
		Hashtable<Integer, JLabel> levelTable = new Hashtable<Integer, JLabel>();

		setLayout(new BorderLayout());		
		
		@SuppressWarnings("unchecked")
		List<LogLevel> levels = LogLevel.getLog4JLevels();
		for(int i=0; i<levels.size();i++)
		{
			levelTable.put(i, new JLabel(levels.get(i).getLabel()));
		}
		
		slider = new JSlider(JSlider.HORIZONTAL, 0, levels.size()-1, levels.size()-1);
		slider.setPaintTicks(true);
		slider.setPaintLabels(true);
		slider.setSnapToTicks(true);
		slider.setMajorTickSpacing(1);		
		slider.setLabelTable(levelTable);
		slider.addChangeListener(new LevelChanged());
		
		add(slider, BorderLayout.CENTER);
	}
	
	public void addObserver(ISlidePanelObserver o)
	{
		observers.add(o);
	}
	
	public void removeObserver(ISlidePanelObserver o)
	{
		observers.remove(o);
	}
	
	protected class LevelChanged implements ChangeListener
	{
		@Override
		public void stateChanged(ChangeEvent e)
		{
			int level = slider.getValue();
			LogLevel logLevel = (LogLevel)LogLevel.getLog4JLevels().get(level);

			for(ISlidePanelObserver o : observers)
			{
				o.onLevelChanged(Level.toLevel(logLevel.getLabel()));
			}
		}
	}
}
