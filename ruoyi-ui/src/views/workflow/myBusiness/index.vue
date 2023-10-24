<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="申请人" prop="proposer">
        <el-input
          v-model="queryParams.proposer"
          placeholder="请输入申请人"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="处理过的人" prop="doneUsers">
        <el-input
          v-model="queryParams.doneUsers"
          placeholder="请输入处理过的人"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="流程实例主键" prop="deployId">
        <el-input
          v-model="queryParams.deployId"
          placeholder="请输入流程实例主键"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['workflow:myBusiness:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['workflow:myBusiness:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['workflow:myBusiness:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['workflow:myBusiness:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="myBusinessList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="主键ID" align="center" prop="id" v-if="true"/>
      <el-table-column label="流程定义key" align="center" prop="processDefinitionKey" />
      <el-table-column label="流程定义id " align="center" prop="processDefinitionId" />
      <el-table-column label="流程业务实例id" align="center" prop="processInstanceId" />
      <el-table-column label="流程业务简要描述" align="center" prop="title" />
      <el-table-column label="业务表id" align="center" prop="dataId" />
      <el-table-column label="业务类名" align="center" prop="serviceImplName" />
      <el-table-column label="申请人" align="center" prop="proposer" />
      <el-table-column label="流程状态说明" align="center" prop="actStatus" />
      <el-table-column label="当前的节点定义上的Id," align="center" prop="taskId" />
      <el-table-column label="当前的节点" align="center" prop="taskName" />
      <el-table-column label="当前的节点实例上的Id" align="center" prop="taskNameId" />
      <el-table-column label="当前的节点可以处理的用户名" align="center" prop="todoUsers" />
      <el-table-column label="处理过的人" align="center" prop="doneUsers" />
      <el-table-column label="当前任务节点的优先级 " align="center" prop="priority" />
      <el-table-column label="前端页面显示的路由地址" align="center" prop="routeName" />
      <el-table-column label="流程实例主键" align="center" prop="deployId" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['workflow:myBusiness:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['workflow:myBusiness:remove']"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 添加或修改流程业务扩展对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="流程定义key 一个key会有多个版本的id" prop="processDefinitionKey">
          <el-input v-model="form.processDefinitionKey" placeholder="请输入流程定义key 一个key会有多个版本的id" />
        </el-form-item>
        <el-form-item label="流程定义id 一个流程定义唯一" prop="processDefinitionId">
          <el-input v-model="form.processDefinitionId" placeholder="请输入流程定义id 一个流程定义唯一" />
        </el-form-item>
        <el-form-item label="流程业务实例id 一个流程业务唯一，本表中也唯一" prop="processInstanceId">
          <el-input v-model="form.processInstanceId" placeholder="请输入流程业务实例id 一个流程业务唯一，本表中也唯一" />
        </el-form-item>
        <el-form-item label="流程业务简要描述" prop="title">
          <el-input v-model="form.title" type="textarea" placeholder="请输入内容" />
        </el-form-item>
        <el-form-item label="业务表id，理论唯一" prop="dataId">
          <el-input v-model="form.dataId" placeholder="请输入业务表id，理论唯一" />
        </el-form-item>
        <el-form-item label="业务类名，用来获取spring容器里的服务对象" prop="serviceImplName">
          <el-input v-model="form.serviceImplName" placeholder="请输入业务类名，用来获取spring容器里的服务对象" />
        </el-form-item>
        <el-form-item label="申请人" prop="proposer">
          <el-input v-model="form.proposer" placeholder="请输入申请人" />
        </el-form-item>
        <el-form-item label="当前的节点定义上的Id," prop="taskId">
          <el-input v-model="form.taskId" type="textarea" placeholder="请输入内容" />
        </el-form-item>
        <el-form-item label="当前的节点" prop="taskName">
          <el-input v-model="form.taskName" type="textarea" placeholder="请输入内容" />
        </el-form-item>
        <el-form-item label="当前的节点实例上的Id" prop="taskNameId">
          <el-input v-model="form.taskNameId" type="textarea" placeholder="请输入内容" />
        </el-form-item>
        <el-form-item label="当前的节点可以处理的用户名" prop="todoUsers">
          <el-input v-model="form.todoUsers" type="textarea" placeholder="请输入内容" />
        </el-form-item>
        <el-form-item label="处理过的人" prop="doneUsers">
          <el-input v-model="form.doneUsers" type="textarea" placeholder="请输入内容" />
        </el-form-item>
        <el-form-item label="当前任务节点的优先级 流程定义的时候所填" prop="priority">
          <el-input v-model="form.priority" placeholder="请输入当前任务节点的优先级 流程定义的时候所填" />
        </el-form-item>
        <el-form-item label="前端页面显示的路由地址" prop="routeName">
          <el-input v-model="form.routeName" type="textarea" placeholder="请输入内容" />
        </el-form-item>
        <el-form-item label="流程实例主键" prop="deployId">
          <el-input v-model="form.deployId" placeholder="请输入流程实例主键" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button :loading="buttonLoading" type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listMyBusiness, getMyBusiness, delMyBusiness, addMyBusiness, updateMyBusiness } from "@/api/workflow/myBusiness";

export default {
  name: "MyBusiness",
  data() {
    return {
      // 按钮loading
      buttonLoading: false,
      // 遮罩层
      loading: true,
      // 选中数组
      ids: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 流程业务扩展表格数据
      myBusinessList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        processDefinitionKey: undefined,
        processDefinitionId: undefined,
        processInstanceId: undefined,
        title: undefined,
        dataId: undefined,
        serviceImplName: undefined,
        proposer: undefined,
        actStatus: undefined,
        taskId: undefined,
        taskName: undefined,
        taskNameId: undefined,
        todoUsers: undefined,
        doneUsers: undefined,
        priority: undefined,
        routeName: undefined,
        deployId: undefined
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        id: [
          { required: true, message: "主键ID不能为空", trigger: "blur" }
        ],
        processDefinitionKey: [
          { required: true, message: "流程定义key 一个key会有多个版本的id不能为空", trigger: "blur" }
        ],
        processDefinitionId: [
          { required: true, message: "流程定义id 一个流程定义唯一不能为空", trigger: "blur" }
        ],
        processInstanceId: [
          { required: true, message: "流程业务实例id 一个流程业务唯一，本表中也唯一不能为空", trigger: "blur" }
        ],
        title: [
          { required: true, message: "流程业务简要描述不能为空", trigger: "blur" }
        ],
        dataId: [
          { required: true, message: "业务表id，理论唯一不能为空", trigger: "blur" }
        ],
        serviceImplName: [
          { required: true, message: "业务类名，用来获取spring容器里的服务对象不能为空", trigger: "blur" }
        ],
        proposer: [
          { required: true, message: "申请人不能为空", trigger: "blur" }
        ],
        actStatus: [
          { required: true, message: "流程状态说明，有：启动  撤回  驳回  审批中  审批通过  审批异常不能为空", trigger: "change" }
        ],
        taskId: [
          { required: true, message: "当前的节点定义上的Id,不能为空", trigger: "blur" }
        ],
        taskName: [
          { required: true, message: "当前的节点不能为空", trigger: "blur" }
        ],
        taskNameId: [
          { required: true, message: "当前的节点实例上的Id不能为空", trigger: "blur" }
        ],
        todoUsers: [
          { required: true, message: "当前的节点可以处理的用户名不能为空", trigger: "blur" }
        ],
        doneUsers: [
          { required: true, message: "处理过的人不能为空", trigger: "blur" }
        ],
        priority: [
          { required: true, message: "当前任务节点的优先级 流程定义的时候所填不能为空", trigger: "blur" }
        ],
        routeName: [
          { required: true, message: "前端页面显示的路由地址不能为空", trigger: "blur" }
        ],
        deployId: [
          { required: true, message: "流程实例主键不能为空", trigger: "blur" }
        ]
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询流程业务扩展列表 */
    getList() {
      this.loading = true;
      listMyBusiness(this.queryParams).then(response => {
        this.myBusinessList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    // 取消按钮
    cancel() {
      this.open = false;
      this.reset();
    },
    // 表单重置
    reset() {
      this.form = {
        id: undefined,
        createBy: undefined,
        createTime: undefined,
        updateBy: undefined,
        updateTime: undefined,
        processDefinitionKey: undefined,
        processDefinitionId: undefined,
        processInstanceId: undefined,
        title: undefined,
        dataId: undefined,
        serviceImplName: undefined,
        proposer: undefined,
        actStatus: undefined,
        taskId: undefined,
        taskName: undefined,
        taskNameId: undefined,
        todoUsers: undefined,
        doneUsers: undefined,
        priority: undefined,
        routeName: undefined,
        deployId: undefined
      };
      this.resetForm("form");
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id)
      this.single = selection.length!==1
      this.multiple = !selection.length
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "添加流程业务扩展";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.loading = true;
      this.reset();
      const id = row.id || this.ids
      getMyBusiness(id).then(response => {
        this.loading = false;
        this.form = response.data;
        this.open = true;
        this.title = "修改流程业务扩展";
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          this.buttonLoading = true;
          if (this.form.id != null) {
            updateMyBusiness(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            }).finally(() => {
              this.buttonLoading = false;
            });
          } else {
            addMyBusiness(this.form).then(response => {
              this.$modal.msgSuccess("新增成功");
              this.open = false;
              this.getList();
            }).finally(() => {
              this.buttonLoading = false;
            });
          }
        }
      });
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const ids = row.id || this.ids;
      this.$modal.confirm('是否确认删除流程业务扩展编号为"' + ids + '"的数据项？').then(() => {
        this.loading = true;
        return delMyBusiness(ids);
      }).then(() => {
        this.loading = false;
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {
      }).finally(() => {
        this.loading = false;
      });
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('workflow/myBusiness/export', {
        ...this.queryParams
      }, `myBusiness_${new Date().getTime()}.xlsx`)
    }
  }
};
</script>
