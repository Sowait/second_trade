import { createRouter, createWebHistory } from 'vue-router'
import MarketplaceView from '../views/MarketplaceView.vue'
import Register from "../views/Register.vue";
import Login from "../views/Login.vue";
import SellDeviceView from "../views/SellDeviceView.vue";
import OrderCenterView from "../views/OrderCenterView.vue";
import ProfileView from "../views/ProfileView.vue";
import AdminUsersView from "../views/AdminUsersView.vue";

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {path: '/', name: 'marketplace', component: MarketplaceView,},
    {path: '/register', name: 'register', component: Register,},
    {path: '/login', name: 'login', component: Login,},
    {path: '/seller', name: 'seller', component: SellDeviceView,},
    {path: '/orders', name: 'orders', component: OrderCenterView,},
    {path: '/profile', name: 'profile', component: ProfileView,},
    {path: '/admin', name: 'admin', component: AdminUsersView,},
  ],
})

function getAccessToken(): string | null {
  const v =
    localStorage.getItem("access_token") ||
    localStorage.getItem("access") ||
    localStorage.getItem("token");
  if (!v) return null;
  const t = v.trim();
  if (!t || t === "null" || t === "undefined") return null;
  return t;
}

function clearAuth() {
  localStorage.removeItem("access_token");
  localStorage.removeItem("access");
  localStorage.removeItem("token");
  localStorage.removeItem("refresh");
  localStorage.removeItem("user");
}

function isJwtExpired(token: string): boolean {
  try {
    const parts = token.split(".");
    if (parts.length < 2) return true;
    const payloadPart = parts[1];
    if (!payloadPart) return true;
    const payloadJson = atob(payloadPart.replace(/-/g, "+").replace(/_/g, "/"));
    const payload = JSON.parse(payloadJson);
    const exp = Number(payload?.exp);
    if (!Number.isFinite(exp)) return false;
    const now = Math.floor(Date.now() / 1000);
    return exp <= now;
  } catch {
    return true;
  }
}

let authCheckPromise: Promise<boolean> | null = null;
async function ensureAuthed(): Promise<boolean> {
  const token = getAccessToken();
  if (!token) return false;
  if (isJwtExpired(token)) {
    clearAuth();
    return false;
  }
  if (authCheckPromise) return authCheckPromise;
  authCheckPromise = fetch("/api/auth/me/", {
    method: "GET",
    headers: { Authorization: `Bearer ${token}` },
  })
    .then((r) => {
      if (r.ok) return true;
      if (r.status === 401 || r.status === 403) {
        clearAuth();
        return false;
      }
      return true;
    })
    .catch(() => true)
    .finally(() => {
      authCheckPromise = null;
    });
  return authCheckPromise;
}

router.beforeEach(async (to) => {
  const publicPaths = new Set(["/login", "/register"]);
  if (publicPaths.has(to.path)) {
    if (await ensureAuthed()) {
      const redirect = typeof to.query.redirect === "string" ? to.query.redirect : "/";
      if (redirect.startsWith("/")) return redirect;
      return "/";
    }
    return true;
  }

  if (!(await ensureAuthed())) {
    return { path: "/login", query: { redirect: to.fullPath } };
  }

  return true;
});

export default router
