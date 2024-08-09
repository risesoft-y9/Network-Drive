<template>
    <div class="wrap">
      <iframe :src="pdfUrl"  width="80%" height="80%"></iframe>
    </div>
  </template>
  
  <script setup lang="ts">
  import { reactive, onMounted,ref, computed,defineProps } from "vue";

  const props = defineProps({
    pdfUrl: {
        type: String,
        required: true
    }
})


const pdfUrl = ref('');
 
onMounted(() => {
    loadPDF();
});
async function loadPDF() { 
    // pdf路径，将文件放到public下的时候注意路径，否则会提示 xxx>.
    pdfUrl.value = '../../public/pdfjs-dist/web/viewer.html?file='+props.pdfUrl;
}
  </script>
  
  <style scoped>
  .wrap {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    bottom: 0;
  }
  </style>