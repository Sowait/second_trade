import Mock from 'mockjs';

Mock.setup({
  timeout: '200-600',
});

function safeJsonParse(text: any) {
  try {
    if (typeof text !== 'string') return null;
    return JSON.parse(text);
  } catch {
    return null;
  }
}

function getUrlInfo(rawUrl: string) {
  try {
    const u = new URL(rawUrl, window.location.origin);
    return { pathname: u.pathname, searchParams: u.searchParams };
  } catch {
    const qIndex = rawUrl.indexOf('?');
    const pathname = qIndex >= 0 ? rawUrl.slice(0, qIndex) : rawUrl;
    const search = qIndex >= 0 ? rawUrl.slice(qIndex + 1) : '';
    const sp = new URLSearchParams(search);
    return { pathname, searchParams: sp };
  }
}

function toInt(v: any, fallback: number) {
  const n = Number(v);
  return Number.isFinite(n) ? Math.trunc(n) : fallback;
}

function nowDate() {
  const d = new Date();
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${y}-${m}-${day}`;
}

type MockMe = {
  id: number;
  username: string;
  email?: string;
  role?: string;
  nickname?: string;
  avatar?: string;
  school?: string;
  phone?: string;
  address?: string;
  credit_score?: number;
};

type MockAuthUser = {
  id: number;
  username: string;
  student_id: string;
  email?: string;
  phone?: string;
  school?: string;
  role: 'user' | 'admin';
  nickname?: string;
  avatar?: string;
  address?: string;
  credit_score?: number;
  is_disabled?: boolean;
  is_superuser?: boolean | number | string;
  is_staff?: boolean | number | string;
  created_at: string;
};
type MockComment = {
  id: number;
  product_id: number;
  sender_id: number;
  sender_name: string;
  sender_role: 'buyer' | 'seller';
  content: string;
  created_at: string;
};
type MockProduct = {
  id: number;
  seller_id: number;
  category_id: number;
  category_name?: string;
  device_model_id: number;
  device_model_name?: string;
  brand_text?: string;
  title: string;
  school?: string;
  product_summary?: string;
  description?: string;
  grade_label: string;
  years_used: number;
  defects?: string[];
  main_image: string;
  images?: string[];
  selling_price: number;
  original_price: number;
  favorite_count?: number;
  view_count?: number;
  market_tag?: string;
  created_at?: string;
  status?: string;
  review_reject_reason?: string;
};

type MockOrder = {
  id: number;
  order_no: string;
  status: string;
  buyer_id: number;
  seller_id: number;
  product_id: number;
  created_at: string;
  paid_at?: string;
  pickup?: boolean;
};

type MockAdminUser = {
  id: number;
  username: string;
  nickname?: string;
  email?: string;
  phone?: string;
  role: 'user' | 'admin';
  balance: number;
  credit_score: number;
  avatar?: string;
  address?: string;
  is_superuser?: boolean | number | string;
  is_staff?: boolean | number | string;
};

const mockState: {
  me: MockMe;
  categories: { id: number; name: string; code?: string }[];
  deviceModels: { id: number; name: string; brand_id: number; category_id: number }[];
  products: MockProduct[];
  nextProductId: number;
  commentsByProductId: Record<number, MockComment[]>;
  nextCommentId: number;
  orders: MockOrder[];
  nextOrderId: number;
  adminUsers: MockAdminUser[];
  nextAdminUserId: number;
  authUsersByStudentId: Record<string, any>;
  nextAuthUserId: number;
  authUsers: MockAuthUser[];
} = {
  me: {
    id: 1,
    username: 'mockuser',
    email: 'mock@example.com',
    role: 'user',
    nickname: 'Mock User',
    avatar: 'https://placehold.co/80x80?text=U',
    school: '示例大学',
    phone: '13800138000',
    address: 'Mock Address',
    credit_score: 90,
  },
  categories: [
    { id: 1, name: 'Smartphones', code: 'smartphones' },
    { id: 2, name: 'Laptops', code: 'laptops' },
    { id: 3, name: 'Tablets', code: 'tablets' },
    { id: 4, name: 'Smartwatches', code: 'smartwatches' },
  ],
  deviceModels: [
    { id: 101, name: 'iPhone 13', brand_id: 1, category_id: 1 },
    { id: 102, name: 'iPhone 14', brand_id: 1, category_id: 1 },
    { id: 103, name: 'Galaxy S21', brand_id: 2, category_id: 1 },
    { id: 104, name: 'XPS 13', brand_id: 4, category_id: 2 },
  ],
  products: [
    {
      id: 2001,
      seller_id: 1,
      category_id: 1,
      device_model_id: 101,
      brand_text: 'Apple',
      title: 'iPhone 13 128GB Blue',
      description: 'Used for 1 year, good condition.',
      grade_label: 'A',
      years_used: 1,
      defects: ['scratch_screen', 'dent_body'],
      main_image: 'https://placehold.co/300x300?text=iPhone+13',
      images: [
        'https://placehold.co/400x400?text=iPhone+13+Front',
        'https://placehold.co/400x400?text=iPhone+13+Back',
      ],
      selling_price: 3200,
      original_price: 6000,
      favorite_count: 12,
      view_count: 103,
      market_tag: 'Hot',
      created_at: nowDate(),
      status: 'on_sale',
    },
    {
      id: 2002,
      seller_id: 1,
      category_id: 2,
      device_model_id: 104,
      brand_text: 'Dell',
      title: 'XPS 13 9310',
      description: 'Light scratches, works perfectly.',
      grade_label: 'B',
      years_used: 2,
      defects: ['minor_scratch'],
      main_image: 'https://placehold.co/300x300?text=XPS+13',
      images: ['https://placehold.co/400x400?text=XPS+13'],
      selling_price: 5200,
      original_price: 9999,
      favorite_count: 3,
      view_count: 41,
      market_tag: 'Good Deal',
      created_at: nowDate(),
      status: 'on_sale',
    },
    {
      id: 2003,
      seller_id: 99,
      category_id: 1,
      device_model_id: 103,
      brand_text: 'Samsung',
      title: 'Samsung Galaxy S21',
      description: 'Normal usage marks.',
      grade_label: 'B',
      years_used: 2,
      defects: ['battery_wear'],
      main_image: 'https://placehold.co/300x300?text=Galaxy+S21',
      images: ['https://placehold.co/400x400?text=Galaxy+S21'],
      selling_price: 2500,
      original_price: 5000,
      favorite_count: 8,
      view_count: 88,
      market_tag: 'Recommend',
      created_at: nowDate(),
      status: 'on_sale',
    },
  ],
  nextProductId: 3000,
  commentsByProductId: {
    2001: [
      {
        id: 9001,
        product_id: 2001,
        sender_id: 2,
        sender_name: '买家A',
        sender_role: 'buyer',
        content: '请问电池健康多少？有没有维修史？',
        created_at: new Date(Date.now() - 1000 * 60 * 60 * 6).toISOString(),
      },
      {
        id: 9002,
        product_id: 2001,
        sender_id: 1,
        sender_name: 'Mock User',
        sender_role: 'seller',
        content: '电池健康 88%，无维修史。屏幕有轻微划痕，拍照不明显。',
        created_at: new Date(Date.now() - 1000 * 60 * 60 * 5).toISOString(),
      },
    ],
  },
  nextCommentId: 9002,
  orders: [
    {
      id: 3001,
      order_no: 'ORD-20231027-0001',
      status: 'pending_payment',
      buyer_id: 1,
      seller_id: 99,
      product_id: 2003,
      created_at: '2023-10-27T10:00:00Z',
      pickup: false,
    },
    {
      id: 3002,
      order_no: 'ORD-20231026-9999',
      status: 'completed',
      buyer_id: 1,
      seller_id: 1,
      product_id: 2002,
      created_at: '2023-10-26T15:30:00Z',
      pickup: false,
    },
    {
      id: 4001,
      order_no: 'ORD-SELL-001',
      status: 'shipped',
      buyer_id: 2,
      seller_id: 1,
      product_id: 2001,
      created_at: '2023-10-25T09:00:00Z',
      pickup: false,
    },
  ],
  nextOrderId: 5000,
  adminUsers: [
    {
      id: 1,
      username: 'admin',
      nickname: '管理员',
      email: 'admin@example.com',
      phone: '13800000000',
      role: 'admin',
      balance: 100000,
      credit_score: 100,
      avatar: 'https://placehold.co/80x80?text=A',
      address: 'Admin Address',
      is_superuser: true,
      is_staff: true,
    },
    {
      id: 2,
      username: 'mockuser',
      nickname: 'Mock User',
      email: 'mock@example.com',
      phone: '13800138000',
      role: 'user',
      balance: 10000,
      credit_score: 90,
      avatar: 'https://placehold.co/80x80?text=U',
      address: 'Mock Address',
      is_superuser: false,
      is_staff: false,
    },
  ],
  nextAdminUserId: 2,
  authUsersByStudentId: {},
  nextAuthUserId: 2,
  authUsers: [],
};

{
  const admin: MockAuthUser = {
    id: 1,
    username: 'admin',
    student_id: 'admin',
    email: 'admin@example.com',
    phone: '13800000000',
    school: '管理员',
    role: 'admin',
    nickname: '管理员',
    avatar: 'https://placehold.co/80x80?text=A',
    address: 'Admin Address',
    credit_score: 100,
    is_disabled: false,
    is_superuser: true,
    is_staff: true,
    created_at: new Date(Date.now() - 1000 * 60 * 60 * 24 * 30).toISOString(),
  };

  const user: MockAuthUser = {
    id: 2,
    username: '20250001',
    student_id: '20250001',
    email: 'mock@example.com',
    phone: '13800138000',
    school: '示例大学',
    role: 'user',
    nickname: 'Mock User',
    avatar: 'https://placehold.co/80x80?text=U',
    address: 'Mock Address',
    credit_score: 90,
    is_disabled: false,
    is_superuser: false,
    is_staff: false,
    created_at: new Date(Date.now() - 1000 * 60 * 60 * 24 * 7).toISOString(),
  };

  mockState.authUsers = [admin, user];
  mockState.authUsersByStudentId[admin.student_id] = admin;
  mockState.authUsersByStudentId[user.student_id] = user;
  mockState.nextAuthUserId = 2;
}

// -------------------------
// Auth
// -------------------------
Mock.mock(/\/api\/auth\/login/, 'post', (options: any) => {
  const body = safeJsonParse(options?.body) || {};
  const studentIdRaw =
    body?.student_id != null
      ? String(body.student_id)
      : (body?.username != null ? String(body.username) : 'mockuser');
  const studentId = studentIdRaw.trim() || 'mockuser';
  const existing: MockAuthUser | null = (mockState.authUsersByStudentId[studentId] as any) || null;

  const user: MockAuthUser =
    existing ||
    (() => {
      const newId = (mockState.nextAuthUserId += 1);
      const u: MockAuthUser = {
        id: newId,
        username: studentId,
        student_id: studentId,
        email: '',
        phone: '',
        school: '',
        role: studentId === 'admin' ? 'admin' : 'user',
        nickname: studentId === 'admin' ? '管理员' : '',
        avatar: studentId === 'admin' ? 'https://placehold.co/80x80?text=A' : 'https://placehold.co/80x80?text=U',
        address: '',
        credit_score: 100,
        is_disabled: false,
        is_superuser: studentId === 'admin',
        is_staff: studentId === 'admin',
        created_at: new Date().toISOString(),
      };
      mockState.authUsers.push(u);
      mockState.authUsersByStudentId[studentId] = u;
      return u;
    })();

  if (user.is_disabled) return { detail: '账号已被禁用' };

  mockState.me = {
    id: user.id,
    username: user.username,
    email: user.email,
    role: user.role,
    nickname: user.nickname,
    avatar: user.avatar,
    school: user.school,
    phone: user.phone,
    address: user.address,
    credit_score: user.credit_score,
  };

  return {
    access: 'mock-access-token',
    refresh: 'mock-refresh-token',
    user,
  };
});

Mock.mock(/\/api\/auth\/register/, 'post', (options: any) => {
  const body = safeJsonParse(options?.body) || {};
  const studentIdRaw =
    body?.student_id != null
      ? String(body.student_id)
      : (body?.username != null ? String(body.username) : '');
  const studentId = studentIdRaw.trim();
  if (!studentId) return { detail: 'student_id required' };
  if (mockState.authUsersByStudentId[studentId]) return { detail: '学号/社区号已注册' };

  const u: MockAuthUser = {
    id: (mockState.nextAuthUserId += 1),
    username: studentId,
    student_id: studentId,
    email: body?.email ? String(body.email) : '',
    role: 'user',
    is_superuser: false,
    is_staff: false,
    nickname: body?.nickname ? String(body.nickname) : '',
    phone: body?.phone ? String(body.phone) : '',
    school: body?.school ? String(body.school) : '',
    address: body?.address ? String(body.address) : '',
    credit_score: 100,
    is_disabled: false,
    created_at: new Date().toISOString(),
  };

  mockState.authUsers.push(u);
  mockState.authUsersByStudentId[studentId] = u;

  return {
    access: 'mock-access-token-new',
    refresh: 'mock-refresh-token-new',
    user: u,
  };
});

Mock.mock(/\/api\/auth\/refresh/, 'post', {
  access: 'mock-access-token-refreshed',
});

Mock.mock(/\/api\/auth\/me\/?$/, 'get', () => {
  return mockState.me;
});

Mock.mock(/\/api\/auth\/me\/?$/, 'put', (options: any) => {
  const body = safeJsonParse(options?.body) || {};
  mockState.me = {
    ...mockState.me,
    ...body,
    id: mockState.me.id,
    username: mockState.me.username,
  };

  const sid = String(mockState.me.username || '').trim();
  if (sid) {
    const u = mockState.authUsersByStudentId[sid] as MockAuthUser | undefined;
    if (u) {
      const next = { ...u, ...body, id: u.id, username: u.username, student_id: u.student_id } as MockAuthUser;
      mockState.authUsersByStudentId[sid] = next;
      const idx = mockState.authUsers.findIndex((x) => x.id === next.id);
      if (idx >= 0) mockState.authUsers[idx] = next;
    }
  }
  return mockState.me;
});

Mock.mock(/\/api\/auth\/admin\/users\/\d+\/disable\/?$/, 'post', (options: any) => {
  if (mockState.me?.role !== 'admin') return { detail: 'forbidden' };
  const info = getUrlInfo(options?.url || '');
  const idStr = info.pathname.split('/').filter(Boolean).slice(-2)[0] || '';
  const id = toInt(idStr, 0);
  const idx = mockState.authUsers.findIndex((x) => x.id === id);
  if (idx < 0) return { ok: false };
  const prev = mockState.authUsers[idx];
  if (!prev) return { ok: false };
  const next: MockAuthUser = { ...prev, is_disabled: true };
  mockState.authUsers[idx] = next;
  mockState.authUsersByStudentId[next.student_id] = next;
  return { ok: true, id: next.id, is_disabled: true };
});

Mock.mock(/\/api\/auth\/admin\/users\/?(?:\?|$)/, 'get', (options: any) => {
  if (mockState.me?.role !== 'admin') return { detail: 'forbidden' };
  const info = getUrlInfo(options?.url || '');
  const sp = info.searchParams;
  const q = (sp.get('q') || '').trim().toLowerCase();
  const page = Math.max(1, toInt(sp.get('page'), 1));
  const pageSize = Math.max(1, toInt(sp.get('page_size'), 10));

  let list = mockState.authUsers.slice();
  if (q) {
    list = list.filter((u) => {
      const s = `${u.student_id} ${u.username} ${u.nickname ?? ''} ${u.email ?? ''} ${u.phone ?? ''} ${u.school ?? ''}`.toLowerCase();
      return s.includes(q);
    });
  }

  const count = list.length;
  const start = (page - 1) * pageSize;
  const end = Math.min(count, start + pageSize);
  const results = list.slice(start, end);
  return { count, results };
});

Mock.mock(/\/api\/auth\/admin\/users\/?$/, 'post', (options: any) => {
  const body = safeJsonParse(options?.body) || {};
  const newId = (mockState.nextAdminUserId += 1);
  const u: MockAdminUser = {
    id: newId,
    username: body?.username ? String(body.username) : `user${newId}`,
    nickname: body?.nickname ? String(body.nickname) : '',
    email: body?.email ? String(body.email) : '',
    phone: body?.phone ? String(body.phone) : '',
    role: body?.role === 'admin' ? 'admin' : 'user',
    balance: toInt(body?.balance, 0),
    credit_score: toInt(body?.credit_score, 100),
    avatar: body?.avatar ? String(body.avatar) : '',
    address: body?.address ? String(body.address) : '',
    is_superuser: body?.role === 'admin',
    is_staff: body?.role === 'admin',
  };
  mockState.adminUsers.unshift(u);
  return u;
});

Mock.mock(/\/api\/auth\/admin\/users\/\d+\/?$/, 'put', (options: any) => {
  const info = getUrlInfo(options?.url || '');
  const idStr = info.pathname.split('/').filter(Boolean).pop() || '';
  const id = toInt(idStr, 0);
  const body = safeJsonParse(options?.body) || {};
  const idx = mockState.adminUsers.findIndex((x) => x.id === id);
  if (idx < 0) return null;
  const prev = mockState.adminUsers[idx];
  if (!prev) return null;
  const role = body?.role === 'admin' ? 'admin' : 'user';
  const next: MockAdminUser = {
    ...prev,
    username: body?.username != null ? String(body.username) : prev.username,
    nickname: body?.nickname != null ? String(body.nickname) : prev.nickname,
    email: body?.email != null ? String(body.email) : prev.email,
    phone: body?.phone != null ? String(body.phone) : prev.phone,
    address: body?.address != null ? String(body.address) : prev.address,
    avatar: body?.avatar != null ? String(body.avatar) : prev.avatar,
    role,
    balance: body?.balance != null ? toInt(body.balance, prev.balance) : prev.balance,
    credit_score: body?.credit_score != null ? toInt(body.credit_score, prev.credit_score) : prev.credit_score,
    is_superuser: role === 'admin',
    is_staff: role === 'admin',
  };
  mockState.adminUsers[idx] = next;
  return next;
});

Mock.mock(/\/api\/auth\/admin\/users\/\d+\/?$/, 'delete', (options: any) => {
  const info = getUrlInfo(options?.url || '');
  const idStr = info.pathname.split('/').filter(Boolean).pop() || '';
  const id = toInt(idStr, 0);
  mockState.adminUsers = mockState.adminUsers.filter((x) => x.id !== id);
  return { ok: true };
});

// -------------------------
// Brands & Models
// -------------------------
Mock.mock(/\/api\/market\/brands\//, 'get', (_options: any) => {
  // console.log('Mock brands', _options);
  return [
    { id: 1, name: 'Apple', category_id: 1 },
    { id: 2, name: 'Samsung', category_id: 1 },
    { id: 3, name: 'Xiaomi', category_id: 1 },
    { id: 4, name: 'Dell', category_id: 2 },
    { id: 5, name: 'Lenovo', category_id: 2 },
  ];
});

Mock.mock(/\/api\/market\/device-models\/reference\//, 'get', (_options: any) => {
  // console.log('Mock device models reference', _options);
  return {
    id: 101,
    name: 'iPhone 13',
    brand_id: 1,
    brand_name: 'Apple',
    image_url: 'https://placehold.co/200x200?text=iPhone+13',
    msrp_price: 5999,
    index_type: 'phone_standard',
  };
});

// -------------------------
// Drafts (Selling Flow)
// -------------------------
Mock.mock(/\/api\/market\/drafts\/init\//, 'post', {
  draft_key: 'mock-draft-key-123',
  meta: {
    category_id: 1,
    device_model_id: 101,
    years_used: 1,
    original_price: 5000,
  },
});

Mock.mock(/\/api\/market\/drafts\/.*\/images\//, 'post', {
  id: 1,
  image: 'https://placehold.co/300x300?text=Uploaded+Image',
});

Mock.mock(/\/api\/market\/drafts\/.*\/analyze\//, 'post', {
  main_image: 'https://placehold.co/300x300?text=Analyzed+Image',
  grade_label: 'A',
  grade_score: 95,
  defects: ['scratch_screen', 'dent_body'],
});

Mock.mock(/\/api\/market\/drafts\/.*\/estimate\//, 'post', {
  estimated_price_min: 3000,
  estimated_price_max: 3500,
  suggested_price: 3200,
});

Mock.mock(/\/api\/market\/drafts\/.*\/publish\//, 'post', (options: any) => {
  const body = safeJsonParse(options?.body) || {};
  const newId = (mockState.nextProductId += 1);
  const categoryId = toInt(body?.category_id, 1);
  const modelId = toInt(body?.device_model_id, 101);
  const category = mockState.categories.find((c) => c.id === categoryId) || null;
  const model = mockState.deviceModels.find((m) => m.id === modelId) || null;

  const product: MockProduct = {
    id: newId,
    seller_id: mockState.me.id,
    category_id: categoryId,
    category_name: category?.name,
    device_model_id: modelId,
    device_model_name: model?.name,
    brand_text: body?.brand_text ? String(body.brand_text) : undefined,
    title: body?.title ? String(body.title) : `商品#${newId}`,
    school: body?.school ? String(body.school) : undefined,
    product_summary: body?.product_summary ? String(body.product_summary) : '',
    description: body?.description ? String(body.description) : '',
    grade_label: body?.grade_label ? String(body.grade_label) : 'A',
    years_used: toInt(body?.years_used, 1),
    defects: Array.isArray(body?.defects) ? body.defects.map((x: any) => String(x)) : [],
    main_image: 'https://placehold.co/300x300?text=New+Product',
    images: ['https://placehold.co/400x400?text=New+Product'],
    selling_price: toInt(body?.selling_price, 1999),
    original_price: toInt(body?.original_price, 3999),
    favorite_count: 0,
    view_count: 0,
    market_tag: 'New',
    created_at: nowDate(),
    status: 'pending_review',
    review_reject_reason: undefined,
  };

  mockState.products.unshift(product);

  return { product_id: newId, status: 'published' };
});

