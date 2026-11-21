import { apiRequest } from "./apiClient.js";

// Получить список шаблонов
export function getTemplates(includeArchived = false) {
  const query = includeArchived ? "?includeArchived=true" : "";

  return apiRequest(`/templates${query}`);
}

// Получить один шаблон по id
export function getTemplate(id) {
  return apiRequest(`/templates/${encodeURIComponent(id)}`);
}

// Создать новый шаблон
export function createTemplate(data) {
  return apiRequest("/templates", {
    method: "POST",
    body: data,
  });
}

// Изменить существующий шаблон
export function updateTemplate(id, data) {
  return apiRequest(`/templates/${encodeURIComponent(id)}`, {
    method: "PUT",
    body: data,
  });
}

// Архивировать шаблон
export function archiveTemplate(id) {
  return apiRequest(`/templates/${encodeURIComponent(id)}/archive`, {
    method: "POST",
  });
}
