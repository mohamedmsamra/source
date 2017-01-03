package org.seamcat.presentation;

import org.seamcat.exception.SimulationInvalidException;
import org.seamcat.model.factory.Model;
import org.seamcat.model.types.LibraryItem;
import org.seamcat.presentation.resources.ImageLoader;
import org.seamcat.tabulardataio.FileFormat;
import org.seamcat.tabulardataio.TabularDataFactory;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.ResourceBundle;

public class DialogHelper {
    private static final ResourceBundle STRINGLIST = ResourceBundle.getBundle("stringlist", java.util.Locale.ENGLISH);

    public static boolean restoreLibrary( JFrame owner ) {
        return JOptionPane.showConfirmDialog(owner, "Are you sure you want to restore original library?",
                "Restore Library",
                JOptionPane.OK_CANCEL_OPTION ) == JOptionPane.OK_OPTION;
    }

    public static int closeDirtyBatch(String name) {
        return JOptionPane.showConfirmDialog(
                MainWindow.getInstance(),
                String.format(STRINGLIST.getString("SAVE_BATCH_ON_EXIT"), name),
                STRINGLIST.getString("SAVE_WORKSPACE_ON_EXIT_TITLE"),
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.INFORMATION_MESSAGE);
    }

    public static int closeNoResults(String name) {
        return JOptionPane.showConfirmDialog(
                MainWindow.getInstance(),
                String.format(STRINGLIST.getString("SAVE_WORKSPACE_ON_EXIT"), name),
                STRINGLIST.getString("SAVE_WORKSPACE_ON_EXIT_TITLE"),
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.INFORMATION_MESSAGE);
    }

    public static int saveResultsWhenClosing(String name, long estimatedEventSize) {
        String megabyteSize = megabyteSize(estimatedEventSize);
        return JOptionPane.showOptionDialog(
                MainWindow.getInstance(),
                String.format(STRINGLIST.getString("SAVE_WORKSPACE_ON_EXIT_WITH_RESULTS"), name, megabyteSize),
                STRINGLIST.getString("SAVE_WORKSPACE_ON_EXIT_TITLE"),
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                new String[]{
                        "Yes",
                        "No",
                        STRINGLIST.getString("CLOSE_WORKSPACE_CANCEL")
                },
                STRINGLIST.getString("SAVE_WORKSPACE_WITHOUT_RESULTS"));
    }

    public static int saveBatchWhenClosing( JDialog owner ) {
        return JOptionPane.showOptionDialog(owner,
                STRINGLIST.getString("SAVE_BATCH_ON_CLOSE"),
                STRINGLIST.getString("SAVE_BATCH_TITLE"),
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                new String[] {"Yes","No","Cancel"},
                "Yes"
        );
    }

    public static int saveModified() {
        return JOptionPane.showOptionDialog(
                MainWindow.getInstance(),
                STRINGLIST.getString("SAVE_OR_SAVE_WITH_MODIFIED_RESULTS"),
                "Save workspace",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                new String[]{
                        STRINGLIST.getString("SAVE_WORKSPACE_ORIGINAL"),
                        STRINGLIST.getString("SAVE_WORKSPACE_NEW_SETTING"),
                        STRINGLIST.getString("SAVE_WORKSPACE_CANCEL")
                },
                STRINGLIST.getString("SAVE_WORKSPACE_WITHOUT_RESULTS"));
    }

    public static int closeModified() {
        return JOptionPane.showOptionDialog(
                MainWindow.getInstance(),
                STRINGLIST.getString("SAVE_OR_SAVE_WITH_MODIFIED_RESULTS"),
                STRINGLIST.getString("SAVE_WORKSPACE_ON_EXIT_TITLE"),
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                new String[]{
                        STRINGLIST.getString("SAVE_WORKSPACE_ORIGINAL"),
                        STRINGLIST.getString("SAVE_WORKSPACE_NEW_SETTING"),
                        "No",
                        STRINGLIST.getString("CLOSE_WORKSPACE_CANCEL")
                },
                STRINGLIST.getString("SAVE_WORKSPACE_WITHOUT_RESULTS"));
    }

