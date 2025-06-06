<template>
  <div class="tools-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <h1 class="page-title">PDF工具</h1>
      <p class="page-subtitle">选择需要的PDF处理功能</p>
    </div>

    <!-- 快速工具 -->
    <div class="quick-tools-section">
      <div class="section-header">
        <h2 class="section-title">
          <span class="title-icon">⚡</span>
          快速工具
        </h2>
      </div>
      <div class="quick-tools-grid">
        <div 
          v-for="tool in quickTools"
          :key="tool.id"
          class="quick-tool-card"
          @click="handleToolClick(tool)"
        >
          <div class="tool-icon">{{ tool.emoji }}</div>
          <span class="tool-name">{{ tool.name }}</span>
        </div>
      </div>
    </div>

    <!-- 工具分类 -->
    <div class="tools-section">
      <!-- 常用工具 -->
      <div class="tool-category">
        <h2 class="category-title">
          <span class="category-icon">🔥</span>
          常用工具
        </h2>
        <div class="tools-card">
          <div 
            v-for="tool in popularTools"
            :key="tool.id"
            class="tool-item"
            @click="handleToolClick(tool)"
          >
            <div class="tool-icon-wrapper">
              <span class="tool-emoji">{{ tool.emoji }}</span>
            </div>
            <div class="tool-info">
              <h3 class="tool-title">{{ tool.name }}</h3>
              <p class="tool-description">{{ tool.description }}</p>
            </div>
            <div class="tool-arrow">
              <el-icon><ArrowRight /></el-icon>
            </div>
          </div>
        </div>
      </div>

      <!-- 页面操作 -->
      <div class="tool-category">
        <h2 class="category-title">
          <span class="category-icon">📄</span>
          页面操作
        </h2>
        <div class="tools-card">
          <div 
            v-for="tool in pageTools"
            :key="tool.id"
            class="tool-item"
            @click="handleToolClick(tool)"
          >
            <div class="tool-icon-wrapper">
              <span class="tool-emoji">{{ tool.emoji }}</span>
            </div>
            <div class="tool-info">
              <h3 class="tool-title">{{ tool.name }}</h3>
              <p class="tool-description">{{ tool.description }}</p>
            </div>
            <div class="tool-arrow">
              <el-icon><ArrowRight /></el-icon>
            </div>
          </div>
        </div>
      </div>

      <!-- 格式转换 -->
      <div class="tool-category">
        <h2 class="category-title">
          <span class="category-icon">🔄</span>
          格式转换
        </h2>
        <div class="tools-card">
          <div 
            v-for="tool in conversionTools"
            :key="tool.id"
            :class="['tool-item', { disabled: tool.disabled }]"
            @click="tool.disabled ? null : handleToolClick(tool)"
          >
            <div class="tool-icon-wrapper">
              <span class="tool-emoji">{{ tool.emoji }}</span>
            </div>
            <div class="tool-info">
              <h3 class="tool-title">{{ tool.name }}</h3>
              <p class="tool-description">{{ tool.description }}</p>
            </div>
            <div class="tool-arrow">
              <el-icon><ArrowRight /></el-icon>
            </div>
          </div>
        </div>
      </div>

      <!-- 安全工具 -->
      <div class="tool-category">
        <h2 class="category-title">
          <span class="category-icon">🔒</span>
          安全工具
        </h2>
        <div class="tools-card">
          <div 
            v-for="tool in securityTools"
            :key="tool.id"
            class="tool-item"
            @click="handleToolClick(tool)"
          >
            <div class="tool-icon-wrapper">
              <span class="tool-emoji">{{ tool.emoji }}</span>
            </div>
            <div class="tool-info">
              <h3 class="tool-title">{{ tool.name }}</h3>
              <p class="tool-description">{{ tool.description }}</p>
            </div>
            <div class="tool-arrow">
              <el-icon><ArrowRight /></el-icon>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 工具操作弹窗 -->
    <el-dialog
      v-model="showToolDialog"
      :title="selectedTool?.name"
      width="calc(100vw - 32px)"
      class="tool-dialog"
      center
      :style="dialogStyle"
    >
      <div class="tool-content" v-if="selectedTool">
        <!-- 工具说明 -->
        <div class="tool-header">
          <div class="tool-large-icon">{{ selectedTool.emoji }}</div>
          <div class="tool-details">
            <h2 class="tool-name">{{ selectedTool.name }}</h2>
            <p class="tool-desc">{{ selectedTool.fullDescription || selectedTool.description }}</p>
          </div>
        </div>

        <!-- 文件选择 -->
        <div class="file-selection">
          <h3 class="selection-title">选择文件</h3>
          
          <!-- 文件来源选择 -->
          <div class="file-source-tabs">
            <button 
              :class="['source-tab', { active: fileSource === 'upload' }]"
              @click="fileSource = 'upload'"
            >
              <span class="tab-icon">📁</span>
              上传新文件
            </button>
            <button 
              :class="['source-tab', { active: fileSource === 'existing' }]"
              @click="fileSource = 'existing'; loadExistingFiles()"
            >
              <span class="tab-icon">📂</span>
              选择已有文件
            </button>
          </div>

          <!-- 上传新文件 -->
          <div v-if="fileSource === 'upload'" class="file-upload-area" @click="handleFileUpload">
            <div class="upload-icon">📁</div>
            <div class="upload-text">
              <div class="upload-title">{{ getUploadTitle() }}</div>
              <div class="upload-subtitle">{{ getUploadSubtitle() }}</div>
            </div>
            <input
              ref="fileInput"
              type="file"
              :accept="getAcceptType()"
              multiple
              style="display: none"
              @change="handleFileChange"
            >
          </div>

          <!-- 选择已有文件 -->
          <div v-if="fileSource === 'existing'" class="existing-files-section">
            <div v-if="loadingFiles" class="loading-files">
              <el-icon class="is-loading"><Loading /></el-icon>
              加载文件列表...
            </div>
            
            <div v-else-if="existingFiles.length === 0" class="no-files">
              <div class="no-files-icon">📄</div>
              <div class="no-files-text">暂无已上传的文件</div>
              <el-button type="primary" @click="fileSource = 'upload'">
                去上传文件
              </el-button>
            </div>
            
            <div v-else class="existing-files-grid">
              <div 
                v-for="file in existingFiles"
                :key="file.id"
                :class="['existing-file-item', { selected: selectedExistingFileIds.includes(file.id) }]"
                @click="toggleExistingFile(file)"
              >
                <div class="file-icon">📄</div>
                <div class="file-info">
                  <div class="file-name" :title="file.originalName">{{ file.originalName }}</div>
                  <div class="file-meta">
                    <span class="file-size">{{ formatFileSize(file.fileSize) }}</span>
                    <span class="file-date">{{ formatDate(file.createdAt) }}</span>
                  </div>
                </div>
                <div v-if="selectedExistingFileIds.includes(file.id)" class="selected-indicator">
                  <el-icon><Check /></el-icon>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 已选择的文件 -->
          <div v-if="selectedFiles.length > 0 || selectedExistingFileIds.length > 0" class="selected-files">
            <h4 class="files-title">
              已选择 {{ getTotalSelectedFiles() }} 个文件
            </h4>
            <div class="files-list">
              <!-- 上传的新文件 -->
              <div 
                v-for="(file, index) in selectedFiles"
                :key="`upload-${index}`"
                class="file-item"
              >
                <div class="file-icon">📄</div>
                <div class="file-info">
                  <div class="file-name">{{ file.name }}</div>
                  <div class="file-size">{{ formatFileSize(file.size) }}</div>
                </div>
                <el-icon 
                  class="file-remove" 
                  @click="removeFile(index)"
                >
                  <Close />
                </el-icon>
              </div>
              
              <!-- 已有文件 -->
              <div 
                v-for="fileId in selectedExistingFileIds"
                :key="`existing-${fileId}`"
                class="file-item"
              >
                <div class="file-icon">📄</div>
                <div class="file-info">
                  <div class="file-name">{{ getExistingFileName(fileId) }}</div>
                  <div class="file-size">{{ getExistingFileSize(fileId) }}</div>
                </div>
                <el-icon 
                  class="file-remove" 
                  @click="removeExistingFile(fileId)"
                >
                  <Close />
                </el-icon>
              </div>
            </div>
          </div>
        </div>

        <!-- 工具参数设置 -->
        <div v-if="selectedTool.options" class="tool-options">
          <h3 class="options-title">参数设置</h3>
          <div class="options-list">
            <!-- 拆分类型设置 -->
            <div v-if="selectedTool.id === 'split'" class="option-item">
              <label class="option-label">拆分方式</label>
              <el-radio-group v-model="toolParams.splitType" class="option-control">
                <el-radio value="pages">按页数拆分</el-radio>
                <el-radio value="range">按范围拆分</el-radio>
              </el-radio-group>
              
              <!-- 按页数拆分的页数设置 -->
              <div v-if="toolParams.splitType === 'pages'" class="sub-option">
                <label class="sub-option-label">每个文件页数</label>
                <el-input-number 
                  v-model="toolParams.pageCount" 
                  :min="1" 
                  :max="100"
                  class="page-count-input"
                />
              </div>
              
              <!-- 按范围拆分的范围设置 -->
              <div v-if="toolParams.splitType === 'range'" class="sub-option">
                <label class="sub-option-label">页面范围 (例如: 1-5,8,10-15)</label>
                <el-input
                  v-model="toolParams.pageRanges"
                  placeholder="1-5,8,10-15"
                  class="range-input"
                />
                <div class="range-help">
                  支持单页（如8）、范围（如1-5）、组合（如1-5,8,10-15）
                </div>
              </div>
            </div>

            <!-- 页面范围设置 -->
            <div v-if="selectedTool.options.includes('pageRange') && selectedTool.id !== 'split' && selectedTool.id !== 'reorder'" class="option-item">
              <label class="option-label">页面范围</label>
              <el-radio-group v-model="toolParams.pageRange" class="option-control">
                <el-radio value="all">全部页面</el-radio>
                <el-radio value="custom">自定义范围</el-radio>
              </el-radio-group>
              <el-input
                v-if="toolParams.pageRange === 'custom'"
                v-model="toolParams.customRange"
                placeholder="例如: 1-5, 8, 10-15"
                class="range-input"
              />
            </div>

            <!-- 旋转角度设置 -->
            <div v-if="selectedTool.id === 'rotate'" class="option-item">
              <label class="option-label">旋转角度</label>
              <el-radio-group v-model="toolParams.rotationAngle" class="option-control">
                <el-radio :value="90">顺时针90°</el-radio>
                <el-radio :value="180">180°</el-radio>
                <el-radio :value="270">逆时针90°</el-radio>
                <el-radio :value="-90">逆时针90° (负值)</el-radio>
              </el-radio-group>
            </div>

            <!-- 重新排序设置 -->
            <div v-if="selectedTool.id === 'reorder'" class="option-item">
              <label class="option-label">页面顺序设置</label>
              <div class="reorder-section">
                <div class="reorder-description">
                  <p>请输入新的页面顺序，用逗号分隔。例如：</p>
                  <ul>
                    <li><strong>3,1,2,4</strong> - 第3页在前，然后是第1、2、4页</li>
                    <li><strong>4,3,2,1</strong> - 倒序排列</li>
                    <li><strong>1,3,2</strong> - 仅重排前3页</li>
                  </ul>
                </div>
                <el-input
                  v-model="toolParams.pageOrder"
                  placeholder="例如: 3,1,2,4 或 4,3,2,1"
                  class="page-order-input"
                >
                  <template #prepend>页面顺序</template>
                </el-input>
                <div class="reorder-help">
                  <el-alert
                    title="提示"
                    type="info"
                    :closable="false"
                    show-icon
                  >
                    <p>输入的页码数量必须等于PDF的总页数。例如5页的PDF需要输入5个页码。</p>
                    <p>页码从1开始计数，每个页码只能出现一次。</p>
                  </el-alert>
                </div>
                
                <div class="reorder-examples">
                  <label class="examples-label">常用排序模式：</label>
                  <div class="examples-buttons">
                    <el-button size="small" @click="setPageOrder('reverse')" type="primary" plain>
                      倒序排列
                    </el-button>
                    <el-button size="small" @click="setPageOrder('odd-even')" type="success" plain>
                      奇偶分离
                    </el-button>
                    <el-button size="small" @click="setPageOrder('even-odd')" type="warning" plain>
                      偶奇分离
                    </el-button>
                  </div>
                  <div class="examples-note">
                    <span>注意：这些模式需要知道PDF的总页数，建议上传文件后使用</span>
                  </div>
                </div>
              </div>
            </div>

            <!-- 质量设置 -->
            <div v-if="selectedTool.options.includes('quality')" class="option-item">
              <label class="option-label">输出质量</label>
              <el-select v-model="toolParams.quality" class="option-control">
                <el-option label="高质量(质量高文件可能变大)" value="high" />
                <el-option label="中等质量" value="medium" />
                <el-option label="低质量(文件更小)" value="low" />
              </el-select>
            </div>

            <!-- 密码设置 -->
            <div v-if="selectedTool.options.includes('password')" class="option-item">
              <label class="option-label">设置密码</label>
              <el-input
                v-model="toolParams.password"
                type="password"
                placeholder="请输入密码"
                class="option-control"
                show-password
              />
            </div>

            <!-- 格式选择 -->
            <div v-if="selectedTool.options.includes('format')" class="option-item">
              <label class="option-label">输出格式</label>
              <el-select v-model="toolParams.format" class="option-control">
                <el-option 
                  v-for="format in getFormatOptions()"
                  :key="format.value"
                  :label="format.label"
                  :value="format.value"
                />
              </el-select>
            </div>

            <!-- 水印设置 -->
            <div v-if="selectedTool.options.includes('watermark')" class="option-item">
              <label class="option-label">水印类型</label>
              <el-radio-group v-model="toolParams.watermarkType" class="option-control">
                <el-radio value="text">文字水印</el-radio>
                <el-radio value="image">图片水印</el-radio>
              </el-radio-group>
              
              <!-- 文字水印设置 -->
              <div v-if="toolParams.watermarkType === 'text'" class="sub-option">
                <label class="sub-option-label">水印文字</label>
                <el-input
                  v-model="toolParams.watermarkText"
                  placeholder="请输入水印文字"
                  class="watermark-input"
                />
                
                <div class="watermark-props">
                  <div class="prop-item">
                    <label class="prop-label">字体大小</label>
                    <el-input-number 
                      v-model="toolParams.watermarkSize" 
                      :min="10" 
                      :max="72"
                      class="size-input"
                    />
                  </div>
                  
                  <div class="prop-item">
                    <label class="prop-label">文字颜色</label>
                    <el-color-picker 
                      v-model="toolParams.watermarkColor"
                      show-alpha
                      size="small"
                    />
                  </div>
                </div>
              </div>
              
              <!-- 图片水印设置 -->
              <div v-if="toolParams.watermarkType === 'image'" class="sub-option">
                <label class="sub-option-label">选择水印图片</label>
                <el-upload
                  :show-file-list="false"
                  :before-upload="handleWatermarkImageUpload"
                  accept="image/*"
                  class="watermark-upload"
                >
                  <el-button type="primary" size="small">
                    {{ toolParams.watermarkImageFile ? '更换图片' : '选择图片' }}
                  </el-button>
                </el-upload>
                <div v-if="toolParams.watermarkImageFile" class="image-preview">
                  已选择: {{ toolParams.watermarkImageFile.name }}
                </div>
              </div>
              
              <!-- 通用水印设置 -->
              <div class="watermark-common">
                <div class="prop-item">
                  <label class="prop-label">水印位置</label>
                  <el-select v-model="toolParams.watermarkPosition" size="small">
                    <el-option label="居中" value="center" />
                    <el-option label="左上角" value="top-left" />
                    <el-option label="右上角" value="top-right" />
                    <el-option label="左下角" value="bottom-left" />
                    <el-option label="右下角" value="bottom-right" />
                  </el-select>
                </div>
                
                <div class="prop-item">
                  <label class="prop-label">透明度</label>
                  <el-slider 
                    v-model="toolParams.watermarkOpacity" 
                    :min="10" 
                    :max="100"
                    :step="5"
                    show-input
                    size="small"
                    style="width: 120px;"
                  />
                </div>
                
                <div class="prop-item">
                  <label class="prop-label">旋转角度</label>
                  <el-input-number 
                    v-model="toolParams.watermarkRotation" 
                    :min="-90" 
                    :max="90"
                    :step="15"
                    size="small"
                    class="rotation-input"
                  />
                </div>
              </div>
            </div>

            <!-- OCR语言设置 -->
            <div v-if="selectedTool.options.includes('ocrLanguage')" class="option-item">
              <label class="option-label">识别语言</label>
              <el-select v-model="toolParams.ocrLanguage" class="option-control">
                <el-option label="简体中文" value="chi_sim" />
                <el-option label="繁体中文" value="chi_tra" />
                <el-option label="英语" value="eng" />
                <el-option label="日语" value="jpn" />
                <el-option label="韩语" value="kor" />
                <el-option label="法语" value="fra" />
                <el-option label="德语" value="deu" />
                <el-option label="西班牙语" value="spa" />
              </el-select>
              <div class="option-help">
                <p class="help-text">选择要识别的文字语言，支持多种语言的OCR识别</p>
              </div>
            </div>

            <!-- === 安全工具特殊配置 === -->
            
            <!-- PDF加密设置 -->
            <div v-if="selectedTool.id === 'encrypt'" class="option-item">
              <label class="option-label">用户密码（打开密码）</label>
              <el-input
                v-model="toolParams.password"
                type="password"
                placeholder="请输入用于打开PDF的密码"
                class="option-control"
                show-password
              />
              <div class="option-help">
                <p class="help-text">用户需要输入此密码才能打开PDF文件</p>
              </div>
              
              <label class="option-label">权限密码（可选）</label>
              <el-input
                v-model="toolParams.ownerPassword"
                type="password"
                placeholder="请输入权限密码（留空则与用户密码相同）"
                class="option-control"
                show-password
              />
              <div class="option-help">
                <p class="help-text">用于控制PDF的编辑、打印等权限，留空则与用户密码相同</p>
              </div>
            </div>

            <!-- PDF解密设置 -->
            <div v-if="selectedTool.id === 'decrypt'" class="option-item">
              <label class="option-label">解密密码</label>
              <el-input
                v-model="toolParams.password"
                type="password"
                placeholder="请输入PDF的密码"
                class="option-control"
                show-password
              />
              <div class="option-help">
                <p class="help-text">请输入加密PDF文件的密码以进行解密</p>
              </div>
            </div>

            <!-- 内容编辑（涂黑）设置 -->
            <div v-if="selectedTool.id === 'redact'" class="option-item">
              <label class="option-label">涂黑关键词</label>
              <el-input
                v-model="toolParams.redactKeywords"
                placeholder="请输入要涂黑的关键词，多个关键词用逗号分隔"
                class="option-control"
                type="textarea"
                :rows="3"
              />
              <div class="option-help">
                <p class="help-text">输入要涂黑的敏感关键词，多个关键词用英文逗号分隔，如：身份证号,电话,姓名</p>
              </div>
            </div>

            <!-- 数字签名设置 -->
            <div v-if="selectedTool.id === 'sign'" class="option-item">
              <label class="option-label">签名者姓名</label>
              <el-input
                v-model="toolParams.signerName"
                placeholder="请输入签名者姓名"
                class="option-control"
              />
              
              <label class="option-label">签名原因</label>
              <el-input
                v-model="toolParams.signReason"
                placeholder="请输入签名原因"
                class="option-control"
              />
              
              <label class="option-label">签名位置</label>
              <el-input
                v-model="toolParams.signLocation"
                placeholder="请输入签名位置"
                class="option-control"
              />
              
              <label class="option-label">签名页面</label>
              <el-radio-group v-model="toolParams.pageRange" class="option-control">
                <el-radio value="all" class="radio-option">全部页面</el-radio>
                <el-radio value="first" class="radio-option">仅首页</el-radio>
                <el-radio value="last" class="radio-option">仅末页</el-radio>
                <el-radio value="custom" class="radio-option">自定义页面</el-radio>
              </el-radio-group>
              
              <div v-if="toolParams.pageRange === 'custom'" class="custom-range-input">
                <el-input
                  v-model="toolParams.customRange"
                  placeholder="请输入页面范围，如：1,3,5-8"
                  class="option-control"
                />
                <div class="option-help">
                  <p class="help-text">页面范围格式：1,3,5-8 (支持单页和范围)</p>
                </div>
              </div>
              
              <div class="option-help">
                <p class="help-text">数字签名用于验证文档的真实性和完整性</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showToolDialog = false" class="cancel-btn">取消</el-button>
          <el-button 
            type="primary" 
            @click="startProcessing" 
            :disabled="getTotalSelectedFiles() === 0"
            :loading="isProcessing"
            class="process-btn"
          >
            {{ isProcessing ? '处理中...' : '开始处理' }}
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 处理进度弹窗 -->
    <el-dialog
      v-model="showProgressDialog"
      title="处理进度"
      width="calc(100vw - 32px)"
      class="progress-dialog"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      :show-close="false"
      center
      :style="dialogStyle"
    >
      <div class="progress-content">
        <div class="progress-icon">
          <el-icon :size="48" color="#07c160">
            <Loading />
          </el-icon>
        </div>
        <div class="progress-text">
          <div class="progress-title">正在处理文件...</div>
          <div class="progress-subtitle">{{ currentProcessingFile }}</div>
        </div>
        <div class="progress-bar">
          <el-progress 
            :percentage="processingProgress" 
            :stroke-width="8"
            color="#07c160"
          />
        </div>
        <div class="progress-info">
          {{ processedCount }} / {{ totalFiles }} 个文件已完成
        </div>
      </div>
    </el-dialog>

    <!-- 处理结果弹窗 -->
    <el-dialog
      v-model="showResultDialog"
      title="处理完成"
      width="calc(100vw - 32px)"
      class="result-dialog"
      center
      :style="dialogStyle"
    >
      <div class="result-content">
        <div class="result-header">
          <div class="result-icon">✅</div>
          <div class="result-text">
            <div class="result-title">处理完成</div>
            <div class="result-subtitle">共处理 {{ processedFiles.length }} 个文件</div>
          </div>
        </div>
        
        <div class="result-files">
          <div 
            v-for="(file, index) in processedFiles"
            :key="index"
            class="result-file"
            :class="{ 'ocr-result': file.isTextResult }"
          >
            <div class="file-icon">{{ file.isTextResult ? '👁️' : '📄' }}</div>
            <div class="file-info">
              <div class="file-name">{{ file.name }}</div>
              <div class="file-size">{{ formatFileSize(file.size) }}</div>
              <!-- OCR结果的额外信息 -->
              <div v-if="file.isTextResult" class="ocr-info">
                <div class="ocr-stats">
                  <span>识别语言: {{ getLanguageName(file.language) }}</span>
                  <span>文字长度: {{ file.textLength }} 字符</span>
                </div>
                <!-- OCR文本预览 -->
                <div class="ocr-preview">
                  <div class="preview-label">识别内容预览:</div>
                  <div class="preview-text">{{ file.textContent.substring(0, 200) }}{{ file.textContent.length > 200 ? '...' : '' }}</div>
                </div>
              </div>
            </div>
            <el-button 
              type="primary" 
              size="small" 
              @click="downloadFile(file)"
              class="download-btn"
            >
              下载
            </el-button>
          </div>
        </div>
      </div>

      <template #footer>
        <div class="result-footer">
          <el-button @click="showResultDialog = false" class="close-btn">关闭</el-button>
          <el-button type="primary" @click="downloadAllFiles" class="download-all-btn">
            下载全部
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, nextTick, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElLoading } from 'element-plus'
import { ArrowRight, Close, Loading, Check } from '@element-plus/icons-vue'
import { pdfApi, fileApi, utils } from '@/api'

