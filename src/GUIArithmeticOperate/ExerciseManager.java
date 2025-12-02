package GUIArithmeticOperate;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 负责习题的生成、存储和批改
 * 专门处理文件IO和批改逻辑
 */
public class ExerciseManager {
    private OperationBase operationBase;
    private Map<String,List<ExerciseItem>> exerciseSets;    // 存储多套习题
    private Map<String,Map<Integer,Integer>> studentAnswers;    // 存储学生答案
    private String basePath = FileConfig.EXERCISE_DIR;

    public ExerciseManager(OperationBase operationBase) {
        this.operationBase = operationBase;
        this.exerciseSets = new HashMap<>();
        this.studentAnswers = new HashMap<>();

        // 通过 FileConfig 确保目录存在
        FileConfig.ensureExerciseDirExists();
    }

    // 批量生成多套习题文件
    public void  generateMultipleExerciseSets(int setCount, int additionCount, int subtractionCount, String baseFilename) {
        // 确保文件名包含完整路径
        if (!baseFilename.startsWith(basePath)){
            baseFilename = basePath + baseFilename;
        }

        for (int i = 1; i <= setCount; i++) {
            String filename = baseFilename + "_set" + i + ".csv";
            generateExerciseSet(filename, additionCount, subtractionCount, i);
        }
        System.out.printf("成功生成 %d 套习题文件%n", setCount);
    }

    // 生成单套习题文件， CSV 头部增加“正确答案”列，解决严重依赖“题目字符串”反向解析答案
    private void generateExerciseSet(String filename, int additionCount, int subtractionCount, int setNumber) {
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(filename), StandardCharsets.UTF_8))) {

            writer.write("\uFEFF");
            writer.println("题号,题型,题目,学生答案");

            int questionNumber = 1;
            List<ExerciseItem> exerciseSet = new ArrayList<>();

            // 生成加法习题
            List<BinaryOperation> additionExercises = operationBase.getRandomAdditionOperations(additionCount);
            for (BinaryOperation operation : additionExercises) {
                String question = formatQuestion(operation);
                writer.printf("%d,加法,\"%s\",%d%n", questionNumber, question,operation.calculate());

                // 保存到内存中用于后续批改
                exerciseSet.add(new ExerciseItem(
                        questionNumber,
                        "加法",
                        question,
                        operation.calculate() // 保存正确答案
                ));
                questionNumber++;
            }

            // 生成减法习题
            List<BinaryOperation> subtractionExercises = operationBase.getRandomSubtractionOperations(subtractionCount);
            for (BinaryOperation operation : subtractionExercises) {
                String question = formatQuestion(operation);
                writer.printf("%d,减法,\"%s\",%d%n", questionNumber, question,operation.calculate());

                exerciseSet.add(new ExerciseItem(
                        questionNumber,
                        "减法",
                        question,
                        operation.calculate() // 保存正确答案
                ));
                questionNumber++;
            }

            // 保存习题集到内存 - 使用文件名作为key，而不是完整路径
            String fileKey = new File(filename).getName();
            exerciseSets.put(fileKey, exerciseSet);

            System.out.printf("习题集 %d 已生成: %s (%d道题)%n",
                    setNumber, filename, exerciseSet.size());

        } catch (IOException e) {
            System.err.println("生成习题集失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 静态方法加载习题集,用于在线练习
     */
    public static List<ExerciseItem> loadExerciseSetStatic(String filename) {
        try {
            // 使用完整路径加载文件
            String fullPath = FileConfig.EXERCISE_DIR + filename;
            File file = new File(fullPath);

            if (!file.exists()) {
                System.err.println("文件不存在: " + fullPath);
                return null;
            }

            System.out.println("文件存在，大小: " + file.length() + " bytes");

            List<ExerciseItem> exercises = new ArrayList<>();

            // 文件读取
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {

                String line;
                boolean firstLine = true;

                while ((line = reader.readLine()) != null) {
                    // 跳过UTF-8 BOM和空行
                    if (firstLine) {
                        line = line.replace("\uFEFF", "");
                        firstLine = false;
                    }

                    // 跳过标题行和空行
                    if (line.trim().isEmpty() || line.startsWith("题号") || line.startsWith("题目")) {
                        continue;
                    }

                    // 解析CSV行
                    String[] parts = line.split(",", -1); // -1 保留空字段
                    if (parts.length >= 4) {
                        try {
                            int questionNumber = Integer.parseInt(parts[0].trim());
                            String type = parts[1].trim();
                            String question = parts[2].trim().replace("\"", ""); // 移除引号

                            // 直接读取第4列作为正确答案
                            int correctAnswer = 0;
                            try {
                                correctAnswer = Integer.parseInt(parts[3].trim());
                            }catch (NumberFormatException e){
                                // 如果文件没有第4列，作为兼容，可以使用从题目中解析答案
                                correctAnswer = parseAnswerFromQuestion(question, type);
                            }
                            exercises.add(new ExerciseItem(questionNumber, type, question, correctAnswer));
                        } catch (NumberFormatException ex) {
                            System.err.println("解析题目编号失败: " + line);
                        }
                    }
                }
            }
            return exercises;

        } catch (Exception e) {
            System.err.println("加载习题集异常: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // 从题目中解析答案
    private static int parseAnswerFromQuestion(String question, String type) {
        try {
            if (question.contains("＋") || question.contains("+")) { // 全角加号
                String separator = question.contains("＋") ? "＋" : "\\+";
                String[] operands = question.split(separator);
                if (operands.length == 2) {
                    int a = Integer.parseInt(operands[0].trim());
                    int b = Integer.parseInt(operands[1].trim());
                    return a + b;
                }
            } else if (question.contains("—") || question.contains("-")) { // 全角减号
                String separator = question.contains("—") ? "—" : "-";
                String[] operands = question.split(separator);
                if (operands.length == 2) {
                    int a = Integer.parseInt(operands[0].trim());
                    int b = Integer.parseInt(operands[1].trim());
                    return a - b;
                }
            }
        } catch (NumberFormatException ex) {
            System.err.println("解析题目答案失败: " + question);
        }

        return 0; // 默认值
    }

    /**
     * 格式化题目显示
     */
    private String formatQuestion(BinaryOperation operation) {
        if (operation instanceof AdditionOperation) {
            return operation.getOperand1() + " ＋ " + operation.getOperand2();
        } else if (operation instanceof SubtractOperation) {
            return operation.getOperand1() + " — " + operation.getOperand2();
        } else {
            return operation.toString().replace("=", "").trim();
        }
    }

    // 获取习题集列表
    public Set<String> getExerciseSets() {
        return exerciseSets.keySet();
    }

    // 获取学生列表
    public Set<String> getStudents() {
        return studentAnswers.keySet();
    }

    // 获取基础路径
    public String getBasePath() {
        return FileConfig.EXERCISE_DIR;
    }
}
