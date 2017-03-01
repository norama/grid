package com.grid;

import java.util.function.Function;
import org.eclipse.persistence.jpa.jpql.Assert;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Nora Mate
 */
public final class GridCell {
    
    private int id;
    private int row;
    private int col;
    private int status;
    private String ticket;
    private String owner;
    private String data;
    private String remark;
    private String modified;
    
    public GridCell() {
        
    }
    
    public GridCell init(String source) {
        try {
            return init(new JSONObject(source));
        } catch (JSONException e) {
            throw new IllegalArgumentException(e);
        }
    }
    
    public GridCell init(JSONObject source) {
        Assert.isTrue(source.has("id"), "Missing parameter: id");
        try {
            setId(source.getInt("id"));            
            setIntProp(this::setRow, "row", source);
            setIntProp(this::setCol, "col", source);
            setIntProp(this::setStatus, "status", source);
            setStringProp(this::setTicket, "ticket", source);
            setStringProp(this::setOwner, "owner", source);
            setStringProp(this::setData, "data", source);
            setStringProp(this::setRemark, "remark", source);
            setStringProp(this::setModified, "modified", source);
        } catch (JSONException e) {
            throw new IllegalArgumentException(e);
        }
        return this;
    }
    
    private void setIntProp(Function<Integer, GridCell> setter, String key, JSONObject source) throws JSONException {
        if (source.has(key)) {
            if (!source.isNull(key)) {
                setter.apply(source.getInt(key));
            }
        }
    }
    
    private void setStringProp(Function<String, GridCell> setter, String key, JSONObject source) throws JSONException {
        if (source.has(key)) {
            if (!source.isNull(key)) {
                setter.apply(source.getString(key));
            }
        }
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public GridCell setId(int id) {
        this.id = id;
        return this;
    }

    /**
     * @return the row
     */
    public int getRow() {
        return row;
    }

    /**
     * @param row the row to set
     */
    public GridCell setRow(int row) {
        this.row = row;
        return this;
    }

    /**
     * @return the col
     */
    public int getCol() {
        return col;
    }

    /**
     * @param col the col to set
     */
    public GridCell setCol(int col) {
        this.col = col;
        return this;
    }

    /**
     * @return the status
     */
    public int getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public GridCell setStatus(int status) {
        this.status = status;
        return this;
    }

    /**
     * @return the ticket
     */
    public String getTicket() {
        return ticket;
    }

    /**
     * @param ticket the ticket to set
     */
    public GridCell setTicket(String ticket) {
        this.ticket = ticket;
        return this;
    }

    /**
     * @return the owner
     */
    public String getOwner() {
        return owner;
    }

    /**
     * @param owner the owner to set
     */
    public GridCell setOwner(String owner) {
        this.owner = owner;
        return this;
    }

    /**
     * @return the data
     */
    public String getData() {
        return data;
    }

    /**
     * @param data the data to set
     */
    public GridCell setData(String data) {
        this.data = data;
        return this;
    }

    /**
     * @return the remark
     */
    public String getRemark() {
        return remark;
    }

    /**
     * @param remark the remark to set
     */
    public GridCell setRemark(String remark) {
        this.remark = remark;
        return this;
    }

    /**
     * @return the modified
     */
    public String getModified() {
        return modified;
    }

    /**
     * @param modified the modified to set
     */
    public GridCell setModified(String modified) {
        this.modified = modified;
        return this;
    }
    

}
