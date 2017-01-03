package org.seamcat.presentation.layout;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ComponentSplitLayout {

    /**
     * Generates a split view of the components. Each element in the outer list
     * represents a column and each element in the inner list represents a row
     */
    public static JPanel splitLayout( List<List<Component>> components, LinkedList<LinkedList<Integer>> positions ) {
        int cols = components.size();
        if ( cols == 1 ) {
            // no horizontal splits
            return split(components.get(0), JSplitPane.VERTICAL_SPLIT, positions.get(0));
        } else {
            LinkedList<Integer> colPos = positions.removeLast();
            List<Component> columns = new ArrayList<Component>();
            for (List<Component> component : components) {
                columns.add( split(component, JSplitPane.VERTICAL_SPLIT, positions.isEmpty() || component.size() == 1 ? new LinkedList<Integer>() : positions.removeFirst()));
            }
            return split(columns, JSplitPane.HORIZONTAL_SPLIT, colPos);
        }
    }

    private static JPanel split(List<Component> components, int splitOrientation, LinkedList<Integer> position) {
        if ( components.size() < 1 ) throw new IllegalArgumentException("cannot layout empty component list");
        JPanel col = new JPanel(new BorderLayout());

        int totalSize = 0;
        for (Component component : components) {
            Dimension size = component.getPreferredSize();
            totalSize += size.getHeight();
        }
        List<JSplitPane> splits = new ArrayList<>();
        Component last = null;
        for (Component component : components) {
            last = component;
            JSplitPane jSplitPane = new JSplitPane(splitOrientation);
            if ( !position.isEmpty() ) {
                jSplitPane.setDividerLocation(position.removeFirst());
            }
            jSplitPane.add(component);
            splits.add(jSplitPane);
        }
        if ( splits.size() == 1) {
            col.add( components.get(0), BorderLayout.CENTER);
        } else {
            JSplitPane previous = null;
            for (int i = 0; i < splits.size()-1; i++) {
                if (previous != null) {
                    previous.add( splits.get(i) );
                }
                previous = splits.get(i);
            }
            assert previous != null;
            previous.add( last );

            col.add( splits.get(0), BorderLayout.CENTER );
        }

        return col;
    }


    public static <T> LinkedList<T> linkedList( T... ts ) {
        LinkedList<T> list = new LinkedList<T>();
        Collections.addAll( list, ts);
        return list;
    }

    public static List<Component> clist( Component... components) {
        List<Component> list = new ArrayList<Component>();
        Collections.addAll( list, components );
        return list;
    }
}