// 响应式数据
const showToolDialog = ref(false)
const showProgressDialog = ref(false)
const showResultDialog = ref(false)
const selectedTool = ref<any>(null)
const selectedFiles = ref<File[]>([])
const isProcessing = ref(false)
const processingProgress = ref(0)
const processedCount = ref(0)
const totalFiles = ref(0)
const currentProcessingFile = ref('')
const processedFiles = ref<any[]>([])
const fileInput = ref<HTMLInputElement>()
const fileSource = ref<'upload' | 'existing'>('upload')
const existingFiles = ref<any[]>([])
const selectedExistingFileIds = ref<string[]>([])
const loadingFiles = ref(false)

  // 工具参数
const toolParams = ref({
  pageRange: 'all',
  customRange: '',
  quality: 'medium',
  password: '',
  ownerPassword: '',
  format: 'pdf',
  splitType: 'pages',
  pageCount: 1,
  pageRanges: '',
  // 水印相关参数
  watermarkType: 'text',
  watermarkText: '',
  watermarkImageFile: null as File | null,
  watermarkPosition: 'center',
  watermarkOpacity: 50,
  watermarkSize: 24,
  watermarkColor: '#666666',
  watermarkRotation: 0,
  // 页面操作相关参数
  rotationAngle: 90,
  pageOrder: '', // 重新排序的页面顺序，如 "3,1,2,4"
  // OCR相关参数
  ocrLanguage: 'chi_sim',
  // 安全工具相关参数
  redactKeywords: '',
  signerName: '',
  signReason: '数字签名',
  signLocation: '中国'
})

