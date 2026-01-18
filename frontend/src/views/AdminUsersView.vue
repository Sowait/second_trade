<template>
  <div class="p-6 md:p-8">
    <div class="max-w-6xl mx-auto">
      <div class="flex flex-col md:flex-row md:items-end md:justify-between gap-4 mb-6">
        <div>
          <h1 class="text-2xl md:text-3xl font-bold text-slate-800">系统管理</h1>
          <div class="mt-4 inline-flex rounded-xl border border-slate-200 bg-white p-1 gap-1">
            <button
              class="px-4 py-2 rounded-lg text-sm font-semibold transition"
              :class="tab === 'audit' ? 'bg-[#165DFF] text-white' : 'text-slate-700 hover:bg-slate-50'"
              type="button"
              @click="tab = 'audit'"
            >
              审核管理
            </button>
            <button
              class="px-4 py-2 rounded-lg text-sm font-semibold transition"
              :class="tab === 'users' ? 'bg-[#165DFF] text-white' : 'text-slate-700 hover:bg-slate-50'"
              type="button"
              @click="tab = 'users'"
            >
              用户管理
            </button>
            <button
              class="px-4 py-2 rounded-lg text-sm font-semibold transition"
              :class="tab === 'orders' ? 'bg-[#165DFF] text-white' : 'text-slate-700 hover:bg-slate-50'"
              type="button"
              @click="tab = 'orders'"
            >
              订单管理
            </button>
          </div>
        </div>

        <div v-if="tab === 'audit'" class="flex flex-wrap items-center gap-3">
          <div class="relative">
            <i class="fas fa-search absolute left-3 top-1/2 -translate-y-1/2 text-slate-400"></i>
            <input
              v-model.trim="auditQ"
              class="pl-9 pr-3 py-2 w-64 rounded-xl border border-slate-200 focus:outline-none focus:ring-2 focus:ring-[#165DFF]/30"
              placeholder="搜索标题/学校/卖家ID"
              type="text"
            />
          </div>
          <button
            class="px-4 py-2 rounded-xl border border-slate-200 text-slate-700 hover:bg-slate-50 transition inline-flex items-center gap-2"
            type="button"
            @click="loadAudit()"
          >
            <i class="fas fa-rotate"></i>
            刷新
          </button>
        </div>
        <div v-else-if="tab === 'users'" class="flex flex-wrap items-center gap-3">
          <div class="relative">
            <i class="fas fa-search absolute left-3 top-1/2 -translate-y-1/2 text-slate-400"></i>
            <input
              v-model.trim="usersQ"
              class="pl-9 pr-3 py-2 w-64 rounded-xl border border-slate-200 focus:outline-none focus:ring-2 focus:ring-[#165DFF]/30"
              placeholder="搜索学号/邮箱/手机号/学校"
              type="text"
            />
          </div>
          <button
            class="px-4 py-2 rounded-xl border border-slate-200 text-slate-700 hover:bg-slate-50 transition inline-flex items-center gap-2"
            type="button"
            @click="loadUsers()"
          >
            <i class="fas fa-rotate"></i>
            刷新
          </button>
        </div>
        <div v-else class="flex flex-wrap items-center gap-3">
          <div class="relative">
            <i class="fas fa-search absolute left-3 top-1/2 -translate-y-1/2 text-slate-400"></i>
            <input
              v-model.trim="ordersQ"
              class="pl-9 pr-3 py-2 w-64 rounded-xl border border-slate-200 focus:outline-none focus:ring-2 focus:ring-[#165DFF]/30"
              placeholder="搜索订单号/商品/买卖家ID"
              type="text"
            />
          </div>

          <select
            v-model="ordersStatus"
            class="px-3 py-2 rounded-xl border border-slate-200 bg-white focus:outline-none focus:ring-2 focus:ring-[#165DFF]/30"
          >
            <option value="">全部状态</option>
            <option value="pending_payment">待付款</option>
            <option value="pending_shipment">待发货/待自提</option>
            <option value="shipped">已发货</option>
            <option value="pending_receipt">待收货</option>
            <option value="completed">已完成</option>
            <option value="received">已完成</option>
            <option value="refunded">已取消</option>
          </select>

          <input
            v-model="ordersStart"
            class="px-3 py-2 rounded-xl border border-slate-200 focus:outline-none focus:ring-2 focus:ring-[#165DFF]/30"
            type="date"
          />
          <input
            v-model="ordersEnd"
            class="px-3 py-2 rounded-xl border border-slate-200 focus:outline-none focus:ring-2 focus:ring-[#165DFF]/30"
            type="date"
          />

          <button
            class="px-4 py-2 rounded-xl bg-[#165DFF] text-white font-semibold hover:opacity-95 transition inline-flex items-center gap-2"
            type="button"
            @click="loadOrders()"
          >
            <i class="fas fa-magnifying-glass"></i>
            查询
          </button>
          <button
            class="px-4 py-2 rounded-xl border border-slate-200 text-slate-700 hover:bg-slate-50 transition inline-flex items-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed"
            type="button"
            :disabled="ordersLoading"
            @click="exportOrdersCsv()"
          >
            <i class="fas fa-file-export"></i>
            导出CSV
          </button>
        </div>
      </div>

      <div v-if="tab === 'audit'" class="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
        <div class="hidden md:grid grid-cols-12 px-6 py-3 bg-slate-50 border-b border-slate-200 text-sm text-slate-500 font-semibold">
          <div class="col-span-5">商品</div>
          <div class="col-span-2">卖家ID</div>
          <div class="col-span-2">学校</div>
          <div class="col-span-1">价格</div>
          <div class="col-span-2 text-right">操作</div>
        </div>

        <div v-if="auditLoading" class="p-10 flex items-center justify-center text-slate-500">
          <div class="w-10 h-10 border-4 border-slate-200 border-t-[#165DFF] rounded-full animate-spin"></div>
          <span class="ml-3">加载中...</span>
        </div>
        <div v-else class="divide-y divide-slate-200">
          <div v-for="p in auditItems" :key="p.id" class="px-6 py-4 hover:bg-slate-50 transition">
            <div class="grid grid-cols-1 md:grid-cols-12 gap-3 md:gap-0 md:items-center">
              <div
                class="md:col-span-5 flex items-center gap-3 min-w-0 cursor-pointer"
                role="button"
                tabindex="0"
                @click="openAuditDetail(p)"
              >
                <img :src="toAbsUrl(p.main_image_url || p.main_image) || fallbackImage" class="w-12 h-12 rounded-xl border border-slate-200 object-cover" alt="img" />
                <div class="min-w-0">
                  <div class="font-semibold text-slate-800 truncate">{{ p.title || `商品#${p.id}` }}</div>
                  <div class="text-sm text-slate-500 truncate">
                    {{ fmtTime(p.created_at) }}
                  </div>
                </div>
              </div>
              <div class="md:col-span-2 font-semibold text-slate-800">{{ p.seller_id ?? '-' }}</div>
              <div class="md:col-span-2 text-slate-700 truncate">{{ p.school || '-' }}</div>
              <div class="md:col-span-1 font-semibold text-slate-800">¥{{ fmtMoney(p.selling_price) }}</div>
              <div class="md:col-span-2 flex md:justify-end gap-2">
                <button
                  class="px-3 py-2 rounded-xl bg-[#00B42A] text-white hover:opacity-95 transition text-sm font-semibold inline-flex items-center gap-2"
                  type="button"
                  @click="approveProduct(p)"
                >
                  <i class="fas fa-check"></i> 通过
                </button>
                <button
                  class="px-3 py-2 rounded-xl bg-[#F53F3F] text-white hover:opacity-95 transition text-sm font-semibold inline-flex items-center gap-2"
                  type="button"
                  @click="openReject(p)"
                >
                  <i class="fas fa-xmark"></i> 驳回
                </button>
              </div>
            </div>
          </div>
          <div v-if="auditItems.length === 0" class="p-10 text-center text-slate-500">暂无待审核物品</div>
        </div>

        <div class="px-6 py-4 border-t border-slate-200 bg-white flex items-center justify-between">
          <div class="text-sm text-slate-500">
            共 {{ auditTotal }} 条 · 第 {{ auditPage }} / {{ auditPageCount }} 页
          </div>
          <div class="flex items-center gap-2">
            <button
              class="px-3 py-2 rounded-xl border border-slate-200 text-slate-700 hover:bg-slate-50 transition disabled:opacity-50 disabled:cursor-not-allowed"
              :disabled="auditPage <= 1"
              @click="auditPage--"
            >
              上一页
            </button>
            <button
              class="px-3 py-2 rounded-xl border border-slate-200 text-slate-700 hover:bg-slate-50 transition disabled:opacity-50 disabled:cursor-not-allowed"
              :disabled="auditPage >= auditPageCount"
              @click="auditPage++"
            >
              下一页
            </button>
          </div>
        </div>
      </div>

      <div v-else-if="tab === 'users'" class="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
        <div class="hidden md:grid grid-cols-12 px-6 py-3 bg-slate-50 border-b border-slate-200 text-sm text-slate-500 font-semibold">
          <div class="col-span-4">用户</div>
          <div class="col-span-3">注册时间</div>
          <div class="col-span-3">联系方式</div>
          <div class="col-span-2 text-right">操作</div>
        </div>

        <div v-if="usersLoading" class="p-10 flex items-center justify-center text-slate-500">
          <div class="w-10 h-10 border-4 border-slate-200 border-t-[#165DFF] rounded-full animate-spin"></div>
          <span class="ml-3">加载中...</span>
        </div>
        <div v-else class="divide-y divide-slate-200">
          <div v-for="u in usersItems" :key="u.id" class="px-6 py-4 hover:bg-slate-50 transition">
            <div class="grid grid-cols-1 md:grid-cols-12 gap-3 md:gap-0 md:items-center">
              <div class="md:col-span-4 flex items-center gap-3 min-w-0">
                <img :src="u.avatar || fallbackAvatar" class="w-10 h-10 rounded-xl border border-slate-200 object-cover" alt="avatar" />
                <div class="min-w-0">
                  <div class="font-semibold text-slate-800 truncate">
                    {{ u.student_id || u.username }}
                    <span v-if="u.nickname" class="text-slate-500 font-medium">· {{ u.nickname }}</span>
                    <span
                      v-if="u.is_disabled"
                      class="ml-2 inline-flex items-center px-2 py-0.5 rounded-full text-[11px] font-semibold bg-[#F53F3F]/10 text-[#F53F3F]"
                    >
                      已禁用
                    </span>
                    <span
                      v-else-if="u.role === 'admin'"
                      class="ml-2 inline-flex items-center px-2 py-0.5 rounded-full text-[11px] font-semibold bg-[#165DFF]/10 text-[#165DFF]"
                    >
                      管理员
                    </span>
                  </div>
                  <div class="text-sm text-slate-500 truncate">{{ u.school || '-' }}</div>
                </div>
              </div>

              <div class="md:col-span-3 text-slate-700">{{ fmtTime(u.created_at) }}</div>

              <div class="md:col-span-3 text-slate-700 truncate">{{ u.phone || '-' }} · {{ u.email || '-' }}</div>

              <div class="md:col-span-2 flex md:justify-end gap-2">
                <button
                  class="px-3 py-2 rounded-xl bg-[#F53F3F] text-white hover:opacity-95 transition text-sm font-semibold inline-flex items-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed"
                  type="button"
                  :disabled="u.is_disabled || u.role === 'admin'"
                  @click="disableUser(u)"
                >
                  <i class="fas fa-ban"></i> 禁用
                </button>
              </div>
            </div>
          </div>
          <div v-if="usersItems.length === 0" class="p-10 text-center text-slate-500">暂无用户</div>
        </div>

        <div class="px-6 py-4 border-t border-slate-200 bg-white flex items-center justify-between">
          <div class="text-sm text-slate-500">
            共 {{ usersTotal }} 条 · 第 {{ usersPage }} / {{ usersPageCount }} 页
          </div>
          <div class="flex items-center gap-2">
            <button
              class="px-3 py-2 rounded-xl border border-slate-200 text-slate-700 hover:bg-slate-50 transition disabled:opacity-50 disabled:cursor-not-allowed"
              :disabled="usersPage <= 1"
              @click="usersPage--"
            >
              上一页
            </button>
            <button
              class="px-3 py-2 rounded-xl border border-slate-200 text-slate-700 hover:bg-slate-50 transition disabled:opacity-50 disabled:cursor-not-allowed"
              :disabled="usersPage >= usersPageCount"
              @click="usersPage++"
            >
              下一页
            </button>
          </div>
        </div>
      </div>

      <div v-else class="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
        <div class="hidden md:grid grid-cols-12 px-6 py-3 bg-slate-50 border-b border-slate-200 text-sm text-slate-500 font-semibold">
          <div class="col-span-3">订单</div>
          <div class="col-span-3">商品</div>
          <div class="col-span-2">买家/卖家</div>
          <div class="col-span-2">状态</div>
          <div class="col-span-2 text-right">时间</div>
        </div>

        <div v-if="ordersLoading" class="p-10 flex items-center justify-center text-slate-500">
          <div class="w-10 h-10 border-4 border-slate-200 border-t-[#165DFF] rounded-full animate-spin"></div>
          <span class="ml-3">加载中...</span>
        </div>
        <div v-else class="divide-y divide-slate-200">
          <div v-for="o in ordersItems" :key="o.id" class="px-6 py-4 hover:bg-slate-50 transition">
            <div class="grid grid-cols-1 md:grid-cols-12 gap-3 md:gap-0 md:items-center">
              <div class="md:col-span-3 min-w-0">
                <div class="font-semibold text-slate-800 truncate">{{ o.order_no }}</div>
                <div class="text-sm text-slate-500 truncate">¥{{ fmtMoney(o.amount ?? o.product_selling_price) }}</div>
              </div>
              <div class="md:col-span-3 flex items-center gap-3 min-w-0">
                <img :src="toAbsUrl(o.product_main_image) || fallbackImage" class="w-10 h-10 rounded-xl border border-slate-200 object-cover" alt="p" />
                <div class="min-w-0">
                  <div class="text-slate-800 font-semibold truncate">{{ o.product_title || `商品#${o.product_id}` }}</div>
                  <div class="text-sm text-slate-500 truncate">商品ID：{{ o.product_id }}</div>
                </div>
              </div>
              <div class="md:col-span-2 text-slate-700">
                <div class="truncate">买：{{ o.buyer_name || `#${o.buyer_id}` }}</div>
                <div class="truncate">卖：{{ o.seller_name || `#${o.seller_id}` }}</div>
              </div>
              <div class="md:col-span-2">
                <span class="inline-flex items-center px-3 py-1 rounded-full text-xs font-semibold bg-slate-100 text-slate-700">
                  {{ orderStatusText(o.status, o.pickup, o.status_view) }}
                </span>
              </div>
              <div class="md:col-span-2 text-right text-slate-700">
                <div class="truncate">{{ fmtTime(o.created_at) }}</div>
                <div class="text-sm text-slate-500 truncate">{{ o.paid_at ? `支付：${fmtTime(o.paid_at)}` : '' }}</div>
              </div>
            </div>
          </div>
          <div v-if="ordersItems.length === 0" class="p-10 text-center text-slate-500">暂无订单</div>
        </div>

        <div class="px-6 py-4 border-t border-slate-200 bg-white flex items-center justify-between">
          <div class="text-sm text-slate-500">
            共 {{ ordersTotal }} 条 · 第 {{ ordersPage }} / {{ ordersPageCount }} 页
          </div>
          <div class="flex items-center gap-2">
            <button
              class="px-3 py-2 rounded-xl border border-slate-200 text-slate-700 hover:bg-slate-50 transition disabled:opacity-50 disabled:cursor-not-allowed"
              :disabled="ordersPage <= 1"
              @click="ordersPage--"
            >
              上一页
            </button>
            <button
              class="px-3 py-2 rounded-xl border border-slate-200 text-slate-700 hover:bg-slate-50 transition disabled:opacity-50 disabled:cursor-not-allowed"
              :disabled="ordersPage >= ordersPageCount"
              @click="ordersPage++"
            >
              下一页
            </button>
          </div>
        </div>
      </div>
    </div>

    <ProductModal v-model:open="auditDetailOpen" :product="auditDetailTarget" />

    <div v-if="rejectOpen" class="fixed inset-0 z-50 bg-black/50 flex items-center justify-center p-6" @click.self="closeReject">
      <div class="w-full max-w-lg bg-white rounded-2xl shadow-lg overflow-hidden">
        <div class="px-6 py-4 border-b border-slate-200 flex items-center justify-between">
          <div class="text-lg font-bold text-slate-800">驳回原因</div>
          <button class="text-slate-400 hover:text-slate-700" type="button" @click="closeReject">
            <i class="fas fa-times text-lg"></i>
          </button>
        </div>
        <div class="p-6 space-y-4">
          <div class="text-sm text-slate-700">
            {{ rejectTarget?.title || `商品#${rejectTarget?.id}` }}
          </div>
          <textarea
            v-model.trim="rejectReason"
            class="w-full min-h-[100px] rounded-xl border border-slate-200 px-3 py-2 focus:outline-none focus:ring-2 focus:ring-[#165DFF]/30"
            placeholder="请输入驳回理由"
          ></textarea>
          <div class="flex justify-end gap-3">
            <button type="button" class="px-4 py-2 rounded-xl border border-slate-200 text-slate-700 hover:bg-slate-50 transition" @click="closeReject">
              取消
            </button>
            <button
              type="button"
              class="px-4 py-2 rounded-xl bg-[#F53F3F] text-white font-semibold hover:opacity-95 transition disabled:opacity-50 disabled:cursor-not-allowed"
              :disabled="rejecting || !rejectTarget"
              @click="submitReject"
            >
              {{ rejecting ? '处理中…' : '确认驳回' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from "vue";
import http from "@/api/http";
import ProductModal from "@/components/common/ProductModal.vue";

const tab = ref<"audit" | "users" | "orders">("audit");

const fallbackAvatar =
  "https://design.gemcoder.com/staticResource/echoAiSystemImages/877eae20b4cd21fa5cfbb0e03eedd327.png";
const fallbackImage = "https://placehold.co/80x80?text=IMG";

const API_ORIGIN =
  (import.meta as any).env?.VITE_API_ORIGIN ||
  (import.meta as any).env?.VITE_BACKEND_ORIGIN ||
  "http://127.0.0.1:8000";

function toAbsUrl(u?: string | null) {
  const s = typeof u === "string" ? u.trim() : "";
  if (!s || s === "null" || s === "undefined") return "";
  if (/^(https?:)?\/\//.test(s) || s.startsWith("blob:") || s.startsWith("data:")) return s;
  if (s.startsWith("/")) return `${API_ORIGIN}${s}`;
  return `${API_ORIGIN}/${s}`;
}

type AuditProduct = {
  id: number;
  title?: string;
  seller_id?: number;
  school?: string;
  created_at?: string;
  selling_price?: number | string;
  main_image?: string;
  main_image_url?: string;
};

type AdminUserRow = {
  id: number;
  username: string;
  student_id?: string;
  nickname?: string;
  email?: string;
  phone?: string;
  school?: string;
  role?: "user" | "admin";
  avatar?: string;
  is_disabled?: boolean;
  created_at?: string;
};

type AdminOrderRow = {
  id: number;
  order_no: string;
  status: string;
  status_view?: string;
  buyer_id?: number;
  seller_id?: number;
  buyer_name?: string;
  seller_name?: string;
  product_id: number;
  product_title?: string;
  product_main_image?: string;
  product_selling_price?: number | string;
  amount?: number | string;
  created_at?: string;
  paid_at?: string;
  pickup?: boolean;
};

const auditLoading = ref(false);
const auditQ = ref("");
const auditPage = ref(1);
const auditPageSize = 10;
const auditTotal = ref(0);
const auditItems = ref<AuditProduct[]>([]);

const auditDetailOpen = ref(false);
const auditDetailTarget = ref<AuditProduct | null>(null);

const rejectOpen = ref(false);
const rejecting = ref(false);
const rejectTarget = ref<AuditProduct | null>(null);
const rejectReason = ref("");

function openAuditDetail(p: AuditProduct) {
  if (rejectOpen.value) closeReject();
  auditDetailTarget.value = p;
  auditDetailOpen.value = true;
}

async function loadAudit() {
  auditLoading.value = true;
  try {
    const params: any = { page: auditPage.value, page_size: auditPageSize };
    if (auditQ.value) params.q = auditQ.value;
    const resp: any = await http.get("/api/market/admin/products/pending_review/", { params });
    const data: any = resp?.data ?? resp;
    const results = Array.isArray(data?.results) ? data.results : Array.isArray(data) ? data : [];
    auditItems.value = results as AuditProduct[];
    auditTotal.value = Number(data?.count ?? results.length ?? 0);
  } catch (e: any) {
    const d = e?.response?.data;
    const msg = d?.detail || d?.error || (typeof d === "string" ? d : "加载待审核列表失败");
    alert(msg);
    auditItems.value = [];
    auditTotal.value = 0;
  } finally {
    auditLoading.value = false;
  }
}

async function approveProduct(p: AuditProduct) {
  if (!confirm(`确定通过 ${p.title || `商品#${p.id}`} 吗？`)) return;
  try {
    await http.post(`/api/market/admin/products/${p.id}/approve/`);
    await loadAudit();
  } catch (e: any) {
    const d = e?.response?.data;
    const msg = d?.detail || d?.error || (typeof d === "string" ? d : "操作失败");
    alert(msg);
  }
}

function openReject(p: AuditProduct) {
  rejectTarget.value = p;
  rejectReason.value = "";
  rejectOpen.value = true;
  document.body.style.overflow = "hidden";
}
function closeReject() {
  rejectOpen.value = false;
  rejectTarget.value = null;
  rejectReason.value = "";
  document.body.style.overflow = "";
}
async function submitReject() {
  if (!rejectTarget.value) return;
  rejecting.value = true;
  try {
    await http.post(`/api/market/admin/products/${rejectTarget.value.id}/reject/`, { reason: rejectReason.value || "" });
    closeReject();
    await loadAudit();
  } catch (e: any) {
    const d = e?.response?.data;
    const msg = d?.detail || d?.error || (typeof d === "string" ? d : "操作失败");
    alert(msg);
  } finally {
    rejecting.value = false;
  }
}

const auditPageCount = computed(() => Math.max(1, Math.ceil(auditTotal.value / auditPageSize)));

watch([auditQ], () => {
  if (tab.value !== "audit") return;
  auditPage.value = 1;
  loadAudit();
});
watch([auditPage], () => {
  if (tab.value !== "audit") return;
  loadAudit();
});

const usersLoading = ref(false);
const usersQ = ref("");
const usersPage = ref(1);
const usersPageSize = 10;
const usersTotal = ref(0);
const usersItems = ref<AdminUserRow[]>([]);

async function loadUsers() {
  usersLoading.value = true;
  try {
    const params: any = { page: usersPage.value, page_size: usersPageSize };
    if (usersQ.value) params.q = usersQ.value;
    const resp: any = await http.get("/api/auth/admin/users/", { params });
    const data: any = resp?.data ?? resp;
    const results = Array.isArray(data?.results) ? data.results : Array.isArray(data) ? data : [];
    usersItems.value = results as AdminUserRow[];
    usersTotal.value = Number(data?.count ?? results.length ?? 0);
  } catch (e: any) {
    const d = e?.response?.data;
    const msg = d?.detail || d?.error || (typeof d === "string" ? d : "加载用户列表失败");
    alert(msg);
    usersItems.value = [];
    usersTotal.value = 0;
  } finally {
    usersLoading.value = false;
  }
}

async function disableUser(u: AdminUserRow) {
  if (!confirm(`确定禁用用户 ${u.student_id || u.username} 吗？`)) return;
  try {
    await http.post(`/api/auth/admin/users/${u.id}/disable/`);
    await loadUsers();
  } catch (e: any) {
    const d = e?.response?.data;
    const msg = d?.detail || d?.error || (typeof d === "string" ? d : "操作失败");
    alert(msg);
  }
}

const usersPageCount = computed(() => Math.max(1, Math.ceil(usersTotal.value / usersPageSize)));

watch([usersQ], () => {
  if (tab.value !== "users") return;
  usersPage.value = 1;
  loadUsers();
});
watch([usersPage], () => {
  if (tab.value !== "users") return;
  loadUsers();
});

const ordersLoading = ref(false);
const ordersQ = ref("");
const ordersStatus = ref("");
const ordersStart = ref("");
const ordersEnd = ref("");
const ordersPage = ref(1);
const ordersPageSize = 10;
const ordersTotal = ref(0);
const ordersItems = ref<AdminOrderRow[]>([]);

async function loadOrders() {
  ordersLoading.value = true;
  try {
    const params: any = { page: ordersPage.value, page_size: ordersPageSize };
    if (ordersQ.value) params.q = ordersQ.value;
    if (ordersStatus.value) params.status = ordersStatus.value;
    if (ordersStart.value) params.start = ordersStart.value;
    if (ordersEnd.value) params.end = ordersEnd.value;
    const resp: any = await http.get("/api/market/admin/orders/", { params });
    const data: any = resp?.data ?? resp;
    const results = Array.isArray(data?.results) ? data.results : Array.isArray(data) ? data : [];
    ordersItems.value = results as AdminOrderRow[];
    ordersTotal.value = Number(data?.count ?? results.length ?? 0);
  } catch (e: any) {
    const d = e?.response?.data;
    const msg = d?.detail || d?.error || (typeof d === "string" ? d : "加载订单列表失败");
    alert(msg);
    ordersItems.value = [];
    ordersTotal.value = 0;
  } finally {
    ordersLoading.value = false;
  }
}

const ordersPageCount = computed(() => Math.max(1, Math.ceil(ordersTotal.value / ordersPageSize)));

watch([ordersPage], () => {
  if (tab.value !== "orders") return;
  loadOrders();
});

watch([ordersQ, ordersStatus, ordersStart, ordersEnd], () => {
  if (tab.value !== "orders") return;
  ordersPage.value = 1;
});

function csvCell(v: any) {
  const s = v == null ? "" : String(v);
  const escaped = s.replace(/"/g, '""');
  return `"${escaped}"`;
}

function downloadCsv(filename: string, rows: any[][]) {
  const content = "\ufeff" + rows.map((r) => r.map(csvCell).join(",")).join("\n");
  const blob = new Blob([content], { type: "text/csv;charset=utf-8" });
  const url = URL.createObjectURL(blob);
  const a = document.createElement("a");
  a.href = url;
  a.download = filename;
  document.body.appendChild(a);
  a.click();
  a.remove();
  URL.revokeObjectURL(url);
}

async function exportOrdersCsv() {
  try {
    const params: any = { page: 1, page_size: 5000 };
    if (ordersQ.value) params.q = ordersQ.value;
    if (ordersStatus.value) params.status = ordersStatus.value;
    if (ordersStart.value) params.start = ordersStart.value;
    if (ordersEnd.value) params.end = ordersEnd.value;
    const resp: any = await http.get("/api/market/admin/orders/", { params });
    const data: any = resp?.data ?? resp;
    const list = Array.isArray(data?.results) ? data.results : Array.isArray(data) ? data : [];
    const rows: any[][] = [
      ["订单号", "状态", "商品", "商品ID", "买家", "卖家", "金额", "下单时间", "支付时间", "自提"],
      ...list.map((o: AdminOrderRow) => [
        o.order_no,
        orderStatusText(o.status, o.pickup, o.status_view),
        o.product_title || "",
        o.product_id,
        o.buyer_name || o.buyer_id || "",
        o.seller_name || o.seller_id || "",
        o.amount ?? o.product_selling_price ?? "",
        o.created_at || "",
        o.paid_at || "",
        o.pickup ? "是" : "否",
      ]),
    ];
    const ts = new Date();
    const yyyy = ts.getFullYear();
    const mm = String(ts.getMonth() + 1).padStart(2, "0");
    const dd = String(ts.getDate()).padStart(2, "0");
    downloadCsv(`orders_${yyyy}${mm}${dd}.csv`, rows);
  } catch (e: any) {
    const d = e?.response?.data;
    const msg = d?.detail || d?.error || (typeof d === "string" ? d : "导出失败");
    alert(msg);
  }
}

function fmtMoney(v: any) {
  const n = typeof v === "string" ? Number(v) : v;
  if (Number.isNaN(n)) return String(v ?? "-");
  return n.toFixed(2).replace(/\.00$/, "");
}

function fmtTime(raw: any) {
  if (!raw) return "-";
  try {
    const d = new Date(String(raw));
    if (Number.isNaN(d.getTime())) return String(raw);
    return d.toLocaleString();
  } catch {
    return String(raw);
  }
}

function orderStatusText(status: string, pickup?: boolean, statusView?: string) {
  if (statusView) return statusView;
  const st = status ? String(status) : "";
  if (st === "pending_payment") return "待付款";
  if (st === "pending_shipment") return pickup ? "待自提" : "待发货";
  if (st === "pending_receipt") return "待收货";
  if (st === "shipped") return "已发货";
  if (st === "completed" || st === "received") return "已完成";
  if (st === "refunded") return "已取消";
  return st || "-";
}

watch(
  tab,
  (t) => {
    if (t === "audit") loadAudit();
    if (t === "users") loadUsers();
    if (t === "orders") loadOrders();
  },
  { immediate: true }
);
</script>
