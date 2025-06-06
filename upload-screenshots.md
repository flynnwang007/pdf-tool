# 📸 截图上传指南

## 🎯 推荐截图列表

根据你提供的界面截图，建议按以下命名保存到 `screenshots/` 目录：

### 核心界面截图

1. **`home.png`** - 项目首页/工具集总览 (第6、7张截图)
2. **`upload.png`** - 文件上传界面 (第2、8张截图)  
3. **`upload-success.png`** - 上传成功弹窗 (第9张截图)
4. **`file-list.png`** - 文件列表页面 (第3张截图)
5. **`upload-history.png`** - 上传历史记录 (第1张截图)
6. **`pdf-tools.png`** - PDF工具主页面 (第5张截图)
7. **`page-operations.png`** - 页面操作工具 (第4张截图)

## 📝 操作步骤

### 方法1: 直接拖放
1. 将你的截图文件重命名为上述建议名称
2. 直接拖放到 `screenshots/` 目录

### 方法2: 使用命令行
```bash
# 进入项目目录
cd /Users/flynnwang/pdf_tool

# 复制截图文件（请替换为你的实际文件路径）
cp ~/Desktop/screenshot1.png screenshots/home.png
cp ~/Desktop/screenshot2.png screenshots/upload.png
cp ~/Desktop/screenshot3.png screenshots/upload-success.png
cp ~/Desktop/screenshot4.png screenshots/file-list.png
cp ~/Desktop/screenshot5.png screenshots/upload-history.png
cp ~/Desktop/screenshot6.png screenshots/pdf-tools.png
cp ~/Desktop/screenshot7.png screenshots/page-operations.png
```

## 🔄 提交到GitHub

完成截图上传后，执行以下命令：

```bash
# 添加所有文件
git add screenshots/ README.md

# 提交更改
git commit -m "📸 添加项目演示截图和界面展示

- 新增移动端界面演示截图
- 更新README添加项目演示部分
- 包含文件上传、工具操作、页面管理等核心功能展示"

# 推送到远程
git push origin main
```

## ✨ 效果预览

完成后，你的GitHub仓库README将展示：
- 精美的移动端界面设计
- 完整的功能流程演示  
- 专业的项目展示效果
- 更好的用户体验说明

这将大大提升项目的可见性和吸引力！🎉 