// 计算属性
const dialogStyle = computed(() => {
  if (window.innerWidth <= 414) {
    return {
      '--el-dialog-width': 'calc(100vw - 8px)',
      '--el-dialog-margin-top': '2vh',
      '--el-dialog-max-width': 'calc(100vw - 8px)',
      'width': 'calc(100vw - 8px) !important',
      'max-width': 'calc(100vw - 8px) !important',
      'min-width': 'auto !important',
      'margin': '2vh auto !important'
    }
  } else if (window.innerWidth <= 768) {
    return {
      '--el-dialog-width': 'calc(100vw - 16px)',
      '--el-dialog-margin-top': '5vh',
      '--el-dialog-max-width': 'calc(100vw - 16px)',
      'width': 'calc(100vw - 16px) !important',
      'max-width': 'calc(100vw - 16px) !important',
      'min-width': 'auto !important',
      'margin': '5vh auto !important'
    }
  } else {
    return {}
  }
})

// 格式选项
const formatOptions = [
  { label: 'Word文档 (.docx)', value: 'docx' },
  { label: 'Excel表格 (.xlsx)', value: 'xlsx' },
  { label: 'PowerPoint (.pptx)', value: 'pptx' },
  { label: '纯文本 (.txt)', value: 'txt' },
  { label: '图片 (.jpg)', value: 'jpg' },
  { label: '图片 (.png)', value: 'png' }
]

// 快速工具
const quickTools = [
  { 
    id: 'pdf-to-word', 
    name: '转换', 
    emoji: '🔄',
    description: '将PDF转换为其他格式',
    fullDescription: '将PDF文件转换为Word、Excel、图片等多种格式。',
    options: ['pageRange', 'format']
  },
  { 
    id: 'merge', 
    name: '合并', 
    emoji: '📄',
    description: '将多个PDF文件合并为一个文件',
    fullDescription: '将多个PDF文件按顺序合并为一个完整的文件，保持原有格式和内容。',
    options: ['pageRange']
  },
  { 
    id: 'compress', 
    name: '压缩', 
    emoji: '🗜️',
    description: '减小PDF文件大小',
    fullDescription: '通过优化图像质量和去除冗余数据来减小PDF文件大小，保持可读性。',
    options: ['quality']
  },
  { 
    id: 'split', 
    name: '拆分', 
    emoji: '✂️',
    description: '将PDF文件拆分为多个独立文件',
    fullDescription: '按页面范围将PDF文件拆分为多个独立的文件，每个文件包含指定的页面。',
    options: ['pageRange']
  }
]

// 常用工具
const popularTools = [
  {
    id: 'merge',
    name: 'PDF合并',
    emoji: '📄',
    description: '将多个PDF文件合并为一个文件',
    fullDescription: '将多个PDF文件按顺序合并为一个完整的文件，保持原有格式和内容。支持自定义页面范围选择。',
    options: ['pageRange']
  },
  {
    id: 'split',
    name: 'PDF拆分',
    emoji: '✂️',
    description: '将PDF文件拆分为多个独立文件',
    fullDescription: '按页面范围将PDF文件拆分为多个独立的文件，每个文件包含指定的页面。支持批量处理。',
    options: ['pageRange']
  },
  {
    id: 'compress',
    name: 'PDF压缩',
    emoji: '🗜️',
    description: '减小PDF文件大小，节省存储空间',
    fullDescription: '通过优化图像质量和去除冗余数据来减小PDF文件大小，在保持可读性的同时大幅减少文件体积。',
    options: ['quality']
  },
  {
    id: 'watermark',
    name: '添加水印',
    emoji: '🏷️',
    description: '为PDF文件添加文字或图片水印',
    fullDescription: '为PDF文件添加文字或图片水印，支持自定义水印位置、透明度和样式。',
    options: ['pageRange', 'watermark']
  },
  {
    id: 'ocr',
    name: 'OCR识别',
    emoji: '👁️',
    description: '从PDF或图片中识别提取文字',
    fullDescription: '使用先进的OCR技术从PDF文档或图片文件中识别并提取文字内容，支持多种语言识别。',
    options: ['ocrLanguage']
  }
]

// 页面操作工具
const pageTools = [
  {
    id: 'rotate',
    name: '页面旋转',
    emoji: '🔄',
    description: '旋转PDF页面方向',
    fullDescription: '将PDF页面按90度的倍数进行旋转，支持选择特定页面或全部页面。',
    options: ['pageRange']
  },
  {
    id: 'delete',
    name: '删除页面',
    emoji: '🗑️',
    description: '删除PDF中的指定页面',
    fullDescription: '从PDF文件中删除不需要的页面，支持批量删除和范围选择。',
    options: ['pageRange']
  },
  {
    id: 'extract',
    name: '提取页面',
    emoji: '📋',
    description: '提取PDF中的特定页面',
    fullDescription: '从PDF文件中提取指定页面，生成新的PDF文件。支持多页面范围选择。',
    options: ['pageRange']
  },
  {
    id: 'reorder',
    name: '重新排序',
    emoji: '🔀',
    description: '调整PDF页面顺序',
    fullDescription: '通过输入页码来重新排列PDF页面的顺序。支持多种排序模式。',
    options: ['pageOrder']
  }
]

// 格式转换工具
const conversionTools = [
  {
    id: 'pdf-to-word',
    name: 'PDF转Word',
    emoji: '📝',
    description: '将PDF转换为可编辑的Word文档',
    fullDescription: '将PDF文件转换为Word文档，保持原有格式和布局，支持文字识别和表格转换。',
    options: ['pageRange', 'quality']
  },
  {
    id: 'pdf-to-excel',
    name: 'PDF转Excel',
    emoji: '📊',
    description: '将PDF表格转换为Excel表格',
    fullDescription: '专门用于转换包含表格的PDF文件，智能识别表格结构并转换为Excel格式。',
    options: ['pageRange']
  },
  {
    id: 'pdf-to-ppt',
    name: 'PDF转PPT',
    emoji: '📺',
    description: '将PDF转换为PowerPoint演示文稿 (开发中)',
    fullDescription: '将PDF文件转换为PowerPoint演示文稿，每页PDF对应一张幻灯片。该功能正在开发中，敬请期待。',
    options: ['pageRange'],
    disabled: true
  },
  {
    id: 'pdf-to-image',
    name: 'PDF转图片',
    emoji: '🖼️',
    description: '将PDF页面转换为图片文件',
    fullDescription: '将PDF页面转换为高质量的图片文件，支持JPG、PNG等格式。',
    options: ['pageRange', 'quality', 'format']
  }
]

// 安全工具
const securityTools = [
  {
    id: 'encrypt',
    name: 'PDF加密',
    emoji: '🔒',
    description: '为PDF文件设置密码保护',
    fullDescription: '为PDF文件设置打开密码和权限密码，保护文档内容不被未授权访问。',
    options: ['password']
  },
  {
    id: 'decrypt',
    name: 'PDF解密',
    emoji: '🔓',
    description: '移除PDF文件的密码保护',
    fullDescription: '移除PDF文件的密码保护和访问限制，需要提供原密码。',
    options: ['password']
  },
  {
    id: 'redact',
    name: '内容编辑',
    emoji: '✏️',
    description: '涂黑或删除敏感内容',
    fullDescription: '永久性地涂黑或删除PDF中的敏感内容，确保信息安全。',
    options: ['pageRange']
  },
  {
    id: 'sign',
    name: '数字签名',
    emoji: '✍️',
    description: '为PDF文件添加数字签名',
    fullDescription: '为PDF文件添加数字签名和时间戳，确保文档的真实性和完整性。',
    options: []
  }
]

// 强制应用移动端样式的方法
const applyMobileDialogStyles = () => {
  if (window.innerWidth <= 768) {
    setTimeout(() => {
      const dialogs = document.querySelectorAll('.el-dialog')
      dialogs.forEach(dialog => {
        const element = dialog as HTMLElement
        if (window.innerWidth <= 414) {
          element.style.setProperty('width', 'calc(100vw - 8px)', 'important')
          element.style.setProperty('max-width', 'calc(100vw - 8px)', 'important')
          element.style.setProperty('min-width', 'auto', 'important')
          element.style.setProperty('margin', '2vh auto', 'important')
        } else {
          element.style.setProperty('width', 'calc(100vw - 16px)', 'important')
          element.style.setProperty('max-width', 'calc(100vw - 16px)', 'important')
          element.style.setProperty('min-width', 'auto', 'important')
          element.style.setProperty('margin', '5vh auto', 'important')
        }
      })
      
      // 同时应用到特定类名的弹窗
      const specificDialogs = document.querySelectorAll('.tool-dialog .el-dialog, .progress-dialog .el-dialog, .result-dialog .el-dialog')
      specificDialogs.forEach(dialog => {
        const element = dialog as HTMLElement
        if (window.innerWidth <= 414) {
          element.style.setProperty('width', 'calc(100vw - 8px)', 'important')
          element.style.setProperty('max-width', 'calc(100vw - 8px)', 'important')
          element.style.setProperty('min-width', 'auto', 'important')
          element.style.setProperty('margin', '2vh auto', 'important')
        } else {
          element.style.setProperty('width', 'calc(100vw - 16px)', 'important')
          element.style.setProperty('max-width', 'calc(100vw - 16px)', 'important')
          element.style.setProperty('min-width', 'auto', 'important')
          element.style.setProperty('margin', '5vh auto', 'important')
        }
      })
    }, 100)
  }
}

// 水印图片上传处理
const handleWatermarkImageUpload = (file: File) => {
  toolParams.value.watermarkImageFile = file
  return false // 阻止自动上传
}

// 事件处理
const handleToolClick = (tool: any) => {
  selectedTool.value = tool
  selectedFiles.value = []
  
  // 根据工具类型设置默认格式
  let defaultFormat = 'pdf'
  if (tool.id === 'pdf-to-image') {
    defaultFormat = 'PNG'
  } else if (tool.id === 'pdf-to-word') {
    defaultFormat = 'docx'
  } else if (tool.id === 'pdf-to-excel') {
    defaultFormat = 'xlsx'
  }
  
  toolParams.value = {
    pageRange: 'all',
    customRange: '',
    quality: 'medium',
    password: '',
    ownerPassword: '',
    format: defaultFormat,
    splitType: 'pages',
    pageCount: 1,
    pageRanges: '',
    // 水印相关参数
    watermarkType: 'text',
    watermarkText: '',
    watermarkImageFile: null as File | null,
    watermarkPosition: 'center',
    watermarkOpacity: 50,
    watermarkSize: 24,
    watermarkColor: '#666666',
    watermarkRotation: 0,
    // 页面操作相关参数
    rotationAngle: 90,
    pageOrder: '',
    // OCR相关参数
    ocrLanguage: 'chi_sim',
    // 安全工具相关参数
    redactKeywords: '',
    signerName: '',
    signReason: '数字签名',
    signLocation: '中国'
  }
  showToolDialog.value = true
  
  // 强制应用移动端样式
  nextTick(() => {
    applyMobileDialogStyles()
  })
}

