<template>
  <div class="home-container">
    <div class="hero-section">
      <h1>Welcome to Life is Bananas 🍌</h1>
      <p class="subtitle">
        Your all-in-one platform to manage recipes and track kitchen stock in real time.
      </p>
    </div>

    <!-- Feature Overview -->
    <div class="features-grid">

      <!-- Card 1: Recipes -->
      <div class="feature-card">
        <div class="icon">📖</div>
        <h2>Recipe Management</h2>
        <p>
          Browse your full recipe collection, add new culinary creations, update existing ingredients, or remove outdated recipes.
        </p>
        <router-link to="/recipes" class="btn-link">Access Recipes</router-link>
      </div>

      <!-- Card 2: Stock & Production -->
      <div class="feature-card">
        <div class="icon">📦</div>
        <h2>Stock & Production</h2>
        <p>
          Track reserved ingredients, increment stock quantities, and automatically deduct supplies when preparing recipe portions.
        </p>
        <router-link to="/stock" class="btn-link">Manage Stock</router-link>
      </div>

    </div>
  </div>
</template>

<script>
export default {
  name: 'HomeView'
}
</script>

<style scoped>
.home-container {
  max-width: 900px;
  margin: 0 auto;
  padding: 30px 20px;
}

.hero-section {
  text-align: center;
  margin-bottom: 40px;
}

.hero-section h1 {
  font-size: 2.2rem;
  color: #2c3e50;
  margin-bottom: 10px;
}

.subtitle {
  font-size: 1.1rem;
  color: #6c757d;
}

.features-grid {
  display: flex;
  gap: 25px;
  justify-content: center;
}

.feature-card {
  flex: 1;
  background-color: #f8f9fa;
  border: 1px solid #e9ecef;
  border-radius: 10px;
  padding: 25px;
  text-align: center;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
}

.icon {
  font-size: 2.5rem;
  margin-bottom: 15px;
}

.feature-card h2 {
  color: #333;
  font-size: 1.4rem;
  margin-bottom: 10px;
}

.feature-card p {
  color: #555;
  line-height: 1.5;
  margin-bottom: 20px;
  font-size: 0.95rem;
}

.btn-link {
  display: inline-block;
  background-color: #28a745;
  color: white;
  padding: 10px 18px;
  border-radius: 5px;
  text-decoration: none;
  font-weight: bold;
}

.btn-link:hover {
  background-color: #218838;
}
</style>