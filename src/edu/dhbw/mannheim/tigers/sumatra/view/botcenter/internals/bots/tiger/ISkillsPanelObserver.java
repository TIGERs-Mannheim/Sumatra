/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 02.09.2010
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.tiger.KickBallV1.EKickDevice;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.tiger.KickBallV1.EKickMode;


/**
 * TigerBotSkills observer.
 * 
 * @author AndreR
 * 
 */
public interface ISkillsPanelObserver
{
	public void onMoveToXY(float x, float y);
	
	
	public void onRotateAndMoveToXY(float x, float y, float angle);
	
	
	public void onStraightMove(int time);
	

	public void onRotate(float targetAngle);
	

	public void onKick(float kicklength, EKickMode mode, EKickDevice device);
	

	public void onLookAt(Vector2 lookAtTarget);
	

	public void onDribble(int rpm);
}
