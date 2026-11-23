import { Outlet, useLocation } from "react-router";

import Header from "./Header.jsx";
import Sidebar from "./Sidebar.jsx";

const menuItems = [
  { name: "Главная", path: "/" },
  { name: "Шаблоны", path: "/templates" },
  { name: "Кампании", path: "/campaigns" },
  { name: "Журнал доставки", path: "/delivery-log" },
  { name: "Клиенты", path: "/clients" },
];

function AppLayout() {
  const location = useLocation();

  const pageTitle =
    menuItems.find((item) => item.path === location.pathname)?.name ??
    "Страница";

  return (
    <div className="mailforge-app">
      <Header pageTitle={pageTitle} />

      <div className="app-layout">
        <Sidebar menuItems={menuItems} />

        <main className="main-content">
          <Outlet />
        </main>
      </div>
    </div>
  );
}

export default AppLayout;
