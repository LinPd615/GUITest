package GUIArithmeticOperate;

public class AdditionOperation extends BinaryOperation{
    public AdditionOperation(int op1, int op2) {
        super(op1, op2, '+');
    }

    @Override
    public int calculate() {
        return operand1+operand2;
    }

    @Override
    public boolean isValid() {
        return true;
    }
}

