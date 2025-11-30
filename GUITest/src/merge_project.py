import os

# 在这里设置你想要忽略的文件夹和文件后缀
IGNORE_DIRS = {'.git', '__pycache__', 'node_modules', '.idea', 'venv', 'bin', 'obj'}
# 在这里设置你需要读取的文件后缀（根据你的项目语言调整）
TARGET_EXTENSIONS = {'.py', '.js', '.java', '.cpp', '.h', '.cs', '.go', '.rs', '.php', '.html', '.css', '.json', '.txt', '.md'}

def merge_files(output_file='project_code.txt'):
    with open(output_file, 'w', encoding='utf-8') as outfile:
        # 遍历当前目录
        for root, dirs, files in os.walk('.'):
            # 移除不需要遍历的目录
            dirs[:] = [d for d in dirs if d not in IGNORE_DIRS]

            for file in files:
                ext = os.path.splitext(file)[1]
                if ext in TARGET_EXTENSIONS and file != 'merge_project.py':
                    file_path = os.path.join(root, file)
                    try:
                        with open(file_path, 'r', encoding='utf-8') as infile:
                            content = infile.read()
                            # 写入分隔符和文件路径，方便AI识别
                            outfile.write(f"\n{'='*20}\n")
                            outfile.write(f"File: {file_path}\n")
                            outfile.write(f"{'='*20}\n\n")
                            outfile.write(content)
                            outfile.write("\n")
                            print(f"已合并: {file_path}")
                    except Exception as e:
                        print(f"跳过文件 {file_path}: {e}")

if __name__ == '__main__':
    print("开始合并文件...")
    merge_files()
    print("完成！请上传生成的 'project_code.txt' 文件。")