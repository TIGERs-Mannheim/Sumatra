/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 26, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoref.view.humanref;

import java.time.Duration;
import java.util.Map;

import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import edu.tigers.autoref.view.humanref.components.GameStatePanel;
import edu.tigers.autoref.view.humanref.components.ScorePanel;
import edu.tigers.autoref.view.humanref.components.TimePanel;
import edu.tigers.sumatra.Referee.SSL_Referee.Stage;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.wp.data.EGameStateNeutral;


/**
 * @author "Lukas Magel"
 */
public class BaseHumanRefPanel extends AHumanRefPanel
{
	
	/**  */
	private static final long	serialVersionUID	= -8134789041519501028L;
	
	private TimePanel				timePanel;
	private ScorePanel			goalPanel;
	
	private GameStatePanel		statePanel;
	
	
	/**
	 * 
	 */
	public BaseHumanRefPanel()
	{
		
	}
	
	
	@Override
	protected void setupGUI()
	{
		timePanel = new TimePanel(regularFont);
		goalPanel = new ScorePanel(smallFont, headerFont);
		statePanel = new GameStatePanel(headerFont);
		
		super.setupGUI();
	}
	
	
	@Override
	protected void fillVerticalLayout()
	{
		JPanel timeAndGoalsPanel = new JPanel(new MigLayout("fillx", "[]20[]"));
		timeAndGoalsPanel.add(timePanel, "alignx left");
		timeAndGoalsPanel.add(goalPanel, "alignx right");
		
		JPanel headerPanel = getHeaderPanel();
		headerPanel.setLayout(new MigLayout("fillx, ins 5", "[fill]"));
		headerPanel.add(timeAndGoalsPanel, "wrap 1%");
		headerPanel.add(statePanel, "center, wrap 2%");
	}
	
	
	@Override
	protected void fillHorizontalLayout()
	{
		JPanel headerPanel = getHeaderPanel();
		
		headerPanel.setLayout(new MigLayout("fill", "[left][fill][right]"));
		headerPanel.add(timePanel, "alignx left, aligny top");
		headerPanel.add(statePanel, "aligny center, alignx center");
		headerPanel.add(goalPanel, "alignx right, aligny top");
	}
	
	
	/**
	 * @param duration
	 */
	public void setTimeLeft(final Duration duration)
	{
		timePanel.setTimeLeft(duration);
	}
	
	
	/**
	 * @param stage
	 */
	public void setStage(final Stage stage)
	{
		timePanel.setStage(stage);
	}
	
	
	/**
	 * @param names
	 */
	public void setTeamNames(final Map<ETeamColor, String> names)
	{
		goalPanel.setTeamNames(names);
	}
	
	
	/**
	 * @param goals
	 */
	public void setGoals(final Map<ETeamColor, Integer> goals)
	{
		goalPanel.setTeamScores(goals);
	}
	
	
	/**
	 * @param state
	 */
	public void setState(final EGameStateNeutral state)
	{
		statePanel.setState(state);
	}
	
}
