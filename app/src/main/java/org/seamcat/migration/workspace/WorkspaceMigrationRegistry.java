package org.seamcat.migration.workspace;

import org.seamcat.migration.MigrationRegistry;


public class WorkspaceMigrationRegistry extends MigrationRegistry {

    public WorkspaceMigrationRegistry() {
        registerMigration(new ZeroPathPositioningsMigration());
        registerMigration(new AzimuthElevationTransceiverWorkspaceMigration());
        registerMigration(new BlockingAttenuationMigration());
        registerMigration(new SpectrumEmissionMaskWorkspaceMigration());
        registerMigration(new AzimuthNegationWorkspaceMigration());
        registerMigration(new XmlToZipFileWorkspaceMigration());
        registerMigration(new ReceiverBlockingWorkspaceMigration());
        registerMigration(new ResultVectorsWorkspaceMigration());
        registerMigration(new RemoveCorrelationsWorkspaceMigration());
        registerMigration(new RemoveUnusedAttributesWorkspaceMigration());
        registerMigration(new PropagationModelWorkspaceMigration());
        registerMigration(new CoverageRadiusWorkspaceMigration());
        registerMigration(new PropagationModelHata21WorkspaceMigration());
        registerMigration(new PropagationModelHata24AndSDWorkspaceMigration());
        registerMigration(new WantedReceiverTransmitterWorkspaceMigration());
        registerMigration(new VictimReceiverWorkspaceMigration());
        registerMigration(new LocalEnvironmentWorkspaceMigration());
        registerMigration(new BlockingUnwantedRemovalWorkspaceMigration());
        registerMigration(new JarIdWorkspaceMigration());
        registerMigration(new CoverageRadiusWorkspaceResultMigration());
        registerMigration(new SignalsWorkspaceResultMigration());
        registerMigration(new DMAWorkspaceResultMigration());
        registerMigration(new TransceiverWorkspaceMigration());
        registerMigration(new PluginWorkspaceMigration());
        registerMigration(new AntennaPluginWorkspaceMigration());
        registerMigration(new CustomResultsWorkspaceResultMigration());
        registerMigration(new PropagationRenameWorkspaceResultMigration());
        registerMigration(new TransmitterWorkspaceMigration());
        registerMigration(new SensingLinkWorkspaceMigration());
        registerMigration(new DensityILWorkspaceMigration());
        registerMigration(new InterferenceLinkWorkspaceMigration());
        registerMigration(new RelativePositionWorkspaceMigration());
        registerMigration(new DmaLayoutSectorWorkspaceMigration());
        registerMigration(new SystemFirstWorkspaceMigration());
        registerMigration(new SystemSecondWorkspaceMigration());
        registerMigration(new SystemThirdWorkspaceMigration());
        registerMigration(new SystemFourthWorkspaceMigration());
        registerMigration(new SystemFifthWorkspaceMigration());
        registerMigration(new SystemUIWorkspaceMigration());
        registerMigration(new LocalEnvironmentsWorkspaceMigration());
        registerMigration(new ShapeWorkspaceMigration());
        registerMigration(new InterferenceLinkNameWorkspaceMigration());
        registerMigration(new ValueDistributionWorkspaceMigration());
        registerMigration(new SystemDistributionWorkspaceMigration());
        registerMigration(new DRSSWorkspaceMigration());
        registerMigration(new AntennaDMAPatternWorkspaceMigration());
        registerMigration(new SensingWorkspaceMigration());
        registerMigration(new InterferersDensityWorkspaceMigration());
        registerMigration(new EmissionMaskWorkspaceMigration());
    }
}
