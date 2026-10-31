import boto3
import sys

PROFILE = 'dev'
BUCKET = sys.argv[1]

print("Removing versioned objects from S3 Bucket: ", BUCKET)

session = boto3.Session(profile_name = PROFILE)
s3 = session.resource('s3')
bucket = s3.Bucket(BUCKET)
bucket.object_versions.delete()