const handleFileUpload = () => {
  fileInput.value?.click()
}

const handleFileChange = (event: Event) => {
  const target = event.target as HTMLInputElement
  if (target.files) {
    const newFiles = Array.from(target.files)
    selectedFiles.value.push(...newFiles)
    target.value = '' // 清空input，允许重复选择
  }
}

const removeFile = (index: number) => {
  selectedFiles.value.splice(index, 1)
}

const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i]
}

const startProcessing = async () => {
  if (selectedFiles.value.length === 0 && selectedExistingFileIds.value.length === 0) {
    ElMessage.warning('请先选择要处理的文件')
    return
  }

  showToolDialog.value = false
  showProgressDialog.value = true
  
  // 强制应用移动端样式
  nextTick(() => {
    applyMobileDialogStyles()
  })
  
  isProcessing.value = true
  
  totalFiles.value = selectedFiles.value.length + selectedExistingFileIds.value.length
  processedCount.value = 0
  processingProgress.value = 0
  processedFiles.value = []

  try {
    // 特殊处理合并功能
    if (selectedTool.value.id === 'merge') {
      // 计算总文件数（包括新上传的文件和已有文件）
      const totalFilesForMerge = selectedFiles.value.length + selectedExistingFileIds.value.length
      if (totalFilesForMerge < 2) {
        throw new Error('合并功能需要至少2个文件')
      }
      
      // 先处理新上传的文件，获取它们的fileId
      const allFileIds = [...selectedExistingFileIds.value.map(id => parseInt(id))]
      
      // 上传新文件并获取fileId
      for (let i = 0; i < selectedFiles.value.length; i++) {
        const file = selectedFiles.value[i]
        currentProcessingFile.value = `正在上传 ${file.name}...`
        try {
          const uploadResult = await fileApi.uploadFile(file)
          if (uploadResult.success) {
            allFileIds.push(uploadResult.data.fileId)
          } else {
            throw new Error(`上传文件 ${file.name} 失败: ${uploadResult.message || '未知错误'}`)
          }
        } catch (error: any) {
          throw new Error(`上传文件 ${file.name} 失败: ${error.message}`)
        }
      }
      
      // 执行合并
      currentProcessingFile.value = '正在合并PDF文件...'
      const result = await pdfApi.mergePdfsByIds(allFileIds, `merged_${Date.now()}.pdf`)
      
      if (result && result.success) {
        const processedFile = {
          name: result.data.fileName || `merged_${Date.now()}.pdf`,
          size: result.data.fileSize || 0,
          fileId: result.data.fileId,
          downloadUrl: `/api/files/${result.data.fileId}/download`
        }
        processedFiles.value.push(processedFile)
        processedCount.value = totalFiles.value
        processingProgress.value = 100
      }
    } else {
      // 处理其他功能
      // 处理上传的新文件
      for (let i = 0; i < selectedFiles.value.length; i++) {
        const file = selectedFiles.value[i]
        currentProcessingFile.value = file.name
        await processFile(file, null)
      }

      // 处理已有文件
      for (let i = 0; i < selectedExistingFileIds.value.length; i++) {
        const fileId = selectedExistingFileIds.value[i]
        const fileName = getExistingFileName(fileId)
        currentProcessingFile.value = fileName
        await processFile(null, fileId)
      }
    }

    ElMessage.success('所有文件处理完成！')
  } catch (error: any) {
    console.error('处理失败:', error)
    ElMessage.error(error.message || '处理过程中出现错误，请重试')
  } finally {
    isProcessing.value = false
    showProgressDialog.value = false
    showResultDialog.value = true
    
    // 强制应用移动端样式
    nextTick(() => {
      applyMobileDialogStyles()
    })
  }
}

