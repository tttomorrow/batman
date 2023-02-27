<template>
  <el-dialog :visible.sync="visible" :title="!dataForm.id ? $t('job.addFull') : $t('job.updateFull')" :close-on-click-modal="false" :close-on-press-escape="false">
    <el-form v-loading="dataListLoading"  :model="dataForm" :rules="dataRule" ref="dataForm" @keyup.enter.native="dataFormSubmitHandle()" label-width="120px">
      <el-form-item prop="name" :label="$t('job.jobName')">
        <el-input v-model="dataForm.name" :placeholder="$t('job.jobName')"></el-input>
      </el-form-item>
      <el-form-item prop="instanceId" :label="$t('job.instanceName')" class="select-list">
        <el-select v-model="dataForm.instanceId"  :placeholder="$t('job.instanceName')">
          <el-option v-for="instance in instanceList" :key="instance.value" :label="instance.label" :value="instance.value"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item prop="type" :label="$t('job.jobType')" class="select-list">
        <el-select v-model="dataForm.type" :placeholder="$t('job.jobType')">
          <el-option v-for="type in jobTypeList" :key="type.value" :label="type.label" :value="type.value"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item prop="schedule" :label="$t('job.jobSchedule')" class="select-list">
        <el-select v-model="jobSchedule" @change="scheduleChanged" multiple :placeholder="$t('job.jobSchedule')">
          <el-option v-for="schedule in jobScheduleList" :key="schedule.value" :label="schedule.label" :value="schedule.value"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item prop="startTime" :label="$t('job.jobStartTime')">
        <el-date-picker v-model="dataForm.startTime" type="datetime" placeholder="设置任务启动时间" value-format="yyyy-MM-dd HH:mm:ss" style="width:100%"></el-date-picker>
      </el-form-item>
      <el-form-item prop="remark" :label="$t('remark')">
        <el-input v-model="dataForm.remark" :placeholder="$t('remark')"></el-input>
      </el-form-item>
    </el-form>
    <template slot="footer">
      <el-button @click="visible = false">{{ $t('cancel') }}</el-button>
      <el-button type="primary" @click="dataFormSubmitHandle()">{{ $t('confirm') }}</el-button>
    </template>
  </el-dialog>
</template>

<script>
import debounce from 'lodash/debounce'
import mixinViewModule from "@/mixins/view-module";
export default {
  mixins: [mixinViewModule],
  data () {
    return {
      visible: false,
      jobTypeList: [{label:'逻辑备份', value:'logical'}, {label:'物理备份', value:'physical'}],
      jobScheduleList: [{label:'周日', value:'SUN'}, {label:'周一',value:'MON'}, {label: '周二',value:'TUE'},
        {label:'周三',value:'WED'}, {label:'周四',value:'THU'}, {label:'周五', value:'FRI'}, {label:'周六',value:'SAT'}],
      instanceList: [],
      jobSchedule: [],
      dataForm: {
        id: '',
        instanceId: '',
        name: '',
        type: '',
        option: 'full',
        schedule: '',
        startTime: '',
        remark: '',
      }
    }
  },
  mounted() {
    this.$refs['dataForm'].resetFields()
    this.dataForm.startTime = this.defaultStartTime()
    this.dataForm.instanceId = ''
    this.jobSchedule = ''
  },
  activated() {
    this.$refs['dataForm'].resetFields()
    this.dataForm.startTime = this.defaultStartTime()
    this.dataForm.instanceId = ''
    this.jobSchedule = ''
  },
  computed: {
    dataRule() {
      const validateJobStartTime = (rule, value, callback) => {
        if (!value) {
          callback(new Error('任务启动时间不能为空'))
        } else {
          let curr = new Date()
          let setValue = new Date(value)
          if (curr > setValue) {
            callback(new Error('任务启动时间不能早于当前时间'))
          } else {
            callback()
          }
        }
      };
      const isSelect = (rule, value, callback) => {
        if (!value) {
          callback(new Error('请选择'))
        } else {
          if (value === '') {
            callback(new Error('请选择'))
          } else {
            callback()
          }
        }
      };
      return {
        instanceId: [
          {required: true, message: this.$t('validate.required'), trigger: 'blur', validator: isSelect}
        ],
        type: [
          {required: true, message: this.$t('validate.required'), trigger: 'blur', validator: isSelect}
        ],
        name: [
          {required: true, message: this.$t('validate.required'), trigger: 'blur'}
        ],
        startTime: [
          {required: true, validator: validateJobStartTime, trigger: 'blur'}
        ]
      }
    },
  },
  methods: {
    defaultStartTime() {
      let curr = new Date();
      let s1 = curr.getFullYear() + "-" + (curr.getMonth() + 1) + "-" + curr.getDate();
      let s2 = curr.getHours() + ":" + (curr.getMinutes() + 5) + ":" + curr.getSeconds();
      return s1 + " " + s2;
    },
    init () {
      this.visible = true
      this.instanceList = []
      this.$nextTick(() => {
        this.$refs['dataForm'].resetFields()
        this.dataForm.startTime = this.defaultStartTime();
        this.dataForm.instanceId = ''
        this.jobSchedule = ''
        this.getInfo()
      })
    },
    // 获取当前实例信息，在创建备份计划时选择
    getInfo () {
      this.dataListLoading = true;
      this.$http.get(`/sys/instance/list`).then(({ data: res }) => {
        this.dataListLoading = false;
        if (res.code !== 0) {
          return this.$message.error(res.msg)
        }
        for (let i = 0; i < res.page.list.length; i++) {
          this.instanceList.push({label: res.page.list[i].instanceName, value: res.page.list[i].id})
        }
        this.dataForm = {
          ...this.dataForm,
          ...res.data
        }
      }).catch(() => {this.dataListLoading = false;})
    },
    scheduleChanged() {
      this.dataForm.schedule = this.jobSchedule.toString()
    },
    // 表单提交
    dataFormSubmitHandle: debounce(function () {
      this.$refs['dataForm'].validate((valid) => {
        if (!valid) {
          return false
        }
        this.dataListLoading = true;
        this.$http[!this.dataForm.id ? 'post' : 'put'](!this.dataForm.id ? '/sys/job/new' : '/sys/job/update', this.dataForm).then(({ data: res }) => {
          this.dataListLoading = false;
          if (res.code !== 0) {
            return this.$message.error(res.msg)
          }
          this.$message({
            message: this.$t('prompt.success'),
            type: 'success',
            duration: 3000,
            onClose: () => {
              this.visible = false
              this.$emit('refreshDataList')
            }
          })
        }).catch(() => {this.dataListLoading = false;})
      })
    }, 1000, { 'leading': true, 'trailing': false })
  },
}
</script>

<style lang="scss">
.mod-sys__job {
  .select-list {
    .el-select {
      width: 100%;
    }
  }
}
</style>