// -------------------------
// Products (Marketplace)
// -------------------------

Mock.mock(/\/api\/market\/categories\/?(?:\?|$)/, 'get', () => {
  return mockState.categories;
});

Mock.mock(/\/api\/market\/categories\/\d+\/?(?:\?|$)/, 'get', (options: any) => {
  const info = getUrlInfo(options?.url || '');
  const idStr = info.pathname.split('/').filter(Boolean).pop() || '';
  const id = toInt(idStr, 0);
  return mockState.categories.find((c) => c.id === id) || null;
});

Mock.mock(/\/api\/market\/device-models\/($|\?)/, 'get', () => {
  return mockState.deviceModels;
});

Mock.mock(/\/api\/market\/device-models\/\d+\/?(?:\?|$)/, 'get', (options: any) => {
  const info = getUrlInfo(options?.url || '');
  const idStr = info.pathname.split('/').filter(Boolean).pop() || '';
  const id = toInt(idStr, 0);
  return mockState.deviceModels.find((m) => m.id === id) || null;
});

Mock.mock(/\/api\/market\/products\/\d+\/?(?:\?|$)/, 'get', (options: any) => {
  const info = getUrlInfo(options?.url || '');
  const idStr = info.pathname.split('/').filter(Boolean).pop() || '';
  const id = toInt(idStr, 0);
  const p = mockState.products.find((x) => x.id === id) || null;
  if (!p) return null;
  const category = mockState.categories.find((c) => c.id === p.category_id) || null;
  const model = mockState.deviceModels.find((m) => m.id === p.device_model_id) || null;
  return {
    ...p,
    category_name: p.category_name ?? category?.name,
    device_model_name: p.device_model_name ?? model?.name,
    price: p.selling_price,
    seller: { id: p.seller_id, username: p.seller_id === mockState.me.id ? mockState.me.username : 'seller' },
  };
});

