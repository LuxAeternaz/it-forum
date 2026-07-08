import { ref, watch, onMounted, onBeforeUnmount } from 'vue'
import { saveDraft, getDraft, deleteDraft } from '../api/draft'

export function useDraft(userId, postId, form) {
  const draftSaved = ref(false)
  const draftSaving = ref(false)
  const hasRestoredDraft = ref(false)
  const currentDraftId = ref(null)

  let debounceTimer = null
  let lastSavedTitle = ''
  let lastSavedContent = ''
  let lastSavedCategoryId = null

  const STORAGE_KEY = `draft_${userId}_${postId ?? 'new'}`

  function saveToLocal(data) {
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify({ ...data, savedAt: Date.now() }))
    } catch { /* localStorage full — silently ignore */ }
  }

  function loadFromLocal() {
    try {
      const raw = localStorage.getItem(STORAGE_KEY)
      if (!raw) return null
      const data = JSON.parse(raw)
      if (Date.now() - data.savedAt > 7 * 24 * 60 * 60 * 1000) {
        localStorage.removeItem(STORAGE_KEY)
        return null
      }
      return data
    } catch { return null }
  }

  function clearLocal() {
    localStorage.removeItem(STORAGE_KEY)
  }

  function buildPayload() {
    return {
      postId: postId?.value ?? postId ?? null,
      title: form.value.title,
      content: form.value.content,
      categoryId: form.value.categoryId
    }
  }

  function hasChanged() {
    return form.value.title !== lastSavedTitle ||
           form.value.content !== lastSavedContent ||
           form.value.categoryId !== lastSavedCategoryId
  }

  async function doSave() {
    if (!hasChanged()) return
    const payload = buildPayload()
    if (!payload.title.trim() && !payload.content.trim()) return

    draftSaving.value = true
    try {
      const result = await saveDraft(payload)
      currentDraftId.value = result?.id ?? currentDraftId.value
      lastSavedTitle = form.value.title
      lastSavedContent = form.value.content
      lastSavedCategoryId = form.value.categoryId
      draftSaved.value = true
      clearLocal()
      setTimeout(() => { draftSaved.value = false }, 2000)
    } catch {
      saveToLocal(payload)
      draftSaved.value = true
      setTimeout(() => { draftSaved.value = false }, 2000)
    } finally {
      draftSaving.value = false
    }
  }

  function scheduleSave() {
    if (debounceTimer) clearTimeout(debounceTimer)
    debounceTimer = setTimeout(doSave, 3000)
  }

  const stopWatchTitle = watch(() => form.value.title, scheduleSave)
  const stopWatchContent = watch(() => form.value.content, scheduleSave)
  const stopWatchCategory = watch(() => form.value.categoryId, scheduleSave)

  async function checkAndRestore() {
    const local = loadFromLocal()
    try {
      const remote = await getDraft(postId?.value ?? postId ?? null)
      if (remote && !hasRestoredDraft.value) {
        const rTime = new Date(remote.updatedAt).getTime()
        const lTime = local?.savedAt ?? 0
        const source = rTime >= lTime ? remote : local
        if (source && (source.title || source.content)) {
          form.value.title = source.title || ''
          form.value.content = source.content || ''
          form.value.categoryId = source.categoryId ?? form.value.categoryId
          hasRestoredDraft.value = true
          return true
        }
      }
    } catch {
      // API unavailable — fall back to localStorage
    }
    if (local && !hasRestoredDraft.value && (local.title || local.content)) {
      form.value.title = local.title || ''
      form.value.content = local.content || ''
      form.value.categoryId = local.categoryId ?? form.value.categoryId
      hasRestoredDraft.value = true
      return true
    }
    return false
  }

  async function clearDraft() {
    if (debounceTimer) clearTimeout(debounceTimer)
    clearLocal()
    try { await deleteDraft(postId?.value ?? postId ?? null) } catch {}
  }

  function forceSave() {
    if (debounceTimer) clearTimeout(debounceTimer)
    return doSave()
  }

  onBeforeUnmount(() => {
    stopWatchTitle()
    stopWatchContent()
    stopWatchCategory()
    if (debounceTimer) clearTimeout(debounceTimer)
  })

  return {
    draftSaved,
    draftSaving,
    hasRestoredDraft,
    checkAndRestore,
    clearDraft,
    forceSave
  }
}
