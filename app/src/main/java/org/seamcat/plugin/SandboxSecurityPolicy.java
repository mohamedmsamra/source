package org.seamcat.plugin;

import org.apache.log4j.Logger;

import java.io.FilePermission;
import java.security.*;
import java.util.PropertyPermission;

/** Security policy which restricts permissions for plugin code while
 * granting regular application code full permissions. 
 */
public class SandboxSecurityPolicy extends Policy {
    
    private static final Logger logger = Logger.getLogger(SandboxSecurityPolicy.class);
    
    @Override
    public PermissionCollection getPermissions(CodeSource codesource) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PermissionCollection getPermissions(ProtectionDomain domain) {
        if (isPlugin(domain)) {
            logger.debug("Plugin permissions for: " + domain.getCodeSource());
            return pluginPermissions(); 
        }
        else {
            logger.debug("Application permissions for: " + domain.getCodeSource());
            return applicationPermissions();
        }        
    }

    private boolean isPlugin(ProtectionDomain domain) {
        return domain.getClassLoader() instanceof PluginClassLoader;
    }

    private PermissionCollection pluginPermissions() {
        Permissions permissions = new Permissions();
        permissions.add(new PropertyPermission("*", "read"));
        permissions.add(new FilePermission("<<ALL FILES>>", "read"));
        permissions.add(new RuntimePermission("preferences"));
        return permissions;
    }

    private PermissionCollection applicationPermissions() {
        Permissions permissions = new Permissions();
        permissions.add(new AllPermission());
        return permissions;
    }
}
