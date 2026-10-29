
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

    <button
        class="refresh-button"
        :disabled="loading"
        @click="fetchStocks"
    >
      {{ loading ? 'Loading...' : 'Refresh Stock' }}
    </button>
  </div>
</template>

<script>
import axios from 'axios'
import { getAccessToken } from '@/services/auth'

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
        const token = getAccessToken()

        if (!token) {
          throw new Error(
              'No access token found. Please log in first.'
          )
        }

        const response = await axios.get(
            'http://localhost:8080/stocks/all',
            {
              headers: {
                Authorization: `Bearer ${token}`,
                Accept: 'application/json'
              }
            }
        )

        if (!Array.isArray(response.data)) {
          throw new Error(
              'The server did not return a stock list.'
          )
        }

        this.stocks = response.data

        console.log(
            'Stock successfully loaded:',
            response.data.length,
            'items'
        )

      } catch (error) {
        console.error('Error loading stock:', error)

        if (error.response?.status === 401) {
          this.errorMessage =
              'Unauthorized (401). Please log in again.'
        } else if (error.response?.status === 403) {
          this.errorMessage =
              'Access denied (403). Insufficient permissions.'
        } else {
          this.errorMessage =
              error.message || 'Unable to load stock from server.'
        }

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

.refresh-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.error {
  color: #dc3545;
  font-weight: bold;
}
</style>
