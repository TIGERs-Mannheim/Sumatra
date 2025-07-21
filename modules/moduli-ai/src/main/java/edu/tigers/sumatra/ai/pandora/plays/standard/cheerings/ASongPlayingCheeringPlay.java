/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.plays.standard.cheerings;

import edu.tigers.sumatra.ai.pandora.plays.standard.CheeringPlay;
import edu.tigers.sumatra.botmanager.botskills.data.ESong;
import lombok.Getter;
import org.apache.commons.lang.Validate;

import java.util.List;


/**
 * A play that generates interrupts based on a song to move along the music
 * <p>
 * Works best with songs that have at least 0.2s of silence in the end or are looped by the robot
 */
public abstract class ASongPlayingCheeringPlay implements ICheeringPlay
{
	private final List<ESong> songs;
	private final double songDuration;
	private final boolean songIsLooping;
	private final List<Double> interrupts;
	private final int numSongLoop;

	@Getter
	private CheeringPlay play;
	private long startingTimestamp = -1;
	@Getter
	private int interruptCounter = 0;
	@Getter
	private int loopCounter = 0;


	protected ASongPlayingCheeringPlay(List<ESong> songs, List<Double> interrupts, int numSongLoop)
	{
		Validate.isTrue(!songs.isEmpty());
		this.songDuration = songs.getFirst().getDuration();
		this.songIsLooping = songs.getFirst().isLooping();
		Validate.isTrue(songs.stream().allMatch(song -> song.getDuration() == songDuration));
		this.songs = songs;

		this.interrupts = interrupts.stream()
				.filter(interruptTime -> 0 <= interruptTime && interruptTime <= songDuration)
				.sorted().toList();
		this.numSongLoop = numSongLoop;
	}


	@Override
	public boolean isDone()
	{
		if (play == null)
		{
			// Not initialized yet
			return false;
		}
		return timeSinceStart() > numSongLoop * songDuration;
	}


	@Override
	public void initialize(CheeringPlay play)
	{
		this.play = play;
		startingTimestamp = -1;
		interruptCounter = 0;
		loopCounter = 0;
	}


	@Override
	public void doUpdate()
	{
		if (startingTimestamp == -1)
		{
			startingTimestamp = play.getWorldFrame().getTimestamp();
		}

		if (timeSinceStartOfLoop() >= songDuration - 0.2 && !songIsLooping)
		{
			// Restart song close to end if it is not automatically looping by the robot
			setSongs(List.of(ESong.NONE));
		} else
		{
			setSongs(songs);
		}
		if (timeSinceStartOfLoop() >= songDuration)
		{
			// Fire all remaining interrupts
			for (int i = interruptCounter; i < interrupts.size(); ++i)
			{
				interruptCounter = i;
				handleInterrupt(loopCounter, interruptCounter);
			}
			interruptCounter = 0;
			++loopCounter;

		}
		for (int i = interruptCounter; i < interrupts.size(); ++i)
		{
			if (interrupts.get(i) <= timeSinceStartOfLoop())
			{
				interruptCounter = i;
				handleInterrupt(loopCounter, interruptCounter);
				++interruptCounter;
			}
		}
	}


	protected double timeSinceStart()
	{
		if (startingTimestamp == -1)
		{
			return 0;
		}
		var now = play.getWorldFrame().getTimestamp();
		return (now - startingTimestamp) * 1e-9;
	}


	protected double timeSinceStartOfLoop()
	{
		var timeSinceStart = timeSinceStart();
		return timeSinceStart - songDuration * loopCounter;
	}


	abstract void handleInterrupt(int loopCount, int interruptCount);


	private void setSongs(List<ESong> songs)
	{
		var numSongs = songs.size();
		var roles = play.getPermutedRoles();
		for (int i = 0; i < roles.size(); i += numSongs)
		{
			for (int j = 0; j < numSongs; ++j)
			{
				if (i + j < roles.size())
				{
					play.setSong(roles.get(i + j).getBotID(), songs.get(j));
				}
			}
		}
	}
}
