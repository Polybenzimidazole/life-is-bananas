<template>
  <div class="about">
    <AboutUs/>
  </div>
</template>

<script>
// @ is an alias to /src
import AboutUs from '@/components/AboutUsPage.vue'

export default {
  name: 'About',
  components: {
    AboutUs
  }
}
</script>