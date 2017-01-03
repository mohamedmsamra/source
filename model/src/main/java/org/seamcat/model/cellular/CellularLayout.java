package org.seamcat.model.cellular;

public interface CellularLayout {

    enum TierSetup {
        SingleCell,
        OneTier,
        TwoTiers
    }

    enum SectorSetup {
        SingleSector("Single"),
        TriSector3GPP("Tri-sector 3GPP"),
        TriSector3GPP2("Tri-sector 3GPP2");
        String name;
        SectorSetup(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    enum SystemLayout{
        CenterOfInfiniteNetwork("Center"),
        LeftHandSideOfNetworkEdge("Left"),
        RightHandSideOfNetworkEdge("Right");
        String name;
        SystemLayout( String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    SectorSetup getSectorSetup();

    TierSetup getTierSetup();

    double getCellRadius();

    SystemLayout getSystemLayout();

    boolean measureInterferenceFromEntireCluster();

    boolean generateWrapAround();

    int getIndexOfReferenceCell();

    int getReferenceSector();
}
