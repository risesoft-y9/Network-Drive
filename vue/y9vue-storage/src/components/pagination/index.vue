<!--
 * @Author: your name
 * @Date: 2022-04-25 09:59:20
 * @LastEditTime: 2022-05-05 10:26:00
 * @LastEditors: Please set LastEditors
 * @Description: 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 * @FilePath: \y9vue-app\sz-team-frontend-vue3.x\y9vue-kernel\src\components\pagination\index.vue
-->

<template>
    <div class="block">
        <el-pagination
            :background="true"
            :current-page="currentPage"
            :page-size="pageSize"
            :page-sizes="[20, 50, 100, 200]"
            :total="total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
        >
        </el-pagination>
    </div>
</template>

<script setup>
    import { defineProps } from 'vue';

    const props = defineProps({
        currentPage: Number,
        total: Number
    });
</script>
