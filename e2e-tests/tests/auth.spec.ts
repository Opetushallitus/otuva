import { expect, Page, test } from '@playwright/test';

test.describe('Authentication', () => {
    const username = process.env.TEST_USER || 'testuser';
    const password = process.env.TEST_PASS || 'password';

    const login = async (page: Page) => {
        await navigateAndRetryUntilRedirectedToDelegatedIdp(page, '/mock-substance-service/secure');

        await page.fill('input[name="username"]', username);
        await page.fill('input[name="password"]', password);

        await page.click('button[name="login"]');
    };

    test('should login successfully', async ({ page }) => {
        await login(page);

        // Await for the url to have ticket
        await page.waitForURL((url) => url.href === 'http://localhost:8180/mock-substance-service/')

        // Check ticket
        const credentials = page.getByText('Credentials');
        expect(await credentials.innerText()).toMatch(/ST-/);
    });

    test('should logout successfully', async ({ page }) => {
        await login(page);

        await page.waitForURL((url) => url.href === 'http://localhost:8180/mock-substance-service/');

        await page.goto('/mock-substance-service/logout');

        await page.waitForURL((url) => url.href === 'http://localhost:8180/mock-substance-service/');

        const credentials = page.getByText('Credentials');
        expect(await credentials.innerText()).not.toMatch(/ST-/);
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
