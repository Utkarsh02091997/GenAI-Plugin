package org.intellij.sdk.toolWindow;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.Objects;

public class CalendarToolWindowFactory implements ToolWindowFactory, DumbAware {

    private Editor editor;
    private JTextArea textArea1;
    private JComboBox<String> fileListComboBox;
    private Utils utils = new Utils();
    private Project project;
    private JTextArea migrateResultTextArea;
    private JTextArea generateResultTextArea;
    private JComboBox<String> fileComboBox;
    private JComboBox<String> targetComboBox;
    private JComboBox<String> sourceComboBox;

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        SwingUtilities.invokeLater(() -> {
            editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
            this.project = project;
            System.out.println("editor = " + editor);
            CalendarToolWindowContent toolWindowContent = new CalendarToolWindowContent(toolWindow);
            if (Objects.isNull(editor)) {
                editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
            }
            System.out.println("summary triggered");

            Content content = ContentFactory.getInstance().createContent(toolWindowContent.getContentPanel(), "", false);
            toolWindow.getContentManager().addContent(content);

        });

    }

    private class CalendarToolWindowContent {

        private final String CALENDAR_ICON_PATH = "/toolWindow/Calendar-icon.png";
        private final String TIME_ZONE_ICON_PATH = "/toolWindow/Time-zone-icon.png";
        private final String TIME_ICON_PATH = "/toolWindow/Time-icon.png";

        private final JPanel contentPanel = new JPanel();
        private final JLabel currentDate = new JLabel();
        private final JLabel timeZone = new JLabel();
        private final JLabel currentTime = new JLabel();

        public CalendarToolWindowContent(ToolWindow toolWindow) {

            contentPanel.setMinimumSize(new Dimension(790, 290));
            contentPanel.setMaximumSize(new Dimension(790, 290));
            contentPanel.setLayout(new BorderLayout());

            SwingUtilities.invokeLater(() -> {
                System.out.println("calender tool window");

                JScrollPane scrollPane = new JScrollPane(createTabUI(toolWindow));
                scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);


                contentPanel.add(scrollPane, BorderLayout.CENTER);
                //  runSummaryEvent();
            });
//            contentPanel.add(createControlsPanel(toolWindow), BorderLayout.PAGE_START);
        }

        private JTabbedPane createTabUI(ToolWindow toolWindow) {
            JTabbedPane tabbedPane = new JTabbedPane();

            // Create tabs with labels
            JPanel tab1 = new JPanel();
            JPanel tab2 = new JPanel();
            JPanel tab3 = new JPanel();
//            ImageIcon loading = new ImageIcon(getClass().getResource("/toolWindow/icons8-loading-circle.gif"));

            tab1.add(createControlsPanel(toolWindow));
            tab2.add(createCodeMigrationPanel());
            tab3.add(createTestCasePanel());

            // Add the tabs to the tabbed pane
            tabbedPane.addTab("Code Summary", tab1);
            tabbedPane.addTab("Code Migration", tab2);
            tabbedPane.addTab("Test Suite", tab3);
            return tabbedPane;

        }

        private JPanel createTestCasePanel() {
            JPanel testCasePanel = new JPanel();
            testCasePanel.setMinimumSize(new Dimension(790, 290));
            testCasePanel.setMaximumSize(new Dimension(790, 290));
            testCasePanel.setLayout(new BorderLayout());

            JPanel contentPanel = new JPanel();
            contentPanel.setLayout(new GridBagLayout());
            GridBagConstraints constraints = new GridBagConstraints();


            JCheckBox useFileCheckbox = new JCheckBox("Select File");
            constraints.gridx = 0;
            constraints.gridy = 0;
            constraints.anchor = GridBagConstraints.WEST;
            contentPanel.add(useFileCheckbox, constraints);


            fileComboBox = new JComboBox<String>();
            utils.populateFileList(fileComboBox, project);
            constraints.gridx = 0;
            constraints.gridy = 1;
            constraints.anchor = GridBagConstraints.WEST;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            contentPanel.add(fileComboBox, constraints);
            fileComboBox.setVisible(false);

            JTextArea textArea = new JTextArea(10, 10);
            useFileCheckbox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    fileComboBox.setVisible(useFileCheckbox.isSelected());
                    textArea.setEnabled(!useFileCheckbox.isSelected());
                }
            });

            JLabel label1 = new JLabel("Input");
            constraints.gridx = 0;
            constraints.gridy = 2;
            constraints.anchor = GridBagConstraints.WEST;
            contentPanel.add(label1, constraints);


            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            textArea.setEditable(true);
            JScrollPane scrollPaneTA = new JBScrollPane(textArea);
            scrollPaneTA.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            constraints.gridx = 0;
            constraints.gridy = 3;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            contentPanel.add(scrollPaneTA, constraints);

            JButton testCaseBtn = new JButton("Generate TestCase");
            constraints.gridx = 0;
            constraints.gridy = 4;
            constraints.insets = new Insets(5, 5, 0, 0);
            constraints.fill = GridBagConstraints.WEST;
            contentPanel.add(testCaseBtn, constraints);

            JTextArea testCasetextArea = new JTextArea(25, 15);
            testCasetextArea.setLineWrap(true);
            testCasetextArea.setWrapStyleWord(true);
            testCasetextArea.setEditable(true);
