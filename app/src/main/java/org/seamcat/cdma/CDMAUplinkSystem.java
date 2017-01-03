package org.seamcat.cdma;

import org.apache.log4j.Logger;
import org.seamcat.dmasystems.AbstractDmaBaseStation;
import org.seamcat.dmasystems.AbstractDmaLink;
import org.seamcat.dmasystems.AbstractDmaSystem;
import org.seamcat.dmasystems.UserShouldBeIgnoredException;
import org.seamcat.eventbus.EventBusFactory;
import org.seamcat.events.CapacityEndingTest;
import org.seamcat.events.CapacityEndingTrial;
import org.seamcat.events.CapacityStartingTest;
import org.seamcat.model.mathematics.Mathematics;
import org.seamcat.model.types.result.BarChartResultType;
import org.seamcat.scenario.CDMAUpLinkImpl;
import org.seamcat.simulation.IgnoreSnapshot;

import java.util.Collections;

import static org.seamcat.simulation.cellular.CellularVictimSystemSimulation.*;

/** method that performs major algorithm like:
 * <ol>
 *     <li>balance Interefered system, i.e. regulates the number of UE in the network</li>
 * </ol>
 */
public class CDMAUplinkSystem extends CDMASystem {

    private static final Logger LOG = Logger.getLogger(CDMAUplinkSystem.class);

    private final int maximumPeakCount = 150;

    public CDMAUplinkSystem(AbstractDmaSystem<?> sys) {
        super(sys);
        init();
        if ( sys instanceof CDMAUplinkSystem ) {
            CDMAUpLinkImpl other = sys.getSystemSettings().getCDMASettings().getUpLinkSettings();
            getSystemSettings().getCDMASettings().getUpLinkSettings().setTargetNetworkNoiseRise(other.getTargetNetworkNoiseRise());
            getSystemSettings().getCDMASettings().getUpLinkSettings().setCellNoiseRise(other.isCellNoiseRise());
            getSystemSettings().getCDMASettings().getUpLinkSettings().setTargetCellNoiseRise(other.getTargetCellNoiseRise());
            getSystemSettings().getCDMASettings().getUpLinkSettings().setMSMaximumTransmitPower(other.getMSMaximumTransmitPower());
            getSystemSettings().getCDMASettings().getUpLinkSettings().setMSPowerControlRange(other.getMSPowerControlRange());
            getSystemSettings().getCDMASettings().getUpLinkSettings().setMSConvergencePrecision(other.getMSConvergencePrecision());
        }
    }

    private void init() {
        getSystemSettings().setUpLink( true );
        getSystemSettings().getCDMASettings().setUpLinkSettings(new CDMAUpLinkImpl());
    }

    private void addAllUsersToSystem(int usersPerCell) {
        for (int i = 0, stop = usersPerCell * getNumberOfBaseStations(); i < stop; i++) {
            CdmaUserTerminal user = generateInitializedMobile();
            if (user != null) {
                user.connectToBaseStationsUplink();
                activeUsers.add(user);
            }
        }// for loop (add users)
    }

