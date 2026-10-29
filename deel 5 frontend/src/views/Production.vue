<template>
  <div class="stock-page">
    <h1>Inventory & Production</h1>

    <div class="components-layout">
      <div class="layout-section">
        <StockList ref="stockListComp" />
      </div>

      <div class="layout-section">
        <StockProduction @stock-updated="onStockUpdated" />
      </div>
    </div>
  </div>
</template>

<script>
import StockList from '@/components/StockList.vue'
import StockProduction from '@/components/StockProduction.vue'

export default {
  name: 'Production',
  components: {
    StockList,
    StockProduction
  },
  methods: {
    onStockUpdated() {
      this.$refs.stockListComp.fetchStockList()
    }
  }
}
</script>

<style scoped>
.stock-page {
  max-width: 1100px;
  margin: 0 auto;
  padding: 20px;
}

.components-layout {
  display: flex;
  gap: 30px;
  margin-top: 20px;
}

.layout-section {
  flex: 1;
}
</style>