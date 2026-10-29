<template>
  <div class="recipes">
    <RecipesTable/>
  </div>
</template>

<script>
// @ is an alias to /src
import RecipesTable from '@/components/RecipesTable.vue'

export default {
  name: 'Recipes',
  components: {
    RecipesTable
  }
}
</script>