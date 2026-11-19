<template>
  <main class="assistant">
    <h1>AI-assistent</h1>
    <p>Kies een recept en stel een vraag over ingrediënten, voorraad of productie.</p>
    <p class="hint">Je vraag en de gegevens van dit recept worden voor het antwoord naar OpenAI gestuurd.</p>

    <p v-if="loadingRecipes" role="status">Recepten laden…</p>
    <p v-else-if="!recipes.length && !error" role="status">Voeg eerst een recept toe om een vraag te stellen.</p>
    <button v-if="recipesFailed" type="button" @click="loadRecipes">Recepten opnieuw laden</button>

    <form @submit.prevent="ask">
      <label for="assistant-recipe">Recept</label>
      <select id="assistant-recipe" v-model.number="recipeId" required :disabled="busy || !recipes.length">
        <option disabled :value="null">Kies een recept</option>
        <option v-for="recipe in recipes" :key="recipe.id" :value="recipe.id">{{ recipe.title }}</option>
      </select>

      <label for="assistant-question">Je vraag</label>
      <textarea id="assistant-question" v-model="question" required maxlength="2000" rows="4"
                :disabled="busy" placeholder="Hebben we voldoende voorraad om 10 eenheden van dit recept te maken?" />
      <div class="examples">
        <span>Voorbeeldvragen:</span>
        <button v-for="example in examples" :key="example" type="button" :disabled="busy"
                @click="question = example">{{ example }}</button>
      </div>
      <button class="submit" type="submit" :disabled="busy || !recipeId || !question.trim()">
        {{ busy ? 'Antwoord wordt opgehaald…' : 'Stel je vraag' }}
      </button>
    </form>

    <p v-if="error" class="error" role="alert">{{ error }}</p>
    <section v-if="answer" class="answer" aria-live="polite" aria-atomic="true">
      <h2>Antwoord</h2>
      <p class="submitted-question">{{ answeredQuestion }}</p>
      <p class="answer-text">{{ answer.answer }}</p>
      <h3>Gebruikte gegevens</h3>
      <ul><li v-for="source in answer.sources" :key="source">{{ source }}</li></ul>
      <p class="hint">Dit is AI-advies. Controleer de berekening vóór productie; voorraad wordt hier niet aangepast.</p>
    </section>
  </main>
</template>

<script>
import axios from 'axios'
import { getAccessToken } from '../services/auth'

export default {
  name: 'AssistantView',
  data() {
    return {
      recipes: [], recipeId: null, question: '', answer: null, answeredQuestion: '',
      busy: false, loadingRecipes: true, recipesFailed: false, error: '',
      examples: [
        'Welke ingrediënten heb ik nodig voor 10 eenheden?',
        'Is er voldoende voorraad voor 10 eenheden?',
        'Welke grondstoffen moet ik aanvullen voor 100 eenheden?'
      ]
    }
  },
  created() { this.loadRecipes() },
  methods: {
    headers() { return { Authorization: `Bearer ${getAccessToken()}` } },
    async loadRecipes() {
      this.loadingRecipes = true
      this.recipesFailed = false
      this.error = ''
      try {
        const { data } = await axios.get('/api/recipes/all', { headers: this.headers() })
        this.recipes = data
        this.recipeId = data.length ? data[0].id : null
      } catch (error) {
        this.recipesFailed = true
        this.error = 'De recepten konden niet worden geladen. Probeer het opnieuw.'
      } finally {
        this.loadingRecipes = false
      }
    },
    async ask() {
      if (this.busy || !this.recipeId || !this.question.trim()) return
      this.busy = true
      this.error = ''
      this.answer = null
      this.answeredQuestion = this.question.trim()
      try {
        const { data } = await axios.post('/api/ai/ask', {
          question: this.answeredQuestion, recipeId: this.recipeId
        }, { headers: this.headers(), timeout: 55000 })
        this.answer = data
      } catch (error) {
        const status = error.response?.status
        this.error = error.response?.data?.detail || ({
          400: 'Controleer je vraag (maximaal 2000 tekens) en kies een geldig recept.',
          401: 'Meld je opnieuw aan om de assistent te gebruiken.',
          404: 'Dit recept bestaat niet meer. Laad de recepten opnieuw.',
          503: 'De AI-assistent is tijdelijk niet beschikbaar.'
        })[status] || 'Het antwoord kon niet worden opgehaald. Probeer het opnieuw.'
        if (status === 404) this.recipesFailed = true
      } finally {
        this.busy = false
      }
    }
  }
}
</script>

<style scoped>
.assistant { max-width: 760px; margin: 2rem auto; padding: 0 1rem; text-align: left; }
form { display: grid; gap: 0.75rem; margin: 1.5rem 0; }
label { font-weight: 600; }
select, textarea { padding: 0.75rem; font: inherit; border: 1px solid #9aa8aa; border-radius: 6px; }
textarea { resize: vertical; }
button { cursor: pointer; padding: 0.6rem 0.9rem; font: inherit; border-radius: 6px; border: 1px solid #718985; }
button:disabled { cursor: default; opacity: 0.6; }
.submit { background: #216957; color: white; justify-self: start; }
.examples { display: flex; flex-wrap: wrap; gap: 0.5rem; align-items: center; font-size: 0.9rem; }
.examples button { background: #f3f7f4; }
.answer { background: #f3f7f4; border-left: 4px solid #216957; padding: 1rem 1.25rem; }
.answer-text { white-space: pre-wrap; overflow-wrap: anywhere; }
.submitted-question { font-weight: 600; }
.hint { color: #53625d; font-size: 0.9rem; }
.error { color: #a32323; }
</style>
