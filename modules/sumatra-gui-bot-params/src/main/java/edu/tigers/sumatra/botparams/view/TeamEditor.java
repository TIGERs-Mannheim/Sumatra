/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.botparams.view;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.tigers.sumatra.bot.params.BotParams;
import edu.tigers.sumatra.botparams.BotParamsDatabase;
import edu.tigers.sumatra.botparams.EBotParamLabel;
import edu.tigers.sumatra.botparams.view.ConfigJSONTreeTableModel.TreeEntry;
import edu.tigers.sumatra.components.DescLabel;
import edu.tigers.sumatra.treetable.ITreeTableModel;
import edu.tigers.sumatra.treetable.TreeTableModelAdapter;
import lombok.extern.log4j.Log4j2;
import net.miginfocom.swing.MigLayout;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serial;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;


/**
 * Editor for bot params for different teams.
 */
@Log4j2
public class TeamEditor extends JPanel
{
	@Serial
	private static final long serialVersionUID = -6385185638076083401L;

	private final ObjectMapper mapper = new ObjectMapper();
	private final transient List<ITeamEditorObserver> observers = new CopyOnWriteArrayList<>();
	private final JScrollPane scrollPane = new JScrollPane();

	private JTreeTableJson treetable;

	private final EnumMap<EBotParamLabel, JComboBox<String>> selectedLabels = new EnumMap<>(EBotParamLabel.class);


	public TeamEditor()
	{
		setLayout(new MigLayout("wrap 2", "[grow, fill]10[grow, fill]", ""));

		DescLabel desc = new DescLabel("Select different bot configurations for the teams and for curtain bots and" +
				"services. You can hover over the drop-down to get a minimal description.");
		add(desc, "wrap, span 2");

		for (EBotParamLabel paramLabel : EBotParamLabel.values())
		{
			JLabel name = new JLabel(paramLabel.toString());
			add(name);

			JComboBox<String> dropdown = new JComboBox<>();
			dropdown.addActionListener(new SelectedLabelListener(paramLabel, dropdown));
			dropdown.setToolTipText(paramLabel.getLabel());
			add(dropdown);
			selectedLabels.put(paramLabel, dropdown);

		}

		JButton addBtn = new JButton("Add Team");
		addBtn.addActionListener(new AddButtonListener());
		add(addBtn);

		JButton delBtn = new JButton("Delete Team");
		delBtn.addActionListener(new DeleteButtonListener());
		add(delBtn);

		// Setup lower part: The actual editor
		scrollPane.setPreferredSize(new Dimension(400, 1000));
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		add(scrollPane, "span 2, pushy");

		add(Box.createGlue(), "push, span 2");
	}


	/**
	 * Set a new database to display.
	 *
	 * @param database
	 */
	public void setDatabase(final BotParamsDatabase database)
	{
		Map<String, BotParams> unsortedTeams = database.getEntries();
		final Map<String, BotParams> teams = unsortedTeams.entrySet().stream()
				.sorted(Map.Entry.comparingByKey(new FavoredStringComparator("tiger")))
				.collect(Collectors.toMap(
						Map.Entry::getKey,
						Map.Entry::getValue,
						(e1, e2) -> e1,
						LinkedHashMap::new));

		JsonNode entries = mapper.valueToTree(teams);

		ITreeTableModel model = new ConfigJSONTreeTableModel(entries);
		model.setEditable(true);
		treetable = new JTreeTableJson(model);
		treetable.getModel().addTableModelListener(new TreeTableListener());

		EventQueue.invokeLater(() -> {
			scrollPane.setViewportView(treetable);

			for (Entry<EBotParamLabel, JComboBox<String>> entry : selectedLabels.entrySet())
			{
				JComboBox<String> box = entry.getValue();

				box.removeAllItems();
				String selection = database.getSelectedParams().get(entry.getKey());

				for (String teamName : teams.keySet())
				{
					box.addItem(teamName);

					if (teamName.equals(selection))
					{
						box.setSelectedIndex(box.getItemCount() - 1);
					}
				}
			}
		});
	}


	/**
	 * Select a new team for a specific label.
	 *
	 * @param label
	 * @param newTeam
	 */
	public void setSelectedTeamForLabel(final EBotParamLabel label, final String newTeam)
	{
		JComboBox<String> box = selectedLabels.get(label);

		if (box.getSelectedItem() == null || box.getSelectedItem().equals(newTeam))
		{
			return;
		}

		for (int index = 0; index < box.getItemCount(); index++)
		{
			if (box.getItemAt(index).equals(newTeam))
			{
				box.setSelectedIndex(index);
				break;
			}
		}
	}


	/**
	 * Clear all information from this panel.
	 */
	public void clear()
	{
		EventQueue.invokeLater(() -> {
			scrollPane.setViewportView(new JPanel());

			for (JComboBox<String> box : selectedLabels.values())
			{
				box.removeAllItems();
			}
		});
	}


	/**
	 * @param observer
	 */
	public void addObserver(final ITeamEditorObserver observer)
	{
		observers.add(observer);
	}


	/**
	 * @param observer
	 */
	public void removeObserver(final ITeamEditorObserver observer)
	{
		observers.remove(observer);
	}


