<template>
  <div class="detail-wrapper">
    <header class="detail-header">
      <button @click="$emit('go-back')" class="back-btn">← 목록으로 돌아가기</button>
    </header>

    <div class="content-container" v-if="data">
      <div class="left-panel">
        <h2 class="title" v-if="data">상품 상세 정보</h2>
        <div class="image-box">
          <img :src="`http://localhost:8083/danawa_db_image/${data.categoryId}/${data.productCode}.jpg`"
                />
        </div>
      </div>

      <div class="right-panel">
        <h1 class="product-name">{{ data.name }}</h1>
        <p class="product-code">상품코드: {{ data.productCode }}</p>

        <div class="spec-section" v-if="data.specifications">
          <h3>상세 스펙</h3>
          <div class="spec-grid">
            <div v-for="(val, key) in data.specifications" :key="key" class="spec-item">
              <span class="spec-key">{{ key }}</span>
              <span class="spec-val">{{ val }}</span>
            </div>
          </div>
        </div>

        <div class="chart-section">
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
import { onMounted, defineProps, defineEmits, nextTick } from 'vue';
import Chart from 'chart.js/auto';

const props = defineProps(['data']);
const emit = defineEmits(['go-back']);

onMounted(async () => {
  if (!props.data || !props.data.priceHistory) return;

  // DOM이 완전히 렌더링된 후 차트를 그리기 위해 nextTick 사용
  await nextTick();

  const canvas = document.getElementById('priceChart');
  if (!canvas) return;

  const ctx = canvas.getContext('2d');

  // 데이터 정렬 (날짜순)
  const sortedHistory = [...props.data.priceHistory].sort((a, b) => new Date(a.date) - new Date(b.date));
  const labels = sortedHistory.map(h => h.date);
  const prices = sortedHistory.map(h => h.price);

  new Chart(ctx, {
    type: 'line',
    data: {
      labels: labels,
      datasets: [{
        label: '최저가 추이 (원)',
        data: prices,
        borderColor: '#42b983',
        backgroundColor: 'rgba(66, 185, 131, 0.1)',
        fill: true,
        tension: 0.3,
        pointRadius: 4,
        pointHoverRadius: 6,
        pointBackgroundColor: '#42b983'
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: { display: false },
        tooltip: {
          padding: 10,
          callbacks: {
            label: (context) => ` ${context.parsed.y.toLocaleString()}원`
          }
        }
      },
      scales: {
        y: {
          beginAtZero: false,
          ticks: {
            callback: (value) => value.toLocaleString() + '원'
          }
        }
      }
    }
  });
});
</script>

<style scoped>
.detail-wrapper { padding: 30px; max-width: 1200px; margin: 0 auto; background: #fff; min-height: 100vh; }

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  border-bottom: 1px solid #eee;
  padding-bottom: 20px;
}

.back-btn {
  padding: 10px 20px;
  border: 1px solid #42b983;
  background: #fff;
  color: #42b983;
  border-radius: 8px;
  cursor: pointer;
  font-weight: bold;
}

.back-btn:hover { background: #42b983; color: white; }

.content-container { display: flex; gap: 60px; }

.left-panel { flex: 0 0 450px; }
.image-box { margin-top: 50px; }
.image-box img {
  width: 100%;
  border-radius: 15px;
  border: 1px solid #f0f0f0;
  box-shadow: 0 10px 30px rgba(0,0,0,0.05);
}

.right-panel { flex: 1; }
.product-name { font-size: 24px; font-weight: 800; margin-bottom: 15px; color: #2c3e50; }
.product-code { color: #aaa; margin-bottom: 30px; font-size: 14px; }

/* 섹션 타이틀 공통 스타일 */
h3 { font-size: 18px; margin-bottom: 20px; color: #333; border-left: 4px solid #42b983; padding-left: 12px; }

/* 스펙 섹션 (위) */
.spec-section { margin-bottom: 40px; }
.spec-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  background: #fff;
  border: 1px solid #eee;
  padding: 20px;
  border-radius: 12px;
}
.spec-item { display: flex; border-bottom: 1px solid #f5f5f5; padding: 10px 0; font-size: 13px; }
.spec-key { font-weight: bold; width: 110px; color: #7f8c8d; }
.spec-val { color: #2c3e50; flex: 1; }

/* 그래프 섹션 (아래) */
.chart-section { background: #fafafa; padding: 30px; border-radius: 16px; border: 1px solid #f0f0f0; }
.canvas-holder { height: 300px; width: 100%; }
</style>