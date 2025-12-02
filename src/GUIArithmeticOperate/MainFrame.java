package GUIArithmeticOperate;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class MainFrame extends JFrame {
    private CardLayout cardLayout; // 卡片布局管理器
    private JPanel mainPanel; // 主面板容器

    // 各个功能面板
    private ExerciseGenerationPanel generationPanel; // 题目生成
    private ExerciseBasePanel basePanel;
    private OnlinePracticePanel practicePanel;
    private GradingPanel gradingPanel;

    public MainFrame() {
        initializeUI();
        setupEventHandlers();
    }

    private void initializeUI() {
        setTitle("数学口算练习系统");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);

        // 主布局，卡片布局
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // 创建各个功能面板
        generationPanel = new ExerciseGenerationPanel();
        basePanel = new ExerciseBasePanel();
        practicePanel = new OnlinePracticePanel();
        gradingPanel = new GradingPanel();

        mainPanel.add(generationPanel, "GENERATION");
        mainPanel.add(basePanel, "BASE");
        mainPanel.add(practicePanel, "PRACTICE");
        mainPanel.add(gradingPanel, "GRADING");

        // 导航菜单
        JPanel navPanel = createNavigationPanel();

        setLayout(new BorderLayout());
        add(navPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
    }

    /**
     * 导航菜单面板
     * @return
     */
    private JPanel createNavigationPanel() {
        JPanel navPanel = new JPanel(new FlowLayout());
        navPanel.setBackground(new Color(70, 130, 180));
        navPanel.setPreferredSize(new Dimension(1000, 60));

        String[] buttons = {
                "题目生成", "算式基管理", "在线练习", "批量批改"
        };

        for (int i = 0; i < buttons.length; i++) {
            JButton button = new JButton(buttons[i]);
            button.setPreferredSize(new Dimension(120, 40));
            button.setFont(new Font("微软雅黑", Font.BOLD, 14));
            button.setBackground(new Color(100, 149, 237));
            button.setForeground(Color.BLACK);

            final String panelName = getPanelName(i);
            button.addActionListener(e -> {
                cardLayout.show(mainPanel, panelName);
                // 切换到批改面板时自动刷新
                if ("GRADING".equals(panelName)){
                    gradingPanel.scanGradingResults();
                }
            });

            navPanel.add(button);
        }

        return navPanel;
    }

    /**
     * 根据索引获取面板名称
     * @param index
     * @return
     */
    private String getPanelName(int index) {
        switch (index) {
            case 0: return "GENERATION";
            case 1: return "BASE";
            case 2: return "PRACTICE";
            case 3: return "GRADING";
            default: return "GENERATION";
        }
    }

    /**
     * 面板间通信
     */
    private void setupEventHandlers() {
        // 面板间通信的事件处理
        generationPanel.addExerciseGeneratedListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 当题目生成完成后，刷新在线练习面板的文件列表
                practicePanel.refreshExerciseFiles();

                // 显示提示信息
                JOptionPane.showMessageDialog(MainFrame.this,
                        "题目生成完成！习题集已更新，可以在在线练习中使用。",
                        "生成成功",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });

        // 临时调试：添加测试按钮到导航栏
        JButton debugBtn = new JButton("调试");
        debugBtn.addActionListener(e -> {
            System.out.println("=== 调试信息 ===");
            System.out.println("当前工作目录: " + new File(".").getAbsolutePath());
            practicePanel.refreshExerciseFiles();
        });

        // 获取导航面板并添加调试按钮
        ((JPanel)getContentPane().getComponent(0)).add(debugBtn);
    }
}