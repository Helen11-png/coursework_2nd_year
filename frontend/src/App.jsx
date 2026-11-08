import "./App.css";

const menuItems = [
  "Главная",
  "Шаблоны",
  "Кампании",
  "Журнал доставки",
  "Клиенты",
];

function App() {
  return (
    <div className="mailforge-app">
      <header className="topbar">
        <span>MailForge / Главная</span>
        <span className="topbar-user">Демо-версия</span>
      </header>

      <div className="app-layout">
        <aside className="sidebar">
          <div className="sidebar-brand">
            Конструктор
            <br />
            рассылок
          </div>

          <nav aria-label="Главное меню">
            {menuItems.map((item) => (
              <div
                key={item}
                className={`menu-item ${item === "Главная" ? "active" : ""}`}
              >
                {item}
              </div>
            ))}
          </nav>
        </aside>

        <main className="main-content">
          <div className="page-heading">
            <div>
              <h1>Добро пожаловать в MailForge</h1>
              <p>Платформа для создания и управления HTML-рассылками</p>
            </div>

            <span className="demo-badge">Frontend Demo</span>
          </div>

          <section className="welcome-card">
            <h2>Начальная настройка проекта</h2>
            <p>Основа фронтенда готова к дальнейшей разработке.</p>

            <div className="tech-list">
              <div>✓ React + Vite</div>
              <div>✓ Bootstrap</div>
              <div>✓ Docker + Nginx</div>
              <div>✓ Структура frontend/src</div>
            </div>

            <div className="notice">
              Следующий этап — реализация экранов из Figma и подключение API
              бэкенда.
            </div>
          </section>

          <p className="demo-note">
            Демонстрационная страница. Разделы меню будут доступны после
            реализации маршрутов.
          </p>
        </main>
      </div>
    </div>
  );
}

export default App;