// -------------------------
// Comments (Product Q&A)
// -------------------------
Mock.mock(/\/api\/market\/products\/\d+\/comments\/?(?:\?|$)/, 'get', (options: any) => {
  const info = getUrlInfo(options?.url || '');
  const parts = info.pathname.split('/').filter(Boolean);
  const idx = parts.indexOf('products');
  const idStr = idx >= 0 ? parts[idx + 1] : '';
  const productId = toInt(idStr, 0);
  const list = mockState.commentsByProductId[productId] || [];
  return { count: list.length, results: list };
});

Mock.mock(/\/api\/market\/products\/\d+\/comments\/?(?:\?|$)/, 'post', (options: any) => {
  const info = getUrlInfo(options?.url || '');
  const parts = info.pathname.split('/').filter(Boolean);
  const idx = parts.indexOf('products');
  const idStr = idx >= 0 ? parts[idx + 1] : '';
  const productId = toInt(idStr, 0);

  const body = safeJsonParse(options?.body) || {};
  const content = body?.content ? String(body.content) : (body?.text ? String(body.text) : '');

  const product = mockState.products.find((p) => p.id === productId) || null;
  const senderRole: MockComment['sender_role'] =
    product && String(product.seller_id) === String(mockState.me.id) ? 'seller' : 'buyer';

  const newId = (mockState.nextCommentId += 1);
  const item: MockComment = {
    id: newId,
    product_id: productId,
    sender_id: mockState.me.id,
    sender_name: mockState.me.nickname || mockState.me.username || `用户#${mockState.me.id}`,
    sender_role: senderRole,
    content,
    created_at: new Date().toISOString(),
  };

  if (!mockState.commentsByProductId[productId]) mockState.commentsByProductId[productId] = [];
  mockState.commentsByProductId[productId].push(item);
  return item;
});

