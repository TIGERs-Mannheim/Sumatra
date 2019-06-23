/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 31.05.2013
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tigerv2;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerCtrlSetControllerType.ControllerType;


/**
 * Select a controller, choose wisely :)
 * 
 * @author AndreR
 * 
 */
public class SelectControllerPanel extends JPanel
{
	/** */
	public interface ISelectControllerPanelObserver
	{
		/**
		 * 
		 * @param type
		 */
		void onNewControllerSelected(ControllerType type);
	}
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	/**  */
	private static final long									serialVersionUID	= -1550931078238006617L;
	
	private JComboBox<ControllerType>						controller			= null;
	private SaveControllerType									listener				= new SaveControllerType();
	
	private final List<ISelectControllerPanelObserver>	observers			= new ArrayList<ISelectControllerPanelObserver>();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 */
	public SelectControllerPanel()
	{
		setLayout(new MigLayout("", "", ""));
		
		controller = new JComboBox<ControllerType>(ControllerType.values());
		controller.addActionListener(listener);
		
		add(controller);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 * @param observer
	 */
	public void addObserver(ISelectControllerPanelObserver observer)
	{
		synchronized (observers)
		{
			observers.add(observer);
		}
	}
	
	
	/**
	 * 
	 * @param observer
	 */
	public void removeObserver(ISelectControllerPanelObserver observer)
	{
		synchronized (observers)
		{
			observers.remove(observer);
		}
	}
	
	
	private void notifyNewControllerSelected(ControllerType type)
	{
		synchronized (observers)
		{
			for (ISelectControllerPanelObserver observer : observers)
			{
				observer.onNewControllerSelected(type);
			}
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param t
	 */
	public void setControllerType(final ControllerType t)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				if (controller.getSelectedIndex() != t.getId())
				{
					controller.setSelectedIndex(t.getId());
				}
			}
		});
	}
	
	private class SaveControllerType implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			notifyNewControllerSelected((ControllerType) controller.getSelectedItem());
		}
	}
}
