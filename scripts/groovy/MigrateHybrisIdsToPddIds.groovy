import info.magnolia.context.MgnlContext
import javax.jcr.Node
import javax.jcr.query.Query
import java.util.stream.Stream

EMEA_CLAUSE = ['eu', 'mea', 'cis'].collect {"ISDESCENDANTNODE('/bobcat/$it')"}.join(' or ')
// get this mapping from gen-id-mapping script in db-api-inner-product
MAPPING = ["exampleHybrisId":"examplePddId"]

List<Node> getNodes(String repository, String nodeType) {
    def session = MgnlContext.getJCRSession(repository)
    def queryManager = session.workspace.queryManager
    def query = queryManager.createQuery("select * from [$nodeType] where length(id) >= 6 and ($EMEA_CLAUSE)", Query.JCR_SQL2)

    return query.execute().nodes.toList()
}

static String joinPaths(List<Node> nodes) {
    return nodes.inject('', { result, x -> result + x.getPath() + '\n' })
}

static String joinIds(List<Node> nodes) {
    return nodes.inject('', { result, x -> result + "'" + x.getProperty('id').getValue().getString() + "',\n" })
}

void execute(boolean update = false) {
    def products = getNodes('products', 'dxp:product')
    println joinPaths(products)

    def attachments = getNodes('attachments', 'dxp:attachment')
    println joinPaths(attachments)

    if (update) {
        Stream.concat(products.stream(), attachments.stream()).forEach {
            def id = it.getProperty('id').getValue().getString()
            if (id in MAPPING) {
                try {
                    it.getProperty('id').setValue(MAPPING[id])
                } catch (e) {
                    println "Error occurred while mapping $id: $e"
                }
            } else {
                println "Item with id $id cannot be mapped"
            }
        }

        if (products.size() > 0) {
            products.get(0).session.save()
        }

        if (attachments.size() > 0) {
            attachments.get(0).session.save()
        }
    } else {
        println "Existing product IDs"
        println joinIds(products)

        println "Existing attachment IDs"
        println joinIds(attachments)
    }
}

def update() {
    execute(true)
}

def dryRun() {
    execute(false)
}

dryRun()
//update()