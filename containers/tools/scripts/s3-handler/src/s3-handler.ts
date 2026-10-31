#!/usr/bin/env node

import pjson = require('../package.json');
import program = require("commander");
import AWS_client_s3 = require('@aws-sdk/client-s3');
import S3Client = AWS_client_s3.S3Client;
import ListObjectsCommand = AWS_client_s3.ListObjectsCommand;
import ListObjectVersionsCommand = AWS_client_s3.ListObjectVersionsCommand;
import ListObjectsOutput = AWS_client_s3.ListObjectsOutput;
import DeleteObjectsCommand = AWS_client_s3.DeleteObjectsCommand;
import ObjectVersion = AWS_client_s3.ObjectVersion;

const s3 = new S3Client({ region: "us-east-1" });

program.version(pjson.version, '-v, --version', 'output the current version');
program.command('empty <bucketId>')
  .description('empty given bucket from all objects and versions')
  .action(bucketId => {
    clearBucket(bucketId);
  });
program.parse(process.argv);

async function clearBucket(bucketId: string) {
  let objects = await listObjects(bucketId);
  while (true) {
    await s3.send(new DeleteObjectsCommand({
      Bucket: bucketId,
      Delete: {
        Objects: ensure(objects.Contents).map((obj: { Key?: string }) => ({ Key: obj.Key }))
      }
    }));

    if (objects.IsTruncated) {
      objects = await listObjects(bucketId, ensure(objects.Contents).at(-1)?.Key);
    } else {
      break;
    }
  }

  let versions = await listVersions(bucketId);
  while (true) {
    await s3.send(new DeleteObjectsCommand({
      Bucket: bucketId,
      Delete: {
        Objects: ensure(versions.DeleteMarkers).map((obj: ObjectVersion) => ({ Key: obj.Key, VersionId: obj.VersionId }))
      }
    }));

    await s3.send(new DeleteObjectsCommand({
      Bucket: bucketId,
      Delete: {
        Objects: ensure(versions.Versions).map((obj: ObjectVersion) => ({ Key: obj.Key, VersionId: obj.VersionId }))
      }
    }));

    if (versions.IsTruncated) {
      versions = await listVersions(bucketId, versions.NextKeyMarker, versions.NextVersionIdMarker);
    } else {
      break;
    }
  }
}

async function listObjects(bucketId: string, nextMarkerId?: string) : Promise<ListObjectsOutput> {
  console.log(`Listing objects [${bucketId}][${nextMarkerId}]...`);
  const params = nextMarkerId
    ? { Bucket: bucketId, Marker: nextMarkerId }
    : { Bucket: bucketId };

  return await s3.send(new ListObjectsCommand(params));
}

async function listVersions(bucketId: string, nextKeyMarker?: string, nextVersionMarker?: string) {
  console.log(`Listing versions [${bucketId}][${nextKeyMarker}][${nextVersionMarker}]...`);
  const params = nextKeyMarker
    ? { Bucket: bucketId, KeyMarker: nextKeyMarker, VersionIdMarker: nextVersionMarker }
    : { Bucket: bucketId };

  return await s3.send(new ListObjectVersionsCommand(params));
}

function ensure(obj:any) {
  if (!obj) {
    throw Error('No object');
  }
  return obj;
}
