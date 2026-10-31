export default {
  '@name': 'bobcat',
  '@path': '/bobcat',
  '@id': '623b76a0-5cdc-48c1-a45b-701f7a191161',
  '@nodeType': 'mgnl:page',
  title: 'bobcat',
  'mgnl:lastModified': '2021-03-31T17:56:34.960+02:00',
  'mgnl:template': 'bobcat:pages/index',
  'mgnl:created': '2021-03-31T17:56:27.641+02:00',
  header: {
    '@name': 'header',
    '@path': '/bobcat/header',
    '@id': 'f331ae47-3f5b-4eb0-b72d-089f0409204c',
    '@nodeType': 'mgnl:area',
    '@nodes': []
  },
  main: {
    '@name': 'main',
    '@path': '/bobcat/main',
    '@id': 'ef6f4eed-879c-4c0d-ac2e-8d2ffe7a9158',
    '@nodeType': 'mgnl:area',
    'mgnl:lastModified': '2021-03-31T17:56:34.961+02:00',
    'mgnl:created': '2021-03-31T17:56:34.961+02:00',
    0: {
      '@name': '0',
      '@path': '/bobcat/main/0',
      '@id': '07872cde-c309-4e28-bc40-352c9ad7b8d3',
      '@nodeType': 'mgnl:component',
      'mgnl:template': 'bobcat:components/quote',
      quote: 'Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua do eiusmod tempor incididunt.',
      authorName: 'Firstname Lastname',
      authorSubtitle: 'Jobtitle / Bobcat Customer',
      avatar: {
        src: 'cloudinary:Bobcat/NA-Bobcat-com/t62#eyJhc3BlY3RfcmF0aW8iOjEuMTYxMTQsImZvcm1hdCI6ImpwZyIsInJlc291cmNlX3R5cGUiOiJpbWFnZSIsInNlY3VyZV91cmwiOiJodHRwczovL3Jlcy5jbG91ZGluYXJ5LmNvbS9kb29zYW4tYm9iY2F0LWRldi9pbWFnZS91cGxvYWQvdjE2MTg5MzgzMzEvQm9iY2F0L05BLUJvYmNhdC1jb20vdDYyLmpwZyIsImNyZWF0ZWRfYXQiOiIyMDIxLTA0LTIwVDE3OjA1OjMxKzAwOjAwIiwiYmFja3VwX2J5dGVzIjoxMjkwNywiYXNzZXRfaWQiOiIwNjI1MGU4NTBiZTFkOGMxY2Y4M2E1N2UxZTYwODY3NSIsInR5cGUiOiJ1cGxvYWQiLCJ2ZXJzaW9uIjoxNjE4OTM4MzMxLCJhY2Nlc3NfbW9kZSI6InB1YmxpYyIsImNyZWF0ZWRfYnkiOnsiYWNjZXNzX2tleSI6Ijg0MzQzMjIzMTE0MTgxMiIsImN1c3RvbV9pZCI6ImRlYi5sdWViY2hvd0Bkb29zYW4uY29tIiwiZXh0ZXJuYWxfaWQiOiIxMzNmNWE4ZGE4NzA2NmYzZWE2NDkwNTVlYmQzYjQifSwidXJsIjoiaHR0cDovL3Jlcy5jbG91ZGluYXJ5LmNvbS9kb29zYW4tYm9iY2F0LWRldi9pbWFnZS91cGxvYWQvdjE2MTg5MzgzMzEvQm9iY2F0L05BLUJvYmNhdC1jb20vdDYyLmpwZyIsInB1YmxpY19pZCI6IkJvYmNhdC9OQS1Cb2JjYXQtY29tL3Q2MiIsInVwbG9hZGVkX2J5Ijp7ImFjY2Vzc19rZXkiOiI4NDM0MzIyMzExNDE4MTIiLCJjdXN0b21faWQiOiJkZWIubHVlYmNob3dAZG9vc2FuLmNvbSIsImV4dGVybmFsX2lkIjoiMTMzZjVhOGRhODcwNjZmM2VhNjQ5MDU1ZWJkM2I0In0sImZvbGRlciI6IkJvYmNhdC9OQS1Cb2JjYXQtY29tIiwiZmlsZW5hbWUiOiJ0NjIiLCJwaXhlbHMiOjUxNjk1LCJ1cGxvYWRlZF9hdCI6IjIwMjEtMDQtMjBUMTc6MDU6MzErMDA6MDAiLCJieXRlcyI6MTI5MDcsIndpZHRoIjoyNDUsImV0YWciOiIyYjliOTRiM2EwZjJiYjk2ZTBhNzBkN2RkMDUzMTgwNyIsImhlaWdodCI6MjExLCJzdGF0dXMiOiJhY3RpdmUifQ==',
        imageAlt: 'alt-avatar',
        imageTitle: 'title-avatar'
      },
      '@nodes': ['quote', 'authorName', 'authorSubtitle', 'avatar']
    },
    '@nodes': ['0']
  },
  '@nodes': [
    'header',
    'main'
  ]
}
