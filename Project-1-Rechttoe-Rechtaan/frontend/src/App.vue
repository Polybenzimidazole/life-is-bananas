<template>
  <header class="topbar">
    <a class="brand" href="./"><span class="brand-icon" aria-hidden="true">◒</span> life is bananas<span class="brand-dot">.</span></a>
    <div v-if="user" class="account"><span>{{ user.name }}</span><button class="quiet" :disabled="busy" @click="logout">Afmelden ↗</button></div>
    <span v-else class="tagline">Van grondstof tot goede snack.</span>
  </header>
  <main>
    <div class="intro"><p class="eyebrow">HET RECEPTENATELIER</p><h1>Kleine ingrediënten.<br>Grote ideeën.</h1><p>Ontwikkel, verfijn en volg je recepturen op één plek.</p></div>
    <p v-if="error" class="notice error" role="alert">{{ error }}</p>
    <p v-if="success" class="notice success" role="status">{{ success }}</p>
    <p v-if="initializing" role="status">Sessie laden…</p>
    <section v-else-if="!user" class="card login">
      <p class="eyebrow">WELKOM TERUG</p><h2>Aan de slag</h2><p>Meld je aan als bio-ingenieur.</p>
      <form @submit.prevent="login">
        <label>E-mailadres<input v-model="credentials.email" type="email" autocomplete="username" required></label>
        <label>Wachtwoord<input v-model="credentials.password" type="password" autocomplete="current-password" required></label>
        <button class="primary" :disabled="busy">{{ busy ? 'Aanmelden…' : 'Aanmelden →' }}</button>
      </form>
    </section>
    <div v-else class="workspace" :aria-busy="busy">
      <section class="card recipes">
        <div class="section-heading"><h2>Recepturen</h2><span class="counter">{{ recipes.length }}</span></div>
        <form class="search" @submit.prevent="searchRecipes"><label class="sr-only" for="search">Zoek op receptnaam</label><input id="search" v-model="search" placeholder="Zoek op receptnaam…"><button :disabled="busy" aria-label="Zoeken">Zoek</button></form>
        <p v-if="!recipes.length" class="empty">Geen recepturen gevonden. Probeer een andere zoekterm.</p>
        <button v-for="recipe in recipes" :key="recipe.id" class="recipe-item" :class="{ selected: detail?.id === recipe.id }" :aria-pressed="detail?.id === recipe.id" :disabled="busy" @click="openRecipe(recipe.id)">
          <span class="recipe-name">{{ recipe.name }} <span aria-hidden="true">↗</span></span><span class="recipe-meta">Versie {{ recipe.version }}<span class="badge">{{ statusLabel(recipe.status) }}</span></span>
        </button>
        <p class="list-footer">GOEDE SNACKS BEGINNEN HIER.</p>
      </section>
      <section v-if="detail" class="card detail">
        <div class="detail-heading"><div><p class="eyebrow">RECEPTUUR / {{ String(detail.id).padStart(3, '0') }}</p><h2>{{ detail.name }}</h2></div><span class="badge">{{ statusLabel(detail.status) }}</span></div>
        <p class="muted">Versie {{ detail.version }} · Bijgewerkt op {{ date(detail.lastModified) }}</p>
        <h3>Bereidingswijze</h3><p class="preparation">{{ detail.preparationMethod || 'Nog geen bereidingswijze beschikbaar.' }}</p>
        <div class="section-heading"><h3>Ingrediënten</h3><span class="counter">{{ detail.ingredients.length }}</span></div>
        <div class="table-wrap"><table><thead><tr><th>Stap</th><th>Grondstof</th><th>Hoeveelheid</th></tr></thead><tbody><tr v-for="ingredient in detail.ingredients" :key="ingredient.id"><td>{{ String(ingredient.sequence).padStart(2, '0') }}</td><td>{{ ingredient.name }}</td><td>{{ ingredient.quantity }} {{ unitLabel(ingredient.unit) }}</td></tr><tr v-if="!detail.ingredients.length"><td colspan="3">Nog geen ingrediënten toegevoegd.</td></tr></tbody></table></div>
        <form v-if="detail.editable" class="ingredient-form" @submit.prevent="addIngredient">
          <h3>Voeg een ingrediënt toe</h3>
          <div class="fields"><label class="material">Grondstof<select v-model="ingredient.rawMaterialId" required><option disabled value="">Kies een grondstof</option><option v-for="m in materials" :key="m.id" :value="m.id">{{ m.name }}{{ m.organicCertified ? ' · bio' : '' }}</option></select></label><label>Hoeveelheid<input v-model="ingredient.quantity" type="number" min="0.01" max="99999999.99" step="0.01" required placeholder="bv. 300"></label><label>Eenheid<select v-model="ingredient.unit" required><option v-for="unit in units" :key="unit" :value="unit">{{ unitLabel(unit) }}</option></select></label></div>
          <button class="secondary" :disabled="busy">Ingrediënt toevoegen +</button>
        </form>
        <p v-else class="locked">Deze receptuur is goedgekeurd. De samenstelling ligt vast.</p>
        <div class="status-action"><div><h3>Volgende stap</h3><p>{{ detail.nextStatus ? 'Klaar voor de volgende fase?' : 'Deze receptuur is in productie.' }}</p></div><button v-if="detail.nextStatus" class="primary" :disabled="busy" @click="changeStatus">{{ statusLabel(detail.nextStatus) }} →</button></div>
      </section>
      <section v-else class="card empty-detail"><span aria-hidden="true">◒</span><h2>Een goed idee begint hier.</h2><p>Kies een receptuur om de samenstelling te bekijken.</p></section>
    </div>
    <footer>Life is Bananas <span>Met aandacht ontwikkeld. Stap voor stap.</span></footer>
  </main>
