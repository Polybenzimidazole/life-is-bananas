
<template>
  <div class="stock-container">
    <h2>Stock Management</h2>
    <p>Overview of available raw materials</p>

    <p v-if="loading">Loading stock...</p>

    <p v-if="errorMessage" class="error">
      {{ errorMessage }}
    </p>

    <table v-if="!loading && !errorMessage" class="stock-table">
      <thead>
      <tr>
        <th>ID</th>
        <th>Raw Material</th>
        <th>Quantity</th>
        <th>Unit</th>
      </tr>
      </thead>

      <tbody>
      <tr v-for="material in stocks" :key="material.id">
        <td>{{ material.id }}</td>
        <td>{{ material.name }}</td>
        <td>{{ material.quantity }}</td>
        <td>{{ material.unit }}</td>
      </tr>

      <tr v-if="stocks.length === 0">
        <td colspan="4">No stock available.</td>
      </tr>
      </tbody>
    </table>

    <button class="refresh-button" @click="fetchStocks">
      Refresh Stock
    </button>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'StockView',

  data() {
    return {
      stocks: [],
      loading: false,
      errorMessage: ''
    }
  },

  mounted() {
    this.fetchStocks()
  },

  methods: {
    async fetchStocks() {
      this.loading = true
      this.errorMessage = ''

      try {
        const response = await axios.get(
            'http://localhost:8080/stocks/all',
            {
              withCredentials: true,
              headers: {
                Accept: 'application/json'
              }
            }
        )

        if (!Array.isArray(response.data)) {
          throw new Error('The server did not return a stock list.')
        }

        this.stocks = response.data

      } catch (error) {
        console.error('Error loading stock:', error)
        this.errorMessage = 'Unable to load stock from server.'
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.stock-container {
  max-width: 900px;
  margin: 30px auto;
  padding: 20px;
}

.stock-table {
  width: 100%;
  border-collapse: collapse;
  margin: 20px 0;
}

.stock-table th,
.stock-table td {
  border: 1px solid #ddd;
  padding: 12px;
  text-align: left;
}

.stock-table th {
  background-color: #f4f4f4;
}

.stock-table tbody tr:hover {
  background-color: #f9f9f9;
}

.refresh-button {
  background-color: #007bff;
  color: white;
  border: none;
  padding: 10px 16px;
  border-radius: 5px;
  cursor: pointer;
}

.refresh-button:hover {
  background-color: #0056b3;
}

.error {
  color: #dc3545;
  font-weight: bold;
}
</style>
