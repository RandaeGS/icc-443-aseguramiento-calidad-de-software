// @ts-check
import { expect, test } from '@playwright/test';

test('should display sakai', async ({ page }) => {
    await page.goto('/');

    await expect(page.getByText('Sakai')).toHaveCount(3);
});

test('get started button exists', async ({ page }) => {
    await page.goto('/');

    // Click the get started link.
    await page.getByRole('button', { name: 'Get Started' }).isVisible();
});
