/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.offense.states.kickstate;

import edu.tigers.sumatra.ai.data.EAiShapesLayer;
import edu.tigers.sumatra.ai.math.OffensiveMath;
import edu.tigers.sumatra.ai.math.OffensiveRedirectorMath;
import edu.tigers.sumatra.ai.metis.offense.OffensiveConstants;
import edu.tigers.sumatra.ai.metis.support.IPassTarget;
import edu.tigers.sumatra.ai.pandora.roles.offense.states.OffensiveRoleKickState;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.skillsystem.ESkill;
import edu.tigers.sumatra.skillsystem.skills.AKickSkill;
import edu.tigers.sumatra.skillsystem.skills.AMoveToSkill;
import edu.tigers.sumatra.skillsystem.skills.ReceiverSkill;
import edu.tigers.sumatra.skillsystem.skills.RedirectSkill;
import edu.tigers.sumatra.wp.data.DynamicPosition;
import edu.tigers.sumatra.wp.data.ITrackedBot;

import java.awt.Color;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * @author geforce
 */
public class RedirectKickStateState extends AOffensiveRoleKickStateState
{
	
	private RedirectSkill redirectSkill;
	
	private ReceiverSkill receiverSkill;
	
	private IVector2 oldTarget = null;
	
	private IPassTarget oldDoublePassTarget = null;
	
	private IPassTarget doublePassTarget = null;
	
	private IVector2 specialMovePos = null;
	
	private ERedirectAction currentAction = null;
	
	private boolean interceptActive = false;
	
	private OffensiveRedirectorMath redirectorMath;
	
