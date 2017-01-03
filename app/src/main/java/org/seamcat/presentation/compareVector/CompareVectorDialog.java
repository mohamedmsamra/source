package org.seamcat.presentation.compareVector;

import org.apache.log4j.Logger;
import org.seamcat.help.SeamcatHelpResolver;
import org.seamcat.model.functions.Point2D;
import org.seamcat.model.simulation.SimulationResultGroup;
import org.seamcat.model.types.result.*;
import org.seamcat.presentation.EscapeDialog;
import org.seamcat.presentation.FileDialogHelper;
import org.seamcat.presentation.MainWindow;
import org.seamcat.presentation.propagationtest.AddRemovePanel;
import org.seamcat.presentation.propagationtest.PropagationHolder;
import org.seamcat.tabulardataio.FileDataIO;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class CompareVectorDialog extends EscapeDialog implements OnSelectionChangedListener, AddRemovePanel.AddRemoveListener, TreeSelectionListener {

	private static final ResourceBundle STRINGLIST = ResourceBundle.getBundle("stringlist", Locale.ENGLISH);
    private static final String EXTERNAL = "External";
    private static final String WORKSPACES = "Workspaces";
    private static final Logger LOG = Logger.getLogger(CompareVectorPanel.class);

    private CompareVectorPanel compareVectorPanel;

    private SelectionTree tree;
    private CheckBoxTreeManager checkBoxTreeManager;
    private RunnerThread runner;
    private JPanel treePanel;
    private AddRemovePanel addRemovePanel;
    private Node root;
    private ExternalRootNode externalRoot;
    private Node selectedNode = null;


    public CompareVectorDialog(JFrame owner, List<WorkspaceVectors> resultVectors ) {
		super(owner, true);
		setLayout(new BorderLayout());
        root = new Node("Root");
        Node workspaceRoot = new Node(WORKSPACES);

        addRemovePanel = new AddRemovePanel();
        addRemovePanel.addAddRemoveListener(this);
        addRemovePanel.setAddToolTip(STRINGLIST.getString("COMPARE_VECTOR_BTN_REMOVE"));
        addRemovePanel.setAddToolTip(STRINGLIST.getString("COMPARE_VECTOR_BTN_ADD"));
        addRemovePanel.enableRemove(false);
        addRemovePanel.setLabelText(STRINGLIST.getString("COMPARE_VECTOR_ADD_REMOVE_LABEL"));

        populateTree(workspaceRoot, resultVectors);

        runner = new RunnerThread();
        root.add(workspaceRoot);
        tree = new SelectionTree(root);
        tree.setRootVisible(false);
        tree.addTreeSelectionListener(this);
        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if ( e.getClickCount() > 1 ) {
                    TreePath tp = tree.getPathForLocation(e.getX(), e.getY());
                    if (tp != null) {
                        Node node = (Node) tp.getLastPathComponent();

                        if ( node.getUserObject() instanceof Node ) {
                            Node inner = (Node) node.getUserObject();
                            if ( inner.getUserObject() instanceof PropagationHolder ) {
                                updateTitle((PropagationHolder) inner.getUserObject());
                            }
                        } else if ( node.getUserObject() instanceof PropagationHolder) {
                            updateTitle((PropagationHolder) node.getUserObject());
                        }
                    }
                }
            }
        });

        checkBoxTreeManager = new CheckBoxTreeManager(tree);
        checkBoxTreeManager.setOnSelectionChangedListener(this);

        JScrollPane scrollPane = new JScrollPane(tree);
        scrollPane.setPreferredSize(new Dimension(300, scrollPane.getPreferredSize().height));

        treePanel = new JPanel(new BorderLayout());
        treePanel.setBorder(new TitledBorder(STRINGLIST.getString("COMPARE_VECTOR_TREE_TITLE")));

        treePanel.add(addRemovePanel, BorderLayout.NORTH);
        treePanel.add(scrollPane, BorderLayout.CENTER);
        ToolTipManager.sharedInstance().registerComponent(tree);
        runner.start();

        compareVectorPanel = new CompareVectorPanel(this);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

	}


    private void updateTitle(PropagationHolder holder) {
        String changed = JOptionPane.showInputDialog("Change name", holder.getTitle());
        if ( changed != null ) {
            holder.setTitle( changed );
            // update graph
            onSelectionChanged( checkBoxTreeManager.getSelectionModel().getAllCheckedPaths(checkBoxTreeManager, tree) );
        }
    }

    public void showDialog() {

        getContentPane().add(treePanel, BorderLayout.WEST);
		getContentPane().add(compareVectorPanel, BorderLayout.CENTER);

		setTitle(STRINGLIST.getString("COMPARE_VECTOR_DIALOG_TITLE"));

		JDialog.setDefaultLookAndFeelDecorated(true);
		pack();
		setLocationRelativeTo(owner);
		setVisible(true);
	}

	@Override
	public void dispose() {
        runner.kill();
		super.dispose();
	}


    private class ExternalNode extends Node {

        public ExternalNode(Object object) {
            super(object);
        }
    }

    private class ExternalRootNode extends ExternalNode {

        public ExternalRootNode(Object object) {
            super(object);
        }

        @Override
        public String toString() {
            return EXTERNAL;
        }
    }

    private class SelectionTree extends JTree {

        public SelectionTree(Node root) {
            super(root);
        }

        @Override
        public boolean isPathEditable(TreePath path) {
            Node node = (Node) path.getLastPathComponent();
            return node.isEnabled() && isEditable();
        }
    }

    /**
     * thread for setting the selected dataset in the graph
     *
     * @author Rasmus.Gohs@Jayway.com
     *
     */
    private class RunnerThread extends Thread {

        List<TreePath> selectedPaths;
        boolean running = true;

        public void kill() {
            synchronized (RunnerThread.this) {
                running = false;
                RunnerThread.this.notify();
            }
        }

        @Override
        public void run() {
            while (running) {
                try {
                    synchronized (this) {
                        wait();
                    }
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                if (running) {
                    try {
                        setSelected(selectedPaths);
                    } catch (Exception ex) {
                        LOG.error("Failed to load dataset", ex);
                    }
                }
            }
        }
    }

    /**
     * Sets the datasets in the graph from the selected vectors in the tree This
     * is a bit heavy. Thus this is called by the runnerThread
     */
    private void setSelected(final List<TreePath> selectedPaths) {
        List<PropagationHolder> propagationHolders = new ArrayList<>();
        Object component;
        Object value;
        for (TreePath treePath : selectedPaths) {
            component = treePath.getLastPathComponent();

            if (component instanceof Node) {
                value = ((Node) component).getUserObject();
                if (value instanceof Node) {
                    Object obj = ((Node) value).getUserObject();
                    if (obj instanceof PropagationHolder) {
                        propagationHolders.add((PropagationHolder) obj);
                    }
                } else if (value instanceof PropagationHolder) {
                    propagationHolders.add((PropagationHolder) value);
                }
            }
        }
        compareVectorPanel.clearChart();
        compareVectorPanel.show(propagationHolders);
    }


    private void populateTree(Node workspaceTop, List<WorkspaceVectors> resultVectors) {
        DefaultMutableTreeNode workspaceNode;
        Node simulationNode;

        for (WorkspaceVectors vectors : resultVectors) {
            workspaceNode = new Node( vectors.getWorkspaceTitle() );

            for (SimulationResultGroup resultGroup : vectors.getSimulationResult().getSeamcatResults()) {
                addGroup( resultGroup, (Node) workspaceNode );
            }

            if ( vectors.getSimulationResult().getEventProcessingResults() != null && vectors.getSimulationResult().getEventProcessingResults().size() > 0 ) {
                simulationNode = new Node("Event Processing Plugin");

                for (SimulationResultGroup eppResult : vectors.getSimulationResult().getEventProcessingResults()) {
                    addGroup( eppResult, simulationNode );
                }
                workspaceNode.add( simulationNode );
            }
            workspaceTop.add(workspaceNode);
        }
    }

    private void addGroup(SimulationResultGroup resultGroup, Node parent) {
        if ( resultGroup.failed() ) return;
        ResultTypes types = resultGroup.getResultTypes();
        Node vectorParent = new Node(resultGroup.getName());
        boolean appended = false;

        for (PropagationHolder holder : toPHs(types.getVectorResultTypes())) {
            Node vectorNode = new Node(holder);
            vectorParent.add( new Node( vectorNode ));
            appended = true;
        }
        for (PropagationHolder holder : groupPHs(types.getVectorGroupResultTypes() )) {
            Node vectorNode = new Node(holder);
            vectorParent.add( new Node( vectorNode ));
            appended = true;
        }
        if ( appended) {
            parent.add( vectorParent );
        }
    }

    @Override
    public void add() {
        FileDialogHelper helper = MainWindow.getInstance().fileDialogHelper.addVector(this);
        if ( helper.selectionMade() ) {
            FileDataIO fileio = new FileDataIO();
            for (File file : helper.getSelectedFiles()) {
                fileio.setFile(file);
                // TODO: add a proper vector type with name, value (maybe
                List<Point2D> points = new ArrayList<>();
                String name = fileio.loadVector(points);
                addExternalVector(file.getName(), name, points);
            }
        }
    }

    @Override
    public void remove() {
        if (selectedNode != null) {
            DefaultTreeModel model = (DefaultTreeModel) tree.getModel();

            if (selectedNode == externalRoot) {
                externalRoot = null;
            }

            model.removeNodeFromParent(selectedNode);

            CheckBoxTreeModel checkBoxTreeModel = checkBoxTreeManager.getSelectionModel();
            onSelectionChanged(checkBoxTreeModel.getAllCheckedPaths(checkBoxTreeManager, tree));
        }
    }

    @Override
    public void help() {
        SeamcatHelpResolver.showHelp(this);
    }

    private void addExternalVector(String fileName, String title, List<? extends Point2D> loadedPoints) {
        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
        if (externalRoot == null) {
            externalRoot = new ExternalRootNode(EXTERNAL);
            model.insertNodeInto(externalRoot, root, root.getChildCount());
        }

        ExternalNode externalNode = new ExternalNode(fileName);
        model.insertNodeInto(externalNode, externalRoot, externalRoot.getChildCount());

        PropagationHolder holder = new PropagationHolder();
        holder.setTitle( title );
        double[] data = new double[loadedPoints.size() ];
        for (int i = 0; i < loadedPoints.size(); i++) {
            Point2D point = loadedPoints.get(i);
            data[i] = point.getY();
        }
        holder.setData( data );
        model.insertNodeInto(new ExternalNode(holder), externalNode, externalNode.getChildCount());
    }

    @SuppressWarnings("unused")
    @Override
    public void valueChanged(TreeSelectionEvent evt) {
        TreePath[] paths = evt.getPaths();
        TreeNode treeNode;
        TreeNode prev;
        boolean enableRemove = false;
        // Iterate through all affected nodes
        for (int i = 0; i < paths.length; i++) {

            if (evt.isAddedPath(i)) {

                treeNode = (TreeNode) paths[i].getLastPathComponent();

                if (!(treeNode instanceof ExternalNode)) {
                    return; // we are not external
                }

                if (treeNode instanceof ExternalRootNode) {
                    selectedNode = (Node) treeNode;
                    break; // We are external root

                }

                while (treeNode != null && !(treeNode instanceof ExternalRootNode)) {
                    enableRemove = true;
                    selectedNode = (Node) treeNode;
                    treeNode = treeNode.getParent(); // find this nodes root
                }

                if (treeNode.getChildCount() <= 1) {
                    selectedNode = (Node) treeNode; // If we have just one child (us)
                    // we remove that branch
                }

                break;
            } else {
                // This node has been deselected
                enableRemove = false;
                selectedNode = null;
                break;
            }
        }

        addRemovePanel.enableRemove(enableRemove);

    }

    @Override
    public void onSelectionChanged(List<TreePath> selectedPaths) {
        synchronized (runner) {
            runner.selectedPaths = selectedPaths;
            runner.notify();
        }
    }

    public List<PropagationHolder> groupPHs( List<VectorGroupResultType> vectors ) {
        List<PropagationHolder> result = new ArrayList<PropagationHolder>();
        for (VectorGroupResultType vector : vectors) {
            for (NamedVectorResult vectorResult : vector.getVectorGroup()) {
                PropagationHolder holder = new PropagationHolder();
                holder.setTitle( vectorResult.getName() );
                holder.setData( vectorResult.getVector().asArray() );
                result.add( holder );
            }
        }
        return result;
    }


    public List<PropagationHolder> toPHs( List<VectorResultType> vectors ) {
        List<PropagationHolder> result = new ArrayList<PropagationHolder>();
        for (VectorResultType vector : vectors) {
            PropagationHolder holder = new PropagationHolder();
            holder.setTitle( vector.getName() );
            holder.setData( vector.getValue().asArray() );
            result.add( holder );
        }
        return result;
    }
}
