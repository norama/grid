package com.grid;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 *
 * @author noram
 */
public class ApplicationContext {
    
    private static final Logger LOGGER = Logger.getLogger("ApplicationContext");
    
    static public long getExpiration() {
        return lookup("settings/expiration", 5);
    }
    
    static public long getExpirationScanPeriod() {
        return lookup("settings/expirationScanPeriod", 5);
    }
    
    static public String getDreamFactoryUrl() {
        return lookup("dreamfactory/url", "http://127.0.0.1:82");
    }
    
    static private <T> T lookup(String name, T defval) {
        T value;
        try {
            InitialContext ic = new InitialContext();
            Context context = (Context) ic.lookup("java:comp/env");
            value = (T) context.lookup(name);
        } catch (NamingException ex) {
            LOGGER.log(Level.INFO, "NamingException: {0} thrown while reading application parameter: {1}. Taking default value: {2}", new Object[]{ex.getMessage(), name, defval});
            value = null;
        }
        return value != null ? value : defval;
    }
    
}
