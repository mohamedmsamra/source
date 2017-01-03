package org.seamcat.presentation.genericgui.panelbuilder;

import org.seamcat.cdma.CDMALinkLevelData;
import org.seamcat.eventbus.Subscriber;
import org.seamcat.eventbus.UIEventHandler;
import org.seamcat.function.FunctionType;
import org.seamcat.model.Workspace;
import org.seamcat.model.distributions.AbstractDistribution;
import org.seamcat.model.distributions.Distribution;
import org.seamcat.model.functions.BlockingMask;
import org.seamcat.model.functions.EmissionMask;
import org.seamcat.model.functions.Function;
import org.seamcat.model.functions.MaskFunction;
import org.seamcat.model.generic.ProxyHelper;
import org.seamcat.model.plugin.*;
import org.seamcat.model.systems.CalculatedValue;
import org.seamcat.model.systems.SystemModel;
import org.seamcat.model.types.AntennaGain;
import org.seamcat.model.types.PropagationModel;
import org.seamcat.plugin.AntennaGainConfiguration;
import org.seamcat.plugin.PluginConfiguration;
import org.seamcat.plugin.PropagationModelConfiguration;
import org.seamcat.presentation.eventprocessing.PluginConfigurationPanel;
import org.seamcat.presentation.genericgui.GenericModelEditorPanel;
import org.seamcat.presentation.genericgui.ItemChangedEvent;
import org.seamcat.presentation.genericgui.WidgetAndKind;
import org.seamcat.presentation.genericgui.item.*;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.List;

import static org.seamcat.presentation.genericgui.WidgetKind.LABEL;

public class GenericPanelEditor<T> extends GenericModelEditorPanel<T> {

    private List<AbstractItem> allItems = new ArrayList<AbstractItem>();
    private List<ChangeListener<T>> changeListeners = new ArrayList<ChangeListener<T>>();
    private Map<BooleanItem, List<GroupItem>> groups = new HashMap<BooleanItem, List<GroupItem>>();
    private Class<T> modelClass;
    private Map<Method, AbstractItem> parameters = new HashMap<>();
    private PluginConfigurationPanel embeddedEditor;
    private Method embeddedMethod;
    private List<CalculatedValueItem> calculatedValues = new ArrayList<CalculatedValueItem>();

    public List<CalculatedValueItem> getCalculatedValues() {
        return calculatedValues;
    }

    public class GroupItem {
        GroupItem( AbstractItem item, boolean inverted ) {
            this.item = item;
            this.inverted = inverted;
        }
        GroupItem( AbstractItem item ) {
            this.item = item;
        }
        private AbstractItem item;
        private boolean inverted;
    }

    public GenericPanelEditor( JFrame owner, PluginConfiguration configuration) {
        this(owner, configuration.getModelClass(), (T) configuration.getModel());
    }

    public GenericPanelEditor( JFrame owner, Class<T> modelClass, T model) {
        this(owner, modelClass, model, null, null);
    }

