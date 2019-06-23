/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.keeper.states;

import edu.tigers.sumatra.ai.pandora.roles.keeper.KeeperRole;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.skillsystem.skills.ReceiverSkill;

/**
 * Restricted use to OneOnOneKeeper. If the Keeper was going out, but get over chipped, this state tries to catch the ball
 */
public class CatchOverChipState extends AKeeperState {

    private ReceiverSkill receiverSkill;

    /**
     * Default
     *
     * @param parent
     */
    public CatchOverChipState(final KeeperRole parent) {
        super(parent);
    }

    @Override
    public void doEntryActions() {
        receiverSkill = new ReceiverSkill();
        receiverSkill.getMoveCon().setPenaltyAreaAllowedOur(true);
        setNewSkill(receiverSkill);
    }

    @Override
    public void doUpdate() {
        IVector2 destination = LineMath.stepAlongLine(getWFrame().getBall().getPos(), Geometry.getGoalOur().getCenter(), Geometry.getBotRadius());
        receiverSkill.setDesiredDestination(destination);
    }


}
