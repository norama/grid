package com.grid;

import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.core.Application;

/**
 *
 * @author Nora Mate
 */
@javax.ws.rs.ApplicationPath("webresources")
public class ApplicationConfig extends Application {
    
    @Override
    public Set<Object> getSingletons() {
        Set<Object> singletons = new HashSet<> ();
        singletons.add(new GridResource());
        return singletons;
    }

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> resources = new HashSet<>();
        addRestResourceClasses(resources);
        return resources;
    }

    /**
     * Do not modify addRestResourceClasses() method.
     * It is automatically populated with
     * all resources defined in the project.
     * If required, comment out calling this method in getClasses().
     */
    private void addRestResourceClasses(Set<Class<?>> resources) {
        resources.add(com.grid.CORSFilter.class);
        resources.add(com.grid.GridResource.class);
        resources.add(com.grid.error.IllegalStateExceptionMapper.class);
        resources.add(com.grid.error.JSONExceptionMapper.class);
        resources.add(com.grid.error.UnirestExceptionMapper.class);
    }
    
}
