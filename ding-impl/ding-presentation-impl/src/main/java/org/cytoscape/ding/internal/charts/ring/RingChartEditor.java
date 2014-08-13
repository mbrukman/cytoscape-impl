package org.cytoscape.ding.internal.charts.ring;

import java.awt.Dimension;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.ding.internal.charts.AbstractChartEditor;
import org.cytoscape.ding.internal.util.IconManager;
import org.cytoscape.view.presentation.property.values.CyColumnIdentifierFactory;

public class RingChartEditor extends AbstractChartEditor<RingChart> {

	private static final long serialVersionUID = -1867268965571724061L;
	
	private JLabel startAngleLbl;
	private JTextField startAngleTxt;
	private JLabel holeLbl;
	private JTextField holeTxt;
	
	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public RingChartEditor(final RingChart chart, final CyApplicationManager appMgr, final IconManager iconMgr,
			final CyColumnIdentifierFactory colIdFactory) {
		super(chart, Number.class, false, 5, false, false, true, false, false, false, appMgr, iconMgr, colIdFactory);
	}
	
	// ==[ PUBLIC METHODS ]=============================================================================================

	// ==[ PRIVATE METHODS ]============================================================================================
	
	@Override
	protected void createLabels() {
		super.createLabels();
		startAngleLbl = new JLabel("Start Angle (degrees)");
		holeLbl = new JLabel("Hole Size (0.0-1.0)");
	}
	
	@Override
	protected JPanel getOtherAdvancedOptionsPnl() {
		final JPanel p = super.getOtherAdvancedOptionsPnl();
		p.setVisible(true);
		
		final GroupLayout layout = new GroupLayout(p);
		p.setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(Alignment.TRAILING, false)
					.addComponent(startAngleLbl)
					.addComponent(holeLbl))
				.addGroup(layout.createParallelGroup(Alignment.TRAILING, true)
					.addComponent(getStartAngleTxt(), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
							GroupLayout.PREFERRED_SIZE)
					.addComponent(getHoleTxt(), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
							GroupLayout.PREFERRED_SIZE))
				);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addComponent(startAngleLbl)
						.addComponent(getStartAngleTxt()))
				.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addComponent(holeLbl)
						.addComponent(getHoleTxt()))
				);
		
		return p;
	}
	
	private JTextField getStartAngleTxt() {
		if (startAngleTxt == null) {
			startAngleTxt = new JTextField("" + chart.get(RingChart.START_ANGLE, Double.class, 0.0));
			startAngleTxt.setToolTipText(
					"Starting from 3 o'clock and measuring clockwise (90\u00B0 = 6 o'clock)");
			startAngleTxt.setInputVerifier(new DoubleInputVerifier());
			startAngleTxt.setPreferredSize(new Dimension(60, startAngleTxt.getMinimumSize().height));
			startAngleTxt.setHorizontalAlignment(JTextField.TRAILING);
			
			startAngleTxt.addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(final FocusEvent e) {
					try {
			            double angle = Double.valueOf(startAngleTxt.getText().trim()).doubleValue();
			            chart.set(RingChart.START_ANGLE, angle);
			        } catch (NumberFormatException ex) {
			        }
				}
			});
		}
		
		return startAngleTxt;
	}
	
	public JTextField getHoleTxt() {
		if (holeTxt == null) {
			holeTxt = new JTextField("" + chart.get(RingChart.HOLE_SIZE, Double.class, 0.4));
			holeTxt.setToolTipText("Diameter of the ring hole, as a proportion of the entire plot");
			holeTxt.setInputVerifier(new DoubleInputVerifier());
			holeTxt.setPreferredSize(new Dimension(60, holeTxt.getMinimumSize().height));
			holeTxt.setHorizontalAlignment(JTextField.TRAILING);
			
			holeTxt.addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(final FocusEvent e) {
					try {
			            double angle = Double.valueOf(holeTxt.getText().trim()).doubleValue();
			            chart.set(RingChart.HOLE_SIZE, angle);
			        } catch (NumberFormatException ex) {
			        }
				}
			});
		}
		
		return holeTxt;
	}
}