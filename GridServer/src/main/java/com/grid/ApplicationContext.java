package com.grid;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 *
 * @author noram
 */
public class ApplicationContext {
    
    static public long getExpiration() {
        return lookup("settings/expiration", 5);
    }
    
    static public long getExpirationScanPeriod() {
        return lookup("settings/expirationScanPeriod", 5);
    }
    
    static private Integer lookup(String name, Integer defval) {
        Integer value;
        try {
            InitialContext ic = new InitialContext();
            Context context = (Context) ic.lookup("java:comp/env");
            value = (Integer) context.lookup(name);
        } catch (NamingException ex) {
            value = null;
        }
        return value != null ? value : defval;
    }
    
}