    /**
     * Method that regulates the number of UE in the network (i.e. add and dropped) in a UL CDMA system in the presence <br>
     *     of external interference
     *
     * <ol>
     *     <li>The users are sorted so that the one with lowest power is on a top of the list, and the bottom list users<br>
     *         have the highest power.</li>
     *     <li>Loop to identify the affected cells based on relativeCellNoiseRise[i]</li>
     *     <li>Sort the list selectedCell so that the cell with the highest relative noise rise is the first element</li>
     *     <li>Calculate the average noise rise (with external interference) in the whole network</li>
     * </ol>
     * <p></p>
     * the algorithn is adjusted to either consider the whole network or to consider cell by cell approach
     * <p></p>
     * <b>Cell per cell approach</b>
     * <ol>
     *     <li>Start loop over the sorted list selectedCells (starting from the first strongest noise rise cell)</li>
     *     <li>Extract the active users of selectedCell[i] from activeUsers into a list selectedCellActiveUsers (one <br>
     *         list per cell)</li>
     *     <li>Calculate the current new noise rise for selectedCell[i] (i.e. currentCellNoiseRise[i])</li>
     *     <li>Start removing (i.e. dropping) the active users from selectedCellActiveUsers (by default the ActiveUsers <br>
     *         list is reduced). The UE with strongest Tx power is removed first.</li>
     *     <li>Proceed with internalPowerBalance() over the remaining users for the whole</li>
     * </ol>
     * <p></p>
     * <b>Whole network approach</b>
     * <ol>
     *     <li> apply Last In First Out (LIFO) method</li>
     *     <li>Drop last added user which is the user with highest power, i.e. remove the users from the bottom of the list</li>
     *     <li>Stop loop if no more users in system</li>
     * </ol>
     * <p></p>
     * After External balancing, some UE can be dropped if Ec/IoR requirement is not met
     */
    @Override
    public void balanceInterferedSystem() {
        boolean noiseRiseAboveLimit = true;
        boolean isSingleCellDone = true;

        CdmaUserTerminal user;

        double initialAverageNetworkNoiseRiseWithoutExternalInterference = calculateAverageNoiseRiseWithoutExternalInterference_dB();
        getEventResult().addValue(avgNetworkNoiseRiseInitialNoExt, initialAverageNetworkNoiseRiseWithoutExternalInterference );
        double initial = calculateAverageNoiseRise_dB();
        getEventResult().addValue(avgNetworkNoiseRiseInitial, initial );
        getEventResult().addValue(avgNetworkNoiseRise, initial );

        boolean cellNoiseRiseSelection = getSystemSettings().getCDMASettings().getUpLinkSettings().isCellNoiseRise();

        // The users are sorted so that the one with lowest power is on a top of the list, and the bottom list users have the highest power.
        Collections.sort(activeUsers, AbstractDmaBaseStation.lowestTransmittingUser);

        double currentAverageNoiseRise;
        double currentCellNoiseRise;

        // step 6 Loop to identify the affected cells based on relativeCellNoiseRise[i]
        createAffectedCellList();
        getEventResult().addValue(numberOfAffectedCells, selectedCell.size());
        // step 8 Sort the list selectedCell so that the cell with the highest relative noise rise is the first element
        Collections.sort(selectedCell, AbstractDmaBaseStation.highestNoiseRise);

        if (!selectedCell.isEmpty() && cellNoiseRiseSelection){
            selectedCell.get(0).setWorstCell(true);
        }

        while (noiseRiseAboveLimit) {
            int loopCount = internalPowerBalance();

            // Calculate the average noise rise (with external interference) in the whole network
            currentAverageNoiseRise = calculateAverageNoiseRise_dB();

            noiseRiseAboveLimit = currentAverageNoiseRise > initialAverageNetworkNoiseRiseWithoutExternalInterference ;

            if (loopCount >= maximumPeakCount) {
                noiseRiseAboveLimit = true;
            }

            if ( noiseRiseAboveLimit ) {
                int absRiseAboveLimit = (int) Math.max(1, Math.ceil(currentAverageNoiseRise - initialAverageNetworkNoiseRiseWithoutExternalInterference));

                if( cellNoiseRiseSelection ){
                    if (!selectedCell.isEmpty()){ // step 7 selection of algorithm
                        // step 9 Start loop over the sorted list selectedCells (starting from the first strongest noise rise cell)
                        for(int z = 0 ; z < getEventResult().getValue(numberOfAffectedCells); z ++){
                            // 9.1	Extract the active users of selectedCell[i] from activeUsers into a list selectedCellActiveUsers (one list per cell)
                            extractActiveUsersToSelectedCellList(z);

                            double cellNoiseRiseInitial = selectedCell.get(z).getCellNoiseRiseInitial();

                            isSingleCellDone = false;
                            while(!isSingleCellDone){
                                // 9.2	Calculate the current new noise rise for selectedCell[i] (i.e. currentCellNoiseRise[i]).
                                currentCellNoiseRise = selectedCell.get(z).calculateNoiseRiseOverThermalNoise_dB();
                                int absCellNoiseRiseAboveLimit = (int) Math.max(1, Math.ceil(currentCellNoiseRise - cellNoiseRiseInitial));

                                // 9.3	Start removing (i.e. dropping) the active users from selectedCellActiveUsers (by default the ActiveUsers list is reduced). The UE with strongest Tx power is removed first.
                                for (int i = 0; i < absCellNoiseRiseAboveLimit && selectedCellActiveUsers.size() > 0; i++) {
                                    user = selectedCellActiveUsers.get(selectedCellActiveUsers.size()-1);
                                    selectedCellActiveUsers.remove(user);
                                    dropActiveUser(user);
                                    user.setDropReason("Highest Transmitting UE from a single cell");
                                }
                                // 9.4	Proceed with internalPowerBalance() over the remaining users for the whole
                                internalPowerBalance();

                                // step 9.5
                                currentAverageNoiseRise = calculateAverageNoiseRise_dB();
                                noiseRiseAboveLimit = currentAverageNoiseRise > initialAverageNetworkNoiseRiseWithoutExternalInterference;

                                currentCellNoiseRise = selectedCell.get(z).calculateNoiseRiseOverThermalNoise_dB();
                                boolean cellNoiseRiseAboveInitial = currentCellNoiseRise > cellNoiseRiseInitial;

                                if( !cellNoiseRiseAboveInitial || selectedCellActiveUsers.size() == 0){
                                    isSingleCellDone = true;

                                    if(!noiseRiseAboveLimit){
                                        z = selectedCell.size(); // to get out of the for loop
                                        noiseRiseAboveLimit = false; // to get out of the main while loop
                                    }
                                }
                            }
                        }
                        noiseRiseAboveLimit = false;
                        getEventResult().addValue( avgNetworkNoiseRise, calculateAverageNoiseRise_dB());
                    }else{
                        noiseRiseAboveLimit = false;
                        getEventResult().addValue( avgNetworkNoiseRise, calculateAverageNoiseRise_dB());
                    }
                }else{
                    // Last In First Out (LIFO) method:
                    for (int i = 0; i < absRiseAboveLimit && activeUsers.size() > 0; i++) {
                        // Drop last added user which is the user with highest power, i.e. remove the users from the bottom of the list
                        user = activeUsers.get(activeUsers.size() - 1);
                        user.setDroppedAsHighest(true);
                        dropActiveUser(user);
                    }
                    // Stop loop if no more users in system
                    if (activeUsers.size() == 0) {
                        noiseRiseAboveLimit = false;
                        getEventResult().addValue(avgNetworkNoiseRise, calculateAverageNoiseRise_dB());
                    }
                }
            }
        }

        for (int i = 0; i < activeUsers.size(); i++) {
            user = activeUsers.get(i);
            double ci = user.calculateAchievedCI();
            double reqci = user.getLinkLevelData().getEbNo();

            double difference = reqci - ci;
            if (difference > getSystemSettings().getCDMASettings().getCallDropThreshold()) {
                dropActiveUser(user);
                user.setDropReason("Ec/IoR requirement deos not meet - after External balancing");
                i--;
            }
        }
    }

