<template>
  <div class="app-root">
    <div class="relative min-h-screen flex items-center justify-center p-6">
      <div class="relative z-10 w-full max-w-md">
        <div class="card p-8">
          <div class="mb-7">
            <div
              class="inline-flex items-center rounded-full border-2 border-slate-900/10 bg-light-3 px-3 py-1 text-xs font-bold text-dark tracking-wider"
            >
              WELCOME
            </div>
            <h1 class="text-3xl font-extrabold mt-3 text-dark">登录</h1>
            <div class="text-sm text-dark-2 mt-2">欢迎回来，继续你的交易。</div>
          </div>

          <form class="space-y-4" @submit.prevent="onSubmit">
            <div class="relative">
              <i class="fas fa-user absolute left-3 top-1/2 -translate-y-1/2 text-light-1"></i>
              <input
                v-model="studentId"
                type="text"
                placeholder="学号/社区号"
                class="w-full border-2 border-slate-900/10 bg-light-3 rounded-2xl pl-10 pr-4 py-3.5 focus:outline-none focus:ring-2 focus:ring-primary/30 focus:border-primary transition"
              />
            </div>
            <div class="relative">
              <i class="fas fa-lock absolute left-3 top-1/2 -translate-y-1/2 text-light-1"></i>
              <input
                v-model="password"
                type="password"
                placeholder="密码"
                class="w-full border-2 border-slate-900/10 bg-light-3 rounded-2xl pl-10 pr-4 py-3.5 focus:outline-none focus:ring-2 focus:ring-primary/30 focus:border-primary transition"
              />
            </div>

            <button
              class="w-full bg-primary text-white py-3.5 rounded-2xl font-extrabold border-2 border-slate-900/10 shadow-[6px_6px_0_rgba(15,23,42,0.12)] hover:opacity-95 active:translate-x-[2px] active:translate-y-[2px] active:shadow-[4px_4px_0_rgba(15,23,42,0.12)] transition disabled:opacity-60 disabled:cursor-not-allowed"
              :disabled="loading"
            >
              {{ loading ? '登录中…' : '登录' }}
            </button>
          </form>

          <p class="text-sm text-dark-2 mt-6">
            没有账户？
            <router-link to="/register" class="text-primary font-semibold hover:underline">去注册</router-link>
          </p>
        </div>
      </div>
    </div>
  </div>
</template>
<script setup lang="ts">
import { ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import http from "@/api/http";

const router = useRouter();
const route = useRoute();
const studentId = ref("");
const password = ref("");
const loading = ref(false);

async function onSubmit() {
  if (!studentId.value || !password.value) {
    alert("请输入学号/社区号和密码");
    return;
  }
  loading.value = true;
  try {
    const { data } = await http.post("/api/auth/login", {
      student_id: studentId.value,
      username: studentId.value,
      password: password.value,
    });
    // 保存 JWT（后续请求由 http.ts 自动带上 Authorization: Bearer <access>）
    localStorage.setItem("access", data.access);
    localStorage.setItem("refresh", data.refresh);
    localStorage.setItem("user", JSON.stringify(data.user));
    const redirect = typeof route.query.redirect === "string" ? route.query.redirect : "/";
    router.push(redirect.startsWith("/") ? redirect : "/");
  } catch (e: any) {
    const data = e?.response?.data;
    const msg =
      (typeof data?.detail === "string" && data.detail) ||
      (data && typeof data === "object" && JSON.stringify(data)) ||
      "登录失败";
    alert(msg);
  } finally {
    loading.value = false;
  }
}
</script>
