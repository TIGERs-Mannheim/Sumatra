/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.config;

import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.treetable.ITreeTableModel;
import edu.tigers.sumatra.treetable.JTreeTable;
import edu.tigers.sumatra.treetable.NodeNameAndObjectTreePath;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.tree.ConfigurationNode;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * A tab for one config in the ConfigEditor. It provides a tree-view of the xml-config with editing support and the
 * possibilities to save, load and switch config.
 */
public class EditorView extends JPanel
{
	private static final long serialVersionUID = -7098099480668190062L;

	private static final boolean DISABLE_APPLY = false;
	private final String configKey;
	private final JTreeTable treetable;
	private final Action applyAction;
	private final Action saveAction;
	private final List<IConfigEditorViewObserver> observers = new CopyOnWriteArrayList<>();
	private boolean wasLoaded = false;
	private transient ITreeTableModel model;
	private transient HierarchicalConfiguration referenceConfig;
	private boolean listenForExpansionEvents;


	public EditorView(final String title, final String configKey, final HierarchicalConfiguration config)
	{
		super();
		this.configKey = configKey;

		// Setup panel
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

		// Setup upper part: Controls
		JPanel controls = new JPanel();
		controls.setLayout(new BoxLayout(controls, BoxLayout.LINE_AXIS));
		controls.setBorder(BorderFactory.createTitledBorder(title));
		controls.add(Box.createHorizontalGlue());
		add(controls, "grow, top");

		// Controls: Apply, Save, Switch, Reload
		// Apply
		applyAction = new AbstractAction("Apply")
		{
			private static final long serialVersionUID = 1L;


			@Override
			public void actionPerformed(final ActionEvent e)
			{
				apply();
			}
		};
		applyAction.setEnabled(!DISABLE_APPLY);
		JButton applyBtn = new JButton(applyAction);
		applyBtn.setToolTipText("Write current config to fields");
		controls.add(applyBtn);
		controls.add(Box.createHorizontalGlue());

		saveAction = new AbstractAction("Save")
		{
			private static final long serialVersionUID = 1L;


			@Override
			public void actionPerformed(final ActionEvent e)
			{
				save();
			}
		};
		saveAction.setEnabled(true);
		JButton saveBtn = new JButton(saveAction);
		saveBtn.setToolTipText("Save current config to file");
		controls.add(saveBtn);
		controls.add(Box.createHorizontalGlue());

		Action reloadAction = new AbstractAction("Reload")
		{
			private static final long serialVersionUID = 1L;


			@Override
			public void actionPerformed(final ActionEvent e)
			{
				reload();
			}
		};
		reloadAction.setEnabled(true);
		JButton reloadBtn = new JButton(reloadAction);
		reloadBtn.setToolTipText("Load config by reading from file");
		controls.add(reloadBtn);
		controls.add(Box.createHorizontalGlue());

		controls.add(Box.createHorizontalGlue());

		// Setup lower part: The actual editor
		JScrollPane scrollpane = new JScrollPane();
		scrollpane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollpane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		add(scrollpane, "grow, top");


		// Finally: Add model
		referenceConfig = config;
		model = new ConfigXMLTreeTableModel(config);
		treetable = new JTreeTable(model);
		treetable.getModel().addTableModelListener(event -> {
			if ((event.getType() == TableModelEvent.UPDATE) && (event.getFirstRow() == event.getLastRow()))
			{
				markDirty();
			}
		});
		listenForExpansionEvents = false;
		treetable.getTree().addTreeExpansionListener(new MyTreeExpansionListener());
		scrollpane.add(treetable);
		scrollpane.setViewportView(treetable);
	}


