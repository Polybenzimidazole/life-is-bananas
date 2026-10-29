<template>
  <div class="about-container">
    <h1>About Life is Bananas</h1>

    <div class="about-card">
      <p class="lead-text">
        <strong>Life is Bananas</strong> is an innovative company specializing in smart digital solutions for culinary management and kitchen optimization in both professional and home environments.
      </p>

      <p>
        Our mission is to simplify daily cooking for chefs and food enthusiasts by connecting recipe creation with automated ingredient inventory tracking. Through our application, we help reduce food waste and streamline meal preparation.
      </p>

      <div class="info-box">
        <h3>Our Vision</h3>
        <p>Making kitchen management simple, fast, and hassle-free!</p>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'AboutView'
}
</script>

<style scoped>
.about-container {
  max-width: 750px;
  margin: 0 auto;
  padding: 30px 20px;
  text-align: left;
}

.about-container h1 {
  text-align: center;
  color: #2c3e50;
  margin-bottom: 30px;
}

.about-card {
  background-color: #f8f9fa;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  padding: 30px;
  line-height: 1.6;
  color: #444;
}

.lead-text {
  font-size: 1.1rem;
  margin-bottom: 15px;
}

.info-box {
  margin-top: 25px;
  background-color: #e8f4ea;
  border-left: 4px solid #28a745;
  padding: 15px;
  border-radius: 4px;
}

.info-box h3 {
  margin: 0 0 5px 0;
  color: #1e7e34;
}

.info-box p {
  margin: 0;
}
</style>