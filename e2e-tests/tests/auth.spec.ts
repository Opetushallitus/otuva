import { test, expect, Page } from '@playwright/test';

test.describe('Authentication', () => {
    const username = process.env.TEST_USER || 'testuser';
    const password = process.env.TEST_PASS || 'password';

    const login = async (page: Page) => {
        await page.goto('/secure');

        await page.fill('input[name="username"]', username);
        await page.fill('input[name="password"]', password);

        await page.click('button[name="login"]');
    };

    test('should login successfully', async ({ page }) => {
        await login(page);

        // Await for the url to have ticket
        await page.waitForURL((url) => url.searchParams.has('ticket'));
        // Check ticket
        const url = new URL(page.url());
        expect(url.searchParams.get('ticket')).toMatch(/^ST-/);
    });

    test('should logout successfully', async ({ page }) => {
        await login(page);

        await page.goto('/logout');

        await page.waitForURL((url) => !url.searchParams.has('ticket'));
        const url = new URL(page.url());
        expect(url.searchParams.get('ticket')).toBeNull();
    });
});
