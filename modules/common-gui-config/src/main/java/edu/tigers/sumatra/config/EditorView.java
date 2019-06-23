/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 27.11.2011
 * Author(s): Gero
 * *********************************************************
 */
package edu.tigers.sumatra.config;

import java.awt.event.ActionEvent;
import java.util.LinkedList;
import java.util.List;

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
import javax.swing.event.TableModelListener;

import org.apache.commons.configuration.HierarchicalConfiguration;

import edu.tigers.sumatra.treetable.ITreeTableModel;
import edu.tigers.sumatra.treetable.JTreeTable;


/**
 * A tab for one config in the ConfigEditor. It provides a tree-view of the xml-config with editing support and the
 * possibilities to save, load and switch config.
 * 
 * @author Gero
 */
public class EditorView extends JPanel
{
	// --------------------------------------------------------------------------
	// --- constants and variables ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long								serialVersionUID	= -7098099480668190062L;
																							
	private static final boolean							disableApply		= false;
																							
	private final String										configKey;
																	
																	
	private transient ITreeTableModel					model;
	private final JTreeTable								treetable;
																	
	private final Action										applyAction;
	private final Action										saveAction;
																	
	private final List<IConfigEditorViewObserver>	observers			= new LinkedList<IConfigEditorViewObserver>();
																							
																							
	// --------------------------------------------------------------------------
	// --- constructor ----------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param title
	 * @param configKey
	 * @param config
	 * @param editable
	 */
	public EditorView(final String title, final String configKey, final HierarchicalConfiguration config,
			final boolean editable)
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
		applyAction.setEnabled(!disableApply);
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
		
		Action reloadAction = new AbstractAction("Load")
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
		model = new ConfigXMLTreeTableModel(config);
		model.setEditable(editable);
		treetable = new JTreeTable(model);
		treetable.getModel().addTableModelListener(new TableModelListener()
		{
			@Override
			public void tableChanged(final TableModelEvent event)
			{
				if ((event.getType() == TableModelEvent.UPDATE) && (event.getFirstRow() == event.getLastRow()))
				{
					markDirty();
				}
			}
		});
		scrollpane.add(treetable);
		scrollpane.setViewportView(treetable);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param config
	 * @param editable
	 */
	public void updateModel(final HierarchicalConfiguration config, final boolean editable)
	{
		model = new ConfigXMLTreeTableModel(config);
		model.setEditable(editable);
		treetable.setTreeTableModel(model);
		treetable.getModel().addTableModelListener(new TableModelListener()
		{
			@Override
			public void tableChanged(final TableModelEvent event)
			{
				if ((event.getType() == TableModelEvent.UPDATE) && (event.getFirstRow() == event.getLastRow()))
				{
					markDirty();
				}
			}
		});
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
	
	
	private void reload()
	{
		notifyReloadPressed(configKey);
	}
	
	
	private void apply()
	{
		// Apply
		notifyApplyPressed(configKey);
		
		// Re-enable btn
		applyAction.setEnabled(!disableApply);
	}
	
	
	// --------------------------------------------------------------------------
	// --- observer -------------------------------------------------------------
	// --------------------------------------------------------------------------
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
}
