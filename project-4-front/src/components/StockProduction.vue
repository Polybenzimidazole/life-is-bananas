<template>
  <div class="production-card">
    <h2>Recipe Production</h2>

    <form @submit.prevent="submitProduction" class="production-form">
      <!-- Choix de la recette -->
      <div class="form-group">
        <label for="recipeSelect">Select Recipe:</label>
        <select id="recipeSelect" v-model="selectedRecipeId" required class="form-control">
          <option value="" disabled>-- Select a recipe --</option>
          <option v-for="recipe in recipeList" :key="recipe.id" :value="recipe.id">
            {{ recipe.title }}
          </option>
        </select>
      </div>

      <div class="form-group">
        <label for="quantityInput">Portions to Prepare:</label>
        <input
            type="number"
            id="quantityInput"
            v-model.number="preparationQuantity"
            min="1"
            required
            class="form-control"
        />
      </div>

      <button type="submit" class="btn btn-submit">
        Deduct Ingredients from Stock
      </button>
    </form>

    <p v-if="errorMessage" class="error-message">{{ errorMessage }}</p>
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
      errorMessage: '',
      endpoints: {
        getRecipes: 'http://localhost:8080/recipes/all',
        useStock: 'http://localhost:8080/stocks/use'
      }
    }
  },
  mounted() {
    this.fetchRecipeList()
  },
  methods: {
    fetchRecipeList() {
      axios.get(this.endpoints.getRecipes, { withCredentials: true })
          .then(response => {
            this.recipeList = response.data
          })
          .catch(error => {
            console.error("Error fetching recipes:", error)
          })
    },

    submitProduction() {
      const payload = {
        recipeId: this.selectedRecipeId,
        quantity: this.preparationQuantity
      }

      axios.post(this.endpoints.useStock, payload, { withCredentials: true })
          .then(() => {
            alert('Stock deducted successfully!')
            // Signaler au composant parent de rafraîchir le tableau des stocks
            this.$emit('stock-updated')
            this.selectedRecipeId = ''
            this.preparationQuantity = 1
          })
          .catch(error => {
            console.error("Error deducting stock:", error)
            this.errorMessage = "Failed to process stock deduction."
          })
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

.error-message {
  color: red;
  margin-top: 10px;
}
</style>