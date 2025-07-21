/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp;

import edu.tigers.sumatra.drawable.ShapeMap;
import edu.tigers.sumatra.drawable.ShapeMapSource;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.persistence.ABufferedPersistenceRecorder;
import edu.tigers.sumatra.persistence.PersistenceDb;


/**
 * Persistance storage for cam frames
 */
public class ShapeMapPersistenceRecorder extends ABufferedPersistenceRecorder<PersistenceShapeMapFrame>
		implements IWorldFrameObserver
{
	/**
	 * Create persistance storage for shape maps
	 */
	public ShapeMapPersistenceRecorder(PersistenceDb db)
	{
		super(db, PersistenceShapeMapFrame.class);
	}


	@Override
	public void start()
	{
		AWorldPredictor wp = SumatraModel.getInstance().getModule(AWorldPredictor.class);
		wp.addObserver(this);
	}


	@Override
	public void stop()
	{
		AWorldPredictor wp = SumatraModel.getInstance().getModule(AWorldPredictor.class);
		wp.removeObserver(this);
	}


	@Override
	public void onNewShapeMap(final long timestamp, final ShapeMap shapeMap, final ShapeMapSource source)
	{
		PersistenceShapeMapFrame frame = new PersistenceShapeMapFrame(timestamp);
		ShapeMap shapeMapCopy = new ShapeMap();
		shapeMapCopy.addAll(shapeMap);
		shapeMapCopy.removeNonPersistent();
		frame.putShapeMap(source, shapeMapCopy);
		queue(frame);
	}
}
