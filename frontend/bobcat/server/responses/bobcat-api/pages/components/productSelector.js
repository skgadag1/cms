export default {
  props: {
    noResultsMessage: 'No results found',
    label: 'Label',
    categoriesLabel: 'Categories',
    subcategoriesLabel: 'Subcategories',
    productGroupsLabel: 'Product Groups',
    productsLabel: 'Products',
    placeholder: 'Placeholder',
    clearAllLabel: 'Clear All',
    validationMessage: 'This field is required',
    name: 'productSelector',
    required: true,
    onChange: () => {}
  },
  productSelectorItems: {
    categories: [
      {
        identifier: 'Loaders',
        name: 'Loaders Title',
        subcategories: [
          {
            identifier: '105',
            name: 'Compact-Track-Loaders',
            productGroups: [
              {
                identifier: '',
                name: '',
                products: [
                  {identifier: 'D-53964', name: 'T750 T4 '},
                  {identifier: 'M0293', name: 'T870 T4'},
                  {identifier: 'M0349', name: 'T66 T4'},
                  {identifier: 'M0355', name: 'T62 T4'},
                  {identifier: 'M0363', name: 'T64 T4'}
                ]
              },
              {
                identifier: 'T450',
                name: 'T450',
                products: [{identifier: 'M0209', name: 'T450 T4 V2'}]
              },
              {
                identifier: 'T550',
                name: 'T550',
                products: [{identifier: 'M0255_50049', name: 'T550 T4 (RS)'}]
              },
              {
                identifier: 'T740',
                name: 'T770-TEST',
                products: [{identifier: 'M0277_53962', name: 'T740 T4 (RS)'}]
              }
            ]
          },
          {
            identifier: '104',
            name: 'Skid-Steer-Loaders',
            productGroups: [
              {
                identifier: '',
                name: '',
                products: [
                  {identifier: 'M0247', name: 'S595 T4'},
                  {identifier: 'M0269', name: 'S650 T4 Skid Steer'},
                  {identifier: 'M0275', name: 'S740 T4'},
                  {identifier: 'M0283', name: 'S770 T4'},
                  {identifier: 'M0289', name: 'S850 T4'}
                ]
              }
            ]
          },
          {
            identifier: '',
            name: '',
            productGroups: [
              {
                identifier: 'testGroup',
                name: 'test Group',
                products: [
                  {identifier: '', name: 'testmodel2'},
                  {identifier: '12345Test', name: 'test model'}
                ]
              },
              {
                identifier: '',
                name: '',
                products: [{identifier: 'M0277', name: 'T740 T4'}]
              }
            ]
          },
          {
            identifier: '107',
            name: 'Mini-Track-Loaders',
            productGroups: [
              {
                identifier: '',
                name: '',
                products: [
                  {identifier: 'M0045', name: 'MT55'},
                  {identifier: 'M0113', name: 'MT85'}
                ]
              }
            ]
          },
          {
            identifier: '56575',
            name: 'Small Articulated Loader',
            productGroups: [
              {
                identifier: '',
                name: '',
                products: [{identifier: 'M1401', name: 'L23'}]
              }
            ]
          }
        ]
      },
      {
        identifier: 'Excavators',
        name: 'Excavators',
        subcategories: [
          {
            identifier: '103',
            name: 'Compact-Excavators',
            productGroups: [
              {
                identifier: 'E32',
                name: 'E32',
                products: [
                  {identifier: 'M3205', name: 'Bobcat E32 (M-Series)'},
                  {identifier: 'M3205_49349', name: 'E32 T4'},
                  {identifier: 'M3309', name: 'Bobcat E32 SA 25 HP'}
                ]
              },
              {
                identifier: '',
                name: '',
                products: [{identifier: 'M5545', name: 'E10 T4'}]
              },
              {
                identifier: 'test-single-group',
                name: 'test single group',
                products: [{identifier: 'M3309_54366-1', name: 'E32R (LA)'}]
              }
            ]
          }
        ]
      }
    ]
  }
}
