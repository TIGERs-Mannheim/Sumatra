/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.view.log;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import edu.tigers.sumatra.presenter.log.LogPresenter;
import org.apache.log4j.Priority;

import edu.tigers.sumatra.view.TextPane;
import edu.tigers.sumatra.views.ISumatraView;
import net.miginfocom.swing.MigLayout;


/**
 * ( @see {@link LogPresenter})
 * 
 * @author AndreR
 */
public class LogPanel extends JPanel implements ISumatraView
{
	private static final long	serialVersionUID	= 1L;
	
	private static final int	INIT_DIVIDER_LOC	= 150;
	
	
	private final TextPane		textPane;
	private final FilterPanel	filterPanel;
	private final TreePanel		treePanel;
	
	
	/**
	 * @param maxCapacity
	 * @param initialLevel
	 */
	public LogPanel(final int maxCapacity, final Priority initialLevel)
	{
		setLayout(new MigLayout("fill, inset 0", "", ""));
		
		textPane = new TextPane(maxCapacity);
		filterPanel = new FilterPanel(initialLevel);
		treePanel = new TreePanel();
		
		final JPanel filter = new JPanel(new MigLayout("fill", "", ""));
		filter.add(treePanel, "push, grow, wrap");
		filter.setMinimumSize(new Dimension(0, 0));
		
		final JPanel display = new JPanel(new MigLayout("fill", "", ""));
		display.add(textPane, "push, grow, wrap");
		display.add(filterPanel, "growx");
		
		final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, filter, display);
		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerLocation(0);
		splitPane.setLastDividerLocation(INIT_DIVIDER_LOC);
		splitPane.setBorder(BorderFactory.createEmptyBorder());
		
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
		return filterPanel.getSlidePanel();
	}
	
	
	/**
	 * @return
	 */
	public TreePanel getTreePanel()
	{
		return treePanel;
	}
	
	
	@Override
	public List<JMenu> getCustomMenus()
	{
		final ArrayList<JMenu> customMenu = new ArrayList<JMenu>();
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
}
