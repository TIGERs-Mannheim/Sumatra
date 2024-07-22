/*
 * Copyright (c) 2009 - 2019, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.botparams;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnore;
import edu.tigers.sumatra.bot.params.BotParams;
import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * Database for bot parameters.
 */
@JsonAutoDetect(fieldVisibility = Visibility.ANY)
public class BotParamsDatabase
{
	@JsonIgnore
	private static final Logger log = LogManager.getLogger(BotParamsDatabase.class.getName());

	@JsonIgnore
	private final List<IBotParamsDatabaseObserver> observers = new CopyOnWriteArrayList<>();

	private Map<EBotParamLabel, String> selectedParams = new EnumMap<>(EBotParamLabel.class);
	private Map<String, BotParams> entries = new HashMap<>();


	/**
	 * Constructor
	 */
	public BotParamsDatabase()
	{
		for (EBotParamLabel label : EBotParamLabel.values())
		{
			selectedParams.put(label, "Unknown");
		}

		entries.put("Unknown", new BotParams());
	}


	/**
	 * Add a new team with default parameters.
	 * If the team already exists it is overwritten with default parameters
	 *
	 * @param teamName
	 */
	public void addEntry(final String teamName)
	{
		entries.put(teamName, new BotParams());

		notifyEntryAdded(teamName, entries.get(teamName));
	}


	/**
	 * Add a new team by copying parameters from an existing team
	 * If a team already exists, it is overwritten with the parameters copied from the other team.
	 * If the team to copy from does not exist, the new team is created with default parameters.
	 *
	 * @param teamName
	 * @param copyTeam
	 */
	public void copyEntry(final String teamName, final String copyTeam)
	{
		if (entries.containsKey(copyTeam))
		{
			entries.put(teamName, entries.get(copyTeam));
		} else
		{
			addEntry(teamName);
		}
		notifyEntryAdded(teamName, entries.get(teamName));
	}


	/**
	 * Update parameters for an entry. If the entry does not exist it will be created.
	 *
	 * @param teamName
	 * @param newParams
	 */
	public void updateEntry(final String teamName, final BotParams newParams)
	{
		entries.put(teamName, newParams);

		notifyEntryUpdated(teamName, newParams);
	}


	/**
	 * Remove a specific entry.
	 * Entry must not be used by any label!
	 *
	 * @param teamName
	 */
	public void deleteEntry(final String teamName)
	{
		Validate.isTrue(entries.containsKey(teamName));

		long teamUsedAtLabels = selectedParams.values().stream().filter(t -> t.equals(teamName)).count();
		Validate.isTrue(teamUsedAtLabels == 0);

		entries.remove(teamName);

		notifyEntryUpdated(teamName, null);
	}


	/**
	 * Select a team for a specific {@link EBotParamLabel}
	 *
	 * @param label
	 * @param teamName
	 */
	public void setTeamForLabel(final EBotParamLabel label, final String teamName)
	{
		Validate.isTrue(entries.containsKey(teamName));

		selectedParams.put(label, teamName);

		notifyBotParamLabelUpdated(label, teamName);
	}


	/**
	 * Get the team name for a label.
	 *
	 * @param label predefined enum for teams
	 * @return the team name
	 */
	public String getTeamStringForLabel(final EBotParamLabel label)
	{
		Validate.isTrue(selectedParams.containsKey(label));

		return selectedParams.get(label);
	}


	/**
	 * Get {@link BotParams} for a labelled type.
	 *
	 * @param label
	 * @return
	 */
	public BotParams getSelectedParams(final EBotParamLabel label)
	{
		String team = selectedParams.get(label);
		if (team == null)
		{
			log.warn("No team selected for label: {}, using defaults", label);
			return new BotParams();
		}

		BotParams params = entries.get(team);
		if (params == null)
		{
			log.error("Labelled params {} do not exist, using defaults", label);
			return new BotParams();
		}

		return params;
	}


	/**
	 * @return the entries
	 */
	public Map<String, BotParams> getEntries()
	{
		return entries;
	}


	/**
	 * @return the selectedParams
	 */
	public Map<EBotParamLabel, String> getSelectedParams()
	{
		return selectedParams;
	}


	/**
	 * @param observer
	 */
	public void addObserver(final IBotParamsDatabaseObserver observer)
	{
		observers.add(observer);
	}


	/**
	 * @param observer
	 */
	public void removeObserver(final IBotParamsDatabaseObserver observer)
	{
		observers.remove(observer);
	}


	private void notifyEntryAdded(final String entry, final BotParams newParams)
	{
		for (IBotParamsDatabaseObserver observer : observers)
		{
			observer.onEntryAdded(entry, newParams);
		}
	}


	private void notifyEntryUpdated(final String entry, final BotParams newParams)
	{
		for (IBotParamsDatabaseObserver observer : observers)
		{
			observer.onEntryUpdated(entry, newParams);
		}
	}


	private void notifyBotParamLabelUpdated(final EBotParamLabel label, final String newEntry)
	{
		for (IBotParamsDatabaseObserver observer : observers)
		{
			observer.onBotParamLabelUpdated(label, newEntry);
		}
	}


	/**
	 * Observer interface for {@link BotParamsDatabase}
	 */
	public interface IBotParamsDatabaseObserver
	{
		/**
		 * Entry added.
		 *
		 * @param entry
		 * @param newParams
		 */
		void onEntryAdded(String entry, BotParams newParams);


		/**
		 * Entry updated/deleted.
		 *
		 * @param entry
		 * @param newParams
		 */
		void onEntryUpdated(String entry, BotParams newParams);


		/**
		 * Label selected.
		 *
		 * @param label
		 * @param newEntry
		 */
		void onBotParamLabelUpdated(EBotParamLabel label, String newEntry);
	}
}
