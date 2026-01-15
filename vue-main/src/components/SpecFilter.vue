<template>
  <div class="spec-filter" v-if="hasSpecs">

    <!-- 가로 (Width) 특별 처리 섹션 -->
    <div class="filter-group" v-if="widthRaw.length > 0">
      <div class="filter-title" @click="toggleSection('width')">
        <span class="toggle-arrow">{{ openSections.width ? '▼' : '▶' }}</span>
        가로
        <label class="precision-toggle" @click.stop>
          <input type="checkbox" v-model="isPrecisionMode"> 정밀
        </label>
      </div>

      <!-- 범위 모드 (Precision Off) -->
      <div class="filter-options" v-show="openSections.width && !isPrecisionMode && Object.keys(widthRanges).length > 0">
        <label v-for="(vals, label) in widthRanges" :key="label" class="filter-label">
          <input
              type="checkbox"
              @change="updateMultiValueFilter('가로', vals, $event.target.checked)"
          />
          {{ label }}
        </label>
      </div>

      <!-- 정밀 모드 (Precision On) -->
      <div class="filter-options" v-show="openSections.width && isPrecisionMode">
        <label v-for="opt in widthRaw" :key="opt" class="filter-label">
          <input
              type="checkbox"
              :value="opt"
              @change="updateFilter('가로', opt, $event.target.checked)"
          />
          {{ opt }}
        </label>
      </div>
    </div>

    <!-- 일반 스펙 (기존 방식) -->
    <div class="filter-group" v-for="(options, key) in normalSpecs" :key="key">
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

    <!-- 기타 스펙 (체크박스로 몰아넣기) -->
    <div class="filter-group" v-if="Object.keys(otherSpecs).length > 0">
      <div class="filter-title" @click="toggleSection('other')">
        <span class="toggle-arrow">{{ openSections.other ? '▼' : '▶' }}</span>
        기타
      </div>
      <div class="filter-options" v-show="openSections.other">
        <label v-for="(values, key) in otherSpecs" :key="key" class="filter-label">
          <input
              type="checkbox"
              @change="updateMultiValueFilter(key, values, $event.target.checked)"
          />
          {{ key }}
        </label>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, defineProps, defineEmits } from 'vue'