// 处理单个文件的函数
const processFile = async (file: File | null, fileId: string | null) => {
  let result
  
  switch (selectedTool.value.id) {
    case 'merge':
      // 合并功能已在startProcessing中处理
      return
      
    case 'split':
      if (fileId) {
        // 根据拆分类型构建不同的参数
        let splitOptions = {}
        if (toolParams.value.splitType === 'pages') {
          splitOptions = { pageCount: toolParams.value.pageCount || 1 }
        } else if (toolParams.value.splitType === 'range') {
          // 解析页面范围字符串
          const ranges = parsePageRanges(toolParams.value.pageRanges)
          splitOptions = { ranges }
        }
        
        result = await pdfApi.splitPdfById(parseInt(fileId), toolParams.value.splitType, splitOptions)
      } else if (file) {
        // 新上传文件的拆分功能：先上传文件获取fileId，然后调用拆分API
        try {
          currentProcessingFile.value = `正在上传 ${file.name}...`
          const uploadResult = await fileApi.uploadFile(file)
          if (uploadResult.success) {
            const uploadedFileId = uploadResult.data.fileId
            currentProcessingFile.value = `正在拆分 ${file.name}...`
            
            // 根据拆分类型构建不同的参数
            let splitOptions = {}
            if (toolParams.value.splitType === 'pages') {
              splitOptions = { pageCount: toolParams.value.pageCount || 1 }
            } else if (toolParams.value.splitType === 'range') {
              // 解析页面范围字符串
              const ranges = parsePageRanges(toolParams.value.pageRanges)
              splitOptions = { ranges }
            }
            
            result = await pdfApi.splitPdfById(parseInt(uploadedFileId), toolParams.value.splitType, splitOptions)
          } else {
            throw new Error(`上传文件失败: ${uploadResult.message || '未知错误'}`)
          }
        } catch (error: any) {
          throw new Error(`处理文件失败: ${error.message}`)
        }
      }
      break
      
    case 'compress':
      if (fileId) {
        const compressionLevel = getCompressionLevelString()
        result = await pdfApi.compressPdfById(parseInt(fileId), compressionLevel)
      } else if (file) {
        // 新上传文件的压缩功能：先上传文件获取fileId，然后调用压缩API
        try {
          currentProcessingFile.value = `正在上传 ${file.name}...`
          const uploadResult = await fileApi.uploadFile(file)
          if (uploadResult.success) {
            const uploadedFileId = uploadResult.data.fileId
            currentProcessingFile.value = `正在压缩 ${file.name}...`
            const compressionLevel = getCompressionLevelString()
            result = await pdfApi.compressPdfById(parseInt(uploadedFileId), compressionLevel)
          } else {
            throw new Error(`上传文件失败: ${uploadResult.message || '未知错误'}`)
          }
        } catch (error: any) {
          throw new Error(`处理文件失败: ${error.message}`)
        }
      }
      break
      
    case 'convert':
      // 根据format参数决定转换类型
      const format = toolParams.value.format || 'docx'
      if (format === 'docx') {
        if (file) {
          result = await pdfApi.convertPdfToWord(file)
        } else if (fileId) {
          result = await pdfApi.convertPdfToWordById(fileId)
        }
      } else if (format === 'xlsx') {
        if (file) {
          result = await pdfApi.convertPdfToExcel(file)
        } else if (fileId) {
          result = await pdfApi.convertPdfToExcelById(fileId)
        }
      } else {
        throw new Error(`暂不支持转换为 ${format} 格式`)
      }
      break
      
    case 'pdf-to-word':
      if (file) {
        result = await pdfApi.convertPdfToWord(file)
      } else if (fileId) {
        result = await pdfApi.convertPdfToWordById(fileId)
      }
      break
      
    case 'pdf-to-excel':
      if (file) {
        result = await pdfApi.convertPdfToExcel(file)
      } else if (fileId) {
        result = await pdfApi.convertPdfToExcelById(fileId)
      }
      break
      
    case 'pdf-to-ppt':
      // PowerPoint转换功能（暂未实现后端API）
      ElMessage.info('PDF转PPT功能正在开发中，敬请期待！')
      return
      
    case 'pdf-to-image':
      const imageFormat = toolParams.value.format || 'PNG'
      const dpi = toolParams.value.quality === 'high' ? 300 : toolParams.value.quality === 'low' ? 150 : 200
      
      if (fileId) {
        result = await pdfApi.pdfToImagesById(parseInt(fileId), imageFormat, dpi, toolParams.value.pageRange, toolParams.value.customRange)
      } else if (file) {
        // 新上传文件的图片转换功能：先上传文件获取fileId，然后调用转换API
        try {
          currentProcessingFile.value = `正在上传 ${file.name}...`
          const uploadResult = await fileApi.uploadFile(file)
          if (uploadResult.success) {
            const uploadedFileId = uploadResult.data.fileId
            currentProcessingFile.value = `正在转换 ${file.name}为${imageFormat}格式...`
            result = await pdfApi.pdfToImagesById(parseInt(uploadedFileId), imageFormat, dpi, toolParams.value.pageRange, toolParams.value.customRange)
          } else {
            throw new Error(`上传文件失败: ${uploadResult.message || '未知错误'}`)
          }
        } catch (error: any) {
          throw new Error(`处理文件失败: ${error.message}`)
        }
      }
      break
      
    // === 页面操作功能 ===
    case 'rotate':
      if (fileId) {
        const rotateOptions = {
          pageRange: toolParams.value.pageRange,
          customRange: toolParams.value.customRange,
          rotation: toolParams.value.rotationAngle || 90
        }
        result = await pdfApi.rotatePagesById(parseInt(fileId), rotateOptions)
      } else if (file) {
        // 新上传文件的旋转功能：先上传文件获取fileId，然后调用旋转API
        try {
          currentProcessingFile.value = `正在上传 ${file.name}...`
          const uploadResult = await fileApi.uploadFile(file)
          if (uploadResult.success) {
            const uploadedFileId = uploadResult.data.fileId
            currentProcessingFile.value = `正在旋转 ${file.name}...`
            const rotateOptions = {
              pageRange: toolParams.value.pageRange,
              customRange: toolParams.value.customRange,
              rotation: toolParams.value.rotationAngle || 90
            }
            result = await pdfApi.rotatePagesById(parseInt(uploadedFileId), rotateOptions)
          } else {
            throw new Error(`上传文件失败: ${uploadResult.message || '未知错误'}`)
          }
        } catch (error: any) {
          throw new Error(`处理文件失败: ${error.message}`)
        }
      }
      break
      
    case 'delete':
      if (fileId) {
        if (toolParams.value.pageRange === 'all') {
          throw new Error('删除页面不能选择"全部页面"，请指定要删除的页面范围')
        }
        const deleteOptions = {
          pageRange: toolParams.value.pageRange,
          customRange: toolParams.value.customRange
        }
        result = await pdfApi.deletePagesById(parseInt(fileId), deleteOptions)
      } else if (file) {
        // 新上传文件的删除页面功能
        try {
          currentProcessingFile.value = `正在上传 ${file.name}...`
          const uploadResult = await fileApi.uploadFile(file)
          if (uploadResult.success) {
            const uploadedFileId = uploadResult.data.fileId
            currentProcessingFile.value = `正在删除页面 ${file.name}...`
            if (toolParams.value.pageRange === 'all') {
              throw new Error('删除页面不能选择"全部页面"，请指定要删除的页面范围')
            }
            const deleteOptions = {
              pageRange: toolParams.value.pageRange,
              customRange: toolParams.value.customRange
            }
            result = await pdfApi.deletePagesById(parseInt(uploadedFileId), deleteOptions)
          } else {
            throw new Error(`上传文件失败: ${uploadResult.message || '未知错误'}`)
          }
        } catch (error: any) {
          throw new Error(`处理文件失败: ${error.message}`)
        }
      }
      break
      
    case 'extract':
      if (fileId) {
        const extractOptions = {
          pageRange: toolParams.value.pageRange,
          customRange: toolParams.value.customRange
        }
        result = await pdfApi.extractPagesById(parseInt(fileId), extractOptions)
      } else if (file) {
        // 新上传文件的提取页面功能
        try {
          currentProcessingFile.value = `正在上传 ${file.name}...`
          const uploadResult = await fileApi.uploadFile(file)
          if (uploadResult.success) {
            const uploadedFileId = uploadResult.data.fileId
            currentProcessingFile.value = `正在提取页面 ${file.name}...`
            const extractOptions = {
              pageRange: toolParams.value.pageRange,
              customRange: toolParams.value.customRange
            }
            result = await pdfApi.extractPagesById(parseInt(uploadedFileId), extractOptions)
          } else {
            throw new Error(`上传文件失败: ${uploadResult.message || '未知错误'}`)
          }
        } catch (error: any) {
          throw new Error(`处理文件失败: ${error.message}`)
        }
      }
      break
      
    case 'reorder':
      // 验证页面顺序输入
      if (!toolParams.value.pageOrder || toolParams.value.pageOrder.trim() === '') {
        throw new Error('请输入页面顺序，例如: 3,1,2,4')
      }
      
      // 解析页面顺序
      const pageOrderArray = toolParams.value.pageOrder.split(',').map(num => {
        const pageNum = parseInt(num.trim())
        if (isNaN(pageNum) || pageNum < 1) {
          throw new Error(`无效的页面号: ${num}`)
        }
        return pageNum
      })
      
      if (fileId) {
        const reorderOptions = {
          pageOrder: pageOrderArray
        }
        result = await pdfApi.reorderPagesById(parseInt(fileId), reorderOptions)
      } else if (file) {
        // 新上传文件的重排序功能
        try {
          currentProcessingFile.value = `正在上传 ${file.name}...`
          const uploadResult = await fileApi.uploadFile(file)
          if (uploadResult.success) {
            const uploadedFileId = uploadResult.data.fileId
            currentProcessingFile.value = `正在重排序页面 ${file.name}...`
            const reorderOptions = {
              pageOrder: pageOrderArray
            }
            result = await pdfApi.reorderPagesById(parseInt(uploadedFileId), reorderOptions)
          } else {
            throw new Error(`上传文件失败: ${uploadResult.message || '未知错误'}`)
          }
        } catch (error: any) {
          throw new Error(`处理文件失败: ${error.message}`)
        }
      }
      break
      
    case 'watermark':
      if (fileId) {
        // 构建水印选项
        const watermarkOptions = {
          watermarkType: toolParams.value.watermarkType,
          watermarkPosition: toolParams.value.watermarkPosition,
          watermarkOpacity: toolParams.value.watermarkOpacity,
          watermarkRotation: toolParams.value.watermarkRotation,
          pageRange: toolParams.value.pageRange,
          customRange: toolParams.value.customRange
        }
        
        if (toolParams.value.watermarkType === 'text') {
          // 文字水印
          if (!toolParams.value.watermarkText || toolParams.value.watermarkText.trim() === '') {
            throw new Error('文字水印内容不能为空')
          }
          Object.assign(watermarkOptions, {
            watermarkText: toolParams.value.watermarkText,
            watermarkSize: toolParams.value.watermarkSize,
            watermarkColor: toolParams.value.watermarkColor
          })
          result = await pdfApi.addWatermarkById(parseInt(fileId), watermarkOptions)
        } else if (toolParams.value.watermarkType === 'image') {
          // 图片水印
          if (!toolParams.value.watermarkImageFile) {
            throw new Error('请选择水印图片')
          }
          result = await pdfApi.addWatermarkWithImageById(parseInt(fileId), toolParams.value.watermarkImageFile, watermarkOptions)
        }
      } else if (file) {
        // 新上传文件的水印功能：先上传文件获取fileId，然后调用水印API
        try {
          currentProcessingFile.value = `正在上传 ${file.name}...`
          const uploadResult = await fileApi.uploadFile(file)
          if (uploadResult.success) {
            const uploadedFileId = uploadResult.data.fileId
            currentProcessingFile.value = `正在添加水印 ${file.name}...`
            
            // 构建水印选项
            const watermarkOptions = {
              watermarkType: toolParams.value.watermarkType,
              watermarkPosition: toolParams.value.watermarkPosition,
              watermarkOpacity: toolParams.value.watermarkOpacity,
              watermarkRotation: toolParams.value.watermarkRotation,
              pageRange: toolParams.value.pageRange,
              customRange: toolParams.value.customRange
            }
            
            if (toolParams.value.watermarkType === 'text') {
              // 文字水印
              if (!toolParams.value.watermarkText || toolParams.value.watermarkText.trim() === '') {
                throw new Error('文字水印内容不能为空')
              }
              Object.assign(watermarkOptions, {
                watermarkText: toolParams.value.watermarkText,
                watermarkSize: toolParams.value.watermarkSize,
                watermarkColor: toolParams.value.watermarkColor
              })
              result = await pdfApi.addWatermarkById(parseInt(uploadedFileId), watermarkOptions)
            } else if (toolParams.value.watermarkType === 'image') {
              // 图片水印
              if (!toolParams.value.watermarkImageFile) {
                throw new Error('请选择水印图片')
              }
              result = await pdfApi.addWatermarkWithImageById(parseInt(uploadedFileId), toolParams.value.watermarkImageFile, watermarkOptions)
            }
          } else {
            throw new Error(`上传文件失败: ${uploadResult.message || '未知错误'}`)
          }
        } catch (error: any) {
          throw new Error(`处理文件失败: ${error.message}`)
        }
      }
      break

    case 'ocr':
      if (fileId) {
        result = await pdfApi.performOcrById(parseInt(fileId), toolParams.value.ocrLanguage)
      } else if (file) {
        // 新上传文件的OCR功能：先上传文件获取fileId，然后调用OCR API
        try {
          currentProcessingFile.value = `正在上传 ${file.name}...`
          const uploadResult = await fileApi.uploadFile(file)
          if (uploadResult.success) {
            const uploadedFileId = uploadResult.data.fileId
            currentProcessingFile.value = `正在识别文字 ${file.name}...`
            result = await pdfApi.performOcrById(parseInt(uploadedFileId), toolParams.value.ocrLanguage)
          } else {
            throw new Error(`上传文件失败: ${uploadResult.message || '未知错误'}`)
          }
        } catch (error: any) {
          throw new Error(`处理文件失败: ${error.message}`)
        }
      }
      break
      
    // === 安全工具 ===
    case 'encrypt':
      // PDF加密功能
      if (!toolParams.value.password || toolParams.value.password.trim() === '') {
        throw new Error('请输入密码')
      }
      
      if (fileId) {
        result = await pdfApi.encryptPdfById(parseInt(fileId), toolParams.value.password, toolParams.value.ownerPassword)
      } else if (file) {
        // 新上传文件的加密功能
        try {
          currentProcessingFile.value = `正在上传 ${file.name}...`
          const uploadResult = await fileApi.uploadFile(file)
          if (uploadResult.success) {
            const uploadedFileId = uploadResult.data.fileId
            currentProcessingFile.value = `正在加密 ${file.name}...`
            result = await pdfApi.encryptPdfById(parseInt(uploadedFileId), toolParams.value.password, toolParams.value.ownerPassword)
          } else {
            throw new Error(`上传文件失败: ${uploadResult.message || '未知错误'}`)
          }
        } catch (error: any) {
          throw new Error(`处理文件失败: ${error.message}`)
        }
      }
      break

    case 'decrypt':
      // PDF解密功能
      if (!toolParams.value.password || toolParams.value.password.trim() === '') {
        throw new Error('请输入解密密码')
      }
      
      if (fileId) {
        result = await pdfApi.decryptPdfById(parseInt(fileId), toolParams.value.password)
      } else if (file) {
        // 新上传文件的解密功能
        try {
          currentProcessingFile.value = `正在上传 ${file.name}...`
          const uploadResult = await fileApi.uploadFile(file)
          if (uploadResult.success) {
            const uploadedFileId = uploadResult.data.fileId
            currentProcessingFile.value = `正在解密 ${file.name}...`
            result = await pdfApi.decryptPdfById(parseInt(uploadedFileId), toolParams.value.password)
          } else {
            throw new Error(`上传文件失败: ${uploadResult.message || '未知错误'}`)
          }
        } catch (error: any) {
          throw new Error(`处理文件失败: ${error.message}`)
        }
      }
      break

    case 'redact':
      // 内容编辑（涂黑）功能
      if (!toolParams.value.redactKeywords || toolParams.value.redactKeywords.trim() === '') {
        throw new Error('请输入要涂黑的关键词')
      }
      
      // 解析关键词（以逗号分隔）
      const keywords = toolParams.value.redactKeywords.split(',').map(k => k.trim()).filter(k => k)
      if (keywords.length === 0) {
        throw new Error('请输入要涂黑的关键词')
      }
      
      if (fileId) {
        result = await pdfApi.redactPdfById(parseInt(fileId), keywords, toolParams.value.pageRange, toolParams.value.customRange)
      } else if (file) {
        // 新上传文件的涂黑功能
        try {
          currentProcessingFile.value = `正在上传 ${file.name}...`
          const uploadResult = await fileApi.uploadFile(file)
          if (uploadResult.success) {
            const uploadedFileId = uploadResult.data.fileId
            currentProcessingFile.value = `正在编辑 ${file.name}...`
            result = await pdfApi.redactPdfById(parseInt(uploadedFileId), keywords, toolParams.value.pageRange, toolParams.value.customRange)
          } else {
            throw new Error(`上传文件失败: ${uploadResult.message || '未知错误'}`)
          }
        } catch (error: any) {
          throw new Error(`处理文件失败: ${error.message}`)
        }
      }
      break

    case 'sign':
      // 数字签名功能
      if (!toolParams.value.signerName || toolParams.value.signerName.trim() === '') {
        throw new Error('请输入签名者姓名')
      }
      
      if (fileId) {
        result = await pdfApi.digitalSignPdfById(
          parseInt(fileId), 
          toolParams.value.signerName, 
          toolParams.value.signReason || '数字签名',
          toolParams.value.signLocation || '中国',
          toolParams.value.pageRange || 'all',
          toolParams.value.customRange
        )
      } else if (file) {
        // 新上传文件的签名功能
        try {
          currentProcessingFile.value = `正在上传 ${file.name}...`
          const uploadResult = await fileApi.uploadFile(file)
          if (uploadResult.success) {
            const uploadedFileId = uploadResult.data.fileId
            currentProcessingFile.value = `正在签名 ${file.name}...`
            result = await pdfApi.digitalSignPdfById(
              parseInt(uploadedFileId), 
              toolParams.value.signerName,
              toolParams.value.signReason || '数字签名',
              toolParams.value.signLocation || '中国',
              toolParams.value.pageRange || 'all',
              toolParams.value.customRange
            )
          } else {
            throw new Error(`上传文件失败: ${uploadResult.message || '未知错误'}`)
          }
        } catch (error: any) {
          throw new Error(`处理文件失败: ${error.message}`)
        }
      }
      break
      
    default:
      // 对于尚未实现的功能，显示提示
      ElMessage.info(`${selectedTool.value.name} 功能即将推出，敬请期待！`)
      return
  }

  // 更新进度
  processingProgress.value = Math.round(((processedCount.value + 1) / totalFiles.value) * 100)
  
  // 处理结果
  if (result && result.success) {
    const fileName = file ? file.name : getExistingFileName(fileId!)
    
    // 特殊处理OCR结果
    if (selectedTool.value.id === 'ocr') {
      // OCR返回的是文本内容，不是文件
      const processedFile = {
        name: `${fileName.replace(/\.[^/.]+$/, '')}_OCR识别结果.txt`,
        size: new Blob([result.recognizedText || '']).size,
        isTextResult: true,
        textContent: result.recognizedText || '',
        language: result.language || toolParams.value.ocrLanguage,
        textLength: result.textLength || 0
      }
      processedFiles.value.push(processedFile)
    }
    // 处理分割、PDF转图片等返回多个文件的情况
    else if ((selectedTool.value.id === 'split' || selectedTool.value.id === 'pdf-to-image') && result.data && Array.isArray(result.data)) {
      // 分割或PDF转图片返回多个文件
      for (const fileData of result.data) {
        const processedFile = {
          name: fileData.fileName || getOutputFileName(fileName, selectedTool.value.id),
          size: fileData.fileSize || 0,
          fileId: fileData.fileId,
          downloadUrl: `/api/files/${fileData.fileId}/download`
        }
        processedFiles.value.push(processedFile)
      }
    } else {
      // 单个文件结果
      // 获取文件大小，如果result中没有，就从API获取
      let fileSize = result.fileSize || (result.data && result.data.fileSize) || 0
      let resultFileId = result.fileId || (result.data && result.data.fileId)
      
      if (fileSize === 0 && resultFileId) {
        try {
          const fileInfo = await fileApi.getById(resultFileId)
          if (fileInfo && fileInfo.success) {
            fileSize = fileInfo.data.fileSize || 0
          }
        } catch (error) {
          console.warn('获取文件大小失败:', error)
        }
      }
      
      const processedFile = {
        name: getOutputFileName(fileName, selectedTool.value.id),
        size: fileSize,
        fileId: resultFileId,
        downloadUrl: `/api/files/${resultFileId}/download`
      }
      processedFiles.value.push(processedFile)
    }
  }
  
  processedCount.value++
}

