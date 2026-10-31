import info.magnolia.context.MgnlContext
import javax.jcr.Session
import javax.jcr.Node
import javax.jcr.RepositoryException
import javax.jcr.query.Query
def params = [
    workspace: 'products',
    productRootPath: '/bobcat',
    region: 'xd',
    category: 'Xd',
    dryRun: true,
    processOnlyMappedIds: true,
    saveEvery: 50,
    runForAllRegionsAndCategories: true,
]
final String MODEL_ID_PROP = 'id'
final String NODE_TYPE_PRODUCT = 'dxp:product'
final String LIFECYCLE_PROP = 'lifecycle'
final String LIFECYCLE_HISTORICAL = 'Historical'
/** OLD IDS -> NEW IDS
 In order to transform CSV mapping to a Groovy map, you can use the following regex:
   Match: ^([^;]*);([^;]*);.*
   Replace: "$2":"$1",
*/
def idMap = [
    "46883098":"46883098",
    "44032779":"44032779",
    "46867638":"46867638",
    "46867276":"46867276",
    "46867342":"46867342",
    "46863620":"46863620",
    "46863756":"46863756",
    "46864230":"46864230",
    "46864336":"46864336",
    "46864665":"46864665",
    "46864666":"46864666",
    "46867424":"46867424",
    "46864582":"46864582",
    "46864757":"46864757",
    "46864826":"46864826",
    "46926267":"46926267",
    "46926358":"46926358",
    "46864409":"46864409",
    "46866203":"46866203",
    "46866355":"46866355",
    "46876453":"46876453",
    "46866776":"46866776",
    "46866509":"46866509",
    "46866386":"46866386",
    "46866417":"46866417",
    "46866448":"46866448",
    "46866479":"46866479",
    "46883619":"46883619",
    "46887458":"46887458",
    "46866079":"46866079",
    "46865762":"46865762",
    "46885901":"46885901",
    "M0701":"7399150",
    "M0705":"7472411",
    "M4816":"46895498",
    "M1623":"7405793",
    "M1624":"7405827",
    "M1626":"7420343",
    "M1627":"7420344",
    "M1628":"7420345",
    "M1629":"7420346",
    "M1630":"7420347",
    "M1631":"7420348",
    "M1609":"7367233",
    "M1611":"7367241",
    "M1632":"7420349",
    "M1621":"7367519",
    "M1633":"7420350",
    "M1625":"7417512",
    "M1634":"7420351",
    "M0689":"46940211",
    "46886755":"46886755",
    "46886791":"46886791",
    "46886828":"46886828",
    "46886848":"46886848",
    "46888808":"46888808",
    "46888832":"46888832",
    "46888856":"46888856",
    "46888876":"46888876",
    "46892172":"46892172",
    "46947691":"46947691",
    "46947663":"46947663",
    "46886960":"46886960",
    "46886980":"46886980",
    "46947693":"46947693",
    "46957091":"46957091",
    "46957092":"46957092",
    "46957093":"46957093",
    "46947666":"46947666",
    "46947665":"46947665",
    "46954899":"46954899",
    "46952243":"46952243",
    "46954893":"46954893",
    "46887001":"46887001",
    "46887022":"46887022",
    "46888260":"46888260",
    "46888282":"46888282",
    "46888298":"46888298",
    "46888762":"46888762",
    "46888917":"46888917",
    "46888918":"46888918",
    "46888919":"46888919",
    "M5559":"7487742",
    "M4850":"46895660",
    "M1635":"7420352",
    "M1636":"7420353",
    "M1637":"7420354",
    "M1638":"7420355",
    "M1639":"7420356",
    "M1641":"7420358",
    "M1619":"7367491",
    "M1620":"7367502",
    "M1622":"7367527",
    "M0209":"7417261",
    "M0213":"46939831",
    "M0677":"7417771",
    "M0679":"7417773",
    "M0687":"46940209",
    "M0400":"7524945",
    "M0401":"7530343",
    "46866240":"46866240",
    "46866806":"46866806",
    "46866900":"46866900",
    "46867607":"46867607",
    "M6171":"7523226",
    "46865957":"46865957",
    "46867106":"46867106",
    "46867734":"46867734",
    "46871819":"46871819",
    "46872370":"46872370",
    "46870436":"46870436",
    "46871076":"46871076",
    "46873039":"46873039",
    "46882627":"46882627",
    "46874364":"46874364",
    "46883012":"46883012",
    "46865871":"46865871",
    "46866717":"46866717",
    "46897166":"46897166",
    "?":"7494428",
    "M5545":"7455282",
    "M5557":"7490643",
    "M5593":"46895624",
    "M5591":"46895625",
    "M5555":"7362640",
    "7525781":"7525781",
    "9990701":"7448935",
    "9990702":"7448937",
    "9990703":"7448938",
    "9990702EU":"7501493",
    "9990703EU":"7501497",
    "9997064":"7456024",
    "9997065":"7456025",
    "9996010EU":"7501731",
    "9996011EU":"7502246",
    "9996010":"9996010",
    "9996011":"9996011",
    "9996013":"9996013",
    "9997010":"9997010",
    "9997011":"9997011",
    "9996010AU":"9996010AU",
    "9997010AU":"9997010AU",
    "9997011AU":"9997011AU",
    "M6201":"7528640",
    "M6202":"7528645",
    "M6203":"7532366",
    "M6205":"7533625",
    "M0115":"7395378",
    "M0117":"7407109",
    "46870581":"46870581",
    "46871019":"46871019",
    "M1403":"7339210",
    "M1402":"7373200",
    "M1401":"7339200",
    "M1405":"7339220",
    "M1404":"7373210",
    "M6132":"7521551",
    "M6131":"7521782",
    "M6231":"46916145",
    "M6232":"46916169",
    "M0211":"7417260",
    "M0215":"46939229",
    "M0673":"7417765",
    "M0675":"7417768",
    "M0683":"46939351",
    "M0685":"46939829",
    "M4831":"46903782",
    "M4833":"46948375",
    "M1642":"7514312",
    "M1643":"7514809",
    "M1644":"7514817",
    "M6100":"7521785",
    "M6101":"7521802",
    "M6216":"7533866",
    "M6226":"46952279",
    "M6228":"46952281",
    "M6227":"46911103",
    "M6221":"46920254",
    "M6222":"46921458",
    "M6223":"46921507",
    "M6225":"46921630",
    "46864582":"46864582",
    "46864757":"46864757",
    "46864826":"46864826",
    "46866776":"46866776",
    "46876453":"46876453",
    "46897166":"46897166",
    "46926267":"46926267",
    "46926358":"46926358",
    "46926401":"46926401",
    "46926453":"46926453",
    "46934943":"46934943",
    "46934983":"46934983",
    "M0083":"7127774",
    "M0085":"7127778",
    "M0115":"7395378",
    "M0117":"7407109",
    "M0209":"7417261",
    "M0211":"7417260",
    "M0213":"46939831",
    "M0215":"46939229",
    "M0231":"7436240",
    "M0233":"7436246",
    "M0273":"7536116",
    "M0283":"7201749",
    "M0285":"7201773",
    "M0347":"7303346",
    "M0349":"7308760",
    "M0359":"7324616",
    "M0362":"7391218",
    "M0363":"7324622",
    "M0364":"7391224",
    "M0369":"7297439",
    "M0370":"7388622",
    "M0371":"7297444",
    "M0372":"7388628",
    "M0374":"7435254",
    "M0376":"7435256",
    "M0378":"7481130",
    "M0382":"7403495",
    "M0383":"7318997",
    "M0384":"7409370",
    "M0385":"7319003",
    "M0386":"7409376",
    "M0400":"7524945",
    "M0401":"7530343",
    "M0501":"46930468",
    "M0503":"46930115",
    "M0504":"46974598",
    "M0505":"46930353",
    "M0507":"46930519",
    "M0508":"46938038",
    "M0509":"46930371",
    "M0510":"46938039",
    "M0511":"46930481",
    "M0513":"46930142",
    "M0514":"46974601",
    "M0515":"46930520",
    "M0517":"46930368",
    "M0518":"46938040",
    "M0519":"46930521",
    "M0522":"46938041",
    "M0523":"46933991",
    "M0525":"46933992",
    "M0673":"7417765",
    "M0675":"7417768",
    "M0677":"7417771",
    "M0679":"7417773",
    "M0683":"46939351",
    "M0685":"46939829",
    "M0687":"46940209",
    "M1225":"7350653",
    "M1227":"7350654",
    "M1401":"7339200",
    "M1402":"7373200",
    "M1403":"7339210",
    "M1404":"7373210",
    "M1405":"7339220",
    "M1501":"7399066",
    "M1503":"7341000",
    "M1505":"7399067",
    "M1507":"7341002",
    "M1509":"7341001",
    "M1511":"7341003",
    "M1609":"7367233",
    "M1611":"7367241",
    "M3321":"7327138",
    "M3327":"7345115",
    "M3407":"7413249",
    "M3409":"7413322",
    "M3414":"7531415",
    "M3415":"7458707",
    "M3417":"7517299",
    "M3419":"7525285",
    "M4816":"46895498",
    "M4831":"46903782",
    "M4833":"46948375",
    "M5545":"7455282",
    "M5555":"7362640",
    "M5557":"7490643",
    "M5591":"46895625",
    "M5593":"46895624",
    "M7011":"6959135",
    "M7012":"6959136",
    "M7013":"6959134",
    "M7014":"7105217",
    "M7015":"6959126",
    "M7016":"7109273",
    "M7040":"7273815",
    "M7043":"7394466",
    "M7044":"7394472",
    "M7045":"7369538",
    "M7046":"7389651",
    "M7047":"7389654",
    "M7048":"7389655",
    "M7049":"7389656",
    "M7052":"7389659",
    "M7053":"7389660",
    "M7054":"7389661",
    "M7055":"7416884",
    "M7056":"7416920",
    "M7057":"7416745",
    "M7058":"7416935",
    "M7059":"46902517",
    "M7060":"46902518"
]
def log = [
    info: { String m -> println "[INFO] ${m}" },
    warn: { String m -> println "[WARN] ${m}" },
    error:{ String m -> println "[ERROR] ${m}" }
]
Session session = null
Node findChildIgnoreCase(Node parent, String name) {
    def it = parent.getNodes()
    while (it.hasNext()) {
        def n = it.nextNode()
        if (n.name?.trim()?.equalsIgnoreCase(name?.trim())) return n
    }
    return null
}
boolean hasNodeSafe(Node parent, String childName) {
    try { return parent.hasNode(childName) } catch (Exception ignore) { return false }
}
String nextAvailableChildName(Node parent, String baseName) {
    String candidate = baseName
    int i = 0
    while (hasNodeSafe(parent, candidate)) {
        i++
        candidate = "${baseName}-${i}"
    }
    return candidate
}
try {
    session = MgnlContext.getJCRSession(params.workspace as String)
    log.info("Connected to workspace: ${params.workspace}")
    if (!session.nodeExists(params.productRootPath as String)) {
        log.error("Product root not found: ${params.productRootPath}")
        return
    }
    def root = session.getNode(params.productRootPath as String)
    def scopeNode
    if (params.runForAllRegionsAndCategories) {
        scopeNode = root
        log.info("runForAllRegionsAndCategories=true: Processing all nodes under ${scopeNode.getPath()}")
    } else {
        def regionNode = findChildIgnoreCase(root, params.region as String)
        if (!regionNode) {
            log.error("Region folder not found: ${params.region}")
            return
        }
        scopeNode = regionNode
        if (params.category) {
            def categoryNode = findChildIgnoreCase(regionNode, params.category as String)
            if (!categoryNode) {
                log.error("Category folder not found: ${params.category} under region ${params.region}")
                return
            }
            scopeNode = categoryNode
        }
    }
    // Query only product nodes under the chosen scope
    String scopePath = scopeNode.getPath().replace("'", "''")
    String stmt = """
        SELECT * FROM [${NODE_TYPE_PRODUCT}] AS p
        WHERE ISDESCENDANTNODE(p, '${scopePath}')
          AND p.[${MODEL_ID_PROP}] IS NOT NULL
    """.trim()
    def qm = session.workspace.queryManager
    def q = qm.createQuery(stmt, Query.JCR_SQL2)
    def result = q.execute()
    int processed = 0
    int changed = 0
    def nodes = result.nodes
    while (nodes.hasNext()) {
        Node product = nodes.nextNode()
        processed++
        String oldId
        try {
            oldId = product.getProperty(MODEL_ID_PROP).string
        } catch (Exception e) {
            log.warn("Skipping ${product.getPath()}: can't read ${MODEL_ID_PROP} (${e.message})")
            continue
        }
        // Only run for CURRENT (non-historical) products
        boolean isHistorical =
                (product.hasProperty(LIFECYCLE_PROP) &&
                 product.getProperty(LIFECYCLE_PROP).string?.trim()?.equalsIgnoreCase(LIFECYCLE_HISTORICAL))
        if (isHistorical) {
            continue
        }
        String newId = idMap[oldId]
        boolean isMapped = (newId != null && newId != oldId)
        // Only proceed if id exists in map
        if (params.processOnlyMappedIds && !isMapped) {
            continue
        }
        if (!isMapped) {
            continue
        }
        Node parent = product.parent
        String originalName = product.name
        String legacyName = nextAvailableChildName(parent, "${originalName}-legacy")
        if (params.dryRun) {
            log.info("[DRY] Would copy ${product.getPath()} -> ${parent.getPath()}/${legacyName} (lifecycle=Historical, id=${oldId}), then set ${product.getPath()} id ${oldId} -> ${newId}")
            continue
        }
        // 1) Create legacy copy with -legacy suffix
        session.workspace.copy(product.getPath(), parent.getPath() + "/" + legacyName)
        Node legacyNode = parent.getNode(legacyName)
        // 2) Mark legacy copy as historical (keeps old id)
        legacyNode.setProperty(LIFECYCLE_PROP, LIFECYCLE_HISTORICAL)
        // 3) Update original node with new id
        product.setProperty(MODEL_ID_PROP, newId)
        changed++
        log.info("Updated: original=${product.getPath()} id ${oldId}->${newId}; legacy=${legacyNode.getPath()} lifecycle=Historical id=${oldId}")
        if (params.saveEvery && (changed % (params.saveEvery as int) == 0)) {
            session.save()
            log.info("Intermediate save: changed=${changed}")
        }
    }
    if (!params.dryRun) {
        session.save()
        log.info("Done. Scanned=${processed}, changed=${changed}")
    } else {
        log.info("Dry run done. Scanned=${processed}")
    }
} catch (RepositoryException re) {
    log.error("Repository exception: ${re.class.name}: ${re.message}")
} finally {
    try { session?.logout() } catch (Exception ignore) {}
}