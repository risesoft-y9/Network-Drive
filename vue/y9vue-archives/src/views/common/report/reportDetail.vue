<template>
    <div class="reportDetail">
        <p class="spanCss">档案四性检测报告</p>
        <table border="0" cellpadding="0" cellspacing="1" class="layui-table" lay-skin="line row">
            <tbody>
                <tr>
                    <td colspan="4" class="toptd">基本信息</td>
                </tr>
                <tr>
                    <td class="lefttd">检测时间</td>
                    <td class="righttd" style="text-align: center">
                    </td>
                    <td class="lefttd">检测耗时</td>
                    <td class="righttd" style="text-align: center">
                    </td>
                </tr>
                <tr>
                    <td class="lefttd">检测环节</td>
                    <td class="righttd" style="text-align: center">归档环节
                    </td>
                    <td class="lefttd">检测数量</td>
                    <td class="righttd" style="text-align: center">
                    </td>
                </tr>
                <tr>
                    <td class="lefttd">检测结果</td>
                    <td class="righttd" colspan="3">
                        
                    </td>
                </tr>
            </tbody>
        </table>
        <table border="0" cellpadding="0" cellspacing="1" class="layui-table" lay-skin="line row">
            <tbody>
                <tr>
                    <td colspan="4" class="toptd">完整性检测</td>
                </tr>
                <tr>
                    <td class="td1 td-center">检测编号</td>
                    <td class="td2 td-center">检测项</td>
                    <td class="td3 td-center">检测目的</td>
                    <td class="td4 td-center">检测结果</td>
                </tr>
                <tr>
                    <td class="td1">GD-1-1</td>
                    <td class="td2">元数据XML签名检测</td>
                    <td class="td3">保证电子文件的来源真实</td>
                    <td class="td4">通过</td>
                </tr>
                <tr>
                    <td class="td1">GD-1-10</td>
                    <td class="td2">电子原文属性一致性检测</td>
                    <td class="td3">保证电子文件内容数据电子属性的一致性</td>
                    <td class="td4">通过</td>
                </tr>
                <tr>
                    <td class="td1">GD-1-11-1</td>
                    <td class="td2">卷内文件完整性检测</td>
                    <td class="td3">保证电子文件元数据与内容数据的关联</td>
                    <td class="td4">通过</td>
                </tr>
                <tr>
                    <td class="td1">GD-1-12</td>
                    <td class="td2">归档说明XML文件规范性检测</td>
                    <td class="td3">保证归档信息包信息组织结构和内容符合归档要求</td>
                    <td class="td4">通过</td>
                </tr>
                <tr>
                    <td class="td1">GD-1-13</td>
                    <td class="td2">信息包目录结构规范性检测</td>
                    <td class="td3">保证归档信息包信息组织结构和内容符合归档要求</td>
                    <td class="td4">通过</td>
                </tr>
                <tr>
                    <td class="td1">GD-1-14</td>
                    <td class="td2">归档目录XML文件签名检测</td>
                    <td class="td3">保证信息包在归档前后完全一致</td>
                    <td class="td4">通过</td>
                </tr>
                <tr>
                    <td class="td1">GD-1-2</td>
                    <td class="td2">元数据项数据长度检测</td>
                    <td class="td3">检测元数据项数据长度是否符合要求</td>
                    <td class="td4">通过</td>
                </tr>
                <tr>
                    <td class="td1">GD-1-3</td>
                    <td class="td2">元数据项数据类型、格式检测</td>
                    <td class="td3">检测元数据项数据类型、格式是否符合要求</td>
                    <td class="td4">通过</td>
                </tr>
                <tr>
                    <td class="td1">GD-1-4</td>
                    <td class="td2">设定值域的元数据项值域符合度检测</td>
                    <td class="td3">检测设定值域的元数据项的数据是否符合值域要求</td>
                    <td class="td4">通过</td>
                </tr>
                <tr>
                    <td class="td1">GD-1-5</td>
                    <td class="td2">元数据项数据值合理性检测</td>
                    <td class="td3">检测元数据项数据值是否在合理范围内</td>
                    <td class="td4">通过</td>
                </tr>
                <tr>
                    <td class="td1">GD-1-6</td>
                    <td class="td2">元数据项数据包含特殊字符检测</td>
                    <td class="td3">检测元数据项数据中是否包含特殊字符</td>
                    <td class="td4">通过</td>
                </tr>
                <tr>
                    <td colspan="4" class="toptd">真实性检测</td>
                </tr>
                <tr>
                    <td class="td1 td-center">检测编号</td>
                    <td class="td2 td-center">检测项</td>
                    <td class="td3 td-center">检测目的</td>
                    <td class="td4 td-center">检测结果</td>
                </tr>
                <tr>
                    <td class="td1">GD-2-1</td>
                    <td class="td2">总件数相符性检测</td>
                    <td class="td3">保证归档电子文件数量和实际接收数量相符</td>
                    <td class="td4">通过</td>
                </tr>
                <tr>
                    <td class="td1">GD-2-11</td>
                    <td class="td2">档案数量匹配检测</td>
                    <td class="td3">保证归档信息包中内容数据齐全、完整</td>
                    <td class="td4">通过</td>
                </tr>
                <tr>
                    <td class="td1">GD-2-2</td>
                    <td class="td2">总字节数相符性检测</td>
                    <td class="td3">保证归档电子文件字节数和实际接收字节数相符</td>
                    <td class="td4">通过</td>
                </tr>
                <tr>
                    <td class="td1">GD-2-3</td>
                    <td class="td2">元数据项完整性检测</td>
                    <td class="td3">保证电子文件元数据项的完整性</td>
                    <td class="td4">通过</td>
                </tr>
                <tr>
                    <td class="td1">GD-2-4</td>
                    <td class="td2">元数据必填著录项目检测</td>
                    <td class="td3">保证电子文件元数据必填项的完整性</td>
                    <td class="td4">通过</td>
                </tr>
                <tr>
                    <td class="td1">GD-2-5</td>
                    <td class="td2">过程信息完整性检测</td>
                    <td class="td3">保证电子文件过程信息的完整性</td>
                    <td class="td4">通过</td>
                </tr>
                <tr>
                    <td class="td1">GD-2-6</td>
                    <td class="td2">连续性元数据项检测</td>
                    <td class="td3">保证电子文件元数据的连续性</td>
                    <td class="td4">通过</td>
                </tr>
                <tr>
                    <td colspan="4" class="toptd">可用性检测</td>
                </tr>
                <tr>
                    <td class="td1 td-center">检测编号</td>
                    <td class="td2 td-center">检测项</td>
                    <td class="td3 td-center">检测目的</td>
                    <td class="td4 td-center">检测结果</td>
                </tr>
                <tr>
                    <td class="td1">GD-3-1</td>
                    <td class="td2">元数据XML可读性检测</td>
                    <td class="td3">保证电子文件元数据可正常读取</td>
                    <td class="td4">通过</td>
                </tr>
                <tr>
                    <td class="td1">GD-3-4</td>
                    <td class="td2">电子原文可读性检测</td>
                    <td class="td3">保证特定格式的电子文件内容数据可读</td>
                    <td class="td4">通过</td>
                </tr>
            </tbody>
        </table>
    </div>
