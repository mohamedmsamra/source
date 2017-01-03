package org.seamcat.plugin;

import org.apache.log4j.Logger;

import java.security.Policy;

public class SandboxInitializer {
    
    private static final Logger logger = Logger.getLogger(SandboxInitializer.class);

    private static SandboxSecurityPolicy policy = new SandboxSecurityPolicy();
    private static SecurityManager securityManager = new SecurityManager();

    public static void initializeSandbox() {
        logger.info("Initializing sandbox");
        Policy.setPolicy(policy);
        System.setSecurityManager(securityManager);
    }
    
    public static void verifySandbox() {
        if (!sandboxIsOkay()) {
            throw new RuntimeException("Sandbox could not be verified");
        }
    }

    private static boolean sandboxIsOkay() {
        return System.getSecurityManager() == securityManager
                && Policy.getPolicy() == policy;
    }
}
