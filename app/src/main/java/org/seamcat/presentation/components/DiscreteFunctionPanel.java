package org.seamcat.presentation.components;

import org.seamcat.calculator.CalculatorInputField;
import org.seamcat.function.DiscreteFunction;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

@SuppressWarnings("serial")
public class DiscreteFunctionPanel extends JPanel {

	private static final int CONSTANT = 0;
	private static final String[] LAYOUTS = { "Constant", "User defined" };

	private static final ResourceBundle stringlist = ResourceBundle.getBundle("stringlist", java.util.Locale.ENGLISH);
	private static final int USERDEFINED = 1;
	private ConstantFunctionPanel constantFunctionPanel = new ConstantFunctionPanel();
	private CardLayout parametersLayout = new CardLayout();

	private JPanel parametersPanel = new JPanel(parametersLayout);

	private TypePanel typePanel = new TypePanel();

	private UserDefinedFunctionPanel userDefinitionPanel = new UserDefinedFunctionPanel("User defined function", "X", "Y" );

	public void setAxisNames( String xAxis, String yAxis ) {
		userDefinitionPanel.setAxisNames( xAxis, yAxis );
	}

	public DiscreteFunctionPanel(  ) {
		super( new BorderLayout());
		parametersPanel.add(LAYOUTS[CONSTANT], constantFunctionPanel);
		parametersPanel.add(LAYOUTS[USERDEFINED], userDefinitionPanel);

		typePanel.btnConstant.doClick();

		add(typePanel, BorderLayout.WEST);
		add(parametersPanel, BorderLayout.CENTER);
	}

	public void setModel(DiscreteFunction f) {
		if (f.isConstant()) {
			constantFunctionPanel.setConstant(f.getConstant());
			typePanel.setSelectedButton(CONSTANT);
		} else {
			userDefinitionPanel.setDiscreteFunction(f);
			typePanel.setSelectedButton(USERDEFINED);
		}
	}

	public DiscreteFunction getModel() {
		if ( typePanel.getSelectedButton() == CONSTANT ) {
			return new DiscreteFunction(constantFunctionPanel.getConstant());
		} else {
			return userDefinitionPanel.getDiscreteFunction();
		}
	}

	private class ConstantFunctionPanel extends JPanel {

		private JFormattedTextField tfConstant;

		public ConstantFunctionPanel() {
			super(new FlowLayout(FlowLayout.LEFT));
			tfConstant = new CalculatorInputField(0);

			JLabel lblConstant = new JLabel(stringlist.getString("FUNCTION_CONSTANT"));
			lblConstant.setLabelFor(tfConstant);

			add(lblConstant);
			add(tfConstant);
			setBorder(new TitledBorder("Parameters"));
		}

		public void clear() {
			setConstant(30);
		}

		public double getConstant() {
			return ((Number) tfConstant.getValue()).doubleValue();
		}

		public void setConstant(double constant) {
			tfConstant.setValue(constant);
		}
	}

	private class TypePanel extends JPanel {

		private JRadioButton btnConstant;
		private JRadioButton btnUserDefined;
		private int selectedButton;

		public TypePanel() {
			super();
			selectedButton = CONSTANT;
			btnConstant = new JRadioButton(stringlist.getString("FUNCTION_CONSTANT"));
			btnUserDefined = new JRadioButton(stringlist.getString("FUNCTION_USERDEFINED"));

			ButtonGroup buttonGroupType = new ButtonGroup();
			buttonGroupType.add(btnConstant);
			buttonGroupType.add(btnUserDefined);

			btnConstant.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent evt) {
					btnConstantActionPerformed(evt);
				}
			});
			btnUserDefined.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent evt) {
					btnUserDefinedActionPerformed(evt);
				}
			});

			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			add(btnConstant);
			add(btnUserDefined);
			setBorder(new TitledBorder("Type"));
		}

		private void btnConstantActionPerformed(ActionEvent e) {
			if (selectedButton != CONSTANT) {
				constantFunctionPanel.clear();
				setSelectedButton(CONSTANT);
			}
		}

		private void btnUserDefinedActionPerformed(ActionEvent e) {
			if (selectedButton != USERDEFINED) {
				userDefinitionPanel.clear();
				setSelectedButton(USERDEFINED);
			}
		}

		public int getSelectedButton() {
			return selectedButton;
		}

		public void setSelectedButton(int button) {
			selectedButton = button;
			switch (button) {
				case CONSTANT: {
					btnConstant.setSelected(true);
					btnConstant.requestFocus();
					parametersLayout.show(parametersPanel, LAYOUTS[CONSTANT]);
					break;
				}
				case USERDEFINED: {
					btnUserDefined.setSelected(true);
					btnUserDefined.requestFocus();
					parametersLayout.show(parametersPanel, LAYOUTS[USERDEFINED]);
					break;
				}
				default: {
					throw new IllegalArgumentException("Illegal button state");
				}
			}
		}
	}
}