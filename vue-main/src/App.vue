<template>
  <div class="app">
    <ProductList
        v-if="view === 'list'"
        :initialCategory="currentCategory"
        @view-detail="loadDetail"
        @update-category="currentCategory = $event"
    />

    <ProductDetail
        v-else-if="view === 'detail' && detailData"
        :data="detailData"
        @go-back="view = 'list'"
    />
  </div>
</template>

<script setup>
import { ref } from 'vue';
import axios from 'axios';
import ProductList from './components/ProductList.vue';
import ProductDetail from './components/ProductDetail.vue';

const view = ref('list');
const detailData = ref(null);
// 2. 현재 카테고리 상태를 부모에서 관리 (기본값 데스크탑)
const currentCategory = ref('데스크탑');

const loadDetail = async (code, isNew) => {
  const apiPath = isNew ? '/api/new/product' : '/api/product';
  const res = await axios.get(`http://localhost:8083${apiPath}/${code}`);
  detailData.value = res.data;
  view.value = 'detail';
};

</script>