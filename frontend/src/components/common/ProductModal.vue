<template>
  <div
    class="fixed inset-0 bg-black/50 z-50 flex items-center justify-center transition-all duration-300"
    :class="open ? 'opacity-100 visible' : 'opacity-0 invisible'"
    @click.self="open = false"
  >
    <div class="flex items-stretch gap-4">
      <!-- 详情页 -->
      <div class="bg-white rounded-xl shadow-xl w-full max-w-4xl max-h-[90vh] overflow-hidden flex flex-col transform transition-all duration-300"
           :class="open ? 'scale-100' : 'scale-95'">
      <div class="p-6 border-b border-light-2 flex items-center justify-between">
        <h3 class="text-xl font-bold text-dark">商品详情</h3>
        <button class="text-dark-2 hover:text-dark transition-colors" @click="open = false">
          <i class="fas fa-times text-xl"></i>
        </button>
      </div>

      <div class="flex-1 overflow-y-auto p-6">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
          <!-- Left: Images -->
          <div>
            <div class="w-full aspect-square bg-light-3 rounded-xl overflow-hidden flex items-center justify-center">
              <img
                v-if="activeImage"
                :src="activeImage"
                class="w-full h-full object-cover"
                alt="product"
              />
              <div v-else class="text-sm text-dark-2">暂无图片</div>
            </div>

            <div v-if="images.length" class="mt-4 grid grid-cols-4 gap-3">
              <button
                v-for="(img, idx) in images.slice(0, 4)"
                :key="idx"
                class="rounded-lg overflow-hidden border transition-all"
                :class="idx === activeImgIndex ? 'border-primary ring-2 ring-primary/30' : 'border-light-2 hover:border-primary/50'"
                type="button"
                @click="activeImgIndex = idx"
              >
                <img :src="img" class="w-full h-16 object-cover" alt="thumb" />
              </button>
            </div>
          </div>

          <!-- Right: Vertical info list -->
          <div class="space-y-4">
            <div>
              <div class="text-xl font-bold text-dark">{{ product?.title ?? '未选择商品' }}</div>
              <div class="mt-1 text-sm text-dark-2">
                {{ product?.created_at ? `上架时间：${product.created_at}` : '' }}
              </div>
              <div v-if="detailLoading" class="mt-2 text-xs text-dark-2">正在加载商品详情…</div>
              <div v-else-if="detailError" class="mt-2 text-xs text-danger">{{ detailError }}</div>
            </div>

            <div class="rounded-xl border border-light-2 bg-white p-4">
              <div class="flex items-end justify-between">
                <div>
                  <div class="text-sm text-dark-2">售价</div>
                  <div class="text-2xl font-bold text-dark">{{ product?.selling_price ?? '-' }}</div>
                </div>
                <div class="text-right">
                  <div class="text-sm text-dark-2">估价区间</div>
                  <div class="text-sm font-medium text-dark">
                    {{ (product?.estimated_min && product?.estimated_max) ? `${product.estimated_min} ~ ${product.estimated_max}` : '-' }}
                  </div>
                </div>
              </div>

              <div v-if="product?.school" class="mt-2 text-sm text-dark-2">
                学校/小区：<span class="text-dark">{{ product.school }}</span>
              </div>

              <div v-if="product?.market_tag" class="mt-3 inline-flex items-center px-3 py-1 rounded-full bg-primary/10 text-primary text-sm">
                {{ product.market_tag }}
              </div>
            </div>

            <div class="rounded-xl border border-light-2 bg-white p-4">
              <div class="flex items-center justify-between mb-3">
                <div class="text-sm font-semibold text-dark">商品信息</div>
              </div>
              <div class="space-y-2 text-sm">
                <div class="flex items-center justify-between">
                  <div class="text-dark-2">类别</div>
                  <div class="text-dark">{{ displayCategoryName }}</div>
                </div>
                <div class="grid grid-cols-[48px_1fr] items-start gap-3">
                  <div class="text-dark-2 whitespace-nowrap">型号</div>
                  <div class="text-dark text-right break-words">{{ displayDeviceModelName }}</div>
                </div>
                <div class="flex items-center justify-between">
                  <div class="text-dark-2">成色</div>
                  <div class="text-dark">{{ product?.grade_label ?? '-' }}</div>
                </div>
                <div class="flex items-center justify-between">
                  <div class="text-dark-2">使用年限</div>
                  <div class="text-dark">{{ product?.years_used ?? '-' }}</div>
                </div>
              </div>
            </div>

            <div class="rounded-xl border border-light-2 bg-white p-4">
              <div class="text-sm font-semibold text-dark mb-3">卖家信息</div>
              <div class="space-y-2 text-sm">
                <div class="flex items-center justify-between">
                  <div class="text-dark-2">卖家</div>
                  <div class="text-dark">{{ product?.seller_name ?? product?.seller?.nickname ?? '—' }}</div>
                </div>
                <div class="flex items-center justify-between">
                  <div class="text-dark-2">发货地址</div>
                  <div class="text-dark">{{ product?.seller_address ?? '待下单后展示' }}</div>
                </div>
              </div>
            </div>

            <div class="rounded-xl border border-light-2 bg-white p-4">
              <div class="text-sm font-semibold text-dark mb-2">商品摘要</div>
              <div class="text-sm text-dark-2 whitespace-pre-wrap">
                {{ product?.product_summary ?? '—' }}
              </div>
            </div>

            <div class="rounded-xl border border-light-2 bg-white p-4">
              <div class="text-sm font-semibold text-dark mb-2">交易说明</div>
              <ul class="text-sm text-dark-2 space-y-1 list-disc list-inside">
                <li>下单后需完成付款，订单才会进入发货流程</li>
                <li>卖家发货后，买家确认收货即完成交易</li>
                <li>若商品与描述不符，可申请退货退款</li>
              </ul>
            </div>

            <div class="rounded-xl border border-light-2 bg-white p-4">
              <div class="text-sm font-semibold text-dark mb-2">描述</div>
              <div class="text-sm text-dark-2 whitespace-pre-wrap">
                {{ product?.description ?? '—' }}
              </div>
            </div>

            <div class="rounded-xl border border-light-2 bg-white p-4">
              <div class="flex items-center justify-between mb-2">
                <div class="text-sm font-semibold text-dark">留言沟通</div>
                <button
                  type="button"
                  class="px-2 py-1 rounded bg-primary/10 text-primary text-xs font-medium hover:bg-primary/20 transition disabled:opacity-50 disabled:cursor-not-allowed"
                  :disabled="!product?.id || commentsLoading"
                  @click="reloadComments"
                >
                  刷新
                </button>
              </div>

              <div v-if="commentsLoading" class="text-xs text-dark-2">正在加载留言…</div>
              <div v-else-if="commentsError" class="text-xs text-danger">{{ commentsError }}</div>
              <div v-else>
                <div v-if="!comments.length" class="text-sm text-dark-2">暂无留言，问问卖家吧</div>
                <div v-else class="mt-2 space-y-2 max-h-56 overflow-y-auto pr-1">
                  <div
                    v-for="c in comments"
                    :key="c.id"
                    class="flex"
                    :class="c.sender_id === me?.id ? 'justify-end' : 'justify-start'"
                  >
                    <div class="max-w-[85%]">
                      <div class="text-[11px] text-dark-2 mb-1" :class="c.sender_id === me?.id ? 'text-right' : 'text-left'">
                        {{ c.sender_name }} · {{ c.created_at }}
                      </div>
                      <div
                        class="rounded-lg px-3 py-2 text-sm whitespace-pre-wrap break-words"
                        :class="c.sender_id === me?.id ? 'bg-primary/10 text-dark' : 'bg-light-3 text-dark'"
                      >
                        {{ c.content }}
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <div class="mt-3 flex items-end gap-2">
                <textarea
                  v-model="commentDraft"
                  class="flex-1 min-h-[38px] max-h-28 rounded-lg border border-light-2 px-3 py-2 text-sm text-dark placeholder:text-dark-2 focus:outline-none focus:ring-2 focus:ring-primary/30"
                  rows="2"
                  :disabled="!product?.id || sendingComment"
                  :placeholder="isSeller ? '回复买家的问题…' : '给卖家留言质询…'"
                ></textarea>
                <button
                  type="button"
                  class="px-4 py-2 rounded-lg bg-primary text-white text-sm font-medium hover:bg-primary/90 disabled:opacity-50 disabled:cursor-not-allowed"
                  :disabled="!product?.id || sendingComment || !commentDraft.trim()"
                  @click="sendComment"
                >
                  {{ sendingComment ? '发送中…' : '发送' }}
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="p-6 border-t border-light-2 bg-light-3/50">
        <div class="flex items-center justify-end">
          <div v-if="isSeller" class="text-sm text-dark-2">
            这是你发布的商品
          </div>
          <div v-else class="flex flex-col items-end gap-1">
            <div v-if="String(product?.status ?? '') === 'pending_review'" class="text-xs text-warning">
              商品待审核，暂不可购买
            </div>
            <div v-else-if="String(product?.status ?? '') === 'rejected'" class="text-xs text-danger max-w-[340px] text-right">
              商品审核未通过{{ product?.review_reject_reason ? `：${product.review_reject_reason}` : '' }}
            </div>
            <button
              class="px-8 py-3 bg-primary text-white rounded-lg hover:bg-primary/90 transition-colors shadow-sm hover:shadow disabled:opacity-50 disabled:cursor-not-allowed"
              :disabled="!product?.id || buying || String(product?.status ?? '') === 'pending_review' || String(product?.status ?? '') === 'rejected'"
              @click="openBuyConfirm"
            >
              {{ buying ? '处理中…' : '立即购买' }}
            </button>
          </div>
        </div>
      </div>
      </div>

    </div>

  <!-- 购买确认弹窗 -->
  <div v-if="buyConfirmOpen" class="fixed inset-0 z-[60] flex items-center justify-center">
    <div class="absolute inset-0 bg-black/40" @click="closeBuyConfirm"></div>
    <div class="relative w-[92vw] max-w-md bg-white rounded-xl shadow-lg border border-neutral-200 p-6">
      <h3 class="text-lg font-semibold text-neutral-800">是否确认购买该商品？</h3>
      <p class="mt-2 text-neutral-600">
        {{ productTitleText }}
      </p>
      <p class="mt-1 text-sm text-neutral-500">
        价格：¥{{ productPriceText }}
      </p>

      <div class="mt-6 flex justify-end gap-3">
        <button type="button" class="px-4 py-2 rounded-lg border border-neutral-300 text-neutral-700 hover:bg-neutral-50" @click="closeBuyConfirm">
          取消
        </button>
        <button type="button" class="px-4 py-2 rounded-lg bg-primary text-white hover:bg-primary/90 disabled:opacity-50 disabled:cursor-not-allowed" :disabled="buying" @click="confirmBuy">
          确认
        </button>
      </div>
    </div>
  </div>

  <!-- 跳转提示弹窗 -->
  <div v-if="jumpingOpen" class="fixed inset-0 z-[70] flex items-center justify-center">
    <div class="absolute inset-0 bg-black/40"></div>
    <div class="relative w-[92vw] max-w-md bg-white rounded-xl shadow-lg border border-neutral-200 p-6">
      <h3 class="text-lg font-semibold text-neutral-800">正在跳转…</h3>
      <p class="mt-2 text-neutral-600">
        即将前往订单中心开始付款
      </p>
      <p class="mt-1 text-sm text-neutral-500">
        {{ jumpCountdown }} 秒后自动跳转
      </p>

      <div class="mt-6 flex justify-end">
        <button type="button" class="px-4 py-2 rounded-lg border border-neutral-300 text-neutral-700 hover:bg-neutral-50" @click="cancelJump">
          取消跳转
        </button>
      </div>
    </div>
  </div>

  <!-- 购买失败提示弹窗 -->
  <div v-if="buyErrorOpen" class="fixed inset-0 z-[80] flex items-center justify-center">
    <div class="absolute inset-0 bg-black/40" @click="closeBuyError"></div>
    <div class="relative w-[92vw] max-w-md bg-white rounded-xl shadow-lg border border-neutral-200 p-6">
      <h3 class="text-lg font-semibold text-neutral-800">购买失败</h3>
      <p class="mt-2 text-neutral-600 break-all">{{ buyErrorMsg }}</p>
      <div class="mt-6 flex justify-end">
        <button type="button" class="px-4 py-2 rounded-lg bg-primary text-white hover:bg-primary/90" @click="closeBuyError">
          知道了
        </button>
      </div>
    </div>
  </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import http from '@/api/http'
