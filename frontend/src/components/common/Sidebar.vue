<template>
  <aside
    class="bg-white border-2 border-slate-900/10 shadow-card z-20 flex flex-col transition-all duration-300 ease-in-out rounded-3xl"
    :class="collapsed ? 'w-20' : 'w-64'"
  >
    <!-- Logo -->
    <div class="flex items-center justify-center h-16 border-b-2 border-slate-900/10">
      <div class="flex items-center space-x-2">
        <span
          class="text-xl font-extrabold text-dark tracking-wide"
          :class="collapsed ? 'hidden' : ''"
        >
          二手电子产品交易中心
        </span>
      </div>
    </div>

    <!-- User -->
    <div class="p-4 border-b-2 border-slate-900/10">
      <div class="flex items-center space-x-3">
        <img
          src="https://design.gemcoder.com/staticResource/echoAiSystemImages/4db1f4e22c08cab67f2bb5c522cad076.png"
          class="w-10 h-10 rounded-2xl object-cover border-2 border-slate-900/10 bg-light-3"
          alt="用户头像"
        />
        <div :class="collapsed ? 'hidden' : ''">
          <h3 class="font-medium text-dark">{{ displayName }}</h3>
          <div class="flex items-center text-xs text-dark-2">
            <span class="inline-block w-2 h-2 rounded-full mr-1" :class="creditDotClass"></span>
            <span>{{ creditLabel }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Nav -->
    <nav class="flex-1 overflow-y-auto scrollbar-hide py-4">
      <ul class="px-2">
        <li v-if="!isSuperuser" class="mb-1">
          <RouterLink
            to="/"
            class="flex items-center space-x-3 py-2.5 rounded-2xl group transition-all duration-200 border-2 border-transparent"
            :class="[
              collapsed ? 'justify-center px-2' : 'px-4',
              isMarket
                ? 'bg-primary text-white border-slate-900/10 shadow-sm'
                : 'bg-light-3 text-dark-2 hover:bg-white hover:border-slate-900/10'
            ]"
          >
            <span
              class="h-9 w-9 inline-flex items-center justify-center rounded-2xl border-2 border-slate-900/10 bg-white/70"
              :class="collapsed ? '' : ' -ml-1'"
            >
              <i class="fas fa-th-large text-base"></i>
            </span>
            <span :class="collapsed ? 'hidden' : ''">市场大厅</span>
          </RouterLink>
        </li>

        <li class="mb-1" v-for="item in menus" :key="item.text">
          <RouterLink
            :to="item.to"
            class="flex items-center space-x-3 py-2.5 rounded-2xl group transition-all duration-200 border-2 border-transparent"
            :class="[
              collapsed ? 'justify-center px-2' : 'px-4',
              isActive(item.to)
                ? 'bg-primary text-white border-slate-900/10 shadow-sm'
                : 'bg-light-3 text-dark-2 hover:bg-white hover:border-slate-900/10'
            ]"
          >
            <span
              class="h-9 w-9 inline-flex items-center justify-center rounded-2xl border-2 border-slate-900/10 bg-white/70"
              :class="collapsed ? '' : ' -ml-1'"
            >
              <i :class="item.icon + ' text-base'"></i>
            </span>
            <span :class="collapsed ? 'hidden' : ''">{{ item.text }}</span>
          </RouterLink>
        </li>
      </ul>
    </nav>

    <!-- Bottom -->
    <div class="p-4 border-t-2 border-slate-900/10">
      <a
        href="javascript:void(0);"
        class="flex items-center space-x-3 py-2.5 rounded-2xl text-dark-2 group transition-all duration-200 bg-light-3 border-2 border-transparent hover:bg-white hover:border-slate-900/10"
        :class="collapsed ? 'justify-center px-2' : 'px-4'"
      >
        <span
          class="h-9 w-9 inline-flex items-center justify-center rounded-2xl border-2 border-slate-900/10 bg-white/70"
          :class="collapsed ? '' : ' -ml-1'"
        >
          <i class="fas fa-question-circle text-base"></i>
        </span>
        <span :class="collapsed ? 'hidden' : ''">帮助中心</span>
      </a>
    </div>
  </aside>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { useRoute } from "vue-router";

defineProps<{ collapsed: boolean }>();
defineEmits<{ (e: "toggle"): void }>();

const rawMenus = [
  { icon: "fas fa-tag", text: "卖家中心", to: "/seller", userOnly: true },
  { icon: "fas fa-shopping-cart", text: "订单中心", to: "/orders", userOnly: true },
  { icon: "fas fa-user-circle", text: "个人中心", to: "/profile" },
  { icon: "fas fa-cog", text: "系统管理", to: "/admin", adminOnly: true },
] as const;

