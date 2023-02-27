<template>
  <el-card shadow="never" class="aui-card--fill">
    <div class="mod-sys__log-operation">
      <el-form :inline="true" :model="dataForm" @keyup.enter.native="getDataList()">
        <el-form-item>
          <el-select v-model="dataForm.status" :placeholder="$t('instance.instanceName')" clearable>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button @click="getDataList()">{{ $t('query') }}</el-button>
        </el-form-item>
        <el-form-item>
          <el-button type="info" @click="exportHandle()">{{ $t('export') }}</el-button>
        </el-form-item>
      </el-form>
      <el-table v-loading="dataListLoading" :data="dataList" border @sort-change="dataListSortChangeHandle" style="width: 100%;">
        <el-table-column prop="username" :label="$t('log.userName')" header-align="center" align="center"></el-table-column>
        <el-table-column prop="logIp" :label="$t('log.logIp')" header-align="center" align="center"></el-table-column>
<!--        <el-table-column prop="instanceName" :label="$t('log.instanceName')" header-align="center" align="center"></el-table-column>-->
        <el-table-column prop="operation" :label="$t('log.operation')" header-align="center" align="center"></el-table-column>
        <el-table-column prop="operationTime" :label="$t('log.operationTime')" sortable="custom" header-align="center" align="center">
          <template slot-scope="scope">
            {{ `${scope.row.operationTime}` }}
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        :current-page="page"
        :page-sizes="[10, 20, 50, 100]"
        :page-size="limit"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="pageSizeChangeHandle"
        @current-change="pageCurrentChangeHandle">
      </el-pagination>
    </div>
  </el-card>
</template>

<script>
import mixinViewModule from '@/mixins/view-module'
export default {
  mixins: [mixinViewModule],
  data () {
    return {
      mixinViewModuleOptions: {
        getDataListURL: '/sys/log/list',
        getDataListIsPage: true,
        exportURL: '/sys/log/export'
      },
      dataForm: {
        status: ''
      }
    }
  }
}
</script>
