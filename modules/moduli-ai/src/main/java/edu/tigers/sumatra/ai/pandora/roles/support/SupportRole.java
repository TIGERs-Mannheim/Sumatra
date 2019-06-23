/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 29.10.2014
 * Author(s): JulianT
 * *********************************************************
 */
package edu.tigers.sumatra.ai.pandora.roles.support;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.data.EGameStateTeam;
import edu.tigers.sumatra.ai.data.EShapesLayer;
import edu.tigers.sumatra.ai.data.math.AiMath;
import edu.tigers.sumatra.ai.data.math.OffensiveMath;
import edu.tigers.sumatra.ai.metis.offense.OffensiveConstants;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.IVector3;
import edu.tigers.sumatra.math.Line;
import edu.tigers.sumatra.math.ValuePoint;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.paramoptimizer.redirect.RedirectParamCalc;
import edu.tigers.sumatra.shapes.circle.Circle;
import edu.tigers.sumatra.skillsystem.skills.AMoveToSkill;
import edu.tigers.sumatra.statemachine.IRoleState;
import edu.tigers.sumatra.wp.data.DynamicPosition;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * Less chaotic and more rational support role.
 * Part of the implementation of intelligent supportive behavior for Small Size League Soccer Robots.
 * 
 * @author JulianT, ChrisC
 */
public class SupportRole extends ARole
{
	
	@Configurable(comment = "Radius around the global position for testing foedefender")
	private static double			localTestRadius				= 1500;
	
	@Configurable(comment = "Radius around the supporter where foes are recognized")
	private static double			recognizedFoeRadius			= 1000;
	
	@Configurable(comment = "Number of iterations searching for a visible spot")
	private static int				numberOfVisibilitySteps		= 8;
	
	@Configurable(comment = "Triggerdistance for ManToManMarkertest")
	private static double			triggerradiusManToManTest	= Geometry.getBotRadius() * 6;
	
	@Configurable(comment = "Waitcycles for ManToManMarkertest")
	private static int				maxWaitcycles					= 75;
	
	private static final Logger	log								= Logger.getLogger(SupportRole.class.getName());
	
	private ITrackedBot				nearestFoe;
	
	protected final AMoveToSkill	moveToSkill;
	
	private IVector2					globalPos						= new Vector2(2000, -100);
	
	private double						drawRadius						= Geometry.getBotRadius();
	private boolean					growDrawRadius					= true;
	
	private enum ESupportEvent
	{
		MOVE,
		I_M_LONELY,
		CATCH_ME_IF_YOU_CAN
	}
	
