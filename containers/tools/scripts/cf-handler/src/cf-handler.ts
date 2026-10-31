#!/usr/bin/env node

import pjson = require('../package.json');
import program = require("commander");
import AWS_client_cloudfront = require('@aws-sdk/client-cloudfront');
import CloudFrontClient = AWS_client_cloudfront.CloudFrontClient;
import CreateInvalidationCommand = AWS_client_cloudfront.CreateInvalidationCommand;
import ListDistributionsCommand = AWS_client_cloudfront.ListDistributionsCommand;
import ListDistributionsCommandOutput = AWS_client_cloudfront.ListDistributionsCommandOutput;
import DistributionSummary = AWS_client_cloudfront.DistributionSummary;

const cf = new CloudFrontClient({ region: "us-east-1" });

program.version(pjson.version, '-v, --version', 'output the current version');
program.command('invalidate <domainName>')
  .description('invalidate CDN for fiven domain')
  .action((domainName: string) => {
    invalidateCDN(domainName);
  });
program.parse(process.argv);

async function invalidateCDN(domainName: string) {
  var distribution: DistributionSummary = await getCDNDistribution(domainName);

  console.log(`Found distribution with id [${distribution.Id}] for domain name: [${domainName}]. Creating invalidation...`);

  await cf.send(new CreateInvalidationCommand({
    DistributionId: distribution.Id,
    InvalidationBatch: {
      Paths: {
        Quantity: 1,
        Items: ['/*']
      },
      CallerReference: `DeploymentInvalidation_${new Date().getTime()}_${domainName}`
    }
  }));

  console.log("Invalidation started!");
}

async function getCDNDistribution(domainName: string) {
  var distributions = await listDistributions();
  while (true) {
    for (let distribution of ensure(distributions.DistributionList!.Items)) {
      for (let alias of ensure(distribution.Aliases.Items)) {
        if (alias == domainName) {
          return Promise.resolve(distribution);
        }
      }
    }

    if (ensure(distributions.DistributionList).IsTruncated) {
      distributions = await listDistributions(ensure(distributions.DistributionList).NextMarker);
    } else {
      console.log("error");
      return Promise.reject(`No distribution with domainName: ${domainName} found!`);
    }
  }
}

async function listDistributions(nextMarker?: string): Promise<ListDistributionsCommandOutput> {
  console.log(`Listing distributions [${nextMarker}]...`);

  const params = nextMarker ? { Marker: nextMarker } : {};
  return await cf.send(new ListDistributionsCommand(params));
}

function ensure(obj:any) {
  if (!obj) {
    throw Error('No object');
  }
  return obj;
}
