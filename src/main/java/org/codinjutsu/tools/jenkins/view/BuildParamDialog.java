package org.codinjutsu.tools.jenkins.view;

import org.apache.commons.lang.StringUtils;
import org.codinjutsu.tools.jenkins.JenkinsConfiguration;
import org.codinjutsu.tools.jenkins.logic.JenkinsRequestManager;
import org.codinjutsu.tools.jenkins.model.Job;
import org.codinjutsu.tools.jenkins.model.JobParameter;
import org.codinjutsu.tools.jenkins.view.util.SpringUtilities;
import org.codinjutsu.tools.jenkins.view.validator.NotNullValidator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BuildParamDialog extends JDialog {
    public static final Color LIGHT_RED_BACKGROUND = new Color(230, 150, 150);
    public static final Color RED_BORDER = new Color(220, 0, 0);
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JPanel contentPanel;
    private JLabel feedbackLabel;

    private final Job job;
    private final JenkinsConfiguration configuration;
    private final JenkinsRequestManager jenkinsManager;
    private Map<JobParameter, JComponent> inputFieldByParameterMap = new HashMap<JobParameter, JComponent>();

    public BuildParamDialog(Job job, JenkinsConfiguration configuration, JenkinsRequestManager jenkinsManager) {
        this.job = job;
        this.configuration = configuration;
        this.jenkinsManager = jenkinsManager;

        addParameterInputs();
        setTitle("This build requires parameters");
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        feedbackLabel.setOpaque(true);

        registerListeners();
    }

    public static void showDialog(final Job job, final JenkinsConfiguration configuration, final JenkinsRequestManager jenkinsManager) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                BuildParamDialog dialog = new BuildParamDialog(job, configuration, jenkinsManager);
                dialog.setLocationRelativeTo(null);
                dialog.setPreferredSize(new Dimension(300, 200));
                dialog.pack();
                dialog.setVisible(true);
            }
        });
    }

    private void addParameterInputs() {
        contentPanel.setLayout(new SpringLayout());
        List<JobParameter> parameters = job.getParameters();

        for (JobParameter jobParameter : parameters) {
            JComponent inputField = createInputField(jobParameter);

            String name = jobParameter.getName();
            inputField.setName(name);

            JLabel label = new JLabel(name + ":", JLabel.TRAILING);
            label.setLabelFor(inputField);

            contentPanel.add(label);
            contentPanel.add(inputField);

            inputFieldByParameterMap.put(jobParameter, inputField);
        }

        SpringUtilities.makeCompactGrid(contentPanel,
                parameters.size(), 2,
                6, 6,        //initX, initY
                6, 6);       //xPad, yPad
    }

    private void registerListeners() {
        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private JComponent createInputField(JobParameter jobParameter) {

        JobParameter.JobParameterType jobParameterType = jobParameter.getJobParameterType();
        String defaultValue = jobParameter.getDefaultValue();
        JComponent inputField;

        if (JobParameter.JobParameterType.ChoiceParameterDefinition.equals(jobParameterType)) {
            inputField = createComboBox(jobParameter, defaultValue);
        } else if (JobParameter.JobParameterType.BooleanParameterDefinition.equals(jobParameterType)) {
            inputField = createCheckBox(defaultValue);
        } else if (JobParameter.JobParameterType.StringParameterDefinition.equals(jobParameterType)) {
            inputField = createTextField(defaultValue);
        } else {
            inputField = new JLabel("Unsupported ParameterDefinitionType: " + jobParameterType.name());
        }
        return inputField;
    }

    private void onOK() {
        try {
            checkInputValues();
            jenkinsManager.runParameterizedBuild(job, configuration, getParamValueMap());
            dispose();
        } catch (Exception e) {
            setErrorOnFeedbackPanel(e.getMessage());
        }
        //ajouter notification ici
    }

    private void onCancel() {//TODO corriger la notification
        dispose();
    }

    private JTextField createTextField(String defaultValue) {
        JTextField textField = new JTextField();
        if (StringUtils.isNotEmpty(defaultValue)) {
            textField.setText(defaultValue);
        }
        return textField;
    }

    private JCheckBox createCheckBox(String defaultValue) {
        JCheckBox checkBox = new JCheckBox();
        if (Boolean.TRUE.equals(Boolean.valueOf(defaultValue))) {
            checkBox.setSelected(true);
        }
        return checkBox;
    }

    private JComboBox createComboBox(JobParameter jobParameter, String defaultValue) {
        JComboBox comboBox = new JComboBox(jobParameter.getValues().toArray());
        if (StringUtils.isNotEmpty(defaultValue)) {
            comboBox.setSelectedItem(defaultValue);
        }
        return comboBox;
    }

    private void checkInputValues() throws Exception {

        NotNullValidator notNullValidator = new NotNullValidator();
        for (Map.Entry<JobParameter, JComponent> componentByJobParameterEntry : inputFieldByParameterMap.entrySet()) {
            JComponent component = componentByJobParameterEntry.getValue();
            if (component instanceof JTextField) {
                notNullValidator.validate((JTextField) component);
            }
        }
    }

    private Map<String, String> getParamValueMap() {//TODO transformer en visiteur
        HashMap<String, String> valueByNameMap = new HashMap<String, String>();
        for (Map.Entry<JobParameter, JComponent> inputFieldByParameter : inputFieldByParameterMap.entrySet()) {
            JobParameter jobParameter = inputFieldByParameter.getKey();
            String name = jobParameter.getName();
            JobParameter.JobParameterType jobParameterType = jobParameter.getJobParameterType();

            JComponent inputField = inputFieldByParameter.getValue();

            if (JobParameter.JobParameterType.ChoiceParameterDefinition.equals(jobParameterType)) {
                JComboBox comboBox = (JComboBox) inputField;
                valueByNameMap.put(name, String.valueOf(comboBox.getSelectedItem()));
            } else if (JobParameter.JobParameterType.BooleanParameterDefinition.equals(jobParameterType)) {
                JCheckBox checkBox = (JCheckBox) inputField;
                valueByNameMap.put(name, Boolean.toString(checkBox.isSelected()));
            } else if (JobParameter.JobParameterType.StringParameterDefinition.equals(jobParameterType)) {
                JTextField textField = (JTextField) inputField;
                valueByNameMap.put(name, textField.getText());
            }
        }
        return valueByNameMap;
    }

    private void setErrorOnFeedbackPanel(String message) {
        feedbackLabel.setText(message);
        feedbackLabel.setBackground(LIGHT_RED_BACKGROUND);
        feedbackLabel.setBorder(BorderFactory.createLineBorder(RED_BORDER));
    }
}