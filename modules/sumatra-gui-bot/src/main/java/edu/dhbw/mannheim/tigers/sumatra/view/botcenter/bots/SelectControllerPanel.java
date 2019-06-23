/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 31.05.2013
 * Author(s): AndreR
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.botcenter.bots;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import edu.tigers.sumatra.botmanager.commands.tigerv2.TigerCtrlSetControllerType.EControllerType;
import edu.tigers.sumatra.botmanager.commands.tigerv2.TigerSystemQuery.EQueryType;
import net.miginfocom.swing.MigLayout;


/**
 * Select a controller, choose wisely :)
 * 
 * @author AndreR
 */
public class SelectControllerPanel extends JPanel
{
	/** */
	public interface ISelectControllerPanelObserver
	{
		/**
		 * @param type
		 */
		void onNewControllerSelected(EControllerType type);
		
		
		/**
		 * @param queryType
		 */
		void onQuery(EQueryType queryType);
	}
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	/**  */
	private static final long									serialVersionUID	= -1550931078238006617L;
	
	private JComboBox<EControllerType>						controller			= null;
	
	private final List<ISelectControllerPanelObserver>	observers			= new CopyOnWriteArrayList<ISelectControllerPanelObserver>();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 */
	public SelectControllerPanel()
	{
		setLayout(new MigLayout("wrap 1", "", ""));
		
		controller = new JComboBox<EControllerType>(EControllerType.values());
		controller.addActionListener(new SaveControllerType());
		
		JButton btnQuery = new JButton("Query controller type");
		btnQuery.addActionListener(new QueryListener());
		
		add(controller);
		add(btnQuery);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param observer
	 */
	public void addObserver(final ISelectControllerPanelObserver observer)
	{
		observers.add(observer);
	}
	
	
	/**
	 * @param observer
	 */
	public void removeObserver(final ISelectControllerPanelObserver observer)
	{
		observers.remove(observer);
	}
	
	
	private void notifyNewControllerSelected(final EControllerType type)
	{
		for (ISelectControllerPanelObserver observer : observers)
		{
			observer.onNewControllerSelected(type);
		}
	}
	
	
	private void notifyQuery(final EQueryType queryType)
	{
		for (ISelectControllerPanelObserver observer : observers)
		{
			observer.onQuery(queryType);
		}
	}
	
	
	/**
	 * @param t
	 */
	public void setControllerType(final EControllerType t)
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
		public void actionPerformed(final ActionEvent e)
		{
			notifyNewControllerSelected((EControllerType) controller.getSelectedItem());
		}
	}
	
	
	private class QueryListener implements ActionListener
	{
		@Override
		public void actionPerformed(final ActionEvent arg0)
		{
			notifyQuery(EQueryType.CTRL_TYPE);
		}
	}
}
