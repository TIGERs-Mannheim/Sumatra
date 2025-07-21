/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.gui.rcm.presenter;

import com.github.g3force.configurable.ConfigRegistration;
import edu.tigers.sumatra.botmanager.BotManager;
import edu.tigers.sumatra.botmanager.bots.ABot;
import edu.tigers.sumatra.gui.rcm.view.RCMPanel;
import edu.tigers.sumatra.ids.AObjectID;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.rcm.ActionSender;
import edu.tigers.sumatra.rcm.CommandInterpreter;
import edu.tigers.sumatra.rcm.CommandInterpreterStub;
import edu.tigers.sumatra.rcm.ICommandInterpreter;
import edu.tigers.sumatra.rcm.IRCMObserver;
import edu.tigers.sumatra.rcm.RcmActionMap.ERcmControllerConfig;
import edu.tigers.sumatra.views.ISumatraViewPresenter;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;

import javax.swing.SwingUtilities;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * This class enables clients to send commands directly to the bots
 */
@Log4j2
public class RCMPresenter implements ISumatraViewPresenter, IRCMObserver
{
	private BotManager botManager = null;

	private final Map<BotID, CommandInterpreter> botInterpreters = new HashMap<>();
	private final List<ActionSender> actionSenders = new LinkedList<>();

	private final List<AControllerPresenter> controllerPresenterS = new ArrayList<>();
	@Getter
	private final RCMPanel viewPanel = new RCMPanel();


	static
	{
		// set library path for jinput
		final String curDir = System.getProperty("user.dir");
		System.setProperty("net.java.games.input.librarypath",
				curDir + "/modules/sumatra-rcm/build/natives");
	}


	public RCMPresenter()
	{
		viewPanel.addObserver(this);
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
			log.info("Controller found: {}", controller.getName());
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
		viewPanel.clearControllerPanels();
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
		viewPanel.addControllerPanel(controller.getName(), presenter.getPanel());
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
			viewPanel.startRcm();
			if (botManager != null)
			{
				botManager.addAllocatedBots(getClass().getCanonicalName(), BotID.getAll());
			}
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
				botManager.removeAllocatedBots(getClass().getCanonicalName(), BotID.getAll());
			}
			viewPanel.stopRcm();
		}
	}


	private void refreshBotControllers(Collection<? extends ABot> bots)
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
	 * @param inc          -1 or 1 (endless loop else...)
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
			if (bot != null && ((bot != oldBot) && !bot.isBlocked()))
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
	public void onModuliStarted()
	{
		ISumatraViewPresenter.super.onModuliStarted();
		botManager = SumatraModel.getInstance().getModule(BotManager.class);
		SwingUtilities.invokeLater(viewPanel::start);
	}


	@Override
	public void onModuliStopped()
	{
		ISumatraViewPresenter.super.onModuliStopped();
		onStartStopButtonPressed(false);
		SwingUtilities.invokeLater(viewPanel::stop);
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
}
