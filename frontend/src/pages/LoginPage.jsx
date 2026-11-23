import { useNavigate, useSearchParams } from "react-router";
import "../styles/login.css";
import { useState } from "react"; // Показывать и скрывать пароль (запоминает состояние)

function LoginPage() {
  const navigate = useNavigate();

  // Параметры адреса страницы, чтоб показывать вариант с ошибкой или обычную форму
  const [searchParams] = useSearchParams();
  const showDemoError =
    import.meta.env.DEV && searchParams.get("demo") === "error";

  const [showPassword, setShowPassword] = useState(false);

  function handleSubmit(event) {
    event.preventDefault();
    navigate("/");
  }

  return (
    <div className="login-page">
      <form className="login-form" onSubmit={handleSubmit}>
        <h1>Конструктор рассылок</h1>
        <h2>Вход в систему</h2>

        <label htmlFor="login">Логин</label>
        <input
          id="login"
          type="text"
          className={showDemoError ? "login-input-error" : ""}
          aria-invalid={showDemoError}
          required
        />

        <label htmlFor="password">Пароль</label>

        <div className="password-field">
          <input
            id="password"
            type={showPassword ? "text" : "password"}
            autoComplete="current-password"
            className={showDemoError ? "login-input-error" : ""}
            aria-invalid={showDemoError}
            required
          />

          <button
            type="button"
            className="password-toggle"
            onClick={() => setShowPassword(!showPassword)}
            aria-label={showPassword ? "Скрыть пароль" : "Показать пароль"}
            aria-pressed={showPassword}
          >
            {showPassword ? "Скрыть" : "Показать"}
          </button>
        </div>

        {showDemoError && (
          <div className="login-error" role="alert">
            Неверный логин или пароль.
            <span>Пример отображения ошибки</span>
          </div>
        )}

        <button type="submit" className="login-submit">
          Войти
        </button>

        <p className="login-help">
          Нет доступа или забыли пароль?
          <br />
          Обратитесь к администратору.
        </p>

        <p className="login-demo">
          Демонстрационный вход — пароль пока не проверяется
        </p>
      </form>
    </div>
  );
}

export default LoginPage;
