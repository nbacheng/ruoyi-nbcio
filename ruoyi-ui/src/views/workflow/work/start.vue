<template>
  <div class="app-container">
    <el-card class="box-card">
      <div slot="header" class="clearfix">
        <span>发起流程</span>
      </div>
      <el-col :span="18" :offset="3" v-if="formOpen">
        <form-builder ref="formBuilder" v-model="formVal" :buildData="formCode" />
        <div style="margin-bottom:15px;text-align:center">
            <el-button type="primary" class="button" @click="submitForm">提交</el-button>
        </div>
      </el-col>
      <!--初始化流程加载显示formdesigner表单-->
      <el-col :span="18" :offset="3" v-if="formViewOpen">
        <div class="test-form">
          <form-viewer ref="formView" v-model="formVal" :buildData="formCode" />
        </div>
      </el-col>
    </el-card>
  </div>
</template>

<script>
import { getProcessForm, startProcess } from '@/api/workflow/process'
import Parser from '@/utils/generator/parser'
//for formdesigner
  import formBuilder from '@/components/formdesigner/components/formBuilder'
  import formViewer from '@/components/formdesigner/components/formViewer'

export default {
  name: 'WorkStart',
  components: {
    Parser,
    formBuilder,
    formViewer,
  },
  data() {
    return {
      definitionId: null,
      deployId: null,
      procInsId: null,
      formOpen: false,
      formData: {}, // formdesigner 默认表单数据
      formCode:'', //formdesigner 变量
      formVal:'',  //formdesigner 数据
      formViewOpen: false,  //是否显示formdesigner的输入后提交的表单
      formViewData: '',    //显示formdesigner的输入后提交的表单数据
    }
  },
  created() {
    this.initData();
  },
  methods: {
    initData() {
      this.deployId = this.$route.params && this.$route.params.deployId;
      this.definitionId = this.$route.query && this.$route.query.definitionId;
      this.procInsId = this.$route.query && this.$route.query.procInsId;
      getProcessForm({
        definitionId: this.definitionId,
        deployId: this.deployId,
        procInsId: this.procInsId
      }).then(res => {
        console.log("getProcessForm res=",res);
        if (res.data) {
          this.formData = res.data;
          this.formCode = JSON.stringify(res.data);
          this.formOpen = true
        }
      })
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
    /** 申请流程表单formdesigner数据提交 nbacheng2023-09-10 */
    submitForm() {
      this.$refs.formBuilder.validate();
      if(this.formVal !='') {
        this.formViewOpen = true;
        this.formOpen = false;
        const variables=JSON.parse(this.formVal);
        const formData = JSON.parse(this.formCode);
        formData.formValue = JSON.parse(this.formVal);

        if (this.definitionId) {
          variables.variables = formData;
          console.log("variables=", variables);
          // 启动流程并将表单数据加入流程变量
          startProcess(this.definitionId, JSON.stringify(variables)).then(res => {
            this.$modal.msgSuccess(res.msg);
            this.$tab.closeOpenPage({
              path: '/task/own'
            })
          })
        }
      }
    },

  }
}
</script>

<style lang="scss" scoped>
.form-conf {
  margin: 15px auto;
  width: 80%;
  padding: 15px;
}
</style>
