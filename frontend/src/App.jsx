import { useEffect, useMemo, useState } from 'react'
import Editor from '@monaco-editor/react'

export default function App() {
  const [problems, setProblems] = useState([])
  const [selectedId, setSelectedId] = useState(1)
  const [topic, setTopic] = useState('All')
  const [code, setCode] = useState('public class Main {\n  public static void main(String[] args) throws Exception {\n    // Read stdin and print the answer\n  }\n}')
  const [run, setRun] = useState(null)
  const [running, setRunning] = useState(false)
  const [status, setStatus] = useState('Loading problem catalog…')
  useEffect(() => {
    fetch('/api/problems').then(r => { if (!r.ok) throw Error('API unavailable'); return r.json() })
      .then(data => { setProblems(data); setStatus('Catalog loaded') })
      .catch(() => setStatus('Backend unavailable. Start Spring Boot on port 8080.'))
  }, [])
  async function runSample() {
    if (!selected || running) return
    setRunning(true)
    setRun({ state: 'RUNNING', detail: 'Submitting to the isolated judge...' })
    try {
      const response = await fetch('/api/runs', {
        method: 'POST', headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ problemId: selected.id, sourceCode: code })
      })
      if (!response.ok) throw new Error(response.status === 503
        ? 'Judge0 is not configured on the backend.'
        : 'Unable to submit the public sample (' + response.status + ').')
      const created = await response.json()
      for (let attempt = 0; attempt < 20; attempt++) {
        await new Promise(resolve => setTimeout(resolve, 1000))
        const resultResponse = await fetch('/api/runs/' + created.runId)
        if (!resultResponse.ok) throw new Error('Failed to retrieve the judge verdict.')
        const result = await resultResponse.json()
        setRun(result)
        if (result.state !== 'RUNNING') return
      }
      setRun({ state: 'TIMEOUT', detail: 'Polling timed out; the run may still be processing.' })
    } catch (error) {
      setRun({ state: 'ERROR', detail: error.message })
    } finally {
      setRunning(false)
    }
  }
  const topics = useMemo(() => ['All', ...new Set(problems.flatMap(p => p.topics))], [problems])
  const filtered = problems.filter(p => topic === 'All' || p.topics.includes(topic))
  const selected = problems.find(p => p.id === selectedId) || filtered[0]
  return <div className="shell">
    <header><strong>◈ CodeForge AI</strong><span>ForgeMind AI • coming soon</span></header>
    <div className="layout">
      <aside><h2>DSA Practice</h2><label>Topic <select value={topic} onChange={e => setTopic(e.target.value)}>{topics.map(t => <option key={t}>{t}</option>)}</select></label>
        {filtered.map(p => <button className={p.id === selected?.id ? 'chosen' : ''} key={p.id} onClick={() => { setSelectedId(p.id); setRun(null); setCode('public class Main {\n  public static void main(String[] args) throws Exception {\n    // Read from standard input and print the answer\n  }\n}') }}>{p.title}<small>{p.difficulty} · {p.topics.join(', ')}</small></button>)}
      </aside>
      <main>{selected ? <><h1>{selected.title}</h1><p>{selected.description}</p><pre>{selected.examples}</pre><div className="editor-header"><strong>Java 21</strong><span>Public sample only — no hidden judging</span></div><Editor height="380px" theme="vs-dark" language="java" value={code} onChange={v => setCode(v || '')} options={{ minimap: { enabled: false } }}/><div className="run-actions"><button onClick={runSample} disabled={running}>{running ? 'Running...' : 'Run public sample'}</button><small>Reads stdin and prints stdout. Passing the sample does not mark a problem solved.</small></div>{run && <pre role="status">Status: {run.state}{run.detail ? '\n' + run.detail : ''}{run.stdout ? '\nstdout:\n' + run.stdout : ''}{run.stderr ? '\nstderr:\n' + run.stderr : ''}{run.compileOutput ? '\ncompiler:\n' + run.compileOutput : ''}</pre>}<p className="hint">ForgeMind AI and verified hidden-test submissions are planned.</p></> : <p>No problems loaded.</p>}</main>
    </div><footer>{status}</footer>
  </div>
}
