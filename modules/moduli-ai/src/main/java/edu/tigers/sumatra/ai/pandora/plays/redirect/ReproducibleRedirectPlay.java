/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.plays.redirect;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.athena.AthenaAiFrame;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.test.RedirectTestRole;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.statemachine.AState;
import edu.tigers.sumatra.wp.data.DynamicPosition;


public class ReproducibleRedirectPlay extends ABallPreparationPlay
{
	private static final int NUM_ROLES = 2;
	
	@Configurable(defValue = "1300.0,700.0")
	private static IVector2 ballPos = Vector2.fromXY(1300, 700);
	@Configurable(defValue = "-800.0;0.0")
	private static IVector2 receiverPos = Vector2.fromXY(-800, 0);
	@Configurable(defValue = "-800.0;0.0")
	private static IVector2 passTarget = Vector2.fromXY(-800, 0);
	@Configurable(defValue = "0;-1000")
	private static DynamicPosition redirectTarget = new DynamicPosition(Vector2.fromXY(0, -1000));
	
	@Configurable(defValue = "false")
	private static boolean receive = false;
	
	@Configurable(defValue = "NORMAL")
	private static RedirectTestRole.EKickMode kickMode = RedirectTestRole.EKickMode.NORMAL;
	
	
	public ReproducibleRedirectPlay()
	{
		super(EPlay.REPRODUCIBLE_REDIRECT, NUM_ROLES);
		setExecutionState(new ExecutionState());
	}
	
	
	@Override
	protected ARole onAddRole()
	{
		return new RedirectTestRole();
	}
	
	
	@Override
	protected void doUpdate(final AthenaAiFrame frame)
	{
		super.doUpdate(frame);
		setBallTargetPos(ballPos);
		
		List<IDrawableShape> shapes = frame.getTacticalField().getDrawableShapes().get(EAiShapesLayer.TEST_REDIRECT);
		shapes.add(new DrawableLine(Line.fromPoints(ballPos, receiverPos), Color.BLUE));
		shapes.add(new DrawableLine(Line.fromPoints(ballPos, passTarget), Color.GREEN));
		shapes.add(new DrawableLine(Line.fromPoints(receiverPos, redirectTarget.getPos()), Color.MAGENTA));
	}
	
	
	@Override
	protected void handleNonPlacingRole(ARole role)
	{
		if (!(role instanceof RedirectTestRole))
		{
			role = switchRoles(role, new RedirectTestRole());
		}
		RedirectTestRole redirectTestRole = (RedirectTestRole) role;
		redirectTestRole.changeToWait();
		redirectTestRole.setDesiredDestination(receiverPos);
	}
	
	
	private class ExecutionState extends AState
	{
		private boolean ballMoved;
		
		
		@Override
		public void doEntryActions()
		{
			for (ARole role : new ArrayList<>(getRoles()))
			{
				if (!(role instanceof RedirectTestRole))
				{
					switchRoles(role, new RedirectTestRole());
				}
			}
			ARole passingRole = getClosestToBall();
			
			for (ARole role : getRoles())
			{
				RedirectTestRole redirectTestRole = (RedirectTestRole) role;
				if (redirectTestRole == passingRole)
				{
					redirectTestRole.setTarget(new DynamicPosition(passTarget));
					redirectTestRole.setKickMode(kickMode);
					redirectTestRole.changeToPass();
				} else
				{
					redirectTestRole.setTarget(redirectTarget);
					if (receive)
					{
						redirectTestRole.changeToReceive();
					} else
					{
						redirectTestRole.changeToRedirect();
					}
				}
			}
			
			ballMoved = false;
		}
		
		
		@Override
		public void doUpdate()
		{
			if (getBall().getPos().distanceTo(ballPos) > 500)
			{
				ballMoved = true;
			}
			if (ballMoved && (getBall().getVel().getLength2() < 1.0
					|| getBall().getTrajectory().getTravelLine().isPointInFront(ballPos)))
			{
				stateMachine.triggerEvent(EEvent.EXECUTED);
			}
		}
	}
}
