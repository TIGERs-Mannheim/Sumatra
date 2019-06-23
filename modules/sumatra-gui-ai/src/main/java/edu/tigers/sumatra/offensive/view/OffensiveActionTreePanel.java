/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.offensive.view;

import java.awt.*;
import java.util.Map;

import javax.swing.*;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.ai.metis.offense.action.OffensiveActionTree;
import edu.tigers.sumatra.ai.metis.offense.action.OffensiveActionTreePath;
import edu.tigers.sumatra.ai.metis.offense.action.situation.EOffensiveSituation;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.views.ISumatraView;
import net.miginfocom.swing.MigLayout;


/**
 * Main Panel for OffensiveActionTree
 *
 * @author Marius Messerschmidt <marius.messerschmidt@dlr.de>
 */
public class OffensiveActionTreePanel extends JPanel implements ISumatraView
{
	protected static final Logger log = Logger.getLogger(OffensiveActionTreePanel.class.getName());
	
	private JRadioButton teamYRadio = new JRadioButton("Yellow");
	private JRadioButton teamBRadio = new JRadioButton("Blue");
	
	private JComboBox<String> treeSelector = new JComboBox<>();
	
	private TreeDrawerPanel treeView = new TreeDrawerPanel();
	
	private JLabel outdatedLabel = new JLabel("Outdated Data");
	
	private OffensiveActionTreePath currentPathYellow = null;
	private OffensiveActionTreePath currentPathBlue = null;
	
	private transient Map<EOffensiveSituation, OffensiveActionTree> teamTreesYellow;
	private transient Map<EOffensiveSituation, OffensiveActionTree> teamTreesBlue;
	
	
	public OffensiveActionTreePanel()
	{
		setLayout(new MigLayout());
		
		for (EOffensiveSituation s : EOffensiveSituation.values())
		{
			treeSelector.addItem(s.name());
		}
		treeSelector.addActionListener(e -> selectTree((String) treeSelector.getSelectedItem(), true));
		
		outdatedLabel.setVisible(false);
		Font f = outdatedLabel.getFont();
		outdatedLabel.setFont(new Font(f.getName(), Font.BOLD, 14));
		
		teamYRadio.setSelected(true);
		teamYRadio.addActionListener(e -> toggleTeam(ETeamColor.YELLOW));
		teamBRadio.addActionListener(e -> toggleTeam(ETeamColor.BLUE));
		
		
		add(new Label("Action Trees for Team: "));
		add(teamYRadio);
		add(teamBRadio, "wrap");
		add(outdatedLabel);
		add(new Label("Tree: "), "span 1");
		add(treeSelector, "wrap");
		add(treeView, "dock center, span 3, wrap");
	}
	
	
	private void toggleTeam(ETeamColor c)
	{
		if (c == ETeamColor.YELLOW)
		{
			teamBRadio.setSelected(false);
			teamYRadio.setSelected(true);
		} else
		{
			teamYRadio.setSelected(false);
			teamBRadio.setSelected(true);
		}
		
		selectTree((String) treeSelector.getSelectedItem(), true);
	}
	
	
	private void selectTree(String name, boolean force)
	{
		EOffensiveSituation sit;
		OffensiveActionTree t = null;
		OffensiveActionTreePath path = null;
		
		try
		{
			sit = EOffensiveSituation.valueOf(name);
		} catch (IllegalArgumentException e)
		{
			log.warn("Could not load OffensiveTreeView", e);
			return;
		}
		
		if (teamYRadio.isSelected() && teamTreesYellow != null)
		{
			t = teamTreesYellow.getOrDefault(sit, null);
			path = currentPathYellow;
		} else if (teamTreesBlue != null)
		{
			t = teamTreesBlue.getOrDefault(sit, null);
			path = currentPathBlue;
		}
		
		if (t == null && !force)
		{
			outdatedLabel.setVisible(true);
			return;
		}
		
		outdatedLabel.setVisible(false);
		treeView.setTree(t);
		treeView.setCurrentPath(path);
		
		if (force)
		{
			treeView.setOrigin(new Point(0, 0));
		}
	}
	
	
	public void setActionTree(ETeamColor team, Map<EOffensiveSituation, OffensiveActionTree> teamMap)
	{
		if (team == ETeamColor.BLUE)
			teamTreesBlue = teamMap;
		else if (team == ETeamColor.YELLOW)
			teamTreesYellow = teamMap;
		else
			throw new IllegalArgumentException("Team must either be YELLOW or BLUE");
		selectTree((String) treeSelector.getSelectedItem(), false);
		repaint();
	}
	
	
	public void setCurrentPath(ETeamColor team, final OffensiveActionTreePath currentPath)
	{
		if (team == ETeamColor.YELLOW)
			this.currentPathYellow = currentPath;
		else
			this.currentPathBlue = currentPath;
		
		selectTree((String) treeSelector.getSelectedItem(), false);
		repaint();
	}
}