</template>
<script lang="ts" setup>
import { ref, reactive, toRefs } from "vue";

const data = reactive({
    formData: {},
    testing:[]
});

let {formData,testing} = toRefs(data);
</script>
<style lang="scss" scoped>
.reportDetail{
    .spanCss {
        font-weight: 500;
        font-size: 24px;
        text-align: center;
    }
    .layui-table {
        width: 100%;
        border-collapse: collapse;
        border-spacing: 0;

        td {
            position: revert;
            padding: 5px 10px;
            min-height: 32px;
            line-height: 32px;
            font-size: 14px;
            border-width: 1px;
            border-style: solid;
            border-color: #e6e6e6;
            display: table-cell;
            vertical-align: inherit;
        }

        .toptd {
            background: #f5f7fa;
            text-align: center;
            font-weight: 600;
            font-family: '微软雅黑';
        }
        .lefttd{
            width: 14%;
        }
        .righttd{
            width: 35%;
            flex-wrap: wrap;
            word-break: break-all;
            white-space: pre-wrap;
        }
        .td-center{
            text-align: center;
        }
        .td1 {
            width: 14%;
        }

        .td2 {
            flex-wrap: wrap;
            word-break: break-all;
            white-space: pre-wrap;
        }
        .td3 {
            flex-wrap: wrap;
            word-break: break-all;
            white-space: pre-wrap;
        }
        .td4 {
            text-align: center;
            width: 14%;
        }
    }
}
</style>