	/**
	 * @param model
	 */
	public void updateModel(ConfigXMLTreeTableModel model, boolean modelIsFiltered)
	{
		this.model = model;
		treetable.setTreeTableModel(model);
		treetable.getTree().addTreeExpansionListener(new MyTreeExpansionListener());
		treetable.getModel().addTableModelListener(event -> {
			if ((event.getType() == TableModelEvent.UPDATE) && (event.getFirstRow() == event.getLastRow()))
			{
				markDirty();
			}
		});
		if (modelIsFiltered)
		{
			listenForExpansionEvents = false;
			model.getAllTreePaths()
					.forEach(pair -> treetable.getTree().expandPath(pair.objectPath()));
		} else
		{
			listenForExpansionEvents = false;
			model.getAllTreePaths().forEach(this::applyExpansions);
			listenForExpansionEvents = true;
		}
	}


	private void applyExpansions(NodeNameAndObjectTreePath pair)
	{
		var expanded = SumatraModel.getInstance()
				.getUserProperty(EditorView.class, pair.nodeNamePath().toString(), false);
		if (expanded)
		{
			treetable.getTree().expandPath(pair.objectPath());
		}
	}


	private void markDirty()
	{
		// Enable Buttons only
		applyAction.setEnabled(true);
		saveAction.setEnabled(true);
	}


	private void save()
	{
		// Save
		notifySavePressed(configKey);

		// Mark
		saveAction.setEnabled(true);
	}


	public void initialReload()
	{
		if (wasLoaded)
			return;

		reload();
	}


	private void reload()
	{
		notifyReloadPressed(configKey);
		wasLoaded = true;
	}


	private void apply()
	{
		// Apply
		notifyApplyPressed(configKey);

		// Re-enable btn
		applyAction.setEnabled(!DISABLE_APPLY);
	}


	/**
	 * @param newObserver
	 */
	public void addObserver(final IConfigEditorViewObserver newObserver)
	{
		observers.add(newObserver);
	}


	/**
	 * @param oldObserver
	 * @return
	 */
	public boolean removeObserver(final IConfigEditorViewObserver oldObserver)
	{
		return observers.remove(oldObserver);
	}


	private void notifyApplyPressed(final String configKey)
	{
		for (final IConfigEditorViewObserver observer : observers)
		{
			observer.onApplyPressed(configKey);
		}
	}


	private boolean notifySavePressed(final String configKey)
	{
		boolean result = true;
		for (final IConfigEditorViewObserver observer : observers)
		{
			// If any returns false (e.g., save fails): return false
			result &= observer.onSavePressed(configKey);
		}
		return result;
	}


	private void notifyReloadPressed(final String configKey)
	{
		for (final IConfigEditorViewObserver observer : observers)
		{
			observer.onReloadPressed(configKey);
		}
	}


	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------


	/**
	 * @return
	 */
	public String getConfigKey()
	{
		return configKey;
	}

	public void setReferenceConfig(HierarchicalConfiguration config)
	{
		referenceConfig = config;
	}

	public HierarchicalConfiguration getReferenceConfig()
	{
		return referenceConfig;
	}

	private class MyTreeExpansionListener implements TreeExpansionListener
	{
		@Override
		public void treeCollapsed(TreeExpansionEvent event)
		{
			if (!listenForExpansionEvents)
			{
				return;
			}
			var obj = event.getPath().getLastPathComponent();
			if (obj instanceof ConfigurationNode node)
			{
				iterateChildrenToCollapse(node);
			}
			var path = model.getNodeNameTreePathFromObjectTreePath(event.getPath());
			path.ifPresent(p -> SumatraModel.getInstance().setUserProperty(EditorView.class, p.toString(), null));
		}


		private void iterateChildrenToCollapse(ConfigurationNode node)
		{
			for (var child : node.getChildren())
			{
				var path = model.getNodeNameTreePathToRoot(child);
				path.ifPresent(p -> SumatraModel.getInstance().setUserProperty(EditorView.class, p.toString(), null));
				iterateChildrenToCollapse(child);
			}
		}


		@Override
		public void treeExpanded(TreeExpansionEvent event)
		{
			if (!listenForExpansionEvents)
			{
				return;
			}
			var path = model.getNodeNameTreePathFromObjectTreePath(event.getPath());
			path.ifPresent(p -> SumatraModel.getInstance().setUserProperty(EditorView.class, p.toString(), true));
		}


	}

}
