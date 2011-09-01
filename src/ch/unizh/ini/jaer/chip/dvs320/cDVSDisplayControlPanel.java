/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * cDVSDisplayControlPanel.java
 *
 * Created on Dec 5, 2010, 11:57:36 AM
 */

package ch.unizh.ini.jaer.chip.dvs320;

import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import net.sf.jaer.graphics.AEChipRenderer;
import org.jdesktop.beansbinding.Validator;

/**
 * Controls display of cDVS data.
 *
 * @author Tobi
 */
public class cDVSDisplayControlPanel extends javax.swing.JPanel implements PropertyChangeListener{

    private cDVSTest20DisplayMethod displayMethod=null;
    private cDVSTest20Renderer renderer=null;
    private cDVSTest20 chip;

    /** Creates new form cDVSDisplayControlPanel */
    public cDVSDisplayControlPanel(cDVSTest20 chip) {
        this.chip=chip;
        this.displayMethod=(cDVSTest20DisplayMethod)chip.getCanvas().getDisplayMethod();
        this.renderer=(cDVSTest20Renderer)chip.getRenderer();
        initComponents();
        renderer.getSupport().addPropertyChangeListener(AEChipRenderer.COLOR_SCALE, this);
        renderer.getSupport().addPropertyChangeListener(cDVSTest20Renderer.AGC_VALUES, this);
        renderer.getSupport().addPropertyChangeListener(cDVSTest20Renderer.LOG_INTENSITY_GAIN, this);
        renderer.getSupport().addPropertyChangeListener(cDVSTest20Renderer.LOG_INTENSITY_OFFSET, this);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        bindingGroup = new org.jdesktop.beansbinding.BindingGroup();

        displayControlPanel = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        logIntensityCB = new javax.swing.JCheckBox();
        invertADCvaluesCB = new javax.swing.JCheckBox();
        logIntenCalibPanel = new javax.swing.JPanel();
        offchipCalibCB = new javax.swing.JCheckBox();
        calibButton = new javax.swing.JButton();
        twoPointCalibCB = new javax.swing.JCheckBox();
        calibData2Button = new javax.swing.JButton();
        agcPanel = new javax.swing.JPanel();
        agcCB = new javax.swing.JCheckBox();
        jPanel5 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        agcSpinner = new javax.swing.JSpinner();
        applyButton = new javax.swing.JButton();
        logIntenStatPanel = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        gainAGCTF = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        maxTF = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        minTF = new javax.swing.JTextField();
        jPanel4 = new javax.swing.JPanel();
        gainLabel = new javax.swing.JLabel();
        gainSlider = new javax.swing.JSlider();
        gainTF = new javax.swing.JTextField();
        offsetLabel = new javax.swing.JLabel();
        offsetSlider = new javax.swing.JSlider();
        offTF = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        logIntensityChangeCB = new javax.swing.JCheckBox();
        colorChangeCB = new javax.swing.JCheckBox();
        colorScaleSpinner = new javax.swing.JSpinner();
        jLabel3 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jCheckBox1 = new javax.swing.JCheckBox();
        jLabel4 = new javax.swing.JLabel();
        jSpinner1 = new javax.swing.JSpinner();

        displayControlPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("cDVS Display"));
        displayControlPanel.setPreferredSize(new java.awt.Dimension(565, 300));

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Log intensity"));

        logIntensityCB.setText("Show log intensity data");
        logIntensityCB.setToolTipText("Shows the scanned out static log intensity values.");