import { getProductDetail } from '@/api/market'

const API_ORIGIN = (import.meta as any).env?.VITE_API_ORIGIN || 'http://127.0.0.1:8000'
const toAbsUrl = (u: string) => {
  if (!u) return ''
  if (u.startsWith('http://') || u.startsWith('https://')) return u
  // backend returns like /media/...
  if (u.startsWith('/')) return `${API_ORIGIN}${u}`
  return `${API_ORIGIN}/${u}`
}

const open = defineModel<boolean>('open', { required: true })
const props = defineProps<{ product: any | null }>()
const emit = defineEmits<{ (e: 'bought', payload: any): void }>()
const buying = ref(false)

const router = useRouter()

const buyConfirmOpen = ref(false)
const jumpingOpen = ref(false)
const jumpCountdown = ref(5)

const buyErrorOpen = ref(false)
const buyErrorMsg = ref('')

const openBuyError = (msg: string) => {
  buyErrorMsg.value = msg || '购买失败'
  buyErrorOpen.value = true
  document.body.style.overflow = 'hidden'
}

const closeBuyError = () => {
  buyErrorOpen.value = false
  buyErrorMsg.value = ''
  document.body.style.overflow = ''
  // Return to market hall by closing the product modal
  open.value = false
}

let jumpTimer: any = null
let jumpInterval: any = null

