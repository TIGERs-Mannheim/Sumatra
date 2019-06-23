/*
 * *********************************************************
 * Copyright (c) 2009 - 2012, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 11.04.2012
 * Author(s): osteinbrecher
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.playpattern;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.MetisCalculators;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.ACalculator;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.IMetisControlHandler;


/**
 * calculator for patterns
 * 
 * @author osteinbrecher
 * 
 */
public class PlayPatternDetect extends ACalculator implements IMetisControlHandler
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private final PatternFrameBuffer		frameBuffer;
	private final List<Pattern>			foundPatterns;
	private final List<Pattern>			filePatterns;
	
	private final PatterListSerializer	patternSerializer;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public PlayPatternDetect()
	{
		frameBuffer = new PatternFrameBuffer();
		foundPatterns = new ArrayList<Pattern>();
		patternSerializer = new PatterListSerializer(AIConfig.getMetisCalculators().getPatterLogFile());
		
		filePatterns = patternSerializer.readFile();
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public void doCalc(AIInfoFrame curFrame, AIInfoFrame preFrame)
	{
		if (curFrame.worldFrame.foeBots.size() < 2)
		{
			curFrame.tacticalInfo.setPlayPattern(foundPatterns);
			return;
		}
		
		foundPatterns.clear();
		
		frameBuffer.addFrame(curFrame);
		
		foundPatterns.addAll(frameBuffer.getFoundPatterns());
		foundPatterns.addAll(filePatterns);
		
		for (final Pattern pattern : foundPatterns)
		{
			pattern.compare(curFrame);
		}
		
		Collections.sort(foundPatterns);
		
		curFrame.tacticalInfo.setPlayPattern(foundPatterns);
	}
	
	
	@Override
	public void setConfiguration(MetisCalculators config)
	{
		patternSerializer.setPath(config.getPatterLogFile());
	}
	
	
	@Override
	public void loadAnalyzingResults()
	{
	}
	
	
	@Override
	public void persistAnalyzingResults()
	{
	}
	
	
	@Override
	public void fallbackCalc(AIInfoFrame curFrame, AIInfoFrame preFrame)
	{
		curFrame.tacticalInfo.setPlayPattern(new ArrayList<Pattern>());
	}
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
}