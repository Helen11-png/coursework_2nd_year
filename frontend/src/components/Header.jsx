function Header({ pageTitle }) {
  return (
    <header className="topbar">
      <span className="topbar-title">MailForge / {pageTitle}</span>

      <div className="topbar-account">
        <span className="topbar-username">admin</span>

        <span className="topbar-avatar" aria-hidden="true">
          A
        </span>
      </div>
    </header>
  );
}

export default Header;
