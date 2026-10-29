
<template>
  <div class="recipes-table-container">

    <p v-if="errorMessage" class="error">
      {{ errorMessage }}
    </p>

    <table class="recipes-table">
      <thead>
      <tr>
        <th>ID</th>
        <th>Title</th>
        <th>Ingredients</th>
        <th>Actions</th>
      </tr>
      </thead>

      <tbody>
      <tr
          v-for="(recipe, index) in recipes"
          :key="recipe.id || index"
      >
        <td>{{ recipe.id }}</td>
        <td>{{ recipe.title }}</td>

        <td>
          <div
              v-for="(ingredient, ingredientIndex) in
                (recipe.recipeingredients || [])"
              :key="ingredient.id || ingredientIndex"
          >
            {{ ingredientName(ingredient) }}:
            {{ ingredient.quantity }}
            {{ ingredientUnit(ingredient) }}
          </div>
        </td>

        <td class="action-buttons">
          <button
              class="btn btn-edit"
              @click="editRecipe(recipe)"
          >
            Edit
          </button>

          <button
              class="btn btn-delete"
              @click="deleteRecipe(recipe.id)"
          >
            Delete
          </button>
        </td>
      </tr>

      <tr v-if="recipes.length === 0">
        <td colspan="4">No recipes found.</td>
      </tr>
      </tbody>
    </table>

    <div class="add-section">
      <button class="btn btn-add" @click="openAddForm">
        + Add Recipe
      </button>
    </div>

    <div v-if="showForm" class="recipe-form-card">

      <h3 v-if="currentRecipe.id === 0">
        Add New Recipe
      </h3>

      <h3 v-else>
        Edit Recipe #{{ currentRecipe.id }}
      </h3>

      <form @submit.prevent="saveRecipe">

        <div class="form-group">
          <label for="title">Title:</label>

          <input
              id="title"
              type="text"
              v-model="currentRecipe.title"
              required
          />
        </div>

        <div class="form-group">
          <label>Ingredients:</label>

          <div
              v-for="(ingredient, index) in currentRecipe.ingredients"
              :key="index"
              class="ingredient-row"
          >
            <select
                v-model.number="ingredient.rawMaterialId"
                required
            >
              <option disabled value="">
                Select ingredient
              </option>

              <option
                  v-for="material in rawMaterials"
                  :key="material.id"
                  :value="material.id"
              >
                {{ material.name }} ({{ material.unit }})
              </option>
            </select>

            <input
                type="number"
                v-model.number="ingredient.quantity"
                min="0.001"
                step="any"
                required
                placeholder="Quantity"
            />

            <button
                type="button"
                @click="removeIngredient(index)"
            >
              Remove
            </button>
          </div>

          <button type="button" @click="addIngredient">
            + Add Ingredient
          </button>
        </div>

        <div class="form-actions">
          <button type="submit" class="btn btn-save">
            {{ currentRecipe.id === 0 ? 'Create' : 'Update' }}
          </button>

          <button
              type="button"
              class="btn btn-cancel"
              @click="closeForm"
          >
            Cancel
          </button>
        </div>

      </form>
    </div>
  </div>
</template>

<script>
import axios from 'axios'
import { getAccessToken } from '@/services/auth'

