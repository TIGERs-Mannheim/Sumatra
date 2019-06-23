/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.autoref.presenter;

import java.awt.*;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;

import edu.tigers.autoref.view.humanref.HumanRefMainPanel;
import edu.tigers.autoref.view.humanref.IHumanRefPanel.EPanelType;
import edu.tigers.autoreferee.AutoRefUtil;
import edu.tigers.autoreferee.IAutoRefFrame;
import edu.tigers.autoreferee.IAutoRefStateObserver;
import edu.tigers.autoreferee.engine.IAutoRefEngine.AutoRefMode;
import edu.tigers.autoreferee.engine.log.GameLog.IGameLogObserver;
import edu.tigers.autoreferee.engine.log.GameLogEntry;
import edu.tigers.autoreferee.module.AutoRefModule;
import edu.tigers.autoreferee.module.AutoRefState;
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
	
	private HumanRefMainPanel			mainPanel	= new HumanRefMainPanel();

	
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
				/*
				 * The AutoRefState can transition into Running when the Referee is started up or if it is resumed after it
				 * has been paused. Due to this all startup actions are triggered by the Started state as it is only active
				 * once right after the AutoRefere has been started up. Since the listener triggers on the Started state
				 * instead of the Running state we manually need to set it to Started to trigger the startup actions if the
				 * panel is opened after the referee has been started.
				 */
				if (state == AutoRefState.RUNNING)
				{
					state = AutoRefState.STARTED;
				}
				onAutoRefStateChanged(state);
				module.addObserver(this);
			});
		try
		{
			AWorldPredictor predictor = (AWorldPredictor) SumatraModel
					.getInstance().getModule(AWorldPredictor.MODULE_ID);
			predictor.addObserver(this);
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
			scheduler.shutdown();
			try
			{
				Validate.isTrue(scheduler.awaitTermination(100, TimeUnit.MILLISECONDS));
			} catch (InterruptedException err)
			{
				log.error("Interrupted while awaiting termination", err);
				Thread.currentThread().interrupt();
			}
		}
		
		AutoRefUtil.ifAutoRefModulePresent(module -> module.removeObserver(this));
		try
		{
			AWorldPredictor predictor = (AWorldPredictor) SumatraModel
					.getInstance().getModule(AWorldPredictor.MODULE_ID);
			predictor.removeObserver(this);
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
				EPanelType type = EPanelType.BASE;
				Optional<AutoRefModule> optModule = AutoRefUtil.getAutoRefModule();
				if (optModule.isPresent())
				{
					AutoRefModule module = optModule.get();
					AutoRefMode mode = module.getEngine().getMode();
					
					if (mode == AutoRefMode.ACTIVE)
					{
						type = EPanelType.ACTIVE;
					} else if (mode == AutoRefMode.PASSIVE)
					{
						type = EPanelType.PASSIVE;
					}
					
					EPanelType finalType = type;
					EventQueue.invokeLater(() -> mainPanel.setPanelType(finalType));
					module.getEngine().getGameLog().addObserver(this);
				}
				break;
			case STOPPED:
				EventQueue.invokeLater(() -> mainPanel.setPanelType(EPanelType.BASE));
				break;
			default:
				break;
		}
	}
	
	
	@Override
	public void onNewWorldFrame(final WorldFrameWrapper wFrameWrapper)
	{
		EventQueue.invokeLater(() -> mainPanel.getDriver().setNewWorldFrame(wFrameWrapper));
	}
	
	
	@Override
	public void onNewAutoRefFrame(final IAutoRefFrame frame)
	{
		EventQueue.invokeLater(() -> mainPanel.getDriver().setNewRefFrame(frame));
	}
	
	
	@Override
	public void run()
	{
		try
		{
			mainPanel.getDriver().paintField();
		} catch (Exception e)
		{
			log.error("Error in Human Ref Visualizer Thread", e);
		}
	}
	
	
	@Override
	public void onNewEntry(final int id, final GameLogEntry entry)
	{
		EventQueue.invokeLater(() -> mainPanel.getDriver().setNewGameLogEntry(entry));
	}
}
