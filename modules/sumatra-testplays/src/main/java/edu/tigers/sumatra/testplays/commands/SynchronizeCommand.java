/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.testplays.commands;

/**
 * @author Sebastian Stein <sebastian-stein@gmx.de>
 */
public class SynchronizeCommand extends ACommand {

    /** All roles of the same group will wait for each other */
    private int syncGroup = 0;

	
	/**
	 * Creates a new SynchronizeCommand with the syncGroup 0.
	 */
    public SynchronizeCommand() {
        this(0);
    }

	
	/**
	 * Creates a new SynchronizeCommand with the given syncGroup.
	 * A bot will wait in syncState until another bot with the same sycGroup enters the syncState.
	 *
	 * @param syncGroup
	 */
    public SynchronizeCommand(int syncGroup) {

        super(CommandType.SYNCHRONIZE);
        this.syncGroup = syncGroup;
    }
	
	
	public int getSyncGroup()
	{
		
		return syncGroup;
	}

	public void setSyncGroup(int syncGroup) {

		this.syncGroup = syncGroup;
	}
	
	
	@Override
	public String toString()
	{
		
		return getCommandType() + " [" + getSyncGroup() + "]";
	}
}
