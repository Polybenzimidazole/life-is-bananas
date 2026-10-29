
<template>
  <div class="stock-list-card">
    <h2>Current Stock</h2>

    <p v-if="loading">Loading stock...</p>

    <p v-if="errorMessage" class="error">
      {{ errorMessage }}
    </p>

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

        <td>
          <strong>{{ item.quantity }}</strong>
          {{ item.unit }}
        </td>

        <td>
          <button
              class="btn btn-add"
              :disabled="addingId === item.id"
              @click="addStockQuantity(item)"
          >
            {{ addingId === item.id ? 'Adding...' : '+ 100 Add' }}
          </button>
        </td>
      </tr>

      <tr v-if="!loading && stockList.length === 0">
        <td colspan="3">No stock items found.</td>
      </tr>
      </tbody>
    </table>
  </div>
</template>

<script>
import axios from 'axios'
import {
  getAccessToken,
  login
} from '@/services/auth'

export default {
  name: 'StockList',

  data() {
    return {
      stockList: [],
      loading: false,
      addingId: null,
      errorMessage: '',

      endpoints: {
        getAll: '/api/stocks/all',
        add: '/api/stocks/add/'
      }
    }
  },

  mounted() {
    this.fetchStockList()
  },

  methods: {
    authConfig() {
      const token = getAccessToken()

      if (!token) {
        throw new Error('NO_ACCESS_TOKEN')
      }

      return {
        headers: {
          Authorization: `Bearer ${token}`,
          Accept: 'application/json'
        }
      }
    },

    async fetchStockList() {
      this.loading = true
      this.errorMessage = ''

      try {
        const response = await axios.get(
            this.endpoints.getAll,
            this.authConfig()
        )

        if (!Array.isArray(response.data)) {
          throw new Error(
              'Server did not return a valid stock list.'
          )
        }

        this.stockList = response.data

      } catch (error) {
        console.error('Failed to load stock:', error)

        if (error.message === 'NO_ACCESS_TOKEN') {
          this.errorMessage =
              'You are not logged in. Please log in first.'
        } else if (error.response?.status === 401) {
          this.errorMessage =
              'Unauthorized (401). Your access token was rejected.'
        } else if (error.response?.status === 403) {
          this.errorMessage =
              'Forbidden (403). You do not have permission.'
        } else {
          this.errorMessage =
              error.message || 'Unable to load stock from server.'
        }

      } finally {
        this.loading = false
      }
    },

    async addStockQuantity(item) {
      this.errorMessage = ''
      this.addingId = item.id

      try {
        await axios.post(
            `${this.endpoints.add}${item.id}`,
            { amount: 100 },
            this.authConfig()
        )

        await this.fetchStockList()

      } catch (error) {
        console.error('Failed to add stock:', error)

        if (error.message === 'NO_ACCESS_TOKEN') {
          this.errorMessage =
              'You are not logged in. Please log in first.'
        } else if (error.response?.status === 401) {
          this.errorMessage =
              'Unauthorized (401). Please log in again.'
        } else if (error.response?.status === 403) {
          this.errorMessage =
              'Forbidden (403). Admin permissions required.'
        } else {
          this.errorMessage =
              error.message || 'Failed to add stock.'
        }

      } finally {
        this.addingId = null
      }
    },

    async startLogin() {
      try {
        await login()
      } catch (error) {
        console.error('Failed to start OAuth login:', error)
        this.errorMessage = 'Unable to start login.'
      }
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

.stock-table th,
.stock-table td {
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

.btn-add:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.error {
  color: #dc3545;
  margin-top: 10px;
}
</style>
