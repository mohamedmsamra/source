package org.seamcat.migration.settings;

import org.seamcat.migration.MigrationRegistry;


public class SettingsMigrationRegistry extends MigrationRegistry {		
	public SettingsMigrationRegistry() {
		registerMigration(new AzimuthElevationTransceiverSettingsMigration());
		registerMigration(new SpectrumEmissionMaskSettingsMigration());
		registerMigration(new AzimuthNegationSettingsMigration());
		registerMigration(new ReceiverBlockingMaskSettingsMigration());
		registerMigration(new PropagationModelSettingsMigration());
        registerMigration(new PropagationModelHata21SettingsMigration());
        registerMigration(new PropagationModelHata24AndSDSettingsMigration());
        registerMigration(new JarIdSettingsMigration());
        registerMigration(new TransceiverSettingsMigration());
        registerMigration(new AntennaPluginSettingsMigration());
        registerMigration(new PropagationRenameSettingsMigration());
        registerMigration(new TransmitterSettingsMigration());
        registerMigration(new SensingLinkSettingsMigration());
        registerMigration(new ReceiverSettingsMigration());
        registerMigration(new TransmitterCompositeSettingsMigration());
        registerMigration(new BlockingMaskSettingsMigration());
        registerMigration(new SensingSettingsMigration());
        registerMigration(new InterferersDensitySettingsMigration());
        registerMigration(new EmissionMaskSettingsMigration());
	}
}
