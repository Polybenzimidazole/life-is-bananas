
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

export default {
  name: 'StockList',

  data() {
    return {
      stockList: [],
      loading: false,
      addingId: null,
      errorMessage: '',

      endpoints: {
        getAll: 'http://localhost:8080/stocks/all',
        add: 'http://localhost:8080/stocks/add/',
        csrf: 'http://localhost:8080/recipes/csrf'
      }
    }
  },

  mounted() {
    this.fetchStockList()
  },

  methods: {
    async fetchStockList() {
      this.loading = true
      this.errorMessage = ''

      try {
        const response = await axios.get(
            this.endpoints.getAll,
            {
              withCredentials: true,
              headers: {
                Accept: 'application/json'
              }
            }
        )

        if (!Array.isArray(response.data)) {
          throw new Error('Server did not return a stock list')
        }

        this.stockList = response.data

      } catch (error) {
        console.error('Failed to load stock:', error)
        this.errorMessage = 'Failed to load stock.'
      } finally {
        this.loading = false
      }
    },

    async addStockQuantity(item) {
      this.errorMessage = ''
      this.addingId = item.id

      try {
        const csrfResponse = await axios.get(
            this.endpoints.csrf,
            { withCredentials: true }
        )

        const csrf = csrfResponse.data

        if (!csrf.token || !csrf.headerName) {
          throw new Error('Invalid CSRF token')
        }

        await axios.post(
            `${this.endpoints.add}${item.id}`,
            { amount: 100 },
            {
              withCredentials: true,
              headers: {
                [csrf.headerName]: csrf.token
              }
            }
        )

        // Haal de actuele voorraad opnieuw op
        await this.fetchStockList()

      } catch (error) {
        console.error('Failed to add stock:', error)
        this.errorMessage =
            'Failed to add stock. Check your permissions and server logs.'
      } finally {
        this.addingId = null
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
