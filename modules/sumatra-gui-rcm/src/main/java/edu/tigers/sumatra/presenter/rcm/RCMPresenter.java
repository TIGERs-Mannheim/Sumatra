/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.presenter.rcm;

import com.github.g3force.configurable.ConfigRegistration;
import edu.tigers.moduli.listenerVariables.ModulesState;
import edu.tigers.sumatra.botmanager.ABotManager;
import edu.tigers.sumatra.botmanager.bots.ABot;
import edu.tigers.sumatra.ids.AObjectID;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.rcm.ActionSender;
import edu.tigers.sumatra.rcm.CommandInterpreter;
import edu.tigers.sumatra.rcm.CommandInterpreterStub;
import edu.tigers.sumatra.rcm.ICommandInterpreter;
import edu.tigers.sumatra.rcm.IRCMObserver;
import edu.tigers.sumatra.rcm.RcmActionMap.ERcmControllerConfig;
import edu.tigers.sumatra.view.rcm.RCMPanel;
import edu.tigers.sumatra.views.ASumatraViewPresenter;
import edu.tigers.sumatra.views.ISumatraView;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * This class enables clients to send commands directly to the bots
 */
public class RCMPresenter extends ASumatraViewPresenter implements IRCMObserver
{
	private static final Logger log = LogManager.getLogger(RCMPresenter.class.getName());

	private ABotManager botManager = null;

	private final Map<BotID, CommandInterpreter> botInterpreters = new HashMap<>();
	private final List<ActionSender> actionSenders = new LinkedList<>();

	private final List<AControllerPresenter> controllerPresenterS = new ArrayList<>();
	private final RCMPanel rcmPanel;


	static
	{
		// set library path for jinput
		final String curDir = System.getProperty("user.dir");
		System.setProperty("net.java.games.input.librarypath",
				curDir + "/modules/sumatra-rcm/build/natives");
	}


	public RCMPresenter()
	{
		rcmPanel = new RCMPanel();
		rcmPanel.addObserver(this);
		setUpController(false);
	}


	@Override
	public void setUpController(final boolean keepConnections)
	{
		final ControllerEnvironment cEnv = ControllerEnvironment.getDefaultEnvironment();
		final Controller[] cs = cEnv.getControllers();
		final List<Controller> controllers = new ArrayList<>(cs.length);
		for (final Controller controller : cs)
		{
			controllers.add(controller);
			log.info("Controller found: " + controller.getName());
		}

		List<ABot> bots = new ArrayList<>(controllerPresenterS.size());
		if (keepConnections)
		{
			for (AControllerPresenter cp : controllerPresenterS)
			{
				bots.add(cp.getActionSender().getCmdInterpreter().getBot());
			}
		}

		onStartStopButtonPressed(false);
		controllerPresenterS.clear();
		rcmPanel.clearControllerPanels();
		for (Controller controller : controllers)
		{
			addController(controller);
		}
		if (controllers.isEmpty())
		{
			log.info("No controller found.");
		}


		if (keepConnections)
		{
			onStartStopButtonPressed(true);
			for (int i = 0; (i < controllerPresenterS.size()) && (i < bots.size()); i++)
			{
				if (bots.get(i).getBotId().isBot())
				{
					changeBotAssignment(controllerPresenterS.get(i).getActionSender(), bots.get(i));
				}
			}
		}
	}


	private void addController(final Controller controller)
	{
		final Controller.Type type = controller.getType();
		AControllerPresenter presenter;
		if (type == Controller.Type.KEYBOARD)
		{
			presenter = new KeyboardPresenter(controller);
		} else if ((type == Controller.Type.GAMEPAD) || (type == Controller.Type.STICK))
		{
			presenter = new GamePadPresenter(controller);
		} else
		{
			return;
		}
		rcmPanel.addControllerPanel(controller.getName(), presenter.getPanel());
		controllerPresenterS.add(presenter);
	}


	@Override
	public void onStartStopButtonPressed(final boolean activeState)
	{
		// --- Start polling when start-button pressed ---
		if (activeState)
		{
			for (AControllerPresenter cP : controllerPresenterS)
			{
				ActionSender actionSender = new ActionSender(cP.getController().getName());
				actionSenders.add(actionSender);
				actionSender.addObserver(this);
				cP.startPolling(actionSender);
			}
			rcmPanel.startRcm();
		}
		// --- Stop polling when stop-button pressed ---
		else
		{
			for (final AControllerPresenter cP : controllerPresenterS)
			{
				cP.getActionSender().removeObserver(this);
				cP.getActionSender().setInterpreter(new CommandInterpreterStub());
				actionSenders.remove(cP.getActionSender());
				cP.stopPolling();
				cP.getPanel().setSelectedBot(BotID.noBot());
			}
			if (botManager != null)
			{
				refreshBotControllers(botManager.getBots().values());
			}
			rcmPanel.stopRcm();
		}
	}


	private void refreshBotControllers(Collection<ABot> bots)
	{
		for (ABot bot : bots)
		{
			if (!bot.getControlledBy().isEmpty())
			{
				bot.setControlledBy("");
			}
		}
	}


