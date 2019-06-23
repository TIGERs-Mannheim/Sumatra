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

import edu.dhbw.mannheim.tigers.sumatra.view.referee.CreateRefereeMsgPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.referee.ICreateRefereeMsgObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.referee.RefereePanel;
import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.moduli.listenerVariables.ModulesState;
import edu.tigers.sumatra.Referee.SSL_Referee;
import edu.tigers.sumatra.Referee.SSL_Referee.Command;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.referee.AReferee;
import edu.tigers.sumatra.referee.IRefereeObserver;
import edu.tigers.sumatra.referee.RefereeMsg;
import edu.tigers.sumatra.util.GlobalShortcuts;
import edu.tigers.sumatra.util.GlobalShortcuts.EShortcut;
import edu.tigers.sumatra.views.ASumatraViewPresenter;
import edu.tigers.sumatra.views.ISumatraView;


/**
 * This is the presenter for the referee in sumatra.
 * 
 * @author MalteM
 */
public class RefereePresenter extends ASumatraViewPresenter implements IRefereeObserver,
		ICreateRefereeMsgObserver
{
	@SuppressWarnings("unused")
	private static final Logger	log				= Logger.getLogger(RefereePresenter.class.getName());
																
	private final RefereePanel		refereePanel	= new RefereePanel();
	private AReferee					refereeHandler;
											
											
	/**
	 */
	public RefereePresenter()
	{
		
		
		GlobalShortcuts.register(EShortcut.REFEREE_HALT, new Runnable()
		{
			@Override
			public void run()
			{
				refereeHandler.sendOwnRefereeMsg(Command.HALT, 0, 0, (short) 0, System.nanoTime(), null);
			}
		});
		GlobalShortcuts.register(EShortcut.REFEREE_STOP, new Runnable()
		{
			@Override
			public void run()
			{
				refereeHandler.sendOwnRefereeMsg(Command.STOP, 0, 0, (short) 0, System.nanoTime(), null);
			}
		});
		GlobalShortcuts.register(EShortcut.REFEREE_START, new Runnable()
		{
			@Override
			public void run()
			{
				refereeHandler.sendOwnRefereeMsg(Command.NORMAL_START, 0, 0, (short) 0, System.nanoTime(), null);
			}
		});
	}
	
	
	@Override
	public void onModuliStateChanged(final ModulesState state)
	{
		super.onModuliStateChanged(state);
		switch (state)
		{
			case ACTIVE:
			{
				try
				{
					refereeHandler = (AReferee) SumatraModel.getInstance().getModule(AReferee.MODULE_ID);
					refereeHandler.addObserver(this);
				} catch (final ModuleNotFoundException err)
				{
					log.error("referee Module not found");
				}
				
				refereePanel.start();
				refereePanel.getCreateRefereeMsgPanel().addObserver(this);
				break;
			}
			
			
			case RESOLVED:
			{
				if (refereeHandler != null)
				{
					refereeHandler.removeObserver(this);
					refereeHandler = null;
				}
				refereePanel.stop();
				final CreateRefereeMsgPanel p = refereePanel.getCreateRefereeMsgPanel();
				p.removeObserver(this);
				break;
			}
			case NOT_LOADED:
			default:
				break;
		}
	}
	
	
	@Override
	public void onNewRefereeMsg(final SSL_Referee msg)
	{
		refereePanel.getShowRefereeMsgPanel().newRefereeMsg(new RefereeMsg(0, msg));
	}
	
	
	@Override
	public void onSendOwnRefereeMsg(final Command cmd, final int goalsBlue, final int goalsYellow,
			final short timeLeft, final long timestamp, final IVector2 placementPos)
	{
		refereeHandler.sendOwnRefereeMsg(cmd, goalsBlue, goalsYellow, timeLeft, timestamp, placementPos);
	}
	
	
	@Override
	public void onEnableReceive(final boolean receive)
	{
		refereeHandler.setReceiveExternalMsg(receive);
	}
	
	
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
}