// 获取压缩级别字符串（用于API调用）
const getCompressionLevelString = () => {
  switch (toolParams.value.quality) {
    case 'high': return 'low'
    case 'medium': return 'medium'
    case 'low': return 'high'
    default: return 'medium'
  }
}

// 获取输出文件名
const getOutputFileName = (originalName: string, toolId: string) => {
  const nameWithoutExt = originalName.replace(/\.[^/.]+$/, '')
  switch (toolId) {
    case 'compress':
      return `${nameWithoutExt}_compressed.pdf`
    case 'split':
      return `${nameWithoutExt}_split.pdf`
    case 'convert':
      // 根据选择的格式返回相应扩展名
      const format = toolParams.value.format || 'docx'
      switch (format) {
        case 'docx':
          return `${nameWithoutExt}.docx`
        case 'xlsx':
          return `${nameWithoutExt}.xlsx`
        case 'pptx':
          return `${nameWithoutExt}.pptx`
        case 'txt':
          return `${nameWithoutExt}.txt`
        case 'jpg':
          return `${nameWithoutExt}.jpg`
        case 'png':
          return `${nameWithoutExt}.png`
        default:
          return `${nameWithoutExt}.${format}`
      }
    case 'pdf-to-word':
      return `${nameWithoutExt}.docx`
    case 'pdf-to-excel':
      return `${nameWithoutExt}.xlsx`
    case 'pdf-to-ppt':
      return `${nameWithoutExt}.pptx`
    case 'pdf-to-image':
      const imageExt = (toolParams.value.format || 'PNG').toLowerCase()
      return `${nameWithoutExt}.${imageExt}`
    case 'watermark':
      return `${nameWithoutExt}_watermarked.pdf`
    case 'ocr':
      return `${nameWithoutExt}_OCR识别结果.txt`
    case 'encrypt':
      return `${nameWithoutExt}_encrypted.pdf`
    case 'decrypt':
      return `${nameWithoutExt}_decrypted.pdf`
    case 'redact':
      return `${nameWithoutExt}_redacted.pdf`
    case 'sign':
      return `${nameWithoutExt}_signed.pdf`
    default:
      return `${nameWithoutExt}_processed.pdf`
  }
}

const downloadFile = async (file: any) => {
  try {
    if (file.isTextResult) {
      // 处理OCR文本结果下载
      const blob = new Blob([file.textContent || ''], { type: 'text/plain;charset=utf-8' })
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = file.name
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(url)
    } else if (file.fileId) {
      // 使用API下载文件
      try {
        // 尝试使用downloadFile获取blob
        const blob = await fileApi.downloadFile(file.fileId)
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.download = file.name
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        window.URL.revokeObjectURL(url)
      } catch {
        // 如果失败，使用download获取URL
        const downloadUrl = fileApi.download(file.fileId)
        const link = document.createElement('a')
        link.href = downloadUrl
        link.download = file.name
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
      }
    } else {
      // 兼容旧的URL下载方式
      const link = document.createElement('a')
      link.href = file.url
      link.download = file.name
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
    }
    ElMessage.success(`开始下载 ${file.name}`)
  } catch (error) {
    console.error('下载失败:', error)
    ElMessage.error('下载失败，请重试')
  }
}

const downloadAllFiles = () => {
  processedFiles.value.forEach(file => {
    setTimeout(() => downloadFile(file), 100) // 稍微延迟避免浏览器阻止多个下载
  })
  showResultDialog.value = false
}

// 加载已有文件
const loadExistingFiles = async () => {
  loadingFiles.value = true
  try {
    const response = await fileApi.getAll()
    if (response.success) {
      // 根据工具类型过滤文件
      existingFiles.value = response.data.filter((file: any) => {
        // 对于PDF工具，只显示PDF文件
        if (['merge', 'split', 'compress', 'pdf-to-word', 'pdf-to-excel', 'pdf-to-image', 'rotate', 'delete', 'extract', 'reorder', 'watermark', 'encrypt', 'decrypt', 'redact', 'sign'].includes(selectedTool.value?.id)) {
          return file.fileType === 'PDF'
        }
        // 对于OCR工具，显示PDF和图片文件
        if (selectedTool.value?.id === 'ocr') {
          return file.fileType === 'PDF' || file.fileType?.includes('image') || ['JPG', 'JPEG', 'PNG', 'TIFF', 'BMP'].includes(file.fileType?.toUpperCase())
        }
        return true
      })
    } else {
      ElMessage.error('获取文件列表失败')
    }
  } catch (error) {
    console.error('获取文件列表失败:', error)
    ElMessage.error('获取文件列表失败')
  } finally {
    loadingFiles.value = false
  }
}

// 切换已有文件选择
const toggleExistingFile = (file: any) => {
  const index = selectedExistingFileIds.value.indexOf(file.id)
  if (index === -1) {
    selectedExistingFileIds.value.push(file.id)
  } else {
    selectedExistingFileIds.value.splice(index, 1)
  }
}

// 格式化日期
const formatDate = (dateString: string) => {
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN')
}

// 获取语言名称
const getLanguageName = (languageCode: string) => {
  const languageMap: Record<string, string> = {
    'chi_sim': '简体中文',
    'chi_tra': '繁体中文',
    'eng': '英语',
    'jpn': '日语',
    'kor': '韩语',
    'fra': '法语',
    'deu': '德语',
    'spa': '西班牙语'
  }
  return languageMap[languageCode] || languageCode
}

// 获取格式选项
const getFormatOptions = () => {
  if (selectedTool.value?.id === 'pdf-to-image') {
    // PDF转图片只显示图片格式
    return [
      { label: 'PNG 图片 (推荐)', value: 'PNG' },
      { label: 'JPG 图片', value: 'JPG' },
      { label: 'JPEG 图片', value: 'JPEG' },
      { label: 'TIFF 图片', value: 'TIFF' },
      { label: 'BMP 图片', value: 'BMP' }
    ]
  }
  // 其他工具使用通用格式选项
  return formatOptions
}

// 获取上传标题
const getUploadTitle = () => {
  if (selectedTool.value?.id === 'ocr') {
    return '点击选择PDF或图片文件'
  }
  return '点击选择PDF文件'
}

// 获取上传副标题
const getUploadSubtitle = () => {
  if (selectedTool.value?.id === 'ocr') {
    return '支持PDF、JPG、PNG、TIFF等格式'
  }
  return '支持多文件选择'
}

// 获取文件接受类型
const getAcceptType = () => {
  if (selectedTool.value?.id === 'ocr') {
    return '.pdf,.jpg,.jpeg,.png,.tiff,.bmp'
  }
  return '.pdf'
}

// 获取总选择文件数
const getTotalSelectedFiles = () => {
  return selectedFiles.value.length + selectedExistingFileIds.value.length
}

// 获取已有文件名
const getExistingFileName = (fileId: string) => {
  const file = existingFiles.value.find(f => f.id === fileId)
  return file ? file.originalName : '未知文件'
}

// 获取已有文件大小
const getExistingFileSize = (fileId: string) => {
  const file = existingFiles.value.find(f => f.id === fileId)
  return file ? formatFileSize(file.fileSize) : '0 B'
}

// 移除已有文件
const removeExistingFile = (fileId: string) => {
  const index = selectedExistingFileIds.value.indexOf(fileId)
  if (index !== -1) {
    selectedExistingFileIds.value.splice(index, 1)
  }
}

// 设置页面顺序（快捷方式）
const setPageOrder = (mode: string, totalPages: number = 5) => {
  let order: number[] = []
  
  switch (mode) {
    case 'reverse':
      // 倒序排列
      for (let i = totalPages; i >= 1; i--) {
        order.push(i)
      }
      break
    case 'odd-even':
      // 奇数页在前，偶数页在后
      for (let i = 1; i <= totalPages; i += 2) {
        order.push(i)
      }
      for (let i = 2; i <= totalPages; i += 2) {
        order.push(i)
      }
      break
    case 'even-odd':
      // 偶数页在前，奇数页在后
      for (let i = 2; i <= totalPages; i += 2) {
        order.push(i)
      }
      for (let i = 1; i <= totalPages; i += 2) {
        order.push(i)
      }
      break
  }
  
  toolParams.value.pageOrder = order.join(',')
  ElMessage.success(`已设置为${mode === 'reverse' ? '倒序' : mode === 'odd-even' ? '奇偶分离' : '偶奇分离'}排列（假设${totalPages}页）`)
}

// 解析页面范围字符串
const parsePageRanges = (rangeStr: string) => {
  if (!rangeStr || !rangeStr.trim()) {
    return []
  }
  
  const ranges = []
  const parts = rangeStr.split(',')
  
  for (const part of parts) {
    const trimmed = part.trim()
    if (trimmed.includes('-')) {
      // 范围格式 "1-5"
      const [start, end] = trimmed.split('-').map(num => parseInt(num.trim()))
      if (!isNaN(start) && !isNaN(end) && start <= end) {
        ranges.push({ start, end })
      }
    } else {
      // 单页格式 "8"
      const page = parseInt(trimmed)
      if (!isNaN(page)) {
        ranges.push({ start: page, end: page })
      }
    }
  }
  
  return ranges
}

// 监听窗口大小变化
const handleResize = () => {
  applyMobileDialogStyles()
}