    /**
     * Method that regulates the number of UE in the network (i.e. add and dropped) in a UL CDMA system without external <br>
     *     interference
     *
     *     <ol>
     *         <li>optimisation of the algorithm by allowing a bunch of users to be added or removed</li>
     *         <li>balance the number of active users so that the current initial average network noise rise without <br>
     *             external interference is compared to a target network noise rise</li>
     *         <li>fine tuning remove active users or add active users. This process is done using a bunch of users to <br>
     *             speed up computation time)</li>
     *             <ul>remove users: Target noise rise too high while initially balancing the power</ul>
     *             <ul>add users: note that while adding users it may happened that they are unable to connect when <br>
     *                 adding a UE while initially balancing the power</ul>
     *     </ol>
     */

     @Override
    public void balancePower() {
        calculateProcessingGain();

        //optimisation of the algorithm by allowing a bunch of users to be added or removed
        int blockNumberUsers = getAllBaseStations().size() == 57 ? 10 : 1;

        int loopCount = internalPowerBalance();
        boolean ignoreSnapshot = loopCount >= maximumPeakCount;
        checkLoop(loopCount);

        CdmaUserTerminal user;
        // balance the number of active users
        boolean initialNoiseRiseAboveLimit = true;
        boolean tempInitialNoiseRiseAboveLimit = true;
        double currentInitialAverageNetworkNoiseRiseWithoutExternalInterference = calculateAverageNoiseRiseWithoutExternalInterference_dB();
        while (initialNoiseRiseAboveLimit){

            if (currentInitialAverageNetworkNoiseRiseWithoutExternalInterference > getSystemSettings().getCDMASettings().getUpLinkSettings().getTargetNetworkNoiseRise()){//fine tuning remove active users
                for (int i=0; i<blockNumberUsers; i++ ){// process users by bunch to speed up computation time
                    user = activeUsers.get(activeUsers.size() - 1);
                    dropActiveUser(user);
                    user.setDropReason("Target noise rise too high while initially balancing the power");
                }
                loopCount = internalPowerBalance();
                ignoreSnapshot = loopCount >= maximumPeakCount;
                checkLoop(loopCount);

                currentInitialAverageNetworkNoiseRiseWithoutExternalInterference = calculateAverageNoiseRiseWithoutExternalInterference_dB();
                initialNoiseRiseAboveLimit = currentInitialAverageNetworkNoiseRiseWithoutExternalInterference >= getSystemSettings().getCDMASettings().getUpLinkSettings().getTargetNetworkNoiseRise();

            }else{//fine tuning add active users
                for (int i=0; i<blockNumberUsers; i++ ){ // process users by bunch of 10 to speed up computation time
                    user = generateInitializedMobile();
                    if (user == null) {
                        continue;
                    }
                    if (user.connect()) {
                        user.setAllowedToConnect(true);
                        activeUsers.add(user);
                    } else {
                        dropActiveUser(user);
                        user.setDropReason("Unable to connect when adding a UE while initially balancing the power");
                    }

                }
                loopCount = internalPowerBalance();
                ignoreSnapshot = loopCount >= maximumPeakCount;

                checkLoop(loopCount);

                currentInitialAverageNetworkNoiseRiseWithoutExternalInterference = calculateAverageNoiseRiseWithoutExternalInterference_dB();
                // new code in highlighted yellow to optimize the number of UEs just below the target noise rise
                tempInitialNoiseRiseAboveLimit = currentInitialAverageNetworkNoiseRiseWithoutExternalInterference <= getSystemSettings().getCDMASettings().getUpLinkSettings().getTargetNetworkNoiseRise();
                while(!tempInitialNoiseRiseAboveLimit){//the target has been reached and is over, so trying to get just below
                    user = activeUsers.get(activeUsers.size() - 1);
                    dropActiveUser(user);
                    user.setDropReason("Target noise rise too high while initially balancing the power");

                    loopCount = internalPowerBalance();
                    ignoreSnapshot = loopCount >= maximumPeakCount;

                    checkLoop(loopCount);

                    currentInitialAverageNetworkNoiseRiseWithoutExternalInterference = calculateAverageNoiseRiseWithoutExternalInterference_dB();
                    tempInitialNoiseRiseAboveLimit=currentInitialAverageNetworkNoiseRiseWithoutExternalInterference<=getSystemSettings().getCDMASettings().getUpLinkSettings().getTargetNetworkNoiseRise();
                    if (tempInitialNoiseRiseAboveLimit){
                        initialNoiseRiseAboveLimit = false;
                    }
                }

            }
        }

        if ( ignoreSnapshot ) {
            throw new IgnoreSnapshot();
        }
        calculateAverageNoiseRise_Linear();
    }

