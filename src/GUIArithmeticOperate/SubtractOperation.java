package GUIArithmeticOperate;

public class SubtractOperation extends BinaryOperation{
    public SubtractOperation(int op1, int op2) {
        super(op1, op2, '-');
    }

    @Override
    public int calculate() {
        return operand1 - operand2;
    }

    @Override
    public boolean isValid() {
        return operand1 >= operand2;
    }  // 确保减法运算结果不小于0
}

