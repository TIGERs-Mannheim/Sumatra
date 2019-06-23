/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 12, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills;

import java.util.ArrayList;
import java.util.List;

import com.sleepycat.persist.model.Persistent;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeamColor;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.spline.BSpline;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.Geometry;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.defense.DefenseCalc;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.defense.data.DefensePoint;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.driver.PositionDriver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillName;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * @author MarkG
 */
@Persistent
public class DefenderSkill extends AMoveSkill
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private BSpline					path										= null;
	private DefensePoint				defPoint									= new DefensePoint(new Vector2((AIConfig
																								.getGeometry().getFieldLength() / 2)
																								+ AIConfig.getGeometry().getPenaltyAreaOur()
																										.getRadiusOfPenaltyArea()
																								+ DefenseCalc.getPenaltyAreaMargin(), 0));
	
	@Configurable(comment = "Which type of command should be send to the bots? POS or VEL", spezis = {
			"", "GRSIM" })
	private static ECommandType	cmdMode									= ECommandType.POS;
	
	@Configurable(comment = "distance to do fine tuning")
	private static float				fineTuneDistance						= 100f;
	
	@Configurable(comment = "critical for the robots driving speed, be careful when adjusting this value")
	private static float				overDriveDistance						= 150f;
	
	@Configurable(comment = "max Distance -> max Speed")
	private static float				maxDistanceForPositionController	= 550f;
	
	@Configurable(comment = "speedFactor, bigger Offset -> faster")
	private static float				speedOffset								= 210f;
	
	@Configurable(comment = "rotation speed of the robot")
	private static float				turnSpeed								= 0.4f;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	  * 
	  */
	public DefenderSkill()
	{
		this(ESkillName.DEFENDER);
	}
	
	
	/**
	 * @param bspline
	 */
	public DefenderSkill(final BSpline bspline)
	{
		this(ESkillName.DEFENDER);
	}
	
	
	/**
	 * @param skillname
	 */
	public DefenderSkill(final ESkillName skillname)
	{
		super(skillname);
		setPathDriver(new DefensePathDriver());
		setCommandType(cmdMode);
		getMoveCon().setDriveFast(true);
	}
	
	
	@Override
	protected void update(final List<ACommand> cmds)
	{
		generateBSpline();
		calcDestAndOrient();
	}
	
	
	private void generateBSpline()
	{
		float distanceToPenArea = Geometry.getPenaltyAreaMargin();
		List<IVector2> controlPoints = new ArrayList<IVector2>();
		controlPoints
				.add(AIConfig
						.getGeometry()
						.getPenaltyAreaOur()
						.getPenaltyCirclePosCentre()
						.addNew(
								new Vector2(0,
										AIConfig.getGeometry().getPenaltyAreaOur().getPenaltyCirclePos().radius()
												+ distanceToPenArea)));
		
		float offset = (AIConfig.getGeometry().getPenaltyAreaOur().getPenaltyCirclePos().radius() * 1.5f)
				+ distanceToPenArea;
		controlPoints.add(AIConfig.getGeometry().getGoalOur().getGoalCenter().addNew(new Vector2(offset, offset)));
		controlPoints.add(AIConfig.getGeometry().getGoalOur().getGoalCenter().addNew(new Vector2(offset, -offset)));
		
		controlPoints
				.add(AIConfig
						.getGeometry()
						.getPenaltyAreaOur()
						.getPenaltyCircleNegCentre()
						.addNew(
								new Vector2(0,
										-AIConfig.getGeometry().getPenaltyAreaOur().getPenaltyCirclePos().radius()
												- distanceToPenArea)));
		
		path = BSpline.newBSpline(controlPoints);
		float isParam = path.getLeadParamOnSpline(getPos());
		float neededParam = path.getLeadParamOnSpline(defPoint);
		if (neededParam < isParam)
		{
			path.invertSpline();
		}
	}
	
	
	private IVector3 calcDestAndOrient()
	{
		if (getBot().getBotID().equals(BotID.createBotId(0, ETeamColor.YELLOW)))
		{
			// System.out.println(defPoint);
		}
		float neededParam = path.getLeadParamOnSpline(defPoint);
		IVector2 destination = null;
		float orientation = 0f;
		
		float disantecToDestination = GeoMath.distancePP(defPoint, getPos());
		if (disantecToDestination < fineTuneDistance)
		{
			// set fine Tuning destination here
			destination = defPoint;
		}
		else
		{
			if (GeoMath.distancePP(defPoint, path.getPointOnSpline(path.getLeadParamOnSpline(defPoint))) > fineTuneDistance)
			{
				
				destination = defPoint;
				// if ((GeoMath.distancePP(path.getPointOnSpline(path.getLeadParamOnSpline(getPos())), getPos()) > 80)
				// && !isPathToDestinationIntersectingOurPenArea())
				// {
				// // USE MOVETO, it is done in the role now
				// // if you are far away from the bspline then drive back to the original bspline
				// destination = defPoint;
			} else
			{
				// use Bsplines to calculate stepwise movement
				float bsplineParameter = path.getMostDistantPointOnStraightLine(
						path.getLeadParamOnSpline(getPos()), 0.001f);
				
				IVector2 LeadPosition = path.getPointOnSpline(path.getLeadParamOnSpline(getPos()));
				
				// Speed Factor configurable.
				bsplineParameter = path.getParamForDistantPoint(bsplineParameter,
						speedOffset + (speedOffset * path.getPointDifficulty(bsplineParameter)));
				
				destination = path.getPointOnSpline(bsplineParameter);
				
				// max speed
				if (GeoMath.distancePP(LeadPosition, destination) > maxDistanceForPositionController)
				{
					destination = path.getPointOnSpline(path.getParamForDistantPoint(
							path.getLeadParamOnSpline(getPos()), maxDistanceForPositionController));
				}
				
				// overdrive
				destination = destination.addNew(destination.subtractNew(getPos()).normalizeNew()
						.multiplyNew(overDriveDistance));
				
				if (bsplineParameter > neededParam)
				{
					destination = path.getPointOnSpline(neededParam);
				}
			}
		}
		
		
		if (defPoint.getProtectAgainst() != null)
		{
			orientation = defPoint.getProtectAgainst().getPos().subtractNew(getPos()).getAngle();
		} else
		{
			// if (!(getWorldFrame().ball.getPos().x() < (getPos().x() + AIConfig.getGeometry().getBotRadius())))
			// {
			orientation = getWorldFrame().getBall().getPos().subtractNew(getPos()).getAngle();
			// } else
			// {
			// orientation = (new Vector2(0, 0)).subtractNew(getPos()).getAngle();
			// }
		}
		
		// IVector2 isDir = new Vector2(getAngle());
		// IVector2 neededDir = new Vector2(orientation);
		
		// if (Math.abs(getAngle() - orientation) > turnSpeed)
		// {
		// if (AngleMath.getShortestRotation(isDir.getAngle(), neededDir.getAngle()) > 0)
		// {
		// orientation = getAngle() + turnSpeed;
		// } else
		// {
		// orientation = getAngle() - turnSpeed;
		// }
		// }
		
		return new Vector3(destination, orientation);
	}
	
	
	// private boolean isPathToDestinationIntersectingOurPenArea()
	// {
	// PenaltyArea ourPenArea = AIConfig.getGeometry().getPenaltyAreaOur();
	// float distance = GeoMath.distancePP(getPos(), defPoint);
	// final float stepSize = 20f;
	// if (distance < (stepSize * 2))
	// {
	// return false;
	// }
	//
	// float iterationDistance = 0;
	// IVector2 posToDest = defPoint.subtractNew(getPos()).normalizeNew();
	// for (int i = 0; iterationDistance < distance; i++)
	// {
	// IVector2 iterationPoint = getPos().addNew(posToDest.multiplyNew(stepSize * i));
	// if (ourPenArea.isPointInShape(iterationPoint, 0f))
	// {
	// return true;
	// }
	// iterationDistance = GeoMath.distancePP(getPos(), iterationPoint);
	// }
	//
	// return false;
	// }
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * @param point
	 */
	public void setDefensePoint(final DefensePoint point)
	{
		defPoint = point;
	}
	
	
	// --------------------------------------------------------------------------
	// --- DefensePathDriver ----------------------------------------------------
	// --------------------------------------------------------------------------
	
	private class DefensePathDriver extends PositionDriver
	{
		@Override
		public IVector3 getNextDestination(final TrackedTigerBot bot, final WorldFrame wFrame)
		{
			setDestination(calcDestAndOrient());
			return super.getNextDestination(bot, wFrame);
		}
		
		
		@Override
		public IVector3 getNextVelocity(final TrackedTigerBot bot, final WorldFrame wFrame)
		{
			setDestination(calcDestAndOrient());
			return super.getNextVelocity(bot, wFrame);
		}
	}
}
