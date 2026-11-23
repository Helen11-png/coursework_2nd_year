function HomePage() {
  return (
    <>
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
      </section>
    </>
  );
}

export default HomePage;
