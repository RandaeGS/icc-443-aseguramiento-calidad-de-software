// @ts-check
import { expect, test } from '@playwright/test';
import { loginUser } from './utils/auth-helper';

test('title is correct', async ({ page }) => {
    await loginUser(page);

    await expect(page.getByText('Manage Products')).toBeVisible();
    await expect(page.getByRole('button', { name: 'New' })).toBeVisible();
    await expect(page.getByRole('button', { name: 'Delete' })).toBeVisible();
    await expect(page.locator('#filter')).toBeVisible();
});

test('new shows product form dialog', async ({ page }) => {
    await loginUser(page);

    // Click New button
    await page.getByRole('button', { name: 'New' }).click();

    // Verify dialog appears with correct title
    await expect(page.getByRole('dialog', { name: 'Product Details' })).toBeVisible();
    await expect(page.locator('#productDialog').locator('#name')).toBeVisible();
    await expect(page.locator('#productDialog').locator('#description')).toBeVisible();
    await expect(page.locator('#productDialog').locator('#category')).toBeVisible();
});

test('create new product with valid data', async ({ page }) => {
    await loginUser(page);

    // Generate random product name to avoid conflicts
    const randomNum = Math.floor(Math.random() * 10000);
    const productName = `Test Product E2E ${randomNum}`;

    // Open new product dialog
    await page.getByRole('button', { name: 'New' }).click();

    // Fill form with valid data using dialog context
    await page.locator('#productDialog').locator('#name').fill(productName);
    await page.locator('#productDialog').locator('#description').fill('Test description for E2E testing');
    await page.locator('#productDialog').locator('#category').click();
    await page.getByRole('option', { name: 'Electronics' }).click();
    await page.locator('#productDialog').locator('#price input').fill('100');
    await page.locator('#productDialog').locator('#cost input').fill('50');
    await page.locator('#productDialog').locator('#minimumStock input').fill('10');
    await page.locator('#productDialog').locator('#quantity input').fill('20');

    // Save product
    await page.getByRole('button', { name: 'Save' }).click();

    // Verify success message and product appears in list
    await expect(page.getByText('Product saved')).toBeVisible();
    await expect(page.getByText(productName)).toBeVisible();
});

test('validate required fields when creating product', async ({ page }) => {
    await loginUser(page);

    // Open new product dialog
    await page.getByRole('button', { name: 'New' }).click();

    // Try to save without filling required fields
    await page.getByRole('button', { name: 'Save' }).click();

    // Verify validation messages appear
    await expect(page.getByText('Name is required (min 3 chars)')).toBeVisible();
    await expect(page.getByText('Description is required (min 3 chars)')).toBeVisible();
    await expect(page.getByText('Category is required')).toBeVisible();
    await expect(page.getByText('Price must be greater than 0')).toBeVisible();
    await expect(page.getByText('Cost must be greater than 0')).toBeVisible();
});

test('profit calculates automatically when price and cost change', async ({ page }) => {
    await loginUser(page);

    // Open new product dialog
    await page.getByRole('button', { name: 'New' }).click();

    // Fill price and cost
    await page.locator('#productDialog').locator('#price input').fill('100');
    await page.locator('#productDialog').locator('#cost input').fill('30');

    // Verify profit is calculated automatically (100 - 30 = 70)
    await expect(page.locator('#productDialog').locator('#profit input')).toHaveValue('$70.00');

    // Change values and verify recalculation
    await page.locator('#productDialog').locator('#price input').fill('200');

    // Change focus to calculate profit
    await page.locator('#productDialog').locator('#quantity input').focus();
    await expect(page.locator('#productDialog').locator('#profit input')).toHaveValue('$170.00');
});

test('search products by name', async ({ page }) => {
    await loginUser(page);

    // Wait for products to load
    await page.waitForSelector('[data-pc-section="table"]');

    // Search for a specific product
    await page.getByPlaceholder('Search...').fill('Test');
    await page.locator('#search-button').click();

    // Verify search results contain only products with "Test" in name
    await page.waitForTimeout(1000); // Wait for search results
    const productRows = page.locator('[data-pc-section="table"] tbody tr');
    const firstRow = productRows.first();
    await expect(firstRow).toContainText('Test');
});

test('filter products by category and price range', async ({ page }) => {
    await loginUser(page);

    // Open filters dialog
    await page.locator('#filter').click();
    let dialog = await page.locator('#filters-dialog');
    await expect(dialog).toBeVisible();

    // Set category filter
    await dialog.locator('#categoryFilter').click();
    await page.getByRole('option', { name: 'Electronics' }).click();

    // Set price range
    await dialog.locator('#minPrice input').fill('50');
    await dialog.locator('#maxPrice input').fill('200');

    // Apply filters
    await dialog.getByRole('button', { name: 'Apply' }).click();

    // Verify filters dialog closes
    await expect(dialog).toBeHidden();

    // Verify filtered results (this would depend on existing data)
    await page.waitForTimeout(1000);
});

test('edit existing product', async ({ page }) => {
    await loginUser(page);

    const randomNum = Math.floor(Math.random() * 10000);
    const productName = `Edited Test Product E2E ${randomNum}`;

    // Wait for products to load
    await page.waitForSelector('[data-pc-section="table"]');

    // Click edit button on first product
    await page.locator('.edit-product').first().click();

    // Verify dialog opens with existing data
    await expect(page.getByRole('dialog', { name: 'Product Details' })).toBeVisible();

    // Modify product name
    const nameField = page.locator('#productDialog').locator('#name');
    await nameField.clear();
    await nameField.fill(productName);

    // Save changes
    await page.getByRole('button', { name: 'Save' }).click();

    // Verify success message
    await expect(page.getByText('Product updated')).toBeVisible();
});

test('delete single product with confirmation', async ({ page }) => {
    await loginUser(page);
    // Wait for products to load
    await page.waitForSelector('[data-pc-section="table"]');

    // Click edit button on first product
    await page.locator('.delete-product').first().click();

    // Verify confirmation dialog appears
    await expect(page.getByRole('dialog', { name: 'Confirm' })).toBeVisible();
    await expect(page.getByText('Are you sure you want to delete')).toBeVisible();

    // Confirm deletion
    await page.getByRole('button', { name: 'Yes' }).click();

    // Verify success message
    await expect(page.getByText('Product deleted')).toBeVisible();
});

test('pagination works correctly', async ({ page }) => {
    await loginUser(page);

    // Wait for products to load
    await page.waitForSelector('[data-pc-section="table"]');

    // Verify pagination controls are visible
    await expect(page.getByRole('button', { name: 'Next Page' })).toBeVisible();
    await expect(page.getByText(/Showing \d+ to \d+ of \d+ products/)).toBeVisible();

    // Change rows per page to 10
    await page.getByRole('combobox', { name: 'Rows per page' }).click();
    await page.getByRole('option', { name: '10' }).click();

    // Verify page updates
    await page.waitForTimeout(1000);
    await expect(page.getByText(/Showing \d+ to \d+ of \d+ products/)).toBeVisible();

    // Navigate to next page if available
    const nextButton = page.getByRole('button', { name: 'Next Page' });
    if (await nextButton.isEnabled()) {
        await nextButton.click();
        await page.waitForTimeout(1000);
        await expect(page.getByText(/Showing \d+ to \d+ of \d+ products/)).toBeVisible();
    }
});