function parseMaybeJson(raw: string | null): any {
  if (!raw) return null;
  try {
    return JSON.parse(raw);
  } catch {
    return null;
  }
}

function getStorageItem(key: string): string | null {
  return localStorage.getItem(key) ?? sessionStorage.getItem(key);
}

function truthySuperuser(v: any): boolean {
  return v === 1 || v === "1" || v === true || v === "true";
}

function decodeJwtPayload(token: string): any {
  try {
    const parts = token.split(".");
    if (parts.length < 2) return null;
    if (!parts[1]) return null;
    const base64 = parts[1].replace(/-/g, "+").replace(/_/g, "/");
    const padded = base64 + "===".slice((base64.length + 3) % 4);
    const json = decodeURIComponent(
      atob(padded)
        .split("")
        .map((c) => "%" + ("00" + c.charCodeAt(0).toString(16)).slice(-2))
        .join("")
    );
    return JSON.parse(json);
  } catch {
    return null;
  }
}

const isSuperuser = computed(() => {
  try {
    const u1 = parseMaybeJson(getStorageItem("user"));
    if (u1 && truthySuperuser(u1?.is_superuser)) return true;

    const u2 = parseMaybeJson(getStorageItem("profile"));
    if (u2 && truthySuperuser(u2?.is_superuser)) return true;

    const u3 = parseMaybeJson(getStorageItem("me"));
    if (u3 && truthySuperuser(u3?.is_superuser)) return true;

    const auth = parseMaybeJson(getStorageItem("auth"));
    const au = auth?.user ?? auth?.data?.user ?? null;
    if (au && truthySuperuser(au?.is_superuser)) return true;

    // fallback: decode access token and check role
    const access =
      (parseMaybeJson(getStorageItem("access")) as any) ||
      getStorageItem("access") ||
      getStorageItem("access_token") ||
      "";
    const token = typeof access === "string" ? access : "";
    if (token) {
      const payload = decodeJwtPayload(token);
      if (payload?.is_superuser && truthySuperuser(payload?.is_superuser)) return true;
      if (payload?.role === "admin") return true;
    }

    return false;
  } catch {
    return false;
  }
});

const currentUser = computed(() => {
  // try common storage keys in order
  const u1 = parseMaybeJson(getStorageItem("user"));
  if (u1) return u1;

  const u2 = parseMaybeJson(getStorageItem("profile"));
  if (u2) return u2;

  const u3 = parseMaybeJson(getStorageItem("me"));
  if (u3) return u3;

  const auth = parseMaybeJson(getStorageItem("auth"));
  const au = auth?.user ?? auth?.data?.user ?? null;
  if (au) return au;

  // fallback: decode token payload (may have username/user_id but usually not credit)
  const access =
    (parseMaybeJson(getStorageItem("access")) as any) ||
    getStorageItem("access") ||
    getStorageItem("access_token") ||
    "";
  const token = typeof access === "string" ? access : "";
  if (token) return decodeJwtPayload(token);

  return null;
});

const displayName = computed(() => {
  const u: any = currentUser.value;
  const name =
    (typeof u?.nickname === "string" && u.nickname.trim()) ||
    (typeof u?.name === "string" && u.name.trim()) ||
    (typeof u?.username === "string" && u.username.trim()) ||
    (typeof u?.account === "string" && u.account.trim()) ||
    "用户";
  return name;
});

const creditScore = computed<number | null>(() => {
  const u: any = currentUser.value;
  const raw = u?.credit_score ?? u?.creditScore ?? u?.credit;
  const n = Number(raw);
  return Number.isFinite(n) ? n : null;
});

const creditLabel = computed(() => {
  const s = creditScore.value;
  if (s === null) return "信用未知";
  if (s <= 60) return "信用极差";
  if (s <= 80) return "信用较差";
  if (s <= 100) return "信用一般";
  return "信用极好";
});

const creditDotClass = computed(() => {
  const s = creditScore.value;
  if (s === null) return "bg-gray-400";
  if (s <= 60) return "bg-red-500";
  if (s <= 80) return "bg-orange-500";
  if (s <= 100) return "bg-yellow-500";
  return "bg-green-500";
});

const menus = computed(() => {
  if (isSuperuser.value) {
    return rawMenus.filter((m: any) => !m.userOnly);
  }
  return rawMenus.filter((m: any) => !m.adminOnly);
});

const route = useRoute();

const isMarket = computed(() => route.path === "/");

function isActive(prefix: string) {
  return route.path === prefix || route.path.startsWith(prefix + "/");
}
</script>
