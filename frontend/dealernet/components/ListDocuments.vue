<template>
    <div class="container py-5" v-if="assetList.length">
        <div class="col-12">
            <h3 class="d-flex align-items-center">{{ componentTitle }} <span class="ms-2 rounded-circle badge bg-primary">{{assetList.length}}</span></h3>
            <hr/>
        </div>

        <div class="col-12">
            <table class="table table-sm small">
                <thead class="bg-dark text-white">
                    <tr>
                        <th scope="col">File name</th>
                        <th scope="col">Description</th>
                        <th scope="col">Download</th>
                    </tr>
                </thead>

                <tbody class="bg-white">
                    <tr v-for="a in assetList" :key="`download-file-${a['@id']}`" :title="a.description">
                        <td>{{a.fileName}}</td>
                        <td>{{a.description}}</td>
                        <td>
                            <a :href="a.url" target="_blank" class="d-inline-flex align-items-center">
                                Download
                                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-download ms-2" viewBox="0 0 16 16">
                                    <path d="M.5 9.9a.5.5 0 0 1 .5.5v2.5a1 1 0 0 0 1 1h12a1 1 0 0 0 1-1v-2.5a.5.5 0 0 1 1 0v2.5a2 2 0 0 1-2 2H2a2 2 0 0 1-2-2v-2.5a.5.5 0 0 1 .5-.5z"/>
                                    <path d="M7.646 11.854a.5.5 0 0 0 .708 0l3-3a.5.5 0 0 0-.708-.708L8.5 10.293V1.5a.5.5 0 0 0-1 0v8.793L5.354 8.146a.5.5 0 1 0-.708.708l3 3z"/>
                                </svg>
                            </a>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>
    </div>
</template>

<script setup>
const props = defineProps({
  assets: [String, Object]
})

const {$cloudinary} = useNuxtApp()

const componentTitle = (useVisibility().getVisibleContent(props.assets))?.title
const assetList = (useVisibility().getVisibleContent(props.assets))?.asset?.['@nodes'].map(n => {
  const assetObject = (useVisibility().getVisibleContent(props.assets))?.asset?.[n]

  return { '@id': assetObject['@id'], fileName: assetObject?.fileName, description: assetObject?.description, url: $cloudinary.getCloudinaryDocument(assetObject?.asset?.filePath) }
})
</script>