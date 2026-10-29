
<template>
  <div class="production-card">
    <h2>Recipe Production</h2>

    <form
        @submit.prevent="submitProduction"
        class="production-form"
    >
      <div class="form-group">
        <label for="recipeSelect">Select Recipe:</label>

        <select
            id="recipeSelect"
            v-model="selectedRecipeId"
            required
            class="form-control"
        >
          <option value="" disabled>
            -- Select a recipe --
          </option>

          <option
              v-for="recipe in recipeList"
              :key="recipe.id"
              :value="recipe.id"
          >
            {{ recipe.title }}
          </option>
        </select>
      </div>

      <div class="form-group">
        <label for="quantityInput">
          Portions to Prepare:
        </label>

        <input
            id="quantityInput"
            v-model.number="preparationQuantity"
            type="number"
            min="1"
            required
            class="form-control"
        />
      </div>

      <button
          type="submit"
          class="btn btn-submit"
          :disabled="loading || recipeList.length === 0"
      >
        {{ loading
          ? 'Processing...'
          : 'Deduct Ingredients from Stock'
        }}
      </button>
    </form>

    <p v-if="errorMessage" class="error-message">
      {{ errorMessage }}
    </p>

    <p v-if="successMessage" class="success-message">
      {{ successMessage }}
    </p>
  </div>
</template>

<script>
import axios from 'axios'
import { getAccessToken } from '@/services/auth'

export default {
  name: 'StockProduction',

  data() {
    return {
      recipeList: [],
      selectedRecipeId: '',
      preparationQuantity: 1,
      loading: false,
      errorMessage: '',
      successMessage: '',

      endpoints: {
        getRecipes: '/api/recipes/all',
        useStock: '/api/stocks/use'
      }
    }
  },

  mounted() {
    this.fetchRecipeList()
  },

  methods: {
    authConfig() {
      const token = getAccessToken()

      if (!token) {
        throw new Error(
            'No access token found. Please log in first.'
        )
      }

      return {
        headers: {
          Authorization: `Bearer ${token}`,
          Accept: 'application/json'
        }
      }
    },

    async fetchRecipeList() {
      this.errorMessage = ''

      try {
        const response = await axios.get(
            this.endpoints.getRecipes,
            this.authConfig()
        )

        if (!Array.isArray(response.data)) {
          throw new Error(
              'The server did not return a recipe list.'
          )
        }

        this.recipeList = response.data

        console.log(
            'Production recipes loaded:',
            response.data.length
        )

      } catch (error) {
        console.error('Error fetching recipes:', error)

        if (error.response?.status === 401) {
          this.errorMessage =
              'HTTP 401: Please log in again.'
        } else if (error.response?.status === 403) {
          this.errorMessage =
              'HTTP 403: Access denied.'
        } else {
          this.errorMessage =
              error.message || 'Failed to load recipes.'
        }
      }
    },

    async submitProduction() {
      this.errorMessage = ''
      this.successMessage = ''

      if (
          !this.selectedRecipeId ||
          !Number.isInteger(this.preparationQuantity) ||
          this.preparationQuantity < 1
      ) {
        this.errorMessage =
            'Select a recipe and a valid quantity.'
        return
      }

      this.loading = true

      try {
        const payload = {
          recipeId: Number(this.selectedRecipeId),
          quantity: Number(this.preparationQuantity)
        }

        console.log('Production payload:', payload)

        await axios.post(
            this.endpoints.useStock,
            payload,
            {
              ...this.authConfig(),
              headers: {
                ...this.authConfig().headers,
                'Content-Type': 'application/json'
              }
            }
        )

        this.successMessage =
            'Stock deducted successfully!'

        this.$emit('stock-updated')

        this.selectedRecipeId = ''
        this.preparationQuantity = 1

      } catch (error) {
        console.error('Error deducting stock:', error)

        const status = error.response?.status

        if (status === 400) {
          this.errorMessage =
              'HTTP 400: Invalid production data.'
        } else if (status === 401) {
          this.errorMessage =
              'HTTP 401: Please log in again.'
        } else if (status === 403) {
          this.errorMessage =
              'HTTP 403: Admin permissions required.'
        } else if (status === 404) {
          this.errorMessage =
              'HTTP 404: Production endpoint not found.'
        } else if (status === 500) {
          this.errorMessage =
              'HTTP 500: Server error while processing production.'
        } else {
          this.errorMessage =
              error.message ||
              'Failed to process stock deduction.'
        }
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.production-card {
  background-color: #f8f9fa;
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 20px;
}

.production-form {
  display: flex;
  flex-direction: column;
  gap: 15px;
  margin-top: 15px;
}

.form-group {
  display: flex;
  flex-direction: column;
  text-align: left;
}

.form-group label {
  font-weight: bold;
  margin-bottom: 5px;
}

.form-control {
  padding: 8px;
  border: 1px solid #ccc;
  border-radius: 4px;
}

.btn-submit {
  background-color: #007bff;
  color: white;
  border: none;
  padding: 10px;
  border-radius: 4px;
  cursor: pointer;
  font-weight: bold;
}

.btn-submit:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.error-message {
  color: #dc3545;
  margin-top: 10px;
}

.success-message {
  color: #28a745;
  margin-top: 10px;
}
</style>
