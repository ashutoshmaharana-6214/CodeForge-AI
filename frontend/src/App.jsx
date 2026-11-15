import { useEffect, useMemo, useState } from 'react'
import Editor from '@monaco-editor/react'

export default function App() {
  const [problems, setProblems] = useState([])
  const [selectedId, setSelectedId] = useState(1)
  const [topic, setTopic] = useState('All')
  const [code, setCode] = useState('class Solution {\n  // Implement your solution here\n}')
  const [status, setStatus] = useState('Loading problem catalog…')
  useEffect(() => {
    fetch('/api/problems').then(r => { if (!r.ok) throw Error('API unavailable'); return r.json() })
      .then(data => { setProblems(data); setStatus('Catalog loaded') })
      .catch(() => setStatus('Backend unavailable. Start Spring Boot on port 8080.'))
  }, [])
  const topics = useMemo(() => ['All', ...new Set(problems.flatMap(p => p.topics))], [problems])
  const filtered = problems.filter(p => topic === 'All' || p.topics.includes(topic))
  const selected = problems.find(p => p.id === selectedId) || filtered[0]
  return <div className="shell">
    <header><strong>◈ CodeForge AI</strong><span>ForgeMind AI • coming soon</span></header>
    <div className="layout">
      <aside><h2>DSA Practice</h2><label>Topic <select value={topic} onChange={e => setTopic(e.target.value)}>{topics.map(t => <option key={t}>{t}</option>)}</select></label>
        {filtered.map(p => <button className={p.id === selected?.id ? 'chosen' : ''} key={p.id} onClick={() => setSelectedId(p.id)}>{p.title}<small>{p.difficulty} · {p.topics.join(', ')}</small></button>)}
      </aside>
      <main>{selected ? <><h1>{selected.title}</h1><p>{selected.description}</p><pre>{selected.examples}</pre><div className="editor-header"><strong>Java 21</strong><span>Editor only — judging not implemented</span></div><Editor height="380px" theme="vs-dark" language="java" value={code} onChange={v => setCode(v || '')} options={{ minimap: { enabled: false } }}/><p className="hint">ForgeMind tool calling, execution, saved submissions and progress tracking are upcoming milestones.</p></> : <p>No problems loaded.</p>}</main>
    </div><footer>{status}</footer>
  </div>
}
