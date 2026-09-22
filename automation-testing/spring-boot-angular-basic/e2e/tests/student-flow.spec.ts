import { test, expect } from '@playwright/test';

// Uebung 2: End-to-End-Test der Angular-Oberflaeche mit Playwright.
// Voraussetzung: Backend (Port 8081) UND Frontend (Port 4200, "ng serve")
// laufen bereits lokal, siehe AUFGABEN5.md.

test('Startseite zeigt beide Navigationslinks', async ({ page }) => {
  await page.goto('/');

  await expect(page.getByRole('link', { name: 'List Students' })).toBeVisible();
  await expect(page.getByRole('link', { name: 'Add Students' })).toBeVisible();
});

test('Studierendenliste zeigt die bestehenden Studierenden aus dem Backend', async ({ page }) => {
  await page.goto('/');
  await page.getByRole('link', { name: 'List Students' }).click();

  await expect(page).toHaveURL(/.*\/students/);
  // Namen kommen echt vom Backend (GET http://localhost:8081/students)
  await expect(page.getByRole('cell', { name: 'Jonas', exact: true })).toBeVisible();
});

test('Neuen Studierenden über das Formular anlegen und in der Liste sehen', async ({ page }) => {
  const uniqueName = 'E2E-Test-' + Date.now();
  const email = uniqueName.toLowerCase() + '@tbz.ch';

  await page.goto('/addstudents');

  await page.getByLabel('Name').fill(uniqueName);
  await page.getByLabel('Email').fill(email);
  await page.getByRole('button', { name: 'Submit' }).click();

  // Nach dem Absenden navigiert die App automatisch zur Liste
  await expect(page).toHaveURL(/.*\/students/);
  await expect(page.getByRole('cell', { name: uniqueName, exact: true })).toBeVisible();
});

test('Submit-Button ist deaktiviert solange Pflichtfelder leer sind', async ({ page }) => {
  await page.goto('/addstudents');

  await expect(page.getByRole('button', { name: 'Submit' })).toBeDisabled();

  await page.getByLabel('Name').fill('Nur Name');
  await expect(page.getByRole('button', { name: 'Submit' })).toBeDisabled();

  await page.getByLabel('Email').fill('nur.name@tbz.ch');
  await expect(page.getByRole('button', { name: 'Submit' })).toBeEnabled();
});
