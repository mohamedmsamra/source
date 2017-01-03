package org.seamcat.presentation.library;

import org.seamcat.model.systems.SystemModel;
import org.seamcat.model.systems.generic.SystemModelGeneric;


import java.util.HashMap;
import java.util.Map;

public class SystemModelActiveTab {

    private static Map<Class<? extends SystemModel>, Integer> activeTab;

    static {
        activeTab = new HashMap<>();
        activeTab.put(SystemModelGeneric.class, 0);

        activeTab.put(SystemModel.class, 0);
    }

    public static int activeTab(Class<? extends SystemModel> clazz) {
        if ( !activeTab.containsKey(clazz)) return 0;

        return activeTab.get(clazz);
    }

    public static void activeTab(Class<? extends SystemModel> clazz, int tab) {
        activeTab.put(clazz, tab);
    }

	public static void activet_Tab(Class<? extends SystemModel> clazz, int tab) {
		  activeTab.put(clazz, tab);
		
	}

	
}