const productTitleText = computed(() => {
  const p: any = product.value
  return p?.title || p?.product_title || '未命名商品'
})

const productPriceText = computed(() => {
  const p: any = product.value
  return p?.selling_price || p?.product_selling_price || '-'
})

const openBuyConfirm = () => {
  if (!product.value?.id || buying.value || isSeller.value) return
  const st = product.value?.status != null ? String(product.value.status) : ''
  if (st && st !== 'on_sale') {
    const reason = product.value?.review_reject_reason != null ? String(product.value.review_reject_reason) : ''
    openBuyError(st === 'pending_review' ? '商品待审核，暂不可购买' : (st === 'rejected' ? `商品审核未通过${reason ? `：${reason}` : ''}，暂不可购买` : '商品当前不可购买'))
    return
  }
  buyConfirmOpen.value = true
  document.body.style.overflow = 'hidden'
}

const closeBuyConfirm = () => {
  buyConfirmOpen.value = false
  if (!jumpingOpen.value && !buyErrorOpen.value) document.body.style.overflow = ''
}

const cancelJump = () => {
  jumpingOpen.value = false
  jumpCountdown.value = 5
  if (jumpTimer) clearTimeout(jumpTimer)
  if (jumpInterval) clearInterval(jumpInterval)
  jumpTimer = null
  jumpInterval = null
  if (!buyErrorOpen.value) document.body.style.overflow = ''
}