onMounted(() => {
  window.addEventListener('resize', handleResize)
  
  // 添加全局样式覆盖
  const style = document.createElement('style')
  style.innerHTML = `
    @media (max-width: 768px) {
      .el-dialog {
        margin: 5vh auto !important;
        width: calc(100vw - 16px) !important;
        border-radius: 12px !important;
        max-height: 95vh !important;
      }
    }
    @media (max-width: 414px) {
      .el-dialog {
        margin: 2vh auto !important;
        width: calc(100vw - 8px) !important;
        max-height: 98vh !important;
      }
    }
  `
  document.head.appendChild(style)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.tools-page {
  min-height: 100%;
  background: #f6f6f6;
  padding: 0;
}

/* 页面头部 */
.page-header {
  padding: 20px 16px 16px;
  text-align: center;
  background: #f6f6f6;
}

.page-title {
  font-size: 22px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0 0 8px 0;
}

.page-subtitle {
  font-size: 14px;
  color: #666666;
  margin: 0;
  line-height: 1.4;
}

/* 快速工具区域 */
.quick-tools-section {
  padding: 0 16px 20px;
}

.section-header {
  margin-bottom: 12px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 6px;
}

.title-icon {
  font-size: 18px;
}

.quick-tools-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}

.quick-tool-card {
  background: white;
  border-radius: 12px;
  padding: 16px 8px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid rgba(0, 0, 0, 0.05);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.quick-tool-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.quick-tool-card:active {
  transform: translateY(0);
}

.quick-tool-card .tool-icon {
  font-size: 28px;
  margin-bottom: 8px;
  display: block;
}

.quick-tool-card .tool-name {
  font-size: 12px;
  color: #1a1a1a;
  font-weight: 500;
}

/* 工具分类 */
.tools-section {
  padding: 0 16px 20px;
}

.tool-category {
  margin-bottom: 24px;
}

.category-title {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 12px 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.category-icon {
  font-size: 18px;
}

.tools-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  border: 1px solid rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.tool-item {
  display: flex;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: all 0.3s ease;
}

.tool-item:last-child {
  border-bottom: none;
}

.tool-item:hover {
  background: #f8f9fa;
}

.tool-item:active {
  background: #f0f0f0;
}

.tool-item.disabled {
  opacity: 0.5;
  cursor: not-allowed;
  pointer-events: none;
}

.tool-item.disabled:hover {
  background: transparent;
}

.tool-icon-wrapper {
  margin-right: 12px;
}

.tool-emoji {
  font-size: 24px;
  display: block;
}

.tool-info {
  flex: 1;
  min-width: 0;
}

.tool-title {
  font-size: 15px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 4px 0;
}

.tool-description {
  font-size: 12px;
  color: #666666;
  margin: 0;
  line-height: 1.4;
}

.tool-arrow {
  margin-left: 12px;
  color: #07c160;
}

/* 工具弹窗 */
.tool-dialog :deep(.el-dialog) {
  border-radius: 16px !important;
  margin: 5vh auto !important;
  max-width: 500px !important;
  width: calc(100vw - 32px) !important;
  max-height: 90vh !important;
  overflow: hidden !important;
}

.tool-dialog :deep(.el-dialog__header) {
  padding: 20px 16px 12px;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 0;
}

.tool-dialog :deep(.el-dialog__title) {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
}

.tool-dialog :deep(.el-dialog__close) {
  width: 32px;
  height: 32px;
  font-size: 18px;
}

.tool-dialog :deep(.el-dialog__body) {
  padding: 16px;
  max-height: calc(90vh - 140px);
  overflow-y: auto;
}

.tool-dialog :deep(.el-dialog__footer) {
  padding: 12px 16px 20px;
  border-top: 1px solid #f0f0f0;
}

.tool-content {
  padding: 0;
}

.tool-header {
  display: flex;
  align-items: flex-start;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 12px;
  margin-bottom: 20px;
}

.tool-large-icon {
  font-size: 40px;
  margin-right: 12px;
  color: #07c160;
  flex-shrink: 0;
}

.tool-details {
  flex: 1;
  min-width: 0;
}

.tool-name {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 6px 0;
}

.tool-desc {
  font-size: 13px;
  color: #666666;
  margin: 0;
  line-height: 1.5;
}

/* 文件选择 */
.file-selection {
  margin-bottom: 20px;
}

.selection-title {
  font-size: 15px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 12px 0;
}

.file-source-tabs {
  display: flex;
  gap: 12px;
  margin-bottom: 12px;
}

.source-tab {
  background: white;
  border: 1px solid #d9d9d9;
  border-radius: 8px;
  padding: 8px 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  font-size: 14px;
}

.source-tab:hover {
  border-color: #07c160;
}

.source-tab.active {
  background: #07c160;
  color: white;
  border-color: #07c160;
}

.tab-icon {
  margin-right: 8px;
}

/* 选择已有文件样式 */
.existing-files-section {
  margin-bottom: 16px;
}

.loading-files {
  text-align: center;
  padding: 40px 20px;
  color: #666666;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.no-files {
  text-align: center;
  padding: 40px 20px;
  color: #666666;
}

.no-files-icon {
  font-size: 48px;
  margin-bottom: 12px;
  color: #07c160;
}

.no-files-text {
  font-size: 14px;
  margin-bottom: 16px;
}

.existing-files-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 8px;
  max-height: 300px;
  overflow-y: auto;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  padding: 8px;
}

.existing-file-item {
  display: flex;
  align-items: center;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 2px solid transparent;
}

.existing-file-item:hover {
  background: #e8f5e8;
  border-color: #07c160;
}

.existing-file-item.selected {
  background: #e8f5e8;
  border-color: #07c160;
}

.existing-file-item .file-icon {
  font-size: 20px;
  margin-right: 12px;
  color: #07c160;
}

.existing-file-item .file-info {
  flex: 1;
  min-width: 0;
}

.existing-file-item .file-name {
  font-size: 14px;
  font-weight: 500;
  color: #1a1a1a;
  margin-bottom: 2px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.file-meta {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: #999999;
}

.selected-indicator {
  margin-left: 12px;
  color: #07c160;
  font-size: 16px;
}

/* 已选择文件 */
.selected-files {
  margin-top: 16px;
  background: white;
  border-radius: 12px;
  padding: 12px;
  border: 1px solid #f0f0f0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.files-title {
  font-size: 13px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 8px 0;
}

.files-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.file-item {
  display: flex;
  align-items: center;
  padding: 10px;
  background: #f8f9fa;
  border-radius: 8px;
  min-height: 52px;
  transition: all 0.3s ease;
}

.file-item:hover {
  background: #f0f0f0;
}

.file-item .file-icon {
  font-size: 16px;
  margin-right: 10px;
  color: #07c160;
}

.file-item .file-info {
  flex: 1;
  min-width: 0;
}

.file-item .file-name {
  font-size: 13px;
  font-weight: 500;
  color: #1a1a1a;
  margin-bottom: 2px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.file-item .file-size {
  font-size: 11px;
  color: #999999;
}

.file-remove {
  margin-left: 8px;
  cursor: pointer;
  color: #999999;
  transition: color 0.3s ease;
}

.file-remove:hover {
  color: #ff4757;
}

/* 工具参数 */
.tool-options {
  margin-bottom: 20px;
}

.options-title {
  font-size: 15px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 12px 0;
}

.options-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.option-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.option-label {
  font-size: 13px;
  font-weight: 500;
  color: #1a1a1a;
}

.option-control {
  width: 100%;
}

.option-control :deep(.el-radio) {
  margin-right: 12px;
  margin-bottom: 6px;
}

.option-control :deep(.el-radio__label) {
  font-size: 13px;
}

.option-control :deep(.el-select) {
  width: 100%;
}

.option-control :deep(.el-input__wrapper) {
  border-radius: 8px;
}

.range-input {
  margin-top: 6px;
}

.range-input :deep(.el-input__wrapper) {
  border-radius: 8px;
}

/* 拆分选项样式 */
.sub-option {
  margin-top: 12px;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #e9ecef;
}

.sub-option-label {
  display: block;
  font-size: 12px;
  font-weight: 500;
  color: #1a1a1a;
  margin-bottom: 8px;
}

.page-count-input {
  width: 120px;
}

.page-count-input :deep(.el-input__wrapper) {
  border-radius: 6px;
}

.range-help {
  font-size: 11px;
  color: #999999;
  margin-top: 6px;
  line-height: 1.4;
}

/* 重新排序专用样式 */
.reorder-section {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 16px;
  margin-top: 8px;
}

.reorder-description {
  margin-bottom: 16px;
}

.reorder-description p {
  margin: 0 0 8px 0;
  font-size: 13px;
  color: #333;
}

.reorder-description ul {
  margin: 8px 0;
  padding-left: 20px;
}

.reorder-description li {
  margin: 4px 0;
  font-size: 12px;
  color: #666;
}

.page-order-input {
  margin-bottom: 12px;
}

.page-order-input :deep(.el-input-group__prepend) {
  background: #07c160;
  color: white;
  border-color: #07c160;
  font-size: 12px;
}

.reorder-help {
  margin-bottom: 16px;
}

.reorder-help :deep(.el-alert) {
  border-radius: 6px;
}

.reorder-help :deep(.el-alert__content) {
  font-size: 12px;
}

.reorder-help p {
  margin: 2px 0;
}

.reorder-examples {
  border-top: 1px solid #e9ecef;
  padding-top: 12px;
}

.examples-label {
  display: block;
  font-size: 12px;
  font-weight: 500;
  color: #333;
  margin-bottom: 8px;
}

.examples-buttons {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 8px;
}

.examples-buttons .el-button {
  font-size: 11px;
  padding: 4px 8px;
}

.examples-note {
  font-size: 11px;
  color: #999;
  font-style: italic;
}

/* 弹窗底部 */
.dialog-footer {
  display: flex;
  gap: 12px;
  justify-content: center;
}

.cancel-btn,
.process-btn {
  flex: 1;
  height: 44px;
  border-radius: 12px;
  font-size: 15px;
  font-weight: 500;
}

.cancel-btn {
  border: 1px solid #d9d9d9;
  color: #666666;
}

.cancel-btn:hover {
  border-color: #07c160;
  color: #07c160;
}

.process-btn {
  background: #07c160;
  border-color: #07c160;
}

.process-btn:hover {
  background: #06ad56;
  border-color: #06ad56;
}

.process-btn:disabled {
  background: #f5f5f5;
  border-color: #d9d9d9;
  color: #bfbfbf;
  cursor: not-allowed;
}

/* 进度弹窗 */
.progress-dialog :deep(.el-dialog) {
  border-radius: 16px !important;
  margin: 15vh auto !important;
  max-width: 400px !important;
  width: calc(100vw - 32px) !important;
}

.progress-dialog :deep(.el-dialog__header) {
  padding: 20px 16px 12px;
  text-align: center;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 0;
}

.progress-dialog :deep(.el-dialog__title) {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
}

.progress-dialog :deep(.el-dialog__body) {
  padding: 20px 16px;
}

.progress-content {
  text-align: center;
  padding: 0;
}

.progress-icon {
  margin-bottom: 16px;
}

.progress-title {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 6px;
}

.progress-subtitle {
  font-size: 13px;
  color: #666666;
  margin-bottom: 20px;
  word-break: break-all;
}

.progress-bar {
  margin-bottom: 12px;
}

.progress-info {
  font-size: 11px;
  color: #999999;
}

/* 结果弹窗 */
.result-dialog :deep(.el-dialog) {
  border-radius: 16px !important;
  margin: 5vh auto !important;
  max-width: 500px !important;
  width: calc(100vw - 32px) !important;
  max-height: 90vh !important;
  overflow: hidden !important;
}

.result-dialog :deep(.el-dialog__header) {
  padding: 20px 16px 12px;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 0;
}

.result-dialog :deep(.el-dialog__title) {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
}

.result-dialog :deep(.el-dialog__close) {
  width: 32px;
  height: 32px;
  font-size: 18px;
}

.result-dialog :deep(.el-dialog__body) {
  padding: 16px;
  max-height: calc(90vh - 140px);
  overflow-y: auto;
}

.result-dialog :deep(.el-dialog__footer) {
  padding: 12px 16px 20px;
  border-top: 1px solid #f0f0f0;
}

.result-content {
  padding: 0;
}

.result-header {
  display: flex;
  align-items: flex-start;
  padding: 16px;
  background: #f0f9f0;
  border-radius: 12px;
  margin-bottom: 20px;
}

.result-icon {
  font-size: 40px;
  margin-right: 12px;
  flex-shrink: 0;
}

.result-text {
  flex: 1;
  min-width: 0;
}

.result-title {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 4px;
}

.result-subtitle {
  font-size: 13px;
  color: #666666;
}

.result-files {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.result-file {
  display: flex;
  align-items: center;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 12px;
  transition: all 0.3s ease;
  min-height: 64px;
}

.result-file:hover {
  background: #f0f0f0;
}

.result-file .file-icon {
  font-size: 18px;
  margin-right: 10px;
  color: #07c160;
}

.result-file .file-info {
  flex: 1;
  min-width: 0;
}

.result-file .file-name {
  font-size: 13px;
  font-weight: 500;
  color: #1a1a1a;
  margin-bottom: 2px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.result-file .file-size {
  font-size: 11px;
  color: #999999;
}

.download-btn {
  background: #07c160;
  border-color: #07c160;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 500;
  padding: 6px 12px;
  height: 32px;
}

.download-btn:hover {
  background: #06ad56;
  border-color: #06ad56;
}

.result-footer {
  display: flex;
  gap: 12px;
  justify-content: center;
}

.close-btn,
.download-all-btn {
  flex: 1;
  height: 44px;
  border-radius: 12px;
  font-size: 15px;
  font-weight: 500;
}

.close-btn {
  border: 1px solid #d9d9d9;
  color: #666666;
}

.close-btn:hover {
  border-color: #07c160;
  color: #07c160;
}

.download-all-btn {
  background: #07c160;
  border-color: #07c160;
}

.download-all-btn:hover {
  background: #06ad56;
  border-color: #06ad56;
}

/* 移动端响应式优化 */
@media (max-width: 768px) {
  /* 工具弹窗移动端优化 */
  .tool-dialog :deep(.el-dialog) {
    margin: 2vh auto !important;
    width: calc(100vw - 16px) !important;
    border-radius: 12px !important;
    max-height: 95vh !important;
  }
  
  .tool-dialog :deep(.el-dialog__header) {
    padding: 16px 12px 8px;
  }
  
  .tool-dialog :deep(.el-dialog__title) {
    font-size: 15px;
  }
  
  .tool-dialog :deep(.el-dialog__close) {
    width: 28px;
    height: 28px;
    font-size: 16px;
  }
  
  .tool-dialog :deep(.el-dialog__body) {
    padding: 12px;
    max-height: calc(95vh - 120px);
  }
  
  .tool-dialog :deep(.el-dialog__footer) {
    padding: 8px 12px 16px;
  }
  
  .tool-header {
    padding: 12px;
    margin-bottom: 16px;
    border-radius: 8px;
  }
  
  .tool-large-icon {
    font-size: 32px;
    margin-right: 10px;
  }
  
  .tool-name {
    font-size: 14px;
    margin-bottom: 4px;
  }
  
  .tool-desc {
    font-size: 12px;
  }
  
  .file-selection {
    margin-bottom: 16px;
  }
  
  .selection-title {
    font-size: 14px;
    margin-bottom: 10px;
  }
  
  .file-upload-area {
    padding: 20px 12px;
    min-height: 80px;
    border-radius: 8px;
    cursor: pointer;
    border: 2px dashed #d9d9d9;
    background: #fafafa;
    text-align: center;
    transition: all 0.3s ease;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
  }
  
  .file-upload-area:hover {
    border-color: #07c160;
    background: #f0f9f0;
  }
  
  .upload-icon {
    font-size: 28px;
    margin-bottom: 6px;
  }
  
  .upload-title {
    font-size: 13px;
  }
  
  .upload-subtitle {
    font-size: 11px;
  }
  
  .selected-files {
    margin-top: 10px;
    padding: 10px;
    border-radius: 8px;
  }
  
  .files-title {
    font-size: 12px;
    margin-bottom: 6px;
  }
  
  .file-item {
    padding: 8px;
    min-height: 48px;
    border-radius: 6px;
  }
  
  .file-icon {
    font-size: 14px;
    margin-right: 8px;
  }
  
  .file-name {
    font-size: 12px;
    margin-bottom: 1px;
  }
  
  .file-size {
    font-size: 10px;
  }
  
  .file-remove {
    margin-left: 6px;
  }
  
  .tool-options {
    margin-bottom: 16px;
  }
  
  .options-title {
    font-size: 14px;
    margin-bottom: 10px;
  }
  
  .options-list {
    gap: 12px;
  }
  
  .option-label {
    font-size: 12px;
  }
  
  .option-control :deep(.el-radio) {
    margin-right: 10px;
    margin-bottom: 4px;
  }
  
  .option-control :deep(.el-radio__label) {
    font-size: 12px;
  }
  
  .cancel-btn,
  .process-btn {
    height: 40px;
    border-radius: 8px;
    font-size: 14px;
  }
  
  /* 进度弹窗移动端优化 */
  .progress-dialog :deep(.el-dialog) {
    margin: 8vh auto !important;
    width: calc(100vw - 16px) !important;
    border-radius: 12px !important;
  }
  
  .progress-dialog :deep(.el-dialog__header) {
    padding: 16px 12px 8px;
  }
  
  .progress-dialog :deep(.el-dialog__title) {
    font-size: 15px;
  }
  
  .progress-dialog :deep(.el-dialog__body) {
    padding: 16px 12px;
  }
  
  .progress-title {
    font-size: 15px;
    margin-bottom: 4px;
  }
  
  .progress-subtitle {
    font-size: 12px;
    margin-bottom: 16px;
  }
  
  .progress-info {
    font-size: 10px;
  }
  
  /* 结果弹窗移动端优化 */
  .result-dialog :deep(.el-dialog) {
    margin: 2vh auto !important;
    width: calc(100vw - 16px) !important;
    border-radius: 12px !important;
    max-height: 95vh !important;
  }
  
  .result-dialog :deep(.el-dialog__header) {
    padding: 16px 12px 8px;
  }
  
  .result-dialog :deep(.el-dialog__title) {
    font-size: 15px;
  }
  
  .result-dialog :deep(.el-dialog__close) {
    width: 28px;
    height: 28px;
    font-size: 16px;
  }
  
  .result-dialog :deep(.el-dialog__body) {
    padding: 12px;
    max-height: calc(95vh - 120px);
  }
  
  .result-dialog :deep(.el-dialog__footer) {
    padding: 8px 12px 16px;
  }
  
  .result-header {
    padding: 12px;
    margin-bottom: 16px;
    border-radius: 8px;
  }
  
  .result-icon {
    font-size: 32px;
    margin-right: 10px;
  }
  
  .result-title {
    font-size: 14px;
  }
  
  .result-subtitle {
    font-size: 12px;
  }
  
  .result-files {
    gap: 8px;
  }
  
  .result-file {
    padding: 10px;
    border-radius: 8px;
    min-height: 56px;
  }
  
  .result-file .file-icon {
    font-size: 16px;
    margin-right: 8px;
  }
  
  .result-file .file-name {
    font-size: 12px;
    margin-bottom: 1px;
  }
  
  .result-file .file-size {
    font-size: 10px;
  }
  
  .download-btn {
    border-radius: 6px;
    font-size: 12px;
    padding: 4px 8px;
    height: 28px;
  }
  
  .close-btn,
  .download-all-btn {
    height: 40px;
    border-radius: 8px;
    font-size: 14px;
  }
}

@media (max-width: 414px) {
  .page-header {
    padding: 16px 12px 12px;
  }
  
  .page-title {
    font-size: 20px;
  }
  
  .quick-tools-section,
  .tools-section {
    padding: 0 12px 16px;
  }
  
  .quick-tools-grid {
    gap: 8px;
  }
  
  .quick-tool-card {
    padding: 12px 6px;
  }
  
  .quick-tool-card .tool-icon {
    font-size: 24px;
  }
  
  .quick-tool-card .tool-name {
    font-size: 11px;
  }
  
  .tool-item {
    padding: 12px;
  }
  
  .tool-title {
    font-size: 14px;
  }
  
  .tool-description {
    font-size: 11px;
  }

  /* 更小屏幕的进一步优化 */
  .tool-dialog :deep(.el-dialog) {
    margin: 1vh auto !important;
    width: calc(100vw - 8px) !important;
    max-height: 98vh !important;
  }
  
  .tool-dialog :deep(.el-dialog__header) {
    padding: 12px 8px 6px;
  }
  
  .tool-dialog :deep(.el-dialog__body) {
    padding: 8px;
    max-height: calc(98vh - 100px);
  }
  
  .tool-dialog :deep(.el-dialog__footer) {
    padding: 6px 8px 12px;
  }
  
  .progress-dialog :deep(.el-dialog) {
    margin: 5vh auto !important;
    width: calc(100vw - 8px) !important;
  }
  
  .result-dialog :deep(.el-dialog) {
    margin: 1vh auto !important;
    width: calc(100vw - 8px) !important;
    max-height: 98vh !important;
  }
  
  .result-dialog :deep(.el-dialog__header) {
    padding: 12px 8px 6px;
  }
  
  .result-dialog :deep(.el-dialog__body) {
    padding: 8px;
    max-height: calc(98vh - 100px);
  }
  
  .result-dialog :deep(.el-dialog__footer) {
    padding: 6px 8px 12px;
  }
}

/* 触摸优化 */
.quick-tool-card,
.tool-item,
.file-upload-area,
.file-item,
.file-remove,
.cancel-btn,
.process-btn,
.download-btn,
.close-btn,
.download-all-btn,
.result-file {
  -webkit-tap-highlight-color: transparent;
  user-select: none;
}

/* 加载动画 */
@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.progress-icon .el-icon {
  animation: rotate 2s linear infinite;
}

/* 安全区域适配 */
@supports (padding: max(0px)) {
  .tool-dialog :deep(.el-dialog),
  .progress-dialog :deep(.el-dialog),
  .result-dialog :deep(.el-dialog) {
    margin-bottom: max(5vh, env(safe-area-inset-bottom) + 5vh);
  }
}

/* 文件上传区域样式 */
.file-upload-area {
  border: 2px dashed #d9d9d9;
  border-radius: 12px;
  padding: 40px 20px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  background: #fafafa;
  min-height: 120px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
}

.file-upload-area:hover {
  border-color: #07c160;
  background: #f0f9f0;
}

.file-upload-area:active {
  transform: scale(0.98);
}

.upload-icon {
  font-size: 48px;
  margin-bottom: 12px;
  color: #07c160;
}

.upload-text {
  color: #666666;
}

.upload-title {
  font-size: 16px;
  font-weight: 500;
  color: #1a1a1a;
  margin-bottom: 4px;
}

.upload-subtitle {
  font-size: 13px;
  color: #999999;
}

.watermark-input {
  width: 100%;
  margin-bottom: 16px;
}

.watermark-props {
  display: flex;
  gap: 16px;
}

.prop-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.prop-label {
  font-size: 14px;
  font-weight: 500;
  color: #1a1a1a;
}

.size-input {
  width: 100px;
}

.size-input :deep(.el-input__wrapper) {
  border-radius: 6px;
}

.image-preview {
  font-size: 13px;
  color: #666666;
  margin-top: 8px;
}

.watermark-upload {
  margin-top: 16px;
}

.watermark-common {
  display: flex;
  gap: 16px;
}

.watermark-common .prop-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.watermark-common .prop-label {
  font-size: 14px;
  font-weight: 500;
  color: #1a1a1a;
}

.watermark-common .option-control {
  width: 100%;
}

.watermark-common .option-control :deep(.el-select) {
  width: 100%;
}

.watermark-common .option-control :deep(.el-input__wrapper) {
  border-radius: 6px;
}

.watermark-common .option-control :deep(.el-option) {
  font-size: 13px;
}

.watermark-common .option-control :deep(.el-option.is-disabled) {
  color: #bfbfbf;
}

.watermark-common .option-control :deep(.el-option.is-disabled.is-selected) {
  background-color: #f5f5f5;
}

.watermark-common .option-control :deep(.el-option.is-disabled:hover) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:hover:not(.is-selected)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:active) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:active:not(.is-selected)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(.is-selected)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

.watermark-common .option-control :deep(.el-option.is-disabled:focus:not(:hover):not(.is-selected):active:not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-disabled):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus):not(.is-focus)) {
  background-color: #e9ecef;
}

/* OCR 结果样式 */
.result-file.ocr-result {
  border-left: 4px solid #1976d2;
  background-color: #f8f9fa;
}

.result-file.ocr-result .file-icon {
  color: #1976d2;
  font-size: 24px;
}

.ocr-info {
  margin-top: 8px;
  padding: 8px;
  background-color: #e8f4fd;
  border-radius: 4px;
  border: 1px solid #bbdefb;
}

.ocr-stats {
  display: flex;
  gap: 16px;
  margin-bottom: 8px;
  font-size: 12px;
  color: #666;
}

.ocr-preview {
  margin-top: 8px;
}

.preview-label {
  font-size: 12px;
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
}

.preview-text {
  font-size: 12px;
  color: #666;
  line-height: 1.4;
  max-height: 80px;
  overflow: hidden;
  padding: 6px;
  background-color: #fff;
  border: 1px solid #ddd;
  border-radius: 3px;
  white-space: pre-wrap;
  word-break: break-all;
}

/* OCR选项帮助文本 */
.option-help {
  margin-top: 8px;
}

.help-text {
  font-size: 12px;
  color: #666;
  margin: 0;
  line-height: 1.4;
}

/* 数字签名页面选择样式 */
.radio-option {
  margin-right: 16px;
  margin-bottom: 8px;
}

.custom-range-input {
  margin-top: 12px;
}

/* 移动端优化 */
@media (max-width: 768px) {
  .radio-option {
    margin-right: 12px;
    margin-bottom: 12px;
  }
  
  .custom-range-input {
    margin-top: 16px;
  }
}

</style> 