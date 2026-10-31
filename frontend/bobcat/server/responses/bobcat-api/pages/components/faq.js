export default {
  '@name': 'FAQ',
  '@path': '/bobcat/na/FAQ',
  '@id': '33174c45-0d42-4226-a38d-00fb807a657b',
  '@nodeType': 'mgnl:page',
  navigationLabel: 'EfAjKju',
  hideInNav: 'false',
  'mgnl:template': 'bobcat:pages/index',
  largeMenu: 'false',
  removeFromBreadcrumb: 'false',
  'mgnl:lastModified': '2022-01-04T11:16:54.131+01:00',
  completelyHidden: 'false',
  customNodeName: 'efaj-q',
  removeFromUrl: 'false',
  dxpTags: {
    '@name': 'dxpTags',
    '@path': '/bobcat/na/FAQ/dxpTags',
    '@id': '6b5fab31-8545-4e64-b313-9c7f45428faf',
    '@nodeType': 'mgnl:contentNode',
    'mgnl:lastModified': '2022-01-03T10:31:44.348+01:00',
    '@nodes': []
  },
  primaryPageImage: {
    '@name': 'primaryPageImage',
    '@path': '/bobcat/na/FAQ/primaryPageImage',
    '@id': 'cfa52f97-154c-419f-b2f0-a71d377145ce',
    '@nodeType': 'mgnl:contentNode',
    'mgnl:lastModified': '2022-01-03T10:31:44.352+01:00',
    '@nodes': []
  },
  pageLink: {
    '@name': 'pageLink',
    '@path': '/bobcat/na/FAQ/pageLink',
    '@id': '7dac3933-7703-4e05-b7de-5e827c1b0cf3',
    '@nodeType': 'mgnl:contentNode',
    link: 'true',
    'mgnl:lastModified': '2022-01-03T10:31:44.354+01:00',
    field: 'none',
    '@nodes': []
  },
  header: {
    '@name': 'header',
    '@path': '/bobcat/na/FAQ/header',
    '@id': 'baa045af-061b-4b07-be56-af6f7e4a7bd0',
    '@nodeType': 'mgnl:area',
    'mgnl:lastModified': '2022-01-03T10:31:47.420+01:00',
    '@nodes': []
  },
  main: {
    '@name': 'main',
    '@path': '/bobcat/na/FAQ/main',
    '@id': 'e681c99d-7c25-47fd-a912-44d1298e5613',
    '@nodeType': 'mgnl:area',
    'mgnl:lastModified': '2022-01-03T21:00:41.791+01:00',
    0: {
      '@name': '0',
      '@path': '/bobcat/na/FAQ/main/0',
      '@id': '5407d2be-33d2-4294-b136-efe24a0d7cd5',
      '@nodeType': 'mgnl:component',
      hint: 'Hint below: keyword and go!',
      htag: 'h2',
      'mgnl:template': 'bobcat:components/faq',
      placeholder: 'Search placeholder',
      title: 'FAQ',
      'mgnl:lastModified': '2022-01-03T21:00:41.791+01:00',
      inheritable: 'false',
      faqSections: {
        '@name': 'faqSections',
        '@path': '/bobcat/na/FAQ/main/0/faqSections',
        '@id': 'e52a778c-a43e-4b40-9338-2c2fdac0e889',
        '@nodeType': 'mgnl:contentNode',
        'mgnl:lastModified': '2022-01-03T21:00:41.791+01:00',
        faqSections0: {
          '@name': 'faqSections0',
          '@path': '/bobcat/na/FAQ/main/0/faqSections/faqSections0',
          '@id': 'd18e7536-b8c0-43c2-8d8d-b1ee7c842ef9',
          '@nodeType': 'mgnl:contentNode',
          faqSection: {
            '@name': 'First-section-here',
            '@path': '/bobcat/na/some-folder/First-section-here',
            '@id': '7f843396-c68f-482b-bc76-4a931594f03f',
            '@nodeType': 'dxp:faqSection',
            tags: [],
            title: 'First section title',
            'mgnl:lastModified': '2022-01-04T11:16:54.272+01:00',
            'Faq-first-question-': {
              '@name': 'Faq-first-question-',
              '@path': '/bobcat/na/some-folder/First-section-here/Faq-first-question-',
              '@id': 'e39080ac-298e-4306-83d1-594985e5c959',
              '@nodeType': 'dxp:faq',
              'mgnl:lastModified': '2022-01-04T11:16:54.272+01:00',
              answer: '<p>test test and more test</p>\n',
              question: 'This is the first question',
              '@nodes': []
            },
            'Second---name': {
              '@name': 'Second---name',
              '@path': '/bobcat/na/some-folder/First-section-here/Second---name',
              '@id': '0446ab1f-ef3b-463c-91c8-1add495b5af0',
              '@nodeType': 'dxp:faq',
              'mgnl:lastModified': '2022-01-04T11:16:54.272+01:00',
              answer: '<p>Second answer.</p>\n',
              question: 'Second question',
              '@nodes': []
            },
            '@nodes': ['Faq-first-question-', 'Second---name']
          },
          'mgnl:lastModified': '2022-01-03T21:00:41.791+01:00',
          '@nodes': []
        },
        faqSections1: {
          '@name': 'faqSections1',
          '@path': '/bobcat/na/FAQ/main/0/faqSections/faqSections1',
          '@id': 'cf6d99bb-3eeb-4bd1-8d0c-7ca991ee0d43',
          '@nodeType': 'mgnl:contentNode',
          faqSection: {
            '@name': 'Warranty',
            '@path': '/bobcat/na/some-folder/Warranty',
            '@id': '18070750-eabc-41b2-898e-8417d2c0d7aa',
            '@nodeType': 'dxp:faqSection',
            icon: 'fas_check-circle',
            title: 'Warranty',
            'mgnl:lastModified': '2022-01-04T11:16:54.272+01:00',
            Warranty: {
              '@name': 'Warranty',
              '@path': '/bobcat/na/some-folder/Warranty/Warranty',
              '@id': 'e1dd1253-c0a8-4e8c-b138-d1e3684a49e9',
              '@nodeType': 'dxp:faq',
              question: 'What is the warranty on compressors?',
              answer: '<p>The following charts summarize the general warranty information for Doosan Portable Power machines. To find the applicable warranty for your specific model click <a href="" target="_parent">here</a>. Test for <a href="z">broken link.</a></p>\n\n<p><a href="https://res.cloudinary.com/doosan-bobcat-dev/image/upload/v1601297432/MB/BB-Web/doosanportablepower/images/compressors/Compressor_P250_Model.jpg">Compressor_P250_Model</a></p>\n\n<p>&nbsp;</p>\n',
              'mgnl:lastModified': '2022-01-04T11:16:54.272+01:00',
              '@nodes': []
            },
            'extended-warranty-transferrable': {
              '@name': 'extended-warranty-transferrable',
              '@path': '/bobcat/na/some-folder/Warranty/extended-warranty-transferrable',
              '@id': '2f9b4ff8-2310-4c4d-9cf9-b8c2321e6185',
              '@nodeType': 'dxp:faq',
              answer: '<p>Yes, the extended warranty is transferrable. It is the obligation of the user to provide verification that these conditions have been satisfied when submitting claims under this optional warranty.</p>\n\n<p>Contact your dealer for specific information regarding the transfer of an extended warranty.</p>\n',
              question: 'Is the extended warranty transferrable.',
              'mgnl:lastModified': '2022-01-04T11:16:54.272+01:00',
              '@nodes': []
            },
            'extended-warranty': {
              '@name': 'extended-warranty',
              '@path': '/bobcat/na/some-folder/Warranty/extended-warranty',
              '@id': 'ba2338e9-6143-40ef-b729-56fd7e2966c5',
              '@nodeType': 'dxp:faq',
              answer: '<p>The Extended Airend Warranty is limited to defects in major components, and it is automatically available to the user when the following three conditions are met:</p>\n\n<ol>\n\t<li>The original airend is returned assembled and unopened.</li>\n\t<li>Use of genuive Doosan parts, fluids and filters.</li>\n\t<li>Maintenance is performed at the prescribed intervals listed in the compressor&rsquo;s maintenance manual</li>\n</ol>\n',
              question: 'How do I get an extended warranty?',
              'mgnl:lastModified': '2022-01-04T11:16:54.272+01:00',
              '@nodes': []
            },
            'extended-warranty-coverage': {
              '@name': 'extended-warranty-coverage',
              '@path': '/bobcat/na/some-folder/Warranty/extended-warranty-coverage',
              '@id': '43a6265e-387a-4af8-8c09-7e0d6f82b875',
              '@nodeType': 'dxp:faq',
              answer: '<p>Doosan Portable Power&#39;s extended warranty coverage is limited to defects in major components.</p>\n\n<p>It is applicable to the earlier of 5 years from compressor shipment or the accumulation of 10,000 hours of service by the user.</p>\n\n<p>Contact a dealer for more information specific to your machine.</p>\n\n<p>&nbsp;</p>\n',
              question: 'What does the extended warranty cover?',
              'mgnl:lastModified': '2022-01-04T11:16:54.272+01:00',
              '@nodes': []
            },
            '@nodes': ['Warranty', 'extended-warranty-transferrable', 'extended-warranty', 'extended-warranty-coverage']
          },
          'mgnl:lastModified': '2022-01-03T21:00:41.755+01:00',
          '@nodes': []
        },
        faqSections2: {
          '@name': 'faqSections2',
          '@path': '/bobcat/na/FAQ/main/0/faqSections/faqSections2',
          '@id': 'def01807-00bc-4c96-9fc2-0100da3d9d32',
          '@nodeType': 'mgnl:contentNode',
          faqSection: {
            '@name': 'Service',
            '@path': '/bobcat/na/some-folder/Service',
            '@id': 'f890bf4d-51ec-47bd-9cc5-71ce1397887d',
            '@nodeType': 'dxp:faqSection',
            icon: 'fal_cog',
            title: 'Service',
            'mgnl:lastModified': '2022-01-04T11:16:54.272+01:00',
            'Compressor--Maintenance': {
              '@name': 'Compressor--Maintenance',
              '@path': '/bobcat/na/some-folder/Service/Compressor--Maintenance',
              '@id': 'cec8e5ce-de6f-4c95-892d-e7c8b7035a02',
              '@nodeType': 'dxp:faq',
              answer: '<p>Lorem ipsum dolor sit amet, consectetur adipiscing elit. Aliquam fermentum vestibulum est. Cras rhoncus. Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. Sed quis tortor. Donec non ipsum. Mauris condimentum, odio nec porta tristique, ante neque malesuada massa, in dignissim eros velit at tellus. Donec et risus in ligula eleifend consectetur. Donec volutpat eleifend augue. Integer gravida sodales leo. Nunc vehicula neque ac erat. Vivamus non nisl. Fusce ac magna. Suspendisse euismod libero eget mauris.</p>\n',
              question: 'How often should I change the oil in my Compressor',
              'mgnl:lastModified': '2022-01-04T11:16:54.272+01:00',
              '@nodes': []
            },
            'Machine-Codes': {
              '@name': 'Machine-Codes',
              '@path': '/bobcat/na/some-folder/Service/Machine-Codes',
              '@id': '96ca38f0-1830-47ae-9a2c-322093d7f1f0',
              '@nodeType': 'dxp:faq',
              answer: '<p>Contact a <a href="http://www.maporama.com" target="_blank">Doosan certified service dealer</a> to supply information regarding machine codes.</p>\n',
              question: 'Where can I find a list of machine codes?',
              'mgnl:lastModified': '2022-01-04T11:16:54.272+01:00',
              '@nodes': []
            },
            '@nodes': ['Compressor--Maintenance', 'Machine-Codes']
          },
          'mgnl:lastModified': '2022-01-03T21:00:41.765+01:00',
          '@nodes': []
        },
        '@nodes': ['faqSections0', 'faqSections1', 'faqSections2']
      },
      '@nodes': ['faqSections']
    },
    '@nodes': ['0']
  },
  footer: {
    '@name': 'footer',
    '@path': '/bobcat/na/FAQ/footer',
    '@id': '0fac4926-785d-46bd-b4ce-744f45711764',
    '@nodeType': 'mgnl:area',
    'mgnl:lastModified': '2022-01-03T10:31:47.421+01:00',
    '@nodes': []
  },
  '@nodes': ['dxpTags', 'primaryPageImage', 'pageLink', 'header', 'main', 'footer']
}
