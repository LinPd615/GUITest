package GUIArithmeticOperate;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class ExerciseBasePanel extends JPanel {
    private JTextArea baseInfoArea; // 算式基基本信息显示
    private JButton generateBaseBtn;
    private JButton viewBaseBtn;
    private JButton clearBtn;

    private JSpinner maxOperandSpinner;
    private JComboBox<String> baseTypeCombo;    // 算式基类型选择
    private JProgressBar progressBar;   // 进度条

    private OperationBase operationBase;

    public ExerciseBasePanel() {
        operationBase = new OperationBase(100); // 默认最大操作数100
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 顶部设置面板
        add(createSettingsPanel(), BorderLayout.NORTH);
        // 中间信息显示区域
        add(createInfoPanel(), BorderLayout.CENTER);
        // 底部按钮面板
        add(createButtonPanel(), BorderLayout.SOUTH);
    }

    /**
     * 创建设置面板
     */
    private JPanel createSettingsPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 4, 10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("算式基设置"));

        // 最大操作数设置
        panel.add(new JLabel("最大操作数:"));
        maxOperandSpinner = new JSpinner(new SpinnerNumberModel(100, 10, 1000, 10));
        panel.add(maxOperandSpinner);

        // 算式基类型
        panel.add(new JLabel("算式基类型:"));
        baseTypeCombo = new JComboBox<>(new String[]{"加法算式基", "减法算式基", "加减混合算式基"});
        panel.add(baseTypeCombo);

        // 进度条
        panel.add(new JLabel("生成进度:"));
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        panel.add(progressBar);

        return panel;
    }

    /**
     * 算式基信息面板
     * @return
     */
    private JPanel createInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("算式基信息"));

        baseInfoArea = new JTextArea(15, 50);
        baseInfoArea.setEditable(false);
        baseInfoArea.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(baseInfoArea);

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout());

        generateBaseBtn = new JButton("生成算式基");
        viewBaseBtn = new JButton("查看算式基");
        clearBtn = new JButton("清空显示");

        panel.add(generateBaseBtn);
        panel.add(viewBaseBtn);
        panel.add(clearBtn);

        setupEventHandlers();

        return panel;
    }

    private void setupEventHandlers() {
        generateBaseBtn.addActionListener(this::generateOperationBase);
        viewBaseBtn.addActionListener(this::viewOperationBase);
        clearBtn.addActionListener(e -> baseInfoArea.setText(""));
    }

    /**
     * 生成算式基的具体实现
     */
    private void generateOperationBase(ActionEvent e) {
        try {
            int maxOperand = (int) maxOperandSpinner.getValue();
            String baseType = (String) baseTypeCombo.getSelectedItem();

            // 更新OperationBase的最大操作数
            operationBase = new OperationBase(maxOperand);

            // 在后台线程中生成算式基，避免界面卡顿
            SwingWorker<Void, String> worker = new SwingWorker<Void, String>() {
                @Override
                protected Void doInBackground() throws Exception {
                    publish("开始生成" + baseType + "...\n");
                    publish("最大操作数: " + maxOperand + "\n");
                    publish("生成时间: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "\n");
                    publish("----------------------------------------\n");

                    long startTime = System.currentTimeMillis();

                    // 根据选择的类型生成不同的算式基
                    switch (baseType) {
                        case "加法算式基":
                            generateAdditionBase();
                            break;
                        case "减法算式基":
                            generateSubtractionBase();
                            break;
                        case "加减混合算式基":
                            generateMixedBase();
                            break;
                    }

                    long endTime = System.currentTimeMillis();
                    long duration = endTime - startTime;

                    publish("----------------------------------------\n");
                    publish("生成完成！耗时: " + duration + " 毫秒\n");

                    return null;
                }

                @Override
                protected void process(List<String> chunks) {
                    for (String message : chunks) {
                        baseInfoArea.append(message);
                    }
                    // 滚动到最新内容
                    baseInfoArea.setCaretPosition(baseInfoArea.getDocument().getLength());
                }

                @Override
                protected void done() {
                    generateBaseBtn.setEnabled(true);
                    progressBar.setValue(100);
                }
            };

            generateBaseBtn.setEnabled(false);
            progressBar.setValue(0);
            worker.execute();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "生成算式基失败: " + ex.getMessage(),
                    "错误", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    /**
     * 生成加法算式基 - 正斜三角形
     */
    private void generateAdditionBase() {
        try {
            List<BinaryOperation> additionBase = operationBase.getAdditionBase();
            int totalCount = additionBase.size();

            baseInfoArea.append("=== 加法算式基生成详情 ===\n");
            baseInfoArea.append("算式基大小: " + totalCount + " 个算式\n");
            baseInfoArea.append("生成规则: 正斜三角形 (i <= j)\n\n");

            // 显示前20个算式作为示例
            baseInfoArea.append("前20个算式示例:\n");
            int displayCount = Math.min(20, totalCount);
            for (int i = 0; i < displayCount; i++) {
                BinaryOperation operation = additionBase.get(i);
                baseInfoArea.append(String.format("%3d. %2d + %2d = %3d\n",
                        i + 1, operation.getOperand1(), operation.getOperand2(), operation.calculate()));
            }

            if (totalCount > 20) {
                baseInfoArea.append("... 还有 " + (totalCount - 20) + " 个算式\n");
            }

            // 统计信息
            baseInfoArea.append("\n统计信息:\n");
            baseInfoArea.append("- 最小和: " + getMinSum(additionBase) + "\n");
            baseInfoArea.append("- 最大和: " + getMaxSum(additionBase) + "\n");
            baseInfoArea.append("- 平均和: " + String.format("%.2f", getAverageSum(additionBase)) + "\n");

            // 保存到文件
            saveBaseToFile(additionBase, "addition_base.csv", "加法");

        } catch (Exception e) {
            throw new RuntimeException("生成加法算式基失败", e);
        }
    }

    /**
     * 生成减法算式基 - 倒斜三角形
     */
    private void generateSubtractionBase() {
        try {
            List<BinaryOperation> subtractionBase = operationBase.getSubtractionBase();
            int totalCount = subtractionBase.size();

            baseInfoArea.append("=== 减法算式基生成详情 ===\n");
            baseInfoArea.append("算式基大小: " + totalCount + " 个算式\n");
            baseInfoArea.append("生成规则: 倒斜三角形 (i >= j)\n\n");

            // 显示前20个算式作为示例
            baseInfoArea.append("前20个算式示例:\n");
            int displayCount = Math.min(20, totalCount);
            for (int i = 0; i < displayCount; i++) {
                BinaryOperation operation = subtractionBase.get(i);
                baseInfoArea.append(String.format("%3d. %2d - %2d = %3d\n",
                        i + 1, operation.getOperand1(), operation.getOperand2(), operation.calculate()));
            }

            if (totalCount > 20) {
                baseInfoArea.append("... 还有 " + (totalCount - 20) + " 个算式\n");
            }

            // 统计信息
            baseInfoArea.append("\n统计信息:\n");
            baseInfoArea.append("- 最小差: " + getMinDifference(subtractionBase) + "\n");
            baseInfoArea.append("- 最大差: " + getMaxDifference(subtractionBase) + "\n");
            baseInfoArea.append("- 平均差: " + String.format("%.2f", getAverageDifference(subtractionBase)) + "\n");

            // 保存到文件
            saveBaseToFile(subtractionBase, "subtraction_base.csv", "减法");

        } catch (Exception e) {
            throw new RuntimeException("生成减法算式基失败", e);
        }
    }

    /**
     * 生成混合算式基
     */
    private void generateMixedBase() {
        try {
            List<BinaryOperation> additionBase = operationBase.getAdditionBase();
            List<BinaryOperation> subtractionBase = operationBase.getSubtractionBase();

            int additionCount = additionBase.size();
            int subtractionCount = subtractionBase.size();
            int totalCount = additionCount + subtractionCount;

            baseInfoArea.append("=== 加减混合算式基生成详情 ===\n");
            baseInfoArea.append("总算式数量: " + totalCount + " 个算式\n");
            baseInfoArea.append("- 加法算式: " + additionCount + " 个\n");
            baseInfoArea.append("- 减法算式: " + subtractionCount + " 个\n\n");

            // 显示前10个加法和10个减法算式作为示例
            baseInfoArea.append("加法算式示例:\n");
            int addDisplayCount = Math.min(10, additionCount);
            for (int i = 0; i < addDisplayCount; i++) {
                BinaryOperation operation = additionBase.get(i);
                baseInfoArea.append(String.format("%3d. %2d + %2d = %3d\n",
                        i + 1, operation.getOperand1(), operation.getOperand2(), operation.calculate()));
            }

            baseInfoArea.append("\n减法算式示例:\n");
            int subDisplayCount = Math.min(10, subtractionCount);
            for (int i = 0; i < subDisplayCount; i++) {
                BinaryOperation operation = subtractionBase.get(i);
                baseInfoArea.append(String.format("%3d. %2d - %2d = %3d\n",
                        i + 1, operation.getOperand1(), operation.getOperand2(), operation.calculate()));
            }

            // 统计信息
            baseInfoArea.append("\n统计信息:\n");
            baseInfoArea.append("- 加法最小和: " + getMinSum(additionBase) + "\n");
            baseInfoArea.append("- 加法最大和: " + getMaxSum(additionBase) + "\n");
            baseInfoArea.append("- 减法最小差: " + getMinDifference(subtractionBase) + "\n");
            baseInfoArea.append("- 减法最大差: " + getMaxDifference(subtractionBase) + "\n");

            // 保存到文件
            saveMixedBaseToFile(additionBase, subtractionBase, "mixed_base.csv");

        } catch (Exception e) {
            throw new RuntimeException("生成混合算式基失败", e);
        }
    }

    /**
     * 保存算式基到文件
     */
    private void saveBaseToFile(List<BinaryOperation> base, String filename, String type) {
        try {
            // 统一确保目录存在
            FileConfig.ensureExerciseDirExists();

            String fullPath = FileConfig.EXERCISE_DIR + filename;
            try (java.io.PrintWriter writer = new java.io.PrintWriter(
                    new java.io.OutputStreamWriter(
                            new java.io.FileOutputStream(fullPath),
                            java.nio.charset.StandardCharsets.UTF_8))) {

                writer.write("\uFEFF"); // UTF-8 BOM
                writer.println("序号,题型,操作数1,操作数2,运算符,结果");

                for (int i = 0; i < base.size(); i++) {
                    BinaryOperation operation = base.get(i);
                    writer.printf("%d,%s,%d,%d,%c,%d%n",
                            i + 1,
                            type,
                            operation.getOperand1(),
                            operation.getOperand2(),
                            operation.getOperator(),
                            operation.calculate()
                    );
                }
            }

            baseInfoArea.append("\n算式基已保存到: " + fullPath + "\n");
            baseInfoArea.append("文件大小: " + new File(fullPath).length() + " 字节\n");

        } catch (Exception e) {
            baseInfoArea.append("保存文件失败: " + e.getMessage() + "\n");
        }
    }

    /**
     * 保存混合算式基到文件
     */
    private void saveMixedBaseToFile(List<BinaryOperation> additionBase,
                                     List<BinaryOperation> subtractionBase,
                                     String filename) {
        try {
            FileConfig.ensureExerciseDirExists();
            String fullPath = FileConfig.EXERCISE_DIR + filename;

            try (java.io.PrintWriter writer = new java.io.PrintWriter(
                    new java.io.OutputStreamWriter(
                            new java.io.FileOutputStream(fullPath),
                            java.nio.charset.StandardCharsets.UTF_8))) {

                writer.write("\uFEFF");
                writer.println("序号,题型,操作数1,操作数2,运算符,结果");

                int index = 1;

                // 写入加法算式
                for (BinaryOperation operation : additionBase) {
                    writer.printf("%d,加法,%d,%d,%c,%d%n",
                            index++,
                            operation.getOperand1(),
                            operation.getOperand2(),
                            operation.getOperator(),
                            operation.calculate()
                    );
                }

                // 写入减法算式
                for (BinaryOperation operation : subtractionBase) {
                    writer.printf("%d,减法,%d,%d,%c,%d%n",
                            index++,
                            operation.getOperand1(),
                            operation.getOperand2(),
                            operation.getOperator(),
                            operation.calculate()
                    );
                }
            }

            baseInfoArea.append("\n混合算式基已保存到: " + fullPath + "\n");
            baseInfoArea.append("文件大小: " + new File(fullPath).length() + " 字节\n");

        } catch (Exception e) {
            baseInfoArea.append("保存混合算式基文件失败: " + e.getMessage() + "\n");
        }
    }

    // 统计方法
    private int getMinSum(List<BinaryOperation> base) {
        return base.stream().mapToInt(BinaryOperation::calculate).min().orElse(0);
    }

    private int getMaxSum(List<BinaryOperation> base) {
        return base.stream().mapToInt(BinaryOperation::calculate).max().orElse(0);
    }

    private double getAverageSum(List<BinaryOperation> base) {
        return base.stream().mapToInt(BinaryOperation::calculate).average().orElse(0);
    }

    private int getMinDifference(List<BinaryOperation> base) {
        return base.stream().mapToInt(BinaryOperation::calculate).min().orElse(0);
    }

    private int getMaxDifference(List<BinaryOperation> base) {
        return base.stream().mapToInt(BinaryOperation::calculate).max().orElse(0);
    }

    private double getAverageDifference(List<BinaryOperation> base) {
        return base.stream().mapToInt(BinaryOperation::calculate).average().orElse(0);
    }

    private void viewOperationBase(ActionEvent e) {
        // 查看算式基的实现
        baseInfoArea.append("当前内存中的算式基信息:\n");
        baseInfoArea.append("- 加法算式数量: " + operationBase.getAdditionBase().size() + "\n");
        baseInfoArea.append("- 减法算式数量: " + operationBase.getSubtractionBase().size() + "\n");
        baseInfoArea.append("----------------------------------------\n");
    }

    private void loadBaseFromFile(File file) {
        try {
            baseInfoArea.append("正在加载算式基文件: " + file.getName() + "\n");
            // 这里可以实现从文件加载算式基的逻辑
            baseInfoArea.append("文件加载完成\n");
        } catch (Exception ex) {
            baseInfoArea.append("加载失败: " + ex.getMessage() + "\n");
        }
    }
}