const startJump = async (orderId: number | string) => {
  jumpingOpen.value = true
  jumpCountdown.value = 5

  // Let OrderCenterView optionally auto-open the detail modal
  try {
    sessionStorage.setItem('orderCenter.openOrderId', String(orderId))
  } catch {}

  if (jumpInterval) clearInterval(jumpInterval)
  jumpInterval = setInterval(() => {
    jumpCountdown.value = Math.max(0, jumpCountdown.value - 1)
  }, 1000)

  if (jumpTimer) clearTimeout(jumpTimer)
  jumpTimer = setTimeout(async () => {
    cancelJump()

    // Prefer existing route; fallback to a common path
    const routes = router.getRoutes().map(r => r.path)
    const targetPath = routes.includes('/orders')
      ? '/orders'
      : (routes.includes('/order-center') ? '/order-center' : '/orders')

    await router.push({ path: targetPath, query: { order_id: String(orderId) } })

    // Close product modal after navigation
    open.value = false
  }, 5000)
}

const confirmBuy = async () => {
  if (!product.value?.id || buying.value || isSeller.value) return
  const st = product.value?.status != null ? String(product.value.status) : ''
  if (st && st !== 'on_sale') {
    const reason = product.value?.review_reject_reason != null ? String(product.value.review_reject_reason) : ''
    openBuyError(st === 'pending_review' ? '商品待审核，暂不可购买' : (st === 'rejected' ? `商品审核未通过${reason ? `：${reason}` : ''}，暂不可购买` : '商品当前不可购买'))
    return
  }
  buying.value = true
  try {
    const res: any = await http.post('/api/market/orders/create_trade/', { product_id: product.value.id })
    emit('bought', res)
    closeBuyConfirm()

    const oid = res?.order_id || res?.id
    if (oid) {
      await startJump(oid)
    } else {
      open.value = false
      document.body.style.overflow = ''
    }
  } catch (e: any) {
    // Extract backend error message
    const data = e?.response?.data
    const msg =
      (typeof data === 'string' && data) ||
      (data?.error ? String(data.error) : '') ||
      (data?.detail ? String(data.detail) : '') ||
      (e?.message ? String(e.message) : '') ||
      '购买失败'

    closeBuyConfirm()
    cancelJump()
    openBuyError(msg)
  } finally {
    buying.value = false
  }
}

