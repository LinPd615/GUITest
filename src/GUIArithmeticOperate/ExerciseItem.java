package GUIArithmeticOperate;

/**
 * 习题项：封装习题信息
 */
public class ExerciseItem {
    private int questionNumber;
    private String type;
    private String question;
    private int correctAnswer;
    private Integer studentAnswer; // 学生答案
    private boolean isCorrect;     // 是否正确

    public ExerciseItem(int questionNumber, String type, String question, int correctAnswer) {
        this.questionNumber = questionNumber;
        this.type = type;
        this.question = question;
        this.correctAnswer = correctAnswer;
        this.studentAnswer = null;
        this.isCorrect = false;
    }

    public int getQuestionNumber() { return questionNumber; }
    public String getType() { return type; }
    public String getQuestion() { return question; }
    public int getCorrectAnswer() { return correctAnswer; }

    public Integer getStudentAnswer() { return studentAnswer; }
    public void setStudentAnswer(Integer studentAnswer) {
        this.studentAnswer = studentAnswer;
        this.isCorrect = (studentAnswer != null && studentAnswer == correctAnswer);
    }
    public boolean isCorrect() { return isCorrect; }
    public void setCorrect(boolean correct) { this.isCorrect = correct; }
}