	private IVector2 oldCatchPos = null;
	
	
	/**
	 * @param role
	 */
	public RedirectKickStateState(final OffensiveRoleKickState role)
	{
		super(role);
	}
	
	
	@Override
	public IVector2 getDestination()
	{
		return getCurrentSkill().getMoveCon().getDestination();
	}
	
	
	@Override
	public String getIdentifier()
	{
		return EKickStateState.REDIRECT.name();
	}
	
	
	@Override
	public void doEntryActions()
	{
		setNewSkill(new ReceiverSkill());
	}
	
	
	@Override
	public void doExitActions()
	{
		getAiFrame().getAICom().setResponded(false);
	}
	
	
	@Override
	public void doUpdate()
	{
		redirectorMath = new OffensiveRedirectorMath();
		
		// determining Target based on oldTarget
		DynamicPosition dTarget = calcGoalShotTarget();
		
		// Determine pass Target for potential redirect Pass
		IPassTarget currentPassTarget = calcDoublePassTarget();
		currentPassTarget = calcPassTarget(currentPassTarget);
		oldDoublePassTarget = currentPassTarget;
		
		checkAndSetOnGoingPassParams();
		
		// handle skill switches
		// depending on wFrame and the 2 possible targets, determine the aciton of the bot:
		// Catch or RedirectPass or RedirectGoalShot
		handleSkillSwitchesBasedOnCurrentState(dTarget, currentPassTarget);
		
		// check if we want to drive towards ball from behind rather than redirecting
		// -> leave this state and switch to kick
		swtichStateIfRedirectNotValidAnymore();
		
		// draw some debugging information
		drawCurrentStateInformation();
		drawTarget();
		drawSpecialMovePos();
	}
	
	
	private void checkAndSetOnGoingPassParams()
	{
		getAiFrame().getTacticalField().getOngoingPassInfo().ifPresent(pt -> {
			if (pt.getPassTarget().getBotId().equals(getBotID()))
			{
				specialMovePos = pt.getPassTarget().getKickerPos();
			}
		});
	}
	
	
	private IPassTarget calcDoublePassTarget()
	{
		// calculate potentialDoublePassTarget
		Optional<IPassTarget> potentialPassTarget = calcPotentialPassTarget();
		// use old target if new target is not valid anymore
		potentialPassTarget.ifPresent(iPassTarget -> doublePassTarget = iPassTarget);
		return doublePassTarget;
	}
	
	
	private void handleSkillSwitchesBasedOnCurrentState(final DynamicPosition dTarget,
			final IPassTarget currentPassTarget)
	{
		IVector2 catchPoint = calcCatchPoint();
		currentAction = redirectorMath.checkStateSwitches(getAiFrame(),
				currentAction,
				getBot(), getDestination(),
				dTarget, currentPassTarget, catchPoint);
		switch (currentAction)
		{
			case CATCH:
				receiveBall();
				interceptActive = false;
				break;
			case REDIRECT_GOAL:
				redirectBall(dTarget, specialMovePos);
				redirectSkill.setKickMode(AKickSkill.EKickMode.MAX);
				interceptActive = false;
				break;
			case REDIRECT_PASS:
				DynamicPosition newTarget = new DynamicPosition(currentPassTarget.getKickerPos());
				
				if (getDestination() != null && getTimeBallNeedsToReachMe() < 1.5
						&& getAiFrame().getTacticalField().getOffensiveTimeEstimations()
								.get(getBotID()).getBallContactTime() < 1.5)
					getAiFrame().getAICom().setPassTarget(currentPassTarget);
				
				redirectBall(newTarget, specialMovePos);
				redirectSkill.setKickMode(AKickSkill.EKickMode.PASS);
				interceptActive = false;
				break;
			case INTERCEPT:
				interceptBall();
				break;
		}
	}
	
	
	private IVector2 calcCatchPoint()
	{
		if (currentAction != null)
		{
			switch (currentAction)
			{
				case CATCH:
				case REDIRECT_GOAL:
				case REDIRECT_PASS:
					oldCatchPos = getCurrentSkill().getMoveCon().getDestination();
					return oldCatchPos;
				case INTERCEPT:
					if (oldCatchPos == null)
					{
						log.warn("Invalid catch position");
					}
					return oldCatchPos;
			}
		}
		return null;
	}
	
	
	private void interceptBall()
	{
		IVector2 catchPos = redirectorMath.getEnemyCatchPos();
		IVector2 catchPosToBall = getWFrame().getBall().getPos().subtractNew(catchPos)
				.scaleTo(Geometry.getBotRadius() * 2 + 30);
		IVector2 newCatchPos = catchPos.addNew(catchPosToBall);
		if (!Geometry.getField().isPointInShape(newCatchPos))
		{
			newCatchPos = Geometry.getField().nearestPointInside(newCatchPos, 300);
		}
		if (!interceptActive)
		{
			interceptActive = true;
			setNewSkill(AMoveToSkill.createMoveToSkill());
			getCurrentSkill().getMoveCon().updateDestination(newCatchPos);
		} else
		{
			getCurrentSkill().getMoveCon().updateDestination(newCatchPos);
		}
	}
	
	
	private void receiveBall()
	{
		if (getCurrentSkill().getType() != ESkill.RECEIVER)
		{
			receiverSkill = new ReceiverSkill();
			receiverSkill.setDesiredDestination(specialMovePos);
			setNewSkill(receiverSkill);
		}
	}
	
	
	private void redirectBall(DynamicPosition target, IVector2 desiredPos)
	{
		if (getCurrentSkill().getType() != ESkill.REDIRECT)
		{
			redirectSkill = new RedirectSkill(target, OffensiveConstants.getDefaultPassEndVel());
			setNewSkill(redirectSkill);
		} else
		{
			redirectSkill.setTarget(target);
			if (desiredPos != null)
			{
				redirectSkill.setDesiredDestination(desiredPos);
			}
		}
	}
	
	
	private void swtichStateIfRedirectNotValidAnymore()
	{
		if (!getAiFrame().getTacticalField().getOffensiveActions().get(getBotID()).getMoveAndTargetInformation()
				.isReceiveActive())
		{
			// ball is not moving towards us
			triggerInnerEvent(EKickStateEvent.CATCH_NOT_POSSIBLE);
		}
	}
	
	
	private void drawCurrentStateInformation()
	{
		getAiFrame().getTacticalField().getDrawableShapes().get(EAiShapesLayer.OFFENSIVE_ROLE_STATUS)
				.add(new DrawableAnnotation(getPos(), "\n\n\n\n" + getCurrentSkill().getType(),
						getBotID().getTeamColor().getColor())
								.setOffset(Vector2.fromX(130))
								.setFontHeight(50));
		
		if (doublePassTarget != null)
		{
			visualizeTarget(doublePassTarget.getKickerPos());
		}
	}
	
	
	private DynamicPosition calcGoalShotTarget()
	{
		IVector2 target = calcRedirectTarget(oldTarget);
		oldTarget = target;
		return new DynamicPosition(target);
	}
	
	
	private Optional<IPassTarget> calcPotentialPassTarget()
	{
		Map<BotID, ITrackedBot> botMap = OffensiveMath
				.getPotentialOffensiveBotMap(getAiFrame().getTacticalField(), getAiFrame())
				.entrySet().stream()
				.filter(entry -> entry.getKey() != getBotID())
				.filter(entry -> entry.getKey().isBot())
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
		
		return redirectorMath.calcBestRedirectPassTarget(getWFrame(), botMap, getBot(), getAiFrame().getTacticalField(),
				oldDoublePassTarget);
	}
	
	
	private void drawTarget()
	{
		if (getCurrentSkill().getType() == ESkill.REDIRECT)
		{
			visualizeTarget(redirectSkill.getTarget());
		}
	}
	
	
	private void drawSpecialMovePos()
	{
		if (specialMovePos != null)
		{
			DrawableCircle dc = new DrawableCircle(Circle.createCircle(specialMovePos, 200), new Color(200, 0, 0, 80));
			dc.setFill(true);
			getAiFrame().getTacticalField().getDrawableShapes().get(EAiShapesLayer.OFFENSIVE_PASSING).add(dc);
		}
	}
	
	
	private IPassTarget calcPassTarget(IPassTarget newPassTarget)
	{
		if (getDestination() == null || isEnoughTimeToChangeTarget() || oldDoublePassTarget == null)
		{
			return newPassTarget;
		}
		return oldDoublePassTarget;
	}
	
	
	private IVector2 calcRedirectTarget(IVector2 oldTarget)
	{
		IVector2 target = getAiFrame().getTacticalField().getBestDirectShotTarget();
		if (getAiFrame().getTacticalField().getBestDirectShotTargetsForTigerBots().containsKey(getBotID()))
		{
			target = getAiFrame().getTacticalField().getBestDirectShotTargetsForTigerBots().get(getBotID());
		}
		if (target == null)
		{
			target = Geometry.getGoalTheir().getCenter();
		}
		
		if (oldTarget != null && getDestination() != null && !isEnoughTimeToChangeTarget())
		{
			return oldTarget;
		}
		return target;
	}
	
	
	private boolean isEnoughTimeToChangeTarget()
	{
		return getTimeBallNeedsToReachMe() > 0.38;
	}
	
	
	private double getTimeBallNeedsToReachMe()
	{
		double ballDistToMe = getDestination()
				.distanceTo(getWFrame().getBall().getPos());
		double time = getWFrame().getBall().getTrajectory().getTimeByDist(ballDistToMe);
		DrawableAnnotation da = new DrawableAnnotation(getDestination(), "redirect impact time: " + time, Color.black);
		getAiFrame().getTacticalField().getDrawableShapes().get(EAiShapesLayer.REDIRECT_ROLE).add(da);
		return time;
	}
}