    public static void simulationError(SimulationInvalidException e) {
        JOptionPane.showMessageDialog(MainWindow.getInstance(), e.getDescription()  + "\n"+e.getOrigin().getMessage(),
                "Simulation Aborted", JOptionPane.ERROR_MESSAGE);

    }


    public static int closeCleanAndUnmodifiedBig(String name) {
        return JOptionPane.showOptionDialog(
                MainWindow.getInstance(),
                String.format(STRINGLIST.getString("SAVE_WORKSPACE_ON_EXIT_WITH_RESULTS_TOO_LARGE"), name),
                STRINGLIST.getString("SAVE_WORKSPACE_ON_EXIT_TITLE"),
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                new String[]{
                        STRINGLIST.getString("SAVE_WORKSPACE_WITHOUT_RESULTS"),
                        "No",
                        STRINGLIST.getString("CLOSE_WORKSPACE_CANCEL")
                },
                STRINGLIST.getString("SAVE_WORKSPACE_WITHOUT_RESULTS"));
    }

    public static int saveModifiedBig(String name) {
        return JOptionPane.showOptionDialog(
                MainWindow.getInstance(),
                String.format(STRINGLIST.getString("SAVE_WORKSPACE_LARGE_RESULTS_MODIFIED"), name),
                "Save workspace",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                new String[]{
                        STRINGLIST.getString("SAVE_ORIGINAL_WITHOUT_RESULTS"),
                        STRINGLIST.getString("SAVE_WORKSPACE_NEW_SETTING"),
                        STRINGLIST.getString("SAVE_WORKSPACE_CANCEL")
                },
                STRINGLIST.getString("SAVE_ORIGINAL_WITHOUT_RESULTS"));
    }

    public static int closeModifiedBig(String name) {
        return JOptionPane.showOptionDialog(
                MainWindow.getInstance(),
                String.format(STRINGLIST.getString("SAVE_WORKSPACE_LARGE_RESULTS_MODIFIED"), name),
                STRINGLIST.getString("SAVE_WORKSPACE_ON_EXIT_TITLE"),
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                new String[]{
                        STRINGLIST.getString("SAVE_ORIGINAL_WITHOUT_RESULTS"),
                        STRINGLIST.getString("SAVE_WORKSPACE_NEW_SETTING"),
                        "No",
                        STRINGLIST.getString("CLOSE_WORKSPACE_CANCEL")
                },
                STRINGLIST.getString("SAVE_ORIGINAL_WITHOUT_RESULTS"));
    }

    private static String megabyteSize(long sizeInBytes) {
        double megabyteSize = sizeInBytes/1024.0/1024.0;
        NumberFormat formatter = new DecimalFormat("#0.00");
        return formatter.format(megabyteSize);
    }

    public static int saveWorkspaceResults( String name, long estimatedEventSize ) {
        return JOptionPane.showOptionDialog(
                MainWindow.getInstance(),
                String.format( STRINGLIST.getString( "SAVE_WORKSPACE_WITH_RESULTS_TITLE" ), name, megabyteSize(estimatedEventSize)),
                STRINGLIST.getString("SAVE_WORKSPACE_WITH_RESULTS_WINDOW_TITLE"),
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                new String[] {
                        STRINGLIST.getString( "SAVE_WORKSPACE_WITH_RESULTS" ),
                        STRINGLIST.getString("SAVE_WORKSPACE_WITHOUT_RESULTS"),
                        STRINGLIST.getString("SAVE_WORKSPACE_FILE_EXISTS_CANCEL")
                },
                STRINGLIST.getString( "SAVE_WORKSPACE_WITH_RESULTS" ));
    }

    public static int saveCleanAndUnmodifiedBig(String name) {
        return JOptionPane.showOptionDialog(
                MainWindow.getInstance(),
                String.format( STRINGLIST.getString( "SAVE_WORKSPACE_WITH_RESULTS_TOO_LARGE_TITLE" ), name ),
                STRINGLIST.getString("SAVE_WORKSPACE_WITH_RESULTS_TOO_LARGE_WINDOW_TITLE"),
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                new String[] {
                        STRINGLIST.getString("SAVE_WORKSPACE_WITHOUT_RESULTS"),
                        STRINGLIST.getString("SAVE_WORKSPACE_CANCEL")
                },
                STRINGLIST.getString( "SAVE_WORKSPACE_WITHOUT_RESULTS" ));
    }

