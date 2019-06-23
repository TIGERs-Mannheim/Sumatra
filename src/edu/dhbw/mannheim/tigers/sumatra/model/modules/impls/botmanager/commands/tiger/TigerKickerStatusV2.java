/* 
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 30.08.2010
 * Author(s): AndreR
 *
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.ACommand;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.CommandConstants;

/**
 * Kicker status information.
 * - Charge current [mA]
 * - Capacitor level [V]
 * - temps
 * 
 * - Mode (Manual, Automatic charging)
 * - Arm state
 * - Arm device
 * 
 * @author AndreR
 * 
 */
public class TigerKickerStatusV2 extends ACommand
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	/** [mA], uint16_t */
	private int chargeCurrent;
	/** [V*e-1], uint16_t */
	private int capLevel;
	// °Ce-1
	private int TDiode[] = new int[2];
	// °Ce-1
	private int TIGBT[] = new int[2];
	// LENGTH = 12 bytes

	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public TigerKickerStatusV2()
	{
	}

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void setData(byte[] data)
	{
		chargeCurrent = byteArray2UShort(data, 0);
		capLevel = byteArray2UShort(data, 2);
		TDiode[0] = byteArray2UShort(data, 4);
		TDiode[1] = byteArray2UShort(data, 6);
		TIGBT[0] = byteArray2UShort(data, 8);
		TIGBT[1] = byteArray2UShort(data, 10);
	}

	@Override
	public byte[] getData()
	{
		byte data[] = new byte[getDataLength()];
		
		short2ByteArray(data, 0, chargeCurrent);
		short2ByteArray(data, 2, capLevel);
		short2ByteArray(data, 4, TDiode[0]);
		short2ByteArray(data, 6, TDiode[1]);
		short2ByteArray(data, 8, TIGBT[0]);
		short2ByteArray(data, 10, TIGBT[1]);
		
		return data;
	}
	
	@Override
	public int getCommand()
	{
		return CommandConstants.CMD_KICKER_STATUSV2;
	}
	
	@Override
	public int getDataLength()
	{
		return 12;
	}


	public float getChargeCurrent()
	{
		return chargeCurrent/1000f;
	}

	public void setChargeCurrent(float chargeCurrent)
	{
		this.chargeCurrent = (int) (chargeCurrent*1000);
	}

	public float getCapLevel()
	{
		return capLevel/10.0f;
	}

	public void setCapLevel(int capLevel)
	{
		this.capLevel = capLevel*10;
	}

	public float[] getTDiode()
	{
		float temp[] = new float[2];
		temp[0] = TDiode[0]/10.0f;
		temp[1] = TDiode[1]/10.0f;
		
		return temp;
	}

	public float[] getTIGBT()
	{
		float temp[] = new float[2];
		temp[0] = TIGBT[0]/10.0f;
		temp[1] = TIGBT[1]/10.0f;
		
		return temp;
	}
}
