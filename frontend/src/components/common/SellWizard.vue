<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from "vue";
import http from "@/api/http";
import {
  getDeviceModelReference,
  initDraft,
  initDraftFromProduct,
  publishDraft,
  publishDraftUpdateProduct,
  uploadDraftImages,
  estimateDraft,
} from "@/api/market";

type PublishResult = Record<string, any>;

type Step = 1 | 3;

// v-model:open 控制弹窗显隐
const props = withDefaults(
  defineProps<{
    open: boolean;
    mode?: "create" | "edit";
    productId?: number | string | null;
  }>(),
  {
    mode: "create",
    productId: null,
  },
);
const emit = defineEmits<{
  (e: "update:open", v: boolean): void;
  (e: "close"): void;
  (e: "success", payload: any): void;
}>();

// 统一处理全站滚动锁：同时作用于 html/body，且可在卸载时兜底解锁
const _scrollLock = {
  locked: false,
  bodyOverflow: "" as string,
  htmlOverflow: "" as string,
};

function lockBodyScroll(lock: boolean) {
  const body = document?.body;
  const html = document?.documentElement;
  if (!body || !html) return;

  if (lock) {
    if (_scrollLock.locked) return;
    _scrollLock.locked = true;

    // 记录原始 overflow，便于恢复
    _scrollLock.bodyOverflow = body.style.overflow || "";
    _scrollLock.htmlOverflow = html.style.overflow || "";

    // Tailwind class + 兜底 style（避免某些情况下 class 不生效或被覆盖）
    body.classList.add("overflow-hidden");
    html.classList.add("overflow-hidden");
    body.style.overflow = "hidden";
    html.style.overflow = "hidden";
  } else {
    if (!_scrollLock.locked) {
      // 仍然做一次兜底清理，防止残留
      body.classList.remove("overflow-hidden");
      html.classList.remove("overflow-hidden");
      body.style.overflow = "";
      html.style.overflow = "";
      return;
    }

    _scrollLock.locked = false;
    body.classList.remove("overflow-hidden");
    html.classList.remove("overflow-hidden");

    // 恢复原始 overflow
    body.style.overflow = _scrollLock.bodyOverflow;
    html.style.overflow = _scrollLock.htmlOverflow;
  }
}

// 组件卸载兜底：即使路由跳转或父组件直接销毁，也要解锁滚动
onBeforeUnmount(() => {
  lockBodyScroll(false);
});

watch(
  () => props.open,
  (v) => {
    lockBodyScroll(!!v);
    if (!v) {
      // 关闭时清理本弹窗状态，避免下次打开出现旧内容
      resetAll();
    }
  },
  { immediate: true },
);

const isEditMode = computed(() => props.mode === "edit");

function closeModal() {
  emit("update:open", false);
  emit("close");
  resetAll();
}

const step = ref<Step>(1);

// 全局状态
const draftKey = ref<string>("");

const BACKEND_ORIGIN = (import.meta as any).env?.VITE_BACKEND_ORIGIN || "http://127.0.0.1:8000";

const loading = reactive({
  init: false,
  upload: false,
  estimate: false,
  publish: false,
});

const errorMsg = ref<string>("");

type MarketCategory = { id: number; name: string };
type MarketBrand = { id: number; name: string; category_id: number };
type MarketDeviceModel = { id: number; name: string; brand_id: number };

const categoryOptions = ref<MarketCategory[]>([]);
const brandOptions = ref<MarketBrand[]>([]);
const modelOptions = ref<MarketDeviceModel[]>([]);
const loadingOptions = reactive({ category: false, brand: false, model: false });

async function fetchCategories() {
  loadingOptions.category = true;
  try {
    const { data } = await http.get("/api/market/categories/");
    categoryOptions.value = Array.isArray(data) ? data : (data?.results ?? []);
  } finally {
    loadingOptions.category = false;
  }
}

