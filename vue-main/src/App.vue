<template>
  <div class="app">
    <ProductList
        v-if="view === 'list'"
        :products="products"
        @view-detail="loadDetail"
    />

    <ProductDetail
        v-else-if="view === 'detail' && detailData"
        :data="detailData"
        @go-back="view = 'list'"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';
import ProductList from './components/ProductList.vue';
import ProductDetail from './components/ProductDetail.vue';

const view = ref('list');
const products = ref([]);
const detailData = ref(null);

const loadDetail = async (code) => {
  const res = await axios.get(`/api/product/${code}`);
  detailData.value = res.data;
  view.value = 'detail';
};

onMounted(async () => {
  const res = await axios.get('/api/product');
  products.value = res.data;
});
</script>