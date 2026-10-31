/* eslint-disable no-console */
const serverlessExpress = require('@codegenie/serverless-express')
const {Nuxt} = require('nuxt-start')
const config = require('../nuxt.config.js')
const {GetEnvVars} = require('env-cmd')

const binaryMimeTypes = require('./binaryMimeTypes')
let nuxt

module.exports.render = serverlessExpress({
  app,
  binarySettings: {
    isBinary: ({headers}) => process.env.TEXT_OUTPUT !== 'true',
    contentTypes: binaryMimeTypes,
    contentEncodings: []
  }
})

async function overrideEnvironment() {
  const isPublic = process.env.IS_AUTHOR !== true && process.env.IS_AUTHOR !== 'true'
  const environments = ['common', process.env.NUXT_ENV]
  if (!isPublic) {
    environments.push(`${process.env.NUXT_ENV}-author`)
  }
  console.log(`Overriding with ${environments.join(', ')} environments...`)
  const envVars = await GetEnvVars({
    rc: {
      filePath: process.env.ENV_FILE,
      environments
    }
  })

  Object.keys(config.publicRuntimeConfig).forEach(k => {
    config.publicRuntimeConfig[k] = process.env[k] ?? envVars[k] ?? config.publicRuntimeConfig[k]
  })
  console.log(config.publicRuntimeConfig)
}

async function app(req, res) {
  if (!nuxt) {
    await overrideEnvironment()
    nuxt = new Nuxt({...config, dev: false, telemetry: false})
  }

  const {rawHeaders, httpVersion, method, url} = req
  const {statusCode, statusMessage} = res
  const env = process.env.NODE_ENV
  console.log(`ENV=${env}, AUTHOR=${process.env.IS_AUTHOR}`)
  await nuxt.ready()
  console.log(
    JSON.stringify({
      rawHeaders,
      httpVersion,
      method,
      url,
      response: {
        statusCode,
        statusMessage
      },
      env
    })
  )
  return nuxt.server.app(req, res)
}