    /**
     * this method converts the calculated AverageNoiseRise_Linear() into dB
     * @return
     */
    public double calculateAverageNoiseRise_dB() {
        return Mathematics.linear2dB(calculateAverageNoiseRise_Linear());
    }

    /**
     * This method calculates the average noise rise for the system in its
     * current state. Note that this method will recalculate the noise rise for
     * all cells in the system.
     *
     * @return The lineary average noise rise of all BS in the system
     */
    public double calculateAverageNoiseRise_Linear() {
        int cellCounter = 0;
        double noiseRise = 0;

        int cellsPerSite = cellsPerSite();
        for (int j = 0, bStop = cells.length; j < bStop; j++) {
            for (int k = 0; k < cellsPerSite; k++) {
                CdmaBaseStation cell = (CdmaBaseStation) cells[j][k];
                noiseRise += cell.calculateNoiseRiseOverThermalNoise_LinearyFactor();
                cellCounter++;
            }
        }
        return noiseRise / cellCounter;
    }

    public double calculateAverageNoiseRiseWithoutExternalInterference_dB() {
        return Mathematics.linear2dB(calculateAverageNoiseRiseWithoutExternalInterference_Linear());
    }

    public double calculateAverageNoiseRiseWithoutExternalInterference_Linear() {
        int cellCounter = 0;
        double noiseRise = 0;

        int cellsPerSite = cellsPerSite();
        for (int j = 0, bStop = cells.length; j < bStop; j++) {
            for (int k = 0; k < cellsPerSite; k++) {
                CdmaBaseStation cell = (CdmaBaseStation) cells[j][k];
                noiseRise += cell.calculateNoiseRiseOverThermalNoiseWithoutExternal_LinearyFactor();
                cellCounter++;
            }
        }
        return noiseRise / cellCounter;
    }

