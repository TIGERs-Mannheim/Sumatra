package edu.tigers.sumatra.botparams;


import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.core.util.Separators;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import edu.tigers.sumatra.bot.params.BotParams;
import edu.tigers.sumatra.botparams.BotParamsDatabase.IBotParamsDatabaseObserver;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.moduli.AModule;
import edu.tigers.sumatra.referee.AReferee;
import edu.tigers.sumatra.referee.IRefereeObserver;
import edu.tigers.sumatra.referee.proto.SslGcRefereeMessage;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.fasterxml.jackson.core.PrettyPrinter.DEFAULT_SEPARATORS;


/**
 * BotParams module.
 *
 * @author AndreR <andre@ryll.cc>
 */
public class BotParamsManager extends AModule implements IBotParamsDatabaseObserver, BotParamsProvider
{
	private static final Logger log = LogManager.getLogger(BotParamsManager.class.getName());
	private static final String DATABASE_FILE = "config/botParamsDatabase.json";
	private final List<IBotParamsManagerObserver> observers = new CopyOnWriteArrayList<>();
	private BotParamsDatabase database;
	private RefereeObserver refObserver;


	@Override
	public void initModule()
	{
		database = loadDatabase(databaseFile());
		database.addObserver(this);
		refObserver = new RefereeObserver();

	}


	@Override
	public void stopModule()
	{
		saveDatabase();
		refObserver.onStopModuli();
	}


	/**
	 * Only used internally for presenter.
	 *
	 * @return
	 */
	public BotParamsDatabase getDatabase()
	{
		return database;
	}


	/**
	 * Enable choice of opponent team by referee information.
	 *
	 * @param enable
	 */
	public void activateAutomaticChoiceOfOpponent(boolean enable)
	{
		refObserver.setAutoSetOpponent(enable);
	}


	public void activateAutomaticAdaptionOfOpponent(boolean enable)
	{
		refObserver.setAutoLiveOpponent(enable);
	}


	@Override
	public BotParams get(final EBotParamLabel label)
	{
		return database.getSelectedParams(label);
	}


	private void saveDatabase()
	{
		saveDatabase(databaseFile(), database);
	}


	private static File databaseFile()
	{
		return Paths.get(DATABASE_FILE).toFile();
	}


	static void saveDatabase(final File file, final BotParamsDatabase database)
	{
		String lineFeed = detectLineFeed(file);
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		mapper.setDefaultPrettyPrinter(new DefaultPrettyPrinter()
				.withObjectIndenter(new DefaultIndenter().withLinefeed(lineFeed))
				.withSeparators(DEFAULT_SEPARATORS.withObjectFieldValueSpacing(Separators.Spacing.AFTER))
		);

		try
		{
			mapper.writeValue(file, database);
		} catch (IOException e)
		{
			log.error("", e);
		}
	}


	private static String detectLineFeed(final File file)
	{
		if (file.exists())
		{
			try
			{
				String content = Files.readString(file.toPath());
				if (content.contains("\r\n"))
				{
					return "\r\n";
				}
				if (content.contains("\n"))
				{
					return "\n";
				}
			} catch (IOException e)
			{
				log.warn("Could not read existing database to detect line feed: {}", file, e);
			}
		}
		return System.lineSeparator();
	}


	static BotParamsDatabase loadDatabase(final File file)
	{
		if (file.exists())
		{
			log.debug("Open existing bot params database");
			ObjectMapper mapper = new ObjectMapper();
			mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

			try
			{
				return mapper.readValue(file, BotParamsDatabase.class);
			} catch (IOException e)
			{
				log.error("Could not read from database: {}", file, e);
			}
		}
		log.info("Initializing empty bot params database");
		return new BotParamsDatabase();
	}


	@Override
	public void onEntryAdded(final String entry, final BotParams newParams)
	{
		saveDatabase();
	}


	@Override
	public void onEntryUpdated(final String entry, final BotParams newParams)
	{
		saveDatabase();

		database.getSelectedParams().entrySet().stream()
				.filter(e -> e.getValue().equals(entry))
				.forEach(e -> notifyBotParamsUpdated(e.getKey(), newParams));
	}


	@Override
	public void onBotParamLabelUpdated(final EBotParamLabel label, final String newEntry)
	{
		saveDatabase();

		notifyBotParamsUpdated(label, get(label));
	}


	/**
	 * @param observer
	 */
	public void addObserver(final IBotParamsManagerObserver observer)
	{
		observers.add(observer);
	}


	/**
	 * @param observer
	 */
	public void removeObserver(final IBotParamsManagerObserver observer)
	{
		observers.remove(observer);
	}


	private void notifyBotParamsUpdated(final EBotParamLabel label, final BotParams params)
	{
		for (IBotParamsManagerObserver observer : observers)
		{
			observer.onBotParamsUpdated(label, params);
		}
	}


	/**
	 * BotParamsManager observer.
	 */
	@FunctionalInterface
	public interface IBotParamsManagerObserver
	{
		/**
		 * Bot parameters of a specific label have changed.
		 *
		 * @param label
		 * @param params
		 */
		void onBotParamsUpdated(EBotParamLabel label, BotParams params);
	}

	private class RefereeObserver implements IRefereeObserver
	{
		private AReferee referee;
		@Setter
		private boolean autoSetOpponent;
		@Setter
		private boolean autoLiveOpponent;


		public RefereeObserver()
		{
			SumatraModel.getInstance().getModuleOpt(AReferee.class).ifPresent(ref -> {
				referee = ref;
				referee.addObserver(this);
				onRefereeMsgSourceChanged(referee.getActiveSource());
			});
		}


		public void onStopModuli()
		{
			if (referee != null)
			{
				referee.removeObserver(this);
				referee = null;
			}
		}


		@Override
		public void onNewRefereeMsg(final SslGcRefereeMessage.Referee msg)
		{
			if (SumatraModel.getInstance().isSimulation())
			{
				return;
			}

			String yellowTeam = msg.getYellow().getName();
			boolean opponentIsBlue = yellowTeam.contains("TIGERs");
			String opponentTeam = opponentIsBlue ? msg.getBlue().getName() : yellowTeam;

			if (!database.getEntries().containsKey(opponentTeam))
			{
				database.copyEntry(opponentTeam, "Unknown");
			}

			if (autoSetOpponent)
			{
				database.setTeamForLabel(EBotParamLabel.OPPONENT, opponentTeam);
			}

			if (autoLiveOpponent)
			{
				database.setTeamForLabel(
						opponentIsBlue ? EBotParamLabel.BLUE_LIVE : EBotParamLabel.YELLOW_LIVE, opponentTeam);
			}
		}
	}
}
