/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 06.08.2010
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.log;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import net.miginfocom.swing.MigLayout;

import org.apache.log4j.Priority;

import edu.dhbw.mannheim.tigers.sumatra.view.log.internals.FilterPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.log.internals.SlidePanel;
import edu.dhbw.mannheim.tigers.sumatra.view.log.internals.TextPane;
import edu.dhbw.mannheim.tigers.sumatra.view.log.internals.TreePanel;
import edu.dhbw.mannheim.tigers.sumatra.view.main.ISumatraView;


/**
 * ( @see {@link edu.dhbw.mannheim.tigers.sumatra.presenter.log.LogPresenter})
 * 
 * @author AndreR
 * 
 */
public class LogPanel extends JPanel implements ISumatraView
{
	private static final long		serialVersionUID	= 1L;
	
	private static final int		ID						= 1;
	private static final String	TITLE					= "Log";
	private static final int		INIT_DIVIDER_LOC	= 150;
	
	
	private final TextPane			textPane;
	private final FilterPanel		filterPanel;
	private final SlidePanel		slidePanel;
	private final TreePanel			treePanel;
	
	
	/**
	 * @param maxCapacity
	 * @param initialLevel
	 */
	public LogPanel(int maxCapacity, Priority initialLevel)
	{
		textPane = new TextPane(maxCapacity);
		filterPanel = new FilterPanel();
		slidePanel = new SlidePanel(initialLevel);
		treePanel = new TreePanel();
		
		setLayout(new MigLayout("fill", "", ""));
		
		final JPanel filter = new JPanel(new MigLayout("fill", "", ""));
		filter.add(treePanel, "push, grow, wrap");
		filter.add(slidePanel, "growx");
		filter.setMinimumSize(new Dimension(0, 0));
		
		final JPanel display = new JPanel(new MigLayout("fill", "", ""));
		display.add(textPane, "push, grow, wrap");
		display.add(filterPanel, "growx");
		
		final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, filter, display);
		splitPane.setDividerLocation(INIT_DIVIDER_LOC);
		
		add(splitPane, "grow");
	}
	
	
	/**
	 * @return
	 */
	public TextPane getTextPane()
	{
		return textPane;
	}
	
	
	/**
	 * @return
	 */
	public FilterPanel getFilterPanel()
	{
		return filterPanel;
	}
	
	
	/**
	 * @return
	 */
	public SlidePanel getSlidePanel()
	{
		return slidePanel;
	}
	
	
	/**
	 * @return
	 */
	public TreePanel getTreePanel()
	{
		return treePanel;
	}
	
	
	@Override
	public int getId()
	{
		return ID;
	}
	
	
	@Override
	public String getTitle()
	{
		return TITLE;
	}
	
	
	@Override
	public Component getViewComponent()
	{
		return this;
	}
	
	
	@Override
	public List<JMenu> getCustomMenus()
	{
		final ArrayList<JMenu> customMenu = new ArrayList<JMenu>();
		
		final JMenu logMenu = new JMenu("Log");
		final JCheckBoxMenuItem item = new JCheckBoxMenuItem("Autoscrolling");
		item.setSelected(true);
		item.addActionListener(new ChangeAutoscrolling());
		
		logMenu.add(item);
		customMenu.add(logMenu);
		
		return customMenu;
	}
	
	
	@Override
	public void onShown()
	{
		
	}
	
	
	@Override
	public void onHidden()
	{
		
	}
	
	
	@Override
	public void onFocused()
	{
		
	}
	
	
	@Override
	public void onFocusLost()
	{
		
	}
	
	protected class ChangeAutoscrolling implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			final JCheckBoxMenuItem item = (JCheckBoxMenuItem) e.getSource();
			textPane.setAutoscroll(item.isSelected());
		}
	}
}
