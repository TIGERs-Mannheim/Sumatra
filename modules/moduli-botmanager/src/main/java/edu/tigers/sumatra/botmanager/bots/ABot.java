package edu.tigers.sumatra.botmanager.bots;

import edu.tigers.sumatra.bot.EBotType;
import edu.tigers.sumatra.bot.ERobotHealthState;
import edu.tigers.sumatra.bot.IBot;
import edu.tigers.sumatra.bot.params.BotParams;
import edu.tigers.sumatra.botmanager.data.BotFeedback;
import edu.tigers.sumatra.botmanager.data.MatchCommand;
import edu.tigers.sumatra.botparams.EBotParamLabel;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.trajectory.TrajectoryWithTime;
import lombok.Getter;

import java.util.Optional;


/**
 * Bot base class.
 */
public abstract class ABot implements IBot
{
	private final BotID botId;
	private final EBotType type;

	private BotParams botParams = new BotParams();
	private TrajectoryWithTime<IVector3> curTrajectory = null;
	private String controlledBy = "";

	@Getter
	protected MatchCommand lastSentMatchCommand = new MatchCommand();

	@Getter
	protected BotFeedback lastReceivedBotFeedback = new BotFeedback();


	protected ABot(final EBotType type, final BotID id)
	{
		botId = id;
		this.type = type;
	}


	public abstract EBotParamLabel getBotParamLabel();

	public abstract void sendMatchCommand(final MatchCommand matchCommand);


	@Override
	public boolean isAvailableToAi()
	{
		return !isBlocked() && getHealthState() != ERobotHealthState.UNUSABLE;
	}


	@Override
	public final EBotType getType()
	{
		return type;
	}


	@Override
	public String toString()
	{
		return "[Bot: " + type + "|" + getBotId() + "]";
	}


	@Override
	public final String getControlledBy()
	{
		return controlledBy;
	}


	/**
	 * @param controlledBy the controlledBy to set
	 */
	public final void setControlledBy(final String controlledBy)
	{
		this.controlledBy = controlledBy;
	}


	@Override
	public final ETeamColor getColor()
	{
		return getBotId().getTeamColor();
	}


	@Override
	public final boolean isBlocked()
	{
		return !controlledBy.isEmpty();
	}


	@Override
	public final BotID getBotId()
	{
		return botId;
	}


	@Override
	public Optional<TrajectoryWithTime<IVector3>> getCurrentTrajectory()
	{
		return Optional.ofNullable(curTrajectory);
	}


	/**
	 * @param curTrajectory
	 */
	public void setCurrentTrajectory(final TrajectoryWithTime<IVector3> curTrajectory)
	{
		this.curTrajectory = curTrajectory;
	}


	@Override
	public final BotParams getBotParams()
	{
		return botParams;
	}


	public void setBotParams(final BotParams botParams)
	{
		this.botParams = botParams;
	}
}
