# project-4-front

## Run with Docker

From the repository root, start the frontend, backend, and MySQL together:

```powershell
docker compose up --build -d --wait --wait-timeout 240
```

Open <http://localhost:8081> and use **Sign in** (`admin` / `Admin123!`).
The backend is available at <http://localhost:8080>. See the
[repository README](../README.md#project-4-with-docker) for configuration and stop commands.

## Project setup
```
npm install
```

### Compiles and hot-reloads for development
```
npm run serve
```

### Compiles and minifies for production
```
npm run build
```

### Lints and fixes files
```
npm run lint
```

### Customize configuration
See [Configuration Reference](https://cli.vuejs.org/config/).
