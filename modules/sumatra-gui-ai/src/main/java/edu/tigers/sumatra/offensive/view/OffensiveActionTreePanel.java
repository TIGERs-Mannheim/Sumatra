/*
 * Copyright (c) 2009 - 2019, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.offensive.view;

import edu.tigers.sumatra.ai.metis.offense.action.situation.OffensiveActionTreePath;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.trees.EOffensiveSituation;
import edu.tigers.sumatra.trees.OffensiveActionTree;
import edu.tigers.sumatra.trees.OffensiveActionTreeMap;
import edu.tigers.sumatra.trees.OffensiveTreeProvider;
import edu.tigers.sumatra.views.ISumatraView;
import net.miginfocom.swing.MigLayout;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import java.awt.Font;
import java.awt.Label;
import java.awt.Point;
import java.io.File;
import java.util.Map;
import java.util.Optional;


/**
 * Main Panel for edu.tigers.sumatra.trees.OffensiveActionTree
 *
 * @author Marius Messerschmidt <marius.messerschmidt@dlr.de>
 */
public class OffensiveActionTreePanel extends JPanel implements ISumatraView
{
	protected static final Logger log = LogManager.getLogger(OffensiveActionTreePanel.class.getName());

	private JRadioButton teamYRadio = new JRadioButton("Yellow");
	private JRadioButton teamBRadio = new JRadioButton("Blue");

	private JComboBox<String> treeSelector = new JComboBox<>();

	private TreeDrawerPanel treeView = new TreeDrawerPanel();

	private JLabel outdatedLabel = new JLabel("Outdated Data");

	private OffensiveActionTreePath currentPathYellow = null;
	private OffensiveActionTreePath currentPathBlue = null;

	private transient Map<EOffensiveSituation, OffensiveActionTree> teamTreesYellow;
	private transient Map<EOffensiveSituation, OffensiveActionTree> teamTreesBlue;

	private JComboBox<String> filesCombo = new JComboBox<>();

	private static final String BASIC_TREE_PATH = "./config/offensive-trees/";


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

		JButton saveButton = new JButton("Save Tree as ...");
		saveButton.addActionListener(e -> {
			OffensiveActionTreeMap map;
			if (teamYRadio.isSelected())
			{
				map = new OffensiveActionTreeMap(teamTreesYellow);
			} else
			{
				map = new OffensiveActionTreeMap(teamTreesBlue);
			}
			String path = JOptionPane.showInputDialog("Save as: ");
			if (!path.isEmpty())
			{
				map.saveTreeDataToFile(BASIC_TREE_PATH + path + ".json");
			}
			fillComboBox();
		});

		add(saveButton);
		add(new JLabel("Load Tree: "));

		fillComboBox();
		add(filesCombo, "dock center");
		JButton loadSelectedTree = new JButton("Load selected Tree");
		add(loadSelectedTree, "wrap");

		loadSelectedTree.addActionListener(actionEvent -> selectTree());

		add(treeView, "dock center, span 4, wrap");
	}


	private void selectTree()
	{
		String path = BASIC_TREE_PATH + filesCombo.getSelectedItem();
		Optional<OffensiveActionTreeMap> map = OffensiveActionTreeMap.loadTreeDataFromFile(path);
		try
		{
			OffensiveTreeProvider treeProvider = SumatraModel.getInstance().getModule(OffensiveTreeProvider.class);
			if (!map.isPresent())
			{
				return;
			}
			if (teamYRadio.isSelected())
			{
				treeProvider.updateTree(map.get(), ETeamColor.YELLOW);
			} else
			{
				treeProvider.updateTree(map.get(), ETeamColor.BLUE);
			}
		} catch (Exception e)
		{
			log.warn("Can not load Tree model, Please start moduli first!", e);
		}
	}


	private void fillComboBox()
	{
		filesCombo.removeAllItems();
		File folder = new File(BASIC_TREE_PATH);
		File[] listOfFiles = folder.listFiles();
		if (listOfFiles != null)
		{
			for (File p : listOfFiles)
			{
				String[] path = p.getPath().split("/");
				String name = path[path.length - 1];
				filesCombo.addItem(name);
			}
		}
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
