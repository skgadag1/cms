/* eslint-disable no-console */
const path = require('path')
const fs = require('fs')
const readline = require('readline')
const util = require('util')

const VERBOSE = process.env.VERBOSE || false

const matchers = [
  /{{\s*(?<key>.+?)\s*\|\s*translate\s*\(\s*labels\s*,\s*(?<path>[^\s]+)\s*\)\s*}}/g, // find in template filters, {{ conceptKey | translate(labels, conceptPath) }}
  /\.translate\(\s*(?<key>[^\(\)]*)\s*,[^\(\)]*,\s*(?<path>[^\(\)]*)\)\s*/g, // find in code filters, filters.translate(conceptKey, conceptPath)
  /translatedLabel\(\s*(?<key>[^\(\)]*)\s*,\s*(?<path>[^\(\)]*)\)\s*/g,
  /{\s*ck\s*:\s*(?<key>['"][^'"]+['"])\s*,\s*cp\s*:\s*(?<path>['"][^'"]+['"])/g, // find in translation objects, {ck: conceptKey, cp:  conceptPath ...
  /conceptName:\s*\[\s*(?<variablePath>['"][^'"]+['"]|.*)\s*\]/g // find variable concept paths in "conceptName: ['variablePath']"
]

const ignorableVariables = [
  'error.text.cp', 'error.text.ck', 'conceptKey', 'conceptPath'
]

const isString = new RegExp(/^['"][^'"]+['"]$/)
const containString = new RegExp(/['"][^'"]+['"]/g)

async function parser(file) {
  return new Promise((res, rej) => {
    const fileStream = fs.createReadStream(file)

    try {
      const rl = readline.createInterface({
        input: fileStream,
        crlfDelay: Infinity
      })

      let findings = []

      const line_counter = ((i = 0) => () => ++i)()
      rl
        .on('line', (line, lineno = line_counter(0)) => {
          for (let m of matchers) {
            const matches = line.matchAll(m)
            for (const found of matches) {
              findings.push({lineno, found: found.groups})
            }
          }
        })
        .on('close', () => {
          if (VERBOSE && findings.length > 0) console.log(`Found ${findings.length} translations`)
          res(findings)
        })
    } catch(err){
      rej(err)
    }
  })
}

let statistic = {files: 0, matches: 0, concepts: 0}
let concepts = {}
let variables = {}

//joining path of directory
const directoryPath = path.join(__dirname, '../src')
if (VERBOSE) console.log('Scanning directory path:', directoryPath)
const walkOptions = {
  filters: ['multiapp-domain', 'bamboo-specs', 'tests', 'node_modules']
};

(async () => {
  await require('walk').walk(directoryPath, walkOptions)
    .on('file', async function (root, fileStats, next) {
      const dirPath = root.replace(directoryPath, '')
      if (['.js', '.vue'].includes(fileStats.name.match(/\.[0-9a-z]+$/i)[0])) {
        statistic.files++
        if (VERBOSE) console.log('Scanning file: ', dirPath + path.sep + fileStats.name)
        let data = await parser(root + path.sep + fileStats.name)
        statistic.matches = statistic.matches + data.length
        if (data.length) {
          for(let itm of data) {
            let conKeys = []
            let conPath = itm.found.path
            if (itm.found.variablePath) { //save variable values
              for (let conPath of itm.found.variablePath.split(',')) {
                conPath = conPath.trim().slice(1, -1)
                concepts[conPath] = concepts[conPath] || {concepts: {}, locations: [], variable: true}
                concepts[conPath].locations.push(dirPath + path.sep + fileStats.name + '#' + itm.lineno)
              }
            } else if (isString.test(itm.found.key) && isString.test(conPath)) {
              conKeys.push(itm.found.key)
            } else if ((ignorableVariables.indexOf(conPath) < 0 && !isString.test(conPath)) || ignorableVariables.indexOf(itm.found.key) < 0) { //concept Path or Key are not ignorable variables
              let conKey = itm.found.key
              //look up for strings in concept key
              conKeys = conKey.match(containString)
              if (!conKeys) { //add variable
                variables[conPath] = variables[conPath] || {concepts: {}, locations: []}
                variables[conPath].concepts[conKey] = variables[conPath].concepts[conKey] || {locations: []}
                variables[conPath].concepts[conKey].locations.push(dirPath + path.sep + fileStats.name + '#' + itm.lineno)
              }
            }

            if(conKeys) for (let conKey of conKeys) {
              if (conPath.match(containString)) conPath = conPath.trim().slice(1, -1)
              conKey = conKey.slice(1, -1)
              concepts[conPath] = concepts[conPath] || {concepts: {}, locations: []}
              concepts[conPath].concepts[conKey] = concepts[conPath].concepts[conKey] || {locations: []}
              concepts[conPath].concepts[conKey].locations.push(dirPath + path.sep + fileStats.name + '#' + itm.lineno)
            }
          }
        }
      } else {
        if (VERBOSE) console.log('Skipping file: ', dirPath + path.sep + fileStats.name)
      }
      next()
    })
    .on('end', function () {
      console.log(`Results: \x1b[32m${statistic.files}\x1b[0m files scanned, \x1b[32m${statistic.matches}\x1b[0m matches found, \x1b[32m${Object.values(concepts).reduce((p, c) => p + Object.keys(c).length, 0)}\x1b[0m concepts in \x1b[32m${Object.keys(concepts).length}\x1b[0m directories exported.`)

      //console.log(util.inspect(concepts, false, null, true /* enable colors */));
      if (Object.keys(variables).length > 0) {
        console.error('\x1b[31mUnknown variables found!\x1b[0m', util.inspect(variables, false, null, true /* enable colors */))
        // disabled failing for kags
        // process.exit(1)
      }

      fs.writeFile('dist/concepts.json', JSON.stringify(concepts), function (err) {
        if(err) return console.log(err)
        console.log('The file \x1b[36mexport.json\x1b[0m was saved!')
      })
    })
})()


