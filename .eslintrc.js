module.exports = {
  env: {
    browser: true,
    commonjs: true,
    node: true,
  },
  extends: ['prettier', 'eslint:recommended'],
  parser: '@typescript-eslint/parser',
  parserOptions: {
    ecmaVersion: 2018,
    sourceType: 'module',
    ecmaFeatures: {
      jsx: false,
    },
  },
  plugins: ['@typescript-eslint/eslint-plugin'],
  rules: {
    'no-underscore-dangle': 'off',
    'class-methods-use-this': 'off',
    camelcase: 'off',
    'no-unused-vars': 'warn',
    'no-undef': 'warn',
  },
  settings: {
    'import/resolver': {
      node: {
        extensions: ['.ts'],
        typescript: {},
      },
    },
  },
};
