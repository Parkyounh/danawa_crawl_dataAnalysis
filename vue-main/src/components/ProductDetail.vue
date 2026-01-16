<template>
  <div class="detail-wrapper">
    <header class="detail-header">
      <button @click="$emit('go-back')" class="back-btn">← 목록으로 돌아가기</button>
    </header>

    <div class="content-container" v-if="data">
      <div class="left-panel">
        <div class="image-box">
          <img :src="data.imageUrl" @error="e => e.target.src='/placeholder.png'" />
        </div>
        <h1 class="product-name-left">{{ data.name }}</h1>
        <p class="product-code-left">상품코드: {{ data.productCode }}</p>
      </div>

      <div class="right-panel">
        <div class="tab-menu">
          <button
              :class="['tab-btn', { active: currentTab === 'info' }]"
              @click="currentTab = 'info'"
          >
            <span>상품 정보</span>
          </button>
          <button
              :class="['tab-btn', { active: currentTab === 'similar' }]"
              @click="currentTab = 'similar'"
          >
            <span>유사 상품 찾기</span>
          </button>
        </div>

        <div class="tab-content">
          <div v-if="currentTab === 'info'" class="fade-in">
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

          <div v-if="currentTab === 'similar'" class="fade-in">
            <div class="search-widget-card">
              <h3>스펙 필터링 검색</h3>
              <p class="helper-text">찾으시는 상품의 조건을 선택하고 검색 버튼을 눌러주세요.</p>

              <SpecFilter
                  v-if="Object.keys(serverSpecs).length > 0"
                  :specs="serverSpecs"
                  @filter-change="handleFilterChange"
              />

              <div class="filter-status" v-if="candidates.length > 0">
                현재 조건에 맞는 상품: <strong>{{ filteredCandidates.length }}</strong>개
              </div>

              <button class="search-btn" @click="executeSearch" :disabled="loading || filteredCandidates.length === 0">
                {{ loading ? '유사도 분석 중...' : '이 조건으로 유사 상품 검색하기' }}
              </button>
            </div>

            <div class="result-area" v-if="searchResult.length > 0">
              <h3 class="result-title">분석 결과 (상위 {{ searchResult.length }}개)</h3>
              <div class="result-grid">
                <div class="result-item" v-for="res in searchResult" :key="res.productCode" @click="goToProduct(res.productCode)">
                  <div class="res-img-wrapper">
                    <img :src="res.imageUrl" class="result-img" />
                    <div class="res-badge">{{ (res.similarity * 100).toFixed(1) }}%</div>
                  </div>
                  <div class="result-info">
                    <div class="result-name">{{ res.productName }}</div>
                  </div>
                </div>
              </div>
            </div>
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
const currentTab = ref('info');

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
  if (!filters || Object.keys(filters).length === 0) {
    filteredCandidates.value = candidates.value;
    return;
  }

  filteredCandidates.value = candidates.value.filter(p => {
    if (!p.specifications) return false;

    // 1. 만약 specifications가 문자열(JSON)이면 객체로 변환, 아니면 그대로 사용
    let specsObj;
    try {
      specsObj = typeof p.specifications === 'string'
          ? JSON.parse(p.specifications)
          : p.specifications;
    } catch (e) {
      return false;
    }

    // 2. 필터 조건 검사
    return Object.keys(filters).every(key => {
      const selectedOptions = filters[key]; // 예: ["288cm", "180cm"]
      const productVal = specsObj[key];     // 이제 객체에서 키를 찾을 수 있음

      if (productVal === undefined || productVal === null) return false;

      // 상품의 값(productVal)이 선택된 옵션들 중 하나와 일치하는지 확인
      return selectedOptions.includes(String(productVal).trim());
    });
  });

  console.log("필터 후 상품 수:", filteredCandidates.value.length);
};

