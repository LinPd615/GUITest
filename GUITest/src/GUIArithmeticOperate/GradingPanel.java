package GUIArithmeticOperate;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.HashMap;
import java.util.Map;


/**
 * 查看和导出批改结果，支持文件扫描和显示
 */
public class GradingPanel extends JPanel {
    private JTable resultTable;
    private DefaultTableModel tableModel;
    private JTextArea detailArea;
    private JLabel summaryLabel;    // 汇总信息标签

    // 操作按钮
    private JButton batchGradeBtn;
    private JButton exportResultsBtn;
    private JButton viewDetailsBtn;
    private JButton refreshBtn;

    private ExerciseManager exerciseManager;

    public GradingPanel() {
        exerciseManager = new ExerciseManager(new OperationBase(100));
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 顶部按钮面板
        add(createButtonPanel(), BorderLayout.NORTH);
        // 中间结果表格
        add(createTablePanel(), BorderLayout.CENTER);
        // 底部详细信息
        add(createDetailPanel(), BorderLayout.SOUTH);
    }

    /**
     * 按钮面板
     * @return
     */
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout());
        panel.setBorder(BorderFactory.createTitledBorder("批改操作"));

        batchGradeBtn = new JButton("批量批改");
        exportResultsBtn = new JButton("导出批改结果");
        viewDetailsBtn = new JButton("查看详情");
        refreshBtn = new JButton("刷新列表");

        // 添加事件监听器
        batchGradeBtn.addActionListener(e -> batchGradeAll());
        exportResultsBtn.addActionListener(e -> exportResults());
        viewDetailsBtn.addActionListener(e -> viewStudentDetails());
        refreshBtn.addActionListener(e -> refreshResults());


        panel.add(batchGradeBtn);
        panel.add(exportResultsBtn);
        panel.add(viewDetailsBtn);
        panel.add(refreshBtn);

        return panel;
    }

    /**
     * 结果表格面板
     * @return
     */
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("批改结果汇总"));

        // 创建表格模型
        String[] columns = {"学生姓名", "习题集", "总题数", "平均正确率","最新正确率", "批改时间"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;   // 表格不可编辑
            }
        };

        resultTable = new JTable(tableModel);
        resultTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(resultTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // 汇总信息
        summaryLabel = new JLabel("总计: 0 名学生");
        panel.add(summaryLabel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * 详细信息面板
     * @return
     */
    private JPanel createDetailPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("详细批改信息"));
        panel.setPreferredSize(new Dimension(800, 200));

        detailArea = new JTextArea(8, 60);
        detailArea.setEditable(false);
        detailArea.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(detailArea);

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    // 批量批改所以学生
    private void batchGradeAll() {
        // 扫描所有答题文件并进行批改
        scanAndGradeAllAnswers();
        JOptionPane.showMessageDialog(this, "批量批改完成！", "提示", JOptionPane.INFORMATION_MESSAGE);
    }

    // 扫描并批改所有答题文件
    private void scanAndGradeAllAnswers() {
        File exerciseDir = new File(FileConfig.EXERCISE_DIR);
        File[] answerFiles = exerciseDir.listFiles((dir, name) ->
                name.toLowerCase().endsWith("_answer.csv"));

        if (answerFiles != null) {
            for (File answerFile : answerFiles) {
                processAnswerFile(answerFile);
            }
        }
    }

    // 处理单个答题文件
    private void processAnswerFile(File answerFile) {
        String filename = answerFile.getName();

        try{
            // 从文件名解析出 习题集文件名 和 学生姓名 exercises_set1_张三_answer.csv
            if (!filename.toLowerCase().endsWith("_answer.csv")){
                return;
            }
            String withoutSuffix = filename.substring(0,filename.toLowerCase().lastIndexOf("_answer.csv"));
            int lastUnderscore = withoutSuffix.lastIndexOf("_");
            if (lastUnderscore <= 0) {
                // 文件名格式不符合约定，直接跳过
                System.err.println("无法从答题文件名解析学生和习题集: " + filename);
                return;
            }

            String baseName = withoutSuffix.substring(0, lastUnderscore);
            String studentName = withoutSuffix.substring(lastUnderscore + 1);
            String exerciseFileName = baseName + ".csv";

            System.out.println("批改学生答题文件: " + filename +
                    "  学生: " + studentName +
                    "  习题集: " + exerciseFileName);

            // 加载习题集
            List<ExerciseItem> exercises = ExerciseManager.loadExerciseSetStatic(exerciseFileName);
            if (exercises == null || exercises.isEmpty()) {
                System.err.println("习题集为空或不存在，跳过: " + exerciseFileName);
                return;
            }

            // 读取学生答案
            Map<Integer, Integer> answers = new HashMap<>();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(
                            new FileInputStream(answerFile),
                            StandardCharsets.UTF_8))) {

                String line;
                boolean firstLine = true;
                while ((line = reader.readLine()) != null) {
                    if (firstLine) {
                        line = line.replace("\uFEFF", "");
                        firstLine = false;
                    }
                    if (line.trim().isEmpty() || line.startsWith("题号")) {
                        continue;
                    }

                    String[] parts = line.split(",", -1);
                    if (parts.length >= 2) {
                        try {
                            int qNum = Integer.parseInt(parts[0].trim());
                            String ansStr = parts[1].trim();
                            if (!ansStr.isEmpty()) {
                                int ans = Integer.parseInt(ansStr);
                                answers.put(qNum, ans);
                            }
                        } catch (NumberFormatException ignore) {
                            // 无效数字答案忽略
                        }
                    }
                }
            }
            // 按照题号把答案填回ExerciseItem，并统计正确题数
            int correctCount = 0;
            for (ExerciseItem item : exercises) {
                Integer ans = answers.get(item.getQuestionNumber());
                if (ans != null) {
                    item.setStudentAnswer(ans);
                    if (item.isCorrect()) {
                        correctCount++;
                    }
                } else {
                    item.setStudentAnswer(null);
                }
            }

            // 写入/追加_results.csv
            saveBatchGradingResult(studentName, exerciseFileName, correctCount, exercises);

            // 6）让当前批改结果立即体现在表格中
            File resultFile = new File(FileConfig.EXERCISE_DIR
                    + baseName + "_" + studentName + "_results.csv");
            if (resultFile.exists()) {
                loadSingleResultFile(resultFile);
            }

        } catch (IOException ex) {
            System.err.println("批改答题文件失败: " + filename + " - " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // 批量批改时保存结果文件
    private void saveBatchGradingResult(String studentName,
                                        String exerciseFile,
                                        int correctCount,
                                        List<ExerciseItem> exercises) {
        FileConfig.ensureExerciseDirExists();

        String baseName = exerciseFile.replace(".csv", "");
        String resultFile = FileConfig.EXERCISE_DIR + baseName + "_" + studentName + "_results.csv";

        boolean fileExists = new File(resultFile).exists();

        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(resultFile, true), StandardCharsets.UTF_8))) {

            // 如果是新文件，写入UTF-8 BOM和表头
            if (!fileExists) {
                writer.write("\uFEFF");
                writer.println("题号,题目,学生答案,正确答案,是否正确");
            }

            // 写入本次每一道题目的详细信息
            for (ExerciseItem exercise : exercises) {
                Integer studentAnswer = exercise.getStudentAnswer();
                writer.printf("%d,%s,%s,%d,%s%n",
                        exercise.getQuestionNumber(),
                        exercise.getQuestion(),
                        studentAnswer != null ? studentAnswer.toString() : "",
                        exercise.getCorrectAnswer(),
                        (studentAnswer != null && studentAnswer.equals(exercise.getCorrectAnswer())) ? "正确" : "错误");
            }

            // 追加统计信息
            writer.println();
            writer.printf("学生,%s%n", studentName);
            writer.printf("习题集,%s%n", exerciseFile);
            writer.printf("总题数,%d题%n", exercises.size());
            writer.printf("答对题数,%d题%n", correctCount);
            writer.printf("正确率,%.1f%%%n",
                    exercises.size() > 0 ? (double) correctCount / exercises.size() * 100 : 0);

            String gradingTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            writer.printf("批改时间,%s%n", gradingTime);
            writer.println(); // 空行分隔不同批改记录

            System.out.println("批量批改结果已追加保存到: " + resultFile);
        } catch (Exception e) {
            System.err.println("保存批量批改结果失败: " + e.getMessage());
            e.printStackTrace();
        }
    }


    // 导出批改结果
    private void exportResults() {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this,
                    "没有可导出的批改结果",
                    "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser(exerciseManager.getBasePath());
        fileChooser.setSelectedFile(new File("grading_summary.csv"));

        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File outputFile = fileChooser.getSelectedFile();
            try {
                exportToCSV(outputFile);
                JOptionPane.showMessageDialog(this,
                        "批改结果已导出到: " + outputFile.getAbsolutePath(),
                        "导出成功", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "导出失败: " + ex.getMessage(),
                        "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void viewStudentDetails() {
        int selectedRow = resultTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "请先选择要查看的学生",
                    "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String studentName = (String) tableModel.getValueAt(selectedRow, 0);
        String exerciseSet = (String) tableModel.getValueAt(selectedRow, 1);

        try {
            showStudentGradingDetails(studentName, exerciseSet);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "加载详细信息失败: " + ex.getMessage(),
                    "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshResults() {
        // 扫描目录中的批改结果文件
        scanGradingResults();
    }

    public void scanGradingResults() {
        // 清空表格
        tableModel.setRowCount(0);

        File resultsDir = new File(exerciseManager.getBasePath());
        File[] resultFiles = resultsDir.listFiles((dir, name) ->
                name.endsWith("_results.csv"));

        if (resultFiles != null && resultFiles.length > 0) {
            int totalStudents = 0;
            double totalAccuracy = 0;

            for (File file : resultFiles) {
                GradingSummary summary = parseGradingSummary(file);
                if (summary != null) {
                    Object[] row = {
                            summary.studentName,
                            summary.exerciseSet,
                            summary.totalQuestions,
                            String.format("%.1f%%", summary.averageAccuracy),
                            String.format("%.1f%%", summary.latestAccuracy),
                            summary.gradingTime
                    };
                    tableModel.addRow(row);

                    totalStudents++;
                    totalAccuracy += summary.averageAccuracy;
                }
            }

            // 更新汇总信息，所有学生的平均正确率
            double avgAccuracy = totalStudents > 0 ? totalAccuracy / totalStudents : 0.0;
            summaryLabel.setText(String.format("总计: %d 名学生, 平均正确率: %.1f%%",
                    totalStudents, avgAccuracy));
        } else {
            summaryLabel.setText("未找到批改结果文件");
        }
    }

    private GradingSummary parseGradingSummary(File resultFile) {
        try {
            // 从文件名解析学生姓名和习题集
            String filename = resultFile.getName();
            String baseName = filename.replace("_results.csv", "");
            String[] parts = baseName.split("_");

            if (parts.length >= 3) {
                // 重新构建习题集文件名
                StringBuilder exerciseSetBuilder = new StringBuilder();
                for (int i = 0; i < parts.length - 1; i++) {
                    if (i > 0) exerciseSetBuilder.append("_");
                    exerciseSetBuilder.append(parts[i]);
                }
                exerciseSetBuilder.append(".csv");

                String exerciseSet = exerciseSetBuilder.toString();
                String studentName = parts[parts.length - 1];

                // 从文件内容读取统计信息
                List<String> lines = Files.readAllLines(resultFile.toPath(), StandardCharsets.UTF_8);
                int totalQuestions = 0;
                double accuracySum = 0.0;
                int accuracyCount = 0;
                double currentLatestAcc = 0.0;
                String lastGradingTime = null;

                for (String line : lines) {
                    if (line.startsWith("总题数,")) {
                        String[] stats = line.split(",");
                        if (stats.length >= 2) {
                            try {
                                totalQuestions = Integer.parseInt(stats[1].replace("题", "").trim());
                            } catch (NumberFormatException ignore) {
                                // 单行格式有问题就忽略
                            }
                        }
                    }else if (line.startsWith("正确率,")) {
                        String[] stats = line.split(",");
                        if (stats.length >= 2) {
                            String percentStr = stats[1].replace("%", "").trim();
                            try {
                                double acc = Double.parseDouble(percentStr);
                                accuracySum += acc;
                                accuracyCount++;
                                currentLatestAcc = acc;
                            } catch (NumberFormatException ignore) {
                                // 忽略单行解析错误
                            }
                        }
                    }else if (line.startsWith("批改时间,")) {
                        String[] stats = line.split(",");
                        if (stats.length >= 2) {
                            lastGradingTime = stats[1].trim();
                        }
                    }
                }

                // 一个学生多次练习同一套题的“平均正确率”
                double averageAccuracy = accuracyCount > 0 ? accuracySum / accuracyCount : 0.0;

                if (lastGradingTime == null) {
                    lastGradingTime = new SimpleDateFormat("yyyy-MM-dd HH:mm")
                            .format(new Date(resultFile.lastModified()));
                }

                return new GradingSummary(
                        studentName, exerciseSet, totalQuestions, averageAccuracy, currentLatestAcc,lastGradingTime
                );
            }
        } catch (Exception e) {
            System.err.println("解析批改结果文件失败: " + resultFile.getName());
            e.printStackTrace();
        }
        return null;
    }

    private void loadSingleResultFile(File resultFile) {
        GradingSummary summary = parseGradingSummary(resultFile);
        if (summary != null) {
            // 检查是否已存在该记录（同一学生 + 同一习题集）
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                String existingStudent = (String) tableModel.getValueAt(i, 0);
                String existingSet = (String) tableModel.getValueAt(i, 1);
                if (existingStudent.equals(summary.studentName) &&
                        existingSet.equals(summary.exerciseSet)) {
                    // 更新现有记录
                    tableModel.setValueAt(summary.totalQuestions, i, 2);
                    tableModel.setValueAt(String.format("%.1f%%", summary.averageAccuracy), i, 3);
                    tableModel.setValueAt(summary.gradingTime, i, 4);
                    updateSummaryLabel();
                    return;
                }
            }

            // 添加新记录
            Object[] row = {
                    summary.studentName,
                    summary.exerciseSet,
                    summary.totalQuestions,
                    String.format("%.1f%%", summary.averageAccuracy),
                    String.format("%.1f%%", summary.latestAccuracy),
                    summary.gradingTime
            };
            tableModel.addRow(row);

            // 更新汇总信息
            updateSummaryLabel();
        }
    }

    private void exportToCSV(File outputFile) throws Exception {
        StringBuilder csvContent = new StringBuilder();

        csvContent.append("\uFEFF");

        // 添加表头
        for (int i = 0; i < tableModel.getColumnCount(); i++) {
            csvContent.append(tableModel.getColumnName(i));
            if (i < tableModel.getColumnCount() - 1) {
                csvContent.append(",");
            }
        }
        csvContent.append("\n");

        // 添加数据行
        for (int row = 0; row < tableModel.getRowCount(); row++) {
            for (int col = 0; col < tableModel.getColumnCount(); col++) {
                Object value = tableModel.getValueAt(row, col);
                String cellValue = (value != null) ? value.toString() : "";

                // 处理包含逗号、引号或换行符的单元格
                if (cellValue.contains(",") || cellValue.contains("\"") || cellValue.contains("\n")) {
                    // 转义双引号并用引号包围
                    cellValue = "\"" + cellValue.replace("\"", "\"\"") + "\"";
                }

                csvContent.append(cellValue);
                if (col < tableModel.getColumnCount() - 1) {
                    csvContent.append(",");
                }
            }
            csvContent.append("\n");
        }


        // 使用UTF-8编码写入文件
        try (OutputStreamWriter writer = new OutputStreamWriter(
                new FileOutputStream(outputFile), StandardCharsets.UTF_8)) {
            writer.write(csvContent.toString());
        }
    }

    private void showStudentGradingDetails(String studentName, String exerciseSet) {
        String resultFile = exerciseManager.getBasePath() + exerciseSet.replace(".csv", "") +
                "_" + studentName + "_results.csv";

        File file = new File(resultFile);
        if (file.exists()) {
            try {
                StringBuilder details = new StringBuilder();
                details.append("学生: ").append(studentName).append("\n");
                details.append("习题集: ").append(exerciseSet).append("\n\n");
                details.append("详细批改结果:\n");
                details.append("------------\n");

                List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
                for (String line : lines) {
                    details.append(line).append("\n");
                }

                detailArea.setText(details.toString());

            } catch (Exception ex) {
                detailArea.setText("读取详细批改信息失败: " + ex.getMessage());
            }
        } else {
            detailArea.setText("未找到批改结果文件: " + resultFile);
        }
    }

    private void updateSummaryLabel() {
        int totalStudents = tableModel.getRowCount();
        double totalAccuracy = 0.0;
        for (int row = 0; row < totalStudents; row++) {
            Object value = tableModel.getValueAt(row, 3);
            if (value != null) {
                String text = value.toString().replace("%", "").trim();
                try {
                    totalAccuracy += Double.parseDouble(text);
                } catch (NumberFormatException ignore) {
                    // 忽略单行解析错误
                }
            }
        }

        double avgAccuracy = totalStudents > 0 ? totalAccuracy / totalStudents : 0.0;
        summaryLabel.setText(String.format("总计: %d 名学生, 平均正确率: %.1f%%",
                totalStudents, avgAccuracy));
    }

    // 内部类用于存储批改汇总信息
    private static class GradingSummary {
        String studentName;
        String exerciseSet;
        int totalQuestions;
        double averageAccuracy;
        double latestAccuracy;
        String gradingTime;

        GradingSummary(String studentName, String exerciseSet, int totalQuestions, double averageAccuracy,double latestAccuracy,String gradingTime) {
            this.studentName = studentName;
            this.exerciseSet = exerciseSet;
            this.totalQuestions = totalQuestions;
            this.averageAccuracy = averageAccuracy;
            this.latestAccuracy = latestAccuracy;
            this.gradingTime = gradingTime;
        }
    }

    // 在面板显示时自动扫描结果
    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            scanGradingResults();
        }
    }
}

