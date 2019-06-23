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
 * Common multicast packet to update 5 bots.
 * 
 * @author AndreR
 * 
 */
public class TigerMulticastUpdateAllV2 extends ACommand
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private int useMove;
	private int botId[] = new int[5];
	private TigerMotorMoveV2 move[] = new TigerMotorMoveV2[5];
	private TigerKickerKickV2 kick[] = new TigerKickerKickV2[5];
	private TigerDribble dribble[] = new TigerDribble[5];
	
	private final int moveLength;
	private final int kickLength;
	private final int dribbleLength;
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public TigerMulticastUpdateAllV2()
	{
		moveLength = new TigerMotorMoveV2().getDataLength();
		kickLength = new TigerKickerKickV2().getDataLength();
		dribbleLength = new TigerDribble().getDataLength();
		
		for(int i = 0; i < 5; i++)
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
		
		for(int i = 0; i < 5; i++)
		{
			botId[i] = byteArray2UByte(data, i);
		}
		
		pos += 5;
		
		for(int i = 0; i < 5; i++)
		{
			move[i].setData(Arrays.copyOfRange(data, pos+i*moveLength, pos+i*moveLength+moveLength));
		}
		
		pos += 5*moveLength;
		
		for(int i = 0; i < 5; i++)
		{
			kick[i].setData(Arrays.copyOfRange(data, pos+i*kickLength, pos+i*kickLength+kickLength));
		}
		
		pos += 5*kickLength;
		
		for(int i = 0; i < 5; i++)
		{
			dribble[i].setData(Arrays.copyOfRange(data, pos+i*dribbleLength, pos+i*dribbleLength+dribbleLength));
		}
		
		pos += 5*dribbleLength;
		
		useMove = byteArray2UByte(data, pos);
	}

	@Override
	public byte[] getData()
	{
		byte data[] = new byte[getDataLength()];
		
		int pos = 0;
		
		for(int i = 0; i < 5; i++)
		{
			byte2ByteArray(data, i, botId[i]);
		}
		
		pos += 5;
		
		for(int i = 0; i < 5; i++)
		{
			System.arraycopy(move[i].getData(), 0, data, pos+i*moveLength, moveLength);
		}
		
		pos += 5*moveLength;
		
		for(int i = 0; i < 5; i++)
		{
			System.arraycopy(kick[i].getData(), 0, data, pos+i*kickLength, kickLength);
		}
		
		pos += 5*kickLength;
		
		for(int i = 0; i < 5; i++)
		{
			System.arraycopy(dribble[i].getData(), 0, data, pos+i*dribbleLength, dribbleLength);
		}
		
		pos += 5*dribbleLength;
		
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
		return 5 + 5*moveLength + 5*kickLength + 5*dribbleLength + 1;
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	public void setBot(int slot, int botId, TigerMotorMoveV2 move, TigerKickerKickV2 kick, TigerDribble dribble)
	{
		if(slot > 4)
		{
			return;
		}
		
		this.botId[slot] = botId;
		this.move[slot] = move;
		this.kick[slot] = kick;
		this.dribble[slot] = dribble;
		this.useMove |= (1 << slot);
	}
	
	public void setMove(int slot, TigerMotorMoveV2 move)
	{
		if(slot > 4)
		{
			return;
		}
		
		this.move[slot] = move;
		this.useMove |= (1 << slot);
	}
	
	public void setKick(int slot, TigerKickerKickV2 kick)
	{
		if(slot > 4)
		{
			return;
		}

		this.kick[slot] = kick;
	}
	
	public void setDribble(int slot, TigerDribble dribble)
	{
		if(slot > 4)
		{
			return;
		}

		this.dribble[slot] = dribble;
	}
	
	public void setId(int slot, int botId)
	{
		if(slot > 4)
		{
			return;
		}

		this.botId[slot] = botId;
	}
	
	
	public int getSlot(int botId)
	{
		for (int i = 0; i <= 4; i++)
		{
			if (this.botId[i] == botId)
			{
				return i;
			}
		}
		
		return -1;
	}
	
	
	public TigerMotorMoveV2 getMove(int slot)
	{
		if (slot > 4)
		{
			return null;
		}
		
		return move[slot];
	}
	
	
	public TigerKickerKickV2 getKick(int slot)
	{
		if (slot > 4)
		{
			return null;
		}
		
		return kick[slot];
	}
	
	
	public TigerDribble getDribble(int slot)
	{
		if (slot > 4)
		{
			return null;
		}
		
		return dribble[slot];
	}
	
	
	public boolean getUseMove(int slot)
	{
		if (slot > 4)
		{
			return false;
		}
		
		return (useMove & (1 << slot)) > 0;
	}
}
