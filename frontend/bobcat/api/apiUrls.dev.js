module.exports = {
  CONTENT_API: '//content.api.dev.dice-tools.com/',
  MAGNOLIA_API: process.env.MOCK_MAGNOLIA > 0 ? `${process.env.MAGNOLIA_SERVER}` : '/'
}
