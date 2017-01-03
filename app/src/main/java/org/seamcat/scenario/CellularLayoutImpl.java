package org.seamcat.scenario;

import org.seamcat.model.cellular.CellularLayout;

public class CellularLayoutImpl implements CellularLayout {

    private TierSetup tierSetup;
    private SectorSetup sectorSetup;
    private double cellRadius;
    private SystemLayout systemLayout;
    private boolean measureInterferenceFromEntireCluster;
    private boolean generateWrapAround;
    private int indexOfReferenceCell;
    private int referenceSector;

    public CellularLayoutImpl() {
        tierSetup = TierSetup.TwoTiers;
        sectorSetup = SectorSetup.TriSector3GPP2;
        cellRadius = 5;
        systemLayout = SystemLayout.CenterOfInfiniteNetwork;
        measureInterferenceFromEntireCluster = false;
        generateWrapAround = true;
        indexOfReferenceCell = 0;
    }


    @Override
    public SectorSetup getSectorSetup() {
        return sectorSetup;
    }

    public void setSectorSetup( SectorSetup sectorSetup ) {
        this.sectorSetup = sectorSetup;
    }

    @Override
    public TierSetup getTierSetup() {
        return tierSetup;
    }

    public void setTierSetup( TierSetup tierSetup ) {
        this.tierSetup = tierSetup;
    }

    @Override
    public double getCellRadius() {
        return cellRadius;
    }

    public void setCellRadius( double cellRadius ) {
        this.cellRadius = cellRadius;
    }

    @Override
    public SystemLayout getSystemLayout() {
        return systemLayout;
    }

    public void setSystemLayout( SystemLayout systemLayout ) {
        this.systemLayout = systemLayout;
    }

    @Override
    public boolean measureInterferenceFromEntireCluster() {
        return measureInterferenceFromEntireCluster;
    }

    public void setMeasureInterferenceFromEntireCluster( boolean measureInterferenceFromEntireCluster ) {
        this.measureInterferenceFromEntireCluster = measureInterferenceFromEntireCluster;
    }

    @Override
    public boolean generateWrapAround() {
        return generateWrapAround;
    }

    public void setGenerateWrapAround( boolean generateWrapAround ) {
        this.generateWrapAround = generateWrapAround;
    }

    @Override
    public int getIndexOfReferenceCell() {
        return indexOfReferenceCell;
    }

    public void setIndexOfReferenceCell( int indexOfReferenceCell ) {
        this.indexOfReferenceCell = indexOfReferenceCell;
    }

    @Override
    public int getReferenceSector() {
        return referenceSector;
    }

    public void setReferenceSector( int referenceSector ) {
        this.referenceSector = referenceSector;
    }
}
