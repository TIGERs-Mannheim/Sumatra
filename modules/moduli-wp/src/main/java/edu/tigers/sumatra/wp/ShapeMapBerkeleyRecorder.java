/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp;

import edu.tigers.sumatra.drawable.ShapeMap;
import edu.tigers.sumatra.drawable.ShapeMapSource;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.persistence.BerkeleyDb;
import edu.tigers.sumatra.persistence.IBerkeleyRecorder;
import lombok.extern.log4j.Log4j2;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;


/**
 * Berkeley storage for cam frames
 */
@Log4j2
public class ShapeMapBerkeleyRecorder implements IBerkeleyRecorder
{
	private static final int MAX_BUFFER_SIZE = 10000;
	private static final long BUFFER_TIME = 1_000_000_000L;
	private final WfwObserver wfwObserver = new WfwObserver();
	private final BerkeleyDb db;
	private final Map<Long, Map<ShapeMapSource, ShapeMap>> buffer = new ConcurrentSkipListMap<>();
	private long latestReceivedTimestamp = 0;
	private boolean running = false;
	private boolean droppingFrames = false;


	/**
	 * Create berkeley storage for cam frames
	 */
	public ShapeMapBerkeleyRecorder(BerkeleyDb db)
	{
		this.db = db;
	}


	@Override
	public void start()
	{
		AWorldPredictor wp = SumatraModel.getInstance().getModule(AWorldPredictor.class);
		wp.addObserver(wfwObserver);
		running = true;
	}


	@Override
	public void stop()
	{
		AWorldPredictor wp = SumatraModel.getInstance().getModule(AWorldPredictor.class);
		wp.removeObserver(wfwObserver);
		running = false;
	}


	@Override
	public void flush()
	{
		Map<Long, BerkeleyShapeMapFrame> toSave = new HashMap<>();
		for (var entry : buffer.entrySet())
		{
			if (isBuffering(entry.getKey()))
			{
				break;
			}
			var frame = new BerkeleyShapeMapFrame(entry.getKey());
			entry.getValue().forEach(frame::putShapeMap);
			toSave.put(entry.getKey(), frame);
			buffer.remove(entry.getKey());
		}

		db.write(BerkeleyShapeMapFrame.class, toSave.values());
	}


	private boolean isBuffering(long timestamp)
	{
		return running && timestamp >= latestReceivedTimestamp - BUFFER_TIME;
	}


	private class WfwObserver implements IWorldFrameObserver
	{
		@Override
		public void onNewShapeMap(final long timestamp, final ShapeMap shapeMap, final ShapeMapSource source)
		{

			if (buffer.size() > MAX_BUFFER_SIZE)
			{
				if (!droppingFrames)
				{
					log.warn("ShapeMap buffer is full. Dropping frames!");
					droppingFrames = true;
				}
			} else
			{
				var frame = buffer.computeIfAbsent(timestamp, k -> new ConcurrentHashMap<>());
				ShapeMap shapeMapCopy = new ShapeMap();
				shapeMapCopy.addAll(shapeMap);
				shapeMapCopy.removeNonPersistent();
				frame.put(source, shapeMapCopy);
				latestReceivedTimestamp = Math.max(timestamp, latestReceivedTimestamp);
				droppingFrames = false;
			}
		}
	}
}
