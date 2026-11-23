import { NavLink } from "react-router";

function Sidebar({ menuItems }) {
  return (
    <aside className="sidebar">
      <div className="sidebar-brand">
        Конструктор
        <br />
        рассылок
      </div>

      <nav aria-label="Главное меню">
        {menuItems.map((item) => (
          <NavLink
            key={item.path}
            to={item.path}
            end={item.path === "/"}
            className={({ isActive }) =>
              `menu-item ${isActive ? "active" : ""}`
            }
          >
            {item.name}
          </NavLink>
        ))}
      </nav>
    </aside>
  );
}

export default Sidebar;
