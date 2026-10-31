import javax.jcr.*
import javax.jcr.query.*

BASE_PROPERTY = 'isInheritanceDisabled'
DERIVED_PROPERTY = 'isInheritanceDisabledForPricing'

def session = MgnlContext.getJCRSession('products')
def queryManager = session.workspace.queryManager
def query = queryManager.createQuery("select * from [nt:base] where ${BASE_PROPERTY} = true", Query.JCR_SQL2)
//query.setLimit(1)

def List<Node> results = query.execute().nodes.toList()
def affected = results.inject('', { result, x -> result + x.getPath() + '\n' })

results.forEach { it.setProperty(DERIVED_PROPERTY, true) }
session.save()

println affected