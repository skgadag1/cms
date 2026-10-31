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
      'mgnl:template': 'bobcat:components/notification-bar',
      paragraph: 'If we have a lot to say about how we’re dealing with the COVID-19 crisis or zombies or whatever, the width of this element is constrained for readabilty. But really, brevity, as they say, is the soul of wit.',
      link: {
        '@name': 'link',
        '@path': '/bobcat/main/0/variant/link',
        '@id': '173e68cc-512b-4972-bd9b-35fdc3aeaf1d',
        '@nodeType': 'mgnl:contentNode',
        field: 'custom',
        link: {
          '@name': 'link',
          '@path': '/bobcat/main/0/variant/link/link',
          '@id': 'dfd6de6b-573a-4294-aa75-13cd1cb422d1',
          '@nodeType': 'mgnl:contentNode',
          customLabel: 'Learn More',
          target: '_blank',
          customUrl: '/',
          '@nodes': []
        },
        '@nodes': [
          'link'
        ]
      },
      icon: 'info',
      '@nodes': ['paragraph', 'link', 'icon']
    },
    '@nodes': ['0']
  },
  '@nodes': [
    'header',
    'main'
  ]
}
