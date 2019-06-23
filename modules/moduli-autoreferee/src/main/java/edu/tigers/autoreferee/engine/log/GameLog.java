/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoreferee.engine.log;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import org.apache.log4j.Logger;

import edu.tigers.autoreferee.engine.FollowUpAction;
import edu.tigers.autoreferee.engine.RefboxRemoteCommand;
import edu.tigers.autoreferee.engine.events.IGameEvent;
import edu.tigers.sumatra.referee.data.GameState;
import edu.tigers.sumatra.referee.data.RefereeMsg;


/**
 * @author "Lukas Magel"
 */
public class GameLog implements IGameLog
{
	private static final Logger log = Logger.getLogger(GameLog.class);
	private final List<GameLogEntry> entries = new ArrayList<>();
	private long currentTimestamp = 0;
	private GameTime currentGameTime = GameTime.empty();
	private List<IGameLogObserver> observer = new CopyOnWriteArrayList<>();
	
	
	private Instant getCurrentInstant()
	{
		return Instant.now();
	}
	
	
	@Override
	public List<GameLogEntry> getEntries()
	{
		return Collections.unmodifiableList(entries);
	}
	
	
	@Override
	public void clearEntries()
	{
		entries.clear();
		observer.forEach(IGameLogObserver::onClear);
	}
	
	
	/**
	 * Set the current frame timestamp which will be used as reference timestamp for all entries added after this
	 * invocation
	 * 
	 * @param timestamp the current frame timestamp in nanoseconds
	 */
	public void setCurrentTimestamp(final long timestamp)
	{
		currentTimestamp = timestamp;
	}
	
	
	/**
	 * Set the current game time
	 * 
	 * @param time
	 */
	public void setCurrentGameTime(final GameTime time)
	{
		currentGameTime = time;
	}
	
	
	private void addEntryToLog(final GameLogEntry entry)
	{
		int id;
		synchronized (entries)
		{
			entries.add(entry);
			id = entries.size() - 1;
		}
		log.debug(entry);
		observer.forEach(obs -> obs.onNewEntry(id, entry));
	}
	
	
	private GameLogEntry buildEntry(final Consumer<? super GameLogEntryBuilder> consumer)
	{
		GameLogEntryBuilder builder = new GameLogEntryBuilder();
		builder.setTimestamp(currentTimestamp);
		builder.setGameTime(currentGameTime);
		builder.setInstant(getCurrentInstant());
		
		consumer.accept(builder);
		return builder.toEntry();
	}
	
	
	private GameLogEntry buildAndAddEntry(final Consumer<? super GameLogEntryBuilder> consumer)
	{
		GameLogEntry entry = buildEntry(consumer);
		addEntryToLog(entry);
		return entry;
	}
	
	
	/**
	 * @param gamestate
	 * @return
	 */
	public GameLogEntry addEntry(final GameState gamestate)
	{
		return buildAndAddEntry(builder -> builder.setGamestate(gamestate));
	}
	
	
	/**
	 * @param event
	 * @return
	 */
	public GameLogEntry addEntry(final IGameEvent event)
	{
		return addEntry(event, false);
	}
	
	
	/**
	 * @param event
	 * @param acceptedByEngine true if the game event was accepted by the engine and caused a change of game state
	 * @return
	 */
	public GameLogEntry addEntry(final IGameEvent event, final boolean acceptedByEngine)
	{
		return buildAndAddEntry(builder -> builder.setGameEvent(event, acceptedByEngine));
	}
	
	
	/**
	 * @param refereeMsg
	 * @return
	 */
	public GameLogEntry addEntry(final RefereeMsg refereeMsg)
	{
		return buildAndAddEntry(builder -> builder.setRefereeMsg(refereeMsg));
	}
	
	
	/**
	 * @param action
	 * @return
	 */
	public GameLogEntry addEntry(final FollowUpAction action)
	{
		return buildAndAddEntry(builder -> builder.setFollowUpAction(action));
	}
	
	
	/**
	 * @param command
	 * @return
	 */
	public GameLogEntry addEntry(final RefboxRemoteCommand command)
	{
		return buildAndAddEntry(builder -> builder.setCommand(command));
	}
	
	
	/**
	 * @param observer
	 */
	@Override
	public void addObserver(final IGameLogObserver observer)
	{
		this.observer.add(observer);
	}
	
	
	/**
	 * @param observer
	 */
	@Override
	public void removeObserver(final IGameLogObserver observer)
	{
		this.observer.remove(observer);
	}
	
	/**
	 * @author "Lukas Magel"
	 */
	public interface IGameLogObserver
	{
		/**
		 * @param id
		 * @param entry
		 */
		void onNewEntry(int id, GameLogEntry entry);
		
		
		void onClear();
	}
}
