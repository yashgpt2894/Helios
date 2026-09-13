# Tsconfig.Node

```json
{
  "compilerOptions": {
    "target": "ES2022",
    "lib": ["ES2023", "DOM"],
    "module": "ESNext",
    "skipLibCheck": true,
    "moduleResolution": "bundler",
    "allowSyntheticDefaultImports": true,
    "strict": true,
    "composite": true,
    "noEmit": false,
    "outDir": "./dist-tsc"
  },
  "include": ["vite.config.ts"]
}

```