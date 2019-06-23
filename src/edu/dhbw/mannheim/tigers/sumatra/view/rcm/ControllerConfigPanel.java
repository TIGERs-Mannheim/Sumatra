/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Sep 20, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.rcm;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.rcm.RcmActionMap;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.rcm.RcmActionMap.ERcmControllerConfig;
import edu.dhbw.mannheim.tigers.sumatra.presenter.rcm.IRCMConfigChangedObserver;


/**
 * Panel for controller configs
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class ControllerConfigPanel extends JPanel
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**  */
	private static final long										serialVersionUID	= -7083335280756862591L;
	private final Map<ERcmControllerConfig, JTextField>	textFields			= new HashMap<ERcmControllerConfig, JTextField>();
	private final List<IRCMConfigChangedObserver>			observers			= new CopyOnWriteArrayList<IRCMConfigChangedObserver>();
	
	
	/**
	 * @param observer
	 */
	public void addObserver(final IRCMConfigChangedObserver observer)
	{
		synchronized (observers)
		{
			observers.add(observer);
		}
	}
	
	
	/**
	 * @param observer
	 */
	public void removeObserver(final IRCMConfigChangedObserver observer)
	{
		synchronized (observers)
		{
			observers.remove(observer);
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 */
	public ControllerConfigPanel()
	{
	}
	
	
	/**
	 * @param config
	 */
	public final void updateConfig(final RcmActionMap config)
	{
		textFields.clear();
		removeAll();
		for (Map.Entry<ERcmControllerConfig, Float> entry : config.getConfigValues().entrySet())
		{
			JLabel label = new JLabel(entry.getKey().name() + ": ");
			JTextField txtField = new JTextField(entry.getValue().toString(), 4);
			txtField.addFocusListener(new ChangeListener(txtField, entry.getKey()));
			textFields.put(entry.getKey(), txtField);
			add(label);
			add(txtField);
		}
		
	}
	
	
	private class ChangeListener implements FocusListener
	{
		private final JTextField				txtField;
		private final ERcmControllerConfig	config;
		
		
		/**
		 * @param txtField
		 * @param config
		 */
		public ChangeListener(final JTextField txtField, final ERcmControllerConfig config)
		{
			super();
			this.txtField = txtField;
			this.config = config;
		}
		
		
		@Override
		public void focusGained(final FocusEvent e)
		{
		}
		
		
		@Override
		public void focusLost(final FocusEvent e)
		{
			try
			{
				float value = Float.valueOf(txtField.getText());
				synchronized (observers)
				{
					for (IRCMConfigChangedObserver o : observers)
					{
						o.onConfigChanged(config, value);
					}
				}
			} catch (NumberFormatException err)
			{
			}
		}
	}
}
