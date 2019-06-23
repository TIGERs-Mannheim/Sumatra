/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 14.01.2011
 * Author(s): Malte
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.presenter.referee;

import java.awt.Component;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.dhbw.mannheim.tigers.moduli.listenerVariables.ModulesState;
import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Referee.SSL_Referee.Command;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.referee.RefereeMsg;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.IRefereeObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AReferee;
import edu.dhbw.mannheim.tigers.sumatra.presenter.laf.ILookAndFeelStateObserver;
import edu.dhbw.mannheim.tigers.sumatra.presenter.laf.LookAndFeelStateAdapter;
import edu.dhbw.mannheim.tigers.sumatra.presenter.moduli.IModuliStateObserver;
import edu.dhbw.mannheim.tigers.sumatra.presenter.moduli.ModuliStateAdapter;
import edu.dhbw.mannheim.tigers.sumatra.view.referee.CreateRefereeMsgPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.referee.ICreateRefereeMsgObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.referee.RefereePanel;
import edu.dhbw.mannheim.tigers.sumatra.views.ISumatraView;
import edu.dhbw.mannheim.tigers.sumatra.views.ISumatraViewPresenter;


/**
 * This is the presenter for the referee in sumatra.
 * 
 * @author MalteM
 */
public class RefereePresenter implements ILookAndFeelStateObserver, IModuliStateObserver, IRefereeObserver,
		ICreateRefereeMsgObserver, ISumatraViewPresenter
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	// Logger
	private static final Logger	log	= Logger.getLogger(RefereePresenter.class.getName());
	
	// model
	private final SumatraModel		model	= SumatraModel.getInstance();
	private AReferee					refereeHandler;
	
	// view
	private final RefereePanel		refereePanel;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
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
	public void onModuliStateChanged(final ModulesState state)
	{
		switch (state)
		{
			case ACTIVE:
			{
				try
				{
					refereePanel.start();
					refereeHandler = (AReferee) model.getModule(AReferee.MODULE_ID);
					refereeHandler.addObserver(this);
					refereePanel.getCreateRefereeMsgPanel().addObserver(this);
					
				} catch (final ModuleNotFoundException err)
				{
					log.fatal("AI Module not found");
				}
				break;
			}
			
			
			case RESOLVED:
			{
				if (refereeHandler != null)
				{
					refereeHandler.removeObserver(this);
				}
				if (refereePanel != null)
				{
					refereePanel.stop();
					final CreateRefereeMsgPanel p = refereePanel.getCreateRefereeMsgPanel();
					if (p != null)
					{
						p.removeObserver(this);
					}
				}
				break;
			}
			case NOT_LOADED:
			default:
				break;
		}
	}
	
	
	@Override
	public void onNewRefereeMsg(final RefereeMsg msg)
	{
		refereePanel.getShowRefereeMsgPanel().newRefereeMsg(msg);
	}
	
	
	@Override
	public void onSendOwnRefereeMsg(final Command cmd, final int goalsBlue, final int goalsYellow,
			final short timeLeft)
	{
		refereeHandler.sendOwnRefereeMsg(cmd, goalsBlue, goalsYellow, timeLeft);
	}
	
	
	@Override
	public void onLookAndFeelChanged()
	{
	}
	
	
	@Override
	public void onEnableReceive(final boolean receive)
	{
		refereeHandler.setReceiveExternalMsg(receive);
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	public Component getComponent()
	{
		return refereePanel;
	}
	
	
	@Override
	public ISumatraView getSumatraView()
	{
		return refereePanel;
	}
	
	
	@Override
	public void onEmergencyStop()
	{
	}
	
}
