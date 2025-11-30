package GUIArithmeticOperate;

/**
 * 批改结果：封装批改信息
 */
public class GradingResult {
    private int questionNumber;
    private String question;
    private int correctAnswer;
    private int userAnswer;
    private boolean isCorrect;

    public GradingResult(int questionNumber, String question, int correctAnswer, int userAnswer, boolean isCorrect) {
        this.questionNumber = questionNumber;
        this.question = question;
        this.correctAnswer = correctAnswer;
        this.userAnswer = userAnswer;
        this.isCorrect = isCorrect;
    }

    public int getQuestionNumber() {
        return questionNumber;
    }

    public String getQuestion() {
        return question;
    }

    public int getCorrectAnswer() {
        return correctAnswer;
    }

    public int getUserAnswer() {
        return userAnswer;
    }

    public boolean isCorrect() {
        return isCorrect;
    }
}