async function fetchBrandsByCategory(categoryId: number) {
  loadingOptions.brand = true;
  try {
    const { data } = await http.get("/api/market/brands/", { params: { category_id: categoryId } });
    const arr = Array.isArray(data) ? data : (data?.results ?? []);
    const list = Array.isArray(arr) ? arr : [];

    // 后端已按 category_id 过滤时，品牌项通常不会携带 category_id 字段。
    // 只有在确实存在 category_id 字段时才做前端兜底过滤，否则直接使用返回列表。
    const hasCategoryField = list.some((b) => (b as any).category_id !== undefined && (b as any).category_id !== null);
    if (hasCategoryField) {
      brandOptions.value = list.filter((b) => Number((b as any).category_id) === categoryId);
    } else {
      brandOptions.value = list;
    }
  } catch {
    brandOptions.value = [];
  } finally {
    loadingOptions.brand = false;
  }
}

async function fetchModelsByBrand(categoryId: number, brandId: number) {
  loadingOptions.model = true;
  try {
    const { data } = await http.get("/api/market/device-models/", {
      params: { category_id: categoryId, brand_id: brandId },
    });

    const arr = Array.isArray(data) ? data : (data?.results ?? []);
    const list = Array.isArray(arr) ? arr : [];

    // 永远前端兜底过滤，避免串数据（即使后端已过滤）
    modelOptions.value = list.filter((m) => {
      const bid = Number((m as any).brand_id);
      const cid = Number((m as any).category_id);
      // 后端若不返回 category_id，则只按 brand_id 过滤
      if (Number.isFinite(cid) && cid > 0) {
        return bid === brandId && cid === categoryId;
      }
      return bid === brandId;
    });
  } catch {
    modelOptions.value = [];
  } finally {
    loadingOptions.model = false;
  }
}

onMounted(() => {
  fetchCategories();
});

// Step1 表单
const form1 = reactive({
  category_id: "" as string | number,
  brand_id: "" as string | number,
  device_model_id: "" as string | number,
  years_used: "" as string | number,
  original_price: "" as string | number,
});

const isPrefilling = ref(false);

watch(
  () => form1.category_id,
  (v) => {
    if (isPrefilling.value) return;
    const cid = Number(String(v).trim());
    // reset dependent fields/options
    form1.brand_id = "";
    form1.device_model_id = "";
    brandOptions.value = [];
    modelOptions.value = [];
    if (Number.isFinite(cid) && cid > 0) fetchBrandsByCategory(cid);
  },
);

watch(
  () => form1.brand_id,
  (v) => {
    if (isPrefilling.value) return;
    const bid = Number(String(v).trim());
    form1.device_model_id = "";
    modelOptions.value = [];
    const cid = Number(String(form1.category_id).trim());
    if (Number.isFinite(cid) && cid > 0 && Number.isFinite(bid) && bid > 0) {
      fetchModelsByBrand(cid, bid);
    }
  },
);

// 图片
const files = ref<File[]>([]);
const previews = ref<string[]>([]);
const existingImages = ref<string[]>([]);
const useExistingImages = ref(true);

