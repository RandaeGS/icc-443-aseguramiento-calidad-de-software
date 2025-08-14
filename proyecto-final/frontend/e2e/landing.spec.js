// @ts-check
import { expect, test } from '@playwright/test';

test('should display sakai', async ({ page }) => {
    await page.goto('/');

    await expect(page.getByText('Sakai')).toHaveCount(3);
});

test('get started button exists', async ({ page }) => {
    await page.goto('/');

    // Click the get started link.
    await page.getByRole('link', { name: 'Get Started' }).isVisible();
});

test('get started redirects to login', async ({ page }) => {
    await page.goto('/');

    // Click the get started link.
    await page.getByRole('link', { name: 'Get Started' }).click();

    // Wait for keycloak login site
    await page.waitForURL('**/realms/project/protocol/openid-connect/auth**');
    await expect(page.getByText('project')).toBeVisible();
    await expect(page.locator('#username')).toBeVisible();
});

test('login redirects to products', async ({ page }) => {
    await page.goto('/');

    // Wait for keycloak login site
    await page.getByRole('link', { name: 'Get Started' }).click();
    await page.waitForURL('**/realms/project/protocol/openid-connect/auth**');

    await page.locator('#username').fill('admin');
    await page.locator('#password').fill('123456');

    await page.locator('#kc-login').click();
    await page.waitForURL('**/products**');
});
