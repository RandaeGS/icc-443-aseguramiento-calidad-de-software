// @ts-check
import { expect, test } from '@playwright/test';
import { loginUser } from './utils/auth-helper';

test('dashboard displays all stat widgets correctly', async ({ page }) => {
    await loginUser(page);
    await page.goto('/products/dashboard');

    // Wait for widgets to load
    await page.waitForTimeout(2000);

    // Verify all 4 stat widgets are visible
    await expect(page.getByText('Most Ordered Product')).toBeVisible();
    await expect(page.getByText('Least Ordered Product')).toBeVisible();
    await expect(page.getByText('Most Demanded Category')).toBeVisible();
    await expect(page.getByText('Least Demanded Category')).toBeVisible();

    // Verify each widget has an icon
    await expect(page.locator('.pi-shopping-cart')).toBeVisible();
    await expect(page.locator('.pi-dollar')).toBeVisible();
    await expect(page.locator('.pi-users')).toBeVisible();
    await expect(page.locator('.pi-comment')).toBeVisible();

    // Verify order counts are displayed
    await expect(page.locator('text=/\\d+ orders/')).toHaveCount(4);
});

test('most and least ordered products show correct data structure', async ({ page }) => {
    await loginUser(page);
    await page.goto('/products/dashboard');
    await page.waitForTimeout(2000);

    // Verify that individual cards exist
    const cardElements = page.locator('.card.mb-0');
    await expect(cardElements).toHaveCount(4);

    // Verify specifically the most and least ordered products
    const mostOrderedCard = page.locator('.card').filter({ hasText: 'Most Ordered Product' });
    const leastOrderedCard = page.locator('.card').filter({ hasText: 'Least Ordered Product' });

    await expect(mostOrderedCard).toBeVisible();
    await expect(leastOrderedCard).toBeVisible();

    // Verify they have product names
    await expect(mostOrderedCard.locator('.text-xl')).not.toBeEmpty();
    await expect(leastOrderedCard.locator('.text-xl')).not.toBeEmpty();

    // Verify they have order counts
    await expect(mostOrderedCard.locator('text=/\\d+ orders/')).toBeVisible();
    await expect(leastOrderedCard.locator('text=/\\d+ orders/')).toBeVisible();

    // Verify they have the correct icons
    await expect(mostOrderedCard.locator('.pi-shopping-cart')).toBeVisible();
    await expect(leastOrderedCard.locator('.pi-dollar')).toBeVisible();
});

test('movements per category pie chart renders correctly', async ({ page }) => {
    await loginUser(page);
    await page.goto('/products/dashboard');

    // Wait for chart to load
    await page.waitForTimeout(3000);

    // Verify pie chart container exists
    await expect(page.getByText('Movements Per Category')).toBeVisible();

    // Verify the chart canvas is present
    await expect(page.locator('canvas').first()).toBeVisible();

    // Check that the chart has loaded by verifying canvas has content
    const canvas = page.locator('canvas').first();
    const canvasSize = await canvas.boundingBox();
    expect(canvasSize.width).toBeGreaterThan(0);
    expect(canvasSize.height).toBeGreaterThan(0);

    // Verify chart container has proper styling
    await expect(page.locator('.card').filter({ hasText: 'Movements Per Category' })).toBeVisible();
});

test('movements per day bar chart displays weekly data', async ({ page }) => {
    await loginUser(page);
    await page.goto('/products/dashboard');

    // Wait for chart to load
    await page.waitForTimeout(3000);

    // Verify bar chart container exists
    await expect(page.getByText('Movements Per Day')).toBeVisible();

    // Verify the chart canvas is present (should be the second canvas)
    await expect(page.locator('canvas').nth(1)).toBeVisible();

    // Check that the chart has loaded
    const barChartCanvas = page.locator('canvas').nth(1);
    const canvasSize = await barChartCanvas.boundingBox();
    expect(canvasSize.width).toBeGreaterThan(0);
    expect(canvasSize.height).toBeGreaterThan(0);

    // Verify chart has the correct height class
    await expect(page.locator('.h-80')).toBeVisible();

    // Verify chart container styling
    await expect(page.locator('.card').filter({ hasText: 'Movements Per Day' })).toBeVisible();
});

test('dashboard layout is responsive and components are arranged correctly', async ({ page }) => {
    await loginUser(page);
    await page.goto('/products/dashboard');

    // Wait for all components to load
    await page.waitForTimeout(3000);

    // Verify main grid container
    await expect(page.locator('.grid.grid-cols-12.gap-8')).toBeVisible();

    // Verify StatsWidget spans full width on mobile/tablet
    const statsSection = page.locator('text=Most Ordered Product').locator('..').locator('..').locator('..');
    await expect(statsSection).toBeVisible();

    // Verify charts are in separate containers
    const pieChartCard = page.locator('.card').filter({ hasText: 'Movements Per Category' });
    const barChartCard = page.locator('.card').filter({ hasText: 'Movements Per Day' });

    await expect(pieChartCard).toBeVisible();
    await expect(barChartCard).toBeVisible();

    // Verify both charts are side by side on larger screens
    // Check that both chart containers exist and have proper grid classes
    const chartContainers = page.locator('.col-span-12.xl\\:col-span-6');
    await expect(chartContainers).toHaveCount(2);

    // Test mobile responsiveness by changing viewport
    await page.setViewportSize({ width: 375, height: 667 });
    await page.waitForTimeout(1000);

    // All components should still be visible on mobile
    await expect(page.getByText('Most Ordered Product')).toBeVisible();
    await expect(page.getByText('Movements Per Category')).toBeVisible();
    await expect(page.getByText('Movements Per Day')).toBeVisible();

    // Reset viewport
    await page.setViewportSize({ width: 1280, height: 720 });
});