    public static int consistencyError( Component component, StringBuffer messages ) {
        JLabel label = new JLabel(
                STRINGLIST.getString("CONSISTENCY_ERROR_PRE")
                        + messages.toString()
                        + STRINGLIST.getString("CONSISTENCY_ERROR_POST"));
        JScrollPane scrollPane = new JScrollPane(label);
        scrollPane.setPreferredSize( new Dimension( 800, 500 ) );
        return JOptionPane.showOptionDialog(component, scrollPane, STRINGLIST
                        .getString("CONSISTENCY_ERROR_TITLE"),
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null,
                new String[] {
                        STRINGLIST.getString("CONSISTENCY_YES_OPTION"),
                        STRINGLIST.getString("CONSISTENCY_NO_OPTION") }, null);
    }

    public static void about() {
        String title = "About SEAMCAT";
        String version = STRINGLIST.getString("APPLICATION_TITLE");
        String message = "<html>" +
                "<br><center><strong>"+version+"</strong></center><br>"+
                "<p>The SEAMCAT project is an ongoing <a href=\"http://www.cept.org/ecc\">ECC</a> activity within working group spectrum <br>" +
                "engineering (<a href=\"http://www.cept.org/ecc/groups/ecc/wg-se\">WGSE</a>).</p>" +
                "<p>The software is maintained by <a href=\"http://www.cept.org/eco\">ECO</a> and distributed free-of-charge.</p>" +
                "<p>SEAMCAT is a software tool based on the Monte-Carlo simulation method, which is <br>" +
                "developed within the frame of European Conference of Postal <br>" +
                "and Telecommunication administrations (<a href=\"http://www.cept.org/cept\">CEPT</a>). This tool permits statistical modelling <br>" +
                "of different radio interference scenarios for performing sharing and compatibility <br>" +
                "studies between radiocommunications systems (short range devices, GSM, UMTS, <br>" +
                "LTE, etc ) in the same or adjacent frequency bands. <br>" +
                "</p></html>";
        JPanel about = new JPanel( new BorderLayout());
        about.setBorder( BorderFactory.createEtchedBorder() );
        ImageIcon icon = new ImageIcon( ImageLoader.class.getResource( "about_seamcat_logo.png" ) );
        JLabel image = new JLabel("", icon, JLabel.CENTER );

        about.add( image, BorderLayout.NORTH );

        HtmlPanel panel = new HtmlPanel( message, "5px 5px 5px 5px" );
        about.add(panel, BorderLayout.CENTER);
        JOptionPane.showMessageDialog(MainWindow.getInstance(), about, title, JOptionPane.PLAIN_MESSAGE);
    }

    public static void reportError() {
        String messageString =
                "<html>You can report bugs, propose enhancements or new <br>features by sending an e-mail to<br>" +
                        "<a href=\"mailto:seamcat@eco.cept.org\">seamcat@eco.cept.org</a> using this <a href=\"http://www.cept.org/Documents/stg/30128/stg-1x-xyz_stg-template\">template</a>.<br><br>"+
                        "For bug reports, please describe the error and attach <br>" +
                        "relevant information (e.g. workspace files, <br>" +
                        "screen shots, system log file etc). <br><br>" +
                        "The System Log file can be found in the folder: <br>" +
                        ""+Model.seamcatHome+"/logfiles/.<br><br>" +
                        "Thank you for your cooperation.<br><br>" +
                        "The SEAMCAT Team.</html>";

        JEditorPane messagePane = new HtmlPanel( messageString );
        JOptionPane.showMessageDialog(MainWindow.getInstance(), messagePane, "Reporting bugs", JOptionPane.PLAIN_MESSAGE, SeamcatIcons.getImageIcon("SEAMCAT_ICON_BUG_ERROR",SeamcatIcons.IMAGE_SIZE_TOOLBAR));
    }

    public static void consistencyOk(Component component) {
        JOptionPane.showMessageDialog(component, STRINGLIST.getString("CONSISTENCY_OK"),
                STRINGLIST.getString("CONSISTENCY_OK_TITLE"),JOptionPane.INFORMATION_MESSAGE);
    }