const executeSearch = async () => {
  if (!props.data) return;

  // 필터링된 결과가 있는지 확인
  if (filteredCandidates.value.length === 0) {
    alert("선택한 조건에 맞는 상품이 없습니다. 필터를 조정해주세요.");
    return;
  }

  loading.value = true;
  searchResult.value = [];

  try {
    // 중요: candidates가 아니라 'filteredCandidates'를 사용해야 합니다.
    const filterIdList = filteredCandidates.value.map(p => String(p.productCode));

    console.log("서버로 보내는 필터링된 상품 개수:", filterIdList.length);

    const res = await axios.post('http://localhost:8083/api/similar-images', {
      pcode: String(props.data.productCode),
      top: 10,
      filters: filterIdList // 이 리스트 안의 상품들 중에서만 유사도를 계산하게 됩니다.
    });

    if (res.data.success) {
      searchResult.value = res.data.similarImages;
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

const serverSpecs = ref({}); // 서버에서 받아온 스펙 필터 데이터

onMounted(async () => {
  initChart();

  // 1. 현재 상품의 카테고리 이름을 가져옴 (예: "소파")
  const categoryName = getCategoryName(props.data.categoryId);

  try {
    // 2. 백엔드에서 해당 카테고리의 정제된 스펙 데이터 로드
    const specRes = await axios.get('http://localhost:8083/api/category/specs', {
      params: { category: categoryName }
    });
    serverSpecs.value = specRes.data.specs; // { "가로": [...], "재질": [...] }

    // 3. 검색 대상 전체 상품 리스트 로드 (기존 로직 유지)
    const listUrl = props.isNewDb ? 'http://localhost:8083/api/new/product' : 'http://localhost:8083/api/product';
    const res = await axios.get(listUrl, { params: { size: 1000 } });
    candidates.value = res.data;
    filteredCandidates.value = res.data;

  } catch (e) {
    console.error("데이터 로드 실패", e);
  }
});

// 카테고리 ID -> 한글명 변환 (백엔드 로직과 맞춤)
const getCategoryName = (categoryId) => {
  if (categoryId === 112756) return "데스크탑";
  if (categoryId === 15236036) return "소파";
  if (categoryId === 18234911) return "패딩";
  if (categoryId === 18242355) return "신발";
  return "기타";
};

watch(() => props.data, () => {
  initChart();
});

watch(currentTab, async (newTab) => {
  if (newTab === 'info') {
    await nextTick();
    initChart();
  }
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
.tab-menu {
  display: flex;
  gap: 40px; /* 버튼 사이 간격 넓힘 */
  margin-bottom: 30px;
  border-bottom: 1px solid #eee; /* 선 두께 줄임 */
  padding-left: 5px;
}

.tab-btn {
  padding: 15px 0; /* 좌우 패딩 제거 */
  border: none;
  background: none;
  font-size: 14px; /* 폰트 사이즈 살짝 줄임 */
  font-weight: 600;
  letter-spacing: 1px; /* 글자 간격 넓혀서 고급스럽게 */
  color: #bbb; /* 기본 컬러는 연하게 */
  cursor: pointer;
  position: relative;
  transition: color 0.3s ease;
  text-transform: uppercase; /* 영문일 경우 대문자화 (선택 사항) */
}

/* 마우스를 올렸을 때 */
.tab-btn:hover {
  color: #333;
}

/* 활성화 상태 (Active) */
.tab-btn.active {
  color: #333; /* 활성화 시 진하게 */
}

/* 활성화 시 하단 라인 애니메이션 */
.tab-btn::after {
  content: '';
  position: absolute;
  bottom: -1px; /* border-bottom과 겹치게 */
  left: 0;
  width: 0;
  height: 2px;
  background: #333; /* 강조 색상은 차분한 블랙 또는 딥그린 */
  transition: width 0.3s ease;
}

.tab-btn.active::after {
  width: 100%; /* 활성화 시 라인이 쓱 그어짐 */
}

/* 탭 콘텐츠 영역 애니메이션 */
.tab-content {
  margin-top: 20px;
  min-height: 400px;
}
</style>