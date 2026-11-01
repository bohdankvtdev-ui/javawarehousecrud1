import { useState, type FormEvent } from "react";
import { KeyRound, LogOut } from "lucide-react";
import { clearAuth, isLoggedIn, setAuth } from "../api/auth";

interface Props {
  onAuthChange: () => void;
  /** Icon-only / stacked for collapsed sidebar */
  compact?: boolean;
}

/**
 * STUDY: HTTP Basic login for write APIs.
 */
export default function LoginBar({ onAuthChange, compact = false }: Props) {
  const [username, setUsername] = useState("user");
  const [password, setPassword] = useState("user");
  const loggedIn = isLoggedIn();

  function login(event: FormEvent) {
    event.preventDefault();
    setAuth(username.trim(), password);
    onAuthChange();
  }

  function logout() {
    clearAuth();
    onAuthChange();
  }

  if (loggedIn) {
    return (
      <div className={`login-bar login-bar--ok ${compact ? "login-bar--compact" : ""}`}>
        <KeyRound size={14} aria-hidden />
        {!compact && <span>Signed in</span>}
        <button type="button" className="btn-secondary btn-tiny" onClick={logout} title="Log out">
          <LogOut size={13} aria-hidden />
          {!compact && " Out"}
        </button>
      </div>
    );
  }

  if (compact) {
    return (
      <form className="login-bar login-bar--compact" onSubmit={login} title="Login user/user">
        <button type="submit" className="btn-tiny" aria-label="Log in as user">
          <KeyRound size={14} />
        </button>
      </form>
    );
  }

  return (
    <form className="login-bar login-bar--stack" onSubmit={login}>
      <div className="login-stack-row">
        <KeyRound size={14} aria-hidden />
        <span className="login-label">API</span>
      </div>
      <input
        value={username}
        onChange={(e) => setUsername(e.target.value)}
        placeholder="user"
        aria-label="username"
      />
      <input
        type="password"
        value={password}
        onChange={(e) => setPassword(e.target.value)}
        placeholder="pass"
        aria-label="password"
      />
      <button type="submit" className="btn-tiny">
        Log in
      </button>
      <span className="hint">user / admin</span>
    </form>
  );
}
