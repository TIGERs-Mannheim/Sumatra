package edu.tigers.sumatra.botmanager.commands.tigerv3;

import edu.tigers.sumatra.botmanager.commands.ACommand;
import edu.tigers.sumatra.botmanager.commands.ECommand;
import edu.tigers.sumatra.botmanager.serial.SerialData;
import edu.tigers.sumatra.botmanager.serial.SerialData.ESerialDataType;

import java.nio.charset.StandardCharsets;


/**
 * Description of a config element (single value).
 * Probably only the name for now.
 *
 * @author AndreR
 */
public class TigerConfigItemDesc extends ACommand
{
	@SerialData(type = ESerialDataType.UINT16)
	private int configId;

	@SerialData(type = ESerialDataType.UINT8)
	private int element;

	@SerialData(type = ESerialDataType.TAIL)
	private byte[] name;

	/**
	 * Constant for the filename of a config, element number otherwise.
	 */
	public static final int CONFIG_ITEM_FILE_NAME = 0xFF;


	@SuppressWarnings("unused") // used by reflection
	public TigerConfigItemDesc()
	{
		super(ECommand.CMD_CONFIG_ITEM_DESC, true);
	}


	/**
	 * @param cfgId
	 * @param element
	 */
	public TigerConfigItemDesc(final int cfgId, final int element)
	{
		super(ECommand.CMD_CONFIG_ITEM_DESC, true);

		configId = cfgId;
		this.element = element;
	}


	/**
	 * @return the configId
	 */
	public int getConfigId()
	{
		return configId;
	}


	/**
	 * @param configId the configId to set
	 */
	public void setConfigId(final int configId)
	{
		this.configId = configId;
	}


	/**
	 * @return the element
	 */
	public int getElement()
	{
		return element;
	}


	/**
	 * @param element the element to set
	 */
	public void setElement(final int element)
	{
		this.element = element;
	}


	/**
	 * @return
	 */
	public String getName()
	{
		return new String(name, StandardCharsets.US_ASCII);
	}
}
