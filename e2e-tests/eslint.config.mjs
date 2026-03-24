import { defineConfig } from 'eslint/config';
// import eslint from '@eslint/js';
import tseslint from 'typescript-eslint';
import eslintPluginPrettierRecommended from 'eslint-plugin-prettier/recommended';

export default defineConfig([
  // eslint.configs.recommended,
  tseslint.configs.strict,
  tseslint.configs.stylistic,
  eslintPluginPrettierRecommended,
  {
    rules: {
      '@typescript-eslint/consistent-type-definitions': ['error', 'type'],
      '@typescript-eslint/no-explicit-any': 1,
      '@typescript-eslint/no-invalid-void-type': 1, // does not recognise rtk generic type arguments
      '@typescript-eslint/no-non-null-assertion': 1,
      '@typescript-eslint/no-unused-vars': [
        'error',
        {
          argsIgnorePattern: '^_',
          varsIgnorePattern: '^_',
          caughtErrorsIgnorePattern: '^_',
        },
      ],
    },
  },
  {
  // Note: there should be no other properties in this object
    ignores: [
      'coverage/*',
      'node_modules/*',
      'playwright-report/*',
      'eslint.config.mjs',
      'test-results/*',
    ],
  },
]);