        org.jdesktop.beansbinding.Binding binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, org.jdesktop.beansbinding.ELProperty.create("${displayMethod.displayLogIntensity}"), logIntensityCB, org.jdesktop.beansbinding.BeanProperty.create("selected"));
        bindingGroup.addBinding(binding);

        invertADCvaluesCB.setText("Invert ADC values");

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, org.jdesktop.beansbinding.ELProperty.create("${chip.frameData.invertADCvalues}"), invertADCvaluesCB, org.jdesktop.beansbinding.BeanProperty.create("selected"));
        bindingGroup.addBinding(binding);

        logIntenCalibPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Calibration"));

        offchipCalibCB.setText("Off-chip");
        offchipCalibCB.setToolTipText("Use off-chip calibration");

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, org.jdesktop.beansbinding.ELProperty.create("${chip.useOffChipCalibration}"), offchipCalibCB, org.jdesktop.beansbinding.BeanProperty.create("selected"));
        bindingGroup.addBinding(binding);

        calibButton.setText("Store dark");
        calibButton.setToolTipText("Stores teh calibration frame; use while sensor looks at a unifform dark scene");
        calibButton.setMargin(new java.awt.Insets(0, 2, 2, 2));
        calibButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                calibButtonActionPerformed(evt);
            }
        });

        twoPointCalibCB.setText("Two-point");
        twoPointCalibCB.setToolTipText("Use two-point (dark+bright) calibration");

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, org.jdesktop.beansbinding.ELProperty.create("${chip.frameData.twoPointCalibration}"), twoPointCalibCB, org.jdesktop.beansbinding.BeanProperty.create("selected"));
        bindingGroup.addBinding(binding);

        twoPointCalibCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                twoPointCalibCBActionPerformed(evt);
            }
        });

        calibData2Button.setText("Store bright");
        calibData2Button.setToolTipText("Stores the white calibration frame; use while sensor looks at uniform bright scene");
        calibData2Button.setMargin(new java.awt.Insets(2, 2, 2, 2));
        calibData2Button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                calibData2ButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout logIntenCalibPanelLayout = new javax.swing.GroupLayout(logIntenCalibPanel);
        logIntenCalibPanel.setLayout(logIntenCalibPanelLayout);
        logIntenCalibPanelLayout.setHorizontalGroup(
            logIntenCalibPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(logIntenCalibPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(logIntenCalibPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(offchipCalibCB)
                    .addComponent(twoPointCalibCB))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(logIntenCalibPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(calibButton)
                    .addComponent(calibData2Button))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        logIntenCalibPanelLayout.setVerticalGroup(
            logIntenCalibPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(logIntenCalibPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                .addGroup(logIntenCalibPanelLayout.createSequentialGroup()
                    .addComponent(calibButton)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(calibData2Button))
                .addGroup(logIntenCalibPanelLayout.createSequentialGroup()
                    .addComponent(offchipCalibCB)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(twoPointCalibCB)))
        );

        agcPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("AGC"));
        agcPanel.setLayout(new javax.swing.BoxLayout(agcPanel, javax.swing.BoxLayout.LINE_AXIS));

        agcCB.setText("Use AGC");
        agcCB.setToolTipText("Activates Automagitc Gain Control for log intensity display");

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, org.jdesktop.beansbinding.ELProperty.create("${renderer.agcEnabled}"), agcCB, org.jdesktop.beansbinding.BeanProperty.create("selected"));
        bindingGroup.addBinding(binding);

        agcPanel.add(agcCB);

        jPanel5.setMinimumSize(new java.awt.Dimension(0, 0));
        jPanel5.setPreferredSize(new java.awt.Dimension(5, 0));

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 19, Short.MAX_VALUE)
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 23, Short.MAX_VALUE)
        );

        agcPanel.add(jPanel5);

        jLabel1.setText("AGC time constant (ms)");
        agcPanel.add(jLabel1);

        agcSpinner.setModel(new javax.swing.SpinnerNumberModel(Float.valueOf(1000.0f), Float.valueOf(10.0f), null, Float.valueOf(100.0f)));
        agcSpinner.setToolTipText("Set time constant in ms for AGC");

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, org.jdesktop.beansbinding.ELProperty.create("${renderer.AGCTauMs}"), agcSpinner, org.jdesktop.beansbinding.BeanProperty.create("value"));
        bindingGroup.addBinding(binding);

        agcPanel.add(agcSpinner);

        applyButton.setText("Apply");
        applyButton.setToolTipText("Apply AGC offset and gain values to fixed values");
        applyButton.setIconTextGap(1);
        applyButton.setMargin(new java.awt.Insets(1, 3, 1, 3));
        applyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                applyButtonActionPerformed(evt);
            }
        });
        agcPanel.add(applyButton);

        logIntenStatPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("statistics"));
        logIntenStatPanel.setLayout(new javax.swing.BoxLayout(logIntenStatPanel, javax.swing.BoxLayout.LINE_AXIS));

        jLabel2.setText("min (offset)");
        logIntenStatPanel.add(jLabel2);

        gainAGCTF.setColumns(4);
        gainAGCTF.setEditable(false);
        gainAGCTF.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        gainAGCTF.setText("4095");
        gainAGCTF.setToolTipText("gain as computed by AGC");
        logIntenStatPanel.add(gainAGCTF);

        jLabel5.setText("max");
        logIntenStatPanel.add(jLabel5);

        maxTF.setColumns(4);
        maxTF.setEditable(false);
        maxTF.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        maxTF.setText("4095");
        maxTF.setToolTipText("lowpass max log intensity value");
        logIntenStatPanel.add(maxTF);

        jLabel6.setText("gain");
        logIntenStatPanel.add(jLabel6);

        minTF.setColumns(4);
        minTF.setEditable(false);
        minTF.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        minTF.setText("0");
        minTF.setToolTipText("low pass min log intensity value");
        logIntenStatPanel.add(minTF);

        gainLabel.setText("gain");

        gainSlider.setMaximum(cDVSTest20.MAX_ADC/100);
        gainSlider.setToolTipText("Sets the gain applied to ADC count. Gain=1 scales full count to white when offset=0. Gain=MAX_ADC scales a single count to full white when offset=0.");

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, org.jdesktop.beansbinding.ELProperty.create("${logIntensityGain}"), gainSlider, org.jdesktop.beansbinding.BeanProperty.create("value"));
        bindingGroup.addBinding(binding);

        gainTF.setColumns(6);
        gainTF.setHorizontalAlignment(javax.swing.JTextField.TRAILING);

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, gainSlider, org.jdesktop.beansbinding.ELProperty.create("${value}"), gainTF, org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);

        offsetLabel.setText("offset");

        offsetSlider.setMaximum(cDVSTest20.MAX_ADC);
        offsetSlider.setToolTipText("Sets the offset subtracted from ADC count. Gain=1 scales full count to white when offset=0. Offset shifts black point to offset count value.");

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, org.jdesktop.beansbinding.ELProperty.create("${logIntensityOffset}"), offsetSlider, org.jdesktop.beansbinding.BeanProperty.create("value"));
        bindingGroup.addBinding(binding);

        offTF.setColumns(6);
        offTF.setHorizontalAlignment(javax.swing.JTextField.TRAILING);

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, offsetSlider, org.jdesktop.beansbinding.ELProperty.create("${value}"), offTF, org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(offsetLabel)
                        .addComponent(offsetSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(gainLabel)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(gainSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addGap(204, 204, 204)
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(offTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(gainTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))))
                .addContainerGap(15, Short.MAX_VALUE))
        );

        jPanel4Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {gainLabel, offsetLabel});

        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(gainLabel)
                    .addComponent(gainSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(gainTF, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(offsetLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(offsetSlider, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(offTF, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        jPanel4Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {gainLabel, gainSlider, gainTF});

        jPanel4Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {offTF, offsetLabel, offsetSlider});

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(logIntenStatPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(logIntenCalibPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(logIntensityCB)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(invertADCvaluesCB))
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(jPanel4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(agcPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 302, Short.MAX_VALUE)))
                .addContainerGap(62, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(logIntensityCB, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(invertADCvaluesCB))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(agcPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(logIntenStatPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(logIntenCalibPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Events"));

        logIntensityChangeCB.setText("Show log intensity change events");
        logIntensityChangeCB.setToolTipText("Show log intensity change (temporal contrast) Brighter and Darker events.");

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, org.jdesktop.beansbinding.ELProperty.create("${displayMethod.displayLogIntensityChangeEvents}"), logIntensityChangeCB, org.jdesktop.beansbinding.BeanProperty.create("selected"));
        bindingGroup.addBinding(binding);

        colorChangeCB.setText("Show color change events");
        colorChangeCB.setToolTipText("Shows color change events, Redder and Bluer");

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, org.jdesktop.beansbinding.ELProperty.create("${displayMethod.displayColorChangeEvents}"), colorChangeCB, org.jdesktop.beansbinding.BeanProperty.create("selected"));
        bindingGroup.addBinding(binding);

        colorScaleSpinner.setToolTipText("Sets the full scale (white or black) event count.");

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, org.jdesktop.beansbinding.ELProperty.create("${colorScale}"), colorScaleSpinner, org.jdesktop.beansbinding.BeanProperty.create("value"), "colorScale");
        binding.setValidator(valid);
        bindingGroup.addBinding(binding);

        jLabel3.setText("Full scale events");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(logIntensityChangeCB)
                    .addComponent(colorChangeCB)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(colorScaleSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(6, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(logIntensityChangeCB, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(colorChangeCB, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(colorScaleSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3)))
        );

        jPanel2Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {colorChangeCB, logIntensityChangeCB});

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Global intensity"));

        jCheckBox1.setText("Show global intensity");
        jCheckBox1.setToolTipText("Shows the global sum photocurrent value.");

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, org.jdesktop.beansbinding.ELProperty.create("${displayMethod.intensityDisplayEnabled}"), jCheckBox1, org.jdesktop.beansbinding.BeanProperty.create("selected"));
        bindingGroup.addBinding(binding);

        jLabel4.setText("Global intensity scale");

        jSpinner1.setModel(new javax.swing.SpinnerNumberModel(Float.valueOf(1.0f), Float.valueOf(0.1f), Float.valueOf(10.0f), Float.valueOf(0.1f)));
        jSpinner1.setToolTipText("Scales ISI so that scale=1 shows 1ms ISI as full scale.");

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, org.jdesktop.beansbinding.ELProperty.create("${displayMethod.intensityScale}"), jSpinner1, org.jdesktop.beansbinding.BeanProperty.create("value"));
        bindingGroup.addBinding(binding);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jCheckBox1)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jSpinner1, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(45, 45, 45))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jCheckBox1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jSpinner1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(18, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout displayControlPanelLayout = new javax.swing.GroupLayout(displayControlPanel);
        displayControlPanel.setLayout(displayControlPanelLayout);
        displayControlPanelLayout.setHorizontalGroup(
            displayControlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(displayControlPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(displayControlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, 239, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        displayControlPanelLayout.setVerticalGroup(
            displayControlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(displayControlPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(displayControlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(displayControlPanelLayout.createSequentialGroup()
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        displayControlPanelLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {jPanel2, jPanel3});

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(displayControlPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 755, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(51, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(displayControlPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 291, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        bindingGroup.bind();
    }// </editor-fold>//GEN-END:initComponents

    private void applyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_applyButtonActionPerformed
        renderer.applyAGCValues();
    }//GEN-LAST:event_applyButtonActionPerformed

    private void calibButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_calibButtonActionPerformed
        if(chip==null || chip.getFrameData()==null) return;
        chip.getFrameData().setCalibData1();
    }//GEN-LAST:event_calibButtonActionPerformed

    private void twoPointCalibCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_twoPointCalibCBActionPerformed
        // TODO add your handling code here:
        if(chip==null || chip.getFrameData()==null) return;
        chip.getFrameData().calculateCalibration();
    }//GEN-LAST:event_twoPointCalibCBActionPerformed

    private void calibData2ButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_calibData2ButtonActionPerformed
         if(chip==null || chip.getFrameData()==null) return;
        chip.getFrameData().setCalibData2();
    }//GEN-LAST:event_calibData2ButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox agcCB;
    private javax.swing.JPanel agcPanel;
    private javax.swing.JSpinner agcSpinner;
    private javax.swing.JButton applyButton;
    private javax.swing.JButton calibButton;
    private javax.swing.JButton calibData2Button;
    private javax.swing.JCheckBox colorChangeCB;
    private javax.swing.JSpinner colorScaleSpinner;
    private javax.swing.JPanel displayControlPanel;
    private javax.swing.JTextField gainAGCTF;
    private javax.swing.JLabel gainLabel;
    private javax.swing.JSlider gainSlider;
    private javax.swing.JTextField gainTF;
    private javax.swing.JCheckBox invertADCvaluesCB;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JSpinner jSpinner1;
    private javax.swing.JPanel logIntenCalibPanel;
    private javax.swing.JPanel logIntenStatPanel;
    private javax.swing.JCheckBox logIntensityCB;
    private javax.swing.JCheckBox logIntensityChangeCB;
    private javax.swing.JTextField maxTF;
    private javax.swing.JTextField minTF;
    private javax.swing.JTextField offTF;
    private javax.swing.JCheckBox offchipCalibCB;
    private javax.swing.JLabel offsetLabel;
    private javax.swing.JSlider offsetSlider;
    private javax.swing.JCheckBox twoPointCalibCB;
    private org.jdesktop.beansbinding.BindingGroup bindingGroup;
    // End of variables declaration//GEN-END:variables

    /**
     * @return the displayMethod
     */
    public cDVSTest20DisplayMethod getDisplayMethod() {
        return displayMethod;
    }

    /**
     * @param displayMethod the displayMethod to set
     */
    public void setDisplayMethod(cDVSTest20DisplayMethod displayMethod) {
        this.displayMethod = displayMethod;
    }

    /**
     * @return the chip
     */
    public cDVSTest20 getChip() {
        return chip;
    }

    /**
     * @param chip the chip to set
     */
    public void setChip(cDVSTest20 chip) {
        this.chip = chip;
    }

    public void setLogIntensityOffset(int logIntensityOffset) {
        renderer.setLogIntensityOffset(logIntensityOffset);
    }

    public void setLogIntensityGain(int logIntensityGain) {
        renderer.setLogIntensityGain(logIntensityGain);
    }

    public int getLogIntensityOffset() {
        return (int)(renderer.getLogIntensityOffset());
    }

    public int getLogIntensityGain() {
        return (int)(renderer.getLogIntensityGain());
    }


    public int getColorScale(){
        if(chip==null || chip.getRenderer()==null) return 1;
        return chip.getRenderer().getColorScale();
    }

    public void setColorScale(int s){
        if(s<1) s=1;
         if (chip == null || chip.getRenderer() == null) {
            return;
        }
        chip.getRenderer().setColorScale(s);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if(evt.getPropertyName()==AEChipRenderer.COLOR_SCALE){
            colorScaleSpinner.setValue((Integer)evt.getNewValue());
        }else if(evt.getPropertyName()==cDVSTest20Renderer.AGC_VALUES){
            Point2D.Float f=(Point2D.Float)evt.getNewValue();
            minTF.setText(String.format("%.0f",f.x));
            maxTF.setText(String.format("%.0f",f.y));
            gainAGCTF.setText(String.format("%.0f",cDVSTest20.MAX_ADC/(f.y-f.x)));
        }else if(evt.getPropertyName()==cDVSTest20Renderer.LOG_INTENSITY_GAIN){
            gainSlider.setValue(renderer.getLogIntensityGain());
        }else if(evt.getPropertyName()==cDVSTest20Renderer.LOG_INTENSITY_OFFSET){
            offsetSlider.setValue(renderer.getLogIntensityOffset());
        }
    }

    /**
     * @return the renderer
     */
    public cDVSTest20Renderer getRenderer() {
        return renderer;
    }

    private class Valid extends Validator{

        @Override
        public Validator.Result validate(Object t) {
            if(!(t instanceof Integer) || (Integer)t<1){
                return new Result(null,"color scale must be >0"); // TODO does nothing!
            }
            return null;
        }

    }

    private Valid valid=new Valid();


}
