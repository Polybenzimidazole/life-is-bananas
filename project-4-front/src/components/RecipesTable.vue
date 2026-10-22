<template>
  <div class="recipes-table-container">

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
      <tr v-for="recipe in recipes" :key="recipe.id">
        <td>{{ recipe.id }}</td>
        <td>{{ recipe.title }}</td>
        <td>{{ recipe.ingredients }}</td>
        <td class="action-buttons">
          <button class="btn btn-edit" @click="editRecipe(recipe)">Edit</button>
          <button class="btn btn-delete" @click="deleteRecipe(recipe.id)">Delete</button>
        </td>
      </tr>
      <tr v-if="recipes.length === 0">
        <td colspan="4">No recipes found.</td>
      </tr>
      </tbody>
    </table>

    <div class="add-section">
      <button class="btn btn-add" @click="openAddForm">+ Add Recipe</button>
    </div>

    <div v-if="showForm" class="recipe-form-card">
      <h3 v-if="currentRecipe.id === 0">Add New Recipe</h3>
      <h3 v-else>Edit Recipe #{{ currentRecipe.id }}</h3>

      <form @submit.prevent="saveRecipe">
        <div class="form-group">
          <label for="title">Title:</label>
          <input type="text" id="title" v-model="currentRecipe.title" required />
        </div>

        <div class="form-group">
          <label for="ingredients">Ingredients:</label>
          <textarea id="ingredients" v-model="currentRecipe.ingredients" required></textarea>
        </div>

        <div class="form-actions">
          <button type="submit" class="btn btn-save">
            {{ currentRecipe.id === 0 ? 'Create' : 'Update' }}
          </button>
          <button type="button" class="btn btn-cancel" @click="closeForm">Cancel</button>
        </div>
      </form>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'RecipesTable',
  data() {
    return {
      recipes: [],
      showForm: false,
      errorMessage: '',
      currentRecipe: {
        id: 0,
        title: '',
        ingredients: ''
      },

      endpoints: {
        getAll:  'http://localhost:8080/api/recipes/all',
        create:  'http://localhost:8080/api/recipes/add',
        update:  'http://localhost:8080/api/recipes/update',
        delete:  'http://localhost:8080/api/recipes/delete'
      }
    }
  },
  mounted() {
    this.fetchRecipes()
  },
  methods: {
    fetchRecipes() {
      axios.get(this.endpoints.getAll, { withCredentials: true })
          .then(response => {
            this.recipes = response.data
            this.errorMessage = ''
          })
          .catch(error => {
            console.error("Error loading recipes:", error)
            this.errorMessage = "Unable to load recipes from server."
          })
    },

    openAddForm() {
      this.currentRecipe = { id: 0, title: '', ingredients: '' }
      this.showForm = true
    },

    editRecipe(recipe) {
      this.currentRecipe = { ...recipe }
      this.showForm = true
    },

    saveRecipe() {
      const config = { withCredentials: true }

      if (this.currentRecipe.id === 0) {
        axios.post(this.endpoints.create, this.currentRecipe, config)
            .then(() => {
              this.fetchRecipes()
              this.closeForm()
            })
            .catch(error => {
              console.error("Error creating recipe:", error)
              this.errorMessage = "Failed to create recipe."
            })
      } else {
        const updateUrl = `${this.endpoints.update}/${this.currentRecipe.id}`
        axios.put(updateUrl, this.currentRecipe, config)
            .then(() => {
              this.fetchRecipes()
              this.closeForm()
            })
            .catch(error => {
              console.error("Error updating recipe:", error)
              this.errorMessage = "Failed to update recipe."
            })
      }
    },

    deleteRecipe(id) {
      if (confirm("Are you sure you want to delete this recipe?")) {
        const deleteUrl = `${this.endpoints.delete}/${id}`
        axios.delete(deleteUrl, { withCredentials: true })
            .then(() => {
              this.fetchRecipes()
            })
            .catch(error => {
              console.error("Error deleting recipe:", error)
              this.errorMessage = "Failed to delete recipe."
            })
      }
    },

    closeForm() {
      this.showForm = false
      this.currentRecipe = { id: 0, title: '', ingredients: '' }
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
.recipes-table th, .recipes-table td {
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
.btn-edit { background-color: #ffc107; color: #000; }
.btn-delete { background-color: #dc3545; color: #fff; }
.btn-add { background-color: #28a745; color: #fff; font-size: 1rem; }
.btn-save { background-color: #007bff; color: #fff; }
.btn-cancel { background-color: #6c757d; color: #fff; }

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
.form-group input, .form-group textarea {
  width: 100%;
  padding: 8px;
  box-sizing: border-box;
}
.form-actions {
  display: flex;
  gap: 10px;
}
.error {
  color: red;
  font-weight: bold;
}
</style>