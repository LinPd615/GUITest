package GUIArithmeticOperate;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 在线练习面板，支持实时打分和统计
 */
public class OnlinePracticePanel extends JPanel {
    private JComboBox<String> exerciseFileCombo;    // 习题集选择
    private JTextField studentNameField;
    private JTextArea questionArea;
    private JTextField answerField;
    private JButton prevBtn, nextBtn, submitBtn;
    private JLabel timerLabel, scoreLabel;  // 计时和分数显示
    private JLabel statusLabel; // 状态标签
    private JPanel statsPanel;  // 统计面板

    // 数据模型
    private List<ExerciseItem> currentExercises;    // 当前习题集
    private Map<Integer, Integer> studentAnswers;
    private int currentQuestionIndex = 0;   // 题目索引
    private Timer timer;
    private long startTime;
    private boolean isPracticeStarted = false;

    // 统计相关
    private int totalQuestions = 0;
    private int answeredQuestions = 0;
    private int correctAnswers = 0;

    public OnlinePracticePanel() {
        initializeUI();
        setupTimer();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 顶部控制面板
        add(createControlPanel(), BorderLayout.NORTH);
        // 题目显示面板
        add(createQuestionPanel(), BorderLayout.CENTER);
        // 底部按钮面板和统计面板
        add(createBottomPanel(), BorderLayout.SOUTH);
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("练习设置"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // 选择习题集
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("选择习题集:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        exerciseFileCombo = new JComboBox<>();
        refreshExerciseFiles();
        panel.add(exerciseFileCombo, gbc);

        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        JButton refreshBtn = new JButton("刷新");
        refreshBtn.addActionListener(e -> refreshExerciseFiles());
        panel.add(refreshBtn, gbc);

        // 学生姓名
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("学生姓名:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        studentNameField = new JTextField();
        panel.add(studentNameField, gbc);

        gbc.gridx = 2;
        gbc.gridy = 1;
        panel.add(new JLabel(), gbc); // 空标签占位

        // 用时、得分显示
        gbc.gridx = 0;
        gbc.gridy = 2;
        timerLabel = new JLabel("用时: 00:00");
        panel.add(timerLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        scoreLabel = new JLabel("得分: 0/0");
        panel.add(scoreLabel, gbc);

        gbc.gridx = 2;
        gbc.gridy = 2;
        statusLabel = new JLabel("准备开始");
        statusLabel.setForeground(Color.BLUE);
        panel.add(statusLabel, gbc);

        return panel;
    }

    /**
     * 题目练习模块面板
     * @return
     */
    private JPanel createQuestionPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("题目练习"));

        questionArea = new JTextArea(5, 40);
        questionArea.setEditable(false);
        questionArea.setFont(new Font("微软雅黑", Font.PLAIN, 18));
        questionArea.setLineWrap(true);
        questionArea.setText("请先输入学生姓名并选择习题集，然后点击\"开始练习\"按钮");
        JScrollPane scrollPane = new JScrollPane(questionArea);

        JPanel answerPanel = new JPanel(new BorderLayout(10, 10));
        answerPanel.add(new JLabel("您的答案: "), BorderLayout.WEST);
        answerField = new JTextField();
        answerField.setFont(new Font("微软雅黑", Font.PLAIN, 16));

        // 添加答案输入监听
        answerField.addActionListener(e -> saveAndShowNext());
        answerPanel.add(answerField, BorderLayout.CENTER);

        // 状态指示器，预留答题状态显示用
        JLabel answerStatus = new JLabel(" ");
        answerStatus.setPreferredSize(new Dimension(30, 30));
        answerStatus.setHorizontalAlignment(SwingConstants.CENTER);
        answerStatus.setFont(new Font("Arial", Font.BOLD, 20));
        answerPanel.add(answerStatus, BorderLayout.EAST);

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(answerPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // 按钮面板
        JPanel buttonPanel = createButtonPanel();
        panel.add(buttonPanel, BorderLayout.NORTH);

        // 统计面板
        statsPanel = createStatsPanel();
        panel.add(statsPanel, BorderLayout.CENTER);

        return panel;
    }

    /**
     * 按钮面板
     * @return
     */
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout());

        prevBtn = new JButton("上一题");
        nextBtn = new JButton("下一题");
        submitBtn = new JButton("开始练习");

        prevBtn.setEnabled(false);
        nextBtn.setEnabled(false);
        answerField.setEnabled(false);

        prevBtn.addActionListener(e -> showPreviousQuestion());
        nextBtn.addActionListener(e -> showNextQuestion());
        submitBtn.addActionListener(e -> {
            if (!isPracticeStarted) {
                startPractice();
            } else {
                submitAnswer();
            }
        });

        panel.add(prevBtn);
        panel.add(nextBtn);
        panel.add(submitBtn);

        return panel;
    }

    /**
     * 答题信息情况
     * @return
     */
    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 10, 5));
        panel.setBorder(BorderFactory.createTitledBorder("练习统计"));
        panel.setPreferredSize(new Dimension(0, 80));

        // 总题数
        JPanel totalPanel = createStatItem("总题数", "0", Color.BLUE);
        // 已答题
        JPanel answeredPanel = createStatItem("已答题", "0", Color.ORANGE);

        panel.add(totalPanel);
        panel.add(answeredPanel);

        return panel;
    }

    /**
     * 答题情况
     * @param title
     * @param value
     * @param color
     * @return
     */
    private JPanel createStatItem(String title, String value, Color color) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));

        JLabel valueLabel = new JLabel(value, SwingConstants.CENTER);
        valueLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        valueLabel.setForeground(color);

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(valueLabel, BorderLayout.CENTER);

        // 设置标签名称以便后续更新
        valueLabel.setName(title);

        return panel;
    }

    // 更新统计信息
    private void updateStatistics() {
        Component[] stats = statsPanel.getComponents();
        for (Component comp : stats) {
            if (comp instanceof JPanel) {
                JPanel statPanel = (JPanel) comp;
                Component[] children = statPanel.getComponents();
                for (Component child : children) {
                    if (child instanceof JLabel) {
                        JLabel label = (JLabel) child;
                        if (label.getName() != null) {
                            switch (label.getName()) {
                                case "总题数":
                                    label.setText(String.valueOf(totalQuestions));
                                    break;
                                case "已答题":
                                    label.setText(String.valueOf(answeredQuestions));
                                    break;
                            }
                        }
                    }
                }
            }
        }
    }

    public void refreshExerciseFiles() {
        exerciseFileCombo.removeAllItems();

        // 扫描当前目录下的所有CSV文件
        File exerciseDir = new File(FileConfig.EXERCISE_DIR);
        System.out.println("刷新文件列表，扫描目录: " + exerciseDir.getAbsolutePath());

        if (!exerciseDir.exists()) {
            System.out.println("目录不存在，创建目录: " + exerciseDir.getAbsolutePath());
            exerciseDir.mkdirs();
        }

        File[] csvFiles = exerciseDir.listFiles((dir, name) ->
                name.toLowerCase().endsWith(".csv") &&
                        !name.contains("_answer") &&
                        !name.contains("_results"));

        if (csvFiles != null && csvFiles.length > 0) {
            // 按文件名排序（新的在前面）
            Arrays.sort(csvFiles, (f1, f2) -> f2.getName().compareTo(f1.getName()));

            for (File file : csvFiles) {
                exerciseFileCombo.addItem(file.getName());
                System.out.println("找到CSV文件: " + file.getName());
            }

            System.out.println("刷新习题文件列表，找到 " + csvFiles.length + " 个CSV文件");

            // 如果组合框有项目，选择第一个
            if (exerciseFileCombo.getItemCount() > 0) {
                exerciseFileCombo.setSelectedIndex(0);
            }
        } else {
            System.out.println("未找到任何CSV文件");
            // 添加提示
            exerciseFileCombo.addItem("请先生成题目");
        }

        // 刷新显示
        exerciseFileCombo.revalidate();
        exerciseFileCombo.repaint();
    }

    private void setupTimer() {
        timer = new Timer(1000, e -> updateTimer());
    }

    private void updateTimer() {
        long elapsed = System.currentTimeMillis() - startTime;
        long seconds = elapsed / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        timerLabel.setText(String.format("用时: %02d:%02d", minutes, seconds));
    }

    /**
     * 在线练习
     */
    private void startPractice() {
        String studentName = studentNameField.getText().trim();
        if (studentName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入学生姓名", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String selectedFile = (String) exerciseFileCombo.getSelectedItem();
        if (selectedFile == null || selectedFile.equals("请先生成题目")) {
            JOptionPane.showMessageDialog(this, "请选择有效的习题集", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            System.out.println("尝试加载习题集: " + selectedFile);

            // 检查文件是否存在
            String fullPath = FileConfig.EXERCISE_DIR + selectedFile;
            File file = new File(fullPath);
            if (!file.exists()) {
                JOptionPane.showMessageDialog(this, "习题集文件不存在: " + fullPath, "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            System.out.println("文件存在，路径: " + file.getAbsolutePath());

            // 加载习题集
            currentExercises = ExerciseManager.loadExerciseSetStatic(selectedFile);
            if (currentExercises == null || currentExercises.isEmpty()) {
                JOptionPane.showMessageDialog(this, "习题集为空或加载失败", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            System.out.println("成功加载 " + currentExercises.size() + " 道题目");

            studentAnswers = new HashMap<>();
            currentQuestionIndex = 0;
            isPracticeStarted = true;

            // 初始化统计信息
            totalQuestions = currentExercises.size();
            answeredQuestions = 0;
            correctAnswers = 0;
            updateStatistics();

            // 启用控件
            prevBtn.setEnabled(false);
            nextBtn.setEnabled(currentExercises.size() > 1);
            answerField.setEnabled(true);
            submitBtn.setText("提交答案");
            statusLabel.setText("练习中...");
            statusLabel.setForeground(Color.GREEN);

            // 开始计时
            startTime = System.currentTimeMillis();
            timer.start();

            showCurrentQuestion();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "加载习题集失败: " + ex.getMessage(),
                    "错误", JOptionPane.ERROR_MESSAGE);
            System.err.println("加载习题集详细错误:");
            ex.printStackTrace();
        }
    }

    /**
     * 显示当前题目
     */
    private void showCurrentQuestion() {
        if (currentExercises == null || currentExercises.isEmpty()) return;

        ExerciseItem exercise = currentExercises.get(currentQuestionIndex);

        // 显示题目信息
        String questionText = String.format("第%d题 [%s]\n%s = ",
                currentQuestionIndex + 1,
                exercise.getType(),
                exercise.getQuestion());

        questionArea.setForeground(Color.BLACK);
        questionArea.setText(questionText);

        // 显示已保存的答案
        Integer savedAnswer = studentAnswers.get(currentQuestionIndex);
        answerField.setText(savedAnswer != null ? savedAnswer.toString() : "");
        answerField.requestFocus();

        // 更新按钮状态
        prevBtn.setEnabled(currentQuestionIndex > 0);
        nextBtn.setEnabled(currentQuestionIndex < currentExercises.size() - 1);

        // 更新进度
        scoreLabel.setText(String.format("进度: %d/%d", currentQuestionIndex + 1, currentExercises.size()));

        // 更新状态标签
        updateStatusLabel();
    }


    private void updateStatusLabel() {
        if (currentExercises == null) return;

        ExerciseItem currentExercise = currentExercises.get(currentQuestionIndex);
        if (currentExercise.getStudentAnswer() != null) {
            if (currentExercise.isCorrect()) {
                statusLabel.setText("回答正确 ✓");
                statusLabel.setForeground(Color.GREEN);
            } else {
                statusLabel.setText("回答错误 ✗");
                statusLabel.setForeground(Color.RED);
            }
        } else {
            statusLabel.setText("等待答题...");
            statusLabel.setForeground(Color.BLUE);
        }
    }

    private void showPreviousQuestion() {
        saveCurrentAnswer();
        if (currentQuestionIndex > 0) {
            currentQuestionIndex--;
            showCurrentQuestion();
        }
    }

    private void showNextQuestion() {
        saveCurrentAnswer();
        if (currentQuestionIndex < currentExercises.size() - 1) {
            currentQuestionIndex++;
            showCurrentQuestion();
        }
    }

    // 保存答案并自动下一题
    private void saveAndShowNext() {
        saveCurrentAnswer();
        if (currentQuestionIndex < currentExercises.size() - 1) {
            currentQuestionIndex++;
            showCurrentQuestion();
        } else {
            // 如果是最后一题，提示提交
            JOptionPane.showMessageDialog(this,
                    "已经是最后一题了，请点击\"提交答案\"按钮完成练习",
                    "提示", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * 保存当前答案
     */
    private void saveCurrentAnswer() {
        if (currentExercises == null || currentQuestionIndex < 0 ||
                currentQuestionIndex >= currentExercises.size()) {
            return;
        }

        ExerciseItem currentExercise = currentExercises.get(currentQuestionIndex);
        Integer previousAnswer = currentExercise.getStudentAnswer();
        boolean wasCorrect = (previousAnswer != null &&
                previousAnswer.equals(currentExercise.getCorrectAnswer()));

        String answerText = answerField.getText().trim();
        // 用户把答案清空：从“已作答”恢复为“未作答“
        if (answerText.isEmpty()) {
            if (previousAnswer != null) {
                answeredQuestions--;
                if (wasCorrect) {
                    correctAnswers--;
                }
                currentExercise.setStudentAnswer(null);
                studentAnswers.remove(currentQuestionIndex);
                updateStatistics();
            }
            return;
        }
        // 输入新答案
        try {
            int answer = Integer.parseInt(answerText);
            answerField.setBackground(Color.WHITE);     // 输入合法，恢复背景色
            studentAnswers.put(currentQuestionIndex, answer);
            currentExercise.setStudentAnswer(answer);

            // 统计“已作答题数”
            if (previousAnswer == null) {
                answeredQuestions++;
            }
            boolean isNowCorrect = currentExercise.isCorrect();
            if (isNowCorrect && !wasCorrect) {
                correctAnswers++;
            } else if (!isNowCorrect && wasCorrect) {
                correctAnswers--;
            }

            updateStatistics();
        } catch (NumberFormatException e) {
            // 非数字输入，添加错误提示
            answerField.setBackground(new Color(255,200,200));
            JOptionPane.showMessageDialog(this, "输入无效！请输入整数答案。",
                    "输入错误",
                    JOptionPane.WARNING_MESSAGE);
            // 让输入框重新获得焦点并选中内容，方便修改
            answerField.requestFocus();
            answerField.selectAll();
        }
    }

    /**
     * 提交答案并显示详细结果
     */
    private void submitAnswer() {
        saveCurrentAnswer();

        // 计算最终得分
        int finalCorrectCount = 0;
        if (currentExercises != null) {
            for (ExerciseItem exercise : currentExercises) {
                if (exercise.isCorrect()) {
                    finalCorrectCount++;
                }
            }
        }

        // 停止计时
        timer.stop();

        // 显示详细结果
        showDetailedResults(finalCorrectCount);

        // 保存学生答题文件和批改结果
        saveStudentPracticeResults(finalCorrectCount);

        // 重置状态
        resetPractice();
    }

    // 显示详细结果对话框 - 支持颜色显示，并支持导出本次错题
    private void showDetailedResults(int correctCount) {
        // 创建结果对话框
        JDialog resultDialog = new JDialog((Frame)SwingUtilities.getWindowAncestor(this), "练习结果", true);
        resultDialog.setLayout(new BorderLayout(10, 10));
        resultDialog.setSize(600, 500);
        resultDialog.setLocationRelativeTo(this);

        // 标题
        JLabel titleLabel = new JLabel("练习完成!", SwingConstants.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        titleLabel.setForeground(Color.BLUE);
        resultDialog.add(titleLabel, BorderLayout.NORTH);

        // 使用JTextPane来支持颜色显示
        JTextPane resultPane = new JTextPane();
        resultPane.setEditable(false);
        resultPane.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        resultPane.setContentType("text/html");

        // 用于导出错题的临时列表：题号,题型,题目,学生答案,正确答案
        List<String[]> wrongQuestions = new java.util.ArrayList<>();

        // 构建HTML格式的结果文本
        StringBuilder resultHtml = new StringBuilder();
        resultHtml.append("<html><body style='font-family:微软雅黑; font-size:12pt; margin:10px'>");

        // 统计信息
        resultHtml.append("<div style='margin-bottom:15px'>");
        resultHtml.append("<b>练习结果统计</b><br>");
        resultHtml.append(String.format("学生姓名: %s<br>", studentNameField.getText()));
        resultHtml.append(String.format("习题集: %s<br>", exerciseFileCombo.getSelectedItem()));
        resultHtml.append(String.format("总题数: %d题<br>", totalQuestions));
        resultHtml.append(String.format("答对题数: %d题<br>", correctCount));

        double accuracy = totalQuestions > 0 ? (double)correctCount/totalQuestions*100 : 0;
        String accuracyColor = accuracy >= 80 ? "green" : (accuracy >= 60 ? "orange" : "red");
        resultHtml.append(String.format("正确率: <span style='color:%s; font-weight:bold'>%.1f%%</span><br>",
                accuracyColor, accuracy));
        resultHtml.append(String.format("用时: %s<br>", timerLabel.getText().replace("用时: ", "")));
        resultHtml.append("</div>");

        // 各题详情
        resultHtml.append("<b>各题详情:</b><br>");
        resultHtml.append("<hr>");

        for (int i = 0; i < currentExercises.size(); i++) {
            ExerciseItem exercise = currentExercises.get(i);
            Integer studentAnswer = exercise.getStudentAnswer();
            Integer correctAnswer = exercise.getCorrectAnswer();
            boolean isCorrect = (studentAnswer != null && studentAnswer.equals(correctAnswer));

            // 题目和正确答案
            resultHtml.append(String.format("%d. %s = <b>%d</b>",
                    i + 1, exercise.getQuestion(), correctAnswer));

            // 学生作答情况
            if (studentAnswer != null) {
                if (isCorrect) {
                    resultHtml.append(String.format(
                            " <span style='color:green; font-weight:bold'>" +
                                    "✓ 正确 (你的答案: %d)</span>",
                            studentAnswer));
                } else {
                    resultHtml.append(String.format(
                            " <span style='color:red; font-weight:bold'>" +
                                    "✗ 错误 (你的答案: %d)</span>",
                            studentAnswer));
                }
            } else {
                resultHtml.append(" <span style='color:gray'>○ 未作答</span>");
            }
            resultHtml.append("<br>");

            // 收集错题集或未作答，用于导出
            if (!isCorrect){
                wrongQuestions.add(new String[]{
                        String.valueOf(i+1),
                        exercise.getType(),
                        exercise.getQuestion(),
                        studentAnswer != null ? studentAnswer.toString() : "",
                        String.valueOf(correctAnswer)
                });
            }
        }

        resultHtml.append("</body></html>");

        resultPane.setText(resultHtml.toString());

        // 确保文本从顶部开始显示
        resultPane.setCaretPosition(0);

        JScrollPane scrollPane = new JScrollPane(resultPane);
        resultDialog.add(scrollPane, BorderLayout.CENTER);

        // 底部按钮：导出错题+确定
        JPanel buttonPanel = new JPanel();
        JButton exportWrongButton = new JButton("导出错题");
        exportWrongButton.addActionListener(e -> {
            String studentName = studentNameField.getText().trim();
            String exerciseFile = (String) exerciseFileCombo.getSelectedItem();

            if (studentName.isEmpty() || exerciseFile == null ||
                    exerciseFile.equals("请先生成题目")) {
                JOptionPane.showMessageDialog(resultDialog,
                        "学生姓名或习题集信息无效，无法导出错题。",
                        "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (wrongQuestions.isEmpty()) {
                JOptionPane.showMessageDialog(resultDialog,
                        "本次练习没有错题，无需导出。",
                        "提示", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            try {
                FileConfig.ensureExerciseDirExists();

                String baseName = exerciseFile.replace(".csv", "");
                String wrongFile = FileConfig.EXERCISE_DIR
                        + baseName + "_" + studentName + "_wrong.csv";

                try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(
                        new FileOutputStream(wrongFile), StandardCharsets.UTF_8))) {
                    writer.write("\uFEFF");
                    writer.println("题号,题型,题目,学生答案,正确答案");
                    for (String[] rec : wrongQuestions) {
                        writer.printf("%s,%s,\"%s\",%s,%s%n",
                                rec[0], rec[1], rec[2], rec[3], rec[4]);
                    }
                }
                JOptionPane.showMessageDialog(resultDialog,
                        "错题已导出到: " + wrongFile,
                        "导出成功", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(resultDialog,
                        "导出错题失败: " + ex.getMessage(),
                        "错误", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });
        JButton okButton = new JButton("确定");
        okButton.addActionListener(e -> resultDialog.dispose());
        buttonPanel.add(exportWrongButton);
        buttonPanel.add(okButton);
        resultDialog.add(buttonPanel, BorderLayout.SOUTH);

        resultDialog.setVisible(true);
    }

    private void saveStudentPracticeResults(int correctCount) {
        try {
            String studentName = studentNameField.getText().trim();
            String selectedFile = (String) exerciseFileCombo.getSelectedItem();

            if (studentName.isEmpty() || selectedFile == null || selectedFile.equals("请先生成题目")) {
                return;
            }

            // 1. 保存学生答题文件
            saveStudentAnswersToFile(studentName, selectedFile);

            // 2. 保存批改结果
            saveGradingResultsToFile(studentName, selectedFile, correctCount);

            System.out.println("学生答题结果和批改结果已保存");

        } catch (Exception ex) {
            System.err.println("保存学生答题结果失败: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // 保存学生答题文件,追加模式
    // 保存学生答题文件 - 追加模式
    private void saveStudentAnswersToFile(String studentName, String exerciseFile) {
        FileConfig.ensureExerciseDirExists();

        String baseName = exerciseFile.replace(".csv", "");
        String answerFile = FileConfig.EXERCISE_DIR + baseName + "_" + studentName + "_answer.csv";

        boolean fileExists = new File(answerFile).exists();

        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(answerFile, true), StandardCharsets.UTF_8))) { // true 表示追加模式

            // 如果是新文件，写入UTF-8 BOM和表头
            if (!fileExists) {
                writer.write("\uFEFF");
                writer.println("题号,学生答案");
            }

            for (int i = 0; i < currentExercises.size(); i++) {
                ExerciseItem exercise = currentExercises.get(i);
                String answerStr = (exercise.getStudentAnswer() != null) ?
                        String.valueOf(exercise.getStudentAnswer()) : "未作答";
                writer.printf("%d,%s%n", i + 1, answerStr);
            }

            System.out.println("学生答案已追加保存到: " + answerFile);

        } catch (Exception e) {
            System.err.println("保存学生答案失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 保存批改结果文件 - 追加模式
    private void saveGradingResultsToFile(String studentName, String exerciseFile, int correctCount) {
        FileConfig.ensureExerciseDirExists();

        String baseName = exerciseFile.replace(".csv", "");
        String resultFile = FileConfig.EXERCISE_DIR + baseName + "_" + studentName + "_results.csv";

        boolean fileExists = new File(resultFile).exists();

        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(resultFile, true), StandardCharsets.UTF_8))) { // true 表示追加模式

            // 如果是新文件，写入UTF-8 BOM和表头
            if (!fileExists) {
                writer.write("\uFEFF");
                writer.println("题号,题目,正确答案,学生答案,结果");
            }

            // 生成详细的批改结果
            for (int i = 0; i < currentExercises.size(); i++) {
                ExerciseItem exercise = currentExercises.get(i);
                String studentAnswerStr = (exercise.getStudentAnswer() != null) ?
                        String.valueOf(exercise.getStudentAnswer()) : "未作答";
                String resultStr = exercise.isCorrect() ? "正确" : "错误";

                writer.printf("%d,\"%s\",%d,%s,%s%n",
                        i + 1,
                        exercise.getQuestion(),
                        exercise.getCorrectAnswer(),
                        studentAnswerStr,
                        resultStr);
            }

            // 统计信息
            writer.println();
            writer.printf("学生姓名,%s%n", studentName);
            writer.printf("习题集,%s%n", exerciseFile);
            writer.printf("总题数,%d题%n", currentExercises.size());
            writer.printf("答对题数,%d题%n", correctCount);
            writer.printf("正确率,%.1f%%%n",
                    currentExercises.size() > 0 ? (double) correctCount / currentExercises.size() * 100 : 0);

            // 批改时间
            String gradingTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            writer.printf("批改时间,%s%n", gradingTime);
            writer.println(); // 空行分隔不同批改记录

            System.out.println("批改结果已追加保存到: " + resultFile);

        } catch (Exception e) {
            System.err.println("保存批改结果失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void resetPractice() {
        isPracticeStarted = false;
        currentExercises = null;
        studentAnswers = null;
        currentQuestionIndex = 0;

        prevBtn.setEnabled(false);
        nextBtn.setEnabled(false);
        answerField.setEnabled(false);
        submitBtn.setText("开始练习");
        statusLabel.setText("准备开始");
        statusLabel.setForeground(Color.BLUE);

        questionArea.setText("请先输入学生姓名并选择习题集，然后点击\"开始练习\"按钮");
        answerField.setText("");
        scoreLabel.setText("得分: 0/0");
        timerLabel.setText("用时: 00:00");

        // 重置统计信息
        totalQuestions = 0;
        answeredQuestions = 0;
        correctAnswers = 0;
        updateStatistics();
    }
}