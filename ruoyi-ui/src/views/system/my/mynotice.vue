<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="公告标题" prop="noticeTitle">
        <el-input
          v-model="queryParams.noticeTitle"
          placeholder="请输入公告标题"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="操作人员" prop="createBy">
        <el-input
          v-model="queryParams.createBy"
          placeholder="请输入操作人员"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="类型" prop="noticeType">
        <el-select v-model="queryParams.noticeType" placeholder="公告类型" clearable>
          <el-option
            v-for="dict in dict.type.sys_notice_type"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click=""
          v-hasPermi="['system:noticeSend:list']"
        >全部标注已读</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="noticeList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="序号" align="center" prop="noticeId" width="100" />
      <el-table-column
        label="消息标题"
        align="center"
        prop="noticeTitle"
        :show-overflow-tooltip="true"
      />
      <el-table-column label="消息类型" align="center" prop="noticeType" width="100">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.sys_notice_type" :value="scope.row.noticeType"/>
        </template>
      </el-table-column>
      <el-table-column label="发布人" align="center" prop="sender" width="100" />
      <el-table-column label="发布时间" align="center" prop="sendTime" width="100" />
      <el-table-column label="优先级" align="center" prop="priority" width="100">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.sys_priority" :value="scope.row.priority"/>
        </template>
      </el-table-column>
      <el-table-column label="阅读状态" align="center" prop="readFlag" width="100">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.sys_readflag" :value="scope.row.readFlag"/>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-view"
            @click="handleSee(scope.row)"
            v-hasPermi="['system:noticeSend:list']"
          >查看</el-button>

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
     <show-notice ref="ShowNotice" @ok="modalFormOk"></show-notice>
     <dynamic-notice ref="showDynamNotice" :path="openPath" :formData="formData" /
  </div>
</template>

<script>
  import ShowNotice from '@/components/HeaderNotice/ShowNotice'
  import DynamicNotice from '@/components/HeaderNotice/DynamicNotice'
  import { listNotice, getMyNoticeSend, updateUserIdAndNotice, getNotice, updateNotice } from "@/api/system/notice";
  
export default {
  name: "MyNotice",
  components: {
    DynamicNotice,
    ShowNotice,
  },
  dicts: ['sys_readflag', 'sys_notice_type','sys_priority'],
  data() {
    return {
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
      // 公告表格数据
      noticeList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        noticeSendModel: {},
        pageNum: 1,
        pageSize: 10
      },

      // 表单参数
      form: {},
      // 表单校验
      rules: {
        noticeTitle: [
          { required: true, message: "公告标题不能为空", trigger: "blur" }
        ],
        noticeType: [
          { required: true, message: "公告类型不能为空", trigger: "change" }
        ]
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询公告列表 */
    getList() {
      this.loading = true;
      console.log("this.queryParams",this.queryParams);
      getMyNoticeSend(this.queryParams).then(res => {
        console.log("getMyNoticeSend res", res);
        this.noticeList = res.data.records;
        this.total = res.data.total;
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
        noticeId: undefined,
        noticeTitle: undefined,
        noticeType: undefined,
        noticeContent: undefined,
        status: "0"
      };
      this.resetForm("form");
    },
    handleSee(record) {
      console.log("handleSee record",record);
      if(record.readFlag == '0')
      {
        updateUserIdAndNotice({
          noticeId: record.noticeId
        }).then((res) => {
          if (res.code == 200) {
            this.getList();
          }
        });
      }  
      this.hovered = false;
      if (record.openType === 'component') {
        this.openPath = record.openPage;
        this.formData = {
          id: record.busId
        };
        this.$refs.showDynamNotice.detail(record.openPage);
      } else {
        this.$refs.ShowNotice.detail(record);
      } 
    },
    modalFormOk() {},
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
      this.ids = selection.map(item => item.noticeId)
      this.single = selection.length!=1
      this.multiple = !selection.length
    }
  }
};
</script>
