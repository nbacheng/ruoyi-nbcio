<template>
  <div class="app-container">
    <el-tabs tab-position="top" v-model="activeName" :value="processed === true ? 'approval' : 'form'" @tab-click="changeTab">

      <el-tab-pane label="任务办理" name="approval" v-if="processed === true">
        <el-card class="box-card" shadow="hover" v-if="taskFormOpen">
          <div slot="header" class="clearfix">
            <span>填写表单</span>
          </div>
          <el-col :span="20" :offset="2">
            <parser :form-conf="taskFormData" ref="taskFormParser"/>
          </el-col>
        </el-card>
        <el-card class="box-card" shadow="hover">
          <div slot="header" class="clearfix">
            <span>审批流程</span>
          </div>
          <el-row>
            <el-col :span="20" :offset="2">
              <el-form ref="taskForm" :model="taskForm" :rules="rules" label-width="120px">
                <el-form-item label="审批意见" prop="comment">
                  <el-input type="textarea" :rows="5" v-model="taskForm.comment" placeholder="请输入 审批意见" />
                </el-form-item>
                <el-form-item label="抄送人" prop="copyUserIds">
                  <el-tag
                    :key="index"
                    v-for="(item, index) in copyUser"
                    closable
                    :disable-transitions="false"
                    @close="handleClose('copy', item)">
                    {{ item.nickName }}
                  </el-tag>
                  <el-button class="button-new-tag" type="primary" icon="el-icon-plus" size="mini" circle @click="onSelectCopyUsers" />
                </el-form-item>
                <el-form-item label="指定审批人" prop="copyUserIds">
                  <el-tag
                    :key="index"
                    v-for="(item, index) in nextUser"
                    closable
                    :disable-transitions="false"
                    @close="handleClose('next', item)">
                    {{ item.nickName }}
                  </el-tag>
                  <el-button class="button-new-tag" type="primary" icon="el-icon-plus" size="mini" circle @click="onSelectNextUsers" />
                </el-form-item>
              </el-form>
            </el-col>
          </el-row>
          <el-row :gutter="10" type="flex" justify="center" >
            <el-col :span="1.5">
              <el-button icon="el-icon-circle-check" type="success" @click="handleComplete">通过</el-button>
            </el-col>
            <el-col :span="1.5">
              <el-button icon="el-icon-chat-line-square" type="primary" @click="handleDelegate">委派</el-button>
            </el-col>
            <el-col :span="1.5">
              <el-button icon="el-icon-thumb" type="success" @click="handleTransfer">转办</el-button>
            </el-col>
            <el-col :span="1.5">
              <el-button icon="el-icon-refresh-left" type="warning" @click="handleReturn">退回</el-button>
            </el-col>
            <el-col :span="1.5">
              <el-button icon="el-icon-circle-close" type="danger" @click="handleReject">拒绝</el-button>
            </el-col>
          </el-row>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="表单信息" name="form">
        <div v-if="customForm.visible"> <!-- 自定义表单 -->
            <!--<component ref="refCustomForm" :disabled="customForm.disabled" v-bind:is="customForm.formComponent" :model="customForm.model"
                        :customFormData="customForm.customFormData" :isNew = "customForm.isNew"></component> -->
            <component ref="refCustomForm" :disabled="customForm.disabled" v-bind:is="customForm.formComponent" :model="customForm.model"
                        :customFormData="customForm.customFormData" :isNew = "customForm.isNew"></component>
        </div>
        <div v-if="formOpen"> <!-- formdesigner表单 -->
          <el-card class="box-card" shadow="never" v-for="(formInfo, index) in formViewData" :key="index">
            <!--<div slot="header" class="clearfix">
              <span>{{ formInfo.title }}</span>
            </div>-->
            <!--流程处理表单模块-->
            <el-col :span="20" :offset="2">
              <form-viewer ref="formViewer" v-model="formVal[index]" :buildData="formInfo" />
            </el-col>
          </el-card>
        </div>
        <div style="margin-left:10%;margin-bottom: 30px">
           <!--对上传文件进行显示处理，临时方案 add by nbacheng 2022-07-27 -->
           <el-upload action="#" :on-preview="handleFilePreview" :file-list="fileList" v-if="fileDisplay" />
        </div>
      </el-tab-pane >

      <el-tab-pane label="流转记录" name="record">
        <el-card class="box-card" shadow="never">
          <el-col :span="20" :offset="2">
            <div class="block">
              <el-timeline>
                <el-timeline-item v-for="(item,index) in historyProcNodeList" :key="index" :icon="setIcon(item.endTime)" :color="setColor(item.endTime)">
                  <p style="font-weight: 700">{{ item.activityName }}</p>
                  <el-card v-if="item.activityType === 'startEvent'" class="box-card" shadow="hover">
                    {{ item.assigneeName }} 在 {{ item.createTime }} 发起流程
                  </el-card>
                  <el-card v-if="item.activityType === 'userTask'" class="box-card" shadow="hover">
                    <el-descriptions :column="5" :labelStyle="{'font-weight': 'bold'}">
                      <el-descriptions-item label="实际办理">{{ item.assigneeName || '-'}}</el-descriptions-item>
                      <el-descriptions-item label="候选办理">{{ item.candidate || '-'}}</el-descriptions-item>
                      <el-descriptions-item label="接收时间">{{ item.createTime || '-'}}</el-descriptions-item>
                      <el-descriptions-item label="办结时间">{{ item.endTime || '-' }}</el-descriptions-item>
                      <el-descriptions-item label="耗时">{{ item.duration || '-'}}</el-descriptions-item>
                    </el-descriptions>
                    <div v-if="item.commentList && item.commentList.length > 0">
                      <div v-for="(comment, index) in item.commentList" :key="index">
                        <el-divider content-position="left">
                          <el-tag :type="approveTypeTag(comment.type)" size="mini">{{ commentType(comment.type) }}</el-tag>
                          <el-tag type="info" effect="plain" size="mini">{{ comment.time }}</el-tag>
                        </el-divider>
                        <span>{{ comment.fullMessage }}</span>
                      </div>
                    </div>
                  </el-card>
                  <el-card v-if="item.activityType === 'endEvent'" class="box-card" shadow="hover">
                    {{ item.createTime }} 结束流程
                  </el-card>
                </el-timeline-item>
              </el-timeline>
            </div>
          </el-col>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="流程跟踪" name="track">
        <el-card class="box-card" shadow="never">
          <process-viewer :key="`designer-${loadIndex}`" :style="'height:' + height" :xml="xmlData"
                          :finishedInfo="finishedInfo" :allCommentList="historyProcNodeList"
          />
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!--退回流程-->
    <el-dialog :title="returnTitle" :visible.sync="returnOpen" width="40%" append-to-body>
      <el-form ref="taskForm" :model="taskForm" label-width="80px" >
        <el-form-item label="退回节点" prop="targetKey">
          <el-radio-group v-model="taskForm.targetKey">
            <el-radio-button
              v-for="item in returnTaskList"
              :key="item.id"
              :label="item.id"
            >{{item.name}}</el-radio-button>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="returnOpen = false">取 消</el-button>
        <el-button type="primary" @click="submitReturn">确 定</el-button>
      </span>
    </el-dialog>

    <el-dialog :title="userData.title" :visible.sync="userData.open" width="60%" append-to-body>
      <el-row type="flex" :gutter="20">
        <!--部门数据-->
        <el-col :span="5">
          <el-card shadow="never" style="height: 100%">
            <div slot="header">
              <span>部门列表</span>
            </div>
            <div class="head-container">
              <el-input v-model="deptName" placeholder="请输入部门名称" clearable size="small" prefix-icon="el-icon-search"/>
              <el-tree
                :data="deptOptions"
                :props="deptProps"
                :expand-on-click-node="false"
                :filter-node-method="filterNode"
                ref="tree"
                default-expand-all
                @node-click="handleNodeClick"
              />
            </div>
          </el-card>
        </el-col>
        <el-col :span="18">
          <el-table ref="userTable"
                    :key="userData.type"
                    height="500"
                    v-loading="userLoading"
                    :data="userList"
                    highlight-current-row
                    @current-change="changeCurrentUser"
                    @selection-change="handleSelectionChange">
            <el-table-column v-if="userData.type === 'copy' || userData.type === 'next'" width="55" type="selection" />
            <el-table-column v-else width="30">
              <template slot-scope="scope">
                <el-radio :label="scope.row.userId" v-model="currentUserId">{{''}}</el-radio>
              </template>
            </el-table-column>
            <el-table-column label="用户名" align="center" prop="nickName" />
            <el-table-column label="手机" align="center" prop="phonenumber" />
            <el-table-column label="部门" align="center" prop="dept.deptName" />
          </el-table>
          <pagination
            :total="total"
            :page.sync="queryParams.pageNum"
            :limit.sync="queryParams.pageSize"
            @pagination="getList"
          />
        </el-col>
      </el-row>
      <span slot="footer" class="dialog-footer">
        <el-button @click="userData.open = false">取 消</el-button>
        <el-button type="primary" @click="submitUserData">确 定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import Vue from 'vue'