Mock.mock(/\/api\/market\/products\/?(?:\?|$)/, 'get', (options: any) => {
  const info = getUrlInfo(options?.url || '');
  const sp = info.searchParams;

  const sellerId = sp.get('seller_id');
  const categoryId = sp.get('category_id');
  const page = Math.max(1, toInt(sp.get('page') ?? sp.get('page_num'), 1));
  const pageSize = Math.max(1, toInt(sp.get('page_size') ?? sp.get('limit'), 24));

  let list = mockState.products.slice();

  if (sellerId != null && String(sellerId).trim() !== '') {
    const sid = toInt(sellerId, -1);
    list = list.filter((p) => p.seller_id === sid);
  } else {
    list = list.filter((p) => (p.status ?? 'on_sale') === 'on_sale');
  }
  if (categoryId != null && String(categoryId).trim() !== '') {
    const cid = toInt(categoryId, -1);
    list = list.filter((p) => p.category_id === cid);
  }

  const total = list.length;
  const start = (page - 1) * pageSize;
  const end = Math.min(total, start + pageSize);
  const results = list.slice(start, end).map((p) => ({
    ...p,
    category_name: p.category_name ?? mockState.categories.find((c) => c.id === p.category_id)?.name,
    device_model_name: p.device_model_name ?? mockState.deviceModels.find((m) => m.id === p.device_model_id)?.name,
    main_image_url: p.main_image,
    image_name: p.main_image,
    views: p.view_count ?? 0,
    view_count: p.view_count ?? 0,
  }));

  return { count: total, results };
});

