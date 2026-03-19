import { test, expect } from '@playwright/test';

test.describe('Authentication', () => {
  const username = process.env.TEST_USER || 'testuser';
  const password = process.env.TEST_PASS || 'password';

  test('should login successfully', async ({ page }) => {
    // Navigate to the login page
    await page.goto('/secure');
    //await page.goto('/secure');

    // Fill in the username and password
    await page.fill('input[name="username"]', username);
    await page.fill('input[name="password"]', password);
    
    // Login
    await page.click('button[name="login"]');

    // Await for the url to have ticket
    await page.waitForURL(url => url.searchParams.has('ticket'));
    // Check ticket
    const url = new URL(page.url());
    expect(url.searchParams.get('ticket')).toMatch(/^ST-/);
  });
});
