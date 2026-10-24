<template>
  <div class="stock-list-card">
    <h2>Current Stock</h2>

    <table class="stock-table">
      <thead>
      <tr>
        <th>Ingredient</th>
        <th>Quantity</th>
        <th>Action</th>
      </tr>
      </thead>
      <tbody>
      <tr v-for="item in stockList" :key="item.id">
        <td>{{ item.name }}</td>
        <td><strong>{{ item.quantity }}</strong> {{ item.unit }}</td>
        <td>
          <button class="btn btn-add" @click="addStockQuantity(item)">
            + 100 Add
          </button>
        </td>
      </tr>
      <tr v-if="stockList.length === 0">
        <td colspan="3">No stock items found.</td>
      </tr>
      </tbody>
    </table>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'StockList',
  data() {
    return {
      stockList: [],
      endpoints: {
        getAll:  'http://localhost:8080/stock/all',
        add:  'http://localhost:8080/stock/add'
      }    }
  },
  mounted() {
    this.fetchStockList()
  },
  methods: {
    fetchStockList() {
      axios.get(this.endpoint.getAll, { withCredentials: true })
          .then(response => {
            this.stockList = response.data
          })
    },

    addStockQuantity(item) {
      axios.post(`${this.endpoint.add}${item.id}`, { amount: 50 }, { withCredentials: true })
          .then(() => {
            item.quantity = response.data.quantity;
          })
    }
  }
}
</script>

<style scoped>
.stock-list-card {
  background-color: #f8f9fa;
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 20px;
}

.stock-table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 10px;
}

.stock-table th, .stock-table td {
  border: 1px solid #ddd;
  padding: 10px;
  text-align: left;
}

.stock-table th {
  background-color: #e9ecef;
}

.btn-add {
  background-color: #28a745;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
  font-weight: bold;
}
</style>