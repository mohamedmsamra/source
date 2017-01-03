package org.seamcat.presentation;

import org.seamcat.eventbus.EventBusFactory;
import org.seamcat.eventbus.UIEventHandler;
import org.seamcat.events.SelectionMadeEvent;
import org.seamcat.function.DiscreteFunction;
import org.seamcat.function.FunctionType;
import org.seamcat.help.SeamcatHelpResolver;
import org.seamcat.model.functions.Bounds;
import org.seamcat.model.functions.Function;
import org.seamcat.presentation.components.NavigateButtonPanel;
import org.seamcat.presentation.components.UserDefinedFunctionPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ResourceBundle;

public class DialogFunctionDefine extends EscapeDialog {

    private static final ResourceBundle stringlist = ResourceBundle.getBundle("stringlist", java.util.Locale.ENGLISH);
    private SelectionPanel selectionPanel = new SelectionPanel(this, stringlist.getString("FUNCTION_CONSTANT"),stringlist.getString("FUNCTION_USERDEFINED"));
    private JPanel detailPanel = new JPanel(new BorderLayout());
    private ConstantPanel constantFunctionPanel = new ConstantPanel();
    private final UserDefinedFunctionPanel userDefinitionPanel;

    public DialogFunctionDefine(Frame parent, Function function, final FunctionType functionType, String xAxis, String yAxis) {
        super(parent, true);
        userDefinitionPanel = new UserDefinedFunctionPanel(function, functionType, xAxis, yAxis);
        setSize(new Dimension(700, 450));
        detailPanel.add( constantFunctionPanel, BorderLayout.CENTER );

         final Bounds[] bounds = new Bounds[1]; // = function.getBounds();
        if ( functionType.isNone() ) {
            getContentPane().add(selectionPanel, BorderLayout.WEST);
        }
        getContentPane().add(detailPanel, BorderLayout.CENTER);
        getContentPane().add(new NavigateButtonPanel(this) {
            public void btnOkActionPerformed() {
                if ( !functionType.isNone() ) {
                    if (userDefinitionPanel.getDiscreteFunction().points().size() == 0) {
                        JOptionPane.showMessageDialog(DialogFunctionDefine.this, "The function needs to have at least one point. Click cancel to exit this function dialog without making changes.", "Error", JOptionPane.ERROR_MESSAGE);
                    } else {
                        bounds[0] = userDefinitionPanel.getDiscreteFunction().getBounds();
                        double min = 0;
                        double max = 0;
                        if ( functionType.isHorizontal() ) {
                            min = 0;
                            max = 360;
                        }
                        if ( functionType.isVertical()) {
                            min = -90;
                            max = 90;
                        }
                        if ( functionType.isSpherical() ) {
                            min = 0;
                            max = 180;
                        }
                        if ((min == bounds[0].getMin()) && (max == bounds[0].getMax())) {
                            setAccept( true );
                            DialogFunctionDefine.this.setVisible(false);
                        } else {
                            JOptionPane.showMessageDialog(DialogFunctionDefine.this, "Range is not complete.\nRange should be ["
                                    + min + " to " + max + "]", "Range error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } else {
                    setAccept( true );
                    DialogFunctionDefine.this.setVisible(false);
                }
            }
        }, BorderLayout.SOUTH);
        registerHelp();
        setLocationRelativeTo(parent);
        EventBusFactory.getEventBus().subscribe( this );
    }

    public void destroy() {
        EventBusFactory.getEventBus().unsubscribe(this);
    }

    @UIEventHandler
    public void handle( SelectionMadeEvent event ) {
        if (event.getContext() == this ) {
            updateEditPanel(event.getName());
        }
    }

    private void updateEditPanel( String selected ) {
        detailPanel.removeAll();
        if ( selected.equals(stringlist.getString("FUNCTION_CONSTANT"))) {
            detailPanel.add( constantFunctionPanel, BorderLayout.CENTER );
        } else {
            detailPanel.add( userDefinitionPanel, BorderLayout.CENTER );
        }
        detailPanel.revalidate();
        detailPanel.repaint();

    }

    public DiscreteFunction getFunction() {
        if ( selectionPanel.getSelection().equals(stringlist.getString("FUNCTION_CONSTANT"))) {
            return new DiscreteFunction(constantFunctionPanel.getConstant());
        }
        return userDefinitionPanel.getDiscreteFunction();
    }


    private void registerHelp() {
        KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0);
        getRootPane().registerKeyboardAction(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                SeamcatHelpResolver.showHelp(DialogFunctionDefine.this);
            }

        }, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private void setFunction(Function f) {
        if (f.isConstant()) {
            constantFunctionPanel.setConstant(f.getConstant());
            selectionPanel.setSelection(stringlist.getString("FUNCTION_CONSTANT"));
        } else if (f instanceof DiscreteFunction) {
            userDefinitionPanel.setDiscreteFunction((DiscreteFunction) f);
            selectionPanel.setSelection(stringlist.getString("FUNCTION_USERDEFINED"));
        }
        updateEditPanel(selectionPanel.getSelection());
    }

    public boolean show(Function function, String windowtitle) {
        setFunction(function);
        setTitle(windowtitle);
        setAccept( false );
        super.setVisible(true);
        userDefinitionPanel.stopEditing();
        return isAccept();
    }
}