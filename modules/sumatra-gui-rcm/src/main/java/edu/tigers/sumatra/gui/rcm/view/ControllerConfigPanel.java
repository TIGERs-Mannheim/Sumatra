/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.gui.rcm.view;

import edu.tigers.sumatra.gui.rcm.presenter.IRCMConfigChangedObserver;
import edu.tigers.sumatra.rcm.RcmActionMap;
import edu.tigers.sumatra.rcm.RcmActionMap.ERcmControllerConfig;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;


/**
 * Panel for controller configs
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class ControllerConfigPanel extends JPanel
{
	private static final long serialVersionUID = -7083335280756862591L;
	private final List<IRCMConfigChangedObserver> observers = new CopyOnWriteArrayList<>();
	
	
	/**
	 * @param observer
	 */
	public void addObserver(final IRCMConfigChangedObserver observer)
	{
		observers.add(observer);
	}
	
	
	/**
	 * @param observer
	 */
	public void removeObserver(final IRCMConfigChangedObserver observer)
	{
		observers.remove(observer);
	}
	
	
	/**
	 * @param config
	 */
	public final void updateConfig(final RcmActionMap config)
	{
		removeAll();
		for (Map.Entry<ERcmControllerConfig, Double> entry : config.getConfigValues().entrySet())
		{
			JLabel label = new JLabel(entry.getKey().name() + ": ");
			JTextField txtField = new JTextField(entry.getValue().toString(), 4);
			txtField.addFocusListener(new ChangeListener(txtField, entry.getKey()));
			add(label);
			add(txtField);
		}
		
	}
	
	
	private class ChangeListener implements FocusListener
	{
		private final JTextField txtField;
		private final ERcmControllerConfig config;
		
		
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
			// empty
		}
		
		
		@Override
		public void focusLost(final FocusEvent e)
		{
			try
			{
				double value = Double.parseDouble(txtField.getText());
				for (IRCMConfigChangedObserver o : observers)
				{
					o.onConfigChanged(config, value);
				}
			} catch (NumberFormatException err)
			{
				Logger.getAnonymousLogger().warning(err.getMessage());
			}
		}
	}
}
