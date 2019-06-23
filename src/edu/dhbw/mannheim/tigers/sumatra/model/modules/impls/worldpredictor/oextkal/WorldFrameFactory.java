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

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeam;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.fieldraster.FieldRasterGenerator;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.fieldraster.FieldRasterGenerator.EGeneratorTyp;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.AVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.AVector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBall;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedFoeBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotIDMap;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.FieldRasterConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.TeamProps;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.playpattern.Pattern;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.WPConfig;


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
	private final FieldRasterConfig	fieldRasterConfig;
	
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
		
		fieldRasterConfig = AIConfig.getFieldRaster();
		raster = new FieldRasterGenerator(EGeneratorTyp.PLAYFINDER);
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
		
		while (selectionList.size() != fieldRasterConfig.getNumberOfAnalysingFields())
		{
			final int randomField = randomGenerator.nextInt(fieldRasterConfig.getNumberOfAnalysingFields()) + 1;
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
	 * Creates a new WorldFrame with random positioned bots.
	 * 
	 * @param frameNumber the id of the {@link WorldFrame}.
	 * @return
	 */
	private WorldFrame createWorldFrame(long frameNumber)
	{
		final BotIDMap<TrackedBot> foeBots = new BotIDMap<TrackedBot>();
		final BotIDMap<TrackedTigerBot> tigerBots = new BotIDMap<TrackedTigerBot>();
		
		for (int i = 0; i < 5; i++)
		{
			BotID idF = new BotID(i, ETeam.OPPONENTS);
			foeBots.put(idF, createBot(idF));
			
			BotID idT = new BotID(i);
			tigerBots.put(idT, createTigerBot(idT));
		}
		
		final TrackedBall ball = new TrackedBall(AVector3.ZERO_VECTOR, AVector3.ZERO_VECTOR, AVector3.ZERO_VECTOR, 0,
				true);
		
		return new WorldFrame(foeBots, tigerBots, new BotIDMap<TrackedTigerBot>(tigerBots), ball, 0, frameNumber, null, 0);
	}
	
	
	/**
	 * Creates a new WorldFrame with random positioned bots.
	 * 
	 * @param frameNumber the id of the {@link WorldFrame}.
	 * @param teamProps
	 * @return
	 */
	public static WorldFrame createEmptyWorldFrame(long frameNumber, TeamProps teamProps)
	{
		final BotIDMap<TrackedBot> foeBots = new BotIDMap<TrackedBot>();
		final BotIDMap<TrackedTigerBot> tigerBots = new BotIDMap<TrackedTigerBot>();
		final TrackedBall ball = new TrackedBall(AVector3.ZERO_VECTOR, AVector3.ZERO_VECTOR, AVector3.ZERO_VECTOR, 0,
				false);
		final double time = (System.nanoTime() - WPConfig.getFilterTimeOffset())
				* WPConfig.FILTER_CONVERT_NS_TO_INTERNAL_TIME;
		WorldFrame wf = new WorldFrame(foeBots, tigerBots, new BotIDMap<TrackedTigerBot>(tigerBots), ball, time,
				frameNumber, teamProps, 0);
		wf.setWfFps(0);
		return wf;
	}
	
	
	/**
	 * Creates a foe bot within a random analysing rectangle
	 * created by the {@link FieldRasterGenerator}.
	 * 
	 * @param id
	 * @return foeBot
	 */
	public TrackedBot createBot(BotID id)
	{
		if (runningIterator == fieldRasterConfig.getNumberOfAnalysingFields())
		{
			runningIterator = 0;
		}
		
		final IVector2 pos = raster.getAnalysisFieldRectangle(runningIterator + 1).getRandomPointInShape();
		
		return new TrackedFoeBot(id, pos, AVector2.ZERO_VECTOR, AVector2.ZERO_VECTOR, 0, 0, 0, 0, 0);
	}
	
	
	/**
	 * Creates a tiger bot within a random analysing rectangle
	 * created by the {@link FieldRasterGenerator}.
	 * 
	 * @param id
	 * @return foeBot
	 */
	public TrackedTigerBot createTigerBot(BotID id)
	{
		if (runningIterator == fieldRasterConfig.getNumberOfAnalysingFields())
		{
			runningIterator = 0;
		}
		
		final IVector2 pos = raster.getAnalysisFieldRectangle(runningIterator + 1).getRandomPointInShape();
		
		return new TrackedTigerBot(id, pos, AVector2.ZERO_VECTOR, AVector2.ZERO_VECTOR, 0, 0, 0, 0, 0, null);
	}
	
	
	/**
	 * <b>WARNING:</b> Only for debugging/test purpose!!!
	 * @return A instance of {@link WorldFrame} which is filled with everything to at least initialize fake-plays
	 */
	public AIInfoFrame createFakeAIInfoFrame()
	{
		final WorldFrame wf = createWorldFrame(0);
		final AIInfoFrame frame = new AIInfoFrame(wf, null, null);
		
		final TrackedBot fakeBot = new TrackedTigerBot(new BotID(), new Vector2(0, 0), new Vector2(0, 0), new Vector2(0,
				0), 15, 0.0f, 0.0f, 0.0f, 0.0f, null);
		
		// needed for testing StopMarkerPlay
		frame.tacticalInfo.setOpponentPassReceiver(fakeBot);
		
		// needed for PatternBlockPlay
		final List<Pattern> patternList = new ArrayList<Pattern>();
		patternList.add(new Pattern(new Vector2(0, 0), new Vector2(5, 5), 0, 0));
		frame.tacticalInfo.setPlayPattern(patternList);
		
		return frame;
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
