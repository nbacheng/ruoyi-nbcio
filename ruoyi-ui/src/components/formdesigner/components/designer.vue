<template>
  <!--中间面板-->
  <div class="center-board" >
    <div class="action-bar">
      <el-button icon="el-icon-plus" type="text" @click="save">
        保存
      </el-button>
      <el-button icon="el-icon-view" type="text" @click="preview">
        预览
      </el-button>
      <el-button icon="el-icon-view" type="text" @click="view">
        查看
      </el-button>
      <el-button icon="el-icon-tickets" type="text" @click="viewJSON">
        JSON
      </el-button>
      <el-button icon="el-icon-s-tools" type="text" @click="setting">
        设置
      </el-button>
      <el-button class="delete-btn" icon="el-icon-delete-solid" type="text" @click="clear">
        清空
      </el-button>
      <el-button icon="el-icon-question" type="text" @click="help">
        帮助
      </el-button>
    </div>
    <el-scrollbar class="center-scrollbar">
      <el-row class="center-board-row" :gutter="formConf.gutter">
        <el-form
                :size="formConf.size"
                :label-position="formConf.labelPosition"
                :disabled="formConf.disabled"
                :label-width="formConf.labelWidth + 'px'"
                class="design-form"
        >
          <draggable
                  class="drawing-board center-board-row"
                  :list="list"
                  :animation="100"
                  group="componentsGroup"
          >
            <template v-for="element in list">
              <el-row
                      :gutter="element.gutter"
                      class="drawing-item"
              >
                <design-item
                        :model="element"
                        :activeItem="activeItem"
                        @rowItemRollBack="handlerRollBack"
                        @onActiveItemChange="handlerActiveItemChange"
                        @copyItem="handlerItemCopy"
                        @deleteItem="handlerItemDelete"
                />
              </el-row>
            </template>
          </draggable>
          <div v-show="infoShow" class="empty-info">
            <el-empty description="从左侧拖拽添加控件"></el-empty>
          </div>
        </el-form>
      </el-row>
    </el-scrollbar>
    <config-panel :activeItem="activeItem" :itemList="list" :formConf="formConf"/>
    <!-- 设计器配置弹出框 -->
    <el-dialog  :visible.sync="formConfVisible" width="50%" top="30px" :center="true">
      <el-tabs v-model="activeName">
        <el-tab-pane label="表单配置" name="formConf">
          <el-form ref="formConf" :model="formConf" label-width="100px">
            <el-form-item label="表单名">
              <el-input class="input" v-model="formConf.formRef"></el-input>
            </el-form-item>
            <el-form-item label="表单模型">
              <el-input class="input" v-model="formConf.formModel"></el-input>
            </el-form-item>
            <el-form-item label="校验模型">
              <el-input class="input" v-model="formConf.formRules"></el-input>
            </el-form-item>
            <el-form-item label="表单尺寸">
              <el-radio-group v-model="formConf.size">
                <el-radio-button label="medium">中等</el-radio-button>
                <el-radio-button label="small">较小</el-radio-button>
                <el-radio-button label="mini">迷你</el-radio-button>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="标签对齐">
              <el-radio-group v-model="formConf.labelPosition">
                <el-radio-button label="right">右对齐</el-radio-button>
                <el-radio-button label="left">左对齐</el-radio-button>
                <el-radio-button label="top">顶部对齐</el-radio-button>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="标签宽度">
              <el-input-number v-model="formConf.labelWidth"  :min="60" :max="140"></el-input-number>
            </el-form-item>
            <el-form-item label="栅格间隔">
              <el-input-number v-model="formConf.gutter"  :min="0" :max="30"></el-input-number>
            </el-form-item>
            <el-form-item label="动态表格支持组件高亮显示">
              <el-switch v-model="formConfig.dynamicTableAllowed"></el-switch>
            </el-form-item>
            <el-form-item label="禁用表单">
              <el-switch v-model="formConf.disabled"></el-switch>
            </el-form-item>
            <el-form-item label="表单样式表">
              <css-codemirror ref="cssCodemirror"  :formConf="formConf"></css-codemirror>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        <!-- <el-tab-pane label="提交前" name="fourth">开发中...</el-tab-pane> -->
      </el-tabs>
      <span slot="footer" class="dialog-footer">
            <el-button type="primary" @click="handlerSaveFormConf">确 定</el-button>
        </span>
    </el-dialog>
    <el-dialog :visible.sync="previewVisible" width="70%" title="预览">
      <preview :itemList="itemList"  :formConf="formConf" v-if="previewVisible"/>
    </el-dialog>
    <el-dialog :visible.sync="JSONVisible" width="70%" title="JSON" center :close-on-click-modal="false">
      <codemirror v-model="viewCode" :options="options"/>
      <span slot="footer" class="dialog-footer">
        <el-button type="primary" @click="handlerSetJson()">确 定</el-button>
      </span>
    </el-dialog>
    <!--表单配置详情  add by nbacheng 2022-09-05-->
    <el-dialog :title="formTitle" :visible.sync="formOpen" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="表单名称" prop="formName">
          <el-input v-model="form.formName" placeholder="请输入表单名称" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>
