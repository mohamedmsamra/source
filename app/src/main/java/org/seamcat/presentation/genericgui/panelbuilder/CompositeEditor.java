package org.seamcat.presentation.genericgui.panelbuilder;

import org.seamcat.model.Workspace;
import org.seamcat.model.generic.ProxyHelper;
import org.seamcat.model.systems.SystemModel;
import org.seamcat.model.systems.UIPosition;
import org.seamcat.model.systems.UITab;
import org.seamcat.model.types.*;
import org.seamcat.model.workspace.CustomPanelBuilder;
import org.seamcat.plugin.AntennaGainConfiguration;
import org.seamcat.plugin.CoverageRadiusConfiguration;
import org.seamcat.plugin.PluginConfiguration;
import org.seamcat.plugin.PropagationModelConfiguration;
import org.seamcat.presentation.components.BorderPanelBuilder;
import org.seamcat.presentation.eventprocessing.PluginConfigurationPanel;
import org.seamcat.presentation.genericgui.item.AbstractItem;
import org.seamcat.presentation.genericgui.item.CalculatedValueItem;
import org.seamcat.presentation.genericgui.item.Item;
import org.seamcat.presentation.genericgui.item.TextItem;
import org.seamcat.presentation.layout.ComponentSplitLayout;
import org.seamcat.presentation.library.ChangeNotifier;
import org.seamcat.presentation.resources.ImageLoader;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Method;
import java.util.*;
import java.util.List;

public class CompositeEditor<T> extends JPanel {

    private static final ResourceBundle STRINGLIST = ResourceBundle.getBundle("stringlist", java.util.Locale.ENGLISH);

    private Workspace workspace;
    private SystemModel owningSystem;
    private JFrame owner;
    private Class<T> compositeClass;
    private boolean libraryMode;
    private final ChangeNotifier notifier;
    private final CustomPanelBuilder customPanels;
    private Map<Method, Object> widgetModelMap;
    private List<CalculatedValueItem> calculatedValues;
    private int index;
    private boolean hasTabs;
    private JTabbedPane jTabbedPane;
    private List<Component> components;

    public void enableItem(Class<?> clazz, int number, boolean enable) {
        for (Map.Entry<Method, Object> entry : widgetModelMap.entrySet()) {
            Object widget = entry.getValue();
            if (clazz.isAssignableFrom(entry.getKey().getReturnType())) {
                if (widget instanceof GenericPanelEditor) {
                    List<AbstractItem> items = ((GenericPanelEditor) widget).getAllItems();
                    items.get( number-1).setRelevant( enable );
                }
            }

            if (widget instanceof CompositeEditor) {
                ((CompositeEditor) widget).enableItem(clazz, number, enable);
            }
        }
    }

    public void enableWidget( Class<?> clazz, boolean enable ) {
        for (Map.Entry<Method, Object> entry : widgetModelMap.entrySet()) {
            Object widget = entry.getValue();
            if ( clazz.isAssignableFrom(entry.getKey().getReturnType())) {
                if ( widget instanceof GenericPanelEditor) {
                    ((GenericPanelEditor) widget).setGlobalRelevance(enable);
                } else if (widget instanceof PluginConfigurationPanel) {
                    ((PluginConfigurationPanel) widget).setGlobalRelevance(enable);
                } else if ( widget instanceof CompositeEditor ) {
                    // check if this is a tab
                    if ( hasTabs ) {
                        int i = jTabbedPane.indexOfComponent((Component) widget);
                        if ( i > 0 ) {
                            jTabbedPane.setEnabledAt( i , enable );
                        }
                    }
                }
            }

            if ( widget instanceof CompositeEditor) {
                ((CompositeEditor) widget).enableWidget(clazz, enable);
            }
        }
    }

    public GenericPanelEditor getEditor( Class<?> panelClass ) {
        for (Component component : components) {
            if ( component instanceof GenericPanelEditor) {
                if ( ((GenericPanelEditor) component).getModelClass() == panelClass) {
                    return (GenericPanelEditor) component;
                }
            }
        }

        return null;
    }

    public void setModel( T model ) {
        removeAll();
        initialize(model);
        revalidate();
        repaint();
    }

    public CompositeEditor(JFrame owner, Class<T> compositeClass, T compositeModel, boolean libraryMode,
                           ChangeNotifier notifier) {
        this(owner, compositeClass, compositeModel, libraryMode, notifier, null, null, null);
    }

    public CompositeEditor(JFrame owner, Class<T> compositeClass, T compositeModel, boolean libraryMode,
                           ChangeNotifier notifier, CustomPanelBuilder customPanels, SystemModel owningSystem, Workspace workspace ) {
        this.owner = owner;
        this.compositeClass = compositeClass;
        this.libraryMode = libraryMode;
        this.notifier = notifier;
        this.customPanels = customPanels;
        this.workspace = workspace;
        this.owningSystem = owningSystem;

        calculatedValues = new ArrayList<CalculatedValueItem>();
        widgetModelMap = new HashMap<Method, Object>();

        initialize(compositeModel);
    }

