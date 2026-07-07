package edu.tigers.sumatra.botmanager.commands.basestation;

import edu.tigers.sumatra.botmanager.commands.ACommand;
import edu.tigers.sumatra.botmanager.commands.ECommand;
import edu.tigers.sumatra.botmanager.serial.SerialData;
import edu.tigers.sumatra.botmanager.serial.SerialData.ESerialDataType;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.SumatraMath;
import lombok.Getter;


/**
 * Synopsis of all Wifi traffic and feedback from bot.
 *
 * @author AndreR
 */
public class BaseStationWifiStatsV2 extends ACommand
{
	/**
	 * Maximum number of bots in this report.
	 * If more bots are connected than their information is discarded.
	 */
	public static final int NUM_BOTS = 16;

	/**
	 * Bot network statistics.
	 */
	public static class BotStats
	{
		public static class BufferStats
		{
			@SerialData(type = ESerialDataType.UINT32)
			public long rxPacketsTotal;

			@SerialData(type = ESerialDataType.UINT32)
			public long rxBytesTotal;

			@SerialData(type = ESerialDataType.UINT32)
			public long rxBufferOverruns;

			@SerialData(type = ESerialDataType.UINT32)
			public long rxBufferDataOverflows;

			@SerialData(type = ESerialDataType.UINT32)
			public long rxCobsDecodingErrors;

			@SerialData(type = ESerialDataType.UINT32)
			public long rxCrcErrors;

			@SerialData(type = ESerialDataType.UINT32)
			public long txPacketsTotal;

			@SerialData(type = ESerialDataType.UINT32)
			public long txBytesTotal;

			@SerialData(type = ESerialDataType.UINT32)
			public long txBufferUnderrun;


			public BufferStats subtractNew(final BufferStats rhs)
			{
				BufferStats ret = new BufferStats();

				ret.rxPacketsTotal = rxPacketsTotal - rhs.rxPacketsTotal;
				ret.rxBytesTotal = rxBytesTotal - rhs.rxBytesTotal;
				ret.rxBufferOverruns = rxBufferOverruns - rhs.rxBufferOverruns;
				ret.rxBufferDataOverflows = rxBufferDataOverflows - rhs.rxBufferDataOverflows;
				ret.rxCobsDecodingErrors = rxCobsDecodingErrors - rhs.rxCobsDecodingErrors;
				ret.rxCrcErrors = rxCrcErrors - rhs.rxCrcErrors;
				ret.txPacketsTotal = txPacketsTotal - rhs.txPacketsTotal;
				ret.txBytesTotal = txBytesTotal - rhs.txBytesTotal;
				ret.txBufferUnderrun = txBufferUnderrun - rhs.txBufferUnderrun;
				ret.rxPacketsTotal &= 0xFFFFFFFFL;
				ret.rxBytesTotal &= 0xFFFFFFFFL;
				ret.rxBufferOverruns &= 0xFFFFFFFFL;
				ret.rxBufferDataOverflows &= 0xFFFFFFFFL;
				ret.rxCobsDecodingErrors &= 0xFFFFFFFFL;
				ret.rxCrcErrors &= 0xFFFFFFFFL;
				ret.txPacketsTotal &= 0xFFFFFFFFL;
				ret.txBytesTotal &= 0xFFFFFFFFL;
				ret.txBufferUnderrun &= 0xFFFFFFFFL;

				return ret;
			}
		}

		public static class OTAStats
		{
			@SerialData(type = ESerialDataType.UINT32)
			public long rxPacketsLost;

			@SerialData(type = ESerialDataType.UINT32)
			public long txPacketsLost;

			@SerialData(type = ESerialDataType.INT16)
			public int rxRssi;


			public OTAStats subtractNew(final OTAStats rhs)
			{
				OTAStats ret = new OTAStats();

				ret.rxPacketsLost = rxPacketsLost - rhs.rxPacketsLost;
				ret.txPacketsLost = txPacketsLost - rhs.txPacketsLost;
				ret.rxPacketsLost &= 0xFFFFFFFFL;
				ret.txPacketsLost &= 0xFFFFFFFFL;
				ret.rxRssi = rxRssi;

				return ret;
			}
		}

		/**
		 * Buffer stats from data processing.
		 */
		@SerialData(type = ESerialDataType.EMBEDDED)
		public BufferStats buf = new BufferStats();

		/**
		 * Over-the-air stats from wireless module.
		 */
		@SerialData(type = ESerialDataType.EMBEDDED)
		public OTAStats ota = new OTAStats();

		/**
		 * Bot ID. 255 if unused (bot is offline).
		 */
		@SerialData(type = ESerialDataType.UINT8)
		private int botId = 0xFF;


