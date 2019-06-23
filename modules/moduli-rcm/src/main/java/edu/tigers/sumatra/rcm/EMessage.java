package edu.tigers.sumatra.rcm;

import com.google.protobuf.Message;

import edu.dhbw.mannheim.tigers.sumatra.proto.AiControlAvailableProtos.AiControlAvailable;
import edu.dhbw.mannheim.tigers.sumatra.proto.AiControlProtos.AiControl;
import edu.dhbw.mannheim.tigers.sumatra.proto.AiControlStateProtos.AiControlState;
import edu.dhbw.mannheim.tigers.sumatra.proto.BotActionCommandProtos.BotActionCommand;
import edu.dhbw.mannheim.tigers.sumatra.proto.BotStatusProtos.BotStati;
import edu.dhbw.mannheim.tigers.sumatra.proto.LifeSignProtos.LifeSign;
import edu.dhbw.mannheim.tigers.sumatra.proto.LogMessagesProtos.LogMessage;
import edu.dhbw.mannheim.tigers.sumatra.proto.RefereeMsgProtos.RefereeCommandSimple;
import edu.tigers.bluetoothprotobuf.IMessageType;


/**
 * 
 */
public enum EMessage implements IMessageType
{
	/**  */
	AI_CONTROL_AVAILABLE(1, AiControlAvailable.getDefaultInstance()),
	/**  */
	AI_CONTROL(2, AiControl.getDefaultInstance()),
	/**  */
	BOT_ACTION_COMMAND(3, BotActionCommand.getDefaultInstance()),
	/**  */
	BOT_STATI(4, BotStati.getDefaultInstance()),
	/**  */
	LIFE_SIGN(5, LifeSign.getDefaultInstance()),
	/**  */
	LOG_MESSAGES(6, LogMessage.getDefaultInstance()),
	/**  */
	AI_CONTROL_STATE(7, AiControlState.getDefaultInstance()),
	/**  */
	REFEREE_COMMAND(8, RefereeCommandSimple.getDefaultInstance());
	
	private final int			id;
	private final Message	protoMsg;
	
	
	private EMessage(final int id, final Message protoMsg)
	{
		this.id = id;
		this.protoMsg = protoMsg;
	}
	
	
	/**
	 * @return the id
	 */
	@Override
	public final int getId()
	{
		return id;
	}
	
	
	/**
	 * @return the protoMsg
	 */
	@Override
	public final Message getProtoMsg()
	{
		return protoMsg;
	}
}