const detailLoading = ref(false)
const detail = ref<any | null>(null)
const detailError = ref<string | null>(null)

// Resolved display names (avoid showing 类目#xxx / 型号#xxx)
const resolvedCategoryName = ref<string>('')
const resolvedDeviceModelName = ref<string>('')

const displayCategoryName = computed(() => {
  const p: any = product.value
  return (
    resolvedCategoryName.value ||
    p?.category_name ||
    p?.category?.name ||
    (p?.category_id ? `类目#${p.category_id}` : '-')
  )
})

const displayDeviceModelName = computed(() => {
  const p: any = product.value
  return (
    resolvedDeviceModelName.value ||
    p?.device_model_name ||
    p?.device_model?.name ||
    (p?.device_model_id ? `型号#${p.device_model_id}` : '-')
  )
})

const fetchCategoryName = async (categoryId: number | string) => {
  try {
    const res: any = await http.get(`/api/market/categories/${categoryId}/`)
    resolvedCategoryName.value = res?.name ? String(res.name) : ''
  } catch {
    resolvedCategoryName.value = ''
  }
}

const fetchDeviceModelName = async (deviceModelId: number | string) => {
  try {
    const res: any = await http.get(`/api/market/device-models/${deviceModelId}/`)
    resolvedDeviceModelName.value = res?.name ? String(res.name) : ''
  } catch {
    resolvedDeviceModelName.value = ''
  }
}

// Use detail response when available, otherwise fall back to the passed-in list item
const product = computed<any | null>(() => detail.value ?? props.product)

watch(
  () => props.product?.id,
  () => {
    activeImgIndex.value = 0
  }
)

watch(
  [() => open.value, () => props.product?.id],
  async ([isOpen, id]) => {
    if (!isOpen || !id) {
      detail.value = null
      detailError.value = null
      buyConfirmOpen.value = false
      cancelJump()
      buyErrorOpen.value = false
      buyErrorMsg.value = ''
      // clear resolved names
      resolvedCategoryName.value = ''
      resolvedDeviceModelName.value = ''
      me.value = null
      commentsLoading.value = false
      commentsError.value = null
      comments.value = []
      commentDraft.value = ''
      sendingComment.value = false
      meSeq += 1
      commentsSeq += 1
      return
    }

    loadMe()
    loadComments(Number(id))

    detailLoading.value = true
    detailError.value = null
    try {
      detail.value = await getProductDetail(id)

      // After detail loaded, resolve names by ID (when API doesn't embed names)
      const p: any = detail.value
      resolvedCategoryName.value = ''
      resolvedDeviceModelName.value = ''

      if (p?.category_id) {
        await fetchCategoryName(p.category_id)
      }
      if (p?.device_model_id) {
        await fetchDeviceModelName(p.device_model_id)
      }
    } catch (e: any) {
      detail.value = null
      detailError.value = e?.message || '加载失败'
      resolvedCategoryName.value = ''
      resolvedDeviceModelName.value = ''
    } finally {
      detailLoading.value = false
    }
  },
  { immediate: true }
)

