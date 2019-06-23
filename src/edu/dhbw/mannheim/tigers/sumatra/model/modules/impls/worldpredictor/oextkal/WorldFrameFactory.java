/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 08.11.2011
 * Author(s): osteinbrecher
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.SimpleWorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeamColor;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.AVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.AVector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBall;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotIDMap;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.IBotIDMap;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.fieldanalysis.FieldRasterGenerator;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.WPConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.fieldPrediction.FieldPredictor;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.fieldPrediction.WorldFramePrediction;


/**
 * Factory which creates a list of {@link WorldFrame}s with random positioned bots.
 * 
 * @author Oliver Steinbrecher
 * 
 */
public class WorldFrameFactory
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private final Set<Integer>			selectionList;
	
	private int								runningIterator	= 0;
	
	private final List<WorldFrame>	wFrames;
	private FieldRasterGenerator		raster;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 */
	public WorldFrameFactory()
	{
		selectionList = new HashSet<Integer>();
		wFrames = new ArrayList<WorldFrame>();
		
		raster = new FieldRasterGenerator();
		createSelectionList();
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Creates a list of random numbers which specify the way
	 * robots are added in the analysing field raster.
	 * 
	 */
	private void createSelectionList()
	{
		final Random randomGenerator = new Random(System.nanoTime());
		
		while (selectionList.size() != FieldRasterGenerator.getNumberOfAnalysingFields())
		{
			final int randomField = randomGenerator.nextInt(FieldRasterGenerator.getNumberOfAnalysingFields()) + 1;
			if (!selectionList.contains(randomField))
			{
				selectionList.add(randomField);
			}
		}
	}
	
	
	/**
	 * Creates a list of {@link WorldFrame}.
	 * 
	 * @param number of frames
	 */
	public void createFrames(int number)
	{
		for (int i = 0; i < number; i++)
		{
			wFrames.add(createWorldFrame(i));
		}
	}
	
	
	/**
	 * @param frameNumber
	 * @return
	 */
	public WorldFrame createWorldFrame(long frameNumber)
	{
		return new WorldFrame(createSimpleWorldFrame(frameNumber), ETeamColor.YELLOW, false);
	}
	
	
	/**
	 * Creates a new WorldFrame with random positioned bots.
	 * 
	 * @param frameNumber the id of the {@link WorldFrame}.
	 * @return
	 */
	public SimpleWorldFrame createSimpleWorldFrame(long frameNumber)
	{
		final IBotIDMap<TrackedTigerBot> bots = new BotIDMap<TrackedTigerBot>();
		
		for (int i = 0; i < 6; i++)
		{
			BotID idF = BotID.createBotId(i, ETeamColor.BLUE);
			bots.put(idF, createBot(idF, ETeamColor.BLUE));
			
			BotID idT = BotID.createBotId(i, ETeamColor.YELLOW);
			bots.put(idT, createBot(idT, ETeamColor.YELLOW));
		}
		
		final TrackedBall ball = new TrackedBall(AVector3.ZERO_VECTOR, AVector3.ZERO_VECTOR, AVector3.ZERO_VECTOR, 0,
				true);
		final double time = (System.nanoTime() - WPConfig.getFilterTimeOffset())
				* WPConfig.FILTER_CONVERT_NS_TO_INTERNAL_TIME;
		
		WorldFramePrediction wfp = new FieldPredictor(bots.values(), ball).create();
		SimpleWorldFrame swf = new SimpleWorldFrame(bots, ball, time, frameNumber, 0, wfp);
		swf.setCamFps(210);
		swf.setWfFps(60);
		return swf;
	}
	
	
	/**
	 * Creates a new WorldFrame with random positioned bots.
	 * 
	 * @param frameNumber the id of the {@link WorldFrame}.
	 * @return
	 */
	public static SimpleWorldFrame createEmptyWorldFrame(long frameNumber)
	{
		final IBotIDMap<TrackedTigerBot> bots = new BotIDMap<TrackedTigerBot>();
		
		final TrackedBall ball = new TrackedBall(AVector3.ZERO_VECTOR, AVector3.ZERO_VECTOR, AVector3.ZERO_VECTOR, 0,
				true);
		final double time = (System.nanoTime() - WPConfig.getFilterTimeOffset())
				* WPConfig.FILTER_CONVERT_NS_TO_INTERNAL_TIME;
		
		WorldFramePrediction wfp = new FieldPredictor(bots.values(), ball).create();
		SimpleWorldFrame swf = new SimpleWorldFrame(bots, ball, time, frameNumber, 0, wfp);
		swf.setCamFps(210);
		swf.setWfFps(60);
		return swf;
	}
	
	
	/**
	 * Creates a bot within a random analysing rectangle
	 * created by the {@link FieldRasterGenerator}.
	 * 
	 * @param id
	 * @param color
	 * @return bot
	 */
	public TrackedTigerBot createBot(BotID id, ETeamColor color)
	{
		if (runningIterator == FieldRasterGenerator.getNumberOfAnalysingFields())
		{
			runningIterator = 0;
		}
		
		final IVector2 pos = raster.getAnalysisFieldRectangle(runningIterator + 1).getRandomPointInShape();
		
		return new TrackedTigerBot(id, pos, AVector2.ZERO_VECTOR, AVector2.ZERO_VECTOR, 0, 0, 0, 0, 0, null, color);
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * @return the wFrames
	 */
	public List<WorldFrame> getwFrames()
	{
		return wFrames;
	}
	
	
}
