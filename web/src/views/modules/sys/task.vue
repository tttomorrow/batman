<template>
  <el-card shadow="never" class="aui-card--fill">
    <div class="mod-sys__task">
      <el-form :inline="true" :model="dataForm" @keyup.enter.native="getDataList()">
        <el-form-item>
          <el-input v-model="dataForm.instanceName" :placeholder="$t('instance.instanceName')" clearable></el-input>
        </el-form-item>
        <el-form-item>
          <el-button @click="getDataList()">{{ $t('query') }}</el-button>
        </el-form-item>
        <el-form-item>
          <el-button type="danger" @click="deleteHandle()">{{ $t('deleteBatch') }}</el-button>
        </el-form-item>
        <el-form-item>
          <el-button type="info" @click="exportHandle()">{{ $t('export') }}</el-button>
        </el-form-item>
      </el-form>
      <el-table
        v-loading="dataListLoading"
        :data="dataList"
        border
        @selection-change="dataListSelectionChangeHandle"
        @sort-change="dataListSortChangeHandle"
        style="width: 100%;">
        <el-table-column type="selection" header-align="center" align="center" width="50"></el-table-column>
        <el-table-column prop="instanceId" :label="$t('instance.instanceName')" sortable="custom" header-align="center" align="center" :formatter="convertIdToName"></el-table-column>
        <el-table-column prop="name" :label="$t('task.taskName')" sortable="custom" header-align="center" align="center"></el-table-column>
        <el-table-column prop="startTime" :label="$t('task.startTime')" sortable="custom" header-align="center" align="center"></el-table-column>
        <el-table-column prop="endTime" :label="$t('task.endTime')" sortable="custom" header-align="center" align="center"></el-table-column>
        <el-table-column prop="size" :label="$t('task.size')" sortable="custom" header-align="center" align="center"></el-table-column>
        <el-table-column prop="status" :label="$t('task.status')" sortable="custom" header-align="center" align="center">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.status === 'SUCCESS'" size="small" type="success">{{ $t('task.success') }}</el-tag>
            <el-tag v-else-if="scope.row.status === 'FAILED'" size="small" type="danger">{{ $t('task.failed') }}</el-tag>
            <el-tag v-else size="small" type="info">{{ $t('task.running') }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="$t('handle')" fixed="right" header-align="center" align="center" width="150">
          <template slot-scope="scope">
            <el-button  v-if="scope.row.status === 'SUCCESS' && scope.row.type !== 'restore' && scope.row.backupId !== null" type="text" size="small" @click="restoreHandle(scope.row.id)">{{ $t('restore') }}</el-button>
            <el-button  type="text" size="small" @click="deleteHandle(scope.row.id)">{{ $t('delete') }}</el-button>
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
export default {
  mixins: [mixinViewModule],
  data () {
    return {
      instances: [],
      mixinViewModuleOptions: {
        getDataListURL: '/sys/task/list',
        getDataListIsPage: true,
        deleteURL: '/sys/task/delete',
        deleteIsBatch: true,
        exportURL: '/sys/task/export',
        restoreURL: '/sys/task/restore'
      },
      dataForm: {
        instanceName: ''
      }
    }
  },
  mounted() {
    if (this.timer) {
      clearInterval(this.timer);
    } else {
      this.timer = setInterval(() => {
        this.getDataList();
      }, 60000);
    }
  },
  destroyed() {
    clearInterval(this.timer);
  },
  created () {
    this.getInfo()
  },
  activated () {
    this.getInfo()
  },
  methods: {
    init () {
      this.instances = []
      this.$nextTick(() => {
        this.getInfo()
      })
    },
    // 获取当前实例信息，关联回填备份实例名称
    getInfo () {
      this.$http.get(`/sys/instance/list`).then(({ data: res }) => {
        if (res.code !== 0) {
          return this.$message.error(res.msg)
        }
        for (let i = 0; i < res.page.list.length; i++) {
          this.instances.push({id: res.page.list[i].id, name: res.page.list[i].instanceName})
        }
      }).catch(() => {})
    },
    convertIdToName(row) {
      for (let index = 0; index < this.instances.length; index++) {
        if (row.instanceId === this.instances[index].id) {
          return this.instances[index].name
        }
      }
      return ""
    },
    restoreHandle (id) {
      if (this.dataListSelections.length > 1) {
        return this.$message({
          message: this.$t('prompt.restoreMoreOne'),
          type: 'error',
          duration: 3000
        })
      }
      this.$confirm(this.$t('prompt.restore_info'), this.$t('prompt.warn'), {
        confirmButtonText: this.$t('confirm'),
        cancelButtonText: this.$t('cancel'),
        type: 'warning'
      }).then(() => {
        this.$http.put(
            `${this.mixinViewModuleOptions.restoreURL}${'/' + id}`
        ).then(({ data: res }) => {
          if (res.code !== 0) {
            return this.$message.error(res.msg)
          }
          this.$message({
            message: this.$t('prompt.success'),
            type: 'success',
            duration: 3000,
            onClose: () => {
              this.query()
            }
          })
        }).catch(() => {})
      }).catch(() => {})
    },
  }
}
</script>