Mock.mock(/\/api\/market\/admin\/products\/pending_review\/?(?:\?|$)/, 'get', (options: any) => {
  if (mockState.me?.role !== 'admin') return { detail: 'forbidden' };
  const info = getUrlInfo(options?.url || '');
  const sp = info.searchParams;
  const q = (sp.get('q') || '').trim().toLowerCase();
  const page = Math.max(1, toInt(sp.get('page') ?? sp.get('page_num'), 1));
  const pageSize = Math.max(1, toInt(sp.get('page_size') ?? sp.get('limit'), 20));

  let list = mockState.products.slice().filter((p) => (p.status ?? '') === 'pending_review');
  if (q) {
    list = list.filter((p) => {
      const s = `${p.title ?? ''} ${p.school ?? ''} ${p.seller_id ?? ''} ${p.category_name ?? ''} ${p.device_model_name ?? ''}`.toLowerCase();
      return s.includes(q);
    });
  }
  const total = list.length;
  const start = (page - 1) * pageSize;
  const end = Math.min(total, start + pageSize);
  const results = list.slice(start, end).map((p) => ({
    ...p,
    category_name: p.category_name ?? mockState.categories.find((c) => c.id === p.category_id)?.name,
    device_model_name: p.device_model_name ?? mockState.deviceModels.find((m) => m.id === p.device_model_id)?.name,
    main_image_url: p.main_image,
    image_name: p.main_image,
  }));

  return { count: total, results };
});

