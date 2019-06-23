/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.keeper.states;

import edu.tigers.sumatra.ai.metis.keeper.EKeeperState;
import edu.tigers.sumatra.ai.pandora.roles.keeper.KeeperRole;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.line.ILine;
import edu.tigers.sumatra.math.line.Line;
import edu.tigers.sumatra.skillsystem.skills.AMoveToSkill;
import edu.tigers.sumatra.skillsystem.skills.CatchBallSkill;

/**
 * For OneOnOne Keeper. If ball velocity is directed to Goal, first intercept the shooting line and then go out.
 */
public class InterceptAndGoOutState extends AKeeperState {

    private boolean isCatchSkillActive = true;

    /**
     * @param parent
     */
    public InterceptAndGoOutState(final KeeperRole parent) {
		super(parent, EKeeperState.INTERCEPT_AND_GO_OUT);
    }

    @Override
    public void doEntryActions() {
		CatchBallSkill catchBallSkill = new CatchBallSkill();
		catchBallSkill.getMoveCon().setDestinationOutsideFieldAllowed(true);
		catchBallSkill.getMoveCon().setPenaltyAreaAllowedOur(true);
		catchBallSkill.getMoveCon().setBotsObstacle(false);
		setNewSkill(catchBallSkill);
    }


    @Override
    public void doExitActions() {
        // nothing to do here
    }


    @Override
    public void doUpdate() {
        ILine ballLine = Line.fromDirection(getWFrame().getBall().getPos(), getWFrame().getBall().getVel().multiplyNew(10));

        if (ballLine.distanceTo(getPos()) < Geometry.getBotRadius() / 2) {
            if (isCatchSkillActive && !getWFrame().getBall().isChipped() && parent.getBot().getVel().getLength2() < 0.1) {
				AMoveToSkill moveToTrajSkill = AMoveToSkill.createMoveToSkill();
                moveToTrajSkill.getMoveCon().setDestinationOutsideFieldAllowed(false);
                moveToTrajSkill.getMoveCon().setPenaltyAreaAllowedOur(true);
                moveToTrajSkill.getMoveCon().setBallObstacle(false);
                moveToTrajSkill.getMoveCon().setBotsObstacle(false);
                moveToTrajSkill.getMoveCon().updateDestination(getWFrame().getBall().getPos());
                moveToTrajSkill.getMoveCon().updateLookAtTarget(getWFrame().getBall().getPos());
                setNewSkill(moveToTrajSkill);
                isCatchSkillActive = false;
            } else {
                super.parent.getCurrentSkill().getMoveCon().updateDestination(getWFrame().getBall().getPos());
                super.parent.getCurrentSkill().getMoveCon().updateLookAtTarget(getWFrame().getBall().getPos());
            }
        } else {
            if (!isCatchSkillActive) {
				CatchBallSkill catchBallSkill = new CatchBallSkill();
				catchBallSkill.getMoveCon().setDestinationOutsideFieldAllowed(true);
				catchBallSkill.getMoveCon().setPenaltyAreaAllowedOur(true);
				catchBallSkill.getMoveCon().setBotsObstacle(false);
				setNewSkill(catchBallSkill);
                isCatchSkillActive = true;
            }
        }
    }
}
