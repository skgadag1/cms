export default function useInterval() {
  const waitUntil = (conditionHandler, timeout) => {
    const retryCountLimit = 10
    let retryCount = 0

    return new Promise((resolve, reject) => {
      const timer = setInterval(() => {
        if (conditionHandler()) {
          clearInterval(timer)
          resolve()
        }

        retryCount++

        if (retryCount >= retryCountLimit) {
          clearInterval(timer)
          reject(new Error(`[useInterval] Retry count exceeded. ${retryCountLimit} times with ${timeout} timeout`))
        }
      }, timeout)
    })
  }

  return {
    waitUntil
  }
}