    public static void consistencyErrorPre(Component component, StringBuffer messages ) {
        JLabel label = new JLabel(
                STRINGLIST.getString("CONSISTENCY_ERROR_PRE")
                        + messages.toString());
        JScrollPane scrollPane = new JScrollPane(label);
        scrollPane.setPreferredSize( new Dimension( 800, 500 ) );
        JOptionPane.showMessageDialog(component,scrollPane,
                STRINGLIST.getString("CONSISTENCY_ERROR_TITLE"),
                JOptionPane.WARNING_MESSAGE);
    }

    public static String getSaveDoneString(File file) {
        StringBuilder str = new StringBuilder();
        String image = ImageLoader.class.getResource("save_successed_16x16.png").toString();
        str.append("<html>");
        str.append("<img src=\"").append( image ).append("\"> ");
        str.append( String.format(STRINGLIST.getString("SAVE_WORKSPACE_DONE"), file.getAbsolutePath()));
        str.append(" </html>");
        return str.toString();
    }

    public static String getSaveDoneWithResultsString(File file) {
        StringBuilder str = new StringBuilder();
        String image = ImageLoader.class.getResource("save_successed_16x16.png").toString();
        str.append("<html>");
        str.append("<img src=\"").append( image ).append("\"> ");
        str.append( String.format(STRINGLIST.getString("SAVE_WORKSPACE_WITH_RESULTS_DONE"), file.getAbsolutePath()));
        str.append(" </html>");
        return str.toString();
    }


    public static String saveFaildMessage( String path) {
        StringBuilder str = new StringBuilder();
        String image = ImageLoader.class.getResource("save_failed_16x16.png").toString();
        str.append("<html>");
        str.append("<img src=\"").append( image ).append("\"> ");
        str.append( String.format(STRINGLIST.getString("SAVE_WORKSPACE_FAILED"), path ));
        str.append(" </html>");
        return str.toString();
    }

    public static void workspaceTitleError() {
        JOptionPane.showMessageDialog(MainWindow.getInstance(), STRINGLIST
                        .getString("SAVE_WORKSPACE_FILENAME_ERROR"), STRINGLIST
                        .getString("SAVE_WORKSPACE_FILENAME_ERROR_TITLE"),
                JOptionPane.ERROR_MESSAGE);
    }

