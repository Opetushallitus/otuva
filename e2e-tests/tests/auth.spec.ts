import { expect, Page, test } from '@playwright/test';

test.describe('Authentication', () => {
    const username = process.env.TEST_USER || 'testuser';
    const password = process.env.TEST_PASS || 'password';

    const login = async (page: Page) => {
        await navigateAndRetryUntilRedirectedToDelegatedIdp(page, '/secure');

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

async function navigateAndRetryUntilRedirectedToDelegatedIdp(page: Page, url: string) {
    const MAX_ATTEMPTS = 5;
    for (let attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
        await page.goto(url);
        try {
            await page.waitForURL((url) => isAtDelegatedIdp(url), { timeout: 5000 });
            return;
        } catch {
            if (attempt === MAX_ATTEMPTS) {
                throw new Error(`Failed to reach Keycloak IdP after ${MAX_ATTEMPTS} attempts. Last URL: ${page.url()}`);
            }
        }
    }
}

const isAtDelegatedIdp = (url: URL) => {
    return url.toString().startsWith('http://localhost:8083/realms/test/protocol/saml');
};
