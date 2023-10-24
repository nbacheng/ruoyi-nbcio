<template>
  <div class="app-container">
  <!-- 显示DEMO数据 -->
    <el-form ref="form" :model="form" label-width="80px" :disabled="disabled">
      <el-form-item label="用户账号" prop="userName">
        <el-input v-model="form.userName" placeholder="" />
      </el-form-item>
      <el-form-item label="用户昵称" prop="nickName">
        <el-input v-model="form.nickName" placeholder="" />
      </el-form-item>
      <el-form-item label="用户邮箱" prop="email">
        <el-input v-model="form.email" placeholder="" />
      </el-form-item>
      <el-form-item label="头像地址" prop="avatar">
        <el-input v-model="form.avatar" placeholder="" />
      </el-form-item>
      <el-form-item label="备注" prop="remark">
        <el-input v-model="form.remark" type="textarea" placeholder="" />
      </el-form-item>
    </el-form>
    <!--<div slot="footer" class="dialog-footer">
      <el-button :loading="buttonLoading" type="primary" @click="submitForm">确 定</el-button>
      <el-button @click="cancel">取 消</el-button>
    </div> -->
  </div>
</template>

<script>
  import {getDemo, delDemo, addDemo, updateDemo } from "@/api/workflow/demo";

  export default {
    name: "wfDemo",
    components: {
    },
    props: {
      //表单禁用
      disabled: {
        type: Boolean,
        default: false,
        required: false
      },
      /*流程自定义表单数据*/
      customFormData:{
         type:Object,
         default:()=>{return {}},
         required:false
      },
    },
    data() {
      return {
        // 按钮loading
        buttonLoading: false,
        // 弹出层标题
        title: "",
        // 是否显示弹出层
        open: true,
        // 表单参数
        form: {},
      }
    },
    created() {
      //流程调用自定义表单的传入参数
      if(this.customFormData != null) {
        console.log("this.customFormData=",this.customFormData);
        this.form = this.customFormData;
      }
      //this.handleView(this.form);
    },
    methods: {
      // 取消按钮
      cancel() {
        this.open = false;
        this.reset();
      },
      // 表单重置
      reset() {
        this.form = {
          demoId: undefined,
          userName: undefined,
          nickName: undefined,
          email: undefined,
          avatar: undefined,
          status: undefined,
          delFlag: undefined,
          createBy: undefined,
          createTime: undefined,
          updateBy: undefined,
          updateTime: undefined,
          remark: undefined
        };
        this.resetForm("form");
      },
      /** 新增按钮操作 */
      handleAdd() {
        this.reset();
        this.open = true;
        this.title = "添加DEMO";
      },
      /** 查看按钮操作 */
      handleView(row) {
        this.loading = true;
        this.reset();
        const demoId = row.demoId || this.ids
        getDemo(demoId).then(response => {
          this.loading = false;
          this.form = response.data;
          this.open = true;
          this.title = "DEMO详情";
        });
      },
      /** 修改按钮操作 */
      handleUpdate(row) {
        this.loading = true;
        this.reset();
        const demoId = row.demoId || this.ids
        getDemo(demoId).then(response => {
          this.loading = false;
          this.form = response.data;
          this.open = true;
          this.title = "修改DEMO";
        });
      },
      /** 提交按钮 */
      submitForm() {
        this.$refs["form"].validate(valid => {
          if (valid) {
            this.buttonLoading = true;
            if (this.form.demoId != null) {
              updateDemo(this.form).then(response => {
                this.$modal.msgSuccess("修改成功");
                this.open = false;
                this.getList();
              }).finally(() => {
                this.buttonLoading = false;
              });
            } else {
              addDemo(this.form).then(response => {
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
    }
  }
</script>

<style>
</style>
