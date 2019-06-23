/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 08.07.2011
 * Author(s): DanielW
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.offense;

import edu.dhbw.mannheim.tigers.sumatra.model.data.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.EGameSituation;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.Receiver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense.Sender;


/**
 * Play sollte nur gewählt werden wenn man sich in der gegnerischen spielfeldhälfte befindet
 * 
 * @author DanielW
 * 
 */
public class IndirectShotV2 extends APlay
{
	/**  */
	private static final long	serialVersionUID	= 4374322157438267682L;
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private Sender					passer;
	private Receiver				receiver;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public IndirectShotV2(AIInfoFrame curFrame)
	{
		super(EPlay.INDIRECT_SHOTV2, curFrame);
		passer = new Sender(EGameSituation.SET_PIECE);
		receiver = new Receiver();
		addAggressiveRole(passer);
		addAggressiveRole(receiver);
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	protected void beforeFirstUpdate(AIInfoFrame frame)
	{
		receiver.setTarget(calcReceiverPosition(frame));
		passer.setTarget(calcReceiverPosition(frame));
	}
	

	@Override
	protected void beforeUpdate(AIInfoFrame frame)
	{
		
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	private IVector2 calcReceiverPosition(AIInfoFrame frame)
	{
		IVector2 receiverPos = new Vector2f(frame.worldFrame.ball.pos);
		
		if (receiverPos.y() > 0)
		{
			return receiverPos.addNew((new Vector2(-0.5f, -1).scaleTo(2500)));
		} else
		{
			return receiverPos.addNew((new Vector2(-0.5f, 1).scaleTo(2500)));
		}
		

	}
}
