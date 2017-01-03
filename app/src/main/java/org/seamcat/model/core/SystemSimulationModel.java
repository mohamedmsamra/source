package org.seamcat.model.core;

import org.seamcat.cdma.CDMASystem;
import org.seamcat.dmasystems.AbstractDmaMobile;
import org.seamcat.dmasystems.AbstractDmaSystem;
import org.seamcat.interfaces.Identifiable;
import org.seamcat.model.RadioSystem;
import org.seamcat.ofdma.OfdmaSystem;

import java.util.UUID;

public class SystemSimulationModel implements Identifiable {

    private boolean isDMASystem = false;
    private AbstractDmaSystem<? extends AbstractDmaMobile> dmasystem = null;
    private RadioSystem system;
    private String id;
    private final String name;

    public SystemSimulationModel(RadioSystem system, String name) {
        this.name = name;
        this.system = system;
        id = UUID.randomUUID().toString();
    }

    public RadioSystem getSystem() {
        return system;
    }

    public void setDMASystem( AbstractDmaSystem dmasystem ) {
        this.dmasystem = dmasystem;
        isDMASystem = true;
    }

    public AbstractDmaSystem getDMASystem() {
        return dmasystem;
    }

	public boolean isCDMASystem() {
		return isDMASystem && dmasystem instanceof CDMASystem;
	}

	public boolean isDMASystem() {
		return isDMASystem;
	}

	public boolean isOFDMASystem() {
		return isDMASystem && dmasystem instanceof OfdmaSystem;
	}

    public String getName() {
        return name;
    }

    @Override
    public String getReference() {
        return name;
    }

    public boolean isOfdmaSystem() {
        return isDMASystem && dmasystem instanceof OfdmaSystem;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return name;
    }
}
