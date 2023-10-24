<style lang="less">
</style>
<template>
    <span>
      <a-button :type="btnType" @click="applySubmit()" :loading="submitLoading">{{text}}</a-button>
      <a-modal :z-index="100" :title="firstInitiatorTitle" @cancel="firstInitiatorOpen = false" :visible.sync="firstInitiatorOpen"
        :width="'50%'" append-to-body>
         <a-descriptions bordered layout="vertical">
           <a-descriptions-item :span="3">
                 <a-badge status="processing" text="选择提醒" />
            </a-descriptions-item>
            <a-descriptions-item label="重新发起新流程按钮" labelStyle="{ color: '#fff', fontWeight: 'bold', fontSize='18px'}">
              重新发起新流程会删除之前发起的任务,重新开始.
            </a-descriptions-item>
            <a-descriptions-item label="继续发起老流程按钮">
              继续发起流程就在原来流程基础上继续流转.
            </a-descriptions-item>
         </a-descriptions>
        <span slot="footer" class="dialog-footer">
          <el-button type="primary" @click="ReStartByDataId(true)">重新发起新流程</el-button>
          <el-button type="primary" @click="ReStartByDataId(false)">继续发起老流程</el-button>
          <el-button @click="firstInitiatorOpen = false">取 消</el-button>
        </span>
      </a-modal>
    </span>
</template>

<script>
  import {
    startByDataId,
    isFirstInitiator,
    deleteActivityAndJoin
  } from "@/api/workflow/process";

  export default {
    name: 'ActApplyBtn',
    components: {},
    props: {
      btnType: {
        type: String,
        default: 'link',
        required: false
      },
      /**/
      dataId: {
        type: String,
        default: '',
        required: true
      },
      serviceName: {
        type: String,
        default: '',
        required: true
      },
      variables: {
        type: Object,
        default: {},
      },
      text: {
        type: String,
        default: '提交申请',
        required: false
      }

    },
    data() {
      return {
        modalVisible: false,
        submitLoading: false,
        form: {},
        firstInitiatorOpen: false,
        firstInitiatorTitle: '',
      };
    },
    created() {},
    watch: {},
    methods: {
      ReStartByDataId(isNewFlow) {
          if(isNewFlow) {
            this.submitLoading = true;
            deleteActivityAndJoin(this.dataId,this.variables)
            .then(res => {
              if (res.success && res.result) { //若删除成功
                var params = Object.assign({
                  dataId: this.dataId
                }, this.variables);
                startByDataId(this.dataId, this.serviceName, params)
                  .then(res => {
                    if (res.success) {
                      this.firstInitiatorOpen = false;
                      this.$message.success(res.message);
                      this.$emit('success');
                    } else {
                      this.$message.error(res.message);
                    }
                  })
              }
            })
            .finally(() => (this.submitLoading = false));
          }
          else {//继续原有流程流转，跳到流程处理界面上
            console.log("this.variables",this.variables);
            this.$router.push({ path: '/flowable/task/record/index',
              query: {
                procInsId: this.variables.processInstanceId,
                deployId: this.variables.deployId,
                taskId: this.variables.taskId,
                businessKey: this.dataId,
                nodeType: "",
                category: "zdyyw",
                finished: true
              }})
          }
      },
      applySubmit() {
        if (this.dataId && this.dataId.length < 1) {
          this.error = '必须传入参数dataId';
          this.$message.error(this.error);
          return;
        }
        if (this.serviceName && this.serviceName.length < 1) {
          this.error = '必须传入参数serviceName';
          this.$message.error(this.error);
          return;
        } else {
          this.error = '';
        }
        //对于自定义业务，判断是否是驳回或退回的第一个发起人节点
        this.submitLoading = true;
        console.log("applySubmit this.dataId",this.dataId);
        console.log("applySubmit this.variables",this.variables);
        isFirstInitiator(this.dataId, this.variables)
          .then(res => {
            if (res.success && res.result) { //若是，弹出窗口选择重新发起新流程还是继续老流程
              this.firstInitiatorTitle = "根据自己需要进行选择"
              this.firstInitiatorOpen = true;
            }
            else {
              this.submitLoading = true;
              var params = Object.assign({
                dataId: this.dataId
              }, this.variables);
              startByDataId(this.dataId, this.serviceName, params)
                .then(res => {
                  console.log("startByDataId res",res);
                  if (res.code == 200 ) {
                    this.$message.success(res.msg);
                    this.$emit('success');
                  } else {
                    this.$message.error(res.msg);
                  }
                })
                .finally(() => (this.submitLoading = false));
            }
          })
          .finally(() => (this.submitLoading = false));
      }
    }

  };
</script>
