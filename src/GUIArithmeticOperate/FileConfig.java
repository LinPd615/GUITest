package GUIArithmeticOperate;

import java.io.File;

/**
 * 统一的文件路径配置类
 */
public class FileConfig {
    // 习题集文件存储目录
    public static final String EXERCISE_DIR =
            System.getProperty("user.home")
                    + File.separator + "exercise"
                    + File.separator;

    /**
     * 确保目录存在（所有写文件前都可以先调用）
     */
    public static void ensureExerciseDirExists() {
        File dir = new File(EXERCISE_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }
}