    private void initialize(T compositeModel) {
        components = new ArrayList<>();
        JPanel[] tabs = new JPanel[5];
        String[] tabNames = new String[5];
        String[] tabToolTips = new String[5];
        int[][] heights = new int[6][6];
        int[] widths = new int[6];
        calculatedValues.clear();
        widgetModelMap.clear();

        List<SortedMap<Integer, JPanel>> columns = new ArrayList<SortedMap<Integer, JPanel>>();
        for (int i=0; i<5; i++) {
            columns.add( new TreeMap<Integer, JPanel>());
        }
        for (Method method : compositeClass.getDeclaredMethods()) {

            UIPosition panel = method.getAnnotation(UIPosition.class);
            UITab tab = method.getAnnotation(UITab.class);
            Object invoke = null;
            try {
                invoke = method.invoke(compositeModel);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if ( panel != null )  {
                heights[panel.col()][ panel.row()] = panel.height();
                widths[panel.col()] = Math.max(widths[panel.col()], panel.width());
                // if panel has special renderer use that to generate panel
                Class panelClass = method.getReturnType();

                if (customPanels != null && customPanels.canBuild(panelClass)) {
                    PanelModelEditor editor = customPanels.build(panelClass, invoke, panel.name());
                    SortedMap<Integer, JPanel> column = columns.get( panel.col() );
                    column.put( panel.row(),  editor.getPanel());
                    widgetModelMap.put(method, editor);
                } else if ( CustomPanelHandler.canHandle(panelClass)) {
                    PanelModelEditor editor = CustomPanelHandler.create(method, panelClass, invoke, panel.name());
                    SortedMap<Integer, JPanel> column = columns.get( panel.col() );
                    column.put( panel.row(),  editor.getPanel());
                    widgetModelMap.put(method, editor);
                } else {
                    Component comp;
                    if (AntennaGain.class.isAssignableFrom( panelClass )) {
                        comp = new PluginConfigurationPanel(owner, (PluginConfiguration) invoke,false, AntennaGainConfiguration.class);
                    } else if (PropagationModel.class.isAssignableFrom(panelClass)) {
                        comp = new PluginConfigurationPanel(owner, (PluginConfiguration) invoke, false, PropagationModelConfiguration.class);
                    } else if (CoverageRadius.class.isAssignableFrom(panelClass)) {
                        comp = new PluginConfigurationPanel(owner, (PluginConfiguration) invoke,false, CoverageRadiusConfiguration.class);
                    } else {
                        if ( Description.class.isAssignableFrom(panelClass) && !libraryMode && LibraryItem.class.isAssignableFrom(compositeClass)) {
                            comp = new LibraryItemDescriptionPanel(owner, compositeClass, this, (Description) invoke);
                        } else {
                            GenericPanelEditor editor = new GenericPanelEditor(owner, panelClass, invoke, workspace, owningSystem);
                            calculatedValues.addAll(editor.getCalculatedValues());
                            if (Description.class.isAssignableFrom(panelClass) && libraryMode ) {
                                List<AbstractItem> allItems = editor.getAllItems();
                                for (AbstractItem item : allItems) {
                                    if ( item instanceof TextItem && item.getLabel().equals("Name") ) {
                                        ((TextItem) item).addChangeNotifier( notifier );
                                    }
                                }
                            }
                            comp = editor;
                        }
                    }
                    components.add( comp );
                    widgetModelMap.put( method, comp);
                    SortedMap<Integer, JPanel> column = columns.get( panel.col() );
                    BorderPanelBuilder builder = new BorderPanelBuilder(new JScrollPane(comp), panel.name());

                    if ( STRINGLIST.containsKey( panelClass.getName() ) ) {
                        builder.help("See SEAMCAT manual", STRINGLIST.getString(panelClass.getName()));
                    }
                    if ( STRINGLIST.containsKey( panelClass.getName()+".info")) {
                        builder.info( replaceIMG(STRINGLIST.getString(panelClass.getName() + ".info")));
                    }

                    column.put( panel.row(), builder.build());
                }
            } else if ( tab != null ) {
                CompositeEditor editor = new CompositeEditor(owner, method.getReturnType(), invoke, false, null, customPanels, owningSystem, workspace);
                components.add( editor );
                calculatedValues.addAll( editor.getCalculatedValues() );
                tabs[tab.order()] = editor;
                widgetModelMap.put( method, tabs[tab.order()]);
                tabNames[tab.order()] = tab.value();

                if ( STRINGLIST.containsKey( method.getReturnType().getName() + ".tip") ) {
                    tabToolTips[tab.order()] = replaceIMG(STRINGLIST.getString(method.getReturnType().getName()+".tip"));
                }
            }
        }
        setLayout(new BorderLayout());

        hasTabs = false;
        for (JPanel tab : tabs) {
            if ( tab != null ) {
                hasTabs = true;
                break;
            }
        }
        if ( hasTabs ) {
            add(createTopTabPanel(columns), BorderLayout.NORTH);
            jTabbedPane = new JTabbedPane();
            for (int i = 0; i < tabs.length; i++) {
                if ( tabs[i] == null  ) continue;
                if ( tabToolTips[i] != null ) {
                    jTabbedPane.addTab(tabNames[i], null, tabs[i], tabToolTips[i]);
                } else {
                    jTabbedPane.add(tabNames[i], tabs[i]);
                }
            }
            add( jTabbedPane, BorderLayout.CENTER);

        } else {
            LinkedList<LinkedList<Integer>> layout = new LinkedList<LinkedList<Integer>>();
            List<List<Component>> components = new ArrayList<List<Component>>();
            for (SortedMap<Integer, JPanel> column : columns) {
                List<Component> col = new ArrayList<Component>();
                for (JPanel jPanel : column.values()) {
                    col.add( jPanel );
                }
                if ( !col.isEmpty()) {
                    components.add(col);
                }
            }
            for (int[] height : heights) {
                LinkedList<Integer> rowSplit = new LinkedList<Integer>();
                for (int h : height) {
                    if ( h != 0 ) {
                        rowSplit.add( h );
                    }
                }
                if ( !rowSplit.isEmpty()) {
                    layout.add( rowSplit );
                }
            }
            LinkedList<Integer> colSplit = new LinkedList<Integer>();
            for (int width : widths) {
                if ( width != 0 ) {
                    colSplit.add( width );
                }
            }
            if ( !colSplit.isEmpty()) {
                layout.add( colSplit );
            }
            add(ComponentSplitLayout.splitLayout(components, layout), BorderLayout.CENTER);
        }
    }

    public static String replaceIMG( String raw) {
        if ( raw.contains("{$IMG")) {
            // generalize to several placeholders
            int begin = raw.indexOf("{$IMG");
            int end = raw.indexOf("}", begin);
            StringBuilder sb = new StringBuilder(raw.substring(0, begin));
            sb.append("<img src=\"");
            sb.append( ImageLoader.class.getResource( raw.substring(begin+6,end)));
            sb.append("\"></body></html>");
            return sb.toString();
        } else {
            return raw;
        }
    }

    private JComponent createTopTabPanel(List<SortedMap<Integer, JPanel>> columns) {
        if ( !columns.get(1).isEmpty() && !columns.get(2).isEmpty() ) {
            JSplitPane top = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
            top.setDividerLocation(700);
            SortedMap<Integer, JPanel> col = columns.get(1);
            top.add(col.get( col.firstKey()));
            col = columns.get(2);
            top.add(col.get(col.firstKey()));
            return top;
        }

        return new JPanel();
    }

    public Class<?> getCompositeClass() {
        return compositeClass;
    }

    public T getModel() {
        Map<Method, Object> values = new LinkedHashMap<>();
        for (Method method : Cache.ordered(compositeClass)) {

            Object modelHolder = widgetModelMap.get(method);
            Object model;
            if ( modelHolder instanceof PluginConfigurationPanel) {
                model = ((PluginConfigurationPanel) modelHolder).getModel();
            } else if ( modelHolder instanceof PanelModelEditor) {
                model = ((PanelModelEditor) modelHolder).getModel();
            } else if ( modelHolder instanceof GenericPanelEditor) {
                model = ((GenericPanelEditor) modelHolder).getModel();
            } else if ( modelHolder instanceof CompositeEditor) {
                model = ((CompositeEditor) modelHolder).getModel();
            } else if ( modelHolder instanceof LibraryItemDescriptionPanel) {
                LibraryItemDescriptionPanel panel = (LibraryItemDescriptionPanel) modelHolder;
                model = panel.getDescription();
            } else {
                throw new RuntimeException("unknown instance of model widget: " + modelHolder);
            }
            values.put( method, model);
        }
        return ProxyHelper.proxy( compositeClass, values );
    }

    public List<CalculatedValueItem> getCalculatedValues() {
        return calculatedValues;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public boolean containsItem(Item item) {
        for (Component component : components) {
            if ( component instanceof CompositeEditor) {
                if(((CompositeEditor) component).containsItem(item)){
                    return true;
                }
            } else if ( component instanceof GenericPanelEditor) {
                List<AbstractItem> allItems = ((GenericPanelEditor) component).getAllItems();
                for (AbstractItem anItem : allItems) {
                    if ( anItem == item ) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void activeTab(Integer integer) {
        if ( jTabbedPane != null ) {
            jTabbedPane.setSelectedIndex(integer);
        }

    }

    public Integer getActiveTab() {
        if ( jTabbedPane != null) {
            return jTabbedPane.getSelectedIndex();
        }
        return 0;
    }

    public <K> CompositeEditor<K> getTab(Class<K> clazz) {
        if ( jTabbedPane != null ){
            for ( int i = 0; i<jTabbedPane.getComponentCount(); i++) {
                Component component = jTabbedPane.getComponentAt(i);
                if ( component instanceof CompositeEditor ) {
                    if ( ((CompositeEditor) component).getCompositeClass() == clazz) {
                        return (CompositeEditor<K>) component;
                    }
                }
            }
        }
        return null;
    }
}
