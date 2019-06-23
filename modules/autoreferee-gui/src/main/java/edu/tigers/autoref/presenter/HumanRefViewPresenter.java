/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 4, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoref.presenter;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import edu.tigers.autoref.presenter.humanref.ActiveHumanRefViewDriver;
import edu.tigers.autoref.presenter.humanref.BaseHumanRefViewDriver;
import edu.tigers.autoref.presenter.humanref.PassiveHumanRefViewDriver;
import edu.tigers.autoref.view.generic.SumatraViewPanel;
import edu.tigers.autoref.view.humanref.ActiveHumanRefPanel;
import edu.tigers.autoref.view.humanref.BaseHumanRefPanel;
import edu.tigers.autoref.view.humanref.PassiveHumanRefPanel;
import edu.tigers.autoreferee.AutoRefModule;
import edu.tigers.autoreferee.AutoRefModule.AutoRefState;
import edu.tigers.autoreferee.AutoRefUtil;
import edu.tigers.autoreferee.IAutoRefFrame;
import edu.tigers.autoreferee.IAutoRefStateObserver;
import edu.tigers.autoreferee.engine.IAutoRefEngine.AutoRefMode;
import edu.tigers.autoreferee.engine.log.GameLog.IGameLogObserver;
import edu.tigers.autoreferee.engine.log.GameLogEntry;
import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.moduli.listenerVariables.ModulesState;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.thread.NamedThreadFactory;
import edu.tigers.sumatra.views.ISumatraView;
import edu.tigers.sumatra.views.ISumatraViewPresenter;
import edu.tigers.sumatra.wp.AWorldPredictor;
import edu.tigers.sumatra.wp.IWorldFrameObserver;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;


/**
 * @author "Lukas Magel"
 */
public class HumanRefViewPresenter implements ISumatraViewPresenter, IAutoRefStateObserver, IWorldFrameObserver,
		IGameLogObserver, Runnable
{
	private static final Logger		log			= Logger.getLogger(HumanRefViewPresenter.class);
	
	private ScheduledExecutorService	scheduler;
	
	private SumatraViewPanel			mainPanel	= new SumatraViewPanel();
	private BaseHumanRefViewDriver	driver;
	
	
	/**
	 * 
	 */
	public HumanRefViewPresenter()
	{
		BaseHumanRefPanel basePanel = new BaseHumanRefPanel();
		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(basePanel, BorderLayout.CENTER);
		
		driver = new BaseHumanRefViewDriver(basePanel);
	}
	
	
	@Override
	public Component getComponent()
	{
		return mainPanel;
	}
	
	
	@Override
	public ISumatraView getSumatraView()
	{
		return mainPanel;
	}
	
	
	@Override
	public void onModuliStateChanged(final ModulesState state)
	{
		switch (state)
		{
			case ACTIVE:
				start();
				break;
			case RESOLVED:
				stop();
				break;
			default:
				break;
		}
	}
	
	
	private void start()
	{
		AutoRefUtil.ifAutoRefModulePresent(module -> {
			/*
			 * We manually feed the current state to the listener to force a refresh of the ref panel
			 */
				AutoRefState state = module.getState();
				onAutoRefStateChanged(state);
				module.addObserver(this);
			});
		try
		{
			AWorldPredictor predictor = (AWorldPredictor) SumatraModel
					.getInstance().getModule(AWorldPredictor.MODULE_ID);
			predictor.addWorldFrameConsumer(this);
		} catch (ModuleNotFoundException err)
		{
			log.warn("Could not find a module", err);
		}
		
		scheduler = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("HumanRefViewVisualizerUpdater"));
		scheduler.scheduleAtFixedRate(this, 0, 32, TimeUnit.MILLISECONDS);
	}
	
	
	private void stop()
	{
		if (scheduler != null)
		{
			scheduler.shutdownNow();
			try
			{
				scheduler.awaitTermination(100, TimeUnit.MILLISECONDS);
			} catch (InterruptedException err)
			{
				log.error("Timed out waiting for update thread shutdown...");
			}
		}
		
		AutoRefUtil.ifAutoRefModulePresent(module -> module.removeObserver(this));
		try
		{
			AWorldPredictor predictor = (AWorldPredictor) SumatraModel
					.getInstance().getModule(AWorldPredictor.MODULE_ID);
			predictor.removeWorldFrameConsumer(this);
		} catch (ModuleNotFoundException err)
		{
			log.warn("Could not find a module", err);
		}
	}
	
	
	@Override
	public void onAutoRefStateChanged(final AutoRefState state)
	{
		switch (state)
		{
			case STARTED:
				PanelType type = PanelType.BASE;
				Optional<AutoRefModule> optModule = AutoRefUtil.getAutoRefModule();
				if (optModule.isPresent())
				{
					AutoRefModule module = optModule.get();
					AutoRefMode mode = module.getEngine().getMode();
					
					if (mode == AutoRefMode.ACTIVE)
					{
						type = PanelType.ACTIVE;
					} else if (mode == AutoRefMode.PASSIVE)
					{
						type = PanelType.PASSIVE;
					}
					
					PanelType finalType = type;
					EventQueue.invokeLater(() -> setPanelType(finalType));
					module.getEngine().getGameLog().addObserver(this);
				}
				break;
			case STOPPED:
				EventQueue.invokeLater(() -> setPanelType(PanelType.BASE));
			default:
				break;
		}
	}
	
	
	@Override
	public void onNewWorldFrame(final WorldFrameWrapper wFrameWrapper)
	{
		EventQueue.invokeLater(() -> driver.setNewWorldFrame(wFrameWrapper));
	}
	
	
	@Override
	public void onNewAutoRefFrame(final IAutoRefFrame frame)
	{
		EventQueue.invokeLater(() -> driver.setNewRefFrame(frame));
	}
	
	
	@Override
	public void run()
	{
		try
		{
			driver.paintField();
		} catch (Exception e)
		{
			log.error("Error in Human Ref Visualizer Thread", e);
		}
	}
	
	
	@Override
	public void onNewEntry(final int id, final GameLogEntry entry)
	{
		EventQueue.invokeLater(() -> driver.setNewGameLogEntry(entry));
	}
	
	
	private void setPanelType(final PanelType type)
	{
		BaseHumanRefPanel panel = null;
		driver.stop();
		switch (type)
		{
			case ACTIVE:
			{
				ActiveHumanRefPanel activePanel = new ActiveHumanRefPanel();
				driver = new ActiveHumanRefViewDriver(activePanel);
				panel = activePanel;
				break;
			}
			case BASE:
			{
				panel = new BaseHumanRefPanel();
				driver = new BaseHumanRefViewDriver(panel);
				break;
			}
			case PASSIVE:
			{
				PassiveHumanRefPanel passivePanel = new PassiveHumanRefPanel();
				driver = new PassiveHumanRefViewDriver(passivePanel);
				panel = passivePanel;
				break;
			}
			default:
				break;
		}
		mainPanel.removeAll();
		mainPanel.add(panel, BorderLayout.CENTER);
		
		driver.start();
	}
	
	private enum PanelType
	{
		BASE,
		ACTIVE,
		PASSIVE
	}
	
}
