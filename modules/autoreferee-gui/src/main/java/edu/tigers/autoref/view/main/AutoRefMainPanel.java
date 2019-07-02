/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoref.view.main;

import java.awt.BorderLayout;
import java.util.Arrays;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import edu.tigers.autoreferee.engine.detector.EGameEventDetectorType;
import edu.tigers.sumatra.components.BetterScrollPane;
import edu.tigers.sumatra.components.EnumCheckBoxPanel;
import edu.tigers.sumatra.views.ISumatraView;
import net.miginfocom.swing.MigLayout;


public class AutoRefMainPanel extends JPanel implements ISumatraView
{
	private StartStopPanel startStopPanel = new StartStopPanel();
	private EnumCheckBoxPanel<EGameEventDetectorType> gameEventDetectorPanel;
	
	
	public AutoRefMainPanel()
	{
		gameEventDetectorPanel = new EnumCheckBoxPanel<>(EGameEventDetectorType.class, "Game Event Detectors",
				BoxLayout.PAGE_AXIS);
		gameEventDetectorPanel.addToggleAllButton();
		
		setLayout(new BorderLayout());
		
		add(startStopPanel, BorderLayout.NORTH);
		
		JPanel panel = new JPanel();
		final BetterScrollPane scrollPane = new BetterScrollPane(panel);
		add(scrollPane, BorderLayout.CENTER);
		
		panel.setLayout(new MigLayout("", "", ""));
		panel.add(gameEventDetectorPanel, "grow x, top");
	}
	
	
	public StartStopPanel getStartStopPanel()
	{
		return startStopPanel;
	}
	
	
	public EnumCheckBoxPanel<EGameEventDetectorType> getGameEventDetectorPanel()
	{
		return gameEventDetectorPanel;
	}
	
	
	@Override
	public void setEnabled(final boolean enabled)
	{
		Arrays.asList(getComponents()).forEach(c -> c.setEnabled(enabled));
	}
}