export default {
  name: 'RecipesTable',

  data() {
    return {
      recipes: [],
      rawMaterials: [],
      showForm: false,
      errorMessage: '',

      currentRecipe: {
        id: 0,
        title: '',
        ingredients: []
      },

      endpoints: {
        getAll: '/api/recipes/all',
        create: '/api/recipes/add',
        update: '/api/recipes/update',
        delete: '/api/recipes/delete',
        getRawMaterials: '/api/stocks/all'
      }
    }
  },

  mounted() {
    this.fetchRecipes()
    this.fetchRawMaterials()
  },

  methods: {

    // OAuth 2.0: voeg het access token toe
    authConfig() {
      const token = getAccessToken()

      if (!token) {
        throw new Error(
            'Geen access token beschikbaar. Log eerst in.'
        )
      }

      return {
        headers: {
          Authorization: `Bearer ${token}`
        }
      }
    },

    // Alle recepten ophalen
    async fetchRecipes() {
      try {
        const response = await axios.get(
            this.endpoints.getAll,
            this.authConfig()
        )

        console.log('Ontvangen recepten:', response.data)

        if (!Array.isArray(response.data)) {
          throw new Error(
              'De API gaf geen receptenlijst terug.'
          )
        }

        this.recipes = response.data

      } catch (error) {
        console.error('Error loading recipes:', error)
        console.error(
            'HTTP-status:',
            error.response?.status
        )

        this.errorMessage =
            'Unable to load recipes from server.'
      }
    },

    // Grondstoffen ophalen
    async fetchRawMaterials() {
      try {
        const response = await axios.get(
            this.endpoints.getRawMaterials,
            this.authConfig()
        )

        console.log(
            'Ontvangen grondstoffen:',
            response.data
        )

        this.rawMaterials = Array.isArray(response.data)
            ? response.data
            : []

      } catch (error) {
        console.error(
            'Error loading raw materials:',
            error
        )

        console.error(
            'HTTP-status:',
            error.response?.status
        )

        this.errorMessage =
            'Unable to load raw materials.'
      }
    },

    ingredientName(ingredient) {
      return (
          ingredient.name ||
          (ingredient.rawMaterial &&
              ingredient.rawMaterial.name) ||
          (ingredient.rawmaterial &&
              ingredient.rawmaterial.name) ||
          'Unknown ingredient'
      )
    },

    ingredientUnit(ingredient) {
      return (
          ingredient.unit ||
          (ingredient.rawMaterial &&
              ingredient.rawMaterial.unit) ||
          (ingredient.rawmaterial &&
              ingredient.rawmaterial.unit) ||
          ''
      )
    },

    ingredientMaterialId(ingredient) {
      return (
          ingredient.rawMaterialId ||
          (ingredient.rawMaterial &&
              ingredient.rawMaterial.id) ||
          (ingredient.rawmaterial &&
              ingredient.rawmaterial.id) ||
          ''
      )
    },

    addIngredient() {
      this.currentRecipe.ingredients.push({
        rawMaterialId: '',
        quantity: 1
      })
    },

    removeIngredient(index) {
      this.currentRecipe.ingredients.splice(index, 1)
    },

    openAddForm() {
      this.currentRecipe = {
        id: 0,
        title: '',
        ingredients: []
      }

      this.errorMessage = ''
      this.showForm = true
    },

    editRecipe(recipe) {
      this.currentRecipe = {
        id: recipe.id,
        title: recipe.title,

        ingredients: (
            recipe.recipeingredients || []
        ).map(ingredient => ({
          rawMaterialId:
              this.ingredientMaterialId(ingredient),
          quantity: ingredient.quantity
        }))
      }

      this.errorMessage = ''
      this.showForm = true
    },

    // Recept toevoegen of aanpassen
    async saveRecipe() {
      this.errorMessage = ''

      try {
        const config = this.authConfig()

        if (this.currentRecipe.id === 0) {
          await axios.post(
              this.endpoints.create,
              this.currentRecipe,
              config
          )
        } else {
          const updateUrl =
              `${this.endpoints.update}/${this.currentRecipe.id}`

          await axios.put(
              updateUrl,
              this.currentRecipe,
              config
          )
        }

        this.closeForm()
        await this.fetchRecipes()

      } catch (error) {
        console.error('Error saving recipe:', error)

        console.error(
            'HTTP-status:',
            error.response?.status
        )

        this.errorMessage =
            'Failed to save recipe. Check your permissions and backend.'
      }
    },

    // Recept verwijderen
    async deleteRecipe(id) {
      if (!confirm(
          'Are you sure you want to delete this recipe?'
      )) {
        return
      }

      this.errorMessage = ''

      try {
        const config = this.authConfig()

        const deleteUrl =
            `${this.endpoints.delete}/${id}`

        await axios.delete(deleteUrl, config)

        await this.fetchRecipes()

      } catch (error) {
        console.error('Error deleting recipe:', error)

        console.error(
            'HTTP-status:',
            error.response?.status
        )

        this.errorMessage =
            'Failed to delete recipe. Check your permissions and backend.'
      }
    },

    closeForm() {
      this.showForm = false

      this.currentRecipe = {
        id: 0,
        title: '',
        ingredients: []
      }
    }
  }
}
</script>

<style scoped>
.recipes-table-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.recipes-table {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 20px;
}

.recipes-table th,
.recipes-table td {
  border: 1px solid #ddd;
  padding: 10px;
  text-align: left;
}

.recipes-table th {
  background-color: #f4f4f4;
}

.action-buttons {
  display: flex;
  gap: 10px;
}

.add-section {
  margin-top: 15px;
  margin-bottom: 25px;
}

.btn {
  padding: 8px 14px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: bold;
}

.btn-edit {
  background-color: #ffc107;
  color: #000;
}

.btn-delete {
  background-color: #dc3545;
  color: #fff;
}

.btn-add {
  background-color: #28a745;
  color: #fff;
  font-size: 1rem;
}

.btn-save {
  background-color: #007bff;
  color: #fff;
}

.btn-cancel {
  background-color: #6c757d;
  color: #fff;
}

.recipe-form-card {
  border: 1px solid #007bff;
  padding: 20px;
  border-radius: 8px;
  background-color: #f8f9fa;
  text-align: left;
}

.form-group {
  margin-bottom: 15px;
}

.form-group label {
  display: block;
  font-weight: bold;
  margin-bottom: 5px;
}

.form-group input,
.form-group textarea {
  width: 100%;
  padding: 8px;
  box-sizing: border-box;
}

.form-actions {
  display: flex;
  gap: 10px;
}

.ingredient-row {
  display: flex;
  gap: 10px;
  margin-bottom: 10px;
  align-items: center;
}

.ingredient-row select {
  flex: 2;
  padding: 8px;
}

.ingredient-row input {
  flex: 1;
  min-width: 80px;
}

.error {
  color: red;
  font-weight: bold;
  margin-bottom: 15px;
}
</style>
