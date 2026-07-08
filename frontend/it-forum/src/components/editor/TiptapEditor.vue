<script setup>
import { ref, watch, onBeforeUnmount, defineAsyncComponent } from 'vue'
import { useEditor, EditorContent } from '@tiptap/vue-3'
import StarterKit from '@tiptap/starter-kit'
import Placeholder from '@tiptap/extension-placeholder'
import Image from '@tiptap/extension-image'
import TurndownService from 'turndown'
import MarkdownIt from 'markdown-it'

const props = defineProps({
  modelValue: { type: String, default: '' },
  placeholder: { type: String, default: '请输入内容...' }
})

const emit = defineEmits(['update:modelValue', 'polish'])

const md = new MarkdownIt({ html: true, breaks: true, linkify: true })
const turndown = new TurndownService({
  headingStyle: 'atx',
  codeBlockStyle: 'fenced',
  emDelimiter: '*'
})

function toHtml(markdown) {
  if (!markdown) return '<p></p>'
  return md.render(markdown)
}

const editor = useEditor({
  content: toHtml(props.modelValue),
  extensions: [
    StarterKit.configure({
      heading: { levels: [1, 2, 3] }
    }),
    Placeholder.configure({ placeholder: props.placeholder }),
    Image
  ],
  onUpdate: ({ editor }) => {
    const html = editor.getHTML()
    if (html === '<p></p>') {
      emit('update:modelValue', '')
    } else {
      emit('update:modelValue', turndown.turndown(html))
    }
  }
})

watch(() => props.modelValue, (val) => {
  if (editor.value) {
    const current = turndown.turndown(editor.value.getHTML())
    if (current !== val) {
      editor.value.commands.setContent(toHtml(val), false)
    }
  }
})

function getMarkdown() {
  if (!editor.value) return ''
  const html = editor.value.getHTML()
  if (html === '<p></p>') return ''
  return turndown.turndown(html)
}

function getWordCount() {
  if (!editor.value) return 0
  const text = editor.value.getText()
  return text.replace(/\s/g, '').length
}

function handlePolish() {
  const md = getMarkdown()
  if (!md.trim()) return
  emit('polish', md)
}

defineExpose({ getMarkdown, getWordCount })

onBeforeUnmount(() => {
  editor.value?.destroy()
})
</script>

<template>
  <div class="tiptap-wrapper">
    <div class="tiptap-toolbar">
      <div class="toolbar-group">
        <button class="tool-btn" title="加粗" @click="editor?.chain().focus().toggleBold().run()" :class="{ active: editor?.isActive('bold') }">
          <strong>B</strong>
        </button>
        <button class="tool-btn" title="斜体" @click="editor?.chain().focus().toggleItalic().run()" :class="{ active: editor?.isActive('italic') }">
          <em>I</em>
        </button>
        <button class="tool-btn" title="删除线" @click="editor?.chain().focus().toggleStrike().run()" :class="{ active: editor?.isActive('strike') }">
          <s>S</s>
        </button>
        <span class="toolbar-divider" />
        <button class="tool-btn" title="标题" @click="editor?.chain().focus().toggleHeading({ level: 2 }).run()" :class="{ active: editor?.isActive('heading', { level: 2 }) }">
          H
        </button>
        <button class="tool-btn" title="引用" @click="editor?.chain().focus().toggleBlockquote().run()" :class="{ active: editor?.isActive('blockquote') }">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor"><path d="M6 17h3l2-4V7H5v6h3zm8 0h3l2-4V7h-6v6h3z"/></svg>
        </button>
        <button class="tool-btn" title="代码块" @click="editor?.chain().focus().toggleCodeBlock().run()" :class="{ active: editor?.isActive('codeBlock') }">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="16 18 22 12 16 6"/><polyline points="8 6 2 12 8 18"/></svg>
        </button>
        <button class="tool-btn" title="无序列表" @click="editor?.chain().focus().toggleBulletList().run()" :class="{ active: editor?.isActive('bulletList') }">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="9" y1="6" x2="20" y2="6"/><line x1="9" y1="12" x2="20" y2="12"/><line x1="9" y1="18" x2="20" y2="18"/><circle cx="4" cy="6" r="1.5" fill="currentColor" stroke="none"/><circle cx="4" cy="12" r="1.5" fill="currentColor" stroke="none"/><circle cx="4" cy="18" r="1.5" fill="currentColor" stroke="none"/></svg>
        </button>
        <button class="tool-btn" title="有序列表" @click="editor?.chain().focus().toggleOrderedList().run()" :class="{ active: editor?.isActive('orderedList') }">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="9" y1="6" x2="20" y2="6"/><line x1="9" y1="12" x2="20" y2="12"/><line x1="9" y1="18" x2="20" y2="18"/><path d="M4 6h1v4"/><path d="M3 14h2l-1 2h2"/><path d="M4 18h1v4"/></svg>
        </button>
        <button class="tool-btn" title="分割线" @click="editor?.chain().focus().setHorizontalRule().run()">
          —
        </button>
      </div>
      <div class="toolbar-group">
        <span class="word-count">{{ getWordCount() }} 字</span>
        <button class="tool-btn polish-btn" title="AI 润色" @click="handlePolish">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 3l1.5 5.5L19 10l-5.5 1.5L12 17l-1.5-5.5L5 10l5.5-1.5z"/><path d="M12 3v14"/></svg>
          AI 润色
        </button>
      </div>
    </div>
    <EditorContent :editor="editor" class="tiptap-content" />
  </div>
