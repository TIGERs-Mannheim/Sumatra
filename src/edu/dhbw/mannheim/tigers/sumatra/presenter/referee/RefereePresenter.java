/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 14.01.2011
 * Author(s): Malte
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.presenter.referee;

import java.awt.Component;
import java.util.List;

import javax.swing.JMenu;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.RefereeMsg;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.referee.RefereeMsgTransmitter;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.IOwnRefereeMsgObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.IRefereeObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AReferee;
import edu.dhbw.mannheim.tigers.sumatra.presenter.laf.ILookAndFeelStateObserver;
import edu.dhbw.mannheim.tigers.sumatra.presenter.laf.LookAndFeelStateAdapter;
import edu.dhbw.mannheim.tigers.sumatra.presenter.moduli.IModuliStateObserver;
import edu.dhbw.mannheim.tigers.sumatra.presenter.moduli.ModuliStateAdapter;
import edu.dhbw.mannheim.tigers.sumatra.view.main.ISumatraView;
import edu.dhbw.mannheim.tigers.sumatra.view.referee.CreateRefereeMsgPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.referee.RefereePanel;
import edu.moduli.exceptions.ModuleNotFoundException;
import edu.moduli.listenerVariables.ModulesState;


/**
 * This is the presenter for the referee in sumatra.
 * 
 * @author MalteM
 * 
 */
public class RefereePresenter implements ISumatraView, ILookAndFeelStateObserver, IModuliStateObserver,
		IRefereeObserver, IOwnRefereeMsgObserver
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private final Logger				log	= Logger.getLogger(getClass());
	
	// model
	private final SumatraModel		model	= SumatraModel.getInstance();
	private AReferee					refereeMsgReceiver;
	private RefereeMsgTransmitter	transmitter;
	
	// view
	private RefereePanel				refereePanel;
	
	// constants
	private final String				TITLE	= "Referee";
	private final int					ID		= 66;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public RefereePresenter()
	{
		refereePanel = new RefereePanel();
		LookAndFeelStateAdapter.getInstance().addObserver(this);
		ModuliStateAdapter.getInstance().addObserver(this);
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void onModuliStateChanged(ModulesState state)
	{
		switch (state)
		{
			case ACTIVE:
			{
				try
				{
					refereePanel.start();
					refereeMsgReceiver = (AReferee) model.getModule(AReferee.MODULE_ID);
					refereeMsgReceiver.addObserver(this);
					refereePanel.getCreateRefereeMsgPanel().addOwnRefMsgObserver(this);
					transmitter = new RefereeMsgTransmitter(refereeMsgReceiver.getSubnodeConfiguration());
					
				} catch (ModuleNotFoundException err)
				{
					log.fatal("AI Module not found");
				}
				break;
			}
				

			case RESOLVED:
			{
				if (refereeMsgReceiver != null)
				{
					refereeMsgReceiver.removeObserver(this);
				}
				if (refereePanel != null)
				{
					CreateRefereeMsgPanel p = refereePanel.getCreateRefereeMsgPanel();
					if (p != null)
					{
						p.removeOwnRefMsgObserver(this);
					}
				}
				break;
			}
		}
	}
	

	@Override
	public void onNewRefereeMsg(RefereeMsg msg)
	{
		refereePanel.getShowRefereeMsgPanel().newRefereeMsg(msg);
	}
	

	@Override
	public void onNewOwnRefereeMsg(RefereeMsg msg)
	{
		transmitter.sendOwnRefereeMsg(msg);
	}
	

	@Override
	public int getID()
	{
		return ID;
	}
	

	@Override
	public String getTitle()
	{
		return TITLE;
	}
	

	@Override
	public Component getViewComponent()
	{
		return refereePanel;
	}
	

	@Override
	public List<JMenu> getCustomMenus()
	{
		return null;
	}
	

	@Override
	public void onShown()
	{
		
	}
	

	@Override
	public void onHidden()
	{
	}
	

	@Override
	public void onFocused()
	{
	}
	

	@Override
	public void onFocusLost()
	{
	}
	

	@Override
	public void onLookAndFeelChanged()
	{
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
