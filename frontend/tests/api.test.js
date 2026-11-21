import test from "node:test";
import assert from "node:assert/strict";

import {
  getTemplates,
  getTemplate,
  createTemplate,
  updateTemplate,
  archiveTemplate,
} from "../src/api/templatesApi.js";

// Тестовые данные шаблона
const template = {
  id: 1,
  name: "Новогодняя рассылка",
  ownerId: 1,
  htmlBody: "<h1>С Новым годом!</h1>",
  archived: false,
  createdAt: "2026-11-21T09:00:00Z",
  updatedAt: "2026-11-21T09:00:00Z",
};

// 1. Получение списка шаблонов
test("GET: получение списка шаблонов", async (t) => {
  const fetchMock = t.mock.method(globalThis, "fetch", async () =>
    Response.json([template]),
  );

  const result = await getTemplates();

  assert.deepEqual(result, [template]);
  assert.equal(fetchMock.mock.calls[0].arguments[0], "/api/templates");
});

// 2. Получение списка вместе с архивными
test("GET: включение архивных шаблонов", async (t) => {
  const fetchMock = t.mock.method(globalThis, "fetch", async () =>
    Response.json([]),
  );

  await getTemplates(true);

  assert.equal(
    fetchMock.mock.calls[0].arguments[0],
    "/api/templates?includeArchived=true",
  );
});

// 3. Получение одного шаблона
test("GET: получение шаблона по ID", async (t) => {
  const fetchMock = t.mock.method(globalThis, "fetch", async () =>
    Response.json(template),
  );

  const result = await getTemplate(1);

  assert.deepEqual(result, template);
  assert.equal(fetchMock.mock.calls[0].arguments[0], "/api/templates/1");
});

// 4. Создание шаблона
test("POST: создание шаблона", async (t) => {
  const fetchMock = t.mock.method(globalThis, "fetch", async () =>
    Response.json(template, { status: 201 }),
  );

  const data = {
    name: template.name,
    htmlBody: template.htmlBody,
  };

  const result = await createTemplate(data);

  const [url, options] = fetchMock.mock.calls[0].arguments;

  assert.equal(url, "/api/templates");
  assert.equal(options.method, "POST");
  assert.equal(options.body, JSON.stringify(data));
  assert.equal(options.headers.get("Content-Type"), "application/json");
  assert.deepEqual(result, template);
});

// 5. Редактирование шаблона
test("PUT: изменение шаблона", async (t) => {
  const data = {
    name: "Новое название",
    htmlBody: template.htmlBody,
  };

  const updated = { ...template, ...data };

  const fetchMock = t.mock.method(globalThis, "fetch", async () =>
    Response.json(updated),
  );

  const result = await updateTemplate(1, data);

  const [url, options] = fetchMock.mock.calls[0].arguments;

  assert.equal(url, "/api/templates/1");
  assert.equal(options.method, "PUT");
  assert.equal(options.body, JSON.stringify(data));
  assert.deepEqual(result, updated);
});

// 6. Архивирование шаблона
test("POST: архивирование шаблона", async (t) => {
  const archived = { ...template, archived: true };

  const fetchMock = t.mock.method(globalThis, "fetch", async () =>
    Response.json(archived),
  );

  const result = await archiveTemplate(1);

  const [url, options] = fetchMock.mock.calls[0].arguments;

  assert.equal(url, "/api/templates/1/archive");
  assert.equal(options.method, "POST");
  assert.equal(result.archived, true);
});

// 7. Ошибка валидации полей
test("400: ошибка валидации", async (t) => {
  t.mock.method(globalThis, "fetch", async () =>
    Response.json(
      {
        code: "VALIDATION_ERROR",
        message: "Проверьте заполнение полей",
        fields: [
          {
            field: "name",
            message: "не должно быть пустым",
          },
        ],
      },
      { status: 400 },
    ),
  );

  await assert.rejects(
    () => createTemplate({ name: "", htmlBody: "<h1>Тест</h1>" }),
    {
      name: "ApiError",
      code: "VALIDATION_ERROR",
      status: 400,
      message: "Проверьте заполнение полей",
      fields: [
        {
          field: "name",
          message: "не должно быть пустым",
        },
      ],
    },
  );
});

// 8. Шаблон не найден
test("404: шаблон не найден", async (t) => {
  t.mock.method(globalThis, "fetch", async () =>
    Response.json(
      {
        code: "NOT_FOUND",
        message: "Шаблон не найден",
        fields: [],
      },
      { status: 404 },
    ),
  );

  await assert.rejects(() => getTemplate(999), {
    name: "ApiError",
    code: "NOT_FOUND",
    status: 404,
    message: "Шаблон не найден",
    fields: [],
  });
});

// 9. Попытка изменить архивный шаблон
test("409: шаблон находится в архиве", async (t) => {
  t.mock.method(globalThis, "fetch", async () =>
    Response.json(
      {
        code: "TEMPLATE_ARCHIVED",
        message: "Шаблон находится в архиве",
        fields: [],
      },
      { status: 409 },
    ),
  );

  await assert.rejects(
    () =>
      updateTemplate(1, {
        name: "Новое название",
        htmlBody: "<h1>Тест</h1>",
      }),
    {
      code: "TEMPLATE_ARCHIVED",
      status: 409,
    },
  );
});

// 10. Внутренняя ошибка сервера
test("500: внутренняя ошибка сервера", async (t) => {
  t.mock.method(globalThis, "fetch", async () =>
    Response.json(
      {
        code: "INTERNAL_ERROR",
        message: "Техническая ошибка сервера",
        fields: [],
      },
      { status: 500 },
    ),
  );

  await assert.rejects(() => getTemplates(), {
    code: "INTERNAL_ERROR",
    status: 500,
    message: "Что-то пошло не так, попробуйте позже",
  });
});

// 11. Сервер вернул HTML вместо JSON
test("Неожиданный ответ: HTML вместо JSON", async (t) => {
  t.mock.method(
    globalThis,
    "fetch",
    async () =>
      new Response("<html>Server error</html>", {
        status: 502,
        headers: { "Content-Type": "text/html" },
      }),
  );

  await assert.rejects(() => getTemplates(), {
    code: "INVALID_RESPONSE",
    status: 502,
    message: "Что-то пошло не так, попробуйте позже",
  });
});

// 12. Сервер недоступен
test("Сетевая ошибка: сервер недоступен", async (t) => {
  t.mock.method(globalThis, "fetch", async () => {
    throw new TypeError("Failed to fetch");
  });

  await assert.rejects(() => getTemplates(), {
    code: "NETWORK_ERROR",
    status: 0,
    message: "Что-то пошло не так, попробуйте позже",
  });
});

// 13. Ответ не содержит код ошибки
test("Неожиданный ответ: отсутствует code", async (t) => {
  t.mock.method(globalThis, "fetch", async () =>
    Response.json({ message: "Unknown error" }, { status: 400 }),
  );

  await assert.rejects(() => getTemplates(), {
    code: "INVALID_RESPONSE",
    status: 400,
    message: "Что-то пошло не так, попробуйте позже",
  });
});
