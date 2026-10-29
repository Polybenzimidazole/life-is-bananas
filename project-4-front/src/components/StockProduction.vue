
<template>
  <div class="production-card">
    <h2>Recipe Production</h2>

    <form @submit.prevent="submitProduction" class="production-form">
      <div class="form-group">
        <label for="recipeSelect">Select Recipe:</label>

        <select
            id="recipeSelect"
            v-model="selectedRecipeId"
            required
            class="form-control"
        >
          <option value="" disabled>-- Select a recipe --</option>

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
        <label for="quantityInput">Portions to Prepare:</label>

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
          :disabled="loading"
      >
        {{ loading ? 'Processing...' : 'Deduct Ingredients from Stock' }}
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
        getRecipes: 'http://localhost:8080/recipes/all',
        useStock: 'http://localhost:8080/stocks/use',
        csrf: 'http://localhost:8080/recipes/csrf'
      }
    }
  },

  mounted() {
    this.fetchRecipeList()
  },

  methods: {
    async fetchRecipeList() {
      try {
        const response = await axios.get(
            this.endpoints.getRecipes,
            {
              withCredentials: true,
              headers: {
                Accept: 'application/json'
              }
            }
        )

        if (!Array.isArray(response.data)) {
          throw new Error('The server did not return a recipe list.')
        }

        this.recipeList = response.data

      } catch (error) {
        console.error('Error fetching recipes:', error)

        this.errorMessage =
            'Failed to load recipes. HTTP status: ' +
            (error.response?.status || 'unknown')
      }
    },

    async submitProduction() {
      this.errorMessage = ''
      this.successMessage = ''

      if (!this.selectedRecipeId || this.preparationQuantity < 1) {
        this.errorMessage = 'Select a recipe and a valid quantity.'
        return
      }

      this.loading = true

      try {
        // Stap 1: CSRF-token ophalen
        const csrfResponse = await axios.get(
            this.endpoints.csrf,
            {
              withCredentials: true,
              headers: {
                Accept: 'application/json'
              }
            }
        )

        const csrf = csrfResponse.data

        if (!csrf || !csrf.token || !csrf.headerName) {
          throw new Error('Invalid CSRF token received.')
        }

        // Stap 2: Productiegegevens samenstellen
        const payload = {
          recipeId: Number(this.selectedRecipeId),
          quantity: Number(this.preparationQuantity)
        }

        console.log('Production payload:', payload)

        // Stap 3: Stock verminderen
        await axios.post(
            this.endpoints.useStock,
            payload,
            {
              withCredentials: true,
              headers: {
                'Content-Type': 'application/json',
                [csrf.headerName]: csrf.token
              }
            }
        )

        // Stap 4: Gebruiker informeren
        this.successMessage = 'Stock deducted successfully!'

        // Laat de parent-component de stocklijst vernieuwen
        this.$emit('stock-updated')

        this.selectedRecipeId = ''
        this.preparationQuantity = 1

      } catch (error) {
        console.error('Error deducting stock:', error)
        console.error('HTTP status:', error.response?.status)
        console.error('Server response:', error.response?.data)

        const status = error.response?.status

        if (status === 400) {
          this.errorMessage =
              'HTTP 400: Invalid production data.'
        } else if (status === 401) {
          this.errorMessage =
              'HTTP 401: You must log in first.'
        } else if (status === 403) {
          this.errorMessage =
              'HTTP 403: Access denied. Check ADMIN role and CSRF token.'
        } else if (status === 404) {
          this.errorMessage =
              'HTTP 404: Production endpoint not found.'
        } else if (status === 500) {
          this.errorMessage =
              'HTTP 500: Server error while processing production.'
        } else {
          this.errorMessage =
              'Failed to process stock deduction: ' + error.message
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
