/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 27.11.2011
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.config;

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

import org.apache.commons.configuration.XMLConfiguration;

import edu.dhbw.mannheim.tigers.sumatra.view.commons.treetable.ITreeTableModel;
import edu.dhbw.mannheim.tigers.sumatra.view.commons.treetable.JTreeTable;


/**
 * A tab for one config in the ConfigEditor. It provides a tree-view of the xml-config with editing support and the
 * possibilities to save, load and switch config.
 * 
 * @author Gero
 * 
 */
public class EditorView extends JPanel
{
	// --------------------------------------------------------------------------
	// --- constants and variables ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long								serialVersionUID	= -7098099480668190062L;
	
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
	 * @param xmlConfig
	 * @param editable
	 */
	public EditorView(String title, String configKey, XMLConfiguration xmlConfig, boolean editable)
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
			private static final long	serialVersionUID	= 1L;
			
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				apply();
			}
		};
		applyAction.setEnabled(false);
		JButton applyBtn = new JButton(applyAction);
		controls.add(applyBtn);
		controls.add(Box.createHorizontalGlue());
		
		saveAction = new AbstractAction("Save")
		{
			private static final long	serialVersionUID	= 1L;
			
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				save();
			}
		};
		saveAction.setEnabled(false);
		JButton saveBtn = new JButton(saveAction);
		controls.add(saveBtn);
		controls.add(Box.createHorizontalGlue());
		
		Action reloadAction = new AbstractAction("Reload")
		{
			private static final long	serialVersionUID	= 1L;
			
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				reload();
			}
		};
		reloadAction.setEnabled(true);
		JButton reloadBtn = new JButton(reloadAction);
		controls.add(reloadBtn);
		
		controls.add(Box.createHorizontalGlue());
		
		// Setup lower part: The actual editor
		JScrollPane scrollpane = new JScrollPane();
		scrollpane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollpane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		add(scrollpane, "grow, top");
		
		
		// Finally: Add model
		model = new ConfigXMLTreeTableModel(xmlConfig);
		model.setEditable(editable);
		treetable = new JTreeTable(model);
		treetable.getModel().addTableModelListener(new TableModelListener()
		{
			@Override
			public void tableChanged(TableModelEvent event)
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
	public void updateModel(XMLConfiguration config, boolean editable)
	{
		model = new ConfigXMLTreeTableModel(config);
		model.setEditable(editable);
		treetable.setTreeTableModel(model);
		treetable.getModel().addTableModelListener(new TableModelListener()
		{
			@Override
			public void tableChanged(TableModelEvent event)
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
		saveAction.setEnabled(false);
	}
	
	
	private void reload()
	{
		// Save
		notifyReloadPressed(configKey);
	}
	
	
	private void apply()
	{
		// Apply
		notifyApplyPressed(configKey);
		
		// Re-enable btn
		applyAction.setEnabled(false);
	}
	
	
	// --------------------------------------------------------------------------
	// --- observer -------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param newObserver
	 */
	public void addObserver(IConfigEditorViewObserver newObserver)
	{
		observers.add(newObserver);
	}
	
	
	/**
	 * @param oldObserver
	 * @return
	 */
	public boolean removeObserver(IConfigEditorViewObserver oldObserver)
	{
		return observers.remove(oldObserver);
	}
	
	
	private void notifyApplyPressed(String configKey)
	{
		for (final IConfigEditorViewObserver observer : observers)
		{
			observer.onApplyPressed(configKey);
		}
	}
	
	
	private boolean notifySavePressed(String configKey)
	{
		boolean result = true;
		for (final IConfigEditorViewObserver observer : observers)
		{
			// If any returns false (e.g., save fails): return false
			result &= observer.onSavePressed(configKey);
		}
		return result;
	}
	
	
	private void notifyReloadPressed(String configKey)
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
