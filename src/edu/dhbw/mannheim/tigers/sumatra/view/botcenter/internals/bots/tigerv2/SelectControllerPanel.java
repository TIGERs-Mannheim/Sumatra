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

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ECtrlMoveType;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tigerv2.TigerCtrlSetControllerType.EControllerType;


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
		 * @param type
		 */
		void onNewControllerSelected(EControllerType type);
		
		
		/**
		 * @param type
		 */
		void onNewCtrlMoveSelected(ECtrlMoveType type);
		
		
		/**
		 * @param ctrlType
		 * @param ctrlMoveType
		 */
		void onApplyControllerToAll(EControllerType ctrlType, ECtrlMoveType ctrlMoveType);
	}
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	/**  */
	private static final long									serialVersionUID	= -1550931078238006617L;
	
	private JComboBox<EControllerType>						controller			= null;
	private JComboBox<ECtrlMoveType>							ctrlMove				= null;
	
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
		
		controller = new JComboBox<EControllerType>(EControllerType.values());
		controller.addActionListener(new SaveControllerType());
		
		ctrlMove = new JComboBox<ECtrlMoveType>(ECtrlMoveType.values());
		ctrlMove.addActionListener(new SaveCtrlMoveType());
		
		JButton btnApplyToAll = new JButton("Apply to all");
		btnApplyToAll.addActionListener(new ApplyToAllActionListener());
		
		add(controller);
		add(ctrlMove);
		add(btnApplyToAll);
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
	
	
	private void notifyNewControllerSelected(EControllerType type)
	{
		synchronized (observers)
		{
			for (ISelectControllerPanelObserver observer : observers)
			{
				observer.onNewControllerSelected(type);
			}
		}
	}
	
	
	private void notifyNewCtrlMoveSelected(ECtrlMoveType type)
	{
		synchronized (observers)
		{
			for (ISelectControllerPanelObserver observer : observers)
			{
				observer.onNewCtrlMoveSelected(type);
			}
		}
	}
	
	
	private void notifyApplyControllerToAll(EControllerType ctrlType, ECtrlMoveType ctrlMoveType)
	{
		synchronized (observers)
		{
			for (ISelectControllerPanelObserver observer : observers)
			{
				observer.onApplyControllerToAll(ctrlType, ctrlMoveType);
			}
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
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
	
	
	/**
	 * @param t
	 */
	public void setCtrlMoveType(final ECtrlMoveType t)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				if (!ctrlMove.getSelectedItem().equals(t))
				{
					ctrlMove.setSelectedItem(t);
				}
			}
		});
	}
	
	private class SaveControllerType implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			notifyNewControllerSelected((EControllerType) controller.getSelectedItem());
		}
	}
	
	private class SaveCtrlMoveType implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			notifyNewCtrlMoveSelected((ECtrlMoveType) ctrlMove.getSelectedItem());
		}
	}
	
	private class ApplyToAllActionListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			notifyApplyControllerToAll((EControllerType) controller.getSelectedItem(),
					(ECtrlMoveType) ctrlMove.getSelectedItem());
		}
	}
}