Mock.mock(/\/api\/market\/admin\/products\/\d+\/approve\/?$/, 'post', (options: any) => {
  if (mockState.me?.role !== 'admin') return { ok: false, detail: 'forbidden' };
  const info = getUrlInfo(options?.url || '');
  const parts = info.pathname.split('/').filter(Boolean);
  const idx = parts.indexOf('products');
  const idStr = idx >= 0 ? parts[idx + 1] : '';
  const id = toInt(idStr, 0);
  const p = mockState.products.find((x) => x.id === id) || null;
  if (!p) return { ok: false };
  p.status = 'on_sale';
  p.review_reject_reason = undefined;
  return { ok: true, product_id: id, status: p.status };
});

Mock.mock(/\/api\/market\/admin\/products\/\d+\/reject\/?$/, 'post', (options: any) => {
  if (mockState.me?.role !== 'admin') return { ok: false, detail: 'forbidden' };
  const info = getUrlInfo(options?.url || '');
  const parts = info.pathname.split('/').filter(Boolean);
  const idx = parts.indexOf('products');
  const idStr = idx >= 0 ? parts[idx + 1] : '';
  const id = toInt(idStr, 0);
  const p = mockState.products.find((x) => x.id === id) || null;
  if (!p) return { ok: false };
  const body = safeJsonParse(options?.body) || {};
  const reason = body?.reason != null ? String(body.reason).trim() : '';
  p.status = 'rejected';
  p.review_reject_reason = reason || undefined;
  return { ok: true, product_id: id, status: p.status, reason: p.review_reject_reason };
});

