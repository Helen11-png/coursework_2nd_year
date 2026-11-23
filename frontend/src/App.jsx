import { Routes, Route } from "react-router";

import LoginPage from "./pages/LoginPage.jsx";
import AppLayout from "./components/AppLayout.jsx";
import HomePage from "./pages/HomePage.jsx";
import TemplatesPage from "./pages/TemplatesPage.jsx";
import CampaignsPage from "./pages/CampaignsPage.jsx";
import DeliveryLogPage from "./pages/DeliveryLogPage.jsx";
import ClientsPage from "./pages/ClientsPage.jsx";

import "./App.css";

function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      // Все вложенные маршруты будут использовать общий AppLayout
      <Route element={<AppLayout />}>
        <Route path="/" element={<HomePage />} />
        <Route path="/templates" element={<TemplatesPage />} />
        <Route path="/campaigns" element={<CampaignsPage />} />
        <Route path="/delivery-log" element={<DeliveryLogPage />} />
        <Route path="/clients" element={<ClientsPage />} />
      </Route>
    </Routes>
  );
}

export default App;
