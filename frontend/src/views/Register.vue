<template>
  <div class="app-root">
    <div class="min-h-screen flex items-center justify-center p-6">
      <div class="card p-8 max-w-md w-full">
        <div class="mb-6 text-center">
          <div
            class="inline-flex items-center rounded-full border-2 border-slate-900/10 bg-light-3 px-3 py-1 text-xs font-bold text-dark tracking-wider"
          >
            CREATE ACCOUNT
          </div>
          <h1 class="text-3xl font-extrabold mt-3 text-dark">用户注册</h1>
        </div>

        <form class="space-y-5" @submit.prevent="onSubmit">
          <div class="flex flex-col">
            <label class="mb-1 font-bold text-dark-2">学号</label>
            <input
              v-model="studentId"
              type="text"
              class="border-2 border-slate-900/10 bg-light-3 rounded-2xl px-4 py-3 focus:outline-none focus:ring-2 focus:ring-primary/30 focus:border-primary"
            />
          </div>

          <div class="flex flex-col">
            <label class="mb-1 font-bold text-dark-2">学校</label>
            <input
              v-model="school"
              type="text"
              class="border-2 border-slate-900/10 bg-light-3 rounded-2xl px-4 py-3 focus:outline-none focus:ring-2 focus:ring-primary/30 focus:border-primary"
            />
          </div>

          <div class="flex flex-col">
            <label class="mb-1 font-bold text-dark-2">邮箱</label>
            <input
              v-model="email"
              type="email"
              class="border-2 border-slate-900/10 bg-light-3 rounded-2xl px-4 py-3 focus:outline-none focus:ring-2 focus:ring-primary/30 focus:border-primary"
            />
          </div>

          <div class="flex flex-col">
            <label class="mb-1 font-bold text-dark-2">手机号</label>
            <input
              v-model="mobile"
              type="text"
              class="border-2 border-slate-900/10 bg-light-3 rounded-2xl px-4 py-3 focus:outline-none focus:ring-2 focus:ring-primary/30 focus:border-primary"
            />
          </div>

          <div class="flex flex-col">
            <label class="mb-1 font-bold text-dark-2">密码</label>
            <input
              v-model="pwd"
              type="password"
              class="border-2 border-slate-900/10 bg-light-3 rounded-2xl px-4 py-3 focus:outline-none focus:ring-2 focus:ring-primary/30 focus:border-primary"
            />
          </div>

          <div class="flex flex-col">
            <label class="mb-1 font-bold text-dark-2">确认密码</label>
            <input
              v-model="confirm"
              type="password"
              class="border-2 border-slate-900/10 bg-light-3 rounded-2xl px-4 py-3 focus:outline-none focus:ring-2 focus:ring-primary/30 focus:border-primary"
            />
          </div>

          <button
            class="w-full bg-primary text-white py-3.5 rounded-2xl font-extrabold border-2 border-slate-900/10 shadow-[6px_6px_0_rgba(15,23,42,0.12)] hover:opacity-95 active:translate-x-[2px] active:translate-y-[2px] active:shadow-[4px_4px_0_rgba(15,23,42,0.12)] transition"
          >
            注册
          </button>
        </form>

        <p class="text-sm text-dark-2 mt-5 text-center">
          已有账户？
          <router-link to="/login" class="text-primary font-bold hover:underline">立即登录</router-link>
        </p>
      </div>
    </div>
  </div>
</template>
<script setup lang="ts">
import { ref } from "vue";
import { useRouter } from "vue-router";
import http from "@/api/http";

const router = useRouter();
const studentId = ref("");
const school = ref("");
const email = ref("");
const mobile = ref("");
const pwd = ref("");
const confirm = ref("");

async function onSubmit() {
  if (!studentId.value || !school.value || !email.value || !mobile.value || !pwd.value || !confirm.value) {
    alert("请完整填写必填项");
    return;
  }
  if (pwd.value !== confirm.value) {
    alert("两次输入的密码不一致");
    return;
  }
  try {
    await http.post("/api/auth/register", {
      student_id: studentId.value,
      username: studentId.value,
      school: school.value,
      email: email.value,
      phone: mobile.value,
      password: pwd.value,
    });
    alert("注册成功，前往登录");
    router.push("/login");
  } catch (e: any) {
    const data = e?.response?.data;
    const msg =
      (typeof data?.detail === "string" && data.detail) ||
      (data && typeof data === "object" && JSON.stringify(data)) ||
      "注册失败";
    alert(msg);
  }
}
</script>