import { detailProcess, processIscompleted } from '@/api/workflow/process'
import { getProcessVariables } from '@/api/workflow/task'
import Parser from '@/utils/generator/parser'
import { complete, delegate, transfer, rejectTask, returnList, returnTask } from '@/api/workflow/task'
import { selectUser, deptTreeSelect } from '@/api/system/user'
import ProcessViewer from '@/components/ProcessViewer'
import '@riophae/vue-treeselect/dist/vue-treeselect.css'
import Treeselect from '@riophae/vue-treeselect'
import formViewer from '@/components/formdesigner/components/formViewer'
import { getCustomForm} from '@/api/workflow/customForm'
import {
    flowableMixin
  } from '@/views/workflow/mixins/flowableMixin'

export default {
  name: "WorkDetail",
  mixins: [flowableMixin],
  components: {
    ProcessViewer,
    Parser,
    Treeselect,
    formViewer
  },
  props: {},
  computed: {
    commentType() {
      return val => {
        switch (val) {
          case '1': return '通过'
          case '2': return '退回'
          case '3': return '驳回'
          case '4': return '委派'
          case '5': return '转办'
          case '6': return '终止'
          case '7': return '撤回'
        }
      }
    },
    approveTypeTag() {
      return val => {
        switch (val) {
          case '1': return 'success'
          case '2': return 'warning'
          case '3': return 'danger'
          case '4': return 'primary'
          case '5': return 'success'
          case '6': return 'danger'
          case '7': return 'info'
        }
      }
    }
  },
  data() {
    return {
      height: document.documentElement.clientHeight - 205 + 'px;',
      // 模型xml数据
      loadIndex: 0,
      xmlData: undefined,
      finishedInfo: {
        finishedSequenceFlowSet: [],
        finishedTaskSet: [],
        unfinishedTaskSet: [],
        rejectedTaskSet: []
      },
      historyProcNodeList: [],
      // 部门名称
      deptName: undefined,
      // 部门树选项
      deptOptions: undefined,
      userLoading: false,
      // 用户表格数据
      userList: null,
      deptProps: {
        children: "children",
        label: "label"
      },
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        deptId: undefined
      },
      total: 0,
      // 遮罩层
      loading: true,
      taskForm:{
        comment:"", // 意见内容
        procInsId: "", // 流程实例编号
        taskId: "" ,// 流程任务编号
        copyUserIds: "", // 抄送人Id
        vars: "",
        targetKey:""
      },
      rules: {
        comment: [{ required: true, message: '请输入审批意见', trigger: 'blur' }],
      },
      currentUserId: null,
      variables: [], // 流程变量数据
      taskFormOpen: false,
      taskFormData: {}, // 流程变量数据
      processFormList: [], // 流程变量数据
      formOpen: false, // 是否加载流程变量数据
      customForm: { //自定义业务表单
        formId: '',
        title: '',
        disabled: false,
        visible: false,
        formComponent: null,
        model: {},
        /*流程数据*/
        customFormData: {},
        isNew: false,
        disableSubmit: true
      },
      variables: [], // 流程变量数据
      variablesData: {}, // 流程变量数据
      returnTaskList: [],  // 回退列表数据
      processed: false,
      returnTitle: null,
      returnOpen: false,
      rejectOpen: false,
      rejectTitle: null,
      userData: {
        title: '',
        type: '',
        open: false,
      },
      copyUser: [],
      nextUser: [],
      userMultipleSelection: [],
      userDialogTitle: '',
      userOpen: false,
      formVal:[], //formdesigner关联值
      formViewData: [], //显示formdesigner的输入后提交的表单数据
      fileDisplay: false, // formdesigner是否显示上传的文件控件
      fileList: [], //表单设计器上传的文件列表
      activeName:'', //获取当然tabname
    };
  },
  created() {
    this.initData();
  },
  mounted() {

  },
  methods: {
    initData() {
      this.taskForm.procInsId = this.$route.params && this.$route.params.procInsId;
      this.taskForm.taskId  = this.$route.query && this.$route.query.taskId;
      this.processed = this.$route.query && eval(this.$route.query.processed || false);

      //判断流程是否结束
      processIscompleted({procInsId: this.taskForm.procInsId}).then(res => {
        console.log("processIscompleted res=",res);
        if(res.data) {
         this.processed = false;
        }
        // 获取流程变量
        this.processVariables(this.taskForm.taskId);
        /*// 流程任务重获取变量表单
        this.getProcessDetails(this.taskForm.procInsId, this.taskForm.taskId);
        this.loadIndex = this.taskForm.procInsId;
        if(this.processed) {
          this.activeName = "approval";
        }
        else {
          this.activeName = "form";
          // 回填数据,这里主要是处理文件列表显示,临时解决，以后应该在formdesigner里完成
          this.processFormList.forEach((item, i) => {
            if (item.hasOwnProperty('list')) {
              this.fillFormData(item.list, item)
              // 更新表单
              this.key = +new Date().getTime()
            }
          });
        }*/
      });

    },
    /** 查询部门下拉树结构 */
    getTreeSelect() {
      deptTreeSelect().then(response => {
        this.deptOptions = response.data;
      });
    },
    /** 查询用户列表 */
    getList() {
      this.userLoading = true;
      selectUser(this.addDateRange(this.queryParams, this.dateRange)).then(response => {
        this.userList = response.rows;
        this.total = response.total;
        this.toggleSelection(this.userMultipleSelection);
        this.userLoading = false;
      });
    },
    // 筛选节点
    filterNode(value, data) {
      if (!value) return true;
      return data.label.indexOf(value) !== -1;
    },
    changeTab(tab, event) {
      console.log("changeTab tab=",tab);
      if(tab.name === 'form') {
        console.log("changeTab this.processFormList=",this.processFormList);
        if(this.customForm.formId === "") {
          // 回填数据,这里主要是处理文件列表显示,临时解决，以后应该在formdesigner里完成
          this.processFormList.forEach((item, i) => {
            if (item.hasOwnProperty('list')) {
              this.fillFormData(item.list, item)
              // 更新表单
              this.key = +new Date().getTime()
            }
          });
        }
        /*else {
           if(this.processFormList.length == 1 &&
              this.processFormList[0].formValues.hasOwnProperty('routeName')) {
              this.customForm.disabled = true;
              this.customForm.visible = true;
              this.customForm.formComponent = this.getFormComponent(this.processFormList[0].formValues.routeName).component;
              this.customForm.model = this.processFormList[0].formValues.formData;
              this.customForm.customFormData = this.processFormList[0].formValues.formData;
              console.log("detailProcess customForm",this.customForm);
           }
        }*/
      }
    },
    fillFormData(list, formConf) { // for formdesigner
      console.log("fillFormData list=",list);
      console.log("fillFormData formConf=",formConf);
      list.forEach((item, i) => {
        // 特殊处理el-upload，包括 回显图片
        if(formConf.formValues[item.id] != '') {
          const val = formConf.formValues[item.id];
          if (item.ele === 'el-upload') {
            console.log('fillFormData val=',val)
            if(item['list-type'] != 'text') {//图片
              this.fileList = []    //隐藏加的el-upload文件列表
              //item['file-list'] = JSON.parse(val)
              if(val != '') {
                item['file-list'] = JSON.parse(val)
              }
            }
            else {  //列表
              console.log("列表fillFormData val",val)
              this.fileList = JSON.parse(val)
              item['file-list'] = [] //隐藏加的表单设计器的文件列表
            }
            // 回显图片
            this.fileDisplay = true
          }
        }

        if (Array.isArray(item.columns)) {
          this.fillFormData(item.columns, formConf)
        }
      })
    },
    //点击文件列表中已上传文件进行下载
    handleFilePreview(file) {
      location.href=file.url;
    },
    // 节点单击事件
    handleNodeClick(data) {
      this.queryParams.deptId = data.id;
      this.getList();
    },
    setIcon(val) {
      if (val) {
        return "el-icon-check";
      } else {
        return "el-icon-time";
      }
    },
    setColor(val) {
      if (val) {
        return "#2bc418";
      } else {
        return "#b3bdbb";
      }
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.userMultipleSelection = selection
    },
    toggleSelection(selection) {
      if (selection && selection.length > 0) {
        this.$nextTick(()=> {
          selection.forEach(item => {
            let row = this.userList.find(k => k.userId === item.userId);
            this.$refs.userTable.toggleRowSelection(row);
          })
        })
      } else {
        this.$nextTick(() => {
          this.$refs.userTable.clearSelection();
        });
      }
    },
    // 关闭标签
    handleClose(type, tag) {
      let userObj = this.userMultipleSelection.find(item => item.userId === tag.id);
      this.userMultipleSelection.splice(this.userMultipleSelection.indexOf(userObj), 1);
      if (type === 'copy') {
        this.copyUser = this.userMultipleSelection;
        // 设置抄送人ID
        if (this.copyUser && this.copyUser.length > 0) {
          const val = this.copyUser.map(item => item.id);
          this.taskForm.copyUserIds = val instanceof Array ? val.join(',') : val;
        } else {
          this.taskForm.copyUserIds = '';
        }
      } else if (type === 'next') {
        this.nextUser = this.userMultipleSelection;
        // 设置抄送人ID
        if (this.nextUser && this.nextUser.length > 0) {
          const val = this.nextUser.map(item => item.id);
          this.taskForm.nextUserIds = val instanceof Array ? val.join(',') : val;
        } else {
          this.taskForm.nextUserIds = '';
        }
      }
    },
    /** 流程变量赋值 */
    handleCheckChange(val) {
      if (val instanceof Array) {
        this.taskForm.values = {
          "approval": val.join(',')
        }
      } else {
        this.taskForm.values = {
          "approval": val
        }
      }
    },
    /** 获取流程变量内容 */
    processVariables(taskId) {
      console.log("processVariables taskId",taskId);
      if (taskId) {
        getProcessVariables(taskId).then(res => {
          console.log("getProcessVariables res=",res);
          if(res.code == 200) {
            if(res.data.hasOwnProperty('dataId') && res.data.dataId) {
              this.customForm.formId = res.data.dataId;
              // 流程任务重获取变量表单
              this.getProcessDetails(this.taskForm.procInsId, this.taskForm.taskId, res.data.dataId);
              this.loadIndex = this.taskForm.procInsId;
              if(this.processed) {
                this.activeName = "approval";
              }
              else {
                this.activeName = "form";
              }
            }
            else {
              // 流程任务重获取变量表单
              this.getProcessDetails(this.taskForm.procInsId, this.taskForm.taskId, "");
              this.loadIndex = this.taskForm.procInsId;
              if(this.processed) {
                this.activeName = "approval";
              }
              else {
                this.activeName = "form";
                // 回填数据,这里主要是处理文件列表显示,临时解决，以后应该在formdesigner里完成
                this.processFormList.forEach((item, i) => {
                  if (item.hasOwnProperty('list')) {
                    this.fillFormData(item.list, item)
                    // 更新表单
                    this.key = +new Date().getTime()
                  }
                });
              }
            }
          }
        });
      }
    },
    getProcessDetails(procInsId, taskId, dataId) {
      const params = {procInsId: procInsId, taskId: taskId, dataId: dataId}
      detailProcess(params).then(res => {
        console.log("detailProcess res=",res);
        const data = res.data;
        this.xmlData = data.bpmnXml;
        this.processFormList = data.processFormList;
        if(this.processFormList.length == 1 &&
           this.processFormList[0].formValues.hasOwnProperty('routeName')) {
           this.customForm.disabled = true;
           this.customForm.visible = true;
           this.customForm.formComponent = this.getFormComponent(this.processFormList[0].formValues.routeName).component;
           this.customForm.model = this.processFormList[0].formValues.formData;
           this.customForm.customFormData = this.processFormList[0].formValues.formData;
           console.log("detailProcess customForm",this.customForm);
        }
        else {
          this.processFormList.forEach((item, index) => {
            this.formVal[index] = JSON.stringify(item.formValues);
            this.formViewData[index] = JSON.stringify(item);
          });
          this.taskFormOpen = data.existTaskForm;
          if (this.taskFormOpen) {
            this.taskFormData = data.taskFormData;
          }
          this.formOpen = true
        }
        this.historyProcNodeList = data.historyProcNodeList;
        this.finishedInfo = data.flowViewer;
      })
    },
    onSelectCopyUsers() {
      this.userMultipleSelection = this.copyUser;
      this.onSelectUsers('添加抄送人', 'copy')
    },
    onSelectNextUsers() {
      this.userMultipleSelection = this.nextUser;
      this.onSelectUsers('指定审批人', 'next')
    },
    onSelectUsers(title, type) {
      this.userData.title = title;
      this.userData.type = type;
      this.getTreeSelect();
      this.getList()
      this.userData.open = true;
    },
    /** 通过任务 */
    handleComplete() {
      // 校验表单
      const taskFormRef = this.$refs.taskFormParser;
      const isExistTaskForm = taskFormRef !== undefined;
      // 若无任务表单，则 taskFormPromise 为 true，即不需要校验
      const taskFormPromise = !isExistTaskForm ? true : new Promise((resolve, reject) => {
        taskFormRef.$refs[taskFormRef.formConfCopy.formRef].validate(valid => {
          valid ? resolve() : reject()
        })
      });
      const approvalPromise = new Promise((resolve, reject) => {
        this.$refs['taskForm'].validate(valid => {
          valid ? resolve() : reject()
        })
      });
      Promise.all([taskFormPromise, approvalPromise]).then(() => {
        if (isExistTaskForm) {
          this.taskForm.variables = taskFormRef[taskFormRef.formConfCopy.formModel]
        }
        complete(this.taskForm).then(response => {
          this.$modal.msgSuccess(response.msg);
          this.goBack();
        });
      })
    },
    /** 委派任务 */
    handleDelegate() {
      this.$refs["taskForm"].validate(valid => {
        if (valid) {
          this.userData.type = 'delegate';
          this.userData.title = '委派任务'
          this.userData.open = true;
          this.getTreeSelect();
        }
      })
    },
    /** 转办任务 */
    handleTransfer(){
      this.$refs["taskForm"].validate(valid => {
        if (valid) {
          this.userData.type = 'transfer';
          this.userData.title = '转办任务';
          this.userData.open = true;
          this.getTreeSelect();
        }
      })
    },
    /** 拒绝任务 */
    handleReject() {
      this.$refs["taskForm"].validate(valid => {
        if (valid) {
          const _this = this;
          this.$modal.confirm('拒绝审批单流程会终止，是否继续？').then(function() {
            return rejectTask(_this.taskForm);
          }).then(res => {
            this.$modal.msgSuccess(res.msg);
            this.goBack();
          });
        }
      });
    },
    changeCurrentUser(val) {
      this.currentUserId = val.userId
    },
    /** 返回页面 */
    goBack() {
      // 关闭当前标签页并返回上个页面
      this.$tab.closePage(this.$route)
      this.$router.back()
    },
    /** 接收子组件传的值 */
    getData(data) {
      if (data) {
        const variables = [];
        data.fields.forEach(item => {
          let variableData = {};
          variableData.label = item.__config__.label
          // 表单值为多个选项时
          if (item.__config__.defaultValue instanceof Array) {
            const array = [];
            item.__config__.defaultValue.forEach(val => {
              array.push(val)
            })
            variableData.val = array;
          } else {
            variableData.val = item.__config__.defaultValue
          }
          variables.push(variableData)
        })
        this.variables = variables;
      }
    },
    submitUserData() {
      let type = this.userData.type;
      if (type === 'copy' || type === 'next') {
        if (!this.userMultipleSelection || this.userMultipleSelection.length <= 0) {
          this.$modal.msgError("请选择用户");
          return false;
        }
        let userIds = this.userMultipleSelection.map(k => k.userId);
        if (type === 'copy') {
          // 设置抄送人ID信息
          this.copyUser = this.userMultipleSelection;
          this.taskForm.copyUserIds = userIds instanceof Array ? userIds.join(',') : userIds;
        } else if (type === 'next') {
          // 设置下一级审批人ID信息
          this.nextUser = this.userMultipleSelection;
          this.taskForm.nextUserIds = userIds instanceof Array ? userIds.join(',') : userIds;
        }
        this.userData.open = false;
      } else {
        if (!this.taskForm.comment) {
          this.$modal.msgError("请输入审批意见");
          return false;
        }
        if (!this.currentUserId) {
          this.$modal.msgError("请选择用户");
          return false;
        }
        this.taskForm.userId = this.currentUserId;
        if (type === 'delegate') {
          delegate(this.taskForm).then(res => {
            this.$modal.msgSuccess(res.msg);
            this.goBack();
          });
        }
        if (type === 'transfer') {
          transfer(this.taskForm).then(res => {
            this.$modal.msgSuccess(res.msg);
            this.goBack();
          });
        }
      }

    },
    /** 可退回任务列表 */
    handleReturn() {
      this.$refs['taskForm'].validate(valid => {
        if (valid) {
          this.returnTitle = "退回流程";
          returnList(this.taskForm).then(res => {
            this.returnTaskList = res.data;
            this.taskForm.values = null;
            this.returnOpen = true;
          })
        }
      });

    },
    /** 提交退回任务 */
    submitReturn() {
      this.$refs["taskForm"].validate(valid => {
        if (valid) {
          if (!this.taskForm.targetKey) {
            this.$modal.msgError("请选择退回节点！");
          }
          returnTask(this.taskForm).then(res => {
            this.$modal.msgSuccess(res.msg);
            this.goBack()
          });
        }
      });
    }
  }
};
</script>
<style lang="scss" scoped>
.clearfix:before,
.clearfix:after {
  display: table;
  content: "";
}
.clearfix:after {
  clear: both
}

.box-card {
  width: 100%;
  margin-bottom: 20px;
}

.el-tag + .el-tag {
  margin-left: 10px;
}

.el-row {
  margin-bottom: 20px;
  &:last-child {
    margin-bottom: 0;
  }
}
.el-col {
  border-radius: 4px;
}

.button-new-tag {
  margin-left: 10px;
}
</style>
