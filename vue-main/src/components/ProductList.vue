<template>
  <div class="product-list-container">
    <header class="list-header">
      <div class="search-bar">
        <input v-model="searchQuery" placeholder="상품명을 입력하세요" />
      </div>
      <div class="button-group">
        <button class="action-btn similarity" :disabled="selectedCodes.length !== 1">
          유사 이미지 검색 ({{ selectedCodes.length }}/1)
        </button>
<!--        <button class="action-btn compare" :disabled="selectedCodes.length !== 2">-->
<!--          상품 비교하기 ({{ selectedCodes.length }}/2)-->
<!--        </button>-->
      </div>
    </header>

    <nav class="category-tabs">
      <button v-for="cat in categories" :key="cat.id"
              :class="['tab-btn', { active: selectedCategory === cat.id }]"
              @click="selectedCategory = cat.id">
        {{ cat.name }}
      </button>
    </nav>

    <section class="product-grid">
      <div v-for="product in filteredProducts" :key="product.id" class="product-card">
        <div class="card-header">
          <input type="checkbox" :value="product.productCode" v-model="selectedCodes"
                 :disabled="selectedCodes.length >= 2 && !selectedCodes.includes(product.productCode)" />
          <span class="product-code">{{ product.productCode }}</span>
        </div>

        <div class="clickable-area" @click="$emit('view-detail', product.productCode)">
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

      <div v-if="filteredProducts.length === 0" class="no-data">
        조회된 상품이 없습니다.
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, defineProps, defineEmits } from 'vue';

const props = defineProps(['products']);
const emit = defineEmits(['view-detail']);

// 상태 관리
const searchQuery = ref('');
const selectedCategory = ref(null);
const selectedCodes = ref([]);

// 카테고리 목록 (백엔드에서 오는 한글명과 매칭)
const categories = [
  { id: null, name: '전체' },
  { id: '데스크탑', name: '데스크탑' },
  { id: '소파', name: '소파' },
  { id: '패딩', name: '패딩' },
  { id: '신발', name: '신발' }
];

// 필터링 로직: 검색어 + 카테고리 동시 적용
const filteredProducts = computed(() => {
  if (!props.products) return [];
  return props.products.filter(p => {
    const matchesSearch = p.name?.toLowerCase().includes(searchQuery.value.toLowerCase());
    // 백엔드 서비스에서 이미 category를 한글로 변환해서 보내주므로 문자열로 비교
    const matchesCategory = selectedCategory.value ? p.category === selectedCategory.value : true;
    return matchesSearch && matchesCategory;
  });
});
</script>

<style scoped>
.product-list-container { padding: 20px; background: #f9f9f9; }

/* 헤더 & 검색바 */
.list-header { display: flex; justify-content: space-between; margin-bottom: 20px; align-items: center; }
.search-bar input { width: 300px; padding: 12px; border: 1px solid #ddd; border-radius: 8px; outline: none; }
.search-bar input:focus { border-color: #42b983; }

/* 카테고리 탭 */
.category-tabs { display: flex; gap: 10px; margin-bottom: 25px; border-bottom: 1px solid #eee; padding-bottom: 10px; }
.tab-btn { border: none; background: none; padding: 10px 20px; cursor: pointer; color: #666; font-size: 15px; border-radius: 20px; }
.tab-btn.active { background: #42b983; color: white; font-weight: bold; }

/* 버튼 그룹 */
.action-btn { padding: 10px 15px; border: none; border-radius: 6px; cursor: pointer; font-weight: bold; margin-left: 8px; }
.action-btn:disabled { background: #ccc; cursor: not-allowed; }
.compare { background: #42b983; color: white; }
.similarity { background: #35495e; color: white; }

/* 상품 그리드 & 카드 */
.product-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(240px, 1fr)); gap: 20px; }
.product-card { background: #fff; border-radius: 12px; border: 1px solid #eee; transition: all 0.2s; position: relative; overflow: hidden; }
.product-card:hover { transform: translateY(-5px); box-shadow: 0 10px 20px rgba(0,0,0,0.08); }

.card-header { padding: 10px; display: flex; justify-content: space-between; align-items: center; background: #fafafa; }
.product-code { font-size: 11px; color: #999; }

.image-wrapper { height: 180px; padding: 15px; display: flex; align-items: center; justify-content: center; }
.image-wrapper img { max-width: 100%; max-height: 100%; object-fit: contain; }

.product-info { padding: 15px; border-top: 1px solid #f5f5f5; }
.category-tag { font-size: 11px; color: #42b983; font-weight: bold; margin-bottom: 5px; }
.name { font-size: 14px; color: #333; line-height: 1.4; height: 2.8em; overflow: hidden; margin-bottom: 10px; font-weight: 500; }
.price { font-size: 17px; font-weight: bold; color: #e74c3c; }

.no-data { grid-column: 1 / -1; text-align: center; padding: 100px; color: #999; }
</style>