const props = defineProps({
  products: {
    type: Array,
    default: () => []
  },
  targetProduct: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['filter-change'])
const selectedSpecs = ref({})
const isPrecisionMode = ref(false) // 정밀 모드 토글
const openSections = ref({})  // 섹션 열림/닫힘 상태

// 섹션 토글 함수
const toggleSection = (key) => {
  openSections.value[key] = !openSections.value[key]
}

// 기타 항목으로 분류할 키 목록
const featureKeys = [
  '스윙기능', '스펀지(폼)', '헤드기능', '솜', '모션슬라이딩',
  '제로월', '생활방수', '발수', '이지클린',
  '무지', '플리스', '리버서블', '폴리에스터', '기모', '방풍',
  '1Gbps 유선', '1Gbps유선',
  'dp포트', 'DP포트',
  'led쿨러', 'LED쿨러',
  '어항형'
]

// 가로 길이 파싱 헬퍼 (cm 단위로 변환)
const parseWidthToCm = (val) => {
  if (!val) return 0
  const str = String(val).toLowerCase().replace(/\s+/g, '')
  const num = parseFloat(str)
  if (isNaN(num)) return 0

  if (str.includes('mm')) return num / 10
  if (str.includes('cm')) return num
  if (str.includes('m') && !str.includes('mm')) return num * 100

  // 단위가 없는 경우 추론
  if (num >= 500) return num / 10
  return num
}

const specs = computed(() => {
  const products = props.products
  const target = props.targetProduct

  const tempNormal = {} // 일반 스펙
  const tempOther = {}  // 기타 스펙 (Key -> Values)

  const tempWidthRaw = new Set() // 가로 원본 값들
  const tempWidthRanges = {}     // 가로 범위 그룹 ("100~120" -> [list of raw values])

  // 기준 상품의 키 목록 추출
  let targetKeys = null
  if (target && target.specifications) {
    try {
      const targetSpecObj = typeof target.specifications === 'string'
          ? JSON.parse(target.specifications)
          : target.specifications
      targetKeys = Object.keys(targetSpecObj)
    } catch (e) { /* ignore */ }
  }

  // 데이터 없으면 빈 객체 반환
  if (!products || products.length === 0) {
    return { normalSpecs: {}, otherSpecs: {}, widthRaw: [], widthRanges: {} }
  }

  // 제외할 키 목록
  const excludedKeys = [
    '제조회사', '제조사',
    '안전확인인증', '안전 확인인증', '안전확인 인증',
    '적합성평가인증', '적합성 평가인증', '적합성 평가 인증',
    '등록년월', '출시년월',
    'HDMI', '게임용'
  ]

  products.forEach(p => {
    if (!p.specifications) return
    try {
      const specObj = typeof p.specifications === 'string'
          ? JSON.parse(p.specifications)
          : p.specifications

      Object.keys(specObj).forEach(rawKey => {
        const key = rawKey.trim()

        // 기준 상품에 없으면 패스
        if (targetKeys && !targetKeys.some(tk => tk.trim() === key)) return

        // 제외 목록이면 패스
        if (excludedKeys.includes(key)) return

        const val = specObj[key]

        // 1. 가로 (Width) 처리
        if (key === '가로') {
          tempWidthRaw.add(val)

          const cm = parseWidthToCm(val)
          if (cm > 0) {
            // 20cm 단위 버킷팅 (100, 120, 140...)
            const lower = Math.floor(cm / 20) * 20
            const upper = lower + 20
            const label = `${lower}~${upper}`

            if (!tempWidthRanges[label]) tempWidthRanges[label] = new Set()
            tempWidthRanges[label].add(val)
          }
          return // 일반 처리 건너뜀
        }

        // 2. 기타 항목 처리
        if (featureKeys.includes(key)) {
          if (!tempOther[key]) tempOther[key] = new Set()
          tempOther[key].add(val)
        } else {
          // 3. 일반 항목 처리
          if (!tempNormal[key]) tempNormal[key] = new Set()
          tempNormal[key].add(val)
        }
      })
    } catch (e) { /* ignore */ }
  })

  // 데이터 변환 (Set -> Array sorting)

  // Normal
  const finalNormal = {}
  Object.keys(tempNormal).forEach(key => {
    if (tempNormal[key].size >= 1) {
      finalNormal[key] = Array.from(tempNormal[key]).sort()
    }
  })

  // Other (Values keep as Array)
  const finalOther = {}
  Object.keys(tempOther).forEach(key => {
    finalOther[key] = Array.from(tempOther[key])
  })

  // Width
  const finalWidthRaw = Array.from(tempWidthRaw).sort()
  const finalWidthRanges = {}

  // 범위 키 정렬 (100~120, 120~140 순)
  Object.keys(tempWidthRanges).sort((a,b) => {
    const numA = parseInt(a.split('~')[0])
    const numB = parseInt(b.split('~')[0])
    return numA - numB
  }).forEach(rangeKey => {
    finalWidthRanges[rangeKey] = Array.from(tempWidthRanges[rangeKey])
  })

  return {
    normalSpecs: finalNormal,
    otherSpecs: finalOther,
    widthRaw: finalWidthRaw,
    widthRanges: finalWidthRanges
  }
})

// computed 분해
const normalSpecs = computed(() => specs.value.normalSpecs)
const otherSpecs = computed(() => specs.value.otherSpecs)
const widthRaw = computed(() => specs.value.widthRaw)
const widthRanges = computed(() => specs.value.widthRanges)

const hasSpecs = computed(() => {
  return widthRaw.value.length > 0 ||
      Object.keys(normalSpecs.value).length > 0 ||
      Object.keys(otherSpecs.value).length > 0
})


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
  emitChange()
}

// 다중 값 업데이트
const updateMultiValueFilter = (key, values, checked) => {
  if (!selectedSpecs.value[key]) {
    selectedSpecs.value[key] = new Set()
  }
  if (checked) {
    values.forEach(v => selectedSpecs.value[key].add(v))
  } else {
    values.forEach(v => selectedSpecs.value[key].delete(v))
    if (selectedSpecs.value[key].size === 0) {
      delete selectedSpecs.value[key]
    }
  }
  emitChange()
}

const emitChange = () => {
  const emitData = {}
  Object.keys(selectedSpecs.value).forEach(k => {
    emitData[k] = Array.from(selectedSpecs.value[k])
  })
  emit('filter-change', emitData)
}
</script>

<style scoped>
.spec-filter {
  margin-bottom: 20px;
  padding: 10px;
  background: #fdfdfd;
  border: 1px solid #eee;
  border-radius: 4px;
}
.filter-group {
  margin-bottom: 12px;
}
.filter-title {
  font-weight: bold;
  font-size: 13px;
  margin-bottom: 6px;
  color: #333;
  display: flex;
  align-items: center;
  cursor: pointer;
  user-select: none;
}
.toggle-arrow {
  margin-right: 6px;
  font-size: 10px;
  color: #666;
}
.precision-toggle {
  font-weight: normal;
  font-size: 11px;
  margin-left: 10px;
  cursor: pointer;
  color: #666;
}
.filter-options {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.filter-label {
  font-size: 12px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
}
</style>

여기 수정사항 있을까