// Create a JScrollPane for the JTextArea
            JScrollPane scrollPane = new JBScrollPane(testCasetextArea);
            scrollPane.setPreferredSize(new Dimension(400, 300));
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            constraints.gridx = 0;
            constraints.gridy = 5;
            constraints.insets = new Insets(5, 5, 0, 0);
            constraints.fill = GridBagConstraints.BOTH;
            contentPanel.add(scrollPane, constraints);
            //button
            JButton saveAsBtn = new JButton("Save As");
            constraints.gridx = 0;
            constraints.gridy = 6;
            constraints.insets = new Insets(5, 5, 0, 0);
            constraints.fill = GridBagConstraints.WEST;
            contentPanel.add(saveAsBtn, constraints);

            JPanel loadingTestPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            ImageIcon loading = new ImageIcon(getClass().getResource("/toolWindow/icons8-loading-circle.gif"));
            JLabel loadTestLabel = new JLabel("Loading... ", loading, JLabel.CENTER);
            loadTestLabel.setVisible(false);
            loadingTestPanel.add(loadTestLabel);


            testCasePanel.add(contentPanel, BorderLayout.CENTER);
            testCasePanel.add(loadingTestPanel, BorderLayout.SOUTH);


            testCaseBtn.addActionListener(new ActionListener() {
                String userInput = "";
                Boolean isFromFile = true;

                @Override
                public void actionPerformed(ActionEvent e) {

                    loadTestLabel.setVisible(true);
                    if (Objects.isNull(editor)) {
                        editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
                    }

                    if (useFileCheckbox.isSelected()) {
                        String selectedFileName = (String) fileComboBox.getSelectedItem();
                        utils.selectAndReadFileFromComboBox(selectedFileName);
                        String fileData = utils.readFileFromProject();
                        userInput = fileData;
                        isFromFile = true;
//                        if (Objects.nonNull(textArea.getText()) && !textArea.getText().isEmpty()) {
//                            systemInput = textArea.getText();
//                        } else {
//                            systemInput = null;
//                        }
                    } else {
                        String selectedText = editor.getSelectionModel().getSelectedText();
                        isFromFile = false;
                        if (Objects.nonNull(textArea.getText()) && !textArea.getText().isEmpty()) {
                            userInput = textArea.getText();
                        } else if (Objects.nonNull(selectedText) && !selectedText.equals("")) {
                            userInput = selectedText;
                        } else {
                            String fileContent = getFileContent(editor);
                            System.out.println("fileContent - " + fileContent);
                            if (Objects.nonNull(fileContent) && !fileContent.equals("")) {
                                userInput = fileContent;
                            } else {
                                userInput = null;
                            }
                        }
                    }

                    // Use a SwingWorker to perform the test case generation in the background
                    SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
                        @Override
                        protected String doInBackground() throws Exception {
                            String genTestCase = utils.testCaseGeneration(userInput, isFromFile);
                            return genTestCase;
                        }

                        @Override
                        protected void done() {
                            try {
                                String genTestCase = get();
                                testCasetextArea.setText(genTestCase);
                                loadTestLabel.setVisible(false);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                loadTestLabel.setVisible(false);
                                // Handle any exceptions that occur during test case generation
                            }
                        }
                    };

                    worker.execute(); // Start the SwingWorker
                }
            });
            saveAsBtn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    saveToFile(true);
                }
            });

            return testCasePanel;

        }

        private JPanel createCodeMigrationPanel() {
            JPanel codePanel = new JPanel();
            codePanel.setMinimumSize(new Dimension(790, 290));
            codePanel.setMaximumSize(new Dimension(790, 290));
            codePanel.setLayout(new BorderLayout());

            // Create a panel for the main content (excluding loading)
            JPanel contentPanel = new JPanel();
            contentPanel.setLayout(new GridBagLayout());
            GridBagConstraints constraints = new GridBagConstraints();

            JPanel radioPanel = new JPanel();
            radioPanel.setLayout(new GridBagLayout());

//            GridBagConstraints radioconstraints = new GridBagConstraints();

            JPanel sourceTargetPanel = new JPanel();
            sourceTargetPanel.setLayout(new GridBagLayout());
            constraints.insets = new Insets(0, 10, 0, 0);

            JLabel label1 = new JLabel("Source");
            constraints.gridx = 0;
            constraints.gridy = 0;
//            constraints.anchor = GridBagConstraints.WEST;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            sourceTargetPanel.add(label1, constraints);

            String[] sourceItems = {"Struts", "EJB", "MyBatis", "Axis", "Hibernate", "Servlet-JSP", "Spring MVC"};
            sourceComboBox = new JComboBox<>(sourceItems);
            constraints.gridx = 1;
            constraints.gridy = 0;
//            constraints.anchor = GridBagConstraints.WEST;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            sourceComboBox.setPreferredSize(new Dimension(150, 30));
            sourceTargetPanel.add(sourceComboBox, constraints);

            JLabel label2 = new JLabel("Target");
            constraints.gridx = 2;
            constraints.gridy = 0;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            sourceTargetPanel.add(label2, constraints);

            String[] targetItems = {"Spring MVC", "Spring MVC Thymeleaf", "Spring Data JPA", "Spring MVC REST API"};
            targetComboBox = new JComboBox<>(targetItems);
            constraints.gridx = 3; // Place it next to the first ComboBox
            constraints.gridy = 0;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            targetComboBox.setPreferredSize(new Dimension(150, 30));
            sourceTargetPanel.add(targetComboBox, constraints);

            constraints.gridx = 0;
            constraints.gridy = 0;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            contentPanel.add(sourceTargetPanel, constraints);

            // Create a radio button group
            ButtonGroup radioButtonGroup = new ButtonGroup();
            JRadioButton fileRadioButton = new JRadioButton("Select File");
            JRadioButton manualRadioButton = new JRadioButton("Manual Input");

            // Add radio buttons to the group
            radioButtonGroup.add(fileRadioButton);
            radioButtonGroup.add(manualRadioButton);
            fileRadioButton.setSelected(true);

            JPanel radioButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            radioButtonPanel.add(fileRadioButton);
            radioButtonPanel.add(manualRadioButton);

            constraints.gridx = 0;
            constraints.gridy = 1;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            contentPanel.add(radioButtonPanel, constraints);


            // Create a JComboBox for file selection
            fileListComboBox = new JComboBox<>();
            utils.populateFileList(fileListComboBox, project);

            // Create a JTextArea for manual input
            JTextArea manualInputTextArea = new JTextArea(10, 40);
            manualInputTextArea.setLineWrap(true);
            manualInputTextArea.setWrapStyleWord(true);
            manualInputTextArea.setEditable(true);

            JScrollPane manualScrollPane = new JBScrollPane(manualInputTextArea);
            manualScrollPane.setPreferredSize(new Dimension(400, 200));
            manualScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            constraints.gridy = 2;
            constraints.fill = GridBagConstraints.BOTH;
            manualScrollPane.setVisible(false);
            manualScrollPane.setEnabled(true);
            contentPanel.add(manualScrollPane, constraints);

            // Add components based on radio button selection
            constraints.gridx = 0;
            constraints.gridy = 2;
            constraints.weighty = 0.0;
//            constraints.insets = new Insets(5, 5, 0, 0);
            constraints.fill = GridBagConstraints.HORIZONTAL;
            contentPanel.add(fileListComboBox, constraints);


            // Create a JButton for migration
            JButton migrateBtn = new JButton();
            constraints.gridx = 0;
            constraints.gridy = 3;
            migrateBtn.setText("Migrate");
//            constraints.insets = new Insets(5, 5, 0, 0);
            constraints.fill = GridBagConstraints.WEST;
            contentPanel.add(migrateBtn, constraints);

            migrateResultTextArea = new JTextArea(55, 15);
            migrateResultTextArea.setLineWrap(true);
            migrateResultTextArea.setWrapStyleWord(true);
            migrateResultTextArea.setEditable(true);

            JScrollPane scrollPane = new JBScrollPane(migrateResultTextArea);
            scrollPane.setPreferredSize(new Dimension(400, 400));
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            constraints.gridx = 0;
            constraints.gridy = 4;
            constraints.insets = new Insets(5, 5, 0, 0);
            constraints.fill = GridBagConstraints.BOTH;
            contentPanel.add(scrollPane, constraints);

            generateResultTextArea = new JTextArea(55, 15);
            generateResultTextArea.setLineWrap(true);
            generateResultTextArea.setWrapStyleWord(true);
            generateResultTextArea.setEditable(false);

            JScrollPane generateScrollPane = new JBScrollPane(generateResultTextArea);
            generateScrollPane.setPreferredSize(new Dimension(400, 400));
            generateScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            constraints.gridx = 0;
            constraints.gridy = 4;
            constraints.insets = new Insets(5, 5, 0, 0);
            constraints.fill = GridBagConstraints.BOTH;
            contentPanel.add(generateScrollPane, constraints);

            JButton acceptBtn = new JButton("<< Accept Changes");
            acceptBtn.setEnabled(false);
            constraints.gridx = 0;
            constraints.gridy = 5;
//            constraints.insets = new Insets(5, 5, 0, 0);
            constraints.fill = GridBagConstraints.EAST;
            contentPanel.add(acceptBtn, constraints);

            Icon icon = UIManager.getIcon("Tree.leafIcon");
            JButton copyCodeBtn = new JButton("Copy Selected Text", icon);
            copyCodeBtn.setVisible(false);
            constraints.gridx = 0;
            constraints.gridy = 5;
//            constraints.insets = new Insets(5, 5, 0, 0);
            constraints.fill = GridBagConstraints.EAST;
            contentPanel.add(copyCodeBtn, constraints);

            copyCodeBtn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String selectedText = generateResultTextArea.getSelectedText();
                    if (selectedText != null && !selectedText.isEmpty()) {
                        StringSelection selection = new StringSelection(selectedText);
                        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                        clipboard.setContents(selection, null);
                        JOptionPane.showMessageDialog(null, "Selected Data copied to Clipboard");
                    } else {
                        JOptionPane.showMessageDialog(null, "Please select any text!!");
                    }
                }
            });

            acceptBtn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (editor != null) {
                        String newText = migrateResultTextArea.getText();
                        if (newText.isEmpty()) {
                            JOptionPane.showMessageDialog(null, "No Value present!!");
                            return;
                        }
                        String selectedFileName = (String) fileListComboBox.getSelectedItem();
                        utils.selectAndReadFileFromComboBox(selectedFileName);
                        try (FileOutputStream fos = new FileOutputStream(utils.selectedFilePath)) {
                            byte[] bytes = newText.getBytes();
                            fos.write(bytes);

                            JOptionPane.showMessageDialog(null, "File updated successfully.");
                        } catch (IOException ee) {
                            ee.printStackTrace();
                            System.err.println("Error writing to the file: " + ee.getMessage());
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "No editor is open to accept changes.");
                    }
                }
            });


            JPanel loadingPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            ImageIcon loading = new ImageIcon(getClass().getResource("/toolWindow/icons8-loading-circle.gif"));
            JLabel loadLabel = new JLabel("Loading... ", loading, JLabel.CENTER);
            loadLabel.setVisible(false);
            loadingPanel.add(loadLabel);

            codePanel.add(contentPanel, BorderLayout.CENTER);
            codePanel.add(loadingPanel, BorderLayout.SOUTH);

            // Add action listener for the Migrate button
            migrateBtn.addActionListener(e -> {
                if (Objects.isNull(editor)) {
                    editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
                }
                if (fileRadioButton.isSelected()) {
                    migrateResultTextArea.setText("");
                    migrateResultTextArea.setEditable(true);
                    String selectedFileName = (String) fileListComboBox.getSelectedItem();
                    utils.selectAndReadFileFromComboBox(selectedFileName);
                } else if (manualRadioButton.isSelected()) {
                    String manualInput = manualInputTextArea.getText();
                    // Perform migration based on manual input
                    // utils.migrateFileToSpringBoot(manualInput, migrateResultTextArea);
                }


                loadLabel.setVisible(true);

                new SwingWorker<Void, String>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                        String sourceInput = sourceComboBox.getSelectedItem().toString();
                        String targetInput = targetComboBox.getSelectedItem().toString();

                        if (fileRadioButton.isSelected()) {
                            System.out.println("coming here");
                            String fileData = utils.readFileFromProject();
                            utils.migrateFileToSpringBoot(fileData, migrateResultTextArea, sourceInput, targetInput);
                            acceptBtn.setEnabled(true);
                        } else if (manualRadioButton.isSelected()) {
                            // Handle migration for manual input
                            generateResultTextArea.setText("");
                            generateResultTextArea.setEditable(true);
                            utils.migrateFileToSpringBoot(manualInputTextArea.getText(), generateResultTextArea, sourceInput, targetInput);
                            acceptBtn.setEnabled(true);
                        }
                        return null;
                    }

                    @Override
                    protected void done() {
                        // Hide loading indicator
                        loadLabel.setVisible(false);
                    }
                }.execute();
            });

            // Add action listener for the manual radio button to toggle visibility of manual input textarea
            manualRadioButton.addActionListener(e -> {
                System.out.println("manualRadioButton.isSelected()" + manualRadioButton.isSelected());
                manualScrollPane.setVisible(manualRadioButton.isSelected());
                fileListComboBox.setVisible(!manualRadioButton.isSelected());
                migrateBtn.setText("Generate");
                acceptBtn.setVisible(!manualRadioButton.isSelected());
                copyCodeBtn.setVisible(manualRadioButton.isSelected());
                generateScrollPane.setVisible(manualRadioButton.isSelected());
                scrollPane.setVisible(!manualRadioButton.isSelected());
            });
            fileRadioButton.addActionListener(e -> {
                System.out.println("fileRadioButton.isSelected()" + fileRadioButton.isSelected());
                fileListComboBox.setVisible(fileRadioButton.isSelected());
                manualScrollPane.setVisible(!fileRadioButton.isSelected());
                migrateBtn.setText("Migrate");
                acceptBtn.setVisible(fileRadioButton.isSelected());
                copyCodeBtn.setVisible(!fileRadioButton.isSelected());
                generateScrollPane.setVisible(!manualRadioButton.isSelected());
                scrollPane.setVisible(!manualRadioButton.isSelected());

            });

            return codePanel;
        }


        private void setIconLabel(JLabel label, String imagePath) {
            label.setIcon(new ImageIcon(Objects.requireNonNull(getClass().getResource(imagePath))));
        }

        private JPanel createControlsPanel(ToolWindow toolWindow) {
            JPanel controlsPanel = new JPanel();
            controlsPanel.setMinimumSize(new Dimension(790, 290));
            controlsPanel.setMaximumSize(new Dimension(790, 290));
            controlsPanel.setLayout(new BorderLayout());

            JPanel contentPanel = new JPanel();
            contentPanel.setLayout(new GridBagLayout());
            GridBagConstraints constraints = new GridBagConstraints();

            JLabel label1 = new JLabel("Input");
            constraints.gridx = 0;
            constraints.gridy = 0;
            constraints.anchor = GridBagConstraints.WEST;
            contentPanel.add(label1, constraints);

            JTextArea textArea = new JTextArea(10, 10);
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            textArea.setEditable(true);
            JScrollPane scrollPaneTA = new JBScrollPane(textArea);
            scrollPaneTA.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            constraints.gridx = 0;
            constraints.gridy = 1;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            contentPanel.add(scrollPaneTA, constraints);

            JButton summarizeBtn = new JButton("Summarize");
            constraints.gridx = 0;
            constraints.gridy = 2;
            constraints.insets = new Insets(5, 5, 0, 0);
            constraints.fill = GridBagConstraints.WEST;
            contentPanel.add(summarizeBtn, constraints);

            textArea1 = new JTextArea(25, 15);
            textArea1.setLineWrap(true);
            textArea1.setWrapStyleWord(true);
            textArea1.setEditable(true);
// Create a JScrollPane for the JTextArea
            JScrollPane scrollPane = new JBScrollPane(textArea1);
            scrollPane.setPreferredSize(new Dimension(400, 300));
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            constraints.gridx = 0;
            constraints.gridy = 3;
            constraints.insets = new Insets(5, 5, 0, 0);
            constraints.fill = GridBagConstraints.BOTH;
            contentPanel.add(scrollPane, constraints);

            JButton saveButton = new JButton("Save"); // Create a "Save" button
            constraints.gridx = 0;
            constraints.gridy = 4;
            constraints.insets = new Insets(5, 5, 0, 0);
            constraints.fill = GridBagConstraints.WEST;
            contentPanel.add(saveButton, constraints);

            JPanel loadingSumPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            ImageIcon loading = new ImageIcon(getClass().getResource("/toolWindow/icons8-loading-circle.gif"));
            JLabel loadSumLabel = new JLabel("Loading... ", loading, JLabel.CENTER);
            loadSumLabel.setVisible(false);
            loadingSumPanel.add(loadSumLabel);


            summarizeBtn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String userInput;
                    if (Objects.isNull(editor)) {
                        editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
                    }
                    String selectedText = editor.getSelectionModel().getSelectedText();
                    if (Objects.nonNull(textArea.getText()) && !textArea.getText().isEmpty()) {
                        userInput = textArea.getText();
                    } else if (Objects.nonNull(selectedText) && !selectedText.equals("")) {
                        userInput = selectedText;
                    } else {
                        String fileContent = getFileContent(editor);
                        System.out.println("fileContent - " + fileContent);
                        if (Objects.nonNull(fileContent) && !fileContent.equals("")) {
                            userInput = fileContent;
                        } else {
                            userInput = null;
                        }
                    }
                    loadSumLabel.setVisible(true);
                    SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                        @Override
                        protected Void doInBackground() throws Exception {
                            utils.findSummary(userInput, textArea1);
                            return null;
                        }

                        @Override
                        protected void done() {
                            loadSumLabel.setVisible(false);
                        }
                    };

                    // Execute the SwingWorker to start the background task
                    worker.execute();
                }
            });

            saveButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    saveToFile(false); // Call a method to save the content to a file
                }
            });

            controlsPanel.add(contentPanel, BorderLayout.CENTER);
            controlsPanel.add(loadingSumPanel, BorderLayout.SOUTH);

            return controlsPanel;
        }

        //        private void saveToFile() {
