<style lang="less">
</style>
<template>
  <div class="search">
    <el-tabs tab-position="top" v-model="activeName" :value="processed === true ? 'approval' : 'form'" @tab-click="changeTab">
      <el-tab-pane label="表单信息" name="form">
        <div v-if="customForm.visible"> <!-- 自定义表单 -->
            <component ref="refCustomForm" :disabled="customForm.disabled" v-bind:is="customForm.formComponent" :model="customForm.model"
                        :customFormData="customForm.customFormData" :isNew = "customForm.isNew"></component>
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
  </div>
</template>

<script>
import {detailProcessByDataId} from "@/api/workflow/process";
import ProcessViewer from '@/components/ProcessViewer'
import {
    flowableMixin
  } from '@/views/workflow/mixins/flowableMixin'

export default {
  name: 'HistoricDetail',
  mixins: [flowableMixin],
  components: {
    ProcessViewer,
  },
    props: {
    /**/
        dataId: {
            type: String,
            default: '',
            required: true
        }
    },
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
          processed: false,
          activeName:'form', //获取当然tabname
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
          fileDisplay: false, // formdesigner是否显示上传的文件控件
          fileList: [], //表单设计器上传的文件列表
        };
    },
    created() {
        this.init();
    },
    watch: {
      dataId: function(newval, oldName) {
            this.init();
        }
    },

    methods: {
      init() {
       // 获取流程变量
       this.detailProcesssByDataId(this.dataId);
      },
      detailProcesssByDataId(dataId) {
        const params = {dataId: dataId}
        detailProcessByDataId(params).then(res => {
          console.log("detailProcessByDataId res=",res);
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
          this.historyProcNodeList = data.historyProcNodeList;
          this.finishedInfo = data.flowViewer;
        })
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
        }
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
