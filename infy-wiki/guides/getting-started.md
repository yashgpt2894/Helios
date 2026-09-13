# Getting Started & Development

**Summary:** This part of the system covers the prerequisite setup, dependency installation, local development server execution, production compilation, and type checking.

## How it works

The project requires Node 18+ and uses `npm` as its package manager. After cloning `https://github.com/yashgpt2894/Helios.git` and entering the repository directory, running `npm install` installs the dependencies defined in `package.json`. 

The development server is started via `npm run dev`, which uses Vite to launch a local server accessible at `http://localhost:5173`. For production, `npm run build` performs type checking through project compilation via `tsc -b` and builds the static assets and service worker via `vite build`. The production output can be tested locally using `npm run preview`. Type checking can also be run independently using `npm run typecheck`, which executes `tsc -p tsconfig.json --noEmit`.

## Key files

| File | Role |
|---|---|
| `package.json` | Defines project metadata, dependencies, devDependencies, and build/dev scripts. |
| `tsconfig.node.json` | Configures TypeScript compiler options for `vite.config.ts`. |
| `vite.config.ts` | Configures Vite, the PWA manifest, and font caching. |

## Gotchas

None found in the supplied evidence.

## Sources

- [README.md](infy://wiki/40d98eee-a3a7-4272-ba0a-285793586ec1/pages/2032f430-d157-44aa-9925-8d6521a117b5) · SHA-256 c9fb5dc1f8184d9ec23434faa2f300d69fb66584cfbba5e9eb0b0e73e7513731
- [package.json](infy://wiki/40d98eee-a3a7-4272-ba0a-285793586ec1/pages/5f9fe69a-b287-4960-931b-074828a4bbe0) · SHA-256 48e8d4f247cfc63c5825832bc3cc88ce811df79847b12769794561ca3e6b139b
- [tsconfig.node.json](infy://wiki/40d98eee-a3a7-4272-ba0a-285793586ec1/pages/18c318b5-828f-44a5-b251-ae4814690db9) · SHA-256 5150009f63762f78ec239f776f346ea5959760eb3af6695a408aa73360a670af