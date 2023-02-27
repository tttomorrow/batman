<template>
  <el-dialog :visible.sync="visible" :title="!dataForm.id ? $t('instance.addInstance') : $t('instance.updateInstance')" :close-on-click-modal="false" :close-on-press-escape="false">
    <el-form v-loading="dataListLoading" :model="dataForm" :rules="dataRule" ref="dataForm" @keyup.enter.native="dataFormSubmitHandle()" label-width="120px">
      <el-form-item prop="instanceName" :label="$t('instance.instanceName')">
        <el-input v-model="dataForm.instanceName" :placeholder="$t('instance.instanceNameTip')"></el-input>
      </el-form-item>
      <el-form-item prop="instanceIp" :label="$t('instance.instanceIp')">
        <el-input v-model="dataForm.instanceIp" :placeholder="$t('instance.instanceIpTip')"></el-input>
      </el-form-item>
      <el-form-item prop="instancePort" :label="$t('instance.instancePort')">
        <el-input v-model="dataForm.instancePort" :placeholder="$t('instance.instancePortTip')"></el-input>
      </el-form-item>
      <el-form-item prop="osUser" :label="$t('instance.osUser')">
        <el-input v-model="dataForm.osUser" :placeholder="$t('instance.osUserTip')"></el-input>
      </el-form-item>
      <el-form-item prop="osPassword" :label="$t('instance.osPassword')">
        <el-input v-model="dataForm.osPassword" type="password" :placeholder="$t('instance.osPasswordTip')"></el-input>
      </el-form-item>
      <el-form-item prop="backupPath" :label="$t('instance.backupPath')">
        <el-input v-model="dataForm.backupPath" :placeholder="$t('instance.osPasswordTip')"></el-input>
      </el-form-item>
      <el-form-item prop="remark" :label="$t('instance.remark')">
        <el-input v-model="dataForm.remark" :placeholder="$t('instance.remarkTip')"></el-input>
      </el-form-item>
    </el-form>
    <template slot="footer">
      <el-button @click="dataListLoading=false; visible = false">{{ $t('cancel') }}</el-button>
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
      dataForm: {
        id: '',
        instanceName: '',
        instanceIp: '',
        instancePort: '',
        osUser: '',
        osPassword: '',
        backupPath: '',
        remark: '',
      }
    }
  },
  computed: {
    dataRule () {
      return {
        instanceName: [
          { required: true, message: this.$t('validate.required'), trigger: 'blur' }
        ],
        instanceIp: [
          { required: true, message: this.$t('validate.required'), trigger: 'blur' }
        ],
        instancePort: [
          { required: true, message: this.$t('validate.required'), trigger: 'blur' }
        ],
        osUser: [
          { required: true, message: this.$t('validate.required'), trigger: 'blur' }
        ],
        osPassword: [
          { required: true, message: this.$t('validate.required'), trigger: 'blur' }
        ],
        backupPath: [
          { required: true, message: this.$t('validate.required'), trigger: 'blur' }
        ]
      }
    }
  },
  methods: {
    init () {
      this.visible = true
      this.$nextTick(() => {
        this.$refs['dataForm'].resetFields()
        if (this.dataForm.id) {
          this.getInfo()
        }
      })
    },
    // 获取信息
    getInfo () {
      this.dataListLoading = true;
      this.$http.get(`/sys/instance/info/${this.dataForm.id}`).then(({ data: res }) => {
        this.dataListLoading = false;
        if (res.code !== 0) {
          return this.$message.error(res.msg)
        }
        this.dataForm = {
          ...this.dataForm,
          ...res.data
        }
      }).catch(() => {this.dataListLoading = false;})
    },
    // 表单提交
    dataFormSubmitHandle: debounce(function () {
      this.$refs['dataForm'].validate((valid) => {
        if (!valid) {
          return false
        }
        this.dataListLoading = true;
        this.$http[!this.dataForm.id ? 'post' : 'put'](!this.dataForm.id ? '/sys/instance/new' : '/sys/instance/update', this.dataForm).then(({ data: res }) => {
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
  }
}
</script>
