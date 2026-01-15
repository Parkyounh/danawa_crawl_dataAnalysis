<template>
  <div class="product-list-container">
    <header class="list-header">
      <div class="search-bar">
        <input v-model="searchQuery" @keyup.enter="resetAndFetch" placeholder="상품명을 입력하세요" />
      </div>
      <div class="button-group">
        <button
            class="action-btn"
            :class="isNewDb ? 'new-db-btn' : 'old-db-btn'"
            @click="toggleDatabase"
        >
          {{ isNewDb ? '신규 DB 데이터' : '기존 DB 데이터' }}
        </button>
      </div>
    </header>

    <nav class="category-tabs">
      <button v-for="cat in categories" :key="cat.id"
              :class="['tab-btn', { active: selectedCategory === cat.id }]"
              @click="changeCategory(cat.id)">
        {{ cat.name }}
      </button>
    </nav>

    <section class="product-grid">
      <div v-for="product in products" :key="product.id" class="product-card">
        <div class="card-header">
          <span class="product-code">{{ product.productCode }}</span>
        </div>

        <div class="clickable-area" @click="$emit('view-detail', product.productCode, isNewDb)">
          <div class="image-wrapper">
            <img :src="product.imageUrl" @error="e => e.target.src='/placeholder.png'" alt="상품 이미지" />
          </div>

          <div class="product-info">
            <div class="category-tag">{{ product.category }}</div>
            <h3 class="name">{{ product.name }}</h3>
            <div class="price-row">
              <span class="price">{{ product.price?.toLocaleString() }}원</span>
            </div>
          </div>
        </div>
      </div>

      <div ref="loadMoreTrigger" class="loading-trigger">
        <span v-if="isFetching">데이터를 불러오는 중입니다...</span>
        <span v-else-if="isLastPage && products.length > 0">모든 상품을 불러왔습니다.</span>
      </div>

      <div v-if="products.length === 0 && !isFetching" class="no-data">
        조회된 상품이 없습니다.
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, watch, onMounted, onUnmounted, defineProps, defineEmits } from 'vue';
import axios from 'axios';

const props = defineProps(['initialCategory']);
const emit = defineEmits(['view-detail', 'update-category']);

// 상태 관리
const products = ref([]);
const searchQuery = ref('');
const selectedCategory = ref(props.initialCategory || '데스크탑');

// [추가] DB 상태 관리 (false: 기존 DB, true: 신규 DB)
const isNewDb = ref(false);

// 페이징 관련 상태
const page = ref(0);
const size = 35;
const isFetching = ref(false);
const isLastPage = ref(false);
const loadMoreTrigger = ref(null);
let observer = null;

const categories = [
  { id: '데스크탑', name: '데스크탑' },
  { id: '소파', name: '소파' },
  { id: '패딩', name: '패딩' },
  { id: '신발', name: '신발' }
];

// 데이터 초기화 및 재조회
const resetAndFetch = () => {
  products.value = [];
  page.value = 0;
  isLastPage.value = false;
  fetchProducts();
};

const changeCategory = (categoryId) => {
  selectedCategory.value = categoryId;
  emit('update-category', categoryId);
  resetAndFetch();
};

// [수정] 데이터 변경 버튼 클릭 시 호출될 함수
const toggleDatabase = () => {
  isNewDb.value = !isNewDb.value; // 상태 반전
  resetAndFetch(); // 데이터 초기화 후 재호출
};

// API 호출
const fetchProducts = async () => {
  if (isFetching.value || isLastPage.value) return;

  isFetching.value = true;

  // isNewDb 상태에 따라 API 경로 결정
  const baseUrl = isNewDb.value
      ? 'http://localhost:8083/api/new/product'
      : 'http://localhost:8083/api/product';

  try {
    const response = await axios.get(baseUrl, {
      params: {
        page: page.value,
        size: size,
        category: selectedCategory.value,
        query: searchQuery.value
      }
    });

    const newData = response.data;
    if (newData.length < size) {
      isLastPage.value = true;
    }

    products.value.push(...newData);
    page.value++;
  } catch (error) {
    console.error("Fetch error:", error);
  } finally {
    isFetching.value = false;
  }
};

// 검색어 변경 감지
watch(searchQuery, () => {
  resetAndFetch();
});

onMounted(() => {
  fetchProducts();

  observer = new IntersectionObserver((entries) => {
    if (entries[0].isIntersecting && !isFetching.value) {
      fetchProducts();
    }
  }, { threshold: 0.1 });

  if (loadMoreTrigger.value) {
    observer.observe(loadMoreTrigger.value);
  }
});

onUnmounted(() => {
  if (observer) observer.disconnect();
});
</script>

<style scoped>
.product-list-container { padding: 20px; background: #f9f9f9; }

/* 헤더 & 검색바 */
.list-header { display: flex; justify-content: space-between; margin-bottom: 20px; align-items: center; }
.search-bar input { width: 300px; padding: 12px; border: 1px solid #ddd; border-radius: 8px; outline: none; }
.search-bar input:focus { border-color: #42b983; }

.loading-trigger {
  grid-column: 1 / -1;
  text-align: center;
  padding: 30px;
  color: #888;
  font-size: 14px;
}

/* 카테고리 탭 */
.category-tabs { display: flex; gap: 10px; margin-bottom: 25px; border-bottom: 1px solid #eee; padding-bottom: 10px; }
.tab-btn { border: none; background: none; padding: 10px 20px; cursor: pointer; color: #666; font-size: 15px; border-radius: 20px; }
.tab-btn.active { background: #42b983; color: white; font-weight: bold; }

/* 버튼 그룹 */
.action-btn { padding: 10px 15px; border: none; border-radius: 6px; cursor: pointer; font-weight: bold; margin-left: 8px; }
.temp-btn { background: #35495e; color: white; }

/* 상품 그리드 & 카드 */
.product-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(240px, 1fr)); gap: 20px; }
.product-card { background: #fff; border-radius: 12px; border: 1px solid #eee; transition: all 0.2s; position: relative; overflow: hidden; }
.product-card:hover { transform: translateY(-5px); box-shadow: 0 10px 20px rgba(0,0,0,0.08); }

.card-header { padding: 10px; display: flex; justify-content: flex-end; align-items: center; background: #fafafa; }
.product-code { font-size: 11px; color: #999; }

.image-wrapper { height: 180px; padding: 15px; display: flex; align-items: center; justify-content: center; }
.image-wrapper img { max-width: 100%; max-height: 100%; object-fit: contain; }

.product-info { padding: 15px; border-top: 1px solid #f5f5f5; }
.category-tag { font-size: 11px; color: #42b983; font-weight: bold; margin-bottom: 5px; }
.name { font-size: 14px; color: #333; line-height: 1.4; height: 2.8em; overflow: hidden; margin-bottom: 10px; font-weight: 500; }
.price { font-size: 17px; font-weight: bold; color: #e74c3c; }

.no-data { grid-column: 1 / -1; text-align: center; padding: 100px; color: #999; }

/* 버튼 상태별 색상 차이 부여 */
.old-db-btn { background: #35495e; color: white; }
.new-db-btn { background: #e74c3c; color: white; } /* 신규 DB일 때 빨간색 계열로 강조 */

.action-btn {
  padding: 10px 15px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: bold;
  transition: background 0.3s;
}
</style>