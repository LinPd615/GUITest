package GUIArithmeticOperate;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ExerciseGenerationPanel extends JPanel {
    private JComboBox<String> exerciseTypeCombo;    // 题目类型
    private JSpinner questionCountSpinner;
    private JSpinner setCountSpinner;
    private JTextField filenameField;   // 基础文件名输入
    private JTextArea logArea;
    private ExerciseManager exerciseManager;    // 习题管理器

    // 事件监听器列表，用于面板间通信
    private List<ActionListener> exerciseGeneratedListeners;

    public ExerciseGenerationPanel(){
        this.exerciseGeneratedListeners = new ArrayList<>();
        exerciseManager = new ExerciseManager(new OperationBase(100));
        initializeUI();
    }

    // 添加生成完成事件监听器
    public void addExerciseGeneratedListener(ActionListener listener){
        exerciseGeneratedListeners.add(listener);
    }

    // 移除生成完成事件监听器
    public void removeExerciseGeneratedListener(ActionListener listener) {
        exerciseGeneratedListeners.remove(listener);
    }

    // 触发生成完成事件
    private void fireExerciseGenerated() {
        ActionEvent event = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "exerciseGenerated");
        for (ActionListener listener : exerciseGeneratedListeners) {
            listener.actionPerformed(event);
        }
    }

    private void initializeUI(){
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 输入面板
        JPanel inputPanel = createInputPanel();
        // 日志面板
        JPanel logPanel = createLogPanel();
        // 按钮面板
        JPanel buttonPanel = createButtonPanel();

        add(inputPanel, BorderLayout.NORTH);
        add(logPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * 创建输入设置面板
     */
    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("题目生成设置"));

        // 题目类型
        panel.add(new JLabel("题目类型:"));
        exerciseTypeCombo = new JComboBox<>(new String[]{"加法", "减法", "加减混合"});
        panel.add(exerciseTypeCombo);

        // 题目数量
        panel.add(new JLabel("每套题目数量:"));
        questionCountSpinner = new JSpinner(new SpinnerNumberModel(10, 1, 100, 1));
        panel.add(questionCountSpinner);

        // 套数
        panel.add(new JLabel("生成套数:"));
        setCountSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 50, 1));
        panel.add(setCountSpinner);

        // 文件名
        panel.add(new JLabel("基础文件名:"));
        filenameField = new JTextField("exercises");
        panel.add(filenameField);

        return panel;
    }

    /**
     * 创建操作日志面板
     */
    private JPanel createLogPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("操作日志"));

        logArea = new JTextArea(10, 50);
        logArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logArea);

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    /**
     * 创建按钮面板
     */
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout());

        JButton generateBtn = new JButton("生成题目");
        generateBtn.addActionListener(this::generateExercises);

        JButton clearBtn = new JButton("清空日志");
        clearBtn.addActionListener(e -> logArea.setText(""));

        panel.add(generateBtn);
        panel.add(clearBtn);

        return panel;
    }

    /**
     * 生成习题的核心方法
     */
    private void generateExercises(ActionEvent e) {
        try {
            String type = (String) exerciseTypeCombo.getSelectedItem();
            int questionCount = (int) questionCountSpinner.getValue();
            int setCount = (int) setCountSpinner.getValue();
            String baseFilename = filenameField.getText().trim();

            if (baseFilename.isEmpty()){
                JOptionPane.showMessageDialog(this, "请输入基础文件名","提示",JOptionPane.WARNING_MESSAGE);
                return;
            }

            int additionCount = 0;
            int subtractionCount = 0;

            switch (type) {
                case "加法":
                    additionCount = questionCount;
                    break;
                case "减法":
                    subtractionCount = questionCount;
                    break;
                case "加减混合":
                    additionCount = questionCount / 2;
                    subtractionCount = questionCount - additionCount;
                    break;
            }

            // 确保目标目录存在
            File exerciseDir = new File(FileConfig.EXERCISE_DIR);
            if (!exerciseDir.exists()) {
                exerciseDir.mkdirs();
            }

            // 冲突检测逻辑，检查第一套题是否存在，以此判断是否会发生覆盖
            String firstSetFileName = baseFilename + "_set1.csv";
            // 如果 baseFilename 没带路径，手动拼上路径检查
            if (!firstSetFileName.contains(File.separator)) {
                firstSetFileName = FileConfig.EXERCISE_DIR + firstSetFileName;
            }
            File checkFile = new File(firstSetFileName);
            if (checkFile.exists()) {
                int choice = JOptionPane.showConfirmDialog(this,
                        "文件 '" + checkFile.getName() + "' (及相关套题) 已存在。\n是否覆盖旧文件？\n\n注意：覆盖将导致旧的答题记录失效。",
                        "文件冲突",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);

                if (choice != JOptionPane.YES_OPTION) {
                    logArea.append("用户取消生成：文件已存在。\n");
                    return; // 中止操作
                }
            }

            // 记录生成前的文件列表
            Set<String> existingFiles = getExistingCSVFiles();

            // 使用 ExerciseManager 的生成方法
            exerciseManager.generateMultipleExerciseSets(setCount, additionCount, subtractionCount, baseFilename);

            // 获取新生成的文件
            Set<String> newFiles = getExistingCSVFiles();
            newFiles.removeAll(existingFiles);

            logArea.append("成功生成 " + setCount + " 套" + type + "题目\n");
            logArea.append("生成目录: " + FileConfig.EXERCISE_DIR + "\n");

            if (!newFiles.isEmpty()){
                logArea.append("生成的文件: " + String.join(", ", newFiles) + "\n");
                for (String fileName : newFiles) {
                    logArea.append("  " + fileName + "\n");
                }
            } else {
                logArea.append("警告: 未检测到新生成的文件\n");
                // 显示目标目录所有文件
                File[] allFiles = exerciseDir.listFiles();
                if (allFiles != null) {
                    logArea.append("目标目录所有文件:\n");
                    for (File file : allFiles) {
                        logArea.append("  " + file.getName() + "\n");
                    }
                }
            }

            // 自动滚动到最后
            logArea.setCaretPosition(logArea.getDocument().getLength());

            // 触发生成完成事件
            fireExerciseGenerated();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "生成题目失败: " + ex.getMessage(),
                    "错误", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
            logArea.append("生成失败："+ex.getMessage()+"\n");
        }
    }

    // 扫描目标目录而不是当前目录
    private Set<String> getExistingCSVFiles() {
        Set<String> csvFiles = new HashSet<>();
        File exerciseDir = new File(FileConfig.EXERCISE_DIR);
        File[] files = exerciseDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".csv"));
        if (files != null) {
            for (File file : files) {
                csvFiles.add(file.getName());   // 只返回文件名，不包含路径
            }
        }
        return csvFiles;
    }
}