</template>

<script>
import { api } from './api'

export default {
  name: 'App',
  data() {
    return {
      user: null, initializing: true, busy: false, error: '', success: '',
      credentials: { email: '', password: '' }, search: '', recipes: [], detail: null,
      materials: [], units: [], ingredient: { rawMaterialId: '', quantity: '', unit: 'GRAM' }
    }
  },
  async mounted() {
    try {
      this.user = await api('/session')
      await this.loadWorkspace()
    } catch (e) {
      if (e.status !== 401) this.error = e.message
    } finally { this.initializing = false }
  },
  methods: {
    statusLabel(status) { return { IN_DEVELOPMENT: 'In ontwikkeling', TESTED: 'Getest', APPROVED: 'Goedgekeurd', IN_PRODUCTION: 'In productie' }[status] || status },
    unitLabel(unit) { return { GRAM: 'g', KILOGRAM: 'kg', MILLILITER: 'ml', PERCENTAGE: '%', PIECE: 'stuk(s)' }[unit] || unit },
    date(value) { return value ? new Intl.DateTimeFormat('nl-BE').format(new Date(`${value}T12:00:00`)) : '—' },
    async run(action) {
      if (this.busy) return
      this.busy = true; this.error = ''; this.success = ''
      try { await action() } catch (e) {
        this.error = e.message
        if (e.status === 401) { this.user = null; this.detail = null; this.recipes = [] }
      } finally { this.busy = false }
    },
    async loadWorkspace() {
      const [recipes, materials, units] = await Promise.all([api('/recipes'), api('/raw-materials'), api('/units')])
      this.recipes = recipes; this.materials = materials; this.units = units
    },
    login() { return this.run(async () => {
      this.user = await api('/session', { method: 'POST', body: JSON.stringify(this.credentials) })
      this.credentials.password = ''
      await this.loadWorkspace()
    }) },
    logout() { return this.run(async () => {
      await api('/session', { method: 'DELETE' })
      this.user = null; this.detail = null; this.recipes = []; this.search = ''
    }) },
    searchRecipes() { return this.run(async () => {
      this.recipes = await api(`/recipes?search=${encodeURIComponent(this.search)}`)
    }) },
    openRecipe(id) { return this.run(async () => {
      this.detail = null
      this.detail = await api(`/recipes/${id}`)
      this.ingredient = { rawMaterialId: '', quantity: '', unit: 'GRAM' }
    }) },
    updateRecipe(recipe) {
      this.detail = recipe
      this.recipes = this.recipes.map(r => r.id === recipe.id ? { ...r, status: recipe.status, lastModified: recipe.lastModified } : r)
    },
    addIngredient() { return this.run(async () => {
      const recipe = await api(`/recipes/${this.detail.id}/ingredients`, { method: 'POST', body: JSON.stringify(this.ingredient) })
      this.updateRecipe(recipe); this.ingredient.quantity = ''; this.success = 'Ingrediënt toegevoegd en bewaard.'
    }) },
    changeStatus() { return this.run(async () => {
      const recipe = await api(`/recipes/${this.detail.id}/status`, { method: 'PATCH', body: JSON.stringify({ status: this.detail.nextStatus }) })
      this.updateRecipe(recipe); this.success = 'De nieuwe status is bewaard.'
    }) }
  }
}
</script>

