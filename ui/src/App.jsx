import { useEffect, useMemo, useState } from 'react'
import './App.css'

function App() {
  const [loadingLookups, setLoadingLookups] = useState(true)
  const [lookupsError, setLookupsError] = useState('')

  const [standards, setStandards] = useState([])
  const [topics, setTopics] = useState([])

  const [standardId, setStandardId] = useState('')
  const [topicId, setTopicId] = useState('')

  const [title, setTitle] = useState('')
  const [description, setDescription] = useState('')
  const [startsAt, setStartsAt] = useState('')
  const [endsAt, setEndsAt] = useState('')
  const [inviteEmails, setInviteEmails] = useState('')

  const [result, setResult] = useState('')

  const selectedStandardId = useMemo(() => {
    if (!standardId) return null
    const n = Number(standardId)
    return Number.isFinite(n) ? n : null
  }, [standardId])

  const selectedTopicId = useMemo(() => {
    if (!topicId) return null
    const n = Number(topicId)
    return Number.isFinite(n) ? n : null
  }, [topicId])

  useEffect(() => {
    const load = async () => {
      setLoadingLookups(true)
      setLookupsError('')

      try {
        const [stdRes, topicRes] = await Promise.all([
          fetch('/api/standards'),
          fetch('/api/topics'),
        ])

        if (!stdRes.ok) throw new Error(`standards HTTP ${stdRes.status}`)
        if (!topicRes.ok) throw new Error(`topics HTTP ${topicRes.status}`)

        const stdJson = await stdRes.json()
        const topicJson = await topicRes.json()

        setStandards(stdJson)
        setTopics(topicJson)

        if (stdJson?.length) setStandardId(String(stdJson[0].id))
      } catch (e) {
        setLookupsError('Failed to load standards/topics. Are you logged in?')
      } finally {
        setLoadingLookups(false)
      }
    }

    load()
  }, [])

  useEffect(() => {
    const loadTopics = async () => {
      if (!selectedStandardId) return

      try {
        const res = await fetch(`/api/topics?standardId=${selectedStandardId}`)
        if (!res.ok) throw new Error(`topics HTTP ${res.status}`)
        const json = await res.json()
        setTopics(json)
        setTopicId(json?.length ? String(json[0].id) : '')
      } catch (e) {
        setLookupsError('Failed to load topics for standard')
      }
    }

    loadTopics()
  }, [selectedStandardId])

  const onSubmit = async (e) => {
    e.preventDefault()
    setResult('')

    if (!selectedTopicId) {
      setResult('Please select a topic')
      return
    }
    if (!title.trim()) {
      setResult('Please enter a title')
      return
    }
    if (!startsAt || !endsAt) {
      setResult('Please select start and end date/time')
      return
    }

    const emails = inviteEmails
      .split(/[\n,]/)
      .map((s) => s.trim())
      .filter(Boolean)

    try {
      // 1) Create challenge
      const createRes = await fetch('/api/challenges', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          title,
          description,
          topicId: selectedTopicId,
          startsAt: new Date(startsAt).toISOString().slice(0, 19),
          endsAt: new Date(endsAt).toISOString().slice(0, 19),
        }),
      })

      if (!createRes.ok) {
        const text = await createRes.text()
        setResult(`Create challenge failed: HTTP ${createRes.status} ${text}`)
        return
      }

      const challenge = await createRes.json()

      // 2) Invite users (optional)
      if (emails.length) {
        const inviteRes = await fetch(`/api/challenges/${challenge.id}/invitations`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({ emails }),
        })

        if (!inviteRes.ok) {
          const text = await inviteRes.text()
          setResult(`Challenge created (id=${challenge.id}) but invites failed: HTTP ${inviteRes.status} ${text}`)
          return
        }
      }

      setResult(`Challenge created successfully (id=${challenge.id})${emails.length ? ' and invitations sent.' : '.'}`)
    } catch (err) {
      setResult('Request failed. Check backend logs and that you are logged in.')
    }
  }

  return (
    <div style={{ maxWidth: 720, margin: '0 auto', padding: 16 }}>
      <h1>Create Challenge</h1>

      {loadingLookups ? (
        <p>Loading standards/topics…</p>
      ) : lookupsError ? (
        <p style={{ color: 'crimson' }}>{lookupsError}</p>
      ) : null}

      <form onSubmit={onSubmit} style={{ display: 'grid', gap: 12 }}>
        <label>
          Standard
          <select
            value={standardId}
            onChange={(e) => setStandardId(e.target.value)}
            style={{ display: 'block', width: '100%', padding: 8 }}
            disabled={loadingLookups}
          >
            {standards.map((s) => (
              <option key={s.id} value={s.id}>
                {s.name}
              </option>
            ))}
          </select>
        </label>

        <label>
          Topic
          <select
            value={topicId}
            onChange={(e) => setTopicId(e.target.value)}
            style={{ display: 'block', width: '100%', padding: 8 }}
            disabled={loadingLookups}
          >
            {topics.map((t) => (
              <option key={t.id} value={t.id}>
                {t.name}
              </option>
            ))}
          </select>
        </label>

        <label>
          Title
          <input
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            placeholder="e.g., Grade 4 Math Sprint"
            style={{ display: 'block', width: '100%', padding: 8 }}
          />
        </label>

        <label>
          Description
          <input
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            placeholder="optional"
            style={{ display: 'block', width: '100%', padding: 8 }}
          />
        </label>

        <label>
          Starts at
          <input
            type="datetime-local"
            value={startsAt}
            onChange={(e) => setStartsAt(e.target.value)}
            style={{ display: 'block', width: '100%', padding: 8 }}
          />
        </label>

        <label>
          Ends at
          <input
            type="datetime-local"
            value={endsAt}
            onChange={(e) => setEndsAt(e.target.value)}
            style={{ display: 'block', width: '100%', padding: 8 }}
          />
        </label>

        <label>
          Invitee emails (comma or newline separated)
          <textarea
            value={inviteEmails}
            onChange={(e) => setInviteEmails(e.target.value)}
            rows={4}
            placeholder="kid1@example.com, kid2@example.com"
            style={{ display: 'block', width: '100%', padding: 8 }}
          />
        </label>

        <button type="submit" style={{ padding: 10 }}>
          Create
        </button>
      </form>

      {result ? <p style={{ marginTop: 12 }}>{result}</p> : null}

      <hr style={{ margin: '24px 0' }} />

      <button
        onClick={async () => {
          const res = await fetch('/hellp')
          const text = await res.text()
          setResult(`hello called: ${text}`)
        }}
      >
        Test /hellp
      </button>
    </div>
  )
}

export default App

