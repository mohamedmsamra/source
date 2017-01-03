package org.seamcat.presentation.genericgui.item;

import org.seamcat.eventbus.EventBusFactory;
import org.seamcat.events.TextWidgetValueUpdatedEvent;
import org.seamcat.presentation.genericgui.WidgetAndKind;
import org.seamcat.presentation.genericgui.WidgetKind;
import org.seamcat.presentation.library.ChangeNotifier;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;


public class TextItem extends AbstractItem<String, Object> {

    private JComponent valueWidget;
    private int linesToDisplay;
    private TextWidgetValueUpdatedEvent event;
    private boolean readOnly;

    public TextItem() {
        linesToDisplay = 1;
    }

    public TextItem label(String label) {
        super.label(label);
        return this;
    }

    public TextItem linesToDisplay(int linesToDisplay) {
        this.linesToDisplay = linesToDisplay;
        return this;
    }

    public TextItem valueUpdatedEvent(TextWidgetValueUpdatedEvent event) {
        this.event = event;
        return this;
    }

    public TextItem readOnly( ) {
        this.readOnly = true;
        return this;
    }

    @Override
    public List<WidgetAndKind> createWidgets() {
        List<WidgetAndKind> widgets = super.createWidgets();
        if ( readOnly ) {
            valueWidget = new JLabel();
            widgets.add( new WidgetAndKind( valueWidget, WidgetKind.VALUE ));
        } else if (linesToDisplay == 1) {
            valueWidget = new JTextField();
            valueWidget.addPropertyChangeListener(new PropertyChangeListener() {

                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if(evt.getPropertyName().equals("value")) {
                        fireItemChanged();
                    }

                }
            });
            widgets.add(new WidgetAndKind(valueWidget, WidgetKind.VALUE));
        } else {
            JTextArea textArea = new JTextArea(3, 1);
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            JScrollPane pScroll = new JScrollPane(textArea);

            widgets.add(new WidgetAndKind(pScroll, WidgetKind.VALUE));
            valueWidget = textArea;
        }

        if ( event != null ) {
            valueWidget.addFocusListener( new FocusListener() {
                public void focusGained(FocusEvent focusEvent) {
                }
                public void focusLost(FocusEvent focusEvent) {
                    event.setValue( getValue() );
                    EventBusFactory.getEventBus().publish( event );
                }
            });
        }
        return widgets;
    }

    @Override
    public void setRelevant(boolean relevant) {
        super.setRelevant(relevant);
        valueWidget.setEnabled( relevant );
    }

    public void addChangeNotifier( final ChangeNotifier notifier ) {
        valueWidget.addFocusListener( new FocusListener() {
            public void focusGained(FocusEvent focusEvent) {
            }
            public void focusLost(FocusEvent focusEvent) {
                notifier.changed();
            }
        });
    }

    @Override
    public String getValue() {
        if ( valueWidget instanceof JTextComponent ) {
            return ((JTextComponent) valueWidget).getText();
        } else if ( valueWidget instanceof JLabel ) {
            return ((JLabel) valueWidget).getText();
        }
        return null;
    }

    @Override
    public void setValue(String value) {
        if ( valueWidget instanceof JTextComponent) {
            ((JTextComponent) valueWidget).setText(value);
        } else if ( valueWidget instanceof JLabel ) {
            ((JLabel) valueWidget).setText(value);
        }
    }
}