	private enum ECatchMeEvent
	{
		MAN_TO_GOAL_MARKER_TEST,
		MAN_TO_BALL_MARKER_TEST,
		OPTIMIZE_POSITION
	}
	
	
	/**
	 * Constructor. What else?
	 */
	public SupportRole()
	{
		super(ERole.SUPPORT);
		
		IRoleState movingState = new MovingState();
		IRoleState iAmAloneState = new IAmAloneState();
		IRoleState catchMeIfYouCan = new CatchMeIfYouCan();
		
		IRoleState manToGoalMarkerTest = new ManToGoalMarkerTest();
		IRoleState manToBallMarkerTest = new ManToBallMarkerTest();
		IRoleState optimizePosition = new OptimizePosition();
		
		setInitialState(movingState);
		addTransition(ESupportEvent.MOVE, movingState);
		addTransition(ESupportEvent.I_M_LONELY, iAmAloneState);
		addTransition(ESupportEvent.CATCH_ME_IF_YOU_CAN, catchMeIfYouCan);
		
		// Possible moves, when we recognized a ManToMan marker
		addTransition(ESupportEvent.CATCH_ME_IF_YOU_CAN, ECatchMeEvent.MAN_TO_BALL_MARKER_TEST, manToBallMarkerTest);
		addTransition(ESupportEvent.CATCH_ME_IF_YOU_CAN, ECatchMeEvent.MAN_TO_GOAL_MARKER_TEST, manToGoalMarkerTest);
		addTransition(ESupportEvent.CATCH_ME_IF_YOU_CAN, ECatchMeEvent.OPTIMIZE_POSITION, optimizePosition);
		
		moveToSkill = AMoveToSkill.createMoveToSkill();
	}
	
	
	/**
	 * Calculate the state for this Frame
	 * 
	 * @author ChrisC
	 */
	@Override
	protected void beforeUpdate()
	{
		if (getAiFrame().getWorldFrame().foeBots.isEmpty())
		{
			return;
		}
		IVector2 pos = getBot().getPos();
		getAiFrame().getTacticalField().getDrawableShapes().get(EShapesLayer.SUPPORTER)
				.add(new DrawableCircle(getPos(), Geometry.getBotRadius(), Color.BLACK));
		
		nearestFoe = AiMath.getNearestBot(getAiFrame().getWorldFrame().foeBots, pos);
		
		if (globalPos != null)
		{
			Circle circle = new Circle(globalPos, localTestRadius);
			
			if (circle.isPointInShape(getBot().getPos()))
			{
				
				getAiFrame().getTacticalField().getDrawableShapes().get(EShapesLayer.SUPPORTER)
						.add(new DrawableCircle(circle, Color.ORANGE));
				// is Foe near bot?
				if (GeoMath.distancePP(globalPos, nearestFoe.getPos()) < recognizedFoeRadius)
				{
					// there is a foe near the supporter
					// test if foe follows you
					if ((getCurrentState() != ESupportEvent.CATCH_ME_IF_YOU_CAN)
							&& (getCurrentState().getClass() != ECatchMeEvent.class))
					{
						triggerEvent(ESupportEvent.CATCH_ME_IF_YOU_CAN);
					}
				} else
				{
					// make yourself visible to the ball
					triggerEvent(ESupportEvent.I_M_LONELY);
				}
			} else
			{
				triggerEvent(ESupportEvent.MOVE);
			}
		}
	}
	
	
	@Override
	protected void afterUpdate()
	{
		// Some fancy Drawing for debug purpose
		if (growDrawRadius)
		{
			drawRadius += 10;
			if (drawRadius > (Geometry.getBotRadius() * 2))
			{
				growDrawRadius = false;
			}
		} else
		{
			drawRadius -= 10;
			if (drawRadius <= Geometry.getBotRadius())
			{
				growDrawRadius = true;
			}
		}
		
		DrawableCircle position = new DrawableCircle(getDestination(), drawRadius, new Color(200, 200, 0, 200));
		position.setFill(true);
		getAiFrame().getTacticalField().getDrawableShapes().get(EShapesLayer.SUPPORTER).add(position);
	}
	
	
	/**
	 * @return Destination support position
	 */
	public IVector2 getDestination()
	{
		return moveToSkill.getMoveCon().getDestination();
	}
	
	/**
	 * This State should be active, when a foe is near the supportPosition, but do not a man to man marker
	 * 
	 * @author ChrisC
	 */
	private class OptimizePosition implements IRoleState
	{
		
		IVector2 dest = Geometry.getCenter();
		
		
		@Override
		public void doEntryActions()
		{
			dest = GeoMath.stepAlongLine(nearestFoe.getPos(), globalPos, recognizedFoeRadius);
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public void doUpdate()
		{
			// GoBackToCatchMeifYouCan, if there is a foe close to you
			if (GeoMath.distancePP(getPos(), nearestFoe.getPos()) < triggerradiusManToManTest)
			{
				triggerEvent(ESupportEvent.CATCH_ME_IF_YOU_CAN);
			}
			List<ValuePoint> list = calcFreePointToBall(dest);
			if (!list.isEmpty())
			{
				moveToSkill.getMoveCon().updateDestination(list.get(0));
			} else
			{
				// Try to go away from the foe
				if (LegalPointChecker.checkPoint(dest, getAiFrame(), getAiFrame().getTacticalField()))
				{
					moveToSkill.getMoveCon().updateDestination(dest);
				} else
				{
					// Try to go away in the other direction
					dest = GeoMath.stepAlongLine(nearestFoe.getPos(), globalPos, -recognizedFoeRadius);
					if (LegalPointChecker.checkPoint(dest, getAiFrame(), getAiFrame().getTacticalField()))
					{
						moveToSkill.getMoveCon().updateDestination(dest);
					} else
					{
						// Move to the center to get a new Position
						moveToSkill.getMoveCon().updateDestination(Geometry.getCenter());
					}
				}
			}
			
			getAiFrame().getTacticalField().getDrawableShapes().get(EShapesLayer.SUPPORTER)
					.add(new DrawableCircle(getPos(), triggerradiusManToManTest, Color.YELLOW));
			
		}
		
		
		@Override
		public Enum<?> getIdentifier()
		{
			return ECatchMeEvent.OPTIMIZE_POSITION;
		}
		
	}
	
