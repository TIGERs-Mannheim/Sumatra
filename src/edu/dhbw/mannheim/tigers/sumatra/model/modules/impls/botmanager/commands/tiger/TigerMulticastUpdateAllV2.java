/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 06.04.2011
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger;

import java.util.Arrays;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.CommandConstants;


/**
 * Common multicast packet to update MAX_BOTS bots.
 * 
 * @author AndreR
 * 
 */
public class TigerMulticastUpdateAllV2 extends ACommand
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private int								useMove;
	private final int						botId[];
	private final TigerMotorMoveV2	move[];
	private final TigerKickerKickV2	kick[];
	private final TigerDribble			dribble[];
	
	private final int						moveLength;
	private final int						kickLength;
	private final int						dribbleLength;
	
	private final int						MAX_BOTS	= 6;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 */
	public TigerMulticastUpdateAllV2()
	{
		botId = new int[MAX_BOTS];
		move = new TigerMotorMoveV2[MAX_BOTS];
		kick = new TigerKickerKickV2[MAX_BOTS];
		dribble = new TigerDribble[MAX_BOTS];
		
		moveLength = new TigerMotorMoveV2().getDataLength();
		kickLength = new TigerKickerKickV2().getDataLength();
		dribbleLength = new TigerDribble().getDataLength();
		
		for (int i = 0; i < MAX_BOTS; i++)
		{
			botId[i] = 255;
			move[i] = new TigerMotorMoveV2();
			kick[i] = new TigerKickerKickV2();
			dribble[i] = new TigerDribble();
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void setData(byte[] data)
	{
		int pos = 0;
		
		for (int i = 0; i < MAX_BOTS; i++)
		{
			botId[i] = byteArray2UByte(data, i);
		}
		
		pos += MAX_BOTS;
		
		for (int i = 0; i < MAX_BOTS; i++)
		{
			move[i].setData(Arrays.copyOfRange(data, pos + (i * moveLength), pos + (i * moveLength) + moveLength));
		}
		
		pos += MAX_BOTS * moveLength;
		
		for (int i = 0; i < MAX_BOTS; i++)
		{
			kick[i].setData(Arrays.copyOfRange(data, pos + (i * kickLength), pos + (i * kickLength) + kickLength));
		}
		
		pos += MAX_BOTS * kickLength;
		
		for (int i = 0; i < MAX_BOTS; i++)
		{
			dribble[i].setData(Arrays.copyOfRange(data, pos + (i * dribbleLength), pos + (i * dribbleLength)
					+ dribbleLength));
		}
		
		pos += MAX_BOTS * dribbleLength;
		
		useMove = byteArray2UByte(data, pos);
	}
	
	
	@Override
	public byte[] getData()
	{
		final byte data[] = new byte[getDataLength()];
		
		int pos = 0;
		
		for (int i = 0; i < MAX_BOTS; i++)
		{
			byte2ByteArray(data, i, botId[i]);
		}
		
		pos += MAX_BOTS;
		
		for (int i = 0; i < MAX_BOTS; i++)
		{
			System.arraycopy(move[i].getData(), 0, data, pos + (i * moveLength), moveLength);
		}
		
		pos += MAX_BOTS * moveLength;
		
		for (int i = 0; i < MAX_BOTS; i++)
		{
			System.arraycopy(kick[i].getData(), 0, data, pos + (i * kickLength), kickLength);
		}
		
		pos += MAX_BOTS * kickLength;
		
		for (int i = 0; i < MAX_BOTS; i++)
		{
			System.arraycopy(dribble[i].getData(), 0, data, pos + (i * dribbleLength), dribbleLength);
		}
		
		pos += MAX_BOTS * dribbleLength;
		
		byte2ByteArray(data, pos, useMove);
		
		return data;
	}
	
	
	@Override
	public int getCommand()
	{
		return CommandConstants.CMD_MULTICAST_UPDATE_ALL_V2;
	}
	
	
	@Override
	public int getDataLength()
	{
		return MAX_BOTS + (MAX_BOTS * moveLength) + (MAX_BOTS * kickLength) + (MAX_BOTS * dribbleLength) + 1;
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 * @param slot
	 * @param botId
	 * @param move
	 * @param kick
	 * @param dribble
	 */
	public void setBot(int slot, int botId, TigerMotorMoveV2 move, TigerKickerKickV2 kick, TigerDribble dribble)
	{
		if (slot > (MAX_BOTS - 1))
		{
			return;
		}
		
		this.botId[slot] = botId;
		this.move[slot] = move;
		this.kick[slot] = kick;
		this.dribble[slot] = dribble;
		useMove |= (1 << slot);
	}
	
	
	/**
	 * 
	 * @param slot
	 * @param move
	 */
	public void setMove(int slot, TigerMotorMoveV2 move)
	{
		if (slot > (MAX_BOTS - 1))
		{
			return;
		}
		
		this.move[slot] = move;
		useMove |= (1 << slot);
	}
	
	
	/**
	 * 
	 * @param slot
	 * @param kick
	 */
	public void setKick(int slot, TigerKickerKickV2 kick)
	{
		if (slot > (MAX_BOTS - 1))
		{
			return;
		}
		
		this.kick[slot] = kick;
	}
	
	
	/**
	 * 
	 * @param slot
	 * @param dribble
	 */
	public void setDribble(int slot, TigerDribble dribble)
	{
		if (slot > (MAX_BOTS - 1))
		{
			return;
		}
		
		this.dribble[slot] = dribble;
	}
	
	
	/**
	 * 
	 * @param slot
	 * @param botId
	 */
	public void setId(int slot, int botId)
	{
		if (slot > (MAX_BOTS - 1))
		{
			return;
		}
		
		this.botId[slot] = botId;
	}
	
	
	/**
	 * 
	 * @param botId
	 * @return
	 */
	public int getSlot(int botId)
	{
		for (int i = 0; i < MAX_BOTS; i++)
		{
			if (this.botId[i] == botId)
			{
				return i;
			}
		}
		
		return -1;
	}
	
	
	/**
	 * 
	 * @param slot
	 * @return
	 */
	public TigerMotorMoveV2 getMove(int slot)
	{
		if (slot > (MAX_BOTS - 1))
		{
			return null;
		}
		
		return move[slot];
	}
	
	
	/**
	 * 
	 * @param slot
	 * @return
	 */
	public TigerKickerKickV2 getKick(int slot)
	{
		if (slot > (MAX_BOTS - 1))
		{
			return null;
		}
		
		return kick[slot];
	}
	
	
	/**
	 * 
	 * @param slot
	 * @return
	 */
	public TigerDribble getDribble(int slot)
	{
		if (slot > (MAX_BOTS - 1))
		{
			return null;
		}
		
		return dribble[slot];
	}
	
	
	/**
	 * 
	 * @param slot
	 * @return
	 */
	public boolean getUseMove(int slot)
	{
		if (slot > (MAX_BOTS - 1))
		{
			return false;
		}
		
		return (useMove & (1 << slot)) > 0;
	}
}
