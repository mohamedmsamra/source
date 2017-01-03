package org.seamcat.util;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class StringHelper {

	public static String objectToString(Object object) {
		if ( object == null ) return "";
        return object.toString();
	}

    public static String getDuplicatedName(String original, DefaultListModel model ) {
        List<String> existing = toList(model);
        if ( !existing.contains( original) ) {
            return original;
        }
        return getDuplicatedName( original, existing);
    }

    private static List<String> toList(DefaultListModel model ) {
        List<String> existing = new ArrayList<String>();
        for (int x = 0, size = model.getSize(); x < size; x++) {
            existing.add( model.get(x).toString() );
        }
        return existing;
    }

    public static String getDuplicatedName(String original, List<String> existing ) {
        int i = 0;
        while ((i < original.length())
                && (Character.isDigit(original.charAt(i)) || (original.charAt(i) == '.'))) {
            i++;
        }
        String prefix = original.substring(0, i);
        if (!prefix.isEmpty()) {
            prefix += ".";
        }
        String strippedOriginal;
        if (original.charAt(i) == '_') {
            strippedOriginal = original.substring(i + 1);
        } else {
            strippedOriginal = original.substring(i);
        }
        int seq = 1;
        while (true) {
            String newName = prefix + seq + "_" + strippedOriginal;
            if (!existsName(newName, existing)) {
                return newName;
            }
            seq++;
        }
    }

    private static boolean existsName(String name, List<String> existing ) {
        return existing.contains( name );
    }
}
