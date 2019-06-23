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

import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
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


/**
 */
public class BotControlObserver implements ITransceiverUDPObserver
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private final Map<Integer, IFilter>	bots;
	private final ABotManager				botMngr;
	
	private Integer							shiftedId;
	private BotID								originalId;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param bot
	 * @param tigers
	 * @param botMngr
	 */
	public BotControlObserver(ABot bot, Map<Integer, IFilter> tigers, ABotManager botMngr)
	{
		setID(bot.getBotID());
		
		bots = tigers;
		this.botMngr = botMngr;
		
		final ITransceiverUDP txrx = this.botMngr.getMulticastTransceiver();
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
				final TigerMulticastUpdateAllV2 ua = (TigerMulticastUpdateAllV2) cmd;
				final int i = ua.getSlot(originalId.getNumber());
				if ((i == -1) || (!ua.getUseMove(i)))
				{
					return;
				}
				
				final TigerMotorMoveV2 moveCmd = ua.getMove(i);
				
				// first convert command to velocity-controls
				/** y -> positive is foreward, [m/s] */
				final double vt = moveCmd.getY();
				/** x -> positive is right, [m/s] */
				final double vo = -moveCmd.getX();
				/** turn speed, [rad/s] */
				final double omega = moveCmd.getW();
				/** compensated turn speed, [rad/s] */
				final double eta = moveCmd.getV();
				
				// then update control-vector u of the bots with the calculated controls
				final IFilter filter = bots.get(shiftedId);
				if (filter != null)
				{
					filter.setControl(new OmnibotControl_V2(vt * WPConfig.FILTER_CONVERT_MperS_TO_INTERNAL_V, vo
							* WPConfig.FILTER_CONVERT_MperS_TO_INTERNAL_V, omega
							* WPConfig.FILTER_CONVERT_RadPerS_TO_RadPerInternal, eta
							* WPConfig.FILTER_CONVERT_RadPerS_TO_RadPerInternal));
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
	/**
	 * @return
	 */
	public BotID getId()
	{
		return originalId;
	}
	
	
	/**
	 * @param botId
	 */
	public final void setID(BotID botId)
	{
		shiftedId = botId.getNumber() + WPConfig.TIGER_ID_OFFSET;
		originalId = botId;
	}
	
	
	/**
	 */
	public void stopObserving()
	{
		final ITransceiverUDP txrx = botMngr.getMulticastTransceiver();
		txrx.removeObserver(this);
	}
}
