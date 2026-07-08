import { ref, shallowRef } from 'vue'
import { getAgents } from '../api/agent'
import { getMentionSuggestions } from '../api/mention'

const agents = shallowRef([])
let agentsFetched = false

// Global name → { id, type } cache for resolving @mentions
const nameMap = new Map()
const nameMapVersion = ref(0)

export function addToNameMap(name, id, type) {
  if (!nameMap.has(name)) {
    nameMap.set(name, { id, type })
    nameMapVersion.value++
  }
}

export function useMentions() {
  if (!agentsFetched) {
    agentsFetched = true
    getAgents().then(list => {
      agents.value = list || []
      for (const a of agents.value) {
        addToNameMap(a.name, a.id, 'agent')
      }
    }).catch(() => {})
  }

  function renderSegments(text) {
    if (!text) return [{ type: 'text', text: '' }]
    const segments = []
    const regex = /@(\S+)/g
    let last = 0
    let match
    while ((match = regex.exec(text)) !== null) {
      if (match.index > last) {
        segments.push({ type: 'text', text: text.slice(last, match.index) })
      }
      const name = match[1]
      const resolved = nameMap.get(name)
      if (resolved) {
        const link = resolved.type === 'agent'
          ? `/agents/${resolved.id}`
          : `/user/${resolved.id}`
        segments.push({ type: 'mention', text: `@${name}`, link })
      } else {
        segments.push({ type: 'text', text: match[0] })
      }
      last = regex.lastIndex
    }
    if (last < text.length) {
      segments.push({ type: 'text', text: text.slice(last) })
    }
    return segments
  }

  return { agents, renderSegments }
}

// nameMapVersion is a ref so that computed properties depending on renderMentionHtml
// will re-evaluate when new names are added to the cache
export { nameMapVersion }

export function renderMentionHtml(html) {
  if (!html) return ''
  nameMapVersion.value // touch to establish reactive dependency
  let result = html
  for (const [name, { id, type }] of nameMap) {
    const escaped = name.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
    const regex = new RegExp(`@${escaped}\\b`, 'g')
    const link = type === 'agent' ? `/agents/${id}` : `/user/${id}`
    result = result.replace(regex, `<a href="${link}" class="mention-link">@${name}</a>`)
  }
  return result
}

export async function getSuggestions(query) {
  const list = await getMentionSuggestions(query)
  if (list) {
    for (const item of list) {
      addToNameMap(item.name, item.id, item.type)
    }
  }
  return list
}
