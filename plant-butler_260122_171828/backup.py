#!/usr/bin/env python3
import os
import fnmatch
import zipfile
import time
from pathlib import Path

def parse_gitignore(gitignore_path):
    """读取 .gitignore 并返回忽略规则列表（保留原始行，去除注释和空白）"""
    if not os.path.exists(gitignore_path):
        return []
    with open(gitignore_path, 'r', encoding='utf-8', errors='ignore') as f:
        lines = []
        for line in f:
            line = line.strip()
            if line and not line.startswith('#'):
                lines.append(line)
        return lines
    
def _match_path(rel_path: str, name: str, is_dir: bool, pattern: str) -> bool:
    # 处理目录结尾
    original_pattern = pattern
    if pattern.endswith('/'):
        if not is_dir:
            return False
        pattern = pattern.rstrip('/')

    # Git 规则：只要 pattern 包含 '/'，就视为从仓库根开始的路径模式（即使没有前导 /）
    if '/' in pattern:
        # 标准化：确保 rel_path 和 pattern 都不以 / 开头
        if pattern.startswith('/'):
            pattern = pattern[1:]
        # 精确匹配 or 通配匹配？
        # Git 使用 glob，但常见情况是精确路径或带 * 的通配
        # 我们用 fnmatch，但需注意：nuxt.config.ts 不含通配符 → 应精确匹配
        return fnmatch.fnmatch(rel_path, pattern)
    else:
        # 无 /：匹配所有层级的文件或目录名
        return fnmatch.fnmatch(name, pattern)

def is_ignored(path: Path, ignore_patterns, root: Path):
    try:
        rel_path = str(path.relative_to(root)).replace(os.sep, '/')
    except ValueError:
        # 路径不在 root 下，视为不被忽略（通常不应发生，但防御性处理）
        return False

    name = path.name

    for pattern in ignore_patterns:
        if not pattern:
            continue
        if pattern.startswith('!'):
            neg_pattern = pattern[1:]
            if _match_path(rel_path, name, path.is_dir(), neg_pattern):
                return False
        else:
            if _match_path(rel_path, name, path.is_dir(), pattern):
                return True
    return False


def should_include(path: Path, ignore_patterns, root: Path):
    if is_ignored(path, ignore_patterns, root):
        return False

    # 检查从 path 的父目录向上直到 root（不包括 root 的父目录）
    current = path
    while current != root:
        current = current.parent
        if current == root:
            break
        if is_ignored(current, ignore_patterns, root):
            return False
    return True

def main():
    root = Path('.').resolve()
    gitignore_path = root / '.gitignore'
    ignore_patterns = parse_gitignore(gitignore_path)

    timestamp = time.strftime('%y%m%d_%H%M%S')
    repo_name = Path('.').resolve().name  # 获取当前目录的名称
    zip_filename = f'{repo_name}_{timestamp}.zip'
    zip_path = root / zip_filename  # 完整路径

    with zipfile.ZipFile(zip_filename, 'w', zipfile.ZIP_DEFLATED) as zf:
        for dirpath, dirnames, filenames in os.walk(root):
            current_dir = Path(dirpath)

            # 如果当前目录被忽略，跳过整个子树
            if is_ignored(current_dir, ignore_patterns, root):
                dirnames[:] = []
                continue

            rel_dir = str(current_dir.relative_to(root)).replace(os.sep, '/')
            if rel_dir:
                print(rel_dir + '/')

            # 处理文件
            for fname in filenames:
                filepath = current_dir / fname

                # 排除即将生成的 ZIP 文件（防止自包含）
                if filepath.resolve() == zip_path.resolve():
                    continue

                # 如果文件被 .gitignore 忽略，跳过
                if is_ignored(filepath, ignore_patterns, root):
                    continue

                rel_file = str(filepath.relative_to(root)).replace(os.sep, '/')
                print(rel_file)
                zf.write(filepath, rel_file)

    print(f"\n压缩完成: {zip_filename}")

if __name__ == '__main__':
    main()