	@Override
	public void onReconnect(final boolean keepConnections)
	{
		List<ABot> bots = new ArrayList<>(controllerPresenterS.size());
		if (keepConnections)
		{
			for (AControllerPresenter cp : controllerPresenterS)
			{
				bots.add(cp.getActionSender().getCmdInterpreter().getBot());
			}
		}
		onStartStopButtonPressed(false);
		onStartStopButtonPressed(true);
		if (keepConnections)
		{
			for (int i = 0; (i < controllerPresenterS.size()) && (i < bots.size()); i++)
			{
				if (bots.get(i).getBotId().isBot())
				{
					changeBotAssignment(controllerPresenterS.get(i).getActionSender(), bots.get(i));
				}
			}
		}
	}


	@Override
	public void onBotUnassigned(final ActionSender actionSender)
	{
		for (AControllerPresenter cP : controllerPresenterS)
		{
			if (cP.getActionSender() == actionSender)
			{
				cP.getPanel().setSelectedBot(BotID.noBot());
				break;
			}
		}
	}


	@Override
	public synchronized void onNextBot(final ActionSender actionSender)
	{
		switchBot(actionSender, 1);
	}


	@Override
	public synchronized void onPrevBot(final ActionSender actionSender)
	{
		switchBot(actionSender, -1);
	}


	/**
	 * Try to switch given robot. If there is a free robot available, it will be returned.
	 * Else, the given robot will be
	 * returned
	 *
	 * @param actionSender
	 * @param inc -1 or 1 (endless loop else...)
	 */
	private void switchBot(final ActionSender actionSender, final int inc)
	{
		ICommandInterpreter interpreter = actionSender.getCmdInterpreter();
		ABot oldBot = null;
		if (interpreter != null)
		{
			oldBot = interpreter.getBot();
		}
		int initId = 0;
		if ((oldBot != null) && oldBot.getBotId().isBot())
		{
			initId = oldBot.getBotId().getNumberWithColorOffsetBS();
		}
		int idMax = (AObjectID.BOT_ID_MAX * 2) + 2;
		assert initId < idMax;
		assert initId >= 0;
		for (int i = 0; i < idMax; i++)
		{
			int id = (initId + (inc * i) + idMax) % idMax;
			BotID botId = BotID.createBotIdFromIdWithColorOffsetBS(id);
			ABot bot = botManager.getBots().get(botId);
			if (bot != null && ((bot != oldBot) && !bot.isBlocked()
					&& !bot.isHideFromRcm()))
			{
				updateBotOwnership(oldBot);
				changeBotAssignment(actionSender, bot);
				return;
			}
		}
	}


	private void updateBotOwnership(ABot oldBot)
	{
		if ((oldBot != null) && !oldBot.getControlledBy().isEmpty())
		{
			oldBot.setControlledBy("");
		}
	}


	private void changeBotAssignment(final ActionSender actionSender,
			final ABot bot)
	{
		ICommandInterpreter interpreter = actionSender.getCmdInterpreter();
		CommandInterpreter newInterpreter = getInterpreter(bot);
		if (interpreter != null)
		{
			ConfigRegistration.applySpezis(interpreter, "rcm", "");
		}
		ConfigRegistration.applySpezis(newInterpreter, "rcm", "");
		for (ActionSender sender : actionSenders)
		{
			if ((sender.getCmdInterpreter() == newInterpreter) && (sender.getCmdInterpreter() != interpreter))
			{
				log.warn("The interpreter is still assigned to another ActionSender, which should not happen");
				return;
			}
		}
		bot.setControlledBy(actionSender.getIdentifier());

		for (AControllerPresenter cP : controllerPresenterS)
		{
			if (cP.getActionSender() == actionSender)
			{
				cP.getPanel().setSelectedBot(bot.getBotId());
				newInterpreter.setCompassThreshold(cP.getConfig().getConfigValues()
						.get(ERcmControllerConfig.DEADZONE));
				break;
			}
		}

		actionSender.setInterpreter(newInterpreter);
	}


	@Override
	public void onModuliStateChanged(final ModulesState state)
	{
		super.onModuliStateChanged(state);
		switch (state)
		{
			case ACTIVE:
				handleModuliActivation();
				break;
			case RESOLVED:
				handleModuliResolution();
				break;
			case NOT_LOADED:
			default:
				break;
		}
	}


	private void handleModuliActivation()
	{
		botManager = SumatraModel.getInstance().getModule(ABotManager.class);
		rcmPanel.start();
	}


	private void handleModuliResolution()
	{
		onStartStopButtonPressed(false);
		rcmPanel.stop();
	}


	private CommandInterpreter getInterpreter(final ABot bot)
	{
		CommandInterpreter interpreter = botInterpreters.get(bot.getBotId());
		if ((interpreter == null) || (interpreter.getBot() != bot))
		{
			interpreter = new CommandInterpreter(bot);
			botInterpreters.put(bot.getBotId(), interpreter);
		}
		return interpreter;
	}


	@Override
	public Component getComponent()
	{
		return rcmPanel;
	}


	@Override
	public ISumatraView getSumatraView()
	{
		return rcmPanel;
	}
}
