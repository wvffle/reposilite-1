import { ref, watch, reactive } from 'vue'
import { useRoute } from 'vue-router'

const qualifier = reactive({
  watchable: 0,
  path: ''
})

export default function useQualifier(token) {
  const route = useRoute()

  watch(
    () => route.params.qualifier,
    async newQualifier => {
      qualifier.path = newQualifier
      qualifier.watchable++
    },
    { immediate: true }
  )

  watch(
    () => token.name,
    async newSession => {
      qualifier.watchable++
    }
  )

  return {
    qualifier
  }
}