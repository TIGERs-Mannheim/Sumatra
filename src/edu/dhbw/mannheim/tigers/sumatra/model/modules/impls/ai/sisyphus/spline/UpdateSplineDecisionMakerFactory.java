/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 14, 2013
 * Author(s): Dirk Klostermann <klostermannn@googlemail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.spline;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.IDrawableShape;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.spline.ISpline;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.PathFinderInput;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.spline.decisionmaker.CollisionDetectionDecisionMaker;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.spline.decisionmaker.DestinationChangedDecisionMaker;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.spline.decisionmaker.IUpdateSplineDecisionMaker;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.spline.decisionmaker.NewPathShorterDecisionMaker;


/**
 * creates all decision makers
 * 
 * @author Dirk Klostermann <klostermannn@googlemail.com>
 */
public class UpdateSplineDecisionMakerFactory
{
	private List<IUpdateSplineDecisionMaker>	decisionMakers	= new ArrayList<IUpdateSplineDecisionMaker>();
	
	private List<IDrawableShape>					shapes			= new LinkedList<IDrawableShape>();
	
	
	/**
	  * 
	  */
	public UpdateSplineDecisionMakerFactory()
	{
		// decisionMakers.add(new BotNotOnSplineDecisionMaker());
		decisionMakers.add(new CollisionDetectionDecisionMaker());
		decisionMakers.add(new NewPathShorterDecisionMaker());
		// decisionMakers.add(new SplineEndGoalNotReachedDecisionMaker());
		decisionMakers.add(new DestinationChangedDecisionMaker());
	}
	
	
	/**
	 * check if a new spline should be used
	 * 
	 * @param localPathFinderInput
	 * @param oldSpline
	 * @param newSpline
	 * @return
	 */
	public EDecision check(final PathFinderInput localPathFinderInput, final ISpline oldSpline, final ISpline newSpline)
	{
		List<IDrawableShape> newShapes = new LinkedList<IDrawableShape>();
		EDecision hardestDecision = EDecision.NO_VIOLATION;
		for (IUpdateSplineDecisionMaker decisionMaker : decisionMakers)
		{
			EDecision decision = decisionMaker.check(localPathFinderInput, oldSpline, newSpline, newShapes);
			hardestDecision = decision.max(hardestDecision);
		}
		shapes = newShapes;
		return hardestDecision;
	}
	
	
	/**
	 * @return the shapes
	 */
	public List<IDrawableShape> getShapes()
	{
		return shapes;
	}
}
