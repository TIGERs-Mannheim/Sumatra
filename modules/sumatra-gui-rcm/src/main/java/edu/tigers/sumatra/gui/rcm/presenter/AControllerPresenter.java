/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.gui.rcm.presenter;

import edu.tigers.sumatra.gui.rcm.view.ButtonSelectAction;
import edu.tigers.sumatra.gui.rcm.view.ControllerPanel;
import edu.tigers.sumatra.gui.rcm.view.IIdentifierSelectionObserver;
import edu.tigers.sumatra.rcm.ActionSender;
import edu.tigers.sumatra.rcm.ExtIdentifier;
import edu.tigers.sumatra.rcm.PollingService;
import edu.tigers.sumatra.rcm.RcmAction;
import edu.tigers.sumatra.rcm.RcmActionMap;
import edu.tigers.sumatra.rcm.RcmActionMap.ERcmControllerConfig;
import edu.tigers.sumatra.rcm.RcmActionMapping;
import net.java.games.input.Controller;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.SwingUtilities;
import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * Abstract base for any controller presenter
 */
public abstract class AControllerPresenter
{
	private static final Logger log = LogManager.getLogger(AControllerPresenter.class.getName());
	private final RcmActionMap config;
	private final Controller controller;
	private PollingService pollingService;
	private final ControllerPanel panel;


	protected AControllerPresenter(final Controller newController)
	{
		controller = newController;
		config = new RcmActionMap(controller);
		pollingService = new PollingService(config, new ActionSender(""));
		panel = new ControllerPanel();
		ConfigObserver ob = new ConfigObserver();
		panel.addObserver(ob);
		panel.getConfigPanel().addObserver(ob);
		loadDefaultConfig();
	}


	/**
	 */
	private void loadDefaultConfig()
	{
		config.loadDefault(controller);
		SwingUtilities.invokeLater(() -> panel.reloadConfig(config));
	}


	/**
	 * starts polling for controller
	 *
	 * @param actionSender
	 */
	public void startPolling(final ActionSender actionSender)
	{
		if ((controller.getType() == Controller.Type.KEYBOARD)
				|| (controller.getType() == Controller.Type.GAMEPAD)
				|| (controller.getType() == Controller.Type.STICK))
		{
			if (pollingService != null)
			{
				pollingService.stop();
			}
			pollingService = new PollingService(config, actionSender);
			pollingService.start();
		}
	}


	/**
	 * stops polling for controller
	 */
	public void stopPolling()
	{
		pollingService.stop();
	}


	/**
	 * @return Controller
	 */
	public Controller getController()
	{
		return controller;
	}


	/**
	 * @return
	 */
	public RcmActionMap getConfig()
	{
		return config;
	}


	/**
	 * @return
	 */
	public ActionSender getActionSender()
	{
		return pollingService.getActionSender();
	}


	/**
	 * @return the panel
	 */
	public ControllerPanel getPanel()
	{
		return panel;
	}

	private class ConfigObserver implements IRCMConfigChangedObserver
	{

		@Override
		public void onActionMappingCreated(final RcmActionMapping mapping)
		{
			config.addMapping(mapping);
		}


		@Override
		public void onActionMappingChanged(final RcmActionMapping mapping)
		{
			// changes are processed on the fly
		}


		@Override
		public void onActionMappingRemoved(final RcmActionMapping mapping)
		{
			config.removeMapping(mapping);
		}


		@Override
		public void onSaveConfig()
		{
			config.saveDefault(controller);
		}


		@Override
		public void onSaveConfigAs(final File file)
		{
			config.save(file);
		}


		@Override
		public void onLoadConfig(final File file)
		{
			config.load(file);
			SwingUtilities.invokeLater(() -> panel.reloadConfig(config));
		}


		@Override
		public void onLoadDefaultConfig()
		{
			loadDefaultConfig();
		}


		@Override
		public void onSelectAssignment(final RcmActionMapping actionMapping)
		{
			new ButtonSelectAction(new IIdentifierSelectionObserver()
			{
				@Override
				public void onIdentifiersSelectionCanceled()
				{
					// nothing to do here
				}


				@Override
				public void onIdentifiersSelected(final List<ExtIdentifier> identifiers)
				{
					actionMapping.getIdentifiers().clear();
					actionMapping.getIdentifiers().addAll(identifiers);
					SwingUtilities.invokeLater(() -> panel.reloadConfig(config));
				}
			}, controller);
		}


		@Override
		public void onSelectionAssistant()
		{
			new IdentifierSelectorAssistant();
		}


		@Override
		public void onConfigChanged(final ERcmControllerConfig configType, final double value)
		{
			config.getConfigValues().put(configType, value);
		}


		@Override
		public void onUnassignBot()
		{
			getActionSender().notifyBotUnassigned();
		}


		@Override
		public void onEnabled(final boolean enabled)
		{
			config.setEnabled(enabled);
		}
	}

	private class IdentifierSelectorAssistant implements IIdentifierSelectionObserver
	{

		private final List<RcmAction> actions;


		/**
		 *
		 */
		public IdentifierSelectorAssistant()
		{
			actions = new ArrayList<>(RcmAction.getDefaultActions());
			select();
		}


		private void select()
		{
			log.info("Select action for {}", actions.getFirst());
			new ButtonSelectAction(this, controller);
		}


		@Override
		public void onIdentifiersSelected(final List<ExtIdentifier> identifiers)
		{
			RcmActionMapping mapping = new RcmActionMapping(identifiers, actions.get(0));
			config.addMapping(mapping);
			SwingUtilities.invokeLater(() -> panel.reloadConfig(config));
			actions.remove(0);
			if (!actions.isEmpty())
			{
				select();
			} else
			{
				log.info("Done");
			}
		}


		@Override
		public void onIdentifiersSelectionCanceled()
		{
			// nothing to do here
		}
	}
}
