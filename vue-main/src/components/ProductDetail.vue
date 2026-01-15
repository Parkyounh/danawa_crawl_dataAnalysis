<template>
  <div class="detail-wrapper">
    <header class="detail-header">
      <button @click="$emit('go-back')" class="back-btn">← 목록으로 돌아가기</button>
    </header>

    <div class="content-container" v-if="data">
      <div class="left-panel">
        <h2 class="title">상품 상세 정보</h2>
        <div class="image-box">
          <img :src="data.imageUrl" @error="e => e.target.src='/placeholder.png'" />
        </div>

        <div class="widget-area">
          <div class="widget-title">유사 이미지 검색 옵션</div>
          <SpecFilter
              v-if="candidates.length > 0"
              :products="candidates"
              :targetProduct="data"
              @filter-change="handleFilterChange"
          />
          <button class="search-btn" @click="executeSearch" :disabled="loading">
            {{ loading ? '검색 중...' : '🔍 조건으로 검색하기 (Top 10)' }}
          </button>
        </div>

        <div class="result-area" v-if="searchResult.length > 0">
          <div style="font-weight: bold; margin-bottom: 5px; font-size: 14px;">검색 결과 ({{ searchResult.length }}건)</div>
          <div class="result-grid">
            <div class="result-item" v-for="res in searchResult" :key="res.productId"
                 @click="goToProduct(res.productCode)"
                 style="cursor: pointer;">
              <img :src="res.imageUrl"
                   @error="e => e.target.src='/placeholder.png'"
                   class="result-img" />
              <div class="result-name">{{ res.productName }}</div>
              <div class="result-sim">{{ (res.similarity * 100).toFixed(1) }}%</div>
            </div>
          </div>
        </div>
      </div>

      <div class="right-panel">
        <h1 class="product-name">{{ data.name }}</h1>
        <p class="product-code">상품코드: {{ data.productCode }}</p>

        <div class="spec-section" v-if="data.specifications">
          <h3>상세 스펙</h3>
          <div class="spec-grid">
            <div v-for="(val, key) in parsedSpecs" :key="key" class="spec-item">
              <span class="spec-key">{{ key }}</span>
              <span class="spec-val">{{ val }}</span>
            </div>
          </div>
        </div>

        <div class="chart-section" v-if="data.priceHistory && data.priceHistory.length > 0">
          <h3>가격 변동 그래프</h3>
          <div class="canvas-holder">
            <canvas id="priceChart"></canvas>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, defineProps, defineEmits, nextTick, ref, watch, computed } from 'vue';
import axios from 'axios';
import Chart from 'chart.js/auto';
import SpecFilter from './SpecFilter.vue';

const props = defineProps(['data', 'isNewDb']);
const emit = defineEmits(['go-back', 'view-detail']);

const candidates = ref([]);
const filteredCandidates = ref([]);
const searchResult = ref([]);
const loading = ref(false);
let priceChart = null;

// 스펙 JSON 파싱 안전처리
const parsedSpecs = computed(() => {
  if (!props.data?.specifications) return {};
  try {
    return typeof props.data.specifications === 'string'
        ? JSON.parse(props.data.specifications)
        : props.data.specifications;
  } catch (e) { return {}; }
});

const goToProduct = (productCode) => {
  searchResult.value = [];
  window.scrollTo({ top: 0, behavior: 'smooth' });
  emit('view-detail', productCode, props.isNewDb);
};

// SpecFilter에서 전달된 filters를 기반으로 후보군을 필터링함
const handleFilterChange = (filters) => {
  if (Object.keys(filters).length === 0) {
    filteredCandidates.value = candidates.value;
    return;
  }

  filteredCandidates.value = candidates.value.filter(p => {
    return Object.keys(filters).every(key => {
      const selectedOptions = filters[key];
      if (!p.specifications) return false;
      try {
        const specs = typeof p.specifications === 'string'
            ? JSON.parse(p.specifications)
            : p.specifications;
        const productVal = specs[key];
        // 선택된 필터 옵션 중 하나라도 일치하면 통과
        return productVal && selectedOptions.includes(productVal);
      } catch (e) { return false; }
    });
  });
};