<script>
  import draggable from "vuedraggable";
  import configPanel from './configPanel'
  import designItem from './designItem'
  import {getSimpleId} from "./utils/IdGenerate";
  import { isLayout, isTable, inTable,jsonClone } from "./utils/index";
  import formConf from "./custom/formConf";
  import preview from "./preview";
  import {codemirror} from 'vue-codemirror';
  // 核心样式
  import 'codemirror/lib/codemirror.css';
  // 引入主题后还需要在 options 中指定主题才会生效
  import 'codemirror/theme/dracula.css';
  import 'codemirror/mode/javascript/javascript'
  import cssCodemirror from '../components/custom/css/cssCodemirror'
  import {addForm, updateForm} from "@/api/workflow/form";

  export default {
    name:"designer",
    components:{
      draggable,
      configPanel,
      designItem,
      preview,
      codemirror,
      cssCodemirror
    },
    props:{
      list: {
        type: Array,
        default:[]
      },
      formConfig:{
      type:Object,
      default:formConf
      },
      inform: {
        type:Array,
        default:[]
      },
      queryId: {
        type:String,
        default:''
      }
    },
    provide(){
      return{
        getContext:this
      }
    },
    data() {
      return {
        formConf:formConf,
        activeItem:{},
        lastActiveItem:{},
        formConfVisible:false,
        previewVisible:false,
        JSONVisible:false,
        itemList:[],
        activeName:'formConf',
        editorCode:'',
        viewCode:'',
         formOpen: false,
         formTitle: "",
         // 表单参数
         form: {
           formId: null,
           formName: null,
           content: null,
           remark: null
         },
         // 表单校验
         rules: {},
        // 默认配置
        options: {
          tabSize: 2, // 缩进格式
          theme: 'dracula', // 主题，对应主题库 JS 需要提前引入
          lineNumbers: true, // 显示行号
          line: true,
          styleActiveLine: true, // 高亮选中行
          hintOptions: {
            completeSingle: true // 当匹配只有一项的时候是否自动补全
          }
        }
      }
    },
    mounted() {
    },
    methods: {
    save(){// add by nbacheng 2022-09-05
      console.log("save inform=",this.inform);
      if (this.inform.formId) {
        this.form.formId = this.inform.formId;
        this.form.formName = this.inform.formName;
        this.form.remark = this.inform.remark;
      }
      /** 表单保存基本信息 */
       this.formData = {
         list: this.list,
         config: this.formConf
       }
       console.log("save this.formData=",this.formData);
       this.form.content = JSON.stringify(this.formData);
       this.formOpen = true;
       if (this.inform.formId) {
         this.formTitle = "修改表单";
       }
       else {
         this.formTitle = "添加表单";
       }
       console.log("save form=",this.form);
    },
      changeFormConfig(formConfig){
        this.formConf = formConfig;
      },
      preview(){
        const clone = JSON.parse(JSON.stringify(this.list))
        this.itemList = clone;
        this.previewVisible= true;
      },
      viewJSON(){
        this.viewCode = this.code;
        this.JSONVisible = true;
      },
      // 表单重置
    reset() {
      this.form = {
        formId: null,
        formName: null,
        content: null,
        remark: null
      };
      this.resetForm("form");
    },
    // 取消按钮
    cancel() {
      this.formOpen = false;
      this.reset();
    },
    /** 保存表单信息 */
    submitForm(){
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.formId != null) {
            updateForm(this.form).then(response => {
              this.$message.success("修改成功");
            });
          } else {
            addForm(this.form).then(response => {
              this.$message.success("新增成功");
            });
          }
          this.list = []
          this.idGlobal = 100
          this.formOpen = false;
          // 关闭当前标签页并返回上个页面
          this.$store.dispatch("tagsView/delView", this.$route);
          this.$router.go(-1)
        }
      });
    },
      view(){
        localStorage.setItem("formValue",this.code);
        window.open('#/view');
      },
      setting(){
        this.formConfVisible = true;
      },
      clear(){
        this.$confirm('此操作将清空整个表单,是否继续?').then(() => {
          this.$emit('clear');
        })
      },
      help(){
        window.open('https://gitee.com/wurong19870715/formDesigner')
      },
      handlerActiveItemChange(obj){
        this.lastActiveItem = this.activeItem;
        this.activeItem = obj;
      },
      handlerItemCopy(origin,parent){
        if(isLayout(origin)){ //row
          const clone = jsonClone(origin);
          const uId = "row_"+getSimpleId();
          console.log(uId);
          clone.id = uId;
          clone._id = uId;
          clone.columns.map((column)=>{
            let itemList = [];
            column.list.map((item)=>{
              const cloneItem = jsonClone(item);
              const uId = "fd_"+getSimpleId();
              cloneItem.id = uId;
              cloneItem._id = uId;
              itemList.push(cloneItem);
            })
            column.list = [];
            column.list = itemList;
          })
          this.list.push(clone);
          this.handlerActiveItemChange(clone);
        }else if(isTable(origin)){  //表格布局
          const clone = jsonClone(origin);
          const uId = "table_"+getSimpleId();
          clone.id = uId;
          clone._id = uId;
          clone.layoutArray.map((tr)=>{
            tr.map(td=>{
              let itemList = [];
              td.id=getSimpleId();
              td.columns.map((item,i)=>{
                const cloneItem = jsonClone(item);
                const uId = "fd_"+getSimpleId();
                cloneItem.id = uId;
                cloneItem._id = uId;
                itemList.push(cloneItem);
              })
              td.columns = [];
              td.columns = itemList;
            });
          })
          this.list.push(clone);
          this.handlerActiveItemChange(clone);
        }else{  //如果是普通组件，需要判断他是否再布局组件下。
          if(parent){
            if (inTable(parent)) { //增加表格组件的支持
              if (parent.columns.some(item => item.id === origin.id)) {
                const clone = jsonClone(origin);
                const uId = "fd_" + getSimpleId();
                clone.id = uId;
                clone._id = uId;
                parent.columns.push(clone);
                this.handlerActiveItemChange(clone);
              }
            } else {
              parent.columns.map((column) => {
                if (column.list.some(item => item.id === origin.id)) {
                  const clone = jsonClone(origin);
                  const uId = "fd_" + getSimpleId();
                  clone.id = uId;
                  clone._id = uId;
                  column.list.push(clone);
                  this.handlerActiveItemChange(clone);
                }
              })
            }

          }else{
            const clone = jsonClone(origin);
            const uId = "fd_"+getSimpleId();
            clone.id = uId;
            clone._id = uId;
            this.list.push(clone);
            this.handlerActiveItemChange(clone);
          }
        }
      },
      handlerItemDelete(origin,parent){
        if (isLayout(origin) || isTable(origin)){ //如果是布局组件,则直接删除
          const index = this.list.findIndex(item=>item.id === origin.id);
          this.list.splice(index,1);
        }else{  //如果不是布局组件，则先判断是不是再布局内部，如果不是，则直接删除就可以，如果是，则要在布局内部删除
          if(parent){
            if (inTable(parent)){ //增加表格组件的支持
              const colIndex = parent.columns.findIndex(item => item.id === origin.id);
              if (colIndex > -1) {
                parent.columns.splice(colIndex, 1);
              }
            }else{
              parent.columns.map((column) => {
                const colIndex = column.list.findIndex(item => item.id === origin.id);
                if (colIndex > -1) {
                  column.list.splice(colIndex, 1);
                }
              })
            }

          }else{
            const index = this.list.findIndex(item=>item.id === origin.id);
            this.list.splice(index,1);

          }
        }
      },
      handlerSaveFormConf(){
        this.$refs.cssCodemirror.setClassStyle()
        this.formConfVisible = false
      },
      handlerRollBack(rowItem,oldIndex){  //还原
        this.list.splice(oldIndex,0,rowItem);
      },
      handlerSetJson(){
        this.$emit('updateJSON',this.viewCode);
        this.JSONVisible = false;
      },
    },
    computed:{
      infoShow() {
        return this.list.length<1;
      },
      code() {
        let json = {};
        json.config = this.formConf;
        json.list = this.list;
        return JSON.stringify(json,null,4);
      }
    },
    watch: {
      activeItem (newValue,oldValue) {
        this.lastActiveItem = oldValue;
      }
    }
  }

</script>
<style  scoped>
  .el-rate{
    display:inline-block;
  }
  .center-scrollbar >>> .el-scrollbar__bar.is-horizontal {
    display: none;
  }
  .center-scrollbar >>> .el-scrollbar__wrap{
    overflow-x: hidden;
  }
  .empty-info >>> .el-empty__description p{
    color: #ccb1ea;
    font-size:16px;
  }
  .drawing-board >>> .el-radio.is-bordered+.el-radio.is-bordered{
    margin-left:0px;
  }
  .drawing-board >>> .el-checkbox.is-bordered+.el-checkbox.is-bordered{
    margin-left:0px;
  }
</style>
<style lang="scss">
  @import "./style/designer.scss";
</style>
<style>
  @import "./style/designer.css";
</style>
