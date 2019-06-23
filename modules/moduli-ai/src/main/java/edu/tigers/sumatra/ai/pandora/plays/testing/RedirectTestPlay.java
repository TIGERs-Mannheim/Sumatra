/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 22.04.2016
 * Author(s): Dominik Engelhardt <Dominik.Engelhardt@dlr.de>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.pandora.plays.testing;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.data.EGameStateTeam;
import edu.tigers.sumatra.ai.data.frames.AthenaAiFrame;
import edu.tigers.sumatra.ai.data.frames.MetisAiFrame;
import edu.tigers.sumatra.ai.pandora.plays.APlay;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.move.MoveRole;
import edu.tigers.sumatra.ai.pandora.roles.move.MoveRole.EMoveBehavior;
import edu.tigers.sumatra.ai.pandora.roles.test.RedirectOffensiveTestRole;
import edu.tigers.sumatra.ai.pandora.roles.test.SimpleShooterRole;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.wp.data.DynamicPosition;


/**
 * @author Dominik Engelhardt <Dominik.Engelhardt@dlr.de>
 */
public class RedirectTestPlay extends APlay
{
	@Configurable(comment = "Starting-position for the redirector")
	private static IVector2	initialRedirectPosition	= new Vector2(0, -1000);
	@Configurable(comment = "Ignore specified position, use desired bot farthest away from ball as redirector instead.")
	private static boolean	manualPlacement			= true;
	
	private ERedirectState	state							= ERedirectState.PREPARE;
	
	private enum ERedirectState
	{
		PREPARE,
		KICK,
		REDIRECT
	}
	
	private DynamicPosition passTarget;
	
	
	/**
	 */
	public RedirectTestPlay()
	{
		super(EPlay.REDIRECT_TEST);
	}
	
	
	@Override
	protected ARole onRemoveRole(final MetisAiFrame frame)
	{
		return getLastRole();
	}
	
	
	@Override
	protected ARole onAddRole(final MetisAiFrame frame)
	{
		state = ERedirectState.PREPARE;
		if (!manualPlacement && (getRoles().isEmpty()))
		{
			return new MoveRole(initialRedirectPosition, 0);
		}
		return new MoveRole(EMoveBehavior.NORMAL);
	}
	
	
	@Override
	protected void onGameStateChanged(final EGameStateTeam gameState)
	{
	}
	
	
	@Override
	protected void doUpdate(final AthenaAiFrame frame)
	{
		if (getRoles().size() != 2)
		{
			return;
		}
		switch (state)
		{
			case PREPARE:
				boolean positionsReached = (getRoles().size() == 2)
						&& (manualPlacement || getRoles().stream().anyMatch((role) -> role.isCompleted()));
				if (positionsReached)
				{
					initiallySwitchToKickState(frame);
				}
				break;
			case KICK:
				ARole redirector = getRedirectingRole();
				if (redirector != null)
				{
					boolean hasKicked = frame.getTacticalField().getBallPossession().getTigersId()
							.equals(redirector.getBotID());
					if (hasKicked)
					{
						switchToRedirectState();
					}
				}
				break;
			case REDIRECT:
				boolean hasRedirected = getRedirectingRole().getBot().getPos()
						.subtractNew(frame.getWorldFrame().getBall().getPos()).getLength() > 200;
				if (hasRedirected)
				{
					switchToKickState();
				}
				
				break;
		}
		
	}
	
	
	private void initiallySwitchToKickState(final AthenaAiFrame frame)
	{
		List<ARole> sortedRoles = new ArrayList<>(getRoles());
		sortedRoles.sort((final ARole r1, final ARole r2) -> (int) (distanceToBall(r1) - distanceToBall(r2)));
		
		passTarget = new DynamicPosition(sortedRoles.get(1).getBot());
		SimpleShooterRole shooter = new SimpleShooterRole(passTarget);
		
		RedirectOffensiveTestRole redirector = new RedirectOffensiveTestRole();
		redirector.setAiEnabled(false);
		
		switchRoles(sortedRoles.get(0), shooter);
		switchRoles(sortedRoles.get(1), redirector);
		
		state = ERedirectState.REDIRECT;
	}
	
	
	private void switchToKickState()
	{
		setRedirectorsAiEnabled(false);
		
		state = ERedirectState.KICK;
	}
	
	
	private void switchToRedirectState()
	{
		setRedirectorsAiEnabled(true);
		
		state = ERedirectState.REDIRECT;
	}
	
	
	private void setRedirectorsAiEnabled(final boolean enabled)
	{
		RedirectOffensiveTestRole redirector = getRedirectingRole();
		if (redirector != null)
		{
			redirector.setAiEnabled(enabled);
		}
	}
	
	
	private RedirectOffensiveTestRole getRedirectingRole()
	{
		Optional<ARole> opt = getRoles().stream().filter(role -> (role instanceof RedirectOffensiveTestRole)).findFirst();
		if (opt.isPresent())
		{
			return (RedirectOffensiveTestRole) opt.get();
		}
		return null;
	}
	
	
	private double distanceToBall(final ARole role)
	{
		return role.getBot().getPos().subtractNew(role.getAiFrame().getWorldFrame().getBall().getPos()).getLength();
	}
}
