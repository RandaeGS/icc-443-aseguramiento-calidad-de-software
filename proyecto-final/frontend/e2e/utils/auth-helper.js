export async function loginUser(page, username = 'employee', password = '123456') {
    await page.goto('/');
    await page.getByRole('link', { name: 'Get Started' }).click();
    await page.waitForURL('**/realms/project/protocol/openid-connect/auth**');

    await page.locator('#username').fill(username);
    await page.locator('#password').fill(password);

    await page.locator('#kc-login').click();
    await page.waitForURL('**/products**');
}