    /**
     * This method calculates the noise rise for the single cell with the highest noise rise.
     *
     * @return The noise rise for the single cell with the highest noise rise
     */
    public double calculateNoiseRiseForSingleCell_dB(int cellSelectedToHaveTheHighestNoiseRise) {
        double maxNoiseRise = 0;

        int cellsPerSite = cellsPerSite();
        for (int j = 0, bStop = cells.length; j < bStop; j++) {
            for (int k = 0; k < cellsPerSite; k++) {
                if (cells[j][k].getCellid() == cellSelectedToHaveTheHighestNoiseRise){
                    maxNoiseRise = cells[j][k].calculateNoiseRiseOverThermalNoise_dB();
                }
            }
        }
        return maxNoiseRise;

    }

    /**
     * Method that computes the optimum number of UEs during the pre-simulation phase in CDMA UL
     *
     * @param capFinding
     * @param search
     * @param context
     * @return
     * @throws InterruptedException
     */
    @Override
    protected NonInterferedCapacitySearch findNonInterferedCapacityInternal(BarChartResultType capFinding, NonInterferedCapacitySearch search, Object context)
    throws InterruptedException
    {
        int usersPerCell = search.getCapacity();
        int deltaN = search.getDeltaUsers();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Testing non interfered capacity. [N = " + usersPerCell
                    + ", deltaN = " + deltaN + "]");
        }
        int numberOfTrials = getSystemSettings().getCDMASettings().getNumberOfTrials();
        EventBusFactory.getEventBus().publish(new CapacityStartingTest(context, usersPerCell, numberOfTrials));
        try {
            // Prepare system
            calculateProcessingGain();
            // Start trials
            double networkNoiseRiseSum = 0;
            for (int i = 0; i < numberOfTrials; i++) {
                double currentEndResult = 10 * Math.log10((networkNoiseRiseSum / numberOfTrials));
                if (currentEndResult > maxTargetNoiseRise) {
                    // Even if rest of trials have 0.0 noise rise we will have
                    // mena more than target -> no need to continue:

                    double linearyAverage = networkNoiseRiseSum / i;
                    for (; i < numberOfTrials; i++) {
                        // Add average as result for rest of trials:
                        networkNoiseRiseSum += linearyAverage;
                    }
                    continue;
                }

                // Start trial by resetting system
                resetSystem();
                // Generate users
                addAllUsersToSystem(usersPerCell);

                // Start internal power balance loop
                checkLoop(internalPowerBalance());

                // Calculate linear average of noiserise across cluster
                double nr = calculateAverageNoiseRise_Linear();

                // Add average noiserise to the sum of noiserise for this set of
                // trials
                networkNoiseRiseSum += nr;

                double temp_meanNoiseRise = Mathematics.linear2dB(networkNoiseRiseSum / (i + 1));

                EventBusFactory.getEventBus().publish( new CapacityEndingTrial(context, temp_meanNoiseRise,
                        (nr <= getSystemSettings().getCDMASettings().getUpLinkSettings().getTargetNetworkNoiseRise()), i));
            }// Outer for loop (numberOfTrials)

            // We have completed the specified number of trials and evaluates
            // results:

            // Mean Noise Rise of number of trials is calculated as specified by
            // Qualcomm [STG(05)xxx 12. Dec. 2005 - section 2.1]
            meanNoiseRiseOverTrials = 10 * Math
                    .log10((networkNoiseRiseSum / numberOfTrials));

            if (LOG.isDebugEnabled()) {
                LOG.debug("Mean noise rise was: " + meanNoiseRiseOverTrials + " dB");
            }
            addPoint(capFinding, usersPerCell, meanNoiseRiseOverTrials);
            EventBusFactory.getEventBus().publish( new CapacityEndingTest(context, usersPerCell, meanNoiseRiseOverTrials ));
            if (meanNoiseRiseOverTrials < minTargetNoiseRise) {
                if (fineTuning) {
                    if (deltaN == 1 && finalFineTuning) {
                        return new NonInterferedCapacitySearch(usersPerCell);
                    } else {
                        deltaN = (int) Math.ceil(deltaN / 2.0);
                    }
                }// fine tuning loop
                usersPerCell += deltaN;
                return new NonInterferedCapacitySearch(usersPerCell, deltaN );
            } else if (meanNoiseRiseOverTrials > maxTargetNoiseRise) {
                fineTuning = true;

                if (deltaN == 1) {
                    finalFineTuning = true;
                } else {
                    deltaN = (int) Math.ceil(deltaN / 2.0);
                }
                usersPerCell -= deltaN;

                if (usersPerCell > 0) {
                    return new NonInterferedCapacitySearch(usersPerCell, deltaN );
                } else {
                    return new NonInterferedCapacitySearch(0);
                }
            } else {
                return new NonInterferedCapacitySearch(usersPerCell);
            }
        } catch (Exception ex) {
            if ((ex.getCause() instanceof InterruptedException)) {
                throw (InterruptedException) ex.getCause();
            }
            LOG.error("An Error occured", ex);
            return new NonInterferedCapacitySearch(-1);
        }
    }

    private void checkLoop(int loopCount) {
        Double value = getEventResult().getValue(highestPCLoopCount);
        if ( value == null ) {
            value = 0.0;
        }
        if (loopCount > value) {
            getEventResult().addValue(highestPCLoopCount, loopCount);
        }
    }

    /**
     * Loop to identify the affected cells based on relativeCellNoiseRise[i]
     */
    protected void createAffectedCellList(){
        int cellsPerSite = cellsPerSite();
        for (int j = 0, bStop = cells.length; j < bStop; j++) {
            for (int k = 0; k < cellsPerSite; k++) {
                cells[j][k].setCellNoiseRiseInitial(cells[j][k].calculateNoiseRiseOverThermalNoiseWithoutExternal_dB());
                double cellNoiseRiseInterferer = cells[j][k].calculateNoiseRiseOverThermalNoise_dB();

                cells[j][k].setRelativeCellNoiseRise(cellNoiseRiseInterferer - cells[j][k].getCellNoiseRiseInitial());

                if ( cells[j][k].getRelativeCellNoiseRise()> getSystemSettings().getCDMASettings().getUpLinkSettings().getTargetCellNoiseRise()){
                    selectedCell.add(cells[j][k]);
                }
            }
        }
    }

    /**
     * method that extract the active users from the selected cells
     *
     * @param selectedCellIndex
     */
    protected void extractActiveUsersToSelectedCellList(int selectedCellIndex) {
        int cellID;
        int stop = activeUsers.size();
        for (int i = 0; i < stop ; i++){
            cellID = activeUsers.get(i).getActiveList().get(0).getBaseStation().getCellid();
            if (cellID == selectedCell.get(selectedCellIndex).getCellid()){
                selectedCellActiveUsers.add(activeUsers.get(i));

            }
        }
    }

    /**
     * set the Tx power of the UE (CDMA UL)
     *
     * @return
     */
    @Override
    protected int internalPowerBalance() {
        boolean powerConverged = false;
        int peakCount = 0;
        while (!powerConverged) {
            powerConverged = true;
            peakCount++;

            // Calculate CI - might change the active link if user is insofthandover
            for (CdmaUserTerminal u : activeUsers) {
                u.calculateAchievedCI();
            }

            for (CdmaUserTerminal u : activeUsers) {
                double cToI_last_loop = u.getOldAchievedCI();
                double cToI_loop = u.getAchievedCI();
                double Sir_target = u.getLinkLevelData().getEbNo();
                double tx_required_last_loop = Mathematics.fromWatt2dBm(u.getCurrentTransmitPower());
                double tx_required_loop = tx_required_last_loop + Sir_target - cToI_loop;

                double power = Math.min(Math.max(tx_required_loop, u.getMinTxPower()), u.getMaxTxPower());

                u.setCurrentTransmitPower_dBm(power);
                u.resetSummationAffectedBaseStations();
                if (Math.abs(cToI_loop - cToI_last_loop) > getSystemSettings().getCDMASettings().getUpLinkSettings().getMSConvergencePrecision()) {
                    powerConverged = false;
                }

            }
            if (peakCount > maximumPeakCount) {
                powerConverged = true;
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Terminating CDMA Power Control loop after " + maximumPeakCount + " cycles");
                }
            }
        }

        // Update BaseStation values:
        for (AbstractDmaBaseStation[] cell : cells) {
            for (AbstractDmaBaseStation aCell : cell) {
                aCell.calculateTotalInterference_dBm(null);
            }
        }

        return peakCount;
    }

    @Override
    protected void performSystemSpecificInitialization(CdmaUserTerminal user) throws UserShouldBeIgnoredException {
        boolean voice = trialVoiceActivity();
        if (!voice) {
            inactiveUsers.add(user);
            for (AbstractDmaLink link : user.getActiveList()) {
                CDMAUplink l = (CDMAUplink) link;
                l.getBaseStation().addVoiceInActiveUser(l);
            }

            throw new UserShouldBeIgnoredException();
        }

        // Randomly select and set multipath channel (1 or 2) [using the
        // unrelated VOICE_ACTIVITY_RANDOM]
        user.setMultiPathChannel(1 + (int) Math.round(random.trial()));
        user.findLinkLevelDataPoint(getLinkLevelData());

        if (!user.linkLevelDataPointFound()) {
            noLinkLevelFoundUsers.add(user);
            useridcount--;
            throw new UserShouldBeIgnoredException();
        }
    }
}
