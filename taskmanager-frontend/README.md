# TaskFlow - Frontend

This is the frontend for the TaskFlow Team Task Manager.

## Usage

### Option 1: Served by Spring Boot (Recommended)
The `index.html` is already included in the backend at:
`src/main/resources/static/index.html`

When you run the Spring Boot app, the frontend is available at:
http://localhost:8080

### Option 2: Standalone (Point to deployed backend)
If you want to host the frontend separately, edit the `API` variable
at the top of the `<script>` section in `index.html`:

```js
const API = 'https://your-railway-app.up.railway.app';
```

Then open `index.html` in a browser or host it on Netlify/Vercel.

## Demo Credentials (dev mode with seeded data)
- Admin: admin@demo.com / admin123
- Member: member@demo.com / member123
