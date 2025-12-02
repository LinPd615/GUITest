package GUIArithmeticOperate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 算式基类：负责构建加法减法算式基
 * 遵循单一职责原则：只负责算式的生成和管理
 */
public class OperationBase {
    private List<BinaryOperation> additionBase;     // 加法算式基
    private List<BinaryOperation> subtractionBase;  // 减法算式基
    private int maxOperand;

    public OperationBase(int maxOperand){
        this.maxOperand = maxOperand;
        this.additionBase = new ArrayList<>();
        this.subtractionBase = new ArrayList<>();
        generateOperationBases();
    }

    public OperationBase(){}

    /**
     * 生成加减法算式基，通过行列索引进行约束
     */
    private void generateOperationBases(){
        generateAdditionBase();
        generateSubtractionBase();
    }

    /**
     * 加法算式基
     * 假设约束：行索引i <= 列索引j
     */
    private void generateAdditionBase(){
        additionBase.clear();
        for (int i = 0;i <= maxOperand;i++){
            // 保证和结果在100以内
            for(int j = i;j <= maxOperand-i;j++){     // 正斜三角形 j>=i
                AdditionOperation operation = new AdditionOperation(i, j);
                if (operation.isValid()){
                    additionBase.add(operation);
                }
            }
        }
    }

    /**
     * 减法算式基
     * 假设约束：行索引i >= 列索引j
     */
    private void generateSubtractionBase(){
        subtractionBase.clear();
        for (int i = 0; i <= maxOperand ; i++) {
            for (int j = 0; j <= i; j++) {
                SubtractOperation operation = new SubtractOperation(i, j);
                if (operation.isValid()){
                    subtractionBase.add(operation);
                }
            }
        }
    }

    public List<BinaryOperation> getAdditionBase() {
        return new ArrayList<>(additionBase);
    }

    public List<BinaryOperation> getSubtractionBase() {
        return new ArrayList<>(subtractionBase);
    }

    public List<BinaryOperation> getAllOperations() {
        List<BinaryOperation> all = new ArrayList<>();
        all.addAll(additionBase);
        all.addAll(subtractionBase);
        return all;
    }

    public int getAdditionBaseSize() {
        return additionBase.size();
    }

    public int getSubtractionBaseSize() {
        return subtractionBase.size();
    }

    /**
     * 获取指定数量的随机加法算式
     */
    public List<BinaryOperation> getRandomAdditionOperations(int count) {
        return getRandomOperations(additionBase, count);
    }

    /**
     * 获取指定数量的随机减法算式
     */
    public List<BinaryOperation> getRandomSubtractionOperations(int count) {
        return getRandomOperations(subtractionBase, count);
    }

    /**
     * 从算式基中随机选取指定数量的算式
     */
    private List<BinaryOperation> getRandomOperations(List<BinaryOperation> base, int count){
        if (base.isEmpty()){
            System.out.println("算式基为空！！！");
            return new ArrayList<>();
        }

        // 算式基中题目不足时进行扩展
        if (count >= base.size()){
            return new ArrayList<>(base);
        }

        List<BinaryOperation> temp = new ArrayList<>(base);
        Collections.shuffle(temp);   // 随机打乱列表顺序，从算式基中随机选择指定数量的题目，避免每次都选择相同的题目

        return temp.subList(0, count);
    }
}