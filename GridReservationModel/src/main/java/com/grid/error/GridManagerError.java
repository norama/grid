package com.grid.error;

/**
 *
 * @author Nora Mate
 */
public final class GridManagerError {
    
    private String message;
    
    public GridManagerError() {
        
    }
    
    public GridManagerError(Throwable error) {
        setMessage(error.getMessage());
    }

    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @param message the message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }
    
}
