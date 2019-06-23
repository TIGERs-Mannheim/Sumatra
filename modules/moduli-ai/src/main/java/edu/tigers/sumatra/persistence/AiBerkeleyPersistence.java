/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.persistence;

import java.util.Collection;

import org.apache.commons.lang.time.DurationFormatUtils;
import org.apache.log4j.Logger;

import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.evolve.Deleter;
import com.sleepycat.persist.evolve.Mutations;
import com.sleepycat.persist.evolve.Renamer;

import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.frames.VisualizationFrame;
import edu.tigers.sumatra.ai.metis.statistics.PenaltyStats;
import edu.tigers.sumatra.ai.metis.statistics.StatisticData;
import edu.tigers.sumatra.bot.RobotInfo;
import edu.tigers.sumatra.botmanager.bots.ABot;
import edu.tigers.sumatra.botmanager.bots.TigerBotV3;
import edu.tigers.sumatra.drawable.ColorProxy;
import edu.tigers.sumatra.statistics.Percentage;
import edu.tigers.sumatra.util.ConcurrentHashMapProxy;
import edu.tigers.sumatra.util.EnumMapProxy;
import edu.tigers.sumatra.util.LinkedHashSetProxy;
import edu.tigers.sumatra.wp.data.SimpleWorldFrame;


/**
 * Persistence layer for AI
 */
public class AiBerkeleyPersistence extends VisionBerkeleyPersistence
{
	private static final Logger log = Logger
			.getLogger(AiBerkeleyPersistence.class.getName());
	
	private BerkeleyAccessorByTimestamp<RecordFrame> recordFrameAccessor;
	
	
	/**
	 * @param dbPath absolute path to database folder or zip file
	 */
	public AiBerkeleyPersistence(String dbPath)
	{
		super(dbPath);
		
		Mutations mutations = new Mutations();
		mutations.addDeleter(new Deleter(SimpleWorldFrame.class.getCanonicalName(), 2, "frame"));
		mutations.addDeleter(new Deleter(ABot.class.getCanonicalName(), 0, "battery"));
		mutations.addDeleter(new Deleter(TigerBotV3.class.getCanonicalName(), 1, "relBattery"));
		mutations.addDeleter(new Deleter(ABot.class.getCanonicalName(), 1, "performance"));
		mutations.addDeleter(new Deleter(ABot.class.getCanonicalName(), 0, "performance"));
		mutations.addDeleter(new Deleter(RobotInfo.class.getCanonicalName(), 1, "availableToAi"));
		mutations.addDeleter(new Deleter(TacticalField.class.getCanonicalName(), 4, "statistics"));
		mutations.addDeleter(new Deleter("edu.tigers.sumatra.ai.data.MatchStatistics", 4));
		mutations.addDeleter(new Deleter("edu.tigers.sumatra.ai.data.MatchStatistics$EAvailableStatistic", 0));
		mutations.addDeleter(new Deleter(Percentage.class.getCanonicalName(), 0, "percent"));
		mutations.addDeleter(new Deleter(VisualizationFrame.class.getCanonicalName(), 4, "matchStatistics"));
		
		mutations.addRenamer(new Renamer("edu.tigers.sumatra.ai.data.statistics.StatisticData", 0,
				StatisticData.class.getCanonicalName()));
		mutations.addRenamer(new Renamer("edu.tigers.sumatra.ai.data.statistics.PenaltyStats", 0,
				PenaltyStats.class.getCanonicalName()));
		mutations.addRenamer(
				new Renamer(StatisticData.class.getCanonicalName(), 1, "botspecificStatistics", "botSpecificStatistics"));
		
		getEnv().getStoreConfig().setMutations(mutations);
		
		getEnv().getModel().registerClass(ColorProxy.class);
		getEnv().getModel().registerClass(EnumMapProxy.class);
		getEnv().getModel().registerClass(ConcurrentHashMapProxy.class);
		getEnv().getModel().registerClass(LinkedHashSetProxy.class);
	}
	
	
	@Override
	public void open()
	{
		super.open();
		EntityStore store = getEnv().getEntityStore();
		recordFrameAccessor = new BerkeleyAccessorByTimestamp<>(store, RecordFrame.class);
	}
	
	
	@Override
	public void close()
	{
		Long firstKey = getFirstKey();
		Long lastKey = getLastKey();
		if (firstKey != null && lastKey != null)
		{
			long duration = (long) ((lastKey - firstKey) / 1e6);
			long frames = getNumberOfRecordFrames();
			String period = DurationFormatUtils.formatDuration(duration, "HH:mm:ss", true);
			log.info("Closing DB with " + frames + " frames and a period of " + period);
		}
		super.close();
	}
	
	
	/**
	 * @return total number of record frames
	 */
	public long getNumberOfRecordFrames()
	{
		return recordFrameAccessor.size();
	}
	
	
	/**
	 * Get nearest frame to given timestamp
	 * 
	 * @param tCur nearest timestamp
	 * @return frame
	 */
	public RecordFrame getRecordFrame(final long tCur)
	{
		return recordFrameAccessor.get(tCur);
	}
	
	
	/**
	 * save all frames to database
	 * 
	 * @param recordFrames frames to save
	 */
	public void saveRecordFrames(final Collection<RecordFrame> recordFrames)
	{
		recordFrameAccessor.saveFrames(recordFrames);
	}
	
	
	@Override
	public Long getFirstKey()
	{
		Long k1 = super.getFirstKey();
		Long k2 = recordFrameAccessor.getFirstKey();
		return getSmallerKey(k1, k2);
	}
	
	
	@Override
	public Long getLastKey()
	{
		Long k1 = super.getLastKey();
		Long k2 = recordFrameAccessor.getLastKey();
		return getLargerKey(k1, k2);
	}
	
	
	@Override
	public Long getKey(final long tCur)
	{
		Long k1 = super.getKey(tCur);
		Long k2 = recordFrameAccessor.getKey(tCur);
		return getNearestKey(tCur, k1, k2);
	}
	
	
	@Override
	public Long getNextKey(final long key)
	{
		Long k1 = super.getNextKey(key);
		Long k2 = recordFrameAccessor.getNextKey(key);
		return getNearestKey(key, k1, k2);
	}
	
	
	@Override
	public Long getPreviousKey(final long key)
	{
		Long k1 = super.getPreviousKey(key);
		Long k2 = recordFrameAccessor.getPreviousKey(key);
		return getNearestKey(key, k1, k2);
	}
}
