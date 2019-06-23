/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp;

import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;

import edu.tigers.sumatra.drawable.ShapeMap;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.persistence.BerkeleyDb;
import edu.tigers.sumatra.persistence.IBerkeleyRecorder;


/**
 * Berkeley storage for cam frames
 */
public class ShapeMapBerkeleyRecorder implements IBerkeleyRecorder
{
	private static final long BUFFER_TIME = 1_000_000_000L;
	private final WfwObserver wfwObserver = new WfwObserver();
	private final BerkeleyDb db;
	private final Deque<ShapeMapWithSource> buffer = new ConcurrentLinkedDeque<>();
	private long latestWrittenTimestamp = 0;
	private long latestReceivedTimestamp = 0;
	private boolean running = false;
	
	
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
		
		ShapeMapWithSource s = buffer.pollFirst();
		while (s != null)
		{
			// drop frame, if it is too old
			if (s.timestamp > latestWrittenTimestamp)
			{
				// frame is not too old, check if it is still buffering
				if (isBuffering(s.timestamp))
				{
					// this frame is still within the buffering time and so will all following
					// we have to re-add it to the buffer for next time
					buffer.addFirst(s);
					break;
				}
				BerkeleyShapeMapFrame f = toSave.computeIfAbsent(s.timestamp, BerkeleyShapeMapFrame::new);
				f.putShapeMap(s.source, s.shapeMap);
			}
			
			s = buffer.poll();
		}
		
		latestWrittenTimestamp = toSave.keySet().stream().mapToLong(i -> i).max().orElse(latestWrittenTimestamp);
		
		db.write(BerkeleyShapeMapFrame.class, toSave.values());
	}
	
	
	private boolean isBuffering(long timestamp)
	{
		return running && timestamp >= latestReceivedTimestamp - BUFFER_TIME;
	}
	
	private static class ShapeMapWithSource
	{
		long timestamp;
		ShapeMap shapeMap;
		String source;
		
		
		public ShapeMapWithSource(final long timestamp, final ShapeMap shapeMap, final String source)
		{
			this.timestamp = timestamp;
			this.shapeMap = shapeMap;
			this.source = source;
		}
	}
	
	private class WfwObserver implements IWorldFrameObserver
	{
		@Override
		public void onNewShapeMap(final long timestamp, final ShapeMap shapeMap, final String source)
		{
			ShapeMap shapeMapCopy = new ShapeMap(shapeMap);
			shapeMapCopy.removeNonPersistent();
			buffer.addLast(new ShapeMapWithSource(timestamp, shapeMapCopy, source));
			latestReceivedTimestamp = timestamp;
		}
	}
}
