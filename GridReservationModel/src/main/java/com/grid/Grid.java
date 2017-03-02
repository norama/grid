package com.grid;

import java.util.Collections;
import java.util.List;

/**
 *
 * @author Nora Mate
 */
public final class Grid {
    
    private List<GridCell> cells;
    
    public Grid() {
    }
    
    public Grid(List<GridCell> cells) {
        setCells(cells);
    }

    /**
     * @return the cells
     */
    public List<GridCell> getCells() {
        return cells;
    }

    /**
     * @param cells the cells to set
     */
    public void setCells(List<GridCell> cells) {
        this.cells = Collections.unmodifiableList(cells);
    }
}