    public static int openWorkspaceError() {
        return JOptionPane.showConfirmDialog(MainWindow.getInstance(),
                STRINGLIST.getString("OPEN_WORKSPACE_ERROR"), STRINGLIST.getString("OPEN_WORKSPACE_ERROR_TITLE"),
                JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
    }

    public static void saveError() {
        String messageString = "<html>An error occurred while saving. <br><br>"+
                "Please make sure that you have write permissions to <br>"+
                "the file you are writing to and the disk is not full. <br><br>"+
                "Remember that files can be saved wherever you want <br>"+
                "by choosing 'Save as...'</html>";

        JOptionPane.showMessageDialog(MainWindow.getInstance(),
                messageString, "Save error",
                JOptionPane.ERROR_MESSAGE);
    }

    public static int simulationSettings() {
        return JOptionPane.showOptionDialog(
                MainWindow.getInstance(),
                STRINGLIST.getString("CONSISTENCY_CDMA_EVENTS"),
                STRINGLIST.getString("CONSISTENCY_CDMA_EVENTS_TITLE"),
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE, null, new Object[]{"Run simulation with current settings", "Simulate 100 events", "Cancel"}, "Simulate 100 events");
    }

    public static void invalidFile() {
        JOptionPane.showMessageDialog(MainWindow.getInstance(), "Invalid file",
                "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static boolean overrideInLibrary(Component parent, String reference ) {
        return JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(parent,
                String.format( "'%s' already exists in the library. Do you want to override?", reference ),
                "Library conflict",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
    }

    public static void backwardMigrationNotSupported() {
        JOptionPane.showMessageDialog(null,
                "In order to open this workspace, please install the latest version\nof SEAMCAT available for download in www.seamcat.org",
                "SEAMCAT version too old",
                JOptionPane.ERROR_MESSAGE);
    }

    public static void cannotOverwriteError(String filename) {
        JOptionPane.showMessageDialog(null,
                "It is only possible to write new files from plugins.\nFile '"+filename+"' already exists.", "SEAMCAT Error",
                JOptionPane.ERROR_MESSAGE);
    }

    public static void fileExtensionNotRecognized(String filename) {
        StringBuilder sb = new StringBuilder();
        for (FileFormat format : TabularDataFactory.allFormats()) {
            sb.append( format.getExtension());
            sb.append(" ");
        }

        JOptionPane.showMessageDialog(null,
                "Unknown file extension.\nFile '"+filename+"' has an unknown extension to SEAMCAT\nSupported extensions are: "+sb.toString(), "SEAMCAT Error",
                JOptionPane.ERROR_MESSAGE);
    }


    public static void versionWarning() {
        JOptionPane.showMessageDialog(null,
                "You are running an older version of SEAMCAT. For plugins this means " +
                        "non backward compatible changes. Get the latest version of SEAMCAT " +
                        "go to the web page...", "SEAMCAT version warning!",
                JOptionPane.WARNING_MESSAGE);
    }


    public static void versionMessage() {
        JOptionPane.showMessageDialog(null,
                "A newer version of SEAMCAT is available. For plugins the changes " +
                        "backward compatible but the API has more features available than " +
                        "the current one. Get the latest version of SEAMCAT go to the web page..."
                , "SEAMCAT version warning!",
                JOptionPane.INFORMATION_MESSAGE);
    }

    public static void generalSeamcatInitializationError(Exception e) {
        JOptionPane.showMessageDialog(null,
                "Initialization of SEAMCAT failed.",
                "Initialization of SEAMCAT failed",
                JOptionPane.ERROR_MESSAGE);
    }

    public static void generalSeamcatError(Throwable e) {
        generalSeamcatError(
                "An unexpected error occurred.\n\nDiagnostics information: \n" + e.getMessage());
    }

    public static void generalSeamcatError(String message) {
        generalSeamcatError(message, "Error");
    }

    public static void generalSeamcatError(String message, String title) {
        JOptionPane.showMessageDialog(
                MainWindow.getInstance(),
                message,
                title,
                JOptionPane.ERROR_MESSAGE);
    }

    public static int closingAllWorkspacesBatch() {
        return JOptionPane.showConfirmDialog(
                MainWindow.getInstance(),
                "Opening batch dialog will close all open workspaces. Do you want to proceed?",
                "Closing all workspaces",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.INFORMATION_MESSAGE);
    }

    public static void importLibraryNameConflict(List<LibraryItem> duplicates) {
        StringBuilder sb = new StringBuilder();
        for (LibraryItem duplicate : duplicates) {
            sb.append("<li>").append( Model.getInstance().getLibrary().typeName(duplicate )).append(" imported as '").append(duplicate.description().name()).append("' already exists in library</li>");
        }
        sb.append("</ul>");
        JOptionPane.showMessageDialog(
                MainWindow.getInstance(),
                "<html>Duplicate names in library import<br><br><ul>"+sb.toString()+"</html>",
                "Duplicate names for Import",
                JOptionPane.ERROR_MESSAGE);
    }

    public static void cannotDeleteUsedSystem(String name) {
        JOptionPane.showMessageDialog(
                MainWindow.getInstance(),
                "<html>System '"+name+"' is used by the scenario and cannot be deleted</html>",
                "Unable to delete system",
                JOptionPane.ERROR_MESSAGE);
    }

    public static void interferenceCriteriaError() {
        JOptionPane.showMessageDialog(
                MainWindow.getInstance(),
                "<html>Interference criteria can only be calculated when 'Noise Floor' is a constant distribution</html>",
                "Cannot tune interference criteria",
                JOptionPane.ERROR_MESSAGE);
    }

    public static void illegalCriteriaSelection() {
        JOptionPane.showMessageDialog(
                MainWindow.getInstance(),
                "<html>Illegal selection of Interference criteria. Please select a valid row</html>",
                "Selection error",
                JOptionPane.ERROR_MESSAGE);
    }

    public static void gainCalculationError(Exception e) {
        JOptionPane.showMessageDialog(
                MainWindow.getInstance(),
                "<html>Error calculating the antenna gain plot: " + e.getMessage() + "</html>",
                "Gain Evaluation Error",
                JOptionPane.ERROR_MESSAGE);
    }
}
