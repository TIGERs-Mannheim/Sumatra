/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.plays;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.ai.data.frames.MetisAiFrame;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.keeper.KeeperOneOnOneRole;

/**
 * A dummy Play for the Keeper in the shootout
 */
public class KeeperShootoutPlay extends APlay {

    private static final Logger log = Logger.getLogger(KeeperShootoutPlay.class.getName());

    /**
     * Default constructor
     */
    public KeeperShootoutPlay() {
        super(EPlay.KEEPER_SHOOTOUT);
    }

    @Override
    protected ARole onRemoveRole(final MetisAiFrame frame) {
        if (getRoles().isEmpty()) {
            log.warn("Remove role without any role");
            return null;
        }
        return getRoles().get(0);
    }

    @Override
    protected ARole onAddRole(final MetisAiFrame frame) {
        if (getRoles().isEmpty()) {
            return new KeeperOneOnOneRole();
        }
        log.warn("KeeperShootoutPlay can only handle one role");
        return null;
    }
}
