/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 14, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoref.view.humanref.components;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.EnumMap;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import edu.tigers.autoref.util.AutoRefImageRegistry;
import edu.tigers.sumatra.components.JImagePanel;
import edu.tigers.sumatra.components.ResizingLabel;
import edu.tigers.sumatra.ids.ETeamColor;


/**
 * @author "Lukas Magel"
 */
public class ScorePanel extends JPanel
{
	
	/**  */
	private static final long			serialVersionUID	= 3867769107337435973L;
	
	private static final int			INNER_SPACING		= 12;
	
	private JImagePanel					leftImagePanel		= new JImagePanel();
	private JImagePanel					rightImagePanel	= new JImagePanel();
	private ResizingLabel				leftTeamLabel		= new ResizingLabel(false);
	private ResizingLabel				rightTeamLabel		= new ResizingLabel(false);
	private JLabel							scoreLabel			= new JLabel();
	
	private Map<ETeamColor, String>	teamNames			= new EnumMap<>(ETeamColor.class);
	private Map<ETeamColor, Integer>	teamScores			= new EnumMap<>(ETeamColor.class);
	
	private ETeamColor					leftTeam				= ETeamColor.BLUE;
	
	
	/**
	 * @param teamFont
	 * @param scoreFont
	 */
	public ScorePanel(final Font teamFont, final Font scoreFont)
	{
		setupUI(teamFont, scoreFont);
		
		addComponentListener(new ComponentAdapter()
		{
			@Override
			public void componentResized(final ComponentEvent e)
			{
				ImageIcon cardImage = AutoRefImageRegistry.getTeamCard(ETeamColor.BLUE);
				double ratio = (double) cardImage.getIconWidth() / cardImage.getIconHeight();
				
				int height = (int) (scoreLabel.getSize().height * 0.8);
				int width = (int) (height * ratio);
				Dimension dim = new Dimension(width, height);
				leftImagePanel.setPreferredSize(dim);
				rightImagePanel.setPreferredSize(dim);
				
				revalidate();
			}
		});
	}
	
	
	private void setupUI(final Font teamFont, final Font scoreFont)
	{
		leftTeamLabel.setTargetFont(teamFont);
		leftTeamLabel.setAlignmentY(Component.CENTER_ALIGNMENT);
		rightTeamLabel.setTargetFont(teamFont);
		rightTeamLabel.setAlignmentY(Component.CENTER_ALIGNMENT);
		scoreLabel.setFont(scoreFont);
		
		setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		add(leftImagePanel);
		add(Box.createRigidArea(new Dimension(INNER_SPACING, 0)));
		add(leftTeamLabel);
		add(Box.createRigidArea(new Dimension(INNER_SPACING, 0)));
		add(scoreLabel);
		add(Box.createRigidArea(new Dimension(INNER_SPACING, 0)));
		add(rightTeamLabel);
		add(Box.createRigidArea(new Dimension(INNER_SPACING, 0)));
		add(rightImagePanel);
		
		update();
	}
	
	
	/**
	 * @param scores
	 */
	public void setTeamScores(final Map<ETeamColor, Integer> scores)
	{
		teamScores = scores;
		update();
	}
	
	
	/**
	 * @param names
	 */
	public void setTeamNames(final Map<ETeamColor, String> names)
	{
		teamNames = names;
		update();
	}
	
	
	/**
	 * @param color
	 */
	public void setLeftTeam(final ETeamColor color)
	{
		leftTeam = color;
	}
	
	
	private void update()
	{
		ETeamColor rightTeam = leftTeam.opposite();
		
		String leftTeamName = getTeamName(leftTeam);
		String rightTeamName = getTeamName(rightTeam);
		
		Integer leftTeamScore = teamScores.getOrDefault(leftTeam, 0);
		Integer rightTeamScore = teamScores.getOrDefault(rightTeam, 0);
		
		leftImagePanel.setImage(AutoRefImageRegistry.getTeamCard(leftTeam));
		rightImagePanel.setImage(AutoRefImageRegistry.getTeamCard(rightTeam));
		
		leftTeamLabel.setText(leftTeamName);
		rightTeamLabel.setText(rightTeamName);
		
		String scoreString = leftTeamScore + ":" + rightTeamScore;
		scoreLabel.setText(scoreString);
	}
	
	
	private String getTeamName(final ETeamColor color)
	{
		String name = teamNames.get(color);
		if ((name == null) || name.isEmpty())
		{
			name = color == ETeamColor.BLUE ? "Blue" : "Yellow";
		}
		return name;
	}
}
