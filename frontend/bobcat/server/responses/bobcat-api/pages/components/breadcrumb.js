export default {
  '@name': 'machine',
  '@path': '/bobcat',
  '@id': 'bd3f6502-f528-493d-b087-052a07e13ca3',
  '@nodeType': 'mgnl:page',
  title: 'breadcrumb',
  breadcrumbs: [
    {
      title: 'North America',
      url: '/na/en'
    },
    {
      title: 'Equipment',
      url: '/na/en/equipment'
    },
    {
      title: 'Loaders',
      url: '/na/en/equipment/custom-loaders'
    },
    {
      title: 'Skid-Steer Loaders',
      url: '/na/en/equipment/custom-loaders/skid-steer'
    },
    {
      title: 'S740',
      url: '/na/en/equipment/custom-loaders/skid-steer/s740-skid-steer-loader'
    }
  ],
  hideInNav: 'false',
  'mgnl:template': 'bobcat:pages/index',
  largeMenu: 'false',
  pageLink: {
    '@name': 'pageLink',
    '@path': '/bobcat/na/machine/pageLink',
    '@id': 'f306b22e-e210-4c46-ace3-9a03013632d7',
    '@nodeType': 'mgnl:contentNode',
    '@nodes': []
  },
  header: {
    '@name': 'header',
    '@path': '/bobcat/na/machine/header',
    '@id': 'c8d88ea3-ec08-4e2d-a972-ad557e6f604e',
    '@nodeType': 'mgnl:area',
    '@nodes': []
  },
  main: {
    '@name': 'main',
    '@path': '/bobcat/na/machine/main',
    '@id': '9258c44d-8d04-4462-9066-b4a53efe203d',
    '@nodeType': 'mgnl:area',
    0: {
      '@name': '0',
      '@path': '/bobcat/na/machine/main/0',
      '@id': '5410cc57-a498-4ef5-b993-eb19f60eae68',
      '@nodeType': 'mgnl:component',
      title: 'Breadcrumb',
      inheritable: 'false',
      'mgnl:template': 'bobcat:components/breadcrumb',
      '@nodes': []
    },
    '@nodes': ['0']
  },
  footer: {
    '@name': 'footer',
    '@path': '/bobcat/na/machine/footer',
    '@id': '173c8519-75ab-48c9-8be0-94036292ca15',
    '@nodeType': 'mgnl:area',
    '@nodes': []
  },
  '@nodes': ['pageLink', 'header', 'main', 'footer']
}
