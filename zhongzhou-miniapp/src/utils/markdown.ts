export interface MarkdownBlock {
  type: 'h2' | 'h3' | 'p' | 'li' | 'quote' | 'hr'
  text: string
}

/** 移除行内 Markdown 标记，保留可读文本 */
function cleanInline(text: string): string {
  return text
    .replace(/\*\*(.*?)\*\*/g, '$1')
    .replace(/\*(.*?)\*/g, '$1')
    .replace(/`(.*?)`/g, '$1')
    .replace(/\[(.*?)\]\(.*?\)/g, '$1')
    .replace(/^#+\s*/, '')
    .trim()
}

/** 将报告 Markdown 正文解析为轻量展示块（不引入第三方解析库） */
export function parseMarkdown(markdown?: string): MarkdownBlock[] {
  if (!markdown) return []
  const blocks: MarkdownBlock[] = []
  markdown.split(/\r?\n/).forEach((raw) => {
    const line = raw.trim()
    if (!line) return
    if (/^\|/.test(line)) return
    if (/^(-{3,}|\*{3,})$/.test(line)) {
      blocks.push({ type: 'hr', text: '' })
      return
    }
    if (line.startsWith('### ')) {
      blocks.push({ type: 'h3', text: cleanInline(line) })
      return
    }
    if (line.startsWith('## ') || line.startsWith('# ')) {
      blocks.push({ type: 'h2', text: cleanInline(line) })
      return
    }
    if (line.startsWith('> ')) {
      blocks.push({ type: 'quote', text: cleanInline(line) })
      return
    }
    if (/^([-*]|\d+\.)\s+/.test(line)) {
      blocks.push({ type: 'li', text: cleanInline(line.replace(/^([-*]|\d+\.)\s+/, '')) })
      return
    }
    const text = cleanInline(line)
    if (text) blocks.push({ type: 'p', text })
  })
  return blocks
}
