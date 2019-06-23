/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 5, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.referee;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.referee.RefereeHandler;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AReferee;


/**
 * Auto referee control panel
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class AutoRefereePanel extends JPanel
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**  */
	private static final long		serialVersionUID				= 4126983396811915744L;
	private static final Logger	log								= Logger.getLogger(AutoRefereePanel.class.getName());
	
	private final JCheckBox			chkBoxActive					= new JCheckBox("active");
	private final JCheckBox			chkBoxReplaceBallOutside	= new JCheckBox("replace ball outside field");
	private final JCheckBox			chkBoxReplaceBallGoal		= new JCheckBox("replace ball in goal");
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	  * 
	  */
	public AutoRefereePanel()
	{
		this.add(chkBoxActive);
		this.add(chkBoxReplaceBallOutside);
		this.add(chkBoxReplaceBallGoal);
		chkBoxActive.addActionListener(new ActiveActionListener());
		chkBoxReplaceBallOutside.addActionListener(new ReplaceBallOutsideActionListener());
		chkBoxReplaceBallGoal.addActionListener(new ReplaceBallGoalActionListener());
		chkBoxActive.setEnabled(false);
		chkBoxReplaceBallOutside.setEnabled(false);
		chkBoxReplaceBallGoal.setEnabled(false);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 */
	public void init()
	{
		chkBoxActive.setEnabled(true);
		chkBoxReplaceBallOutside.setEnabled(true);
		chkBoxReplaceBallGoal.setEnabled(true);
	}
	
	
	/**
	 */
	public void deinit()
	{
		chkBoxActive.setEnabled(false);
		chkBoxReplaceBallOutside.setEnabled(false);
		chkBoxReplaceBallGoal.setEnabled(false);
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	private class ActiveActionListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			try
			{
				RefereeHandler referee = (RefereeHandler) SumatraModel.getInstance().getModule(AReferee.MODULE_ID);
				referee.getAutoReferee().setActive(chkBoxActive.isSelected());
			} catch (ModuleNotFoundException err)
			{
				log.error("Referee module not found.", err);
			}
		}
	}
	
	private class ReplaceBallOutsideActionListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			try
			{
				RefereeHandler referee = (RefereeHandler) SumatraModel.getInstance().getModule(AReferee.MODULE_ID);
				referee.getAutoReferee().setReplaceBallOutside(chkBoxReplaceBallOutside.isSelected());
			} catch (ModuleNotFoundException err)
			{
				log.error("Referee module not found.", err);
			}
		}
	}
	
	private class ReplaceBallGoalActionListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			try
			{
				RefereeHandler referee = (RefereeHandler) SumatraModel.getInstance().getModule(AReferee.MODULE_ID);
				referee.getAutoReferee().setActive(chkBoxReplaceBallGoal.isSelected());
			} catch (ModuleNotFoundException err)
			{
				log.error("Referee module not found.", err);
			}
		}
	}
}
