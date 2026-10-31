<template>
  <section>
    <div class="intelligent-404-container container mt-4" data-test-id="cludo-intelligent-404">
      <cludo-element data-type="404" data-xid="ccca333c-c07f-4372-89b7-901b81b3573a" />
    </div>
  </section>
</template>

<script>
export default {
  name: 'SectionIntelligent',
  mounted() {
    if (document.getElementById('cludo-experience-manager')) { return }

    const container = this.$el.querySelector('.intelligent-404-container')
    if (!container) { return }

    const script = document.createElement('script')
    script.id = 'cludo-experience-manager'
    script.src = 'https://customer.cludo.com/scripts/bundles/experiences/manager.js'
    script.setAttribute('data-cid', '10001503')
    script.setAttribute('data-eid', '10002599')
    script.defer = true
    container.appendChild(script)
  }
}
</script>