// -------------------------
// Orders
// -------------------------
function getOrderStatusView(o: MockOrder) {
  if (o.pickup && o.status === 'pending_shipment') return '待自提';
  return undefined;
}

function toOrderListItem(o: MockOrder) {
  const p = mockState.products.find((x) => x.id === o.product_id) || null;
  return {
    id: o.id,
    order_no: o.order_no,
    status: o.status,
    buyer_id: o.buyer_id,
    seller_id: o.seller_id,
    product_id: o.product_id,
    product_title: p?.title || `商品#${o.product_id}`,
    product_main_image: p?.main_image || 'https://placehold.co/100x100?text=Product',
    product_selling_price: p?.selling_price ?? 0,
    created_at: o.created_at,
    paid_at: o.paid_at,
    pickup: !!o.pickup,
    status_view: getOrderStatusView(o),
  };
}

function genOrderNo(orderId: number) {
  const date = nowDate().replace(/-/g, '');
  const seq = String(orderId).slice(-4).padStart(4, '0');
  return `ORD-${date}-${seq}`;
}

function parseOrderIdFromUrl(rawUrl: string) {
  const info = getUrlInfo(rawUrl || '');
  const parts = info.pathname.split('/').filter(Boolean);
  const idx = parts.indexOf('orders');
  const idStr = idx >= 0 ? parts[idx + 1] : '';
  return toInt(idStr, 0);
}

Mock.mock(/\/api\/market\/admin\/orders\/?(?:\?|$)/, 'get', (options: any) => {
  if (mockState.me?.role !== 'admin') return { detail: 'forbidden' };
  const info = getUrlInfo(options?.url || '');
  const sp = info.searchParams;
  const q = (sp.get('q') || '').trim().toLowerCase();
  const status = (sp.get('status') || '').trim();
  const startDate = (sp.get('start') || sp.get('start_date') || '').trim();
  const endDate = (sp.get('end') || sp.get('end_date') || '').trim();
  const page = Math.max(1, toInt(sp.get('page') ?? sp.get('page_num'), 1));
  const pageSize = Math.max(1, toInt(sp.get('page_size') ?? sp.get('limit'), 20));

  function dateKey(iso: any) {
    try {
      const d = new Date(String(iso || ''));
      if (Number.isNaN(d.getTime())) return '';
      return d.toISOString().slice(0, 10);
    } catch {
      return '';
    }
  }

  function userLabel(id: number) {
    const u = mockState.authUsers.find((x) => x.id === id) || null;
    return u?.nickname || u?.username || `用户#${id}`;
  }

  let list = mockState.orders.slice();
  if (status) list = list.filter((o) => String(o.status || '') === status);
  if (startDate) list = list.filter((o) => {
    const k = dateKey(o.created_at);
    return !k || k >= startDate;
  });
  if (endDate) list = list.filter((o) => {
    const k = dateKey(o.created_at);
    return !k || k <= endDate;
  });
  if (q) {
    list = list.filter((o) => {
      const p = mockState.products.find((x) => x.id === o.product_id) || null;
      const s = `${o.order_no} ${o.status} ${p?.title ?? ''} ${o.buyer_id} ${o.seller_id}`.toLowerCase();
      return s.includes(q);
    });
  }

  const total = list.length;
  const start = (page - 1) * pageSize;
  const end = Math.min(total, start + pageSize);
  const results = list.slice(start, end).map((o) => {
    const base: any = toOrderListItem(o);
    return {
      ...base,
      buyer_name: userLabel(o.buyer_id),
      seller_name: userLabel(o.seller_id),
      amount: base?.product_selling_price ?? 0,
    };
  });

  return { count: total, results };
});

