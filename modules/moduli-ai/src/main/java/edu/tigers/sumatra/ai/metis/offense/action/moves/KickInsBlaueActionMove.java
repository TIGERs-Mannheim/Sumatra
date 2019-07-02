/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.action.moves;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.ITacticalField;
import edu.tigers.sumatra.ai.metis.TacticalField;
import edu.tigers.sumatra.ai.metis.offense.action.EActionViability;
import edu.tigers.sumatra.ai.metis.offense.action.EOffensiveAction;
import edu.tigers.sumatra.ai.metis.offense.action.KickTarget;
import edu.tigers.sumatra.ai.metis.support.passtarget.EScoreMode;
import edu.tigers.sumatra.ai.metis.support.passtarget.IPassTarget;
import edu.tigers.sumatra.ai.metis.support.passtarget.IPassTargetRating;
import edu.tigers.sumatra.ai.metis.support.passtarget.IRatedPassTarget;
import edu.tigers.sumatra.ai.metis.support.passtarget.PassTarget;
import edu.tigers.sumatra.ai.metis.support.passtarget.PassTargetRatingFactory;
import edu.tigers.sumatra.ai.metis.support.passtarget.PassTargetRatingFactoryInput;
import edu.tigers.sumatra.ai.metis.support.passtarget.RatedPassTarget;
import edu.tigers.sumatra.ai.metis.targetrater.EPassInterceptionRaterMode;
import edu.tigers.sumatra.ai.metis.targetrater.PassInterceptionRater;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;

import java.util.List;


/**
 * Pass to some free spot on the field, no robot as pass Target
 */
public class KickInsBlaueActionMove extends AOffensiveActionMove
{
	@Configurable(comment = "Number of points of the grid in x-direction", defValue = "12")
	protected static int pointNumberLength = 12;
	@Configurable(comment = "Number of points of the grid in y-direction", defValue = "9")
	protected static int pointNumberWidth = 9;
	@Configurable(comment = "Minimum distance from grid points to penalty area for FAR_TO_GOAL modes [mm]", defValue = "1000.0")
	protected static double marginAroundPenArea = 1000.0;
	@Configurable(comment = "Maximum distance from gird points to enemy goal center for NEAR_TO_GOAL mode [mm]", defValue = "4000.0")
	protected static double maxDistanceToGoalCenter = 4000.0;
	@Configurable(comment = "Minimum kick distance [mm]", defValue = "1000.0")
	protected static double minKickDistance = 1000.0;
	@Configurable(comment = "Maximum kick distance [mm]", defValue = "4000.0")
	protected static double maxKickDistance = 4000.0;
	@Configurable(comment = "Minimum target PassScore [0;1]", defValue = "0.25")
	protected static double minTgtPassScore = 0.25;
	@Configurable(comment = "Time difference between Tiger and Foe at target grid point [s]", defValue = "0.5")
	protected static double minTimeDifferenceAtTarget = 0.5;
	@Configurable(comment = "Minimum time needed for backspin pass [s]", defValue = "9000.0")
	// This high number is not a bug, it's a feature to deactivate the BackspinMode while it's developed
	private static double minTimeForBackspin = 9000.0;
	
	private double score = 0.0;
	private KickTarget kickTarget;
	private AKickInsBlaueMode mode;
	private final KickInsBlaueFilterParameters filterParameters = new KickInsBlaueFilterParameters();
	private IRatedPassTarget ratedPassTarget;
	private EScoreMode scoreMode;
	
	static
	{
		ConfigRegistration.registerClass("metis", KickInsBlaueActionMove.class);
	}
	
	
	public KickInsBlaueActionMove()
	{
		super(EOffensiveActionMove.KICK_INS_BLAUE);
	}
	
	
	@Override
	public EActionViability isActionViable(final BotID id, final TacticalField newTacticalField,
			final BaseAiFrame baseAiFrame)
	{
		if (baseAiFrame.getGamestate().isStandardSituationForUs())
		{
			return EActionViability.FALSE;
			
		}
		filterParameters.defaultCalc(id, baseAiFrame);
		
		mode = selectKickInsBlaueMode(baseAiFrame);
		kickTarget = mode.create(id, newTacticalField, baseAiFrame, filterParameters).orElse(null);
		drawShapes(id, newTacticalField, baseAiFrame, mode.getShapes());
		
		if (kickTarget != null)
		{
			
			final IPassTarget passTarget = new PassTarget(kickTarget.getTarget(), id);
			
			final PassTargetRatingFactoryInput ratingFactoryInput = PassTargetRatingFactoryInput.fromAiFrame(baseAiFrame);
			final PassTargetRatingFactory ratingFactory = new PassTargetRatingFactory();
			final PassInterceptionRater rater = new PassInterceptionRater(
					baseAiFrame.getWorldFrame().getFoeBots().values(), EPassInterceptionRaterMode.KICK_INS_BLAUE);
			final IPassTargetRating rating = ratingFactory.ratingFromPassTargetAndInput(passTarget, rater,
					ratingFactoryInput);
			
			ratedPassTarget = new RatedPassTarget(passTarget, rating, scoreMode);
			score = ratedPassTarget.getScore();
			return EActionViability.PARTIALLY;
		}
		score = 0.;
		return EActionViability.FALSE;
		
	}
	
	
	@Override
	public OffensiveAction activateAction(final BotID id, final TacticalField newTacticalField,
			final BaseAiFrame baseAiFrame)
	{
		assert kickTarget != null;
		

		return (id == mode.getClosestTigerBot())
				? createOffensiveAction(EOffensiveAction.KICK_INS_BLAUE, kickTarget)
				: createOffensiveAction(EOffensiveAction.KICK_INS_BLAUE, kickTarget).withPassTarget(ratedPassTarget);
	}
	
	
	@Override
	public double calcViabilityScore(final BotID id, final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		// Score calculation mainly in isActionViable method
		return score * ActionMoveConstants.getViabilityMultiplierKickInsBlaue();
	}
	
	
	private void drawShapes(final BotID id, final ITacticalField newTacticalField, final BaseAiFrame baseAiFrame,
			List<IDrawableShape> shapes)
	{
		// Draw Shapes only for current attacker bot
		if (baseAiFrame.getPrevFrame().getTacticalField().getOffensiveStrategy().getAttackerBot().orElse(BotID.noBot())
				.equals(id))
		{
			newTacticalField.getDrawableShapes().get(EAiShapesLayer.OFFENSIVE_KICK_INS_BLAUE).addAll(shapes);
		}
	}
	
	
	private AKickInsBlaueMode selectKickInsBlaueMode(final BaseAiFrame baseAiFrame)
	{
		if (baseAiFrame.getWorldFrame().getBall().getPos()
				.distanceTo(Geometry.getGoalTheir().getCenter()) < maxDistanceToGoalCenter)
		{
			scoreMode = EScoreMode.SCORE_BY_GOAL_KICK;
			return new KickInsBlaueModeNear();
		}
		scoreMode = EScoreMode.SCORE_BY_PASS;
		return (filterParameters.getMaxAllowedTime() > minTimeForBackspin)
				? new KickInsBlaueModeFarBackspin()
				: new KickInsBlaueModeFar();
	}
}

