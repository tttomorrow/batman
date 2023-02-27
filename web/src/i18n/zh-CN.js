const t = {}

t.loading = '加载中...'

t.brand = {}
t.brand.lg = 'openGauss备份恢复管理系统'
t.brand.mini = 'openGauss'
t.brand.batman = '备份恢复管理系统'

t.add = '新增'
t.delete = '删除'
t.deleteBatch = '批量删除'
t.update = '修改'
t.restore = '恢复'
t.query = '查询'
t.export = '导出'
t.handle = '操作'
t.confirm = '确定'
t.cancel = '取消'
t.clear = '清除'
t.logout = '退出'
t.manage = '处理'
t.createDate = '创建时间'
t.keyword = '关键字：'
t.choose = '请选择'
t.remark = '备注信息'

t.prompt = {}
t.prompt.title = '提示'
t.prompt.info = '确定进行[{handle}]操作?'
t.prompt.success = '操作成功'
t.prompt.failed = '操作失败'
t.prompt.deleteBatch = '请选择删除项'
t.prompt.restoreMoreOne = '不能选择多项进行恢复'
t.prompt.warn = '警告'
t.prompt.restore_info = '确定进行恢复操作???\n' +
    '恢复操作将会使实例恢复到以前的状态，且在恢复过程中实例不可用!!!'

t.validate = {}
t.validate.required = '必填项不能为空'
t.validate.format = '{attr}格式错误'

t.datePicker = {}
t.datePicker.range = '至'
t.datePicker.start = '开始日期'
t.datePicker.end = '结束日期'

t.fullscreen = {}
t.fullscreen.prompt = '您的浏览器不支持此操作'

t.updatePassword = {}
t.updatePassword.title = '修改密码'
t.updatePassword.username = '账号'
t.updatePassword.password = '原密码'
t.updatePassword.newPassword = '新密码'
t.updatePassword.confirmPassword = '确认密码'
t.updatePassword.validate = {}
t.updatePassword.validate.confirmPassword = '确认密码与新密码输入不一致'

t.contentTabs = {}
t.contentTabs.closeCurrent = '关闭当前标签页'
t.contentTabs.closeOther = '关闭其它标签页'
t.contentTabs.closeAll = '关闭全部标签页'

/* 页面 */
t.notFound = {}
t.notFound.desc = '抱歉！您访问的页面<em>失联</em>啦...'
t.notFound.back = '上一页'
t.notFound.sys_task = '任务管理'

t.login = {}
t.login.title = '登录'
t.login.username = '用户名'
t.login.password = '密码'
t.login.captcha = '验证码'
t.login.copyright = 'openGauss'

t.menu = {}
t.menu.name = '名称'
t.menu.icon = '图标'
t.menu.type = '类型'
t.menu.type0 = '菜单'
t.menu.type1 = '按钮'
t.menu.sort = '排序'
t.menu.url = '路由'
t.menu.permissions = '授权标识'
t.menu.parentName = '上级菜单'
t.menu.parentNameDefault = '一级菜单'
t.menu.resource = '授权资源'
t.menu.resourceUrl = '资源URL'
t.menu.resourceMethod = '请求方式'
t.menu.resourceAddItem = '添加一项'

t.params = {}
t.params.paramCode = '编码'
t.params.paramValue = '值'
t.params.remark = '备注'

t.user = {}
t.user.username = '用户名'
t.user.deptName = '所属部门'
t.user.email = '邮箱'
t.user.mobile = '手机号'
t.user.status = '状态'
t.user.status0 = '停用'
t.user.status1 = '正常'
t.user.createDate = '创建时间'
t.user.password = '密码'
t.user.confirmPassword = '确认密码'
t.user.realName = '真实姓名'
t.user.gender = '性别'
t.user.gender0 = '男'
t.user.gender1 = '女'
t.user.gender2 = '保密'
t.user.roleIdList = '角色配置'
t.user.validate = {}
t.user.validate.confirmPassword = '确认密码与密码输入不一致'
t.user.select = '选择用户'
t.user.selecterror = '请选择一条记录'


t.task = {}
t.task.taskName = '任务名'
t.task.startTime = '开始时间'
t.task.endTime = '结束时间'
t.task.backupType = '备份类型'
t.task.backupMethod = '备份方式'
t.task.progress = '进度'
t.task.status = '状态'
t.task.status0 = '运行中'
t.task.status1 = '正常'
t.task.size = '大小'
t.task.desc = '描述'
t.task.addTask = '新建备份任务'
t.task.success = '成功'
t.task.failed = '失败'
t.task.running = '运行中'

t.instance = {}
t.instance.instanceName = '实例名称'
t.instance.instanceNameTip = '请输入实例名称'
t.instance.instanceIp = '实例IP'
t.instance.instanceIpTip = '请输入实例主节点ssh IP'
t.instance.instancePort = '实例端口'
t.instance.instancePortTip = '请输入实例主节点ssh端口'
t.instance.osUser = 'OS用户'
t.instance.osUserTip = '请输入实例节点操作系统用户名'
t.instance.osPassword = 'OS密码'
t.instance.osPasswordTip = '请输入实例节点操作系统用户密码'
t.instance.backupPath = '备份路径'
t.instance.backupPathTip = '请输入实例备份路径'
t.instance.remark = '备注'
t.instance.remarkTip = '请输入实例备注'
t.instance.add = '新增实例'
t.instance.addInstance = '新增实例'
t.instance.updateInstance = '更新实例信息'
t.instance.instanceVersion = '实例版本'
t.instance.backupPath = '备份路径'
t.instance.createTime = '导入时间'
t.instance.ip = 'IP地址'

t.job = {}
t.job.addFull = '创建全量备份'
t.job.addIncrement = "创建增量备份"
t.job.update = '更新备份计划'
t.job.jobName = '计划名称'
t.job.jobType = '备份类型'
t.job.jobMethod = '备份方式'
t.job.jobSchedule = '备份周期'
t.job.jobStartTime = '启动时间'
t.job.createTime = '创建时间'
t.job.instanceName = '备份实例'
t.job.remark = '描述'

t.log = {}
t.log.userName = '操作用户'
t.log.instanceName = '操作实例'
t.log.logIp = '登录IP'
t.log.operation = '实例操作'
t.log.operationTime = '操作时间'

export default t
