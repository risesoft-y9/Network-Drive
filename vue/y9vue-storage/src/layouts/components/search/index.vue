<script lang="ts" setup>
    import { Search } from '@element-plus/icons';
    import { useSettingStore } from '@/store/modules/settingStore';
    import { onMounted } from 'vue-demi';

    const settingStore = useSettingStore();
    const searchVisible = ref(false);
    const searchKey = ref('');
    const fontSizeObj: any = inject('sizeObjInfo') || {};
    // 搜索功能
    const searchFunc = () => {
        const searchUrl = 'https://www.youshengyun.com/datacenter/officeInfo/goToSearchResult?searchName=';
        let windowObjSearch = window.open(searchUrl + searchKey.value, '_search');
    };

    // 添加点击搜索按钮事件
    onMounted(() => {
        // setTimeout( ()=> {
        //     document.getElementsByClassName('search')[0].addEventListener('click', () => {
        //     if (!searchVisible.value) {
        //         searchVisible.value = true
        //     }
        // })
        // }, 500)
    });
</script>

<template>
    <el-drawer v-model="searchVisible" :show-close="false" :z-index="9000" direction="ttb">
        <div :class="{ search: true, 'is-pc': settingStore.getDevice === 'pc' }">
            <el-input
                v-model="searchKey"
                class="input"
                placeholder="Please Input"
                @change="(searchVisible = false), searchFunc()"
                @focus="searchKey = ''"
            >
                <template #append>
                    <el-button
                        :icon="Search"
                        :size="fontSizeObj.buttonSize"
                        :style="{ fontSize: fontSizeObj.baseFontSize }"
                    />
                </template>
            </el-input>
        </div>
    </el-drawer>
</template>

<style lang="scss" scoped>
    .search {
        margin: 0 auto;

        &.is-pc {
            max-width: 50%;
        }
    }
</style>