//            VirtualFile baseDir = project.getBaseDir();
//
//            String docSummaryFolder = ".summaryDocuments";
//            File docSummaryDirectory = new File(baseDir.getPath(), docSummaryFolder);
//            if (!docSummaryDirectory.exists()) {
//                if (docSummaryDirectory.mkdir()) {
//                    // Directory created successfully
//                } else {
//                    // Directory creation failed
//                    JOptionPane.showMessageDialog(null, "Error creating directory.");
//                }
//            }
//
//            String fileName = "summary.html";
//            File filePath = new File(docSummaryDirectory, fileName);
//            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
//                String data = textArea1.getText();
//                String convertsToHtml = utils.convertIntoHtml(data);
//                writer.write(convertsToHtml);
//                JOptionPane.showMessageDialog(null, "Data saved successfully on the path : \n" + filePath);
//            } catch (IOException ex) {
//                ex.printStackTrace();
//                JOptionPane.showMessageDialog(null, "Error saving data.");
//            }
//        }
        private void saveToFile(Boolean isFromTestSuite) {
            if (isFromTestSuite) {


            } else {
                VirtualFile baseDir = project.getBaseDir();
                String docSummaryFolder = ".summaryDocuments";
                File docSummaryDirectory = new File(baseDir.getPath(), docSummaryFolder);
                if (!docSummaryDirectory.exists()) {
                    if (docSummaryDirectory.mkdir()) {
                        // Directory created successfully
                    } else {
                        // Directory creation failed
                        JOptionPane.showMessageDialog(null, "Error creating directory.");
                    }
                }
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Choose a location to save the file");
                fileChooser.setSelectedFile(new File(docSummaryDirectory, String.valueOf(baseDir)));
                FileNameExtensionFilter fileFilter = new FileNameExtensionFilter("HTML Files (*.html)", "html");
                fileChooser.setFileFilter(fileFilter);
                fileChooser.setCurrentDirectory(docSummaryDirectory);
                int userSelection = fileChooser.showSaveDialog(null);
                if (userSelection == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();

                    // Ensure the file has the ".html" extension
                    String fileName = selectedFile.getName();
                    if (fileChooser.getFileFilter() == fileFilter) {
                        if (!fileName.endsWith(".html")) {
                            selectedFile = new File(selectedFile.getParentFile(), fileName + ".html");
                        }
                    } else {
                        selectedFile = new File(selectedFile.getParentFile(), fileName);
                    }


//                File filePath = new File(docSummaryDirectory, fileName);
                    try (BufferedWriter writer = new BufferedWriter(new FileWriter(selectedFile))) {
                        String data = textArea1.getText();
                        String convertsToHtml = utils.convertIntoHtml(data);
                        writer.write(convertsToHtml);
                        JOptionPane.showMessageDialog(null, "Data saved successfully on the path : \n" + selectedFile);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(null, "Error saving data.");
                    }
                }
            }
        }

        private void runSummaryEvent() {
            String userInput = new String();
            if (Objects.isNull(editor)) {
                editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
            }
            String selectedText = editor.getSelectionModel().getSelectedText();
            if (Objects.nonNull(selectedText) && !selectedText.equals("")) {
                userInput = selectedText;
            } else {
                String fileContent = getFileContent(editor);
                System.out.println("fileContent - " + fileContent);
                if (Objects.nonNull(fileContent) && !fileContent.equals("")) {
                    userInput = fileContent;
                }
            }


            utils.findSummary(userInput, textArea1);
        }


        private String getFileContent(Editor editor) {
            if (editor != null) {
                return editor.getDocument().getText();
            }
            return "";
        }

        public JPanel getContentPanel() {
            return contentPanel;
        }

    }


}