</template>

<style scoped>
.tiptap-wrapper {
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  background: var(--color-bg-white);
  overflow: hidden;
  transition: border-color var(--transition-fast);
}
.tiptap-wrapper:focus-within {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(64,158,255,0.1);
}

.tiptap-toolbar {
  display: flex; align-items: center; justify-content: space-between;
  padding: 8px 12px; border-bottom: 1px solid var(--color-border-light);
  background: var(--color-bg-slate); flex-wrap: wrap; gap: 6px;
}
.toolbar-group { display: flex; align-items: center; gap: 2px; }
.toolbar-divider {
  width: 1px; height: 20px; background: var(--color-border);
  margin: 0 4px;
}
.tool-btn {
  display: flex; align-items: center; justify-content: center;
  width: 32px; height: 32px; border: none; border-radius: var(--radius-sm);
  background: transparent; color: var(--color-text-secondary);
  font-size: 13px; font-family: var(--font-family);
  cursor: pointer; transition: all var(--transition-fast);
}
.tool-btn:hover { background: var(--color-border-light); color: var(--color-text); }
.tool-btn.active { background: var(--color-primary-light); color: var(--color-primary); }

.polish-btn {
  width: auto; padding: 0 10px; gap: 5px;
  color: var(--color-primary); font-weight: 600; font-size: 12px;
}
.polish-btn:hover { background: var(--color-primary-light); }

.word-count {
  font-size: 11px; color: var(--color-text-muted);
  margin-right: 8px; white-space: nowrap;
}

/* Editor content area */
.tiptap-content {
  padding: 16px 20px;
  min-height: 280px;
  max-height: 500px;
  overflow-y: auto;
}
.tiptap-content :deep(.ProseMirror) {
  min-height: 260px; outline: none;
  font-size: 15px; line-height: 1.8; color: var(--color-text);
  font-family: var(--font-family);
}
.tiptap-content :deep(.ProseMirror p.is-editor-empty:first-child::before) {
  content: attr(data-placeholder);
  color: var(--color-text-muted); float: left; pointer-events: none; height: 0;
}
.tiptap-content :deep(.ProseMirror h1) { font-size: 1.6em; line-height: 1.3; margin: 16px 0 8px; }
.tiptap-content :deep(.ProseMirror h2) { font-size: 1.3em; line-height: 1.4; margin: 14px 0 6px; }
.tiptap-content :deep(.ProseMirror h3) { font-size: 1.1em; line-height: 1.4; margin: 12px 0 4px; }
.tiptap-content :deep(.ProseMirror p) { margin: 4px 0; }
.tiptap-content :deep(.ProseMirror ul),
.tiptap-content :deep(.ProseMirror ol) { padding-left: 24px; margin: 6px 0; }
.tiptap-content :deep(.ProseMirror li) { margin: 2px 0; }
.tiptap-content :deep(.ProseMirror blockquote) {
  border-left: 3px solid var(--color-primary); padding: 6px 16px;
  margin: 10px 0; background: var(--color-primary-light);
  border-radius: 0 var(--radius-sm) var(--radius-sm) 0;
  color: var(--color-text-secondary);
}
.tiptap-content :deep(.ProseMirror code) {
  background: var(--color-bg-slate); padding: 2px 6px;
  border-radius: 4px; font-size: 0.9em; font-family: 'SF Mono', 'Fira Code', monospace;
}
.tiptap-content :deep(.ProseMirror pre) {
  background: #1e293b; color: #e2e8f0; padding: 14px 18px;
  border-radius: var(--radius-md); overflow-x: auto; margin: 10px 0;
}
.tiptap-content :deep(.ProseMirror pre code) {
  background: none; padding: 0; color: inherit; font-size: 13px;
}
.tiptap-content :deep(.ProseMirror img) {
  max-width: 100%; border-radius: var(--radius-sm); margin: 8px 0;
}
.tiptap-content :deep(.ProseMirror hr) {
  border: none; border-top: 1px solid var(--color-border); margin: 20px 0;
}
</style>
