/* 
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 06.01.2011
 * Author(s): Peter
 *
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal;

import java.util.Map;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.ABot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.udp.ITransceiverUDP;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.communication.udp.ITransceiverUDPObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.CommandConstants;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerMotorMoveV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerMulticastUpdateAllV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.WPConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.data.OmnibotControl_V2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.oextkal.filter.IFilter;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ABotManager;

public class BotControlObserver implements ITransceiverUDPObserver
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private int id;
	private int botMngrId;
	private Map<Integer, IFilter> bots;
	private ABotManager botMngr;

	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public BotControlObserver(ABot bot,  Map<Integer, IFilter> tigers, ABotManager botMngr)
	{
		id = WPConfig.TIGER_ID_OFFSET + bot.getBotId();
		botMngrId = bot.getBotId();
		bots = tigers;
		this.botMngr = botMngr;

		ITransceiverUDP txrx = (ITransceiverUDP) this.botMngr.getMulticastTransceiver(); 
		txrx.addObserver(this);
	}

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void onIncommingCommand(ACommand cmd)
	{
		// nothing to do for us here
	}

	@Override
	public void onOutgoingCommand(ACommand cmd)
	{
		switch (cmd.getCommand())
		{
			case CommandConstants.CMD_MULTICAST_UPDATE_ALL_V2:
				// MOTOR_MOVE
				TigerMulticastUpdateAllV2 ua = (TigerMulticastUpdateAllV2) cmd; 
				int i = ua.getSlot(botMngrId); 
				if (i == -1 || ua.getUseMove(i) == false)
				{
					return;
				}
				
				TigerMotorMoveV2 moveCmd = ua.getMove(i);
				
				// first convert command to velocity-controls
				double vt = moveCmd.getY();		// y -> positive is foreward, [m/s]
				double vo = -moveCmd.getX();		// x -> positive is right, [m/s]
				double omega = moveCmd.getW();	// turn speed, [rad/s]
				double eta = moveCmd.getV();		// compensated turn speed, [rad/s]
												
				// then update control-vector u of the bots with the calculated controls			
				if (bots.containsKey(id))
				{
					bots.get(id).setControl(new OmnibotControl_V2(
							vt  * WPConfig.FILTER_CONVERT_MperS_TO_INTERNAL_V,
							vo * WPConfig.FILTER_CONVERT_MperS_TO_INTERNAL_V,
							omega * WPConfig.FILTER_CONVERT_RadPerS_TO_RadPerInternal,
							eta * WPConfig.FILTER_CONVERT_RadPerS_TO_RadPerInternal
							));
				}
				break;
			default:
				// other command
				break;
		}		
	}

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	public int getId()
	{
		return id;
	}
	
	public void setId(int id)
	{
		this.id = id;
	}
	
	public void stopObserving()
	{
		ITransceiverUDP txrx = (ITransceiverUDP) botMngr.getMulticastTransceiver(); 
		txrx.removeObserver(this);
	}
}
