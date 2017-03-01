package com.grid.background;

import com.grid.GridManager;
import java.util.logging.Logger;

/**
 *
 * @author Nora Mate
 */
public class ExpirationHandlerJob implements Runnable {

    private static final Logger LOGGER = Logger.getLogger("ExpirationHandlerJob");

    private final GridManager gridManager = new GridManager();

    @Override
    public void run() {
        LOGGER.info("--- Expiration scan started. ---");
        
        try {
            String token = gridManager.openSession();

            try {
                gridManager.freeExpiredCells(token);
            } finally {
                gridManager.closeSession(token);
            }
        } catch (Exception ex) {
            LOGGER.throwing("ExpirationHandlerJob", "run", ex);
        }
        
        LOGGER.info("--- Expiration scan ended. ---");
    }
}