Mock.mock(/\/api\/market\/orders\/create_trade\//, 'post', (options: any) => {
  const body = safeJsonParse(options?.body) || {};
  const productId = toInt(body?.product_id, 0);
  const p = mockState.products.find((x) => x.id === productId) || null;
  const newId = (mockState.nextOrderId += 1);
  const order: MockOrder = {
    id: newId,
    order_no: genOrderNo(newId),
    status: 'pending_payment',
    buyer_id: mockState.me.id,
    seller_id: p?.seller_id ?? 0,
    product_id: productId,
    created_at: new Date().toISOString(),
    pickup: false,
  };
  mockState.orders.unshift(order);
  return {
    order_id: order.id,
    order_no: order.order_no,
    status: order.status,
    product_id: order.product_id,
  };
});

Mock.mock(/\/api\/market\/orders\/buy\//, 'get', () => {
  return mockState.orders.filter((o) => o.buyer_id === mockState.me.id).map((o) => toOrderListItem(o));
});

Mock.mock(/\/api\/market\/orders\/sell\//, 'get', () => {
  return mockState.orders.filter((o) => o.seller_id === mockState.me.id).map((o) => toOrderListItem(o));
});

Mock.mock(/\/api\/market\/orders\/\d+\//, 'get', (options: any) => {
  const orderId = parseOrderIdFromUrl(options?.url || '');
  const o = mockState.orders.find((x) => x.id === orderId) || null;
  if (!o) return null;
  const p = mockState.products.find((x) => x.id === o.product_id) || null;
  return {
    ...toOrderListItem(o),
    amount: p?.selling_price ?? 0,
    buyer_name: o.buyer_id === mockState.me.id ? (mockState.me.nickname || mockState.me.username) : `买家#${o.buyer_id}`,
    seller_name: o.seller_id === mockState.me.id ? (mockState.me.nickname || mockState.me.username) : `卖家#${o.seller_id}`,
    address: {
      name: mockState.me.nickname || mockState.me.username || 'Mock User',
      phone: '13800138000',
      detail: 'Mock Address, City, Country',
    },
  };
});

Mock.mock(/\/api\/market\/orders\/\d+\/pay\//, 'post', (options: any) => {
  const orderId = parseOrderIdFromUrl(options?.url || '');
  const o = mockState.orders.find((x) => x.id === orderId) || null;
  if (!o) return { order_id: orderId, status: 'refunded' };
  const body = safeJsonParse(options?.body) || {};
  o.pickup = !!body?.pickup;
  o.paid_at = new Date().toISOString();
  o.status = 'pending_shipment';
  return { order_id: o.id, status: o.status };
});

Mock.mock(/\/api\/market\/orders\/\d+\/cancel_payment\//, 'post', (options: any) => {
  const orderId = parseOrderIdFromUrl(options?.url || '');
  const o = mockState.orders.find((x) => x.id === orderId) || null;
  if (!o) return { order_id: orderId, status: 'refunded' };
  o.status = 'refunded';
  return { order_id: o.id, status: o.status };
});

Mock.mock(/\/api\/market\/orders\/\d+\/ship\//, 'post', (options: any) => {
  const orderId = parseOrderIdFromUrl(options?.url || '');
  const o = mockState.orders.find((x) => x.id === orderId) || null;
  if (!o) return { order_id: orderId, status: 'refunded' };
  if (!o.pickup && o.status === 'pending_shipment') {
    o.status = 'shipped';
  }
  return { order_id: o.id, status: o.status };
});

Mock.mock(/\/api\/market\/orders\/\d+\/confirm_receipt\//, 'post', (options: any) => {
  const orderId = parseOrderIdFromUrl(options?.url || '');
  const o = mockState.orders.find((x) => x.id === orderId) || null;
  if (!o) return { order_id: orderId, status: 'refunded' };
  if (o.pickup) {
    if (o.status === 'pending_shipment') o.status = 'completed';
  } else {
    if (o.status === 'pending_receipt' || o.status === 'shipped') o.status = 'completed';
  }
  return { order_id: o.id, status: o.status };
});

Mock.mock(/\/api\/market\/orders\/\d+\/refund\//, 'post', (options: any) => {
  const orderId = parseOrderIdFromUrl(options?.url || '');
  const o = mockState.orders.find((x) => x.id === orderId) || null;
  if (!o) return { order_id: orderId, status: 'refunded' };
  o.status = 'refunded';
  return { order_id: o.id, status: o.status };
});

console.log('MockJS initialized');
