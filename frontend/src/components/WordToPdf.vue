<template>
  <div class="word-to-pdf">
    <h3>Word转PDF</h3>
    <input type="file" accept=".doc,.docx" @change="handleFileChange" />
    <button :disabled="!file || isLoading" @click="convert" class="convert-btn">开始转换</button>
    <div v-if="pdfUrl" class="download-section">
      <a :href="pdfUrl" download="converted.pdf" class="download-link">下载PDF</a>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { pdfApi, fileApi } from '@/api'

const file = ref<File | null>(null)
const pdfUrl = ref<string | null>(null)
const isLoading = ref(false)

function handleFileChange(e: Event) {
  const target = e.target as HTMLInputElement
  if (target.files && target.files[0]) {
    file.value = target.files[0]
    pdfUrl.value = null
  }
}

async function convert() {
  if (!file.value) return
  isLoading.value = true
  try {
    const result = await pdfApi.convertWordToPdf(file.value)
    if (result.url) {
      pdfUrl.value = result.url
    } else if (result.fileId) {
      const blob = await fileApi.downloadFile(result.fileId)
      pdfUrl.value = URL.createObjectURL(blob)
    }
  } catch (e) {
    alert('转换失败')
  } finally {
    isLoading.value = false
  }
}
</script>

<style scoped>
.word-to-pdf {
  padding: 24px 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.convert-btn {
  background: #07c160;
  color: #fff;
  border: none;
  border-radius: 6px;
  padding: 8px 20px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s;
}
.convert-btn:disabled {
  background: #d9d9d9;
  cursor: not-allowed;
}
.download-section {
  margin-top: 12px;
}
.download-link {
  color: #07c160;
  font-weight: 600;
  text-decoration: underline;
}
</style> 