	/**
	 * Test if the ManToMan marker blocks the goalline
	 * 
	 * @author ChrisC
	 */
	private class ManToGoalMarkerTest implements IRoleState
	{
		private ITrackedBot myDefender;
		
		
		@Override
		public void doEntryActions()
		{
			myDefender = nearestFoe;
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public void doUpdate()
		{
			if (GeoMath.distancePP(myDefender.getPos(), getPos()) > triggerradiusManToManTest)
			{
				triggerEvent(ESupportEvent.CATCH_ME_IF_YOU_CAN);
			} else
			{
				moveToSkill.getMoveCon().updateDestination(getPos());
				// TODO: what to do?
			}
		}
		
		
		@Override
		public Enum<?> getIdentifier()
		{
			return ECatchMeEvent.MAN_TO_GOAL_MARKER_TEST;
		}
		
	}
	
	
	/**
	 * Test if the ManToMan marker blocks the ball visibility
	 * 
	 * @author ChrisC
	 */
	private class ManToBallMarkerTest implements IRoleState
	{
		ITrackedBot myDefender;
		
		
		@Override
		public void doEntryActions()
		{
			myDefender = nearestFoe;
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public void doUpdate()
		{
			if (GeoMath.distancePP(myDefender.getPos(), getPos()) > triggerradiusManToManTest)
			{
				triggerEvent(ESupportEvent.CATCH_ME_IF_YOU_CAN);
			} else
			{
				moveToSkill.getMoveCon().updateDestination(getPos());
				// Possible moves:
				// -go with defender to the border of the field
				// -
			}
		}
		
		
		@Override
		public Enum<?> getIdentifier()
		{
			return ECatchMeEvent.MAN_TO_BALL_MARKER_TEST;
		}
		
	}
	
	
	/**
	 * Test if there is any kind of ManToManMarker
	 * 
	 * @author ChrisC
	 */
	private class CatchMeIfYouCan implements IRoleState
	{
		
		private ITrackedBot	foeUnderTest;
		private IVector2		destination;
		private boolean		startTest	= false;
		private int				counter		= 0;
		
		
		@Override
		public void doEntryActions()
		{
			foeUnderTest = nearestFoe;
			
			destination = globalPos;
			moveToSkill.getMoveCon().updateDestination(getPos());
			startTest = false;
			counter = 0;
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public void doUpdate()
		{
			IVector2 foePos = getWFrame().foeBots.get(foeUnderTest.getBotId()).getPos();
			// Test if Foe defender is man-to-man marker
			if ((GeoMath.distancePP(getPos(), foePos) < triggerradiusManToManTest) && !startTest)
			{
				destination = GeoMath.stepAlongLine(foePos, destination, recognizedFoeRadius);
				if (!LegalPointChecker.checkPoint(destination, getAiFrame(), getAiFrame().getTacticalField()))
				{
					destination = GeoMath.stepAlongLine(destination, foePos, recognizedFoeRadius);
				}
				startTest = true;
				moveToSkill.getMoveCon().updateDestination(destination);
				counter = 0;
			}
			
			if (startTest)
			{
				if (counter < maxWaitcycles)
				{
					counter++;
				} else
				{
					// Foe does not appear to be a ManToManMarker
					if (GeoMath.distancePP(getPos(), foePos) < triggerradiusManToManTest)
					{
						Line line = new Line(getPos(), getPos().subtractNew(foePos));
						
						double distanceBallLine = GeoMath.distancePL(getWFrame().getBall().getPos(), line);
						double distanceGoalLine = GeoMath.distancePL(Geometry.getGoalOur().getGoalCenter(), line);
						
						if (distanceBallLine > distanceGoalLine)
						{
							triggerEvent(ECatchMeEvent.MAN_TO_GOAL_MARKER_TEST);
						} else
						{
							triggerEvent(ECatchMeEvent.MAN_TO_BALL_MARKER_TEST);
						}
					} else
					{
						triggerEvent(ECatchMeEvent.OPTIMIZE_POSITION);
					}
				}
				
			} else
			{
				if (counter < maxWaitcycles)
				{
					counter++;
				} else
				{
					triggerEvent(ECatchMeEvent.OPTIMIZE_POSITION);
				}
			}
		}
		
		
		@Override
		public Enum<?> getIdentifier()
		{
			return ESupportEvent.CATCH_ME_IF_YOU_CAN;
		}
		
	}
	
	/**
	 * If there is no foe near the supporter, he optimize his position for ball visibility
	 * 
	 * @author ChrisC
	 */
	private class IAmAloneState implements IRoleState
	{
		
		@Override
		public void doEntryActions()
		{
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public void doUpdate()
		{
			IVector2 dest = new Vector2(2000, -1000);
			if (globalPos != null)
			{
				dest = globalPos;
				List<ValuePoint> list = calcFreePointToBall(globalPos);
				if (!list.isEmpty())
				{
					for (IVector2 vec : list)
					{
						if (LegalPointChecker.checkPoint(vec, getAiFrame(), getAiFrame().getTacticalField()))
						{
							dest = vec;
							break;
						}
					}
				}
				if (!LegalPointChecker.checkPoint(dest, getAiFrame(), getAiFrame().getTacticalField()))
				{
					dest = GeoMath.stepAlongLine(getWFrame().getBall().getPos(), getPos(),
							Geometry.getBotToBallDistanceStop() + Geometry.getBotRadius());
				}
			}
			moveToSkill.getMoveCon().updateDestination(dest);
		}
		
		
		@Override
		public Enum<?> getIdentifier()
		{
			return ESupportEvent.I_M_LONELY;
		}
		
	}
	
	/**
	 * Move to the given supportPosition
	 * 
	 * @author ChrisC, JulianT
	 */
	private class MovingState implements IRoleState
	{
		
		@Override
		public void doEntryActions()
		{
			if (globalPos != null)
			{
				if (GeoMath.distancePP(globalPos, getWFrame().getBall().getPos()) < 100)
				{
					log.warn("support pos near ball: " + globalPos);
					moveToSkill.getMoveCon().updateDestination(getPos());
				} else if (Geometry.getPenaltyAreaOur().isPointInShape(globalPos))
				{
					log.warn("support pos inside penArea: " + globalPos);
					moveToSkill.getMoveCon().updateDestination(getPos());
				} else
				{
					moveToSkill.getMoveCon()
							.updateDestination(globalPos);
				}
			}
		}
		
		
		@Override
		public void doExitActions()
		{
			
		}
		
		
		@Override
		public void doUpdate()
		{
			// set orientation the same way as offensive will do if it passes to this bot, to minimize needed repositioning
			IVector2 target = getAiFrame().getTacticalField().getBestDirectShootTarget();
			if (globalPos != null)
			{
				moveToSkill.getMoveCon().updateDestination(globalPos);
			} else
			{
				moveToSkill.getMoveCon().updateDestination(getPos()); // Think about a better solution
			}
			if (target == null)
			{
				target = Geometry.getGoalTheir().getGoalCenter();
			}
			
			if (OffensiveMath.isBallRedirectPossible(getWFrame(), getPos(), target))
			{
				DynamicPosition dynTarget = new DynamicPosition(target);
				RedirectParamCalc rpc = RedirectParamCalc.forBot(getBot().getBot());
				double kickSpeed = rpc.getKickSpeed(getWFrame(),
						getBot().getBotId(), dynTarget, OffensiveConstants.getDefaultPassEndVel());
				IVector3 poss = rpc.calcRedirectPose(getBot(), moveToSkill.getMoveCon().getDestination(),
						target.subtractNew(moveToSkill.getMoveCon().getDestination())
								.getAngle(),
						getWFrame()
								.getBall(),
						dynTarget,
						kickSpeed);
				double orientation = poss.z();
				moveToSkill.getMoveCon().updateTargetAngle(orientation);
			} else
			{
				moveToSkill.getMoveCon().updateLookAtTarget(getWFrame().getBall().getPos());
			}
			
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return ESupportEvent.MOVE;
		}
	}
	
	
	/**
	 * Calculates ball visible positions on the orthogonal line.
	 * 
	 * @param center of the free Points
	 * @author ChrisC
	 */
	private List<ValuePoint> calcFreePointToBall(final IVector2 center)
	{
		List<ValuePoint> list = new ArrayList<ValuePoint>();
		IVector2 pos = getBot().getPos();
		IVector2 vectBallPos = center.getXYVector().subtractNew(getWFrame().getBall().getPos());
		IVector2 endPos1 = center.addNew(vectBallPos.getNormalVector().scaleTo(localTestRadius));
		IVector2 endPos2 = center.addNew(vectBallPos.getNormalVector().scaleTo(-localTestRadius));
		
		double stepSize = ((localTestRadius * 2) / numberOfVisibilitySteps);
		IVector2 testPos;
		for (int i = 0; i < numberOfVisibilitySteps; i++)
		{
			testPos = GeoMath.stepAlongLine(endPos1, endPos2, i * stepSize);
			if (AiMath.p2pVisibility(getWFrame(), testPos, getWFrame().getBall().getPos(),
					Geometry.getBotRadius(), getBotID())
					&& LegalPointChecker.checkPoint(testPos, getAiFrame(), getAiFrame().getTacticalField()))
			{
				getAiFrame().getTacticalField().getDrawableShapes().get(EShapesLayer.SUPPORTER)
						.add(new DrawableCircle(testPos, 50, Color.PINK));
				double distance = GeoMath.distancePP(testPos, pos);
				
				list.add(new ValuePoint(testPos, distance));
				
			} else
			{
				getAiFrame().getTacticalField().getDrawableShapes().get(EShapesLayer.SUPPORTER)
						.add(new DrawableCircle(testPos, 50, Color.GRAY));
			}
		}
		
		// Draw Points
		getAiFrame().getTacticalField().getDrawableShapes().get(EShapesLayer.SUPPORTER)
				.add(new DrawableCircle(endPos1, 50, Color.CYAN));
		getAiFrame().getTacticalField().getDrawableShapes().get(EShapesLayer.SUPPORTER)
				.add(new DrawableCircle(endPos2, 50, Color.RED));
		list.sort(ValuePoint.VALUE_LOW_COMPARATOR);
		
		if (getAiFrame().getTacticalField().getGameState().equals(EGameStateTeam.PREPARE_KICKOFF_THEY)
				|| getAiFrame().getTacticalField().getGameState().equals(EGameStateTeam.PREPARE_KICKOFF_WE))
		{
			list.removeIf(p -> p.x() >= -100);
		}
		return list;
	}
	
	
	@Override
	protected void beforeFirstUpdate()
	{
		super.beforeFirstUpdate();
		setNewSkill(moveToSkill);
	}
	
	
	/**
	 * Need to be set from SupportPlay
	 * 
	 * @param globalPos
	 */
	public void setGlobalPos(final IVector2 globalPos)
	{
		this.globalPos = globalPos;
	}
	
}
