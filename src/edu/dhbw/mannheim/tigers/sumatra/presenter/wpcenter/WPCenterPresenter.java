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

import javax.swing.SwingUtilities;

import edu.dhbw.mannheim.tigers.moduli.listenerVariables.ModulesState;
import edu.dhbw.mannheim.tigers.sumatra.presenter.laf.ILookAndFeelStateObserver;
import edu.dhbw.mannheim.tigers.sumatra.presenter.moduli.IModuliStateObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.wpcenter.WPCenterPanel;
import edu.dhbw.mannheim.tigers.sumatra.views.ISumatraView;
import edu.dhbw.mannheim.tigers.sumatra.views.ISumatraViewPresenter;


/**
 * This is the presenter for the ai view in sumatra.
 * 
 * @author Marcel Sauer
 * 
 */
public class WPCenterPresenter implements ILookAndFeelStateObserver, IModuliStateObserver, ISumatraViewPresenter
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	// private final Logger log = Logger.getLogger(getClass());
	
	// private AAgent aiAgent = null;
	
	
	// private ABotManager botManager = null;
	
	
	private WPCenterPanel		wpCenterPanel		= null;
	
	private WPChartPresenter	wpTestPresenter	= null;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 */
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
	public void onModuliStateChanged(ModulesState state)
	{
		switch (state)
		{
			case ACTIVE:
			{
				/*
				 * try
				 * {
				 * 
				 * aiAgent = (AAgent) SumatraModel.getInstance().getModule(AAgent.MODULE_ID);
				 * log.debug("Moduli active");
				 * 
				 * // get AIInfoFrames
				 * aiAgent.addObserver(this);
				 * wpCenterPanel.getModulesPanel().addObserver((Agent) aiAgent);
				 * 
				 * 
				 * } catch (ModuleNotFoundException err)
				 * {
				 * log.fatal("AI Module not found");
				 * }
				 * 
				 * try
				 * {
				 * botManager = (ABotManager) SumatraModel.getInstance().getModule(ABotManager.MODULE_ID);
				 * log.debug("Moduli active");
				 * 
				 * botManager.addObserver(this);
				 * 
				 * for (ABot bot : botManager.getAllBots().values())
				 * {
				 * wpCenterPanel.getBotPanel().addBotPanel(bot.getBotId(), bot.getName());
				 * wpCenterPanel.getModulesPanel().getRolePanel().addBotId(bot.getBotId());
				 * }
				 * 
				 * } catch (ModuleNotFoundException err)
				 * {
				 * log.fatal("Botmanager not found");
				 * }
				 * 
				 * wpCenterPanel.getModulesPanel().setEnabled(true);
				 */
				break;
			}
			
			default:
			{
				/*
				 * if (aiAgent != null)
				 * {
				 * aiAgent.removeObserver(this);
				 * wpCenterPanel.getModulesPanel().removeObserver((Agent) aiAgent);
				 * }
				 * aiAgent = null;
				 * 
				 * if (botManager != null)
				 * {
				 * botManager.removeObserver(this);
				 * wpCenterPanel.getBotPanel().removeAllBotPanels();
				 * SwingUtilities.updateComponentTreeUI(wpCenterPanel);
				 * }
				 * botManager = null;
				 * 
				 * wpCenterPanel.clearView();
				 * wpCenterPanel.getModulesPanel().setEnabled(false);
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
	
	
	@Override
	public Component getComponent()
	{
		return wpCenterPanel;
	}
	
	
	@Override
	public ISumatraView getSumatraView()
	{
		return wpCenterPanel;
	}
	
	
	@Override
	public void onEmergencyStop()
	{
	}
	
}
