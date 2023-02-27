<template>
  <el-card shadow="never" class="aui-card--fill">
    <div class="mod-sys__instance">
      <el-form :inline="true" :model="dataForm" @keyup.enter.native="getDataList()">
        <el-form-item>
          <el-input v-model="dataForm.instanceName" :placeholder="$t('instance.instanceName')" clearable></el-input>
        </el-form-item>
        <el-form-item>
          <el-button @click="getDataList()">{{ $t('query') }}</el-button>
        </el-form-item>
        <el-form-item>
          <el-button  type="primary" @click="addOrUpdateHandle()">{{ $t('instance.add') }}</el-button>
        </el-form-item>
        <el-form-item>
          <el-button  type="danger" @click="deleteHandle()">{{ $t('deleteBatch') }}</el-button>
        </el-form-item>
      </el-form>
      <el-table v-loading="dataListLoading" :data="dataList" border @selection-change="dataListSelectionChangeHandle" style="width: 100%;">
        <el-table-column type="selection" header-align="center" align="center" width="50"></el-table-column>
        <el-table-column prop="instanceName" :label="$t('instance.instanceName')" header-align="center" align="center"></el-table-column>
        <el-table-column prop="instanceVersion" :label="$t('instance.instanceVersion')" header-align="center" align="center"></el-table-column>
        <el-table-column prop="instanceIp" :label="$t('instance.ip')" header-align="center" align="center"></el-table-column>
        <el-table-column prop="backupPath" :label="$t('instance.backupPath')" header-align="center" align="center"></el-table-column>
        <el-table-column prop="createTime" :label="$t('instance.createTime')" header-align="center" align="center"></el-table-column>
        <el-table-column prop="remark" :label="$t('instance.remark')" header-align="center" align="center"></el-table-column>
        <el-table-column :label="$t('handle')" fixed="right" header-align="center" align="center" width="150">
          <template slot-scope="scope">
            <el-button  type="text" size="small" @click="addOrUpdateHandle(scope.row.id)">{{ $t('update') }}</el-button>
            <el-button type="text" size="small" @click="deleteHandle(scope.row.id)">{{ $t('delete') }}</el-button>
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
      <!-- 弹窗, 新增 / 修改 -->
      <add-or-update v-if="addOrUpdateVisible" ref="addOrUpdate" @refreshDataList="getDataList"></add-or-update>
    </div>
  </el-card>
</template>

<script>
import mixinViewModule from '@/mixins/view-module'
import AddOrUpdate from './instance-add-or-update.vue'
export default {
  mixins: [mixinViewModule],
  data () {
    return {
      mixinViewModuleOptions: {
        getDataListURL: '/sys/instance/list',
        getDataListIsPage: true,
        deleteURL: '/sys/instance/delete',
        deleteIsBatch: true
      },
      dataForm: {
        id: '',
        instanceName: ''
      }
    }
  },
  components: {
    AddOrUpdate
  }
}
</script>
