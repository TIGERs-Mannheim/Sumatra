/*
 * *********************************************************
 * Copyright (c) 2009 - 2012, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 6, 2012
 * Author(s): dirk
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.playfinder.apollon;

import javax.persistence.Entity;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ballpossession.BallPossession;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.fieldraster.FieldRasterGenerator.EGeneratorTyp;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.rectangle.AIRectangleVector;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBall;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotIDMap;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotIDMapConst;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.fieldanalysis.FieldAnalyser;


/**
 * This KnowledgeField implementation is based on a field raster.
 * 
 * First approach: use the AnalyseRaster
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
@Entity
public class KnowledgeFieldRaster extends AKnowledgeField
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static long						frameNumber	= 0;
	private transient AIRectangleVector	ratedRectangles;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param tigerBots
	 * @param foeBots
	 * @param ball
	 * @param ballPossession
	 */
	public KnowledgeFieldRaster(BotIDMapConst<TrackedTigerBot> tigerBots, BotIDMapConst<TrackedBot> foeBots,
			TrackedBall ball, BallPossession ballPossession)
	{
		super(tigerBots, foeBots, ball, ballPossession);
		initialize();
	}
	
	
	/**
	 * @param aiFrame
	 */
	public KnowledgeFieldRaster(AIInfoFrame aiFrame)
	{
		this(aiFrame.worldFrame.tigerBotsVisible, aiFrame.worldFrame.foeBots, aiFrame.worldFrame.ball,
				aiFrame.tacticalInfo.getBallPossession());
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * Compare the two knowledgeFields
	 * 
	 * Current approach:
	 * median of distances from tigerBots, foeBots and ball
	 * 
	 * @param knowledgeField
	 * @return
	 */
	@Override
	public IComparisonResult compare(AKnowledgeField knowledgeField)
	{
		if (!(knowledgeField instanceof KnowledgeFieldRaster))
		{
			throw new IllegalArgumentException("knowledgeField must be of Type KnowledgeFieldRaster, but is "
					+ (knowledgeField == null ? "null" : knowledgeField.getClass()));
		}
		final KnowledgeFieldRaster kfR = (KnowledgeFieldRaster) knowledgeField;
		
		if (getAnalysisRectangles().size() != kfR.getAnalysisRectangles().size())
		{
			throw new IllegalArgumentException("knowledgeField must have same number of analysisRectangles");
		}
		
		final int max = getAnalysisRectangles().size() * 100;
		int diff = 0;
		int size = getAnalysisRectangles().size();
		for (int i = 0; i < size; i++)
		{
			diff += Math.abs(getAnalysisRectangles().get(i).getValue() - kfR.getAnalysisRectangles().get(i).getValue());
		}
		
		final double result = 1.0 - ((double) diff / (double) max);
		return new ComparisonResult(result);
	}
	
	
	/**
	 * Increases frame number counter
	 */
	private static void incFrameNumber()
	{
		frameNumber++;
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * @return
	 */
	public AIRectangleVector getAnalysisRectangles()
	{
		return ratedRectangles;
	}
	
	
	@Override
	public final void initialize()
	{
		final WorldFrame worldFrame = new WorldFrame(getFoeBots(), getTigerBots(), new BotIDMap<TrackedTigerBot>(
				getTigerBots()), null, 0, frameNumber, null, 0);
		incFrameNumber();
		final FieldAnalyser fieldAnalyser = new FieldAnalyser(EGeneratorTyp.PLAYFINDER);
		
		final AIInfoFrame aiFrame = new AIInfoFrame(worldFrame, null, null);
		
		fieldAnalyser.doCalc(aiFrame, aiFrame);
		// copy
		ratedRectangles = new AIRectangleVector(fieldAnalyser.getAnalysisRectangles());
	}
}