function toAbsUrl(raw: string) {
  const s = String(raw ?? "").trim();
  if (!s) return "";
  if (/^(https?:)?\/\//.test(s) || s.startsWith("blob:") || s.startsWith("data:")) return s;
  const path = s.startsWith("/") ? s : `/${s}`;
  if (path.startsWith("/media/")) return `${BACKEND_ORIGIN}${path}`;
  return path;
}

// Step3 发布表单
const form3 = reactive({
  title: "",
  school: "",
  product_summary: "",
  description: "",
  selling_price: "" as string | number,
});

// 发布结果（估价区间、对比等）
const publishRes = ref<PublishResult | null>(null);

// Step3 估价结果（来自 /estimate/）
const estimateRes = ref<Record<string, any> | null>(null);

const defaultTitle = computed(() => {
  const cid = Number(String(form1.category_id).trim());
  const mid = Number(String(form1.device_model_id).trim());

  const cname = categoryOptions.value.find((c) => c.id === cid)?.name || (cid ? `类目#${cid}` : "");
  const mname = modelOptions.value.find((m) => m.id === mid)?.name || (mid ? `型号#${mid}` : "");
  const parts = [cname, mname].filter((x) => String(x).trim().length > 0);
  return parts.join(" ").trim();
});

// ---------- utils ----------
function setError(msg: string) {
  errorMsg.value = msg;
}

function clearError() {
  errorMsg.value = "";
}

function toNumber(val: string | number) {
  const n = Number(val);
  return Number.isFinite(n) ? n : NaN;
}

function isJpgOrPng(file: File) {
  const t = file.type?.toLowerCase();
  return t === "image/jpeg" || t === "image/jpg" || t === "image/png";
}

function revokePreviews() {
  previews.value.forEach((u) => URL.revokeObjectURL(u));
  previews.value = [];
}

watch(files, () => {
  revokePreviews();
  previews.value = files.value.map((f) => URL.createObjectURL(f));
});

watch(useExistingImages, (v) => {
  if (v) {
    files.value = [];
    revokePreviews();
  }
});

function resetAll() {
  clearError();
  step.value = 1;
  draftKey.value = "";
  isPrefilling.value = false;
  existingImages.value = [];
  useExistingImages.value = true;

  // reset step1
  form1.category_id = "";
  form1.brand_id = "";
  form1.device_model_id = "";
  form1.years_used = "";
  form1.original_price = "";

  brandOptions.value = [];
  modelOptions.value = [];

  // reset images
  files.value = [];
  revokePreviews();

  // reset step2/3
  form3.title = "";
  form3.product_summary = "";
  form3.description = "";
  form3.selling_price = "";
  publishRes.value = null;
  estimateRes.value = null;
}

async function initEditDraft() {
  clearError();
  publishRes.value = null;
  estimateRes.value = null;

  const pid = props.productId != null ? Number(props.productId) : NaN;
  if (!Number.isFinite(pid) || pid <= 0) {
    setError("productId 缺失或不合法");
    return;
  }

  loading.init = true;
  try {
    const res = await initDraftFromProduct(pid);
    const key = (res as any)?.draft_key || (res as any)?.draftKey || (res as any)?.key;
    if (!key) throw new Error("后端未返回 draft_key");
    draftKey.value = String(key);

    const meta: any = (res as any)?.meta ?? {};
    const product: any = (res as any)?.product ?? {};
    const images: any = (res as any)?.images ?? [];
    existingImages.value = Array.isArray(images) ? images.map((x) => String(x ?? "")).filter((x) => x.trim()) : [];
    useExistingImages.value = existingImages.value.length > 0;

    const categoryId = Number(meta?.category_id);
    const modelId = Number(meta?.device_model_id);
    const yearsUsed = meta?.years_used ?? "";
    const originalPrice = meta?.original_price ?? "";

    isPrefilling.value = true;
    form1.category_id = Number.isFinite(categoryId) ? categoryId : "";
    form1.brand_id = "";
    form1.device_model_id = "";
    form1.years_used = yearsUsed ?? "";
    form1.original_price = originalPrice ?? "";
    isPrefilling.value = false;

    if (Number.isFinite(categoryId) && categoryId > 0) {
      await fetchBrandsByCategory(categoryId);
    }

    let brandId: number | null = null;
    if (Number.isFinite(categoryId) && categoryId > 0 && Number.isFinite(modelId) && modelId > 0) {
      const refModel = await getDeviceModelReference({ category_id: categoryId, device_model_id: modelId });
      const bid = Number((refModel as any)?.brand_id);
      brandId = Number.isFinite(bid) && bid > 0 ? bid : null;
    }

    if (Number.isFinite(categoryId) && categoryId > 0 && brandId != null) {
      isPrefilling.value = true;
      form1.brand_id = brandId;
      isPrefilling.value = false;
      await fetchModelsByBrand(categoryId, brandId);
    }

    if (Number.isFinite(modelId) && modelId > 0) {
      isPrefilling.value = true;
      form1.device_model_id = modelId;
      isPrefilling.value = false;
    }

    form3.title = String(product?.title ?? "");
    form3.school = String(product?.school ?? "");
    form3.product_summary = String(product?.product_summary ?? "");
    form3.description = String(product?.description ?? "");
    form3.selling_price = product?.selling_price ?? "";

    step.value = 1;
  } catch (e: any) {
    setError(e?.message || e?.response?.data?.detail || "初始化编辑失败");
  } finally {
    loading.init = false;
  }
}

watch(
  () => [props.open, props.mode, props.productId] as const,
  ([open, mode]) => {
    if (open && mode === "edit") {
      resetAll();
      initEditDraft();
    }
  },
  { immediate: true },
);

async function handleEstimate() {
  clearError();
  estimateRes.value = null;

  if (!draftKey.value) return setError("draft_key 缺失");

  const categoryId = Number(String(form1.category_id).trim());
  const yearsUsed = toNumber(form1.years_used);
  const originalPrice = toNumber(form1.original_price);

  if (!Number.isFinite(categoryId) || categoryId <= 0) return setError("category_id 缺失或不合法");
  if (!Number.isFinite(yearsUsed) || yearsUsed < 0) return setError("years_used 缺失或不合法");
  if (!Number.isFinite(originalPrice) || originalPrice <= 0) return setError("original_price 缺失或不合法");

  loading.estimate = true;
  try {
    const res = await estimateDraft(draftKey.value, {
      category_id: categoryId,
      years_used: yearsUsed,
      original_price: originalPrice,
    });
    estimateRes.value = res ?? {};
  } catch (e: any) {
    setError(e?.message || "估价失败");
  } finally {
    loading.estimate = false;
  }
}

// ---------- computed ----------
const isBusy = computed(() => {
  return loading.init || loading.upload || loading.estimate || loading.publish;
});

// ---------- actions ----------


function handlePickImages(ev: Event) {
  clearError();
  const input = ev.target as HTMLInputElement;
  const list = input.files ? Array.from(input.files) : [];
  input.value = ""; // 允许重复选择同一文件

  if (!list.length) return;

  if (isEditMode.value) {
    useExistingImages.value = false;
  }

  // 合并 + 限制最多4张
  const merged = [...files.value, ...list].slice(0, 4);

  // 类型校验
  for (const f of merged) {
    if (!isJpgOrPng(f)) {
      setError("仅支持 jpg/png 图片");
      return;
    }
  }

  files.value = merged;
}

function removeImage(idx: number) {
  clearError();
  const next = files.value.slice();
  next.splice(idx, 1);
  files.value = next;
}

async function handleNextFromStep1() {
  clearError();
  publishRes.value = null;

  const categoryId = Number(String(form1.category_id).trim());
  const brandId = Number(String(form1.brand_id).trim());
  const modelId = Number(String(form1.device_model_id).trim());
  const yearsUsed = toNumber(form1.years_used);
  const originalPrice = toNumber(form1.original_price);

  if (!Number.isFinite(categoryId) || categoryId <= 0) return setError("请选择 category_id");
  if (!Number.isFinite(brandId) || brandId <= 0) return setError("请选择 brand");
  if (!Number.isFinite(modelId) || modelId <= 0) return setError("请选择 device_model_id");
  if (!Number.isFinite(yearsUsed) || yearsUsed < 0) return setError("years_used 必须是 ≥0 的数字");
  if (!Number.isFinite(originalPrice) || originalPrice <= 0) return setError("original_price 必须是 >0 的数字");

  if (isEditMode.value && useExistingImages.value) {
    if (!draftKey.value) return setError("draft_key 缺失");
    step.value = 3;
    if (!form3.title.trim()) {
      form3.title = defaultTitle.value;
    }
    await handleEstimate();
    return;
  }

  if (files.value.length < 1) return setError("请至少选择 1 张图片（第 1 张为主图）");
  if (files.value.length > 4) return setError("最多选择 4 张图片");
  for (const f of files.value) {
    if (!isJpgOrPng(f)) return setError("仅支持 jpg/png 图片");
  }

  // 1) 创建草稿
  loading.init = true;
  try {
    const res = await initDraft({
      category_id: categoryId,
      device_model_id: modelId,
      years_used: yearsUsed,
      original_price: originalPrice,
    });

    const anyRes: any = res as any;
    const key = anyRes?.draft_key || anyRes?.key || anyRes?.draftKey;
    if (!key) throw new Error("后端未返回 draft_key");
    draftKey.value = String(key);
  } catch (e: any) {
    setError(e?.response?.data?.detail || e?.message || "创建草稿失败");
    return;
  } finally {
    loading.init = false;
  }

  // 2) 上传图片
  loading.upload = true;
  try {
    await uploadDraftImages(draftKey.value, files.value);
  } catch (e: any) {
    setError(e?.response?.data?.detail || e?.message || "上传图片失败");
    return;
  } finally {
    loading.upload = false;
  }

  step.value = 3;

  if (!form3.title.trim()) {
    form3.title = defaultTitle.value;
  }

  await handleEstimate();
}

async function handlePublish() {
  clearError();
  publishRes.value = null;

  if (!draftKey.value) return setError("draft_key 缺失");

  const title = form3.title.trim();
  const school = form3.school.trim();
  const productSummary = form3.product_summary.trim();
  const description = form3.description.trim();
  const sellingPrice = toNumber(form3.selling_price);

  if (!title) return setError("请填写标题");
  if (!productSummary) return setError("请填写商品摘要");
  if (!description) return setError("请填写描述");
  if (!Number.isFinite(sellingPrice) || sellingPrice <= 0) return setError("selling_price 必须是 >0 的数字");

  // publish 需要携带 step1 + step3 的关键信息（按你后端接口约定）
  const categoryId = Number(String(form1.category_id).trim());
  const modelId = Number(String(form1.device_model_id).trim());
  const yearsUsed = toNumber(form1.years_used);
  const originalPrice = toNumber(form1.original_price);

  if (!Number.isFinite(categoryId) || categoryId <= 0) return setError("category_id 缺失或不合法");
  if (!Number.isFinite(modelId) || modelId <= 0) return setError("device_model_id 缺失或不合法");
  if (!Number.isFinite(yearsUsed) || yearsUsed < 0) return setError("years_used 缺失或不合法");
  if (!Number.isFinite(originalPrice) || originalPrice <= 0) return setError("original_price 缺失或不合法");

  loading.publish = true;
  try {
    const reqBody = {
      category_id: categoryId,
      device_model_id: modelId,
      years_used: yearsUsed,
      original_price: originalPrice,
      title,
      school,
      product_summary: productSummary,
      description,
      selling_price: sellingPrice,
    };

    const res = isEditMode.value
      ? await publishDraftUpdateProduct(draftKey.value, Number(props.productId), reqBody)
      : await publishDraft(draftKey.value, reqBody);

    publishRes.value = res ?? {};
    emit("success", publishRes.value);
    closeModal();
    // 可选：发布成功后你要不要跳转市场大厅，这里不擅自加新流程，保持停留并展示结果
  } catch (e: any) {
    setError(e?.message || "发布失败");
  } finally {
    loading.publish = false;
  }
}

function backToStep(n: Step) {
  clearError();
  // 不引入新流程：只允许“向前完成、向后修改”
  // 但是如果回到 Step1 修改，会导致 draft 已存在，这里允许继续使用同一个 draftKey
  step.value = n;
}

// 释放预览 URL
watch(
  () => previews.value,
  () => {},
);


</script>

<template>
  <div v-if="props.open" class="fixed inset-0 z-50">
    <!-- backdrop -->
    <div class="absolute inset-0 bg-black/50" @click="closeModal"></div>

    <!-- modal panel -->
    <div class="absolute inset-0 flex items-center justify-center p-4">
      <div class="w-full max-w-3xl rounded-2xl bg-white shadow-xl overflow-hidden max-h-[calc(100vh-2rem)] flex flex-col">
        <div class="flex items-center justify-between px-5 py-4 border-b">
          <div class="text-base font-semibold">{{ isEditMode ? "编辑商品" : "商品上架" }}</div>
          <button type="button" class="text-sm text-gray-600 hover:text-gray-900" @click="closeModal">关闭</button>
        </div>

        <div class="px-4 py-6 overflow-y-auto">
    <div class="flex items-center justify-between mb-6">
      <h1 class="text-xl font-semibold">{{ isEditMode ? "编辑商品" : "商品上架" }}</h1>
      <div class="text-sm text-gray-500">Step {{ step }}/3</div>
    </div>

    <!-- error -->
    <div v-if="errorMsg" class="mb-4 rounded-lg border border-red-200 bg-red-50 p-3 text-sm text-red-700">
      {{ errorMsg }}
    </div>

    <!-- Stepper -->
    <div class="mb-6 grid grid-cols-2 gap-2">
      <div class="rounded-lg p-3 text-center" :class="step === 1 ? 'bg-gray-900 text-white' : 'bg-gray-100 text-gray-700'">
        1. 图片上传
      </div>
      <div class="rounded-lg p-3 text-center" :class="step === 3 ? 'bg-gray-900 text-white' : 'bg-gray-100 text-gray-700'">
        2. {{ isEditMode ? "提交审核" : "发布" }}
      </div>
    </div>

    <!-- Step1 -->
    <!-- Step1 -->
<div v-if="step === 1" class="space-y-5">
  <div class="grid grid-cols-1 md:grid-cols-2 gap-5">
    <!-- 左：基本信息 -->
    <div class="rounded-xl border bg-white p-5">
      <div class="flex items-center justify-between mb-4">
        <div class="font-medium">商品信息</div>
        <div class="text-xs text-gray-500" v-if="draftKey">draft_key: <span class="font-mono">{{ draftKey }}</span></div>
      </div>

      <div class="grid grid-cols-1 gap-4">
        <div>
          <label class="block text-sm text-gray-600 mb-1">类别</label>
          <select
            v-model="form1.category_id"
            class="w-full rounded-lg border px-3 py-2 outline-none focus:ring bg-white"
            :disabled="loadingOptions.category || isBusy"
          >
            <option value="" disabled>请选择类目</option>
            <option v-for="c in categoryOptions" :key="c.id" :value="c.id">{{ c.name }}</option>
          </select>
        </div>

        <div>
          <label class="block text-sm text-gray-600 mb-1">品牌</label>
          <select
            v-model="form1.brand_id"
            class="w-full rounded-lg border px-3 py-2 outline-none focus:ring bg-white"
            :disabled="loadingOptions.brand || !form1.category_id || isBusy"
          >
            <option value="" disabled>请选择品牌</option>
            <option v-for="b in brandOptions" :key="b.id" :value="b.id">{{ b.name }}</option>
          </select>
          <div class="mt-1 text-xs text-gray-500" v-if="!form1.category_id">请先选择类目</div>
        </div>

        <div>
          <label class="block text-sm text-gray-600 mb-1">型号</label>
          <select
            v-model="form1.device_model_id"
            class="w-full rounded-lg border px-3 py-2 outline-none focus:ring bg-white"
            :disabled="loadingOptions.model || !form1.category_id || !form1.brand_id || isBusy"
          >
            <option value="" disabled>请选择型号</option>
            <option v-for="m in modelOptions" :key="m.id" :value="m.id">{{ m.name }}</option>
          </select>
          <div class="mt-1 text-xs text-gray-500" v-if="form1.category_id && !form1.brand_id">请先选择品牌</div>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label class="block text-sm text-gray-600 mb-1">使用年限</label>
            <input
              v-model="form1.years_used"
              class="w-full rounded-lg border px-3 py-2 outline-none focus:ring"
              placeholder="例如 1.5"
              :disabled="isBusy"
            />
          </div>
          <div>
            <label class="block text-sm text-gray-600 mb-1">购买价格</label>
            <input
              v-model="form1.original_price"
              class="w-full rounded-lg border px-3 py-2 outline-none focus:ring"
              placeholder="例如 3999"
              :disabled="isBusy"
            />
          </div>
        </div>
      </div>
    </div>

    <!-- 右：图片 -->
    <div class="rounded-xl border bg-white p-5">
      <div class="font-medium mb-2">图片（最多4张，jpg/png；第1张为主图）</div>
      <div class="text-sm text-gray-500 mb-4">如需更换主图，请删除后重新选择，确保主图在第一位。</div>

      <div class="rounded-lg border border-dashed p-4 flex items-center justify-between gap-3">
        <div class="text-sm text-gray-600">
          <span v-if="isEditMode && useExistingImages">沿用原图 {{ existingImages.length }}/4</span>
          <span v-else>已选择 {{ files.length }}/4</span>
        </div>

        <label class="inline-flex items-center justify-center rounded-lg bg-gray-900 text-white px-4 py-2 cursor-pointer">
          <span>选择图片</span>
          <input
            type="file"
            accept="image/png,image/jpeg"
            multiple
            class="hidden"
            @change="handlePickImages"
            :disabled="isBusy"
          />
        </label>
      </div>

      <div v-if="isEditMode && existingImages.length" class="mt-3 flex items-center gap-2">
        <input type="checkbox" v-model="useExistingImages" :disabled="isBusy" />
        <div class="text-sm text-gray-700">沿用原商品图片（如需更换，请取消勾选后重新选择）</div>
      </div>

      <div v-if="previews.length" class="mt-4 grid grid-cols-2 md:grid-cols-4 gap-3">
        <div v-for="(url, idx) in previews" :key="url" class="relative rounded-lg border overflow-hidden">
          <img :src="url" class="w-full h-28 object-cover" />
          <div class="absolute left-2 top-2 text-xs px-2 py-1 rounded bg-black/70 text-white">
            {{ idx === 0 ? "主图" : `图${idx + 1}` }}
          </div>
          <button
            class="absolute right-2 top-2 text-xs px-2 py-1 rounded bg-white/90 border"
            @click="removeImage(idx)"
            :disabled="isBusy"
            type="button"
          >
            删除
          </button>
        </div>
      </div>

      <div
        v-else-if="isEditMode && useExistingImages && existingImages.length"
        class="mt-4 grid grid-cols-2 md:grid-cols-4 gap-3"
      >
        <div v-for="(url, idx) in existingImages" :key="url + ':' + idx" class="relative rounded-lg border overflow-hidden">
          <img :src="toAbsUrl(url)" class="w-full h-28 object-cover" />
          <div class="absolute left-2 top-2 text-xs px-2 py-1 rounded bg-black/70 text-white">
            {{ idx === 0 ? "主图" : `图${idx + 1}` }}
          </div>
        </div>
      </div>
    </div>
  </div>

  <div class="flex justify-end">
    <button
      class="rounded-lg bg-gray-900 text-white px-6 py-2 disabled:opacity-50"
      @click="handleNextFromStep1"
      :disabled="isBusy"
    >
      {{ loading.init || loading.upload ? "处理中..." : "下一步" }}
    </button>
  </div>
</div>

    <!-- Step3 -->
    <div v-else-if="step === 3" class="space-y-5">
      <div class="rounded-xl border bg-white p-5">
        <div class="flex items-center justify-between mb-4">
          <div class="font-medium">{{ isEditMode ? "提交审核" : "发布商品" }}</div>
          <button class="text-sm text-gray-600 underline" @click="backToStep(1)" :disabled="isBusy" type="button">
            返回修改图片
          </button>
        </div>

        <div class="grid grid-cols-1 gap-4">
          <div>
            <label class="block text-sm text-gray-600 mb-1">标题 title</label>
            <input
              v-model="form3.title"
              class="w-full rounded-lg border px-3 py-2 outline-none focus:ring"
              placeholder="例如：iPad Air 64G 9成新"
              :disabled="isBusy"
            />
          </div>
          <div>
            <label class="block text-sm text-gray-600 mb-1">商品摘要</label>
            <textarea
              v-model="form3.product_summary"
              class="w-full rounded-lg border px-3 py-2 outline-none focus:ring min-h-[90px]"
              placeholder="简要概括商品亮点、成色、配件、瑕疵等"
              :disabled="isBusy"
            />
          </div>
          <div>
            <label class="block text-sm text-gray-600 mb-1">学校/小区 school</label>
            <input
              v-model="form3.school"
              class="w-full rounded-lg border px-3 py-2 outline-none focus:ring"
              placeholder="例如：xx大学/xx小区"
              :disabled="isBusy"
            />
          </div>
          <div>
            <label class="block text-sm text-gray-600 mb-1">描述 description</label>
            <textarea
              v-model="form3.description"
              class="w-full rounded-lg border px-3 py-2 outline-none focus:ring min-h-[120px]"
              placeholder="请描述使用情况、配件、瑕疵等"
              :disabled="isBusy"
            />
          </div>
          <div>
            <label class="block text-sm text-gray-600 mb-1 flex items-center justify-between">
              <span>售价 selling_price</span>
            </label>
            <input
              v-model="form3.selling_price"
              class="w-full rounded-lg border px-3 py-2 outline-none focus:ring"
              placeholder="例如：1999"
              :disabled="isBusy"
            />
          </div>
        </div>

        <div class="mt-5 flex gap-3">
          <button
            class="rounded-lg border px-4 py-2 text-gray-700 disabled:opacity-50"
            @click="handleEstimate"
            :disabled="isBusy"
            type="button"
          >
            {{ loading.estimate ? "估价中..." : "重新估价" }}
          </button>

          <button
            class="rounded-lg bg-gray-900 text-white px-4 py-2 disabled:opacity-50"
            @click="handlePublish"
            :disabled="isBusy"
          >
            {{ loading.publish ? (isEditMode ? "提交中..." : "发布中...") : (isEditMode ? "提交审核" : "确认发布") }}
          </button>
        </div>


      </div>
    </div>
        </div>
      </div>
    </div>
  </div>
</template>