    public GenericPanelEditor( JFrame owner, Class<T> modelClass, T model, Workspace workspace, SystemModel owningSystem) {
        this.modelClass = modelClass;
        final ResourceBundle resourceBundle = ResourceBundle.getBundle("stringlist", java.util.Locale.ENGLISH);

        for (Field field : modelClass.getDeclaredFields()) {
            if ( Modifier.isStatic(field.getModifiers()) ) {
                // should always be true since interface is expected
                try {
                    if ( ChangeListener.class.isAssignableFrom(field.getType())) {
                        changeListeners.add((ChangeListener<T>) field.get(null));
                    }
                } catch (IllegalAccessException e) {
                    //System.out.println("error getting default");
                }
            }
        }

        LinkedHashMap<Method, Object> defaultValues = ProxyHelper.defaultValues(modelClass);

        if ( defaultValues.size() == 1 ) {
            // check if embed is an option
            Map.Entry<Method, Object> only = defaultValues.entrySet().iterator().next();
            Class<?> type = only.getKey().getReturnType();
            if ( AntennaGain.class.isAssignableFrom(type)) {
                embeddedEditor = new PluginConfigurationPanel(owner, (PluginConfiguration) only.getValue(), false, AntennaGainConfiguration.class);
            } else if ( PropagationModel.class.isAssignableFrom(type)) {
                embeddedEditor = new PluginConfigurationPanel(owner, (PluginConfiguration) only.getValue(), false, PropagationModelConfiguration.class);
            }

            if ( embeddedEditor != null ) {
                embeddedMethod = only.getKey();
                if ( model != null ) {
                    try {
                        embeddedEditor.setModel((PluginConfiguration) only.getKey().invoke( model ));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
                setLayout(new BorderLayout());
                add( embeddedEditor, BorderLayout.CENTER );
                return;
            }
        }

        Map<String, BooleanItem> groupDefinition = new HashMap<>();
        Map<String, List<GroupItem>> members = new HashMap<>();

        for (Map.Entry<Method,Object> entry : defaultValues.entrySet()) {
            Method method = entry.getKey();
            Class<?> type = method.getReturnType();
            Config con = method.getAnnotation(Config.class);
            String name = method.getName();
            String unit;
            String rangeUnit;
            String information = null;
            String toolTip = null;
            List<String> selection = null;

            name = con.name().isEmpty() ? name : con.name();
            if ( resourceBundle.containsKey( name ) ) {
                name = resourceBundle.getString( name );
            } else if (resourceBundle.containsKey( name)) {
                name = resourceBundle.getString( name );
            }
            unit = con.unit().isEmpty() ? null : con.unit();
            rangeUnit = con.rangeUnit().isEmpty() ? null : con.rangeUnit();
            String values = con.values();
            if ( !values.isEmpty() ) {
                selection = new ArrayList<>();
                Collections.addAll(selection, values.split(","));
            }
            if ( !con.information().isEmpty() ) {
                information = CompositeEditor.replaceIMG( resourceBundle.getString(con.information()) );
            }
            if ( !con.toolTip().isEmpty()) {
                toolTip = con.toolTip();
            }

            AbstractItem item = null;
            if ( type.isPrimitive() ) {
                if ( boolean.class.isAssignableFrom(type)) {
                    item = new BooleanItem();
                } else if (double.class.isAssignableFrom(type)) {
                    item = new DoubleItem();
                } else if (int.class.isAssignableFrom(type)) {
                    item = new IntegerItem();
                }
            } else {
                if ( Boolean.class.isAssignableFrom(type)) {
                    item = new BooleanItem();
                } else if (AbstractDistribution.class.isAssignableFrom( type )) {
                    item = new DistributionItem(owner);
                } else if (String.class.isAssignableFrom(type)) {
                    if (selection == null) {
                        item = new TextItem();
                    } else {
                        item = new SelectionItem<String>().values(selection);
                    }
                } else if (Enum.class.isAssignableFrom( type)) {
                    List<?> selectionItems = Arrays.asList(type.getEnumConstants());
                    item = new SelectionItem().values(selectionItems);
                } else if (EmissionMask.class.isAssignableFrom(type)) {
                    item = new Function2LibraryItem(owner, owningSystem, workspace);
                } else if (BlockingMask.class.isAssignableFrom(type)) {
                    if ( unit != null && rangeUnit != null ) {
                        item = new FunctionLibraryItem(owner).axisNames(unit, rangeUnit);
                    } else {
                        item = new FunctionLibraryItem(owner);
                    }
                } else if ( MaskFunction.class.isAssignableFrom(type)) {
                    item = new Function2Item(owner);
                } else if (Function.class.isAssignableFrom(type)) {
                    FunctionItem functionItem = new FunctionItem(owner);
                    String[] units = getUnits(method, unit, rangeUnit);
                    functionItem.axisNames( units[0], units[1]);
                    item = functionItem.label(name).functionType( getFunctionType( method));
                } else if (OptionalFunction.class.isAssignableFrom(type)) {
                    OptionalFunctionItem functionItem = new OptionalFunctionItem(owner);
                    String[] units = getUnits(method, unit, rangeUnit);
                    functionItem.axis( units[0], units[1]);
                    item = functionItem.functionType( getFunctionType( method ));
                } else if (OptionalDoubleValue.class.isAssignableFrom(type)){
                    item = new OptionalDoubleItem();
                } else if (Distribution.class.isAssignableFrom(type)) {
                    item = new DistributionItem(owner);
                } else if ( Double.class.isAssignableFrom(type)) {
                    item = new DoubleItem();
                } else if (Integer.class.isAssignableFrom(type)) {
                    item = new IntegerItem();
                } else if (PropagationModel.class.isAssignableFrom(type)) {
                    if ( con.embed() ) {
                        item = new EmbeddedPluginItem(owner, (PluginConfiguration) entry.getValue(), PropagationModelConfiguration.class);
                    } else {
                        item = new PluginItem(owner, name);
                    }
                } else if (AntennaGain.class.isAssignableFrom(type)) {
                    if ( con.embed() ) {
                        item = new EmbeddedPluginItem(owner, (PluginConfiguration) entry.getValue(), AntennaGainConfiguration.class);
                    } else {
                        item = new PluginItem(owner, name);
                    }
                } else if ( OptionalMaskFunction.class.isAssignableFrom(type)) {
                    item = new OptionalFunction2Item(owner, name, "MHz", "dBm");
                } else if ( OptionalDistribution.class.isAssignableFrom(type)) {
                    item = new OptionalDistributionItem(owner);
                } else if (CDMALinkLevelData.class.isAssignableFrom(type)) {
                    addItem( new ComponentWrapperItem( new WidgetAndKind(new JLabel("Link Level Data"), LABEL)));
                    item = new LLDItem(owner, con.downLink());
                } else if (CalculatedValue.class.isAssignableFrom(type)) {
                    item = new CalculatedValueItem();
                    calculatedValues.add((CalculatedValueItem) item);
                }
            }

            if ( item == null ) throw new RuntimeException("Missing widget for type: "  + entry.getValue());
            if ( !con.group().isEmpty() ) {
                add( members, new GroupItem(item), con.group() );
            }
            if ( !con.invertedGroup().isEmpty() ) {
                add( members, new GroupItem(item, true), con.invertedGroup());
            }
            if ( !con.defineGroup().isEmpty() && item instanceof BooleanItem) {
                groupDefinition.put( con.defineGroup(), (BooleanItem) item);
            }

            item.label(name);
            if ( unit != null ) {
                item.unit( unit );
            }
            if ( information != null ) {
                item.information( information );
            }
            if ( toolTip != null ) {
                item.tooltip(toolTip);
            }
            item.initialize();
            parameters.put( method, item);
            addItem(item);
            allItems.add(item);
            setInitialValue(model, entry.getValue(), method, item);
        }

        // setup groups correctly
        for (Map.Entry<String, BooleanItem> entry : groupDefinition.entrySet()) {
            groups.put( entry.getValue(), new ArrayList<GroupItem>( members.get( entry.getKey()) ));
        }


        initializeWidgets();
        Subscriber.subscribe(this);
        // How to unsubscribe???
        changed(null);
        for (BooleanItem item : groupDefinition.values()) {
            handleGroupEnablement( item, item.getValue() );
        }
    }

    private void add( Map<String, List<GroupItem>> members, GroupItem member, String name) {
        List<GroupItem> groupItems = members.get(name);
        if ( groupItems == null ) {
            groupItems = new ArrayList<>();
            members.put( name, groupItems );
        }
        groupItems.add( member );
    }


    private String[] getUnits(Method method, String unit, String rangeUnit) {
        String[] units = new String[]{"X","Y"};
        if ( unit != null ) {
            units[1] = unit;
        }
        if ( rangeUnit != null ) {
            units[0] = rangeUnit;
        }

        Horizontal hor = method.getAnnotation(Horizontal.class);
        Vertical ver = method.getAnnotation(Vertical.class);
        Spherical sph = method.getAnnotation(Spherical.class);

        if ( hor != null || ver != null || sph != null) {
            if (rangeUnit == null) {
                units[0] = "Degree";
            }
            if (unit == null) {
                units[1] = "Attenuation (dB)";
            }
        }

        return units;
    }

    private FunctionType getFunctionType(Method method) {
        Horizontal hor = method.getAnnotation(Horizontal.class);
        if ( hor != null ) { return FunctionType.horizontal(); }

        Vertical ver = method.getAnnotation(Vertical.class);
        if ( ver != null ) return FunctionType.vertical();

        Spherical sph = method.getAnnotation(Spherical.class);
        if ( sph != null ) return FunctionType.spherical();

        return FunctionType.none();
    }

    public List<AbstractItem> getAllItems() {
        return allItems;
    }

    private void setInitialValue(T model, Object value, Method method, AbstractItem item) {
        if ( model != null ) {
            try {
                value = method.invoke( model );
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        if ( value instanceof OptionalDoubleValue) {
            OptionalDoubleValue o = (OptionalDoubleValue) value;
            value = new ValueWithUsageFlag<Double>(o.isRelevant(), o.getValue());
        } else if ( value instanceof OptionalFunction) {
            OptionalFunction function = (OptionalFunction) value;
            value = new ValueWithUsageFlag<Function>(function.isRelevant(), function.getFunction());
        } else if ( value instanceof OptionalMaskFunction ) {
            OptionalMaskFunction maskFunction = (OptionalMaskFunction) value;
            value = new ValueWithUsageFlag<MaskFunction>(maskFunction.isRelevant(), maskFunction.getMaskFunction());
        } else if ( value instanceof OptionalDistribution ) {
            OptionalDistribution dist = (OptionalDistribution) value;
            value = new ValueWithUsageFlag<Distribution>(dist.isRelevant(), dist.getValue());
        }

        item.setValue( value );
    }

    @UIEventHandler
    public void handle(ItemChangedEvent event) {
        if ( allItems.contains( event.getItem())) {
            changed((AbstractItem) event.getItem());
        }
    }

    private void changed(AbstractItem changedItem ) {
        // handle groups
        if ( changedItem instanceof BooleanItem && groups.containsKey( changedItem ) ) {
            BooleanItem group = (BooleanItem) changedItem;
            handleGroupEnablement(group, group.getValue());
        }

        for (ChangeListener<T> changeListener : changeListeners) {
            changeListener.handle( getModel(), allItems, changedItem);
        }
    }


    private void handleGroupEnablement(BooleanItem group, boolean enable) {
        for (GroupItem member : groups.get(group)) {
            member.item.setRelevant( member.inverted ? !enable : enable);

            if ( member.item instanceof BooleanItem && groups.containsKey( member.item )) {
                boolean value = member.inverted ? !enable : enable;
                if ( value ) {
                    BooleanItem sub = (BooleanItem) member.item;
                    value = sub.getValue();
                }
                handleGroupEnablement((BooleanItem) member.item, value);
            }
        }

    }

    @Override
    public T getModel() {
        return ProxyHelper.proxy(modelClass, values());
    }

    public Class<?> getModelClass() {
        return modelClass;
    }

    private LinkedHashMap<Method, Object> values() {
        LinkedHashMap<Method, Object> values = new LinkedHashMap<Method, Object>();
        if ( embeddedEditor != null ) {
            values.put( embeddedMethod, embeddedEditor.getModel() );
        } else {
            for (Method method : Cache.orderedConfig(modelClass)) {
                AbstractItem item = parameters.get(method);
                if ( item == null ) continue;
                Object value = item.getValue();
                if ( value instanceof ValueWithUsageFlag) {
                    ValueWithUsageFlag usage = ((ValueWithUsageFlag)value);
                    Constructor<?>[] constructors = method.getReturnType().getConstructors();
                    try {
                        value = constructors[0].newInstance(usage.useValue, usage.value);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                values.put( method, value);
            }
        }
        return values;
    }

    public void setLabel(int itemNumber, String label) {
        allItems.get( itemNumber ).updateLabel(label);
    }


    public void addChangeListener( ChangeListener<T> changeListener ) {
        changeListeners.add( changeListener );
    }
}