const activeImgIndex = ref(0)

const images = computed<string[]>(() => {
  const p: any = product.value
  if (!p) return []

  // Prefer `images` array if provided; otherwise fall back to main_image
  if (Array.isArray(p.images) && p.images.length) {
    // accept either strings or {url/image_name/main_image}
    return p.images
      .map((x: any) => {
        if (typeof x === 'string') return x
        return x?.url || x?.main_image || x?.image || x?.image_name || ''
      })
      .filter((s: any) => typeof s === 'string' && s.length)
      .map((s: string) => toAbsUrl(s))
  }

  if (typeof p.main_image === 'string' && p.main_image) return [toAbsUrl(p.main_image)]
  if (typeof p.image_name === 'string' && p.image_name) return [toAbsUrl(`/media/products/${p.image_name}`)]
  return []
})

const activeImage = computed(() => images.value[activeImgIndex.value] || '')

type ProductComment = {
  id: number
  product_id: number
  sender_id: number
  sender_name: string
  sender_role?: string
  content: string
  created_at: string
}

const me = ref<any | null>(null)
const commentsLoading = ref(false)
const commentsError = ref<string | null>(null)
const comments = ref<ProductComment[]>([])
const commentDraft = ref('')
const sendingComment = ref(false)
let meSeq = 0
let commentsSeq = 0

const isSeller = computed(() => {
  const p: any = product.value
  const mid = me.value?.id
  const sid = p?.seller_id ?? p?.seller?.id
  if (mid === undefined || mid === null || sid === undefined || sid === null) return false
  return String(mid) === String(sid)
})

const loadMe = async () => {
  const seq = (meSeq += 1)
  try {
    const res = await http.get('/api/auth/me/')
    if (seq !== meSeq) return
    me.value = res?.data ?? res
  } catch {
    if (seq !== meSeq) return
    me.value = null
  }
}

function normalizeCommentsPayload(payload: any, productId: number): ProductComment[] {
  const list = Array.isArray(payload)
    ? payload
    : (Array.isArray(payload?.results)
        ? payload.results
        : (payload && typeof payload === 'object' ? [payload] : []))
  return list
    .map((c: any) => ({
      id: Number(c?.id),
      product_id: Number(c?.product_id ?? productId),
      sender_id: Number(c?.sender_id ?? c?.user_id ?? 0),
      sender_name: String(c?.sender_name ?? c?.user_name ?? c?.username ?? (c?.sender_id ? `用户#${c.sender_id}` : '用户')),
      sender_role: c?.sender_role ? String(c.sender_role) : undefined,
      content: String(c?.content ?? c?.text ?? ''),
      created_at: String(c?.created_at ?? c?.createdAt ?? ''),
    }))
    .filter((c: ProductComment) => Number.isFinite(c.id) && !!c.content)
}

const loadComments = async (productId: number) => {
  const seq = (commentsSeq += 1)
  commentsLoading.value = true
  commentsError.value = null
  try {
    const res = await http.get(`/api/market/products/${productId}/comments/`)
    const payload = res?.data ?? res
    if (seq !== commentsSeq) return
    comments.value = normalizeCommentsPayload(payload, productId)
  } catch (e: any) {
    if (seq !== commentsSeq) return
    comments.value = []
    commentsError.value = e?.message || '留言加载失败'
  } finally {
    if (seq !== commentsSeq) return
    commentsLoading.value = false
  }
}

const reloadComments = async () => {
  const pid = product.value?.id
  if (!pid) return
  await loadComments(Number(pid))
}

const sendComment = async () => {
  const pid = product.value?.id
  const content = commentDraft.value.trim()
  if (!pid || sendingComment.value || !content) return

  sendingComment.value = true
  try {
    const res = await http.post(`/api/market/products/${pid}/comments/`, { content })
    const payload = res?.data ?? res
    const created = normalizeCommentsPayload(payload, Number(pid))
    if (created.length) {
      comments.value = comments.value.concat(created)
    } else {
      await loadComments(Number(pid))
    }
    commentDraft.value = ''
  } catch (e: any) {
    commentsError.value = e?.message || '发送失败'
  } finally {
    sendingComment.value = false
  }
}
</script>