const executeSearch = async () => {
  if (!props.data) return;
  loading.value = true;
  searchResult.value = [];

  try {
    // 필터링된 상품들의 productCode 리스트 생성
    const filterIdList = filteredCandidates.value.map(p => String(p.productCode));

    // **서버 DTO가 List<String> filters를 받도록 설정되어 있어야 함**
    const res = await axios.post('http://localhost:8083/api/similar-images', {
      pcode: String(props.data.productCode),
      top: 10,
      filters: filterIdList
    });

    if (res.data.success) {
      searchResult.value = res.data.similarImages;
      if (searchResult.value.length === 0) {
        alert("일치하는 유사 이미지가 없습니다.");
      }
    } else {
      alert("검색 실패: " + res.data.error);
    }
  } catch (e) {
    console.error(e);
    alert("유사 이미지 검색 중 오류가 발생했습니다.");
  } finally {
    loading.value = false;
  }
};

const initChart = async () => {
  if (!props.data?.priceHistory?.length) return;
  await nextTick();
  const canvas = document.getElementById('priceChart');
  if (!canvas) return;
  if (priceChart) priceChart.destroy();

  const ctx = canvas.getContext('2d');
  const sortedHistory = [...props.data.priceHistory].sort((a, b) => new Date(a.date) - new Date(b.date));

  priceChart = new Chart(ctx, {
    type: 'line',
    data: {
      labels: sortedHistory.map(h => h.date),
      datasets: [{
        label: '최저가 추이 (원)',
        data: sortedHistory.map(h => h.price),
        borderColor: '#42b983',
        backgroundColor: 'rgba(66, 185, 131, 0.1)',
        fill: true,
        tension: 0.3
      }]
    },
    options: { responsive: true, maintainAspectRatio: false }
  });
};

onMounted(async () => {
  initChart();
  const listUrl = props.isNewDb ? 'http://localhost:8083/api/new/product' : 'http://localhost:8083/api/product';
  try {
    const res = await axios.get(listUrl, { params: { size: 1000 } });
    candidates.value = res.data;
    filteredCandidates.value = res.data; // 초기화
  } catch (e) { console.error("후보군 로드 실패", e); }
});

watch(() => props.data, () => {
  initChart();
});
</script>

<style scoped>
/* 기존 스타일과 동일 */
.detail-wrapper { padding: 30px; max-width: 1200px; margin: 0 auto; background: #fff; min-height: 100vh; }
.detail-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 30px; border-bottom: 1px solid #eee; padding-bottom: 20px; }
.back-btn { padding: 10px 20px; border: 1px solid #42b983; background: #fff; color: #42b983; border-radius: 8px; cursor: pointer; font-weight: bold; }
.back-btn:hover { background: #42b983; color: white; }
.content-container { display: flex; gap: 60px; }
.left-panel { flex: 0 0 450px; }
.image-box img { width: 100%; border-radius: 15px; border: 1px solid #f0f0f0; box-shadow: 0 10px 30px rgba(0,0,0,0.05); }
.widget-area { margin-top: 30px; border-top: 2px solid #eee; padding-top: 20px; }
.widget-title { font-size: 16px; font-weight: bold; margin-bottom: 10px; color: #333; }
.search-btn { width: 100%; padding: 12px; background: #333; color: white; border: none; border-radius: 8px; font-weight: bold; cursor: pointer; margin-top: 15px; }
.result-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 10px; margin-top: 10px; }
.result-item { border: 1px solid #eee; padding: 10px; border-radius: 8px; }
.result-img { width: 100%; height: 100px; object-fit: contain; }
.right-panel { flex: 1; }
.spec-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; border: 1px solid #eee; padding: 20px; border-radius: 12px; }
.spec-item { display: flex; border-bottom: 1px solid #f5f5f5; padding: 10px 0; font-size: 13px; }
.spec-key { font-weight: bold; width: 110px; color: #7f8c8d; }
.canvas-holder { height: 300px; width: 100%; }
</style>