<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="业务表单名称" prop="businessName">
        <el-input
          v-model="queryParams.businessName"
          placeholder="请输入业务表单名称"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="业务服务名称" prop="businessService">
        <el-input
          v-model="queryParams.businessService"
          placeholder="请输入业务服务名称"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="流程名称" prop="flowName">
        <el-input
          v-model="queryParams.flowName"
          placeholder="请输入流程名称"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="关联流程发布主键" prop="deployId">
        <el-input
          v-model="queryParams.deployId"
          placeholder="请输入关联流程发布主键"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="前端路由地址" prop="routeName">
        <el-input
          v-model="queryParams.routeName"
          placeholder="请输入前端路由地址"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="组件注入方法" prop="component">
        <el-input
          v-model="queryParams.component"
          placeholder="请输入组件注入方法"
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
          v-hasPermi="['workflow:customForm:add']"
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
          v-hasPermi="['workflow:customForm:edit']"
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
          v-hasPermi="['workflow:customForm:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['workflow:customForm:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="customFormList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="主键" align="center" prop="id" v-if="true"/>
      <el-table-column label="业务表单名称" align="center" prop="businessName" />
      <el-table-column label="业务服务名称" align="center" prop="businessService" />
      <el-table-column label="流程名称" align="center" prop="flowName" />
      <el-table-column label="关联流程发布主键" align="center" prop="deployId" />
      <el-table-column label="前端路由地址" align="center" prop="routeName" />
      <el-table-column label="组件注入方法" align="center" prop="component" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="relationProcess(scope.row)"
            v-hasPermi="['workflow:customForm:edit']"
          >关联流程</el-button>
          <el-button
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['workflow:customForm:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['workflow:customForm:remove']"
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

    <!-- 添加或修改流程业务单对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="680px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="160px">
        <el-form-item label="业务表单名称" prop="businessName">
          <el-input v-model="form.businessName" placeholder="请输入业务表单名称" />
        </el-form-item>
        <el-form-item label="业务服务名称" prop="businessService">
          <el-input v-model="form.businessService" placeholder="请输入业务服务名称" />
        </el-form-item>
        <el-form-item label="流程名称" prop="flowName">
          <el-input v-model="form.flowName" placeholder="请输入流程名称" />
        </el-form-item>
        <el-form-item label="关联流程发布主键" prop="deployId">
          <el-input v-model="form.deployId" placeholder="请输入关联流程发布主键" />
        </el-form-item>
        <el-form-item label="前端路由地址" prop="routeName">
          <el-input v-model="form.routeName" type="textarea" placeholder="请输入内容" />
        </el-form-item>
        <el-form-item label="组件注入方法" prop="component">
          <el-input v-model="form.component" type="textarea" placeholder="请输入内容" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button :loading="buttonLoading" type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
    <!--挂载布置流程-->
    <a-modal @cancel="flowOpen = false" :title="flowTitle" :visible.sync="flowOpen" width="70%" append-to-body>
      <el-row :gutter="64">
        <el-form :model="queryFlowParams" ref="queryFlowForm" :inline="true" label-width="100px">
          <el-form-item label="流程名称" prop="processName">
            <el-input v-model="queryFlowParams.processName" placeholder="请输入名称" clearable size="small"
              @keyup.enter.native="handleFlowQuery" />
          </el-form-item>
          <el-form-item label="流程应用类型" prop="appType">
            <el-select @change="handleFlowQuery" v-model="queryFlowParams.appType" placeholder="请选择应用类型" clearable
              prop="appType">
              <el-option
                v-for="dict in dict.type.wf_app_type"
                :key="dict.value"
                :label="dict.label"
                :value="dict.value"
              ></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="激活" prop="active">
            <el-switch v-model="queryFlowParams.active" active-color="#13ce66" inactive-color="#ff4949" @change="handleQuery">
            </el-switch>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="el-icon-search" size="mini" @click="handleFlowQuery">搜索</el-button>
            <el-button icon="el-icon-refresh" size="mini" @click="resetFlowQuery">重置</el-button>
          </el-form-item>
        </el-form>
      </el-row>
      <el-row :gutter="64">
        <el-col :span="20" :xs="64" style="width: 100%">
          <el-table ref="singleTable" :data="deployList" border highlight-current-row
            @current-change="handleCurrentChange" style="width: 100%">
             <el-table-column type="selection" width="55" align="center" />
             <el-table-column label="流程标识" align="center" prop="processKey" :show-overflow-tooltip="true" />
             <el-table-column label="流程名称" align="center" :show-overflow-tooltip="true">
               <template slot-scope="scope">
                 <el-button type="text" @click="handleProcessView(scope.row)">
                   <span>{{ scope.row.processName }}</span>
                 </el-button>
               </template>
             </el-table-column>
             <el-table-column label="流程分类" align="center" prop="categoryName" :formatter="categoryFormat" />
             <el-table-column label="应用类型" align="center" prop="appType" width="100">
               <template slot-scope="scope">
                 <dict-tag :options="dict.type.wf_app_type" :value="scope.row.appType"/>
               </template>
             </el-table-column>
             <el-table-column label="流程版本" align="center">
               <template slot-scope="scope">
                 <el-tag size="medium" >v{{ scope.row.version }}</el-tag>
               </template>
             </el-table-column>
             <el-table-column label="状态" align="center">
               <template slot-scope="scope">
                 <el-tag type="success" v-if="!scope.row.suspended">激活</el-tag>
                 <el-tag type="warning" v-if="scope.row.suspended">挂起</el-tag>
               </template>
             </el-table-column>
             <el-table-column label="部署时间" align="center" prop="deploymentTime" width="180"/>
             <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
               <template slot-scope="scope">
                 <el-button size="mini" type="text" @click="submitCustom(scope.row)">确定</el-button>
               </template>
             </el-table-column>
            </el-table>
          <el-pagination v-show="deployTotal>0" :total="deployTotal" :current-page.sync="queryParams.pageNum"
            :page-size.sync="queryParams.pageSize" @size-change="getDeployList" @current-change="getDeployList" />

        </el-col>
      </el-row>
    </a-modal>

    <!-- 流程图 -->
    <el-dialog :title="processView.title" :visible.sync="processView.open" width="70%" append-to-body>
      <process-viewer :key="`designer-${processView.index}`" :xml="processView.xmlData" :style="{height: '400px'}" />
    </el-dialog>


  </div>
</template>

<script>
import { listCustomForm, getCustomForm, delCustomForm, addCustomForm, updateCustomForm, updateCustom } from "@/api/workflow/customForm";
import { listAllCategory } from '@/api/workflow/category'
import { listDeploy, listPublish, getBpmnXml, changeState, delDeploy } from '@/api/workflow/deploy'
import ProcessViewer from '@/components/ProcessViewer'

export default {
  name: "CustomForm",
  dicts: ['wf_app_type'],
  components: {
    ProcessViewer
  },
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
      // 流程业务单表格数据
      customFormList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        businessName: undefined,
        businessService: undefined,
        flowName: undefined,
        deployId: undefined,
        routeName: undefined,
        component: undefined,
      },
      //查询布置流程相关参数
      flowOpen: false,
      deployTotal: 0,
      flowTitle: '',
      deployList: [],
      categoryOptions: [],
      // 查询参数
      queryFlowParams: {
        pageNum: 1,
        pageSize: 10,
        flowName: undefined,
        appType: 'ZDYYW',
      },
      // 挂载自定义表单到流程实例
      customParam: {
        id: null,
        deployId: null
      },
      processView: {
        title: '',
        open: false,
        index: undefined,
        xmlData:"",
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        id: [
          { required: true, message: "主键不能为空", trigger: "blur" }
        ],
        businessName: [
          { required: true, message: "业务表单名称不能为空", trigger: "blur" }
        ],
        businessService: [
          { required: true, message: "业务服务名称不能为空", trigger: "blur" }
        ],
        flowName: [
          { required: false, message: "流程名称不能为空", trigger: "blur" }
        ],
        deployId: [
          { required: false, message: "关联流程发布主键不能为空", trigger: "blur" }
        ],
        routeName: [
          { required: true, message: "前端路由地址不能为空", trigger: "blur" }
        ],
        component: [
          { required: true, message: "组件注入方法不能为空", trigger: "blur" }
        ],
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    relationProcess(row) {
      this.flowOpen = true;
      this.customParam.id = row.id;
      this.getCategoryList();
      this.getDeployList();
    },
    /** 选择流程更新自定义表单信息 */
    submitCustom(row) {
      if(row.appType != 'ZDYYW') {
        this.$message.warning("不是自定义业务应用类型不能绑定");
        return;
      }
      this.customParam.deployId = row.deploymentId;
     const params = {
       id: this.customParam.id,
       deployId: row.deploymentId,
       flowName: row.processName,
     }
      updateCustom(params).then(res => {
        this.$message.success(res.msg);
        this.flowOpen = false;
        this.getList();
      })
    },
    categoryFormat(row, column) {
      return this.categoryOptions.find(k => k.code === row.category)?.categoryName ?? '';
    },
    /** 查看流程图 */
    handleProcessView(row) {
      let definitionId = row.definitionId;
      this.processView.title = "流程图";
      this.processView.index = definitionId;
      // 发送请求，获取xml
      getBpmnXml(definitionId).then(response => {
        this.processView.xmlData = response.data;
      })
      this.processView.open = true;
    },
    /** 查询流程业务单列表 */
    getList() {
      this.loading = true;
      listCustomForm(this.queryParams).then(response => {
        this.customFormList = response.rows;
        this.total = response.total;
        this.loading = false;
      });
    },
    /** 查询流程分类列表 */
    getCategoryList() {
      listAllCategory().then(response => this.categoryOptions = response.data);
    },
    /** 查询流程部署列表 */
    getDeployList() {
      this.loading = true;
      listDeploy(this.queryFlowParams).then(response => {
        this.deployList = response.rows;
        this.deployTotal = response.total;
        this.loading = false;
      });
    },
    /** 搜索按钮操作 */
    handleFlowQuery() {
      this.queryFlowParams.pageNum = 1;
      this.getDeployList();
    },
    /** 重置按钮操作 */
    resetFlowQuery() {
      this.resetForm("queryFlowForm");
      this.handleQuery();
    },
    handleCurrentChange(data) {
      /*console.log("handleCurrentChange data",data);
      if (data) {
        this.currentRow = JSON.parse(data.formContent);
      }*/
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
        businessName: undefined,
        businessService: undefined,
        flowName: undefined,
        deployId: undefined,
        routeName: undefined,
        component: undefined,
        createBy: undefined,
        createTime: undefined,
        updateBy: undefined,
        updateTime: undefined
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
      this.title = "添加流程业务单";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.loading = true;
      this.reset();
      const id = row.id || this.ids
      getCustomForm(id).then(response => {
        this.loading = false;
        this.form = response.data;
        this.open = true;
        this.title = "修改流程业务单";
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          this.buttonLoading = true;
          if (this.form.id != null) {
            updateCustomForm(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.open = false;
              this.getList();
            }).finally(() => {
              this.buttonLoading = false;
            });
          } else {
            addCustomForm(this.form).then(response => {
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
      this.$modal.confirm('是否确认删除流程业务单编号为"' + ids + '"的数据项？').then(() => {
        this.loading = true;
        return delCustomForm(ids);
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
      this.download('workflow/customForm/export', {
        ...this.queryParams
      }, `customForm_${new Date().getTime()}.xlsx`)
    }
  }
};
</script>
