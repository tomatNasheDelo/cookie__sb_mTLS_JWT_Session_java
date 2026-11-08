import { useState } from 'react'
import type { FormEvent } from 'react'

const base = (import.meta.env.VITE_API_URL ?? '').replace(/\/$/, '')
const api = (path: string) => `${base}${path}`
type AuthResult = { message: string; username: string }
type TokenResult = { accessToken: string; tokenType: string; expiresInSeconds: number }
type MtlsResult = { message: string; clientCertificateRequired: boolean }
const credentials = { username: 'student', password: 'change-me-local' }

export default function App() {
  const [jwt, setJwt] = useState('')
  const [sessionResult, setSessionResult] = useState('')
  const [jwtResult, setJwtResult] = useState('')
  const [mtlsResult, setMtlsResult] = useState('')
  const [error, setError] = useState('')

  async function call<T>(path: string, method = 'GET', body?: unknown, bearer?: string): Promise<T> {
    const response = await fetch(api(path), {
      method,
      credentials: 'include',
      headers: { ...(body ? { 'Content-Type': 'application/json' } : {}), ...(bearer ? { Authorization: `Bearer ${bearer}` } : {}) },
      ...(body ? { body: JSON.stringify(body) } : {}),
    })
    if (!response.ok) throw new Error(`${path}: HTTP ${response.status}`)
    return await response.json() as T
  }

  async function run(action: () => Promise<void>) {
    setError('')
    try { await action() } catch (reason) { setError(reason instanceof Error ? reason.message : 'Запрос не выполнен') }
  }

  function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    void run(async () => {
      const session = await call<AuthResult>('/api/auth/session/login', 'POST', credentials)
      setSessionResult(session.message)
      const token = await call<TokenResult>('/api/auth/jwt/login', 'POST', credentials)
      setJwt(token.accessToken)
      setJwtResult(`JWT выдан на ${token.expiresInSeconds} секунд и хранится только в памяти страницы.`)
    })
  }

  return <main className="wrap">
    <header><p className="eyebrow">COOKIE · AUTH · TRANSPORT SECURITY</p><h1>Identity <span>Lab</span></h1><p>Один demo-login, три разных механизма: server session, JWT и mTLS между сервисами.</p></header>
    <section className="panel login"><div><small>ДЕМО УЧЁТНЫЕ ДАННЫЕ</small><p><code>student / change-me-local</code></p><p>Нажми один раз, чтобы создать сессию и получить JWT.</p></div><form onSubmit={onSubmit}><button type="submit">Войти в демо</button></form></section>
    <div className="grid">
      <section className="panel"><small>01 / SESSION</small><h2>Сессия на сервере</h2><p>Spring хранит user в HttpSession; браузеру выдаётся HttpOnly JSESSIONID cookie.</p><button onClick={() => void run(async () => { const result = await call<AuthResult>('/api/auth/session/me'); setSessionResult(`${result.message}: ${result.username}`) })}>Проверить сессию</button><button className="secondary" onClick={() => void run(async () => { const result = await call<AuthResult>('/api/auth/session/logout', 'POST'); setSessionResult(result.message) })}>Завершить сессию</button>{sessionResult && <p className="result">{sessionResult}</p>}</section>
      <section className="panel"><small>02 / JWT</small><h2>Токен у клиента</h2><p>Backend подписывает JWT; frontend хранит его в React state и передаёт как Bearer.</p><button disabled={!jwt} onClick={() => void run(async () => { const result = await call<AuthResult>('/api/auth/jwt/me', 'GET', undefined, jwt); setJwtResult(`${result.message}: ${result.username}`) })}>Проверить JWT</button>{jwtResult && <p className="result">{jwtResult}</p>}</section>
    </div>
    <section className="panel mtls"><div><small>03 / mTLS</small><h2>Backend → внутренний ресурс</h2><p>Оба Spring сервиса проверяют сертификат другой стороны при TLS handshake.</p><button onClick={() => void run(async () => { const result = await call<MtlsResult>('/api/mtls/ping'); setMtlsResult(`${result.message}; client cert required: ${result.clientCertificateRequired}`) })}>Вызвать mTLS endpoint</button>{mtlsResult && <p className="result">{mtlsResult}</p>}</div></section>
    {error && <p className="error" role="alert">{error}</p>}
    <section className="note"><h2>Не путай понятия</h2><p><b>Session</b> — состояние на сервере, браузер хранит идентификатор. <b>JWT</b> — подписанный токен пользователя. <b>mTLS</b> — аутентификация сервисов при TLS соединении.</p></section>
    <footer>Учебная демо-среда · Без БД · Не используй демо-секреты в production</footer>
  </main>
}
