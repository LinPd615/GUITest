package GUIArithmeticOperate;

public abstract class BinaryOperation {
    protected int operand1;
    protected int operand2;
    protected char operator; // 运算符

    public BinaryOperation(int op1, int op2, char op) {
        this.operand1 = op1;
        this.operand2 = op2;
        this.operator = op;
    }

    // 抽象方法，子类必须实现
    public abstract int calculate();   // 抽象出运算方法
    public abstract boolean isValid(); // 验证算式是否合法


    public String toString() {
        return operand1 + " " + operator + " " + operand2 + " = ";
    }

    public String getFullExpression() {
        return operand1 + " " + operator + " " + operand2 + " = " + calculate();
    }

    public int getOperand1() {
        return operand1;
    }

    public int getOperand2() {
        return operand2;
    }

    public char getOperator() {
        return operator;
    }
}