		/**
		 * return this-rhs
		 * botId stays the same
		 *
		 * @param rhs
		 * @return
		 */
		public BotStats subtractNew(final BotStats rhs)
		{
			BotStats ret = new BotStats();

			ret.buf = buf.subtractNew(rhs.buf);
			ret.ota = ota.subtractNew(rhs.ota);
			ret.botId = botId;

			return ret;
		}


		/**
		 * @return
		 */
		public BotID getBotId()
		{
			return BotID.createBotIdFromIdWithColorOffsetBS(botId);
		}


		public double getBaseRxRssi()
		{
			return ota.rxRssi * 0.1;
		}


		/**
		 * Compute link quality from RSSI.
		 *
		 * @return Quality from 1.0 (good) to 0.0 (bad)
		 */
		public double getLinkQuality()
		{
			return SumatraMath.relative(getBaseRxRssi(), -80.0, -12.0);
		}


		public double getTxLoss()
		{
			return (double) ota.txPacketsLost / (buf.txPacketsTotal + 1);
		}


		public double getRxLoss()
		{
			return (double) ota.rxPacketsLost / (buf.rxPacketsTotal + ota.rxPacketsLost + 1);
		}
	}

	public static class ModuleStats
	{
		@SerialData(type = ESerialDataType.UINT32)
		public long rxSyncErrors;

		@SerialData(type = ESerialDataType.UINT32)
		public long rxCrcErrors;

		@SerialData(type = ESerialDataType.UINT32)
		public long rxPacketsGood;

		@SerialData(type = ESerialDataType.UINT32)
		public long rxFromOtherBase;

		@SerialData(type = ESerialDataType.UINT32)
		public long cycleDuration;

		@SerialData(type = ESerialDataType.UINT8)
		public int numTimeslotsUsed;


		public ModuleStats subtractNew(final ModuleStats rhs)
		{
			ModuleStats ret = new ModuleStats();

			ret.rxSyncErrors = rxSyncErrors - rhs.rxSyncErrors;
			ret.rxCrcErrors = rxCrcErrors - rhs.rxCrcErrors;
			ret.rxPacketsGood = rxPacketsGood - rhs.rxPacketsGood;
			ret.rxFromOtherBase = rxFromOtherBase - rhs.rxFromOtherBase;
			ret.cycleDuration = cycleDuration;
			ret.numTimeslotsUsed = numTimeslotsUsed;
			ret.rxSyncErrors &= 0xFFFFFFFFL;
			ret.rxCrcErrors &= 0xFFFFFFFFL;
			ret.rxPacketsGood &= 0xFFFFFFFFL;
			ret.rxFromOtherBase &= 0xFFFFFFFFL;

			return ret;
		}


		/**
		 * @return Update rate in Hz to update all robots once.
		 */
		public double getUpdateRate()
		{
			return 1e6 / cycleDuration;
		}


		/**
		 * @return how many bytes can be sent or received from a single robot per second.
		 */
		public double getBytesPerSecondPerRobotSlot()
		{
			return getUpdateRate() * 39;
		}
	}

	@Getter
	@SerialData(type = ESerialDataType.EMBEDDED)
	private final BotStats[] botStats = new BotStats[NUM_BOTS];

	@Getter
	@SerialData(type = ESerialDataType.EMBEDDED)
	private ModuleStats moduleStats = new ModuleStats();


	public BaseStationWifiStatsV2()
	{
		super(ECommand.CMD_BASE_WIFI_STATS_V2);

		for (int i = 0; i < NUM_BOTS; i++)
		{
			botStats[i] = new BotStats();
		}
	}


	/**
	 * Create a new BaseStationWifiStats object from the operation a-b.
	 * Fields not representing a counted value are taken from a.
	 *
	 * @param a
	 * @param b
	 */
	public BaseStationWifiStatsV2(final BaseStationWifiStatsV2 a, final BaseStationWifiStatsV2 b)
	{
		super(ECommand.CMD_BASE_WIFI_STATS_V2);

		moduleStats = a.moduleStats.subtractNew(b.moduleStats);

		for (int i = 0; i < NUM_BOTS; i++)
		{
			botStats[i] = a.botStats[i].subtractNew(b.botStats[i]);
		}
	}


	/**
	 * @return Update rate in Hz to update all robots once.
	 */
	public double getUpdateRate()
	{
		return moduleStats.getUpdateRate();
	}


	/**
	 * Check if the given bot exists in this statistics messages.
	 *
	 * @param id
	 * @return
	 */
	public boolean isBotConnected(final BotID id)
	{
		int number = id.getNumberWithColorOffsetBS();

		for (BotStats s : botStats)
		{
			if (s.botId == number)
			{
				return true;
			}
		}

		return false;
	}
}
