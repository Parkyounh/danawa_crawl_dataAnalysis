<template>
  <div class="spec-filter" v-if="specs && Object.keys(specs).length > 0">

    <div class="filter-group" v-for="(options, key) in specs" :key="key">
      <div class="filter-title" @click="toggleSection(key)">
        <span class="toggle-arrow">{{ openSections[key] ? '▼' : '▶' }}</span>
        {{ key }}
      </div>

      <div class="filter-options" v-show="openSections[key]">
        <label v-for="opt in options" :key="opt" class="filter-label">
          <input
              type="checkbox"
              :value="opt"
              @change="updateFilter(key, opt, $event.target.checked)"
          />
          {{ opt }}
        </label>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, defineProps, defineEmits } from 'vue'

const props = defineProps({
  // 이제 products 대신 서버에서 정제된 specs(Map)를 직접 받습니다.
  specs: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits(['filter-change'])
const selectedSpecs = ref({})
const openSections = ref({})

const toggleSection = (key) => {
  openSections.value[key] = !openSections.value[key]
}

const updateFilter = (key, value, checked) => {
  if (!selectedSpecs.value[key]) {
    selectedSpecs.value[key] = new Set()
  }

  if (checked) {
    selectedSpecs.value[key].add(value)
  } else {
    selectedSpecs.value[key].delete(value)
    if (selectedSpecs.value[key].size === 0) {
      delete selectedSpecs.value[key]
    }
  }

  // 선택된 필터 정보를 부모에게 전달
  const emitData = {}
  Object.keys(selectedSpecs.value).forEach(k => {
    emitData[k] = Array.from(selectedSpecs.value[k])
  })
  emit('filter-change', emitData)
}
</script>

<style scoped>
.spec-filter { margin-bottom: 20px; padding: 10px; background: #fdfdfd; border: 1px solid #eee; border-radius: 4px; max-height: 400px; overflow-y: auto; }
.filter-group { margin-bottom: 12px; }
.filter-title { font-weight: bold; font-size: 13px; margin-bottom: 6px; color: #333; display: flex; align-items: center; cursor: pointer; user-select: none; }
.toggle-arrow { margin-right: 6px; font-size: 10px; color: #666; }
.filter-options { display: flex; flex-wrap: wrap; gap: 8px; }
.filter-label { font-size: 12px; cursor: pointer; display: flex; align-items: center; gap: 4px; background: #eee; padding: 2px 6px; border-radius: 4px; }
.filter-label:hover { background: #e0e0e0; }
</style>