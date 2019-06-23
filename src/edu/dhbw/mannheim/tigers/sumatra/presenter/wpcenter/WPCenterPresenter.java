/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 08.09.2010
 * Author(s): Oliver Steinbrecher <OST1988@aol.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.presenter.wpcenter;

import java.awt.Component;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.SwingUtilities;


import edu.dhbw.mannheim.tigers.sumatra.presenter.laf.ILookAndFeelStateObserver;
import edu.dhbw.mannheim.tigers.sumatra.presenter.moduli.IModuliStateObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.main.ISumatraView;
import edu.dhbw.mannheim.tigers.sumatra.view.wpcenter.WPCenterPanel;
import edu.moduli.listenerVariables.ModulesState;


/**
 * This is the presenter for the ai view in sumatra.
 * 
 * @author Marcel Sauer
 * 
 */
public class WPCenterPresenter implements ISumatraView, ILookAndFeelStateObserver, IModuliStateObserver
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final int		ID					= 7;
	private static final String	TITLE				= "WP Center";
	

//	private final Logger				log				= Logger.getLogger(getClass());
	
//	private AAgent						aiAgent			= null;
	

//	private ABotManager				botManager		= null;
	

	private WPCenterPanel			wpCenterPanel	= null;
	
	private WPChartPresenter       wpTestPresenter = null;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public WPCenterPresenter()
	{
		wpCenterPanel = new WPCenterPanel();
		wpTestPresenter = new WPChartPresenter();
		
		wpCenterPanel.setMainPanel(wpTestPresenter.getChart());
		
		
		
		
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
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
		return wpCenterPanel;
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
	public void onModuliStateChanged(ModulesState state)
	{
		switch (state)
		{
			case ACTIVE:
			{
				// TODO: Anbindung an Worldpredictor
				/*
				try
				{
					
					aiAgent = (AAgent) SumatraModel.getInstance().getModule(AAgent.MODULE_ID);
					log.debug("Moduli active");
					
					// get AIInfoFrames
					aiAgent.addObserver(this);
					// TODO Oliver, check typecast
					wpCenterPanel.getModulesPanel().addObserver((Agent) aiAgent);
					

				} catch (ModuleNotFoundException err)
				{
					log.fatal("AI Module not found");
				}
				
				try
				{
					botManager = (ABotManager) SumatraModel.getInstance().getModule(ABotManager.MODULE_ID);
					log.debug("Moduli active");
					
					botManager.addObserver(this);
					
					for (ABot bot : botManager.getAllBots().values())
					{
						wpCenterPanel.getBotPanel().addBotPanel(bot.getBotId(), bot.getName());
						wpCenterPanel.getModulesPanel().getRolePanel().addBotId(bot.getBotId());
					}
					
				} catch (ModuleNotFoundException err)
				{
					log.fatal("Botmanager not found");
				}
				
				wpCenterPanel.getModulesPanel().setEnabled(true);
				*/
				break;
			}
				
			default:
			{
				/*
				if (aiAgent != null)
				{
					aiAgent.removeObserver(this);
					wpCenterPanel.getModulesPanel().removeObserver((Agent) aiAgent);
				}
				aiAgent = null;
				
				if (botManager != null)
				{
					botManager.removeObserver(this);
					wpCenterPanel.getBotPanel().removeAllBotPanels();
					SwingUtilities.updateComponentTreeUI(wpCenterPanel);
				}
				botManager = null;
				
				wpCenterPanel.clearView();
				wpCenterPanel.getModulesPanel().setEnabled(false);
				*/
				break;
			}
		}
		
	}
	

		

	@Override
	public void onLookAndFeelChanged()
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				SwingUtilities.updateComponentTreeUI(wpCenterPanel);
			}
		});
	}
	

	}
