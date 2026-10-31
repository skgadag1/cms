/* eslint-disable object-property-newline */
export default [
  {
    id: 'be2765bf-3829-4a7e-8553-33ec8f251e06',
    type: 'megaList',
    label: 'Equipment',
    items: [{
      id: 'afedb030-5ee3-409d-b74a-930d95d781c5',
      type: 'megaListColumn',
      link: {url: '/equipment', target: '_self', external: true, label: 'View all equipment'},
      items: [{
        id: 'd1da2243-db99-4961-af7d-c8c15c83969a',
        type: 'megaListColumn',
        label: 'Loaders',
        link: {url: '/equipment/loaders', target: '_self', external: true, label: 'View All Loaders'},
        items: [{
          id: 'e5d8a090-6e22-4af0-aab1-6b4686859b4d',
          type: 'item',
          label: 'T450',
          link: {url: 'http://localhost:8080/fallback/pl/dealernet', target: '_self', external: false}
        }, {
          id: 'c7067e08-67aa-489f-a4ff-6d3866cc7a7a',
          type: 'item',
          label: 'T870',
          link: {url: '/na/en/sth', target: '_self', external: false}
        }, {
          id: 'dbbbf2bd-b361-48de-93e2-ccda98b51d02',
          type: 'megaListColumn',
          label: 'Skid-Steer Loaders',
          link: {url: '/equipment/loaders/ssl', target: '_self', external: true, label: 'View All Skid-Steer Loaders'},
          items: [{
            id: 'f6ba7294-1c41-4e79-af40-13464a87bcfb',
            type: 'item',
            label: 'T450',
            link: {url: '/equipment/loaders/ssl/T450', target: '_self', external: true}
          }, {
            id: '1830b3b8-422b-4e6e-a171-73b120e55234',
            type: 'item',
            label: 'T550',
            link: {url: '/equipment/loaders/ssl/T550', target: '_self', external: true}
          }, {
            id: '046be407-e17f-4146-ae8f-eba943f54bb3',
            type: 'item',
            label: 'T66',
            link: {url: '/equipment/loaders/ssl/T450', target: '_self', external: true}
          }]
        }, {
          id: 'e2face78-a781-4e95-a3f7-b9588f177dcd',
          type: 'megaListColumn',
          label: 'Compact Track Loaders',
          link: {url: '', target: '', external: false, label: ''},
          items: [{
            id: '3944aea6-14d9-45bb-985f-ed99d778f95b',
            type: 'item',
            label: 'MT100',
            link: {url: '/equipment/loaders/compact/mt100', target: '_blank', external: true}
          }, {
            id: '98b8db7e-976a-4a85-bb39-187aa2209959',
            type: 'megaListColumn',
            label: 'more',
            link: {url: '', target: '', external: false, label: ''},
            items: [{id: 'b7e2891f-46c4-4f8f-9775-a67f101b17bc', type: 'item', label: 'S570', link: {url: '/S570', target: '_self', external: true}}]
          }]
        }, {
          id: '1e1095c3-f7d8-4f35-8672-1ff1bb19a0ee',
          type: 'megaListColumn',
          label: 'Compact Wheel Loaders',
          link: {url: '', target: '', external: false, label: ''},
          items: [{
            id: '87c486dc-8903-439a-99e6-8dbc34b5561b',
            type: 'item',
            label: 'L75',
            link: {url: '/equipment/loaders/compact-wheel-loaders/L75', target: '_blank_follow', external: true}
          }, {
            id: '1df2b76e-fa70-4ead-abc4-e30672154467',
            type: 'item',
            label: 'L85',
            link: {url: '/equipment/loaders/compact-wheel-loaders/L85', target: '_blank', external: true}
          }]
        }]
      }, {
        id: 'eeb6f4a1-2c2c-481d-862c-f834fa6c126e',
        type: 'megaListColumn',
        label: 'Excavators',
        link: {url: '/equipment/excavators', target: '_self', external: true, label: 'View All Excavators'},
        items: [{
          id: '98903a3b-422d-406d-89cf-207a14aa591f',
          type: 'megaListColumn',
          label: 'Compact Excavators',
          link: {url: '/equipment/excavators', target: '_self', external: true, label: 'View All Compact Excavators'},
          items: [{
            id: 'f3f14a3d-8fbc-415a-bd76-cd28794e7822',
            type: 'item',
            label: 'E20',
            link: {url: '/na/en/no-follow-test', target: '_self', external: false}
          }, {id: '71b10ad4-e99e-4ef2-bc32-577139eef11c', type: 'item', label: 'E26', link: {url: '/equipment/excavators/e26', target: '_self', external: true}}]
        }]
      }]
    }]
  },
  {
    id: '123',
    type: 'linkMenu',
    label: 'Link menu',
    link: {
      url: 'https://ssr.dev.bobcat.com/bobcat/na/en/index',
      external: true,
      target: '_self'
    }
  },
  {
    id: '1234',
    type: 'listMenu',
    label: 'List menu',
    items: [
      {
        id: '123asd',
        type: 'item',
        label: 'Item 1',
        link: {
          url: 'https://ssr.dev.bobcat.com/bobcat/na/en/attachments-implements'
        }
      },
      {
        id: '12asd3',
        type: 'item',
        label: 'Item 2',
        link: {
          url: 'https://ssr.dev.bobcat.com/bobcat/na/en',
          external: true,
          target: '_blank'
        }
      }
    ]
  },
  {
    id: '12356779',
    type: 'megaMenu',
    label: 'Mega Menu',
    rows: [
      {
        id: '123alksjkjbb',
        type: 'megaMenuRow',
        name: 'row-1',
        columns: [
          {
            id: '123ajsdkajsd',
            type: 'megaMenuColumn',
            label: 'Row 1 Column 1',
            position: 'column1',
            items: [
              {
                id: '1sasd3',
                type: 'item',
                label: 'Compact Track Loaders',
                link: {
                  url: 'https://ssr.dev.bobcat.com/bobcat/na/equipment/subpage/Compact-Track-Loaders',
                  external: true,
                  target: '_blank'
                }
              },
              {
                id: '1lkm3',
                type: 'item',
                label: 'Item 2',
                link: {
                  url: 'https://ssr.dev.bobcat.com/bobcat/na/en/index'
                }
              }
            ]
          },
          {
            id: '398798798asdasd879',
            type: 'megaMenuColumn',
            label: 'Row 1 Column 2',
            position: 'column2',
            items: [
              {
                id: '123alksjksd7623423rnjdsv',
                type: 'item',
                label: 'Item 1',
                link: {
                  url: 'https://ssr.dev.bobcat.com/bobcat/na/en/sitemaps'
                }
              }
            ]
          },
          {
            id: 'kdfgpo56403956843562345',
            type: 'megaMenuColumn',
            label: 'Row 1 Column 1 (another)',
            position: 'column1',
            items: [
              {
                id: '1sasd3',
                type: 'item',
                label: 'Item 1',
                link: {
                  url: 'https://ssr.dev.bobcat.com/bobcat/na/en/about'
                }
              },
              {
                id: '1lkm3',
                type: 'item',
                label: 'Item 2',
                link: {
                  url: 'https://ssr.dev.bobcat.com/bobcat/na/en'
                }
              }
            ]
          }
        ]
      },
      {
        id: 'plamsplmasd-asd',
        type: 'megaMenuRow',
        name: 'row-2',
        columns: [
          {
            id: 'plamsplmasd-asd124',
            type: 'megaMenuColumn',
            label: 'Row-2-Column-2',
            position: 'column2',
            items: [
              {
                id: 'plamsplmasd-aasdasd454565',
                type: 'item',
                label: 'item-row-2',
                link: {
                  url: 'https://ssr.dev.bobcat.com/bobcat/na/en/equipment',
                  target: '_blank'
                }
              },
              {
                id: 'plam-asd',
                type: 'item',
                label: 'Custom Test active?',
                link: {
                  url: 'https://ssr.dev.bobcat.com/bobcat/na/en/testing',
                  target: '_self'
                }
              }
            ]
          },
          {
            id: 'kdfgpo564956843562345',
            type: 'megaMenuColumn',
            label: 'Row 2 Column 1',
            position: 'column1',
            items: [
              {
                id: '1sa345sd3',
                type: 'item',
                label: 'Item 1',
                link: {
                  url: 'https://ssr.dev.bobcat.com/bobcat/na/en/about'
                }
              },
              {
                id: '1456lkm3',
                type: 'item',
                label: 'Item 2',
                link: {
                  url: 'https://ssr.dev.bobcat.com/bobcat/na/en/index'
                }
              }
            ]
          }
        ]
      },
      {
        id: '123123msplmasd-asd',
        type: 'megaMenuRow',
        name: 'row-3',
        columns: [
          {
            id: 'plaasd1111asd-asd124',
            type: 'megaMenuColumn',
            label: 'Row-3-Column-3',
            position: 'column3',
            items: [
              {
                id: '345y5768sd-aasdasd454565',
                type: 'item',
                label: 'item-row-3',
                link: {
                  external: true,
                  url: 'https://ssr.dev.bobcat.com/bobcat/na/en/equipment'
                }
              },
              {
                id: 'plam-asd',
                type: 'item',
                label: 'Custom Test',
                link: {
                  external: true,
                  url: 'https://google.com'
                }
              }
            ]
          }
        ]
      }
    ]
  },
  {
    id: '1235677546',
    type: 'megaList',
    label: 'Mega List',
    items: [
      {
        id: '123ajsdjsd',
        type: 'megaListColumn',
        label: 'Column 1',
        link: {
          label: 'All Loaders',
          external: true,
          url: 'https://google.com',
          target: '_blank'
        },
        items: [
          {
            id: '1sas3',
            type: 'item',
            label: 'Compact Track Loaders',
            link: {
              url: 'https://ssr.dev.bobcat.com/bobcat/na/equipment/subpage/Compact-Track-Loaders',
              external: true,
              target: '_blank'
            }
          },
          {
            id: '1lkm3',
            type: 'item',
            label: 'Item 2',
            link: {
              url: 'https://ssr.dev.bobcat.com/bobcat/na/en/index'
            }
          },
          {
            id: '3987987dasd879',
            type: 'megaListColumn',
            label: 'Column 2 (more)',
            link: {
              url: 'https://ssr.dev.bobcat.com/bobcat/na/en/test',
              label: 'All Excavators'
            },
            items: [
              {
                id: '123alksjksd7623423rnjdsv',
                type: 'megaMenuColumn',
                label: 'Item 1 (more)',
                items: [
                  {
                    id: '1234rnjdsv',
                    type: 'item',
                    label: 'Item 1 (stop)',
                    link: {
                      url: 'https://ssr.dev.bobcat.com/bobcat/na/en'
                    }
                  }
                ]
              },
              {
                id: '123alksjk22sd7623423rnjdsv',
                type: 'item',
                label: 'Item 2',
                link: {
                  url: 'https://ssr.dev.bobcat.com/bobcat/na/en/sitemaps'
                }
              },
              {
                id: '123alksjk3sd7623423rnjdsv',
                type: 'item',
                label: 'Item 3',
                link: {
                  url: 'https://ssr.dev.bobcat.com/bobcat/na/en/sitemaps'
                }
              },
              {
                id: '123alksjk4sd7623423rnjdsv',
                type: 'item',
                label: 'Item 4',
                link: {
                  url: 'https://ssr.dev.bobcat.com/bobcat/na/en/sitemaps'
                }
              },
              {
                id: '123alksjk5sd7623423rnjdsv',
                type: 'item',
                label: 'Item 5',
                link: {
                  url: 'https://ssr.dev.bobcat.com/bobcat/na/en/sitemaps'
                }
              },
              {
                id: '123alksjk6sd7623423rnjdsv',
                type: 'item',
                label: 'Item 6',
                link: {
                  url: 'https://ssr.dev.bobcat.com/bobcat/na/en/sitemaps'
                }
              }
            ]
          },
          {
            id: 'kdfgpo56403912345',
            type: 'megaListColumn',
            label: 'Column 2 (another)',
            items: [
              {
                id: '1sasd3',
                type: 'item',
                label: 'Item 1',
                link: {
                  url: 'https://ssr.dev.bobcat.com/bobcat/na/en/about'
                }
              },
              {
                id: '1lkm3',
                type: 'item',
                label: 'Item 2',
                link: {
                  url: 'https://ssr.dev.bobcat.com/bobcat/na/en/sth'
                }
              }
            ]
          }
        ]
      }
    ]
  },
  {
    id: '1237890079056779',
    type: 'megaMenu',
    label: 'Mega Menu 2',
    rows: [
      {
        id: 'pla90lmasd-asd',
        type: 'megaMenuRow',
        name: 'row-2',
        columns: [
          {
            id: 'pla0988780msplmasd-asd124',
            type: 'megaMenuColumn',
            label: 'Row-2-Column-2',
            position: 'column2',
            items: [
              {
                id: '7890splmasd-aasdasd454565',
                type: 'item',
                label: 'item-row-2',
                link: {
                  url: 'https://ssr.dev.bobcat.com/bobcat/na/en/equipment'
                }
              },
              {
                id: 'pl79098am-asd',
                type: 'item',
                label: 'Custom Test 1',
                link: {
                  url: 'https://google.com'
                }
              }
            ]
          },
          {
            id: 'kdfgp09o564956843562345',
            type: 'megaMenuColumn',
            label: 'Row 2 Column 1',
            position: 'column1',
            items: [
              {
                id: '1sa30945sd3',
                type: 'item',
                label: 'Item 1',
                link: {
                  url: 'https://ssr.dev.bobcat.com/bobcat/na/en/about'
                }
              },
              {
                id: '109456lkm3',
                type: 'item',
                label: 'Item 2',
                link: {
                  url: 'https://ssr.dev.bobcat.com/bobcat/na/en/index'
                }
              }
            ]
          }
        ]
      },
      {
        id: '1098723123msplmasd-asd',
        type: 'megaMenuRow',
        name: 'row-3',
        columns: [
          {
            id: 'p0987laasd1111asd-asd124',
            type: 'megaMenuColumn',
            label: 'Row-3-Column-3',
            position: 'column3',
            items: [
              {
                id: '3098745y5768sd-aasdasd454565',
                type: 'item',
                label: 'item-row-3',
                link: {
                  url: 'https://ssr.dev.bobcat.com/bobcat/na/en/equipment'
                }
              },
              {
                id: '0987plam-asd',
                type: 'item',
                label: 'Custom Test 2',
                link: {
                  url: 'https://google.com',
                  external: true
                }
              }
            ]
          }
        ]
      }
    ]
  },
  {
    id: '1iufkjd23',
    type: 'linkMenu',
    label: 'Active /na/en',
    link: {
      url: 'https://ssr.dev.bobcat.com/bobcat/na/en/test',
      target: '_self'
    }
  }
]