	/**
	 * Observer interface.
	 */
	public interface ITeamEditorObserver
	{
		/**
		 * Bot params for a team updated.
		 *
		 * @param teamName
		 * @param newParams
		 */
		void onTeamUpdated(String teamName, BotParams newParams);


		/**
		 * Selected a new team for a specific label.
		 *
		 * @param label
		 * @param teamName
		 */
		void onTeamSelectedForLabel(EBotParamLabel label, String teamName);


		/**
		 * New team added.
		 *
		 * @param teamName
		 */
		void onTeamAdded(String teamName);


		/**
		 * Team deleted.
		 *
		 * @param teamName
		 */
		void onTeamDeleted(String teamName);
	}

	private class SelectedLabelListener implements ActionListener
	{
		private final EBotParamLabel label;
		private final JComboBox<String> box;


		/**
		 * @param label
		 * @param box
		 */
		public SelectedLabelListener(final EBotParamLabel label, final JComboBox<String> box)
		{
			this.label = label;
			this.box = box;
		}


		@Override
		public void actionPerformed(final ActionEvent event)
		{
			String teamName = (String) box.getSelectedItem();
			if (teamName != null)
			{
				notifyTeamSelectedForLabel(label, teamName);
			}
		}


		private void notifyTeamSelectedForLabel(final EBotParamLabel label, final String teamName)
		{
			for (ITeamEditorObserver observer : observers)
			{
				observer.onTeamSelectedForLabel(label, teamName);
			}
		}
	}

	private class TreeTableListener implements TableModelListener
	{
		@Override
		public void tableChanged(final TableModelEvent event)
		{
			if ((event.getType() == TableModelEvent.UPDATE) && (event.getFirstRow() == event.getLastRow()))
			{
				TreeTableModelAdapter adapter = (TreeTableModelAdapter) event.getSource();
				TreeEntry entry = (TreeEntry) adapter.getNodeForRow(event.getFirstRow());

				// go up to the "team" node of the tree
				while (entry.getParent().getParent() != null)
				{
					entry = entry.getParent();
				}

				BotParams newParams = null;
				try
				{
					newParams = mapper.treeToValue(entry.getNode(), BotParams.class);
				} catch (JsonProcessingException e)
				{
					log.error("Could not convert team node to BotParams class", e);
				}

				notifyTeamUpdated(entry.getName(), newParams);
			}
		}


		private void notifyTeamUpdated(final String teamName, final BotParams newParams)
		{
			for (ITeamEditorObserver observer : observers)
			{
				observer.onTeamUpdated(teamName, newParams);
			}
		}
	}

	private class AddButtonListener implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent arg0)
		{
			String teamName = (String) JOptionPane.showInputDialog(null, "Team name: ", "Specify new team name",
					JOptionPane.QUESTION_MESSAGE, null,
					null, null);

			if (teamName == null)
			{
				// user cancelled dialog
				return;
			}

			notifyTeamAdded(teamName);
		}


		private void notifyTeamAdded(final String teamName)
		{
			for (ITeamEditorObserver observer : observers)
			{
				observer.onTeamAdded(teamName);
			}
		}
	}

	private class DeleteButtonListener implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent e)
		{
			if (treetable == null)
			{
				return;
			}

			TreeTableModelAdapter model = (TreeTableModelAdapter) treetable.getModel();
			int selectedRow = treetable.getSelectedRow();
			if (selectedRow < 0)
			{
				// no team selected
				return;
			}
			TreeEntry entry = (TreeEntry) model.getNodeForRow(selectedRow);

			// go up to the "team" node of the tree
			while (entry.getParent().getParent() != null)
			{
				entry = entry.getParent();
			}

			String teamName = entry.getName();
			List<EBotParamLabel> usedAtLabel = new LinkedList<>();

			// check if the team is used with a label
			for (Entry<EBotParamLabel, JComboBox<String>> label : selectedLabels.entrySet())
			{
				String selection = (String) label.getValue().getSelectedItem();

				if (teamName.equals(selection))
				{
					usedAtLabel.add(label.getKey());
				}
			}

			if (!usedAtLabel.isEmpty())
			{
				StringBuilder builder = new StringBuilder(
						"This team is used for the following labels and cannot be deleted:\n");
				usedAtLabel.forEach(l -> builder.append("- ").append(l).append("\n"));
				JOptionPane.showMessageDialog(null, builder.toString(), "Team in use", JOptionPane.WARNING_MESSAGE);
				return;
			}

			notifyTeamDeleted(teamName);
		}


		private void notifyTeamDeleted(final String teamName)
		{
			for (ITeamEditorObserver observer : observers)
			{
				observer.onTeamDeleted(teamName);
			}
		}
	}

	/**
	 * This comparator simply sorts strings but it puts preference to those containing a favor.
	 */
	private static class FavoredStringComparator implements Comparator<String>
	{
		private final String favored;


		/**
		 * Constructor
		 *
		 * @param favor
		 */
		public FavoredStringComparator(final String favor)
		{
			favored = favor;
		}


		@Override
		public int compare(final String a, final String b)
		{
			if (a.toLowerCase().contains(favored) && !b.toLowerCase().contains(favored))
			{
				return -1;
			}

			if (!a.toLowerCase().contains(favored) && b.toLowerCase().contains(favored))
			{
				return 1;
			}

			return a.compareTo(